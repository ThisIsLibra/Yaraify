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

/**
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class YaraifyIdentifierResult {

    private String taskId;
    private String taskStatus;
    private String md5;
    private String sha256;
    private String fileName;

    public YaraifyIdentifierResult(String taskId, String taskStatus, String md5, String sha256, String fileName) {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.md5 = md5;
        this.sha256 = sha256;
        this.fileName = fileName;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public String getMd5() {
        return md5;
    }

    public String getSha256() {
        return sha256;
    }

    public String getFileName() {
        return fileName;
    }
}
