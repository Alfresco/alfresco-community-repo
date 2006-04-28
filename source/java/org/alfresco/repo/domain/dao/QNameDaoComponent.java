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
package org.alfresco.repo.domain.dao;

import org.alfresco.repo.domain.QNameEntity;
import org.alfresco.service.namespace.QName;

/**
 * Service layer accessing persistent <b>qname</b> entities directly
 * 
 * @author Derek Hulley
 */
public interface QNameDaoComponent
{
    /**
     * Create a qname entity
     * 
     * @param qname the qualified name to persist
     * @return Returns either a newly created instance or an existing matching instance
     */
    public QNameEntity createQNameEntity(QName qname);
    
    /**
     * @param qname the qname to match
     * @return Returns the entity if it exists, otherwise null
     */
    public QNameEntity getQNameEntity(QName qname);

    /**
     * @param namespaceUri the namespace URI
     * @param localName the localname part
     * @return Return the entity if it exists, otherwise null
     */
    public QNameEntity getQNameEntity(String namespaceUri, String localName);
}
