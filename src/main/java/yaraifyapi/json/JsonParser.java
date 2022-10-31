/*
 * Copyright (C) 2022 Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
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
package yaraifyapi.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import yaraifyapi.model.YaraifyIdentifierResult;
import yaraifyapi.model.YaraifyMetadata;
import yaraifyapi.model.YaraifyYaraResult;
import yaraifyapi.model.YaraifyYaraRuleMetadata;
import yaraifyapi.model.YaraifyTask;
import yaraifyapi.model.YaraifyTaskResult;
import yaraifyapi.model.YaraifyUnpackResult;

/**
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class JsonParser {

    public boolean checkQueryStatus(String json) {
        String queryStatus = new JSONObject(json).optString("query_status");
        if (queryStatus.equalsIgnoreCase("ok")
                || queryStatus.equalsIgnoreCase("inserted")
                || queryStatus.equalsIgnoreCase("updated")
                || queryStatus.equalsIgnoreCase("success")
                || queryStatus.equalsIgnoreCase("no_results")
                || queryStatus.equalsIgnoreCase("queued")) {
            return true;
        }
        return false;
    }

    public String getString(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        return new JSONObject(json).optString(key);
    }

    public List<YaraifyIdentifierResult> parseIdentifier(String json) {
        List<YaraifyIdentifierResult> results = new ArrayList<>();

        if (json == null) {
            return results;
        }

        JSONObject jsonObject = new JSONObject(json);

        JSONArray data = jsonObject.optJSONArray("data");
        if (data == null) {
            return results;
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);

            if (item == null) {
                continue;
            }

            String taskId = item.optString("task_id");
            String taskSTatus = item.optString("task_status");
            String md5 = item.optString("md5_hash");
            String sha256 = item.optString("sha256_hash");
            String fileName = item.optString("file_name");

            YaraifyIdentifierResult result = new YaraifyIdentifierResult(taskId, taskSTatus, md5, sha256, fileName);
            results.add(result);
        }

        return results;
    }

    public YaraifyMetadata getMetadata(JSONObject json) {
        if (json == null) {
            return null;
        }
        String fileName = json.optString("file_name");
        int fileSize = json.optInt("file_size");
        String fileTypeMime = json.optString("file_type_mime");
        if (fileTypeMime.isEmpty()) {
            //Some of the endpoints use this field, as it is not uniform over all endpoints, but this object will be
            fileTypeMime = json.optString("mime_type");
        }
        String firstSeen = json.optString("first_seen");
        String lastSeen = json.optString("last_seen");
        int sightings = json.optInt("sightings");
        String sha256 = json.optString("sha256_hash");
        String md5 = json.optString("md5_hash");
        String sha1 = json.optString("sha1_hash");
        String sha3_384 = json.optString("sha3_384");
        if (sha3_384.isEmpty()) {
            //Some of the endpoints use this field, as it is not uniform over all endpoints, but this object will be
            sha3_384 = json.optString("sha3_384_hash");
        }
        String importHash = json.optString("imphash");
        String ssdeep = json.optString("ssdeep");
        String tlsh = json.optString("tlsh");
        String telfHash = json.optString("telfhash");
        String gimpHash = json.optString("gimphash");
        String dhashIcon = json.optString("dhash_icon");
        return new YaraifyMetadata(fileName, fileSize, fileTypeMime, firstSeen, lastSeen, sightings, sha256, md5, sha1, sha3_384, importHash, ssdeep, tlsh, telfHash, gimpHash, dhashIcon);
    }

    public List<YaraifyMetadata> getMetadatas(String json) {
        List<YaraifyMetadata> metadatas = new ArrayList<>();

        if (json == null) {
            return metadatas;
        }

        JSONObject jsonObject = new JSONObject(json);

        JSONArray array = jsonObject.optJSONArray("data");
        if (array == null) {
            return metadatas;
        }

        for (int i = 0; i < array.length(); i++) {
            YaraifyMetadata metadata = getMetadata(array.optJSONObject(i));
            if (metadata == null) {
                continue;
            }

            metadatas.add(metadata);
        }
        return metadatas;
    }

    public List<YaraifyYaraResult> getYaraResult(JSONArray json) {
        List<YaraifyYaraResult> results = new ArrayList<>();

        if (json == null) {
            return results;
        }

        for (int i = 0; i < json.length(); i++) {
            JSONObject item = json.getJSONObject(i);

            if (item == null) {
                continue;
            }

            String ruleName = item.optString("rule_name");
            String author = item.optString("author");
            String description = item.optString("description");
            String reference = item.optString("reference");
            String tlp = item.optString("tlp");
            YaraifyYaraResult result = new YaraifyYaraResult(ruleName, author, description, reference, tlp);
            results.add(result);
        }
        return results;
    }

    /**
     * Converts a given JSONArray object into a String array with all values
     *
     * @param input the JSONArray to convert
     * @return the string array with all values
     */
    private String[] optStringArray(JSONArray input) {
        if (input == null) {
            return new String[0];
        }
        String[] output = new String[input.length()];
        for (int i = 0; i < output.length; i++) {
            output[i] = input.optString(i);
        }
        return output;
    }

    public YaraifyTaskResult getQueryTaskId(String taskId, String json) {
        if (json == null || taskId == null) {
            return null;
        }

        JSONObject jsonObject = new JSONObject(json);

        JSONObject data = jsonObject.optJSONObject("data");
        if (data == null) {
            return null;
        }
        YaraifyMetadata metadata = getMetadata(data.optJSONObject("metadata"));

        List<YaraifyTask> yaraifyTasks = new ArrayList<>();

        String timestamp = metadata.getFirstSeen();
        String fileName = metadata.getFileName();

        List<String> clamAvResults = getClamAvResults(data.optJSONArray("clamav_results"));

        List<YaraifyYaraResult> staticResults = getYaraResult(data.optJSONArray("static_results"));

        List<YaraifyUnpackResult> unpackResults = getUnpackResults(data.optJSONArray("unpacker_results"));

        YaraifyTask yaraifyTask = new YaraifyTask(taskId, timestamp, fileName, clamAvResults, staticResults, unpackResults);
        yaraifyTasks.add(yaraifyTask);

        return new YaraifyTaskResult(metadata, yaraifyTasks);
    }

    public YaraifyTaskResult getQueryFileHash(String json) {
        if (json == null) {
            return null;
        }

        JSONObject jsonObject = new JSONObject(json);

        JSONObject data = jsonObject.optJSONObject("data");
        if (data == null) {
            return null;
        }
        YaraifyMetadata metadata = getMetadata(data.optJSONObject("metadata"));

        //Get all tasks
        JSONArray tasks = data.optJSONArray("tasks");
        List<YaraifyTask> yaraifyTasks = new ArrayList<>();

        if (tasks != null) {
            //Iterate over tasks
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.optJSONObject(i);
                if (task == null) {
                    continue;
                }

                String taskId = task.optString("task_id");
                String timestamp = task.optString("time_stamp");
                String fileName = task.optString("file_name");

                List<String> clamAvResults = getClamAvResults(jsonObject.optJSONArray("clamav_results"));

                List<YaraifyYaraResult> staticResults = getYaraResult(task.optJSONArray("static_results"));

                List<YaraifyUnpackResult> unpackResults = getUnpackResults(task.optJSONArray("unpacker_results"));

                YaraifyTask yaraifyTask = new YaraifyTask(taskId, timestamp, fileName, clamAvResults, staticResults, unpackResults);
                yaraifyTasks.add(yaraifyTask);
            }

        }
        return new YaraifyTaskResult(metadata, yaraifyTasks);
    }

    public List<String> getClamAvResults(JSONArray jsonArray) {
        List<String> clamAvResults = new ArrayList<>();
        //The list is instatiated since editing an asList-returned list will throw an exception
        clamAvResults.addAll(Arrays.asList(optStringArray(jsonArray)));
        return clamAvResults;
    }

    public List<YaraifyUnpackResult> getUnpackResults(JSONArray jsonArray) {
        List<YaraifyUnpackResult> unpackResults = new ArrayList<>();

        if (jsonArray == null) {
            return unpackResults;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.optJSONObject(i);
            if (item == null) {
                continue;
            }
            String unpackedFileName = item.optString("unpacked_file_name");
            String unpackedMd5 = item.optString("unpacked_md5");
            String unpackedSha256 = item.optString("unpacked_sha256");
            List<YaraifyYaraResult> yaraResults = getYaraResult(item.optJSONArray("unpacked_yara_matches"));

            YaraifyUnpackResult unpackResult = new YaraifyUnpackResult(unpackedFileName, unpackedMd5, unpackedSha256, yaraResults);
            unpackResults.add(unpackResult);
        }

        return unpackResults;
    }

    public List<YaraifyYaraRuleMetadata> getYaraRuleMetadata(String json) {
        if (json == null) {
            return null;
        }

        JSONObject mainObject = new JSONObject(json);
        if (mainObject == null) {
            return null;
        }

        JSONArray jsonArray = mainObject.optJSONArray("data");
        if (jsonArray == null) {
            return null;
        }

        List<YaraifyYaraRuleMetadata> results = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject == null) {
                continue;
            }

            String timeStamp = jsonObject.optString("time_stamp");
            String yaraHubUuid = jsonObject.optString("yarahub_uuid");
            String ruleName = jsonObject.optString("rule_name");
            String author = jsonObject.optString("author");
            String description = jsonObject.optString("description");
            String date = jsonObject.optString("date");
            String yaraHubLicense = jsonObject.optString("yarahub_license");
            String yaraHubAuthorTwitter = jsonObject.optString("yarahub_author_twitter");
            String yaraHubReferenceLink = jsonObject.optString("yarahub_reference_link");
            String yaraHubReferenceMd5 = jsonObject.optString("yarahub_reference_md5");
            String yaraHubRuleMatchingTlp = jsonObject.optString("yarahub_rule_matching_tlp");
            String yaraHubRuleSharingTlp = jsonObject.optString("yarahub_rule_sharing_tlp");
            String malpediaFamily = jsonObject.optString("malpedia_family");

            results.add(new YaraifyYaraRuleMetadata(timeStamp, yaraHubUuid, ruleName, author, description, date, yaraHubLicense, yaraHubAuthorTwitter, yaraHubReferenceLink, yaraHubReferenceMd5, yaraHubRuleMatchingTlp, yaraHubRuleSharingTlp, malpediaFamily));
        }

        return results;
    }
}
