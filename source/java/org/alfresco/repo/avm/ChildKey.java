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
 * The key to a ChildEntry, a Parent and a name.
 * @author britt
 */
public class ChildKey implements Serializable
{
    private static final long serialVersionUID = 2033634095972856432L;

    /**
     * The Parent.
     */
    private DirectoryNode fParent;
    
    /**
     * The child's name.
     */
    private String fName;
    
    /**
     * Construct one with parameters.
     * @param parent The parent directory.
     * @param name The name of the child.
     */
    public ChildKey(DirectoryNode parent, String name)
    {
        fParent = parent;
        fName = name;
    }

    /**
     * A Default Constructor.
     */
    public ChildKey()
    {
    }
    
    /**
     * Set the parent.
     */
    public void setParent(DirectoryNode parent)
    {
        fParent = parent;
    }
    
    /**
     * Get the parent.
     * @return A DirectoryNode.
     */
    public DirectoryNode getParent()
    {
        return fParent;
    }
    
    /**
     * Set the name.
     */
    public void setName(String name)
    {
        fName = name;
    }
    
    /**
     * Get the name.
     */
    public String getName()
    {
        return fName;
    }
    
    /**
     * Override of equals.
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof ChildKey))
        {
            return false;
        }
        ChildKey o = (ChildKey)other;
        return fParent.equals(o.getParent()) &&
               fName.equals(o.getName());
    }
    
    /**
     * Override of hashCode.
     */
    public int hashCode()
    {
        return fParent.hashCode() + fName.hashCode();
    }
}
