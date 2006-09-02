/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
