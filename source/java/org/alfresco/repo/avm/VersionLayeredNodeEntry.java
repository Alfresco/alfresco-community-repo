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
 * When a snapshot is created we stow away all of the layered
 * nodes that were frozen by the snapshot so that subsequent 
 * snapshots can find them and force copies.
 * @author britt
 */
public interface VersionLayeredNodeEntry
{
    /**
     * Get the VersionRoot for this entry.
     * @return The VersionRoot for this entry.
     */
    public VersionRoot getVersion();
    
    /**
     * Get the path to this entries Layered Node.  This
     * is a store relative path.
     * @return The path.
     */
    public String getPath();
    
    /**
     * Get the MD5 sum of the path.
     * @return
     */
    public String getMd5Sum();
}
