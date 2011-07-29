/*
 * Copyright (C) 2007-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.service.cmr.repository.NodeRef;

public interface PseudoFileOverlay
{
    /**
     * Is this a pseudo file?
     * @param path the path of the file
     * @return true the file is a pseudo file
     */
    public boolean isPseudoFile(NodeRef parentDir, String name);
    
    /**
     * Get the pseudo file
     * @param path the path of the file
     * @return the pseudoFile or null if there is no pseudo file
     */
    public PseudoFile getPseudoFile(NodeRef parentDir, String name);
    
    /**
     * Search for the pseudo files on the specified path
     * @param path the path
     * @return list of pseudo files.
     */
    public PseudoFileList searchPseudoFiles(NodeRef parentDir, String name);
 
}
