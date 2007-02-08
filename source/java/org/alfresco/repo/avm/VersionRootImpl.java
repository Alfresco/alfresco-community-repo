/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.repo.avm;

import java.io.Serializable;


/**
 * Hold a single version root.
 * @author britt
 */
class VersionRootImpl implements VersionRoot, Serializable
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
     * The AVMStore.
     */
    private AVMStore fAVMStore;
    
    /**
     * The root node.
     */
    private DirectoryNode fRoot;

    /**
     * The short description.
     */
    private String fTag;

    /**
     * The thick description.
     */
    private String fDescription;
    
    /**
     * A default constructor.
     */
    public VersionRootImpl()
    {
    }
    
    /**
     * Rich constructor.
     * @param store
     * @param root
     * @param versionID
     * @param createDate
     * @param creator
     */
    public VersionRootImpl(AVMStore store,
                           DirectoryNode root,
                           int versionID,
                           long createDate,
                           String creator,
                           String tag,
                           String description)
    {
        fAVMStore = store;
        fRoot = root;
        fVersionID = versionID;
        fCreateDate = createDate;
        fCreator = creator;
        fTag = tag;
        fDescription = description;
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

    public AVMStore getAvmStore()
    {
        return fAVMStore;
    }

    public void setAvmStore(AVMStore store)
    {
        fAVMStore = store;
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
     * Check equality.  Based on AVMStore equality and version id equality.
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
        if (!(obj instanceof VersionRoot))
        {
            return false;
        }
        VersionRoot other = (VersionRoot)obj;
        return fAVMStore.equals(other.getAvmStore())
            && fVersionID == other.getVersionID();
    }

    /**
     * Generate a hash code.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return fAVMStore.hashCode() + fVersionID;
    }
    
    /**
     * Get the tag (short description).
     * @return The tag.
     */
    public String getTag()
    {
        return fTag;
    }
    
    /**
     * Set the tag (short description).
     * @param tag The short description.
     */
    public void setTag(String tag)
    {
        fTag = tag;
    }
    
    /**
     * Get the thick description.
     * @return The thick description.
     */
    public String getDescription()
    {
        return fDescription;
    }
    
    /**
     * Set the thick description.
     * @param description The thick discription.
     */
    public void setDescription(String description)
    {
        fDescription = description;
    }
}

