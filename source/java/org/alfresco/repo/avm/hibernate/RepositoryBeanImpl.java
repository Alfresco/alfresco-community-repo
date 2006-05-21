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
 * @author britt
 *
 */
public class RepositoryBeanImpl implements RepositoryBean
{
    /**
     * The name of this repository.
     */
    private String fName;
    
    /**
     * The current root directory.
     */
    private DirectoryNodeBean fRoot;
    
    /**
     * The root directories for all versions.
     */
    private Map<Integer, DirectoryNodeBean> fRoots;
    
    /**
     * The next version id.
     */
    private int fNextVersionID;
    
    /**
     * The nodes that are new since the last end operation.
     */
    private Set<AVMNodeBean> fNewNodes;
    
    /**
     * The version (for concurrency control).
     */
    private long fVers;
    
    /**
     * Anonymous constructor.
     */
    public RepositoryBeanImpl()
    {
    }
    
    /**
     *  Rich constructor
     *  @param name The name of the Repository.
     *  @param root The current root node.
     */
    public RepositoryBeanImpl(String name,
                              DirectoryNodeBean root)
    {
        fName = name;
        fNextVersionID = 0;
        fRoot = root;
        fRoots = new HashMap<Integer, DirectoryNodeBean>();
        fNewNodes = new HashSet<AVMNodeBean>();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#getName()
     */
    public String getName()
    {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#setName(java.lang.String)
     */
    public void setName(String name)
    {
        fName = name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#setRoot(org.alfresco.proto.avm.DirectoryNode)
     */
    public void setRoot(DirectoryNodeBean root)
    {
        fRoot = root;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#getRoot()
     */
    public DirectoryNodeBean getRoot()
    {
        return fRoot;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#setRoots(java.util.Map)
     */
    public void setRoots(Map<Integer, DirectoryNodeBean> roots)
    {
        fRoots = roots;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#getRoots()
     */
    public Map<Integer, DirectoryNodeBean> getRoots()
    {
        return fRoots;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#setNextVersionID(int)
     */
    public void setNextVersionID(int nextVersionID)
    {
        fNextVersionID = nextVersionID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Repository#getNextVersionID()
     */
    public int getNextVersionID()
    {
        return fNextVersionID;
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
        if (!(obj instanceof RepositoryBean))
        {
            return false;
        }
        return fName.equals(((RepositoryBean)obj).getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fName.hashCode();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#getNewNodes()
     */
    public Set<AVMNodeBean> getNewNodes()
    {
        return fNewNodes;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#setNewNodes(java.util.Set)
     */
    public void setNewNodes(Set<AVMNodeBean> newNodes)
    {
        fNewNodes = newNodes;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#getVers()
     */
    public long getVers()
    {
        return fVers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.RepositoryBean#setVers(java.lang.int)
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }
}
