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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.io.Serializable;

/**
 * The interface against which permission references are persisted in hibernate.
 * 
 * @author andyh
 */
public interface PermissionReference extends Serializable
{
   /**
    * Get the URI for the type to which this permission applies.
    * 
    * @return
    */ 
    public String getTypeUri();
    
    /**
     * Set the URI for the type to which this permission applies.
     * 
     * @param typeUri
     */
    public void setTypeUri(String typeUri);
    
    /**
     * Get the local name of the type to which this permission applies.
     * 
     * @return
     */
    public String getTypeName();
    
    /**
     * Set the local name of the type to which this permission applies.
     * 
     * @param typeName
     */
    public void setTypeName(String typeName);
    
    /**
     * Get the name of the permission.
     * 
     * @return
     */
    public String getName();
    
    /**
     * Set the name of the permission.
     * 
     * @param name
     */
    public void setName(String name);
}
