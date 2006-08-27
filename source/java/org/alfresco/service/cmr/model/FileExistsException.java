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
package org.alfresco.service.cmr.model;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Common, checked exception thrown when an operation fails because
 * of a name clash.
 * 
 * @author Derek Hulley
 */
public class FileExistsException extends RuntimeException
{
    private static final long serialVersionUID = -4133713912784624118L;
    
    private NodeRef parentNodeRef;
    private String name;

    public FileExistsException(NodeRef parentNodeRef, String name)
    {
        super("Existing file or folder " +
                name +
                " already exists");
        this.parentNodeRef = parentNodeRef;
        this.name = name;
    }

    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }

    public String getName()
    {
        return name;
    }
}
