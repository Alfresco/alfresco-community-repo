/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.io.Serializable;

/**
 * Represents a deleted child in a layered directory.
 * @author britt
 */
class DeletedChildImpl implements DeletedChild, Serializable
{
    private static final long serialVersionUID = 4997060636280774719L;

    /**
     * The name of the deleted child.
     */
    private String fName;
    
    /**
     * The parent directory.
     */
    private LayeredDirectoryNode fParent;
    
    /**
     * Default constructor. For Hibernate.
     */
    protected DeletedChildImpl()
    {
    }
    
    /**
     * Create a new one.
     * @param name
     * @param parent
     */
    public DeletedChildImpl(String name,
                            LayeredDirectoryNode parent)
    {
        fName = name;
        fParent = parent;
    }
    
    /**
     * Set the name of the deleted child. For Hibernate.
     * @param name
     */
    protected void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name of the deleted child.
     * @return The name.
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Set the parent directory.
     * @param parent 
     */
    protected void setParent(LayeredDirectoryNode parent)
    {
        fParent = parent;
    }
    
    /**
     * Get the parent of the deleted child.
     * @return The parent.
     */
    public LayeredDirectoryNode getParent()
    {
        return fParent;
    }

    /**
     * Equality in the database entity sense.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof DeletedChild))
        {
            return false;
        }
        DeletedChild dc = (DeletedChild)obj;
        return fParent.equals(dc.getParent()) && fName.equals(dc.getName());
    }

    /**
     * Get a hash code.
     * @return A hash code.
     */
    @Override
    public int hashCode()
    {
        return fParent.hashCode() + fName.hashCode();
    }
}
