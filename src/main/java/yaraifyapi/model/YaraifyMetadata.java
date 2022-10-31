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
public class YaraifyMetadata {

    private String fileName;
    private int fileSize;
    private String fileTypeMime;
    private String firstSeen;
    private String lastSeen;
    private int sightings;
    private String sha256;
    private String md5;
    private String sha1;
    private String sha3_384;
    private String importHash;
    private String ssdeep;
    private String tlsh;
    private String telfHash;
    private String gimpHash;
    private String dhashIcon;

    public YaraifyMetadata(String fileName, int fileSize, String fileTypeMime, String firstSeen, String lastSeen, int sightings, String sha256, String md5, String sha1, String sha3_384, String importHash, String ssdeep, String tlsh, String telfHash, String gimpHash, String dhashIcon) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileTypeMime = fileTypeMime;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.sightings = sightings;
        this.sha256 = sha256;
        this.md5 = md5;
        this.sha1 = sha1;
        this.sha3_384 = sha3_384;
        this.importHash = importHash;
        this.ssdeep = ssdeep;
        this.tlsh = tlsh;
        this.telfHash = telfHash;
        this.gimpHash = gimpHash;
        this.dhashIcon = dhashIcon;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileTypeMime() {
        return fileTypeMime;
    }

    public void setFileTypeMime(String fileTypeMime) {
        this.fileTypeMime = fileTypeMime;
    }

    public String getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(String firstSeen) {
        this.firstSeen = firstSeen;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getSightings() {
        return sightings;
    }

    public void setSightings(int sightings) {
        this.sightings = sightings;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha3_384() {
        return sha3_384;
    }

    public void setSha3_384(String sha3_384) {
        this.sha3_384 = sha3_384;
    }

    public String getImportHash() {
        return importHash;
    }

    public void setImportHash(String importHash) {
        this.importHash = importHash;
    }

    public String getSsdeep() {
        return ssdeep;
    }

    public void setSsdeep(String ssdeep) {
        this.ssdeep = ssdeep;
    }

    public String getTlsh() {
        return tlsh;
    }

    public void setTlsh(String tlsh) {
        this.tlsh = tlsh;
    }

    public String getTelfHash() {
        return telfHash;
    }

    public void setTelfHash(String telfHash) {
        this.telfHash = telfHash;
    }

    public String getGimpHash() {
        return gimpHash;
    }

    public void setGimpHash(String gimpHash) {
        this.gimpHash = gimpHash;
    }

    public String getDhashIcon() {
        return dhashIcon;
    }

    public void setDhashIcon(String dhashIcon) {
        this.dhashIcon = dhashIcon;
    }
}
