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
    public long getId();

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
}