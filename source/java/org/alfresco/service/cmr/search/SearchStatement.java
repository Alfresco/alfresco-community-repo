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
    
    /**
     * A constructor that takes both arguments.
     * 
     * @param language
     * @param query
     */
    SearchStatement(String language, String query)
    {
        this.language = language;
        this.query = query;
    }

    /**
     * Get the query language. 
     * 
     * @return
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Get the query.
     * 
     * @return
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the query language.
     * 
     * @param language - the query language.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Set the query string.
     * 
     * @param query - the query string.
     */
    public void setQuery(String query)
    {
        this.query = query;
    }
    
}
