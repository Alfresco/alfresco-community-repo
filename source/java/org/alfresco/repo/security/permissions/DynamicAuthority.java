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
package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface for a dynamic authority provider e.g. for the owner of a node
 * or any other authority that is determined by the context rather than just a
 * node.
 * 
 * @author Andy Hind
 */
public interface DynamicAuthority
{
    /**
     * Is this authority granted to the given user for this node ref?
     * 
     * @param nodeRef
     * @param userName
     * @return
     */
    public boolean hasAuthority(NodeRef nodeRef, String userName);

    /**
     * If this authority is granted this method provides the string
     * representation of the granted authority.
     * 
     * @return
     */
    public String getAuthority();
}
