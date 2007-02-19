/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.importer;

import java.io.File;
import java.io.FileFilter;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface to load files and directories into the hub.
 * All will be created as new - there is no detection if a file exists or has changed etc..
 * 
 * @author andyh
 */
public interface FileImporter
{
    /**
     * Load a file or directory into the repository
     * 
     * @param container - the node into which to insert the file or directory
     * @param file - the start point for the import
     * @param recurse - if the start point is a directoty then recurse
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file, boolean recurse) throws FileImporterException;
    
    /**
     * Load all files or directories that match the file filter in the given directory
     * 
     * @param container
     * @param file
     * @param filter
     * @param recurse
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file, FileFilter filter, boolean recurse) throws FileImporterException;
    
    
    /**
     * Load a single file or directory without any recursion
     * 
     * @param container
     * @param file
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file) throws FileImporterException;

    /**
     * Load a file into a given location, giving it a new name.
     * 
     * @param container the target parent to load into
     * @param file the source file to upload
     * @param recurse true to recurse into subfolders
     * @param name the new name of the file or folder when it gets uploaded
     * @return Returns the number of files loaded
     * @throws FileImporterException
     */
    public int loadNamedFile(NodeRef container, File file, boolean recurse, String name) throws FileImporterException;
}
