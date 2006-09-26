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
package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A ref to a home folder 
 * - the node ref
 * - a simple status as to how it was obtained 
 * 
 * @author Andy Hind
 */
public class HomeSpaceNodeRef
{
    public enum Status{VALID, REFERENCED, CREATED};
    
    private NodeRef nodeRef;

    private Status status;
    
    public HomeSpaceNodeRef(NodeRef nodeRef, Status status)
    {
        this.nodeRef = nodeRef;
        this.status = status;
    }

    NodeRef getNodeRef()
    {
        return nodeRef;
    }

    Status getStatus()
    {
        return status;
    }
    
    
}
