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
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * The interface against which permission references are persisted in hibernate.
 * 
 * @author andyh
 */
public interface DbPermission extends Serializable
{
    /**
     * @return Returns the automatically assigned ID
     */
    public long getId();
    
    /**
     * @return Returns the qualified name of this permission
     */
    public QName getTypeQname();
    
    /**
     * @param qname the entity representing the qname for this instance
     */
    public void setTypeQname(QName qname);

    /**
     * @return Returns the permission name
     */
    public String getName();
    
    /**
     * @param name the name of the permission
     */
    public void setName(String name);
    
    /**
     * @return Returns a key combining the {@link #getTypeQname() type}
     *      and {@link #getName() name}
     */
    public DbPermissionKey getKey();
}
