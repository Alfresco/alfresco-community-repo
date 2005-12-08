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
package org.alfresco.repo.dictionary;

import org.alfresco.service.namespace.NamespacePrefixResolver;


/**
 * Namespace DAO Interface.
 * 
 * This DAO is responsible for retrieving and creating Namespace definitions.
 * 
 * @author David Caruana
 */
public interface NamespaceDAO extends NamespacePrefixResolver
{
    
    /**
     * Add a namespace URI
     * 
     * @param uri the namespace uri to add
     */
    public void addURI(String uri);

    /**
     * Remove the specified URI
     * 
     * @param uri the uri to remove
     */
    public void removeURI(String uri);

    /**
     * Add a namespace prefix
     * 
     * @param prefix the prefix
     * @param uri the uri to prefix
     */    
    public void addPrefix(String prefix, String uri);

    /**
     * Remove a namspace prefix
     * 
     * @param prefix the prefix to remove
     */
    public void removePrefix(String prefix);
    
}
