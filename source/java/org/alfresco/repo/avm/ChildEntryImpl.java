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
 * An entry in a directory. Contains a name, parent, and child.
 * @author britt
 */
public class ChildEntryImpl implements ChildEntry, Serializable
{
    private static final long serialVersionUID = -307752114272916930L;

    /**
     * The name of the entry.
     */
    private String fName;
    
    /**
     * The parent.
     */
    private DirectoryNode fParent;
    
    /**
     * The child.
     */
    private AVMNode fChild;
    
    /**
     * Default constructor for Hibernate.
     */
    protected ChildEntryImpl()
    {
    }

    /**
     * Make up a brand new entry.
     * @param name
     * @param parent
     * @param child
     */
    public ChildEntryImpl(String name,
                          DirectoryNode parent,
                          AVMNode child)
    {
        fName = name;
        fParent = parent;
        fChild = child;
    }
    
    /**
     * Set the name of this entry.
     * @param name
     */
    public void setName(String name)
    {
        fName = name;
    }

    /**
     * Get the name of this entry.
     * @return
     */
    public String getName()
    {
        return fName;
    }

    /**
     * Set the parent in this entry.
     * @param parent
     */
    public void setParent(DirectoryNode parent)
    {
        fParent = parent;
    }

    /**
     * Get the parent in this entry.
     * @return 
     */
    public DirectoryNode getParent()
    {
        return fParent;
    }

    /**
     * Set the child in this entry.
     * @param child
     */
    public void setChild(AVMNode child)
    {
        fChild = child;
    }

    /**
     * Get the child in this entry.
     * @return
     */
    public AVMNode getChild()
    {
        return fChild;
    }

    /**
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ChildEntry))
        {
            return false;
        }
        ChildEntry other = (ChildEntry)obj;
        return fName.equals(other.getName()) && fParent.equals(other.getParent());
    }

    /**
     * @return
     */
    @Override
    public int hashCode()
    {
        return fName.hashCode() + fParent.hashCode();
    }
}
