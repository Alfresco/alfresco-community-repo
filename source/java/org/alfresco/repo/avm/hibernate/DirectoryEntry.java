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

package org.alfresco.repo.avm.hibernate;

import org.alfresco.repo.avm.AVMNodeType;

/**
 * This holds Directory Entries in directories.
 * @author britt
 */
public class DirectoryEntry
{
    /**
     * The type of entry a node is.
     */
    private int fType; 
    
    /**
     * This is the actual child Node.
     */
    private AVMNodeBean fChild;
    
    /**
     * Anonymous constructor.
     */
    public DirectoryEntry()
    {
    }
    
    /**
     * Make one from scratch.
     * @param type The type.
     * @param child The child node.
     */
    public DirectoryEntry(int type, AVMNodeBean child)
    {
        fType = type;
        fChild = child;
    }
    
    /** 
     * Set the child.
     * @param child The child to set.
     */
    public void setChild(AVMNodeBean child)
    {
        fChild = child;
    }
    
    /**
     * Get the child.
     * @return The child.
     */
    public AVMNodeBean getChild()
    {
        return fChild;
    }
    
    /**
     * Set the type by name.
     * @param type The type.
     */
    public void setType(int type)
    {
        fType = type;
    }
    
    /**
     * Get the type.
     */
    public int getType()
    {
        return fType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof DirectoryEntry))
        {
            return false;
        }
        return fChild.equals(((DirectoryEntry)obj).fChild);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fChild.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[" + fType + "] " + fChild.getId();
    }
}
