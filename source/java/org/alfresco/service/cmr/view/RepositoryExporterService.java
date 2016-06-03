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
