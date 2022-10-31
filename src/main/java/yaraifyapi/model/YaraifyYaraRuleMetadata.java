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
public class YaraifyYaraRuleMetadata {

    private String timeStamp;
    private String yaraHubUuid;
    private String ruleName;
    private String author;
    private String description;
    private String date;
    private String yaraHubLicense;
    private String yaraHubAuthorTwitter;
    private String yaraHubReferenceLink;
    private String yaraHubReferenceMd5;
    private String yaraHubRuleMatchingTlp;
    private String yaraHubRuleSharingTlp;
    private String malpediaFamily;

    public YaraifyYaraRuleMetadata(String timeStamp, String yaraHubUuid, String ruleName, String author, String description, String date, String yaraHubLicense, String yaraHubAuthorTwitter, String yaraHubReferenceLink, String yaraHubReferenceMd5, String yaraHubRuleMatchingTlp, String yaraHubRuleSharingTlp, String malpediaFamily) {
        this.timeStamp = timeStamp;
        this.yaraHubUuid = yaraHubUuid;
        this.ruleName = ruleName;
        this.author = author;
        this.description = description;
        this.date = date;
        this.yaraHubLicense = yaraHubLicense;
        this.yaraHubAuthorTwitter = yaraHubAuthorTwitter;
        this.yaraHubReferenceLink = yaraHubReferenceLink;
        this.yaraHubReferenceMd5 = yaraHubReferenceMd5;
        this.yaraHubRuleMatchingTlp = yaraHubRuleMatchingTlp;
        this.yaraHubRuleSharingTlp = yaraHubRuleSharingTlp;
        this.malpediaFamily = malpediaFamily;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getYaraHubUuid() {
        return yaraHubUuid;
    }

    public void setYaraHubUuid(String yaraHubUuid) {
        this.yaraHubUuid = yaraHubUuid;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getYaraHubLicense() {
        return yaraHubLicense;
    }

    public void setYaraHubLicense(String yaraHubLicense) {
        this.yaraHubLicense = yaraHubLicense;
    }

    public String getYaraHubAuthorTwitter() {
        return yaraHubAuthorTwitter;
    }

    public void setYaraHubAuthorTwitter(String yaraHubAuthorTwitter) {
        this.yaraHubAuthorTwitter = yaraHubAuthorTwitter;
    }

    public String getYaraHubReferenceLink() {
        return yaraHubReferenceLink;
    }

    public void setYaraHubReferenceLink(String yaraHubReferenceLink) {
        this.yaraHubReferenceLink = yaraHubReferenceLink;
    }

    public String getYaraHubReferenceMd5() {
        return yaraHubReferenceMd5;
    }

    public void setYaraHubReferenceMd5(String yaraHubReferenceMd5) {
        this.yaraHubReferenceMd5 = yaraHubReferenceMd5;
    }

    public String getYaraHubRuleMatchingTlp() {
        return yaraHubRuleMatchingTlp;
    }

    public void setYaraHubRuleMatchingTlp(String yaraHubRuleMatchingTlp) {
        this.yaraHubRuleMatchingTlp = yaraHubRuleMatchingTlp;
    }

    public String getYaraHubRuleSharingTlp() {
        return yaraHubRuleSharingTlp;
    }

    public void setYaraHubRuleSharingTlp(String yaraHubRuleSharingTlp) {
        this.yaraHubRuleSharingTlp = yaraHubRuleSharingTlp;
    }

    public String getMalpediaFamily() {
        return malpediaFamily;
    }

    public void setMalpediaFamily(String malpediaFamily) {
        this.malpediaFamily = malpediaFamily;
    }
}
