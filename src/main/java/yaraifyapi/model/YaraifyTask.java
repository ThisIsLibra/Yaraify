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
package yaraifyapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class YaraifyTask {

    private String taskId;
    private String timestamp;
    private String fileName;
    private List<String> clamAvResults;
    private List<YaraifyYaraResult> staticResults;
    private List<YaraifyUnpackResult> unpackResults;
    private List<YaraifyYaraResult> allYaraResults;

    public YaraifyTask(String taskId, String timestamp, String fileName, List<String> clamAvResults, List<YaraifyYaraResult> staticResults, List<YaraifyUnpackResult> unpackResults) {
        this.taskId = taskId;
        this.timestamp = timestamp;
        this.fileName = fileName;
        this.clamAvResults = clamAvResults;
        this.staticResults = staticResults;
        this.unpackResults = unpackResults;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getClamAvResults() {
        return clamAvResults;
    }

    public List<YaraifyYaraResult> getStaticResults() {
        return staticResults;
    }

    public List<YaraifyUnpackResult> getUnpackResults() {
        return unpackResults;
    }

    public List<YaraifyYaraResult> getAllYaraResults() {
        /**
         * If the list is not initialised, do so. This saves memory as this list
         * is only populated once it is required.
         */
        if (allYaraResults == null) {
            allYaraResults = new ArrayList<>();
            allYaraResults.addAll(staticResults);
            for (YaraifyUnpackResult unpackResult : unpackResults) {
                allYaraResults.addAll(unpackResult.getYaraResults());
            }
        }
        //Return it after initialisation
        return allYaraResults;
    }

}
