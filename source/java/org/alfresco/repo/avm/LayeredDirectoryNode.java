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

import org.alfresco.repo.domain.permissions.Acl;

/**
 * Interface for Layered Directories.
 * @author britt
 */
public interface LayeredDirectoryNode extends DirectoryNode, Layered
{
    /**
     * Does this node have a primary indirection.
     * @return Whether this is a primary indirection.
     */
    public boolean getPrimaryIndirection();

    /**
     * Set whether this has a primary indirection.
     * @param has Whether this has a primary indirection.
     */
    public void setPrimaryIndirection(boolean has);

    /**
     * Get the layer id for this node.
     * @return The layer id.
     */
    public long getLayerID();

    /**
     * Set the layer id for this node.
     * @param id The id to set.
     */
    public void setLayerID(long id);

    /**
     * Set this to be a primary indirection from the path
     * passed in.
     * @param path The indirection path.
     */
    public void rawSetPrimary(Lookup lPath, String path);

    /**
     * Turn this node into a primary indirection node with the indirection
     * taken from the Lookup passed in.
     * Performs a copy on write.
     * @param lPath
     */
    public void turnPrimary(Lookup lPath);

    /**
     * Retarget this directory.
     * @param lPath The Lookup.
     * @param target The new target path.
     */
    public void retarget(Lookup lPath, String target);

    /**
     * Make visible a node deleted in a layer.
     * @param lPath The Lookup.
     * @param name The name to make visible.
     */
    public void uncover(Lookup lPath, String name);
    
    /**
     * Remove name without leaving behind a deleted node.
     * @param name The name of the child to flatten.
     */
    public void flatten(String name);
    
    /**
     * Set the indirection.
     * @param indirection
     */
    public void setIndirection(String indirection);
    
    /**
     * Get the indirection version.
     * @return The indirection version.
     */
    public Integer getIndirectionVersion();
    
    /**
     * Set the opacity of this.
     * @param opacity Whether this should be opaque, i.e. not see the things it
     * in its indirection.
     */
    public void setOpacity(boolean opacity);
    
    /**
     * Get the opacity of this.
     * @return The opacity.
     */
    public boolean getOpacity();
    
    public void setAclAndInherit(LayeredDirectoryNode layeredDirectory, Acl acl, String name);
}