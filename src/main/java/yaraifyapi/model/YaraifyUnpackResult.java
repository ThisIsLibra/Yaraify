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

import java.util.List;

/**
 *
 * @author Max 'Libra' Kersten [@Libranalysis, https://maxkersten.nl]
 */
public class YaraifyUnpackResult {

    private String unpackedFileName;
    private String unpackedMd5;
    private String unpackedSha256;
    private List<YaraifyYaraResult> yaraResults;

    public YaraifyUnpackResult(String unpackedFileName, String unpackedMd5, String unpackedSha256, List<YaraifyYaraResult> yaraResults) {
        this.unpackedFileName = unpackedFileName;
        this.unpackedMd5 = unpackedMd5;
        this.unpackedSha256 = unpackedSha256;
        this.yaraResults = yaraResults;
    }

    public String getUnpackedFileName() {
        return unpackedFileName;
    }

    public String getUnpackedMd5() {
        return unpackedMd5;
    }

    public String getUnpackedSha256() {
        return unpackedSha256;
    }

    public List<YaraifyYaraResult> getYaraResults() {
        return yaraResults;
    }
}
