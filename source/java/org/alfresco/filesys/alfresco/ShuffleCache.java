/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.alfresco;

/**
 * Cache for alfresco "save shuffles" which are used by some applications
 * to compensate for a most computer filesystem being non atomic. 
 * 
 * <p>
 * Overlays an Alfresco repository with temporary files being created and 
 * soft deleted from folders that are likely to have save shuffles going on.
 * <p>
 * Implementations must be thread safe 
 */
public interface ShuffleCache
{
    
    /**
     * Add a new temporary file to the "shuffle cache".   Content is not persisted 
     * in the alfresco repo until either a rename occurs or after a time delay.
     */
    public void createTemporaryFile(String path);
    
    /**
     * Soft delete a file. The file may be re-instated later or the delete made
     * permenant after a time delay.
     */
    public void softDelete(String path);
    
    /**
     * Takes the contents of a temporary file and applies it to the new path.
     * <p>
     * If the new path has been soft deleted then the soft delete is removed.
     * <p>
     * After the contents of the temporary file have been written the it may may be made 
     * available for garbage collection.
     * 
     * @param oldPath the location of the temporaryFile
     * @param newPath the location of the new file.
     */
    public void renameTemporaryFile(String oldPath, String newPath);
    
    /**
     * Does the specified directory contain a shuffled temporary file
     * @param dir String
     * @return boolean
     */
    boolean isShuffleDirectory(String dir);
    
    /**
     * Has the path been "soft deleted"
     */
    boolean isDeleted(String path);
    
    /**
     * Has the path been "soft created"
     * @param path String
     * @return boolean
     */
    boolean isCreated(String path);
}
