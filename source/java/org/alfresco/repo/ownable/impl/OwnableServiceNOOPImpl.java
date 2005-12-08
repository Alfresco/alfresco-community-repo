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
package org.alfresco.repo.ownable.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;

/**
 * A simple implementation that does not support ownership.
 * 
 * @author Andy Hind
 */
public class OwnableServiceNOOPImpl implements OwnableService
{

    public OwnableServiceNOOPImpl()
    {
        super();
    }

    public String getOwner(NodeRef nodeRef)
    {
        // Return null as there is no owner.
        return null;
    }

    public void setOwner(NodeRef nodeRef, String userName)
    {
        // No action.
    }

    public void takeOwnership(NodeRef nodeRef)
    {   
        // No action.
    }

    public boolean hasOwner(NodeRef nodeRef)
    {
        // There is no owner for any node.
        return false;
    }

}
