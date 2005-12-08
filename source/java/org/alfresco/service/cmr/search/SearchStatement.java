/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.search;

/**
 * A search string and language.
 * 
 * @author Andy Hind
 */
public class SearchStatement
{

    private String language;
    private String query;

    SearchStatement()
    {
        super();
    }
    
    SearchStatement(String language, String query)
    {
        this.language = language;
        this.query = query;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getQuery()
    {
        return query;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }
    
}
