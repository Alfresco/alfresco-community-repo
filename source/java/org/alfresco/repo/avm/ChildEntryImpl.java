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
     * The key.
     */
    private ChildKey fKey;
    
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
     * @param key The ChildKey.
     * @param child The child.
     */
    public ChildEntryImpl(ChildKey key,
                          AVMNode child)
    {
        fKey = key;
        fChild = child;
    }

    /**
     * Set the key for this ChildEntry.
     * @param key The ChildKey.
     */
    public void setKey(ChildKey key)
    {
        fKey = key;
    }
    
    /**
     * Get the ChildKey for this ChildEntry.
     * @return
     */
    public ChildKey getKey()
    {
        return fKey;
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
     * @return The child.
     */
    public AVMNode getChild()
    {
        return fChild;
    }

    /**
     * Equals override.
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
        if (!(obj instanceof ChildEntry))
        {
            return false;
        }
        ChildEntry other = (ChildEntry)obj;
        return fKey.equals(other.getKey());
    }

    /**
     * Get the hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fKey.hashCode();
    }
}
