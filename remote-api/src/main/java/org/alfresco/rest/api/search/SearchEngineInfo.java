package org.alfresco.rest.api.search;

public class SearchEngineInfo {
    String searchEngineName;
    String searchEngineVersion;
    String searchEngineLuceneVersion;

    public String getSearchEngineName() {
        return searchEngineName;
    }

    public void setSearchEngineName(String searchEngineName) {
        this.searchEngineName = searchEngineName;
    }

    public String getSearchEngineVersion() {
        return searchEngineVersion;
    }

    public void setSearchEngineVersion(String searchEngineVersion) {
        this.searchEngineVersion = searchEngineVersion;
    }

    public String getSearchEngineLuceneVersion() {
        return searchEngineLuceneVersion;
    }

    public void setSearchEngineLuceneVersion(String searchEngineLuceneVersion) {
        this.searchEngineLuceneVersion = searchEngineLuceneVersion;
    }
}
