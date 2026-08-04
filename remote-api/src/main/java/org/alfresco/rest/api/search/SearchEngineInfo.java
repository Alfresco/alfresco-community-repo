/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
