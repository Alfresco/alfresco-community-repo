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
package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service support around managing ownership.
 * 
 * @author Andy Hind
 */
public interface OwnableService
{
    /**
     * Get the username of the owner of the given object.
     *  
     * @param nodeRef
     * @return the username or null if the object has no owner
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public String getOwner(NodeRef nodeRef);
    
    /**
     * Set the owner of the object.
     * 
     * @param nodeRef
     * @param userName
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "userName"})
    public void setOwner(NodeRef nodeRef, String userName);
    
    /**
     * Set the owner of the object to be the current user.
     * 
     * @param nodeRef
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void takeOwnership(NodeRef nodeRef);
    
    /**
     * Does the given node have an owner?
     * 
     * @param nodeRef
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public boolean hasOwner(NodeRef nodeRef);
}
