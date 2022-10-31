/*
 * Copyright (C) 2022 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl] }
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yaraifyapi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;
import yaraifyapi.json.JsonParser;
import yaraifyapi.model.YaraifyIdentifierFilter;
import yaraifyapi.model.YaraifyIdentifierResult;
import yaraifyapi.model.YaraifyMetadata;
import yaraifyapi.model.YaraifyYaraRuleMetadata;
import yaraifyapi.model.YaraifyTaskResult;
import yaraifyapi.network.YaraifyConnector;

/**
 * This class is the only required class to instantiate to connect with
 * Yaraify's API endpoints. Other classes in the relevant packages are used
 * within this class, and will be instantiated if need be.
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class YaraifyApi {

    /**
     * The connector object to make HTTP(S) connections to the API endpoint with
     */
    private YaraifyConnector connector;

    /**
     * The object to parse returned JSON objects with
     */
    private JsonParser jsonParser;

    /**
     * A valid Malpedia API key, which is used when querying the Yaraify
     * endpoints
     */
    private String malpediaApiKey;

    /**
     * A boolean which is true if a Malpedia key is provided, false if not
     */
    private boolean malpediaEnabled;

    /**
     * Creates an API instance with the given key, which can then be used to
     * interact with Yaraify's API endpoints
     *
     * @param yaraifyApiKey the API key for the service
     * @param malpediaApiKey A valid Malpedia API key, which is used when
     * querying the Yaraify endpoints
     */
    public YaraifyApi(String yaraifyApiKey, String malpediaApiKey) {
        this.jsonParser = new JsonParser();
        String apiUrl = "https://yaraify-api.abuse.ch/api/v1/";
        this.connector = new YaraifyConnector(yaraifyApiKey, apiUrl);
        this.malpediaApiKey = malpediaApiKey;
        this.malpediaEnabled = true;
    }

    /**
     * Creates an API instance with the given key, which can then be used to
     * interact with Yaraify's API endpoints
     *
     * @param yaraifyApiKey the API key for the service
     */
    public YaraifyApi(String yaraifyApiKey) {
        this.jsonParser = new JsonParser();
        String apiUrl = "https://yaraify-api.abuse.ch/api/v1/";
        this.connector = new YaraifyConnector(yaraifyApiKey, apiUrl);
        this.malpediaApiKey = null;
        this.malpediaEnabled = false;
    }

    /**
     * Checks if the given argument is between 1 and 1000. Yaraify's limit for
     * API requests is 1000 results. Any value higher than that will be capped
     * to 1000. Any value lower than or equal to zero will set the limit to the
     * default of 25.
     *
     * @param limit the limit to check
     * @return the given limit, unless the value is less than zero or more than
     * 1000
     */
    private int checkLimit(int limit) {
        if (limit > 1000) {
            limit = 1000;
        } else if (limit <= 0) {
            limit = 25;
        }
        return limit;
    }

    /**
     * Creates the an identifier, which is returned if the call is successful
     *
     * @return the newly created identifier
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public String createIdentifier() throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "generate_identifier");

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getString(response, "identifier");
    }

    /**
     * Query an identifier to get the results, with any of the filters in the
     * enumeration
     *
     * @param identifier the identifier to search for
     * @param filter the filter to apply (server-sided) on the identifier
     * results
     * @return all results in a list
     * @throws IOException if the identifier is null, does not exist, or if
     * anything goes wrong with the HTTP request
     */
    public List<YaraifyIdentifierResult> queryIdentifier(String identifier, YaraifyIdentifierFilter filter) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "list_tasks");
        json.put("identifier", identifier);

        if (null != filter) {
            switch (filter) {
                case ALL:
                    //Omit filter from the request to include all results
                    break;
                case QUEUED:
                    json.put("task_status", "queued");
                    break;
                case PROCESSED:
                    json.put("task_status", "processed");
                    break;
                default:
                    break;
            }
        }

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.parseIdentifier(response);
    }

    /**
     * Uploads a file to Yaraify and scans it, according to the given
     * parameters.
     *
     * @param file the file to upload, which must exist and must be a file (so
     * not a directory)
     * @param identifier the private identifier to bind this upload with, for
     * later reference, can be null
     * @param clamav true if ClamAV signatures should be used to scan this file,
     * false if not
     * @param unpack true if the sample should be unpacked, false if not
     * @param shareFile true if the file can be shared, false if not
     * @param skipKnown true if the file should only be scanned by Yaraify if it
     * hasn't been uploaded on the platform before, false if it should be
     * scanned anyway
     * @param skipNoisy true if the file should be skipped if it has been
     * uploaded at least 10 times in the past 24 hours, false if it should be
     * scanned anyway
     * @return the provided metadata
     * @throws IOException the file object is null, does not exist, or points to
     * a folder, or if anything with regards to the HTTP request goes wrong
     */
    public YaraifyMetadata scanFile(File file, String identifier, boolean clamav, boolean unpack, boolean shareFile, boolean skipKnown, boolean skipNoisy) throws IOException {
        if (file == null) {
            throw new IOException("The given file object is null!");
        }
        if (file.exists() == false) {
            throw new IOException("The given file does not exist!");
        }

        if (file.isDirectory()) {
            throw new IOException("The given file object refers to a folder!");
        }

        JSONObject json = new JSONObject();

        if (identifier != null && identifier.isBlank() == false) {
            json.put("identifier", identifier);
        }

        if (clamav) {
            json.put("clamav_scan", 1);
        } else {
            json.put("clamav_scan", 0);
        }

        if (unpack) {
            json.put("unpack", 1);
        } else {
            json.put("unpack", 0);
        }

        if (shareFile) {
            json.put("share_file", 1);
        } else {
            json.put("share_file", 0);
        }

        if (skipKnown) {
            json.put("skip_known", 1);
        } else {
            json.put("skip_known", 0);
        }

        if (skipNoisy) {
            json.put("skip_noisy", 1);
        } else {
            json.put("skip_noisy", 0);
        }

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", file);
        builder.addTextBody("json_data", json.toString());

        String response = new String(connector.postNew(builder));

        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(response).optString("query_status"));
        }

        YaraifyMetadata metadata = jsonParser.getMetadata(new JSONObject(response).optJSONObject("data"));

        if (metadata == null) {
            throw new IOException("An error occured when parsing the file upload response!");
        }
        return metadata;
    }

    /**
     * Uploads the given files to Yaraify and scans them, according to the given
     * parameters.
     *
     * @param files the files to upload, which must exist and must be a file (so
     * not a directory)
     * @param identifier the private identifier to bind these uploads with, for
     * later reference, can be null
     * @param clamav true if ClamAV signatures should be used to scan the files,
     * false if not
     * @param unpack true if any of the samples should be unpacked, false if not
     * @param shareFile true if any of the files can be shared, false if not
     * @param skipKnown true if any of the the files should only be scanned by
     * Yaraify if it hasn't been uploaded on the platform before, false if it
     * should be scanned anyway
     * @param skipNoisy true if any of the files should be skipped if it has
     * been uploaded at least 10 times in the past 24 hours, false if it should
     * be scanned anyway
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return the provided metadata
     * @throws IOException if any of the file objects is null, does not exist,
     * or points to a folder, or if anything with regards to the HTTP requests
     * goes wrong
     */
    public Map<File, YaraifyMetadata> scanFiles(List<File> files, String identifier, boolean clamav, boolean unpack, boolean shareFile, boolean skipKnown, boolean skipNoisy, boolean suppressExceptions) throws IOException {
        if (files == null) {
            throw new IOException("The given list of files is null!");
        }

        Map<File, YaraifyMetadata> mapping = new HashMap<>();

        for (File file : files) {
            try {
                YaraifyMetadata metadata = scanFile(file, identifier, clamav, unpack, shareFile, skipKnown, skipNoisy);
                mapping.put(file, metadata);
            } catch (IOException ex) {
                if (suppressExceptions == false) {
                    throw ex;
                }
            }
        }

        return mapping;
    }

    /**
     * Uploads all files in the given folder (excluding sub folders) to Yaraify
     * and scans them, according to the given parameters.
     *
     * @param folder the folder from which the files are to be uploaded,
     * excluding sub folders. The folder must exist.
     * @param identifier the private identifier to bind these uploads with, for
     * later reference, can be null
     * @param clamav true if ClamAV signatures should be used to scan the files,
     * false if not
     * @param unpack true if any of the samples should be unpacked, false if not
     * @param shareFile true if any of the files can be shared, false if not
     * @param skipKnown true if any of the the files should only be scanned by
     * Yaraify if it hasn't been uploaded on the platform before, false if it
     * should be scanned anyway
     * @param skipNoisy true if any of the files should be skipped if it has
     * been uploaded at least 10 times in the past 24 hours, false if it should
     * be scanned anyway
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return the provided metadata
     * @throws IOException if any of the file objects is null, does not exist,
     * or points to a folder, or if anything with regards to the HTTP requests
     * goes wrong
     */
    public Map<File, YaraifyMetadata> scanFiles(File folder, String identifier, boolean clamav, boolean unpack, boolean shareFile, boolean skipKnown, boolean skipNoisy, boolean suppressExceptions) throws IOException {
        if (folder == null) {
            throw new IOException("The given file object (referencing the folder) is null!");
        }
        if (folder.exists() == false) {
            throw new IOException("The given folder does not exist!");
        }

        Map<File, YaraifyMetadata> mapping = new HashMap<>();

        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    YaraifyMetadata metadata = scanFile(file, identifier, clamav, unpack, shareFile, skipKnown, skipNoisy);
                    mapping.put(file, metadata);
                } catch (IOException ex) {
                    if (suppressExceptions == false) {
                        throw ex;
                    }
                }
            }
        }
        return mapping;
    }

    /**
     * Queries a task based on the given ID
     *
     * @param taskId the ID to look for
     * @return the report which matches the given task ID
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public YaraifyTaskResult queryTaskId(String taskId) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_results");
        json.put("task_id", taskId);
        if (malpediaEnabled) {
            json.put("malpedia-token", malpediaApiKey);
        }

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        YaraifyTaskResult result = jsonParser.getQueryTaskId(taskId, response);
        if (result == null) {
            throw new IOException("Failure when parsing the returned JSON");
        } else {
            return result;
        }
    }

    /**
     * Returns the results for a given hash
     *
     * @param fileHash the hash of the file to look for
     * @return the report which matches the given file hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public YaraifyTaskResult queryFileHash(String fileHash) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "lookup_hash");
        json.put("search_term", fileHash);
        if (malpediaEnabled) {
            json.put("malpedia-token", malpediaApiKey);
        }

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        YaraifyTaskResult result = jsonParser.getQueryFileHash(response);
        if (result == null) {
            throw new IOException("Failure when parsing the returned JSON");
        } else {
            return result;
        }
    }

    /**
     * Returns the results for a given Yara rule. Any limit value which is lower
     * than or equal to zero, is set to the default value of 25. Any value over
     * 1000 is set to 1000.
     *
     * @param yaraRuleName the name of the Yara rule
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given Yara rule
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryYaraRule(String yaraRuleName, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_yara");
        json.put("search_term", yaraRuleName);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given Yara rule names. Any limit value which
     * is lower than or equal to zero, is set to the default value of 25. Any
     * value over 1000 is set to 1000.
     *
     * @param yaraRules the Yara rule names to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all Yara rule names and their results, where the
     * map's key is the Yara rule name, and the value for the given key is the
     * list of metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryYaraRules(List<String> yaraRules, int limit, boolean suppressExceptions) throws IOException {
        if (yaraRules == null || yaraRules.size() < 1) {
            throw new IOException("The given list of Yara rule names is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : yaraRules) {
            try {
                List<YaraifyMetadata> queryResults = queryClamAvRule(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for a given ClamAV rule. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param clamAvRuleName the name of the ClamAV rule
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given ClamAV rule
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryClamAvRule(String clamAvRuleName, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_clamav");
        json.put("search_term", clamAvRuleName);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given ClamAv rule names. Any limit value
     * which is lower than or equal to zero, is set to the default value of 25.
     * Any value over 1000 is set to 1000.
     *
     * @param clamAvRules the ClamAv rule names to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all ClamAv rule names and their results, where the
     * map's key is the ClamAv rule name, and the value for the given key is the
     * list of metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryClamAvRules(List<String> clamAvRules, int limit, boolean suppressExceptions) throws IOException {
        if (clamAvRules == null || clamAvRules.size() < 1) {
            throw new IOException("The given list of ClamAV rule names is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : clamAvRules) {
            try {
                List<YaraifyMetadata> queryResults = queryClamAvRule(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for a given import hash. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param importHash the given import hash
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given ClamAV rule
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryImportHash(String importHash, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_imphash");
        json.put("search_term", importHash);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given import hashes. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param importHashes the import hashes to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all import hashes and their results, where the
     * map's key is the import hash, and the value for the given key is the list
     * of metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryImportHashes(List<String> importHashes, int limit, boolean suppressExceptions) throws IOException {
        if (importHashes == null || importHashes.size() < 1) {
            throw new IOException("The given list of import hashes is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : importHashes) {
            try {
                List<YaraifyMetadata> queryResults = queryImportHash(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for the TLSH. Any limit value which is lower than or
     * equal to zero, is set to the default value of 25. Any value over 1000 is
     * set to 1000.
     *
     * @param tlsh the given tlsh value
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given tlsh value
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryTlsh(String tlsh, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_tlsh");
        json.put("search_term", tlsh);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given TLSH hashes. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param tlsh the TLSH hashes to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all TLSH hashes and their results, where the map's
     * key is the TLSH hash, and the value for the given key is the list of
     * metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryTlshHashes(List<String> tlsh, int limit, boolean suppressExceptions) throws IOException {
        if (tlsh == null || tlsh.size() < 1) {
            throw new IOException("The given list of TLSH hashes is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : tlsh) {
            try {
                List<YaraifyMetadata> queryResults = queryTlsh(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for a given TELF hash. Any limit value which is lower
     * than or equal to zero, is set to the default value of 25. Any value over
     * 1000 is set to 1000.
     *
     * @param telfHash the given TELF hash
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given TELF hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryTelfHash(String telfHash, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_telfhash");
        json.put("search_term", telfHash);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given TELF hashes. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param telfHashes the TELF hashes to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all TELF hashes and their results, where the map's
     * key is the TELF hash, and the value for the given key is the list of
     * metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryTelfHashes(List<String> telfHashes, int limit, boolean suppressExceptions) throws IOException {
        if (telfHashes == null || telfHashes.size() < 1) {
            throw new IOException("The given list of telf hashes is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : telfHashes) {
            try {
                List<YaraifyMetadata> queryResults = queryTelfHash(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for a given GoLang import hash. Any limit value which
     * is lower than or equal to zero, is set to the default value of 25. Any
     * value over 1000 is set to 1000.
     *
     * @param gimpHash the given GoLang import hash
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given GoLang import hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryGimpHash(String gimpHash, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_gimphash");
        json.put("search_term", gimpHash);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given GoLang import hashes. Any limit value
     * which is lower than or equal to zero, is set to the default value of 25.
     * Any value over 1000 is set to 1000.
     *
     * @param gimpHashes the GoLang import hashes to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all GoLang import hashes and their results, where
     * the map's key is the GoLang import hash, and the value for the given key
     * is the list of metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryGimpHashes(List<String> gimpHashes, int limit, boolean suppressExceptions) throws IOException {
        if (gimpHashes == null || gimpHashes.size() < 1) {
            throw new IOException("The given list of GoLang import hashes is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : gimpHashes) {
            try {
                List<YaraifyMetadata> queryResults = queryGimpHash(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Returns the results for a given icon dhash. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param iconDhash the given icon dhash
     * @param limit any value between 0 and 1000, where the default is 25
     * @return the results for the given icon dhash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyMetadata> queryIconDhash(String iconDhash, int limit) throws IOException {
        limit = checkLimit(limit);

        JSONObject json = new JSONObject();
        json.put("query", "get_dhash_icon");
        json.put("search_term", iconDhash);
        json.put("result_max", limit);

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getMetadatas(response);
    }

    /**
     * Returns the results for the given icon dhashes. Any limit value which is
     * lower than or equal to zero, is set to the default value of 25. Any value
     * over 1000 is set to 1000.
     *
     * @param iconDhashes the icon dhashes to query
     * @param limit the limit to use per given hash, between 0 and 1000
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all icon dhashes and their results, where the
     * map's key is the icon dhash, and the value for the given key is the list
     * of metadata objects that were found for said hash
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, List<YaraifyMetadata>> queryIconDhashes(List<String> iconDhashes, int limit, boolean suppressExceptions) throws IOException {
        if (iconDhashes == null || iconDhashes.size() < 1) {
            throw new IOException("The given list of icon dhashes is null or empty!");
        }
        limit = checkLimit(limit);

        Map<String, List<YaraifyMetadata>> results = new HashMap<>();

        for (String item : iconDhashes) {
            try {
                List<YaraifyMetadata> queryResults = queryIconDhash(item, limit);
                results.put(item, queryResults);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return results;
    }

    /**
     * Downloads the file of which the hash is given, if the file is present on
     * the platform. The output is a ZIP archive with the file, which uses
     * "infected" as its password.
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @return the file in a ZIP archive
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadSampleAsArchive(String sha256) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_file");
        json.put("sha256_hash", sha256);

        return connector.post(json.toString());
    }

    /**
     * Downloads the file of which the hash is given, if the file is present on
     * the platform.
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @param tempPath the temporary path to store the ZIP archive. The archive
     * will be deleted before the function returns, regardless if there is an
     * exception
     * @return the raw file
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadSample(String sha256, String tempPath) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_file");
        json.put("sha256_hash", sha256);

        byte[] rawResponse = connector.post(json.toString());

        List<byte[]> files = readZipArchive(rawResponse, tempPath, "infected");
        if (!files.isEmpty()) {
            return files.get(0);
        } else {
            throw new IOException("No such file found in the downloaded ZIP archive!");
        }
    }

    /**
     * Downloads the file of which the hash is given, if the file is present on
     * the platform.<br>
     * <br>
     * The temporary file is saved in the temporary folder of the operating
     * system, using the given SHA-256 hash as its file name. This method is not
     * thread safe if the same hash is downloaded at the same time. Use another
     * overload to provide a custom path instead!
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @return the raw file
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadSample(String sha256) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_file");
        json.put("sha256_hash", sha256);

        byte[] rawResponse = connector.post(json.toString());

        String tempPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + sha256;

        List<byte[]> files = readZipArchive(rawResponse, tempPath, "infected");
        if (!files.isEmpty()) {
            return files.get(0);
        } else {
            throw new IOException("No such file found in the downloaded ZIP archive!");
        }
    }

    /**
     * Downloads the file of which the hash is given (which is unpacked by the
     * service), if the file is present on the platform. The output is a ZIP
     * archive with the file, which uses "infected" as its password.
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @return the unpacked file as a ZIP archive
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadUnpackedSampleAsArchive(String sha256) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_unpacked");
        json.put("sha256_hash", sha256);

        return connector.post(json.toString());
    }

    /**
     * Downloads the file of which the hash is given (which is unpacked by the
     * service), if the file is present on the platform.
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @param tempPath the temporary path to store the ZIP archive. The archive
     * will be deleted before the function returns, regardless if there is an
     * exception
     * @return the raw unpacked file
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadUnpackedSample(String sha256, String tempPath) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_unpacked");
        json.put("sha256_hash", sha256);

        byte[] rawResponse = connector.post(json.toString());

        List<byte[]> files = readZipArchive(rawResponse, tempPath, "infected");
        if (files.isEmpty() == false) {
            return files.get(0);
        } else {
            throw new IOException("No such file found in the downloaded ZIP archive!");
        }
    }

    /**
     * Downloads the file of which the hash is given (which is unpacked by the
     * service), if the file is present on the platform.<br>
     * <br>
     * The temporary file is saved in the temporary folder of the operating
     * system, using the given SHA-256 hash as its file name. This method is not
     * thread safe if the same hash is downloaded at the same time. Use another
     * overload to provide a custom path instead!
     *
     * @param sha256 the SHA-256 hash of the file to download
     * @return the raw unpacked file
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public byte[] downloadUnpackedSample(String sha256) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_unpacked");
        json.put("sha256_hash", sha256);

        byte[] rawResponse = connector.post(json.toString());

        String tempPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + sha256;

        List<byte[]> files = readZipArchive(rawResponse, tempPath, "infected");
        if (files.isEmpty() == false) {
            return files.get(0);
        } else {
            throw new IOException("No such file found in the downloaded ZIP archive!");
        }
    }

    /**
     * Gets the metadata of all recently deployed Yara rules. Each metadata
     * object contains an UUID, which can be used to download the rule if the
     * TLP-level allows.
     *
     * @return a list of objects which contain the metadata of the recently
     * deployed Yara rules
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public List<YaraifyYaraRuleMetadata> getRecentlyDeployedYaraRuleMetadatas() throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "recent_yararules");

        String response = new String(connector.post(json.toString()));
        if (jsonParser.checkQueryStatus(response) == false) {
            throw new IOException(new JSONObject(json).optString("query_status"));
        }

        return jsonParser.getYaraRuleMetadata(response);
    }

    /**
     * Download a Yara rule based on the given UUID. The rule's author needs to
     * allow the download of rules, as set in the TLP-level of this rule.
     *
     * @param uuid the UUID of the Yara rule to download
     * @return the complete Yara rule
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public String downloadYaraRule(String uuid) throws IOException {
        JSONObject json = new JSONObject();
        json.put("query", "get_yara_rule");
        json.put("uuid", uuid);

        return new String(connector.post(json.toString()));
    }

    /**
     * Downloads Yara rules based on the given UUIDs.The rule's author needs to
     * allow the download of rules, as set in the TLP-level of this rule.
     *
     * @param uuids the UUIDs of the Yara rules to download
     * @param suppressExceptions true to ignore exceptions, false to throw any
     * encountered exception to the caller of this function
     * @return a mapping with all downloaded rules, where the map's key is the
     * UUID, and the value for the given key is the rule
     * @throws IOException if the query status in the response indicates the
     * request was not successful
     */
    public Map<String, String> downloadYaraRules(List<String> uuids, boolean suppressExceptions) throws IOException {
        if (uuids == null || uuids.size() < 1) {
            throw new IOException("The given list of UUIDs is null or empty!");
        }

        Map<String, String> rules = new HashMap<>();

        for (String uuid : uuids) {
            try {
                String rule = downloadYaraRule(uuid);
                rules.put(uuid, rule);
            } catch (IOException e) {
                if (suppressExceptions == false) {
                    throw e;
                }
            }
        }

        return rules;
    }

    /**
     * Download all Yara rules which are present on Yaraify bundeled in a ZIP
     * archive. A new list is generated every 5 minutes, keep that in mind when
     * polling. Only rules with the appropriate TLP-level are included in this
     * download.
     *
     * @return all downloadable Yara rules on Yaraify in a single ZIP archive
     * @throws IOException if the download failed due to any reason
     */
    public byte[] downloadAllYaraRulesAsArchive() throws IOException {
        byte[] allYaraRules = connector.get("https://yaraify-api.abuse.ch/download/yaraify-rules.zip");
        return allYaraRules;
    }

    /**
     * Download all Yara rules which are present on Yaraify.A new list is
     * generated every 5 minutes, keep that in mind when polling. Only rules
     * with the appropriate TLP-level are included in this download.
     *
     * @param tempPath the path to temporarily write the ZIP archive towards. It
     * is deleted once this function returns, regardless if an error occurs.
     * @return all downloadable Yara rules on Yaraify, one rule per string in
     * the given list
     * @throws IOException if the download failed due to any reason
     */
    public List<String> downloadAllYaraRules(String tempPath) throws IOException {
        List<String> rules = new ArrayList<>();
        //String tempPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + sha256;
        byte[] zip = connector.get("https://yaraify-api.abuse.ch/download/yaraify-rules.zip");

        List<byte[]> rawFiles = readZipArchive(zip, tempPath);

        for (byte[] rawFile : rawFiles) {
            String rule = new String(rawFile);
            rules.add(rule);
        }
        return rules;
    }

    /**
     * Download all Yara rules which are present on Yaraify.A new list is
     * generated every 5 minutes, keep that in mind when polling. Only rules
     * with the appropriate TLP-level are included in this download.<br>
     * <br>
     * This function is not thread safe, as it uses a specific file path to
     * temporary file path. If you want to use this function in a threaded way,
     * use the overload with a unique path per thread.
     *
     * @return all downloadable Yara rules on Yaraify, one rule per string in
     * the given list
     * @throws IOException if the download failed due to any reason
     */
    public List<String> downloadAllYaraRules() throws IOException {
        String tempPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "all_yara_rules.zip";

        return downloadAllYaraRules(tempPath);
    }

    /**
     * Reads the contents of the given ZIP archive (passed as a byte array). It
     * is temporarily written to the disk at the given path. The given password
     * is only used if the ZIP is encrypted, if it is not null nor empty. The
     * ZIP archive is removed from the disk prior to returning from this
     * function, regardless if an error occurs.
     *
     * @param zip the ZIP archive as a byte array
     * @param tempPath the temporary path to save the ZIP archive
     * @param password the password with which the ZIP archive is protected
     * @return a list of byte arrays, one for each of the ZIP files in the
     * archive
     * @throws IOException if the temporary path is not writeable, or if
     * something goes wrong with the ZIP archive extraction
     */
    private List<byte[]> readZipArchive(byte[] zip, String tempPath, String password) throws IOException {
        List<byte[]> files = new ArrayList<>();
        File localFile = new File(tempPath);
        localFile.getParentFile().mkdirs();
        Files.write(localFile.toPath(), zip); //overwrites if it exists, not thread safe when downloading the same data, unless a unique path is given

        try {
            ZipFile zipFile = new ZipFile(localFile);
            if (zipFile.isEncrypted() && password != null && password.isEmpty() == false) {
                zipFile.setPassword(password.toCharArray());
            }

            List<FileHeader> headers = zipFile.getFileHeaders();

            for (FileHeader header : headers) {
                byte[] bytes = readFileFromZipArchive(zipFile, header);

                //Ignore errors
                if (bytes == null) {
                    continue;
                }
                files.add(bytes);
            }

            //If all went well, delete the file
            localFile.delete();

            return files;
        } catch (ZipException e) {
            //Also delete the file if there is an error to avoid automated systems filling up over time
            localFile.delete();
            throw new IOException("Error whilst handling the (now deleted) ZIP archive:\n" + e.getMessage());
        }
    }

    /**
     * Reads the contents of the given ZIP archive (passed as a byte array). It
     * is temporarily written to the disk at the given path. The ZIP archive is
     * removed from the disk prior to returning from this function, regardless
     * if an error occurs.
     *
     * @param zip the ZIP archive as a byte array
     * @param tempPath the temporary path to save the ZIP archive
     * @return a list of byte arrays, one for each of the ZIP files in the
     * archive
     * @throws IOException if the temporary path is not writeable, or if
     * something goes wrong with the ZIP archive extraction
     */
    private List<byte[]> readZipArchive(byte[] zip, String tempPath) throws IOException {
        return readZipArchive(zip, tempPath, null);
    }

    /**
     * Reads a file based on the given header from the given ZIP file
     *
     * @param zipFile the ZIP file to read the file from
     * @param header the specific file to read from the ZIP archive
     * @return the raw bytes of the file that was read, or null if an error
     * occurred
     */
    private byte[] readFileFromZipArchive(ZipFile zipFile, FileHeader header) {
        try {
            ZipInputStream inputStream = zipFile.getInputStream(header);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int offset = -1;
            byte[] buff = new byte[1024];
            while ((offset = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, offset);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
