/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.server.pseudo;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.TreeConnection;

/**
 * Pseudo File Interface
 * 
 * <p>Provides the ability to add files into the file listing of a folder.
 * 
 * @author gkspencer
 */
public interface PseudoFileInterface
{
    /**
     * Check if the specified path refers to a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return boolean
     */
    public boolean isPseudoFile(SrvSession sess, TreeConnection tree, String path);

    /**
     * Return the pseudo file for the specified path, or null if the path is not a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return PseudoFile
     */
    public PseudoFile getPseudoFile(SrvSession sess, TreeConnection tree, String path);
    
    /**
     * Add pseudo files to a folder so that they appear in a folder search
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return int
     */
    public int addPseudoFilesToFolder(SrvSession sess, TreeConnection tree, String path);
    
    /**
     * Delete a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     */
    public void deletePseudoFile(SrvSession sess, TreeConnection tree, String path);
}
