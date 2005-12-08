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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.QName;

public interface QueryRegisterComponent
{
    /**
     * Get a query defintion by Qname
     * 
     * @param qName
     * @return
     */
    public CannedQueryDef getQueryDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a query
     * 
     * @param qName
     * @return
     */
    public String getCollectionNameforQueryDefinition(QName qName);
    
    /**
     * Get a parameter definition
     * 
     * @param qName
     * @return
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);
    
    /**
     * Get the name of the collection containing a parameter definition
     * 
     * @param qName
     * @return
     */
    public String getCollectionNameforParameterDefinition(QName qName);
    
    
    /**
     * Get a query collection by name
     * 
     * @param name
     * @return
     */
    public QueryCollection getQueryCollection(String name);
    
    
    /**
     * Load a query collection
     * 
     * @param location
     */
    public void loadQueryCollection(String location);
}
