/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
    @Auditable(parameters = {"repositoryDestination", "packageName"})
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
