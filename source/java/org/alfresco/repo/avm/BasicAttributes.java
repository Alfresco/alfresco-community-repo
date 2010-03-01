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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.avm;

/**
 * Ownership, timestamps, later perhaps ACLs
 * @author britt
 */
public interface BasicAttributes
{
    /**
     * Set the creator of the node.
     * @param creator The creator to set.
     */
    public void setCreator(String creator);
    
    /**
     * Get the creator of the node.
     * @return The creator.
     */
    public String getCreator();
    
    /**
     * Set the owner of the node.
     * @param owner The owner to set.
     */
    public void setOwner(String owner);
    
    /**
     * Get the owner of the node.
     * @return The owner.
     */
    public String getOwner();
    
    /**
     * Set the last modifier of the node.
     * @param lastModifier
     */
    public void setLastModifier(String lastModifier);
    
    /**
     * Get the last modifier of the node.
     * @return The last modifier.
     */
    public String getLastModifier();
    
    /**
     * Set the create date.
     * @param createDate The date to set.
     */
    public void setCreateDate(long createDate);
    
    /**
     * Get the create date.
     * @return The create date.
     */
    public long getCreateDate();
    
    /**
     * Set the modification date.
     * @param modDate The date to set.
     */
    public void setModDate(long modDate);
    
    /**
     * Get the modification date.
     * @return The modification date.
     */
    public long getModDate();
    
    /**
     * Set the access date of the node.
     * @param accessDate The access date.
     */
    public void setAccessDate(long accessDate);
    
    /**
     * Get the access date of the node.
     * @return The access date.
     */
    public long getAccessDate();
}
