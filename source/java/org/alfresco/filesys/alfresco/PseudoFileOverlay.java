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

import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.service.cmr.repository.NodeRef;

public interface PseudoFileOverlay
{
    /**
     * Is this a pseudo file?
     * @param parentDir NodeRef
     * @param name String
     * @return true the file is a pseudo file
     */
    public boolean isPseudoFile(NodeRef parentDir, String name);
    
    /**
     * Get the pseudo file
     * @param parentDir NodeRef
     * @param name String
     * @return the pseudoFile or null if there is no pseudo file
     */
    public PseudoFile getPseudoFile(NodeRef parentDir, String name);
    
    /**
     * Search for the pseudo files on the specified path
     * @param parentDir NodeRef
     * @param name String
     * @return list of pseudo files.
     */
    public PseudoFileList searchPseudoFiles(NodeRef parentDir, String name);
    
    /**
     * Delete a pseudo file.   
     * 
     * Pseudo files may need to be deleted for delete folder operations to work 
     * correctly.  
     * 
     * A pseudo file can be deleted for a short time.  However it may re-appear at some point 
     * later since there is no permanent persistence of pseudo files which are ephemeral!     
     */
    public void delete(NodeRef parentDir, String name);
 
}
