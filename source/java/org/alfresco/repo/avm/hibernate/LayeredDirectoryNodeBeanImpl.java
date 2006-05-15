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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Layered directories are semitransparent links to other directories.  
 * They maintain a delta of what has been changed relative to what they
 * link to. 
 * @author britt
 */
public class LayeredDirectoryNodeBeanImpl extends DirectoryNodeBeanImpl implements LayeredDirectoryNodeBean
{
    /**
     * The Layer ID.
     */
    private long fLayerID;
    
    /**
     * The link to the underlying directory.
     */
    private String fIndirection;
    
    /**
     * The Map of nodes added in this layer.
     */
    private Map<String, DirectoryEntry> fAdded;

    /**
     * The Set of names that have been deleted.
     */
    private Set<String> fDeleted;
    
    /**
     * Whether this is a primary indirection node.
     */
    private boolean fPrimaryIndirection;
    
    /**
     * Anonymous constructor.
     */
    public LayeredDirectoryNodeBeanImpl()
    {
        super();
    }
    
    /**
     * Rich constructor.
     * @param id The id to assign.
     * @param versionID The version id.
     * @param branchID The branch id.
     * @param ancestor The ancestor.
     * @param mergedFrom The node that merged into us.
     * @param parent The parent node.
     * @param layerID The layer id of this node.
     * @param indirection The indirection pointer of this.
     */
    public LayeredDirectoryNodeBeanImpl(long id,
                                        long versionID,
                                        long branchID,
                                        AVMNodeBean ancestor,
                                        AVMNodeBean mergedFrom,
                                        DirectoryNodeBean parent,
                                        RepositoryBean repository,
                                        BasicAttributesBean attrs,
                                        long layerID,
                                        String indirection)
    {
        super(id, versionID, branchID, ancestor, mergedFrom, parent, repository, attrs);
        fLayerID = layerID;
        fIndirection = indirection;
        fAdded = new HashMap<String, DirectoryEntry>();
        fDeleted = new HashSet<String>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#setLayerID(int)
     */
    public void setLayerID(long id)
    {
        fLayerID = id;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#getLayerID()
     */
    public long getLayerID()
    {
        return fLayerID;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#setIndirection(java.lang.String)
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#getIndirection()
     */
    public String getIndirection()
    {
        return fIndirection;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#setAdded(java.util.Map)
     */
    public void setAdded(Map<String, DirectoryEntry> added)
    {
        fAdded = added;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#getAdded()
     */
    public Map<String, DirectoryEntry> getAdded()
    {
        return fAdded;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#setDeleted(java.util.Set)
     */
    public void setDeleted(Set<String> deleted)
    {
        fDeleted = deleted;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.LayeredDirectoryNode#getDeleted()
     */
    public Set<String> getDeleted()
    {
        return fDeleted;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.LayeredDirectoryNodeBean#getPrimaryIndirection()
     */
    public boolean getPrimaryIndirection()
    {
        return fPrimaryIndirection;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.LayeredDirectoryNodeBean#setPrimaryIndirection(boolean)
     */
    public void setPrimaryIndirection(boolean primary)
    {
        fPrimaryIndirection = primary;
    }
}
