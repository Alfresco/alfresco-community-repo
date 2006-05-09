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


/**
 * Base interface for versioned and layered node implementation data objects.
 * @author britt
 */
public class AVMNodeBeanImpl implements AVMNodeBean
{
    /**
     * The Object ID (and Primary Key)
     */
    private long fID;

    /**
     * The Version ID
     */
    private long fVersionID;
    
    /**
     * The Branch ID
     */
    private long fBranchID;
    
    /**
     * The ancestor of this.
     */
    private AVMNodeBean fAncestor;
    
    /**
     * The node that was merged into this.
     */
    private AVMNodeBean fMergedFrom;
    
    /**
     * The id of the parent of this.
     */
    private DirectoryNodeBean fParent;
    
    /**
     * The Repository that owns this.
     */
    private RepositoryBean fRepository;
    
    /**
     * Whether this node is new (and should therefore not be COWed).
     */
    private boolean fIsNew;
    
    /**
     * The version number (for concurrency control).
     */
    private long fVers;
    
    /**
     * Anonymous constructor.
     */
    public AVMNodeBeanImpl()
    {
    }
    
    /**
     * Rich constructor.
     * @param id The ID to set.
     * @param versionID The version id.
     * @param branchID The branch id.
     * @param ancestor The ancestor.
     * @param mergedFrom The node that merged into us.
     * @param parent The parent.
     * @param repository The repository.
     */
    public AVMNodeBeanImpl(long id,
                           long versionID,
                           long branchID,
                           AVMNodeBean ancestor,
                           AVMNodeBean mergedFrom,
                           DirectoryNodeBean parent,
                           RepositoryBean repository)
    {
        fID = id;
        fVersionID = versionID;
        fBranchID = branchID;
        fAncestor = ancestor;
        fMergedFrom = mergedFrom;
        fParent = parent;
        fRepository = repository;
        fIsNew = true;
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
        if (!(obj instanceof AVMNodeBean))
        {
            return false;
        }
        return fID == ((AVMNodeBean)obj).getId();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setId(int)
     */
    public void setId(long id)
    {
        fID = id;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getId()
     */
    public long getId()
    {
        return fID;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setVersionID(int)
     */
    public void setVersionID(long id)
    {
        fVersionID = id;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getVersionID()
     */
    public long getVersionID()
    {
        return fVersionID;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setParent(int)
     */
    public void setParent(DirectoryNodeBean parent)
    {
        fParent = parent;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getParent()
     */
    public DirectoryNodeBean getParent()
    {
        return fParent;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setAncestor(int)
     */
    public void setAncestor(AVMNodeBean ancestor)
    {
        fAncestor = ancestor;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getAncestor()
     */
    public AVMNodeBean getAncestor()
    {
        return fAncestor;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setMergedFrom(int)
     */
    public void setMergedFrom(AVMNodeBean mergedFrom)
    {
        fMergedFrom = mergedFrom;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getMergedFrom()
     */
    public AVMNodeBean getMergedFrom()
    {
        return fMergedFrom;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#setBranchID(int)
     */
    public void setBranchID(long branchID)
    {
        fBranchID = branchID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNode#getBranchID()
     */
    public long getBranchID()
    {
        return fBranchID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNodeBean#getRepository()
     */
    public RepositoryBean getRepository()
    {
        return fRepository;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.AVMNodeBean#setRepository(org.alfresco.proto.avm.RepositoryBean)
     */
    public void setRepository(RepositoryBean repository)
    {
        fRepository = repository;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.AVMNodeBean#getIsNew()
     */
    public boolean getIsNew()
    {
        return fIsNew;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.AVMNodeBean#setIsNew(boolean)
     */
    public void setIsNew(boolean isNew)
    {
        fIsNew = isNew;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.AVMNodeBean#getVers()
     */
    public long getVers()
    {
        return fVers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.AVMNodeBean#setVers(java.lang.int)
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }
}
