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
 * Hold a single version root.
 * @author britt
 */
public class VersionRootImpl implements VersionRoot, Serializable
{
    static final long serialVersionUID = 8826954538210455917L;
    
    /**
     * The object id
     */
    private Long fID;
    
    /**
     * The version id.
     */
    private int fVersionID;
    
    /**
     * The creation date.
     */
    private long fCreateDate;
    
    /**
     * The creator.
     */
    private String fCreator;
    
    /**
     * The Repository.
     */
    private Repository fRepository;
    
    /**
     * The root node.
     */
    private DirectoryNode fRoot;

    /**
     * A default constructor.
     */
    public VersionRootImpl()
    {
    }
    
    /**
     * Rich constructor.
     * @param repository
     * @param root
     * @param versionID
     * @param createDate
     * @param creator
     */
    public VersionRootImpl(Repository repository,
                               DirectoryNode root,
                               int versionID,
                               long createDate,
                               String creator)
    {
        fRepository = repository;
        fRoot = root;
        fVersionID = versionID;
        fCreateDate = createDate;
        fCreator = creator;
    }
    
    public long getCreateDate()
    {
        return fCreateDate;
    }

    public void setCreateDate(long createDate)
    {
        fCreateDate = createDate;
    }

    public String getCreator()
    {
        return fCreator;
    }

    public void setCreator(String creator)
    {
        fCreator = creator;
    }

    public long getId()
    {
        return fID;
    }

    public void setId(long id)
    {
        fID = id;
    }

    public Repository getRepository()
    {
        return fRepository;
    }

    public void setRepository(Repository repository)
    {
        fRepository = repository;
    }

    public DirectoryNode getRoot()
    {
        return fRoot;
    }

    public void setRoot(DirectoryNode root)
    {
        fRoot = root;
    }
    
    /**
     * Set the versionID.
     * @param versionID
     */
    public void setVersionID(int versionID)
    {
        fVersionID = versionID;
    }
    
    /**
     * Get the version id.
     * @return The version id.
     */
    public int getVersionID()
    {
        return fVersionID;
    }

    /**
     * Check equality.  Based on Repository equality and version id equality.
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
        if (!(obj instanceof VersionRoot))
        {
            return false;
        }
        VersionRoot other = (VersionRoot)obj;
        return fRepository.equals(other.getRepository())
            && fVersionID == other.getVersionID();
    }

    /**
     * Generate a hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fRepository.hashCode() + fVersionID;
    }
}

