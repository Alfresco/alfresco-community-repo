/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.repo.avm;

/**
 * Represents a single version root.
 * @author britt
 */
public interface VersionRoot
{
    /**
     * @return the createDate
     */
    public long getCreateDate();

    /**
     * @param createDate the createDate to set
     */
    public void setCreateDate(long createDate);

    /**
     * @return the creator
     */
    public String getCreator();

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator);

    /**
     * @return the id
     */
    public Long getId();

    /**
     * @param id the id to set
     */
    public void setId(long id);

    /**
     * @return the AVMStore
     */
    public AVMStore getAvmStore();

    /**
     * @param store the store to set
     */
    public void setAvmStore(AVMStore store);

    /**
     * @return the root
     */
    public DirectoryNode getRoot();

    /**
     * @param root the root to set
     */
    public void setRoot(DirectoryNode root);
    
    /**
     * Set the version id.
     * @param versionID
     */
    public void setVersionID(int versionID);
    
    /**
     * Get the version id.
     * @return The version id.
     */
    public int getVersionID();
    
    /**
     * Get the tag (short description).
     * @return The tag.
     */
    public String getTag();
    
    /**
     * Get the thick description.
     * @return The thick description.
     */
    public String getDescription();
    
    /**
     * Set the tag.
     * @param tag
     */
    public void setTag(String tag);
    
    /**
     * Set the description.
     * @param description
     */
    public void setDescription(String description);
}