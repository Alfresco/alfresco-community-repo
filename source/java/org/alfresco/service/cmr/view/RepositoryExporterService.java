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
package org.alfresco.service.cmr.view;

import java.io.File;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Repository Export Service
 * 
 * @author davidc
 */
@PublicService
public interface RepositoryExporterService
{

    /**
     * Export complete Repository.
     * 
     * Each store is exported to its own temporary .acp file
     * 
     * @param packageName package name prefix for export .acp files
     * @return list of temporary export files
     */
    @Auditable(parameters = {"packageName"})
    public FileExportHandle[] export(String packageName);

    /**
     * Export complete Repository.
     * 
     * Each store is exported to a file held in the Repository.
     * 
     * @param repositoryDestination  location within Repository to hold .acp files
     * @param packageName package name prefix for export .acp files
     * @return  list of repository held export files
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"repositoryDestination", "packageName"})
    public RepositoryExportHandle[] export(NodeRef repositoryDestination, String packageName);
    
    /**
     * Export complete Repository.
     * 
     * @param directoryDestination  location within File System to hold .acp files
     * @param packageName package name prefix for export .acp files
     * @return  list of export files
     */
    @Auditable(parameters = {"directoryDestination", "packageName"})
    public FileExportHandle[] export(File directoryDestination, String packageName);


    /**
     * General Export Handle
     * 
     * @author davidc
     */
    public class ExportHandle
    {
        public StoreRef storeRef;
        public String packageName;
        public String mimeType; 
    }

    /**
     * File Exort Handle
     * 
     * @author davidc
     */
    public class FileExportHandle extends ExportHandle
    {
        public File exportFile;
    }

    /**
     * Repository File Export Handle
     *
     * @author davidc
     */
    public class RepositoryExportHandle extends ExportHandle
    {
        public NodeRef exportFile;
    }

}
