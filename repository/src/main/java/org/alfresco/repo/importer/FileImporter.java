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
package org.alfresco.repo.importer;

import java.io.File;
import java.io.FileFilter;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface to load files and directories into the hub. All will be created as new - there is no detection if a file exists or has changed etc..
 * 
 * @author andyh
 */
public interface FileImporter
{
    /**
     * Load a file or directory into the repository
     * 
     * @param container
     *            - the node into which to insert the file or directory
     * @param file
     *            - the start point for the import
     * @param recurse
     *            - if the start point is a directoty then recurse
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file, boolean recurse) throws FileImporterException;

    /**
     * Load all files or directories that match the file filter in the given directory
     * 
     * @param container
     *            NodeRef
     * @param file
     *            File
     * @param filter
     *            FileFilter
     * @param recurse
     *            boolean
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file, FileFilter filter, boolean recurse) throws FileImporterException;

    /**
     * Load a single file or directory without any recursion
     * 
     * @param container
     *            NodeRef
     * @param file
     *            File
     * @return Returns the number of successfully imported files and directories
     * @throws FileImporterException
     */
    public int loadFile(NodeRef container, File file) throws FileImporterException;

    /**
     * Load a file into a given location, giving it a new name.
     * 
     * @param container
     *            the target parent to load into
     * @param file
     *            the source file to upload
     * @param recurse
     *            true to recurse into subfolders
     * @param name
     *            the new name of the file or folder when it gets uploaded
     * @return Returns the number of files loaded
     * @throws FileImporterException
     */
    public int loadNamedFile(NodeRef container, File file, boolean recurse, String name) throws FileImporterException;
}
