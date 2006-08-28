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
 * A record of a node that is new and in which store it resides.
 * @author britt
 */
class NewInAVMStoreImpl implements NewInAVMStore, Serializable
{
    private static final long serialVersionUID = 1905996612150732182L;

    /**
     * The Primary Key.
     */
    private Long fID;
    
    /**
     * The AVMStore.
     */
    private AVMStore fAVMStore;
    
    /**
     * The Node.
     */
    private AVMNode fNode;
    
    /**
     * Default constructor.
     */
    public NewInAVMStoreImpl()
    {
    }

    /**
     * Make a new one.
     * @param store The store.
     * @param node The AVMNode that is new.
     */
    public NewInAVMStoreImpl(AVMStore store, AVMNode node)
    {
        fAVMStore = store;
        fNode = node;
    }

    /**
     * @return the fNode
     */
    public AVMNode getNode()
    {
        return fNode;
    }

    /**
     * @param node the fNode to set
     */
    public void setNode(AVMNode node)
    {
        fNode = node;
    }

    /**
     * @return the store
     */
    public AVMStore getAvmStore()
    {
        return fAVMStore;
    }

    /**
     * @param store the AVMStore to set
     */
    public void setAvmStore(AVMStore store)
    {
        fAVMStore = store;
    }
    
    /**
     * Set the primary key. (For Hibernate)
     * @param id The primary key.
     */
    protected void setId(Long id)
    {
        fID = id;
    }
    
    /**
     * Get the primary key. (For Hibernate)
     * @return The primary key.
     */
    protected Long getId()
    {
        return fID;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof NewInAVMStore))
        {
            return false;
        }
        NewInAVMStore o = (NewInAVMStore)other;
        return fAVMStore.equals(o.getAvmStore()) &&
               fNode.equals(o.getNode());
    }
    
    @Override
    public int hashCode()
    {
        return fAVMStore.hashCode() + fNode.hashCode();
    }
}

