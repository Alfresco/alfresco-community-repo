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

import org.alfresco.service.namespace.QName;

/**
 * Simple bean that implements AVMAspectName.
 * @author britt
 */
class AVMAspectNameImpl implements AVMAspectName, Serializable
{
    private static final long serialVersionUID = -6282415309583571934L;

    /**
     * The Primary Key.
     */
    private Long fID;
    
    /**
     * The Node that has the named aspect.
     */
    private AVMNode fNode;
    
    /**
     * The name of the Aspect.
     */
    private QName fName;

    /**
     * Default constructor.
     */
    public AVMAspectNameImpl()
    {
    }
    
    /**
     * Set the node that has the Aspect. 
     * @param node The node.
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }
    
    /**
     * Get the node that has this Aspect name.
     * @return The AVM Node.
     */
    public AVMNode getNode()
    {
        return fNode;
    }
    
    /**
     * Set the name of the Aspect.
     * @param name The QName of the Aspect.
     */
    public void setName(QName name)
    {
        fName = name;
    }
    
    /**
     * Get the name of this Aspect.
     * @return The QName of this aspect.
     */
    public QName getName()
    {
        return fName;
    }

    /**
     * Set the primary key (For Hibernate)
     * @param id The primary key.
     */
    protected void setId(Long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key (For Hibernate)
     * @return The primary key.
     */
    protected Long getId()
    {
        return fID;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMAspectName))
        {
            return false;
        }
        AVMAspectName o = (AVMAspectName)obj;
        return fNode.equals(o.getNode()) && fName.equals(o.getName());
    }

    @Override
    public int hashCode()
    {
        return fNode.hashCode() + fName.hashCode();
    }
}
