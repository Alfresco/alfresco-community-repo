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
package org.alfresco.repo.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.importer.system.SystemExporterImporter;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ReferenceType;
import org.alfresco.service.cmr.view.RepositoryExporterService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;


/**
 * Full Repository Export Service
 *  
 * @author davidc
 */
public class RepositoryExporterComponent implements RepositoryExporterService
{
    private static final String STOREREF_KEY = "storeRef";
    private static final String PACKAGENAME_KEY = "packageName";
    private static final String INCLUDED_PATHS = "includedPaths";
    
    // component dependencies
    private ExporterService exporterService;
    private MimetypeService mimetypeService;
    private FileFolderService fileFolderService;
    private SystemExporterImporter systemExporterImporter;
    private NodeService nodeService;
    private List<Properties> exportStores;
    

    public void setExporterService(ExporterService exporterService)
    {
        this.exporterService = exporterService;
    }
    
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setSystemExporter(SystemExporterImporter systemExporterImporter)
    {
        this.systemExporterImporter = systemExporterImporter;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setStores(List<Properties> exportStores)
    {
        this.exportStores = exportStores;
    }
    
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.view.RepositoryExporterService#export()
     */
    public FileExportHandle[] export(String packageName)
    {
        List<FileExportHandle> exportHandles = exportStores(exportStores, packageName, new TempFileExporter());
        return exportHandles.toArray(new FileExportHandle[exportHandles.size()]);
    }


    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.view.RepositoryExporterService#export(java.io.File)
     */
    public FileExportHandle[] export(File directoryDestination, String packageName)
    {
        ParameterCheck.mandatory("directoryDestination", directoryDestination);
        if (!directoryDestination.isDirectory())
        {
            throw new ExporterException("Export location " + directoryDestination.getAbsolutePath() + " is not a directory");
        }
        
        List<FileExportHandle> exportHandles = exportStores(exportStores, packageName, new FileExporter(directoryDestination));
        return exportHandles.toArray(new FileExportHandle[exportHandles.size()]);
    }

    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.view.RepositoryExporterService#export(org.alfresco.service.cmr.repository.NodeRef)
     */
    public RepositoryExportHandle[] export(NodeRef repositoryDestination, String packageName)
    {
        ParameterCheck.mandatory("repositoryDestination", repositoryDestination);
        FileInfo destInfo = fileFolderService.getFileInfo(repositoryDestination);
        if (destInfo == null || !destInfo.isFolder())
        {
            throw new ExporterException("Repository destination " + repositoryDestination + " is not a folder.");
        }

        List<FileExportHandle> exportHandles = exportStores(exportStores, packageName, new TempFileExporter());
        Map<String, String> mimetypeExtensions = mimetypeService.getExtensionsByMimetype();
        List<RepositoryExportHandle> repoExportHandles = new ArrayList<RepositoryExportHandle>(exportHandles.size());
        for (FileExportHandle exportHandle : exportHandles)
        {
            String name = exportHandle.packageName + "." + mimetypeExtensions.get(exportHandle.mimeType);
            String title = exportHandle.packageName;
            String description;
            if (exportHandle.storeRef != null)
            {
                description = I18NUtil.getMessage("export.store.package.description", new Object[] { exportHandle.storeRef.toString() });
            }
            else
            {
                description = I18NUtil.getMessage("export.generic.package.description");
            }
            
            NodeRef repoExportFile = addExportFile(repositoryDestination, name, title, description, exportHandle.mimeType, exportHandle.exportFile);
            RepositoryExportHandle handle = new RepositoryExportHandle();
            handle.storeRef = exportHandle.storeRef;
            handle.packageName = exportHandle.packageName;
            handle.mimeType = exportHandle.mimeType;
            handle.exportFile = repoExportFile;
            repoExportHandles.add(handle);
            
            // delete temporary export file
            exportHandle.exportFile.delete();
        }
        
        return repoExportHandles.toArray(new RepositoryExportHandle[repoExportHandles.size()]);
    }


    /**
     * Add a file system based .acp file into the repository
     *
     * @param repoDestination  location within repository to place .acp file
     * @param name   name
     * @param title  title
     * @param description  description
     * @param mimeType  mime type
     * @param exportFile  the .acp file
     * @return  node reference to import .acp file
     */
    private NodeRef addExportFile(NodeRef repoDestination, String name, String title, String description, String mimeType, File exportFile)
    {
        //
        // import temp file into repository
        //
    
        // determine if file already exists
        List<String> paths = new ArrayList<String>();
        paths.add(name);
        try
        {
            FileInfo fileInfo = fileFolderService.resolveNamePath(repoDestination, paths);
            // Note: file already exists - delete 
            fileFolderService.delete(fileInfo.getNodeRef());
        }
        catch (org.alfresco.service.cmr.model.FileNotFoundException e)
        {
            // Note: file does not exist - no need to delete
        }
        
        // create acp file in repository
        NodeRef exportFileNodeRef = null;
        try
        {
            FileInfo fileInfo = fileFolderService.create(repoDestination, name, ContentModel.TYPE_CONTENT);
            ContentWriter writer = fileFolderService.getWriter(fileInfo.getNodeRef());
            writer.setMimetype(mimeType);
            writer.putContent(exportFile);
            exportFileNodeRef = fileInfo.getNodeRef();
    
            // add a title for Web Client viewing
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
            titledProps.put(ContentModel.PROP_TITLE, title);
            titledProps.put(ContentModel.PROP_DESCRIPTION, description);
            nodeService.addAspect(exportFileNodeRef, ContentModel.ASPECT_TITLED, titledProps);
            
        }
        catch (FileExistsException e)
        {
            // Note: shouldn't get here
        }
        
        return exportFileNodeRef;
    }
    

    /**
     * Contract for exporting a repository
     * 
     * @author davidc
     *
     * @param <ExportHandleType>
     */
    private interface ExportStore<ExportHandleType extends ExportHandle>
    {
        public ExportHandleType exportStore(ExporterCrawlerParameters exportParameters, String packageName, Exporter progress);
        
        public ExportHandleType exportSystem(String packageName);
    }
    
    
    /**
     * Enumerate list of pre-configured Stores and export one by one
     * 
     * @param stores  the list of stores to export
     * @param packageName  package name
     * @param exportStore  the exporter call-back for handling the actual export
     * @return  the list export file handles
     */
    private <ExportHandleType extends ExportHandle> List<ExportHandleType> exportStores(List<Properties> stores, String packageName, ExportStore<ExportHandleType> exportStore)
    {
        List<ExportHandleType> exportHandles = new ArrayList<ExportHandleType>(stores.size() +1);

        // export repository system info
        {
            String completePackageName = (packageName == null) ? "systeminfo" : packageName + "_systeminfo";            
            ExportHandleType systemInfoHandle = exportStore.exportSystem(completePackageName);
            exportHandles.add(systemInfoHandle);
        }
        
        // export each store
        for (Properties store : stores)
        {
            // retrieve store reference to export
            String storeRefStr = (String)store.get(STOREREF_KEY);
            if (storeRefStr == null || storeRefStr.length() == 0)
            {
                throw new ExporterException("Store Reference has not been provided.");
            }
            StoreRef storeRef = new StoreRef(storeRefStr);

            // retrieve package name to export into
            String storePackageName = (String)store.get(PACKAGENAME_KEY);
            if (storePackageName == null || storePackageName.length() == 0)
            {
                storePackageName = storeRef.getIdentifier();
            }
            String completePackageName = (packageName == null) ? storePackageName : packageName + "_" + storePackageName;
            
            // retrieve included paths (optional)
            // note: the default exporter will currently include parents and children, relative to the path (to support bootstrap import of Dynamic Models)
            String includedPathsStr = (String)store.get(INCLUDED_PATHS);
            String[] includedPaths = (includedPathsStr != null ? includedPathsStr.split(",\\s*") : null);

            // now export
            // NOTE: For now, do not provide exporter progress
            ExporterCrawlerParameters exportParameters = getExportParameters(storeRef, includedPaths);
            ExportHandleType exportHandle = exportStore.exportStore(exportParameters, completePackageName, null);
            exportHandles.add(exportHandle);
        }
        
        return exportHandles;
    }
    
    
    /**
     * Get export parameters for exporting a complete store
     *  
     * @param storeRef  store reference to export
     * @return  the parameters for exporting the complete store
     */
    private ExporterCrawlerParameters getExportParameters(StoreRef storeRef, String[] includedPaths)
    {
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(new Location(storeRef));
        parameters.setCrawlSelf(true);
        parameters.setCrawlChildNodes(true);
        parameters.setCrawlContent(true);
        parameters.setCrawlAssociations(true);
        parameters.setCrawlNullProperties(true);
        parameters.setExcludeNamespaceURIs(new String[] {});
        parameters.setIncludedPaths(includedPaths);
        parameters.setReferenceType(ReferenceType.NODEREF);
        return parameters;
    }
    
    
    /**
     * Export a Store to a temporary file
     *  
     * @author davidc
     */
    private class TempFileExporter implements ExportStore<FileExportHandle>
    {
        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.exporter.RepositoryExporterComponent.ExportStore#exportStore(org.alfresco.service.cmr.view.ExporterCrawlerParameters, java.lang.String, org.alfresco.service.cmr.view.Exporter)
         */
        public FileExportHandle exportStore(ExporterCrawlerParameters exportParameters, String packageName, Exporter progress)
        {
            // create a temporary file to hold the acp export
            File systemTempDir = TempFileProvider.getSystemTempDir();
            File tempFile = TempFileProvider.createTempFile("repoExp" + packageName, "." + ACPExportPackageHandler.ACP_EXTENSION, systemTempDir);

            // create acp export handler around the temp file
            File dataFile = new File(packageName);
            File contentDir = new File(packageName);
            try
            {
                OutputStream outputStream = new FileOutputStream(tempFile);
                ACPExportPackageHandler acpHandler = new ACPExportPackageHandler(outputStream, dataFile, contentDir, mimetypeService);

                // export the store
                exporterService.exportView(acpHandler, exportParameters, progress);
            }
            catch(FileNotFoundException e)
            {
                tempFile.delete();
                throw new ExporterException("Failed to create temporary file for holding export of store " + exportParameters.getExportFrom().getStoreRef());
            }
                
            // return handle onto temp file
            FileExportHandle handle = new FileExportHandle();
            handle.storeRef = exportParameters.getExportFrom().getStoreRef();
            handle.packageName = packageName;
            handle.mimeType = MimetypeMap.MIMETYPE_ACP;
            handle.exportFile = tempFile;
            return handle;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.exporter.RepositoryExporterComponent.ExportStore#exportSystem()
         */
        public FileExportHandle exportSystem(String packageName)
        {
            // create a temporary file to hold the system info export
            File systemTempDir = TempFileProvider.getSystemTempDir();
            File tempFile = TempFileProvider.createTempFile("repoExpSystemInfo", ".xml", systemTempDir);

            try
            {
                OutputStream outputStream = new FileOutputStream(tempFile);
                systemExporterImporter.exportSystem(outputStream);
            }
            catch(FileNotFoundException e)
            {
                tempFile.delete();
                throw new ExporterException("Failed to create temporary file for holding export of system info");
            }
            
            // return handle onto temp file
            FileExportHandle handle = new FileExportHandle();
            handle.storeRef = null;
            handle.packageName = packageName;
            handle.mimeType = MimetypeMap.MIMETYPE_XML;
            handle.exportFile = tempFile;
            return handle;
        }
    };
    
    
    /**
     * Export a Store to a file in a specified folder
     *  
     * @author davidc
     */
    private class FileExporter implements ExportStore<FileExportHandle>
    {
        private File directoryDestination;
        
        /**
         * Construct
         * 
         * @param directoryDestination  destination file system folder to create export file
         */
        public FileExporter(File directoryDestination)
        {
            this.directoryDestination = directoryDestination;
        }
        
        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.exporter.RepositoryExporterComponent.ExportStore#exportStore(org.alfresco.service.cmr.view.ExporterCrawlerParameters, java.lang.String, org.alfresco.service.cmr.view.Exporter)
         */
        public FileExportHandle exportStore(ExporterCrawlerParameters exportParameters, String packageName, Exporter progress)
        {
            // create a file to hold the acp export
            File file = new File(directoryDestination, packageName + "." + ACPExportPackageHandler.ACP_EXTENSION);

            // create acp export handler around the temp file
            File dataFile = new File(packageName);
            File contentDir = new File(packageName);
            try
            {
                OutputStream outputStream = new FileOutputStream(file);
                ACPExportPackageHandler acpHandler = new ACPExportPackageHandler(outputStream, dataFile, contentDir, mimetypeService);

                // export the store
                exporterService.exportView(acpHandler, exportParameters, progress);
            }
            catch(FileNotFoundException e)
            {
                file.delete();
                throw new ExporterException("Failed to create file " + file.getAbsolutePath() + " for holding the export of store " + exportParameters.getExportFrom().getStoreRef());
            }
                
            // return handle onto temp file
            FileExportHandle handle = new FileExportHandle();
            handle.storeRef = exportParameters.getExportFrom().getStoreRef();
            handle.packageName = packageName;
            handle.mimeType = MimetypeMap.MIMETYPE_ACP;
            handle.exportFile = file;
            return handle;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.exporter.RepositoryExporterComponent.ExportStore#exportSystem()
         */
        public FileExportHandle exportSystem(String packageName)
        {
            // create a temporary file to hold the system info export
            File tempFile = TempFileProvider.createTempFile("repoExpSystemInfo", ".xml");

            try
            {
                OutputStream outputStream = new FileOutputStream(tempFile);
                systemExporterImporter.exportSystem(outputStream);
            }
            catch(FileNotFoundException e)
            {
                tempFile.delete();
                throw new ExporterException("Failed to create temporary file for holding export of system info");
            }
            
            // return handle onto temp file
            FileExportHandle handle = new FileExportHandle();
            handle.storeRef = null;
            handle.packageName = packageName;
            handle.mimeType = MimetypeMap.MIMETYPE_XML;
            handle.exportFile = tempFile;
            return handle;
        }
    };
    
    
}
