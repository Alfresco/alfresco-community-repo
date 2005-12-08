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

import java.util.Collection;
import java.util.Map;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * The definition of a canned query
 * 
 * @author andyh
 * 
 */
public interface CannedQueryDef
{
    /**
     * Get the unique name for the query
     * 
     * @return
     */
    public QName getQname();

    /**
     * Get the language in which the query is defined.
     * 
     * @return
     */
    public String getLanguage();

    /**
     * Get the definitions for any query parameters.
     * 
     * @return
     */
    public Collection<QueryParameterDefinition> getQueryParameterDefs();

    /**
     * Get the query string.
     * 
     * @return
     */
    public String getQuery();

    /**
     * Return the mechanism that this query definition uses to map namespace
     * prefixes to URIs. A query may use a predefined set of prefixes for known
     * URIs. I would be unwise to rely on the defaults.
     * 
     * @return
     */
    public NamespacePrefixResolver getNamespacePrefixResolver();

    /**
     * Get a map to look up definitions by Qname
     * 
     * @return
     */
    public Map<QName, QueryParameterDefinition> getQueryParameterMap();
}
