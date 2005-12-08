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
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

public interface QueryCollection
{
    /**
     * The name of the query collection
     * 
     * @return
     */
    public String getName();
    
    /**
     * Does this collection contain a query for the given QName?
     * @param qName
     * @return
     */
    public boolean containsQueryDefinition(QName qName);
    
    /**
     * Get a query definition by QName.
     * @param qName
     * @return
     */
    public CannedQueryDef getQueryDefinition(QName qName);
    
    /**
     * Does this collection contain a query for the given QName?
     * @param qName
     * @return
     */
    public boolean containsParameterDefinition(QName qName);
    
    /**
     * Get a query definition by QName.
     * @param qName
     * @return
     */
    public QueryParameterDefinition getParameterDefinition(QName qName);
    
    /**
     * Return the mechanism that this query definition uses to map namespace prefixes to URIs.
     * A query may use a predefined set of prefixes for known URIs.
     * I would be unwise to rely on the defaults.
     *  
     * @return
     */
    public NamespacePrefixResolver getNamespacePrefixResolver();
    
}
