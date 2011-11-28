/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.repo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.alfresco.PseudoFileOverlayImpl;
import org.alfresco.filesys.alfresco.RepositoryDiskInterface;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.AccessMode;
import org.alfresco.jlan.server.filesys.DirectoryNotEmptyException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskFullException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSizeInterface;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.jlan.server.filesys.pseudo.PseudoNetworkFile;
import org.alfresco.jlan.server.filesys.quota.QuotaManager;
import org.alfresco.jlan.server.filesys.quota.QuotaManagerException;
import org.alfresco.jlan.server.locking.FileLockingInterface;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.jlan.server.locking.OpLockInterface;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.smb.server.SMBServer;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.jlan.util.MemorySize;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * Alfresco Content repository filesystem driver class
 * <p>
 * Provides a JLAN ContentDiskDriver for various JLAN protocols 
 * such as SMB/CIFS, NFS and FTP.
 *
 */
public class ContentDiskDriver2 extends  AlfrescoDiskDriver implements ExtendedDiskInterface, 
    DiskInterface, 
    DiskSizeInterface, 
    IOCtlInterface, 
    RepositoryDiskInterface, 
    OpLockInterface, 
    FileLockingInterface
{
    // Logging
    private static final Log logger = LogFactory.getLog(ContentDiskDriver2.class);
    
    private static final Log readLogger = LogFactory.getLog("org.alfresco.filesys.repo.ContentDiskDriver2.Read");
    private static final Log writeLogger = LogFactory.getLog("org.alfresco.filesys.repo.ContentDiskDriver2.Write");
        
    // Services and helpers
    private CifsHelper cifsHelper;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private PermissionService permissionService;
    private FileFolderService fileFolderService;
    private LockService lockService;
    private CheckOutCheckInService checkOutCheckInService;
    private AuthenticationContext authContext;
    private AuthenticationService authService;
    private BehaviourFilter policyBehaviourFilter;
    private NodeMonitorFactory m_nodeMonitorFactory;

	private boolean isLockedFilesAsOffline;
	
    /**
     * 
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "checkOutCheckInService", checkOutCheckInService);
        PropertyCheck.mandatory(this, "cifsHelper", cifsHelper);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "mimetypeService", mimetypeService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
        PropertyCheck.mandatory(this, "lockService",lockService);
        PropertyCheck.mandatory(this, "authContext", authContext);
        PropertyCheck.mandatory(this, "authService", authService);
        PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
        PropertyCheck.mandatory(this, "m_nodeMonitorFactory", m_nodeMonitorFactory);
        PropertyCheck.mandatory(this, "ioControlHandler", ioControlHandler);
    }
    
    /**
     * Return the CIFS helper
     * 
     * @return CifsHelper
     */
    public final CifsHelper getCifsHelper()
    {
    	return this.cifsHelper;
    }
    
    /**
     * Return the authentication service
     * 
     * @return AuthenticationService
     */
    public final AuthenticationService getAuthenticationService()
    {
    	return authService;
    }

    /**
     * Return the authentication context
     * 
     * @return AuthenticationContext
     */
    public final AuthenticationContext getAuthenticationContext() {
    	return authContext;
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    public final NodeService getNodeService()
    {
    	return this.nodeService;
    }
    
    /**
     * Return the content service
     * 
     * @return ContentService
     */
    public final ContentService getContentService()
    {
    	return this.contentService;
    }

    /**
     * Return the namespace service
     * 
     * @return NamespaceService
     */
    public final NamespaceService getNamespaceService()
    {
    	return this.namespaceService;
    }
    
    /**
     * Return the search service
     * 
     * @return SearchService
     */
    public final SearchService getSearchService(){
    	return this.searchService;
    }

    /**
     * Return the file folder service
     * 
     * @return FileFolderService
     */
    public final FileFolderService getFileFolderService() {
    	return this.fileFolderService;
    }
    
    /**
     * Return the permission service
     * 
     * @return PermissionService
     */
    public final PermissionService getPermissionService() {
    	return this.permissionService;
    }
    
    /**
     * Return the lock service
     * 
     * @return LockService
     */
    public final LockService getLockService() {
        return lockService;
    }
    
    /**
     * Get the policy behaviour filter, used to inhibit versioning on a per transaction basis
     */
    public BehaviourFilter getPolicyFilter()
    {
        return policyBehaviourFilter;
    }

    
    /**
     * @param contentService the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param namespaceService the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param searchService the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService PermissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set the authentication context
     * 
     * @param authContext AuthenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authContext)
    {
        this.authContext = authContext;
    }

    /**
     * Set the authentication service
     * 
     * @param authService AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
    	this.authService = authService;
    }

    /**
     * Set the file folder service
     * 
     * @param fileService FileFolderService
     */
    public void setFileFolderService(FileFolderService fileService)
    {
    	fileFolderService = fileService;
    }
    
    /**
     * @param mimetypeService       service for helping with mimetypes and encoding
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Set the node monitor factory
     * 
     * @param nodeMonitorFactory NodeMonitorFactory
     */
    public void setNodeMonitorFactory(NodeMonitorFactory nodeMonitorFactory) {
    	m_nodeMonitorFactory = nodeMonitorFactory;
    }
    
    
    /**
     * Set the lock service
     * 
     * @param lockService LockService
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }
    
    /**
     * Set the policy behaviour filter, used to inhibit versioning on a per transaction basis
     * 
     * @param policyFilter PolicyBehaviourFilter
     */
    public void setPolicyFilter(BehaviourFilter policyFilter)
    {
        this.policyBehaviourFilter = policyFilter;
    }
    
   // Configuration key names
    
    private static final String KEY_STORE = "store";
    private static final String KEY_ROOT_PATH = "rootPath";
    private static final String KEY_RELATIVE_PATH = "relativePath";

    /**
     * Parse and validate the parameter string and create a device context object for this instance
     * of the shared device. The same DeviceInterface implementation may be used for multiple
     * shares.
     * <p>
     * @deprecated - no longer used.   Construction of context is via spring now. 
     * @param deviceName The name of the device
     * @param cfg ConfigElement the configuration of the device context.
     * @return DeviceContext
     * @exception DeviceContextException
     */
    public DeviceContext createContext(String deviceName, ConfigElement cfg) throws DeviceContextException
    {
        throw new DeviceContextException("Obsolete Method called");
    }
    
    /*
     * Register context implementation
     * <p>
     * Results in various obscure bits and pieces being initialised,  most importantly the 
     * calculation of the root node ref.
     * <p>
     * There's a load of initialisation that needs to be moved out of this method, like the 
     * instantiation of the lock manager, quota manager and node monitor.
     */
    public void registerContext(DeviceContext ctx) throws DeviceContextException
    {
        logger.debug("registerContext");
        super.registerContext(ctx);
        
        final ContentContext context = (ContentContext)ctx;
        
        final String rootPath = context.getRootPath();
        final String storeValue = context.getStoreName();
        
        /**
         * Work using the repo needs to run as system.
         */
        RunAsWork<Void> runAsSystem = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                StoreRef storeRef = new StoreRef(storeValue);
                
                // Connect to the repo and ensure that the store exists
                        
                if (! nodeService.exists(storeRef))
                {
                    throw new DeviceContextException("Store not created prior to application startup: " + storeRef);
                }
                
                NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
                                        
                // Find the root node for this device
                List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPath, null, namespaceService, false);
                        
                NodeRef rootNodeRef = null;
                        
                if (nodeRefs.size() > 1)
                {
                    throw new DeviceContextException("Multiple possible roots for device: \n" +
                                    "   root path: " + rootPath + "\n" +
                                    "   results: " + nodeRefs);
                }
                else if (nodeRefs.size() == 0)
                {
                    // Nothing found
                    throw new DeviceContextException("No root found for device: \n" +
                                    "   root path: " + rootPath);
                }
                else
                {
                    // We found the root node ref            
                    rootNodeRef = nodeRefs.get(0);
                }
                
                // Check if a relative path has been specified        
                String relPath = context.getRelativePath();
                
                try
                {
                    if ( relPath != null && relPath.length() > 0)
                    {
                        // Find the node and validate that the relative path is to a folder
                        NodeRef relPathNode = cifsHelper.getNodeRef( rootNodeRef, relPath);
                    
                        if ( cifsHelper.isDirectory( relPathNode) == false)
                        {
                            throw new DeviceContextException("Relative path is not a folder, " + relPath);
                        }
                            
                        // Use the relative path node as the root of the filesystem    
                        rootNodeRef = relPathNode;
                     }
                     else 
                     {
                         // Make sure the default root node is a folder    
                         if ( cifsHelper.isDirectory( rootNodeRef) == false)
                         {
                            throw new DeviceContextException("Root node is not a folder type node");
                         }    
                     }           
                }   
                catch (Exception ex)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Error during create context", ex);
                    }
                    throw new DeviceContextException("Unable to find root node.", ex);
                }
                       
                 // Record the root node ref
                if(logger.isDebugEnabled())
                {
                    logger.debug("set root node ref:" + rootNodeRef);
                }
                context.setRootNodeRef(rootNodeRef);
                
                return null;
            }
        };
        
        /**
         * Run the above code as system - in particular resolves root node ref.
         */
        AuthenticationUtil.runAs(runAsSystem, AuthenticationUtil.getSystemUserName());
        

        /*
         * Now we have some odds and ends below that should really be configured elsewhere
         */
        
         // Check if locked files should be marked as offline
         if ( context.getOfflineFiles() )
         {
             // Enable marking locked files as offline
             isLockedFilesAsOffline = true;     
             logger.info("Locked files will be marked as offline");
         }
            
         // Enable file state caching
            
//         context.enableStateCache(serverConfig, true);
//         context.getStateCache().setCaseSensitive( false);
         
         logger.debug("initialise the node monitor");
         // Install the node service monitor   
         if ( !context.getDisableNodeMonitor() && m_nodeMonitorFactory != null) 
         {     
                NodeMonitor nodeMonitor = m_nodeMonitorFactory.createNodeMonitor(context);
                context.setNodeMonitor( nodeMonitor);
         }
         
         logger.debug("initialise the file state lock manager");
            
            
         // Check if oplocks are enabled
            
         if ( context.getDisableOplocks() == true)
         {
             logger.warn("Oplock support disabled for filesystem " + context.getDeviceName());
         }
            
         // Start the quota manager, if enabled   
         if ( context.hasQuotaManager()) 
         {     
                try 
                {
                    // Start the quota manager          
                    context.getQuotaManager().startManager( this, context);
                    logger.info("Quota manager enabled for filesystem");
                }
                catch ( QuotaManagerException ex) 
                {
                    logger.error("Failed to start quota manager", ex);
                }
         }
         
         // TODO mode to spring
         PseudoFileOverlayImpl ps = new PseudoFileOverlayImpl();
         ps.setContext(context);
         ps.setNodeService(nodeService);
         ps.setSysAdminParams(context.getSysAdminParams());
         context.setPseudoFileOverlay(ps);
         ps.init();
    }
    
    /**
     * Determine if the disk device is read-only.
     * 
     * @param sess Server session
     * @param ctx Device context
     * @return boolean
     * @exception java.io.IOException If an error occurs.
     */
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx) throws IOException
    {
       if(logger.isDebugEnabled())
       {
           logger.debug("isReadOnly");
       }
       return !m_transactionService.getAllowWrite();
    }
    
    /**
     * Get the file information for the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param name File name/path that information is required for.
     * @return File information if valid, else null
     * @exception java.io.IOException The exception description.
     */
    public FileInfo getFileInformation(SrvSession session, TreeConnection tree, String path) throws IOException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getFileInformation:" + path + ", session:" + session.getUniqueId());
        }
        ContentContext ctx = (ContentContext) tree.getContext();
        
        if ( path == null || path.length() == 0)
        {
            path = FileName.DOS_SEPERATOR_STR;
        }
        
        String infoPath = path;
                
        try
        {
            FileInfo finfo = null;
            
            // Is the node a pseudo file ?
            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {   
                String[] paths = FileName.splitPath(path);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);
                
                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    PseudoFile pfile =  ctx.getPseudoFileOverlay().getPseudoFile(dirNodeRef, paths[1]);
                    FileInfo pseudoFileInfo = pfile.getFileInfo();
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("returning psuedo file details:" + pseudoFileInfo);
                    }
                    return pseudoFileInfo;
                }
            }
            
            // no - this is not a specially named pseudo file.
            NodeRef nodeRef = getNodeForPath(tree, infoPath);
            
            if ( nodeRef != null)
            {
                // Get the file information for the node
                
                finfo = getCifsHelper().getFileInformation(nodeRef, false, isLockedFilesAsOffline);
                
                /**
                 * Special processing for root node
                 */
                if(path.equals(FileName.DOS_SEPERATOR_STR))
                {
                    finfo.setFileName("");
                }

                // DEBUG
                if ( logger.isDebugEnabled())
                {
                    logger.debug("getFileInformation found nodeRef for nodeRef :"+ nodeRef + ", path: " + path);
                }

                // Moved to CIFS Helper
//            	// Set the file id from the node's DBID
//                long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
//                finfo.setFileId((int) (id & 0xFFFFFFFFL));    
            }
            
            // Return the file information or null if the node ref does not exist
            return finfo;
        }
        catch (FileNotFoundException e)
        {
            // Debug
        	
            if (logger.isDebugEnabled())
            {
                // exception not logged - cifs does lots of these
                logger.debug("Get file info - file not found, " + path);
            }
            throw e;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Get file info - access denied, " + path, ex);
            }
            
            // Convert to a filesystem access denied status    
            throw new AccessDeniedException("Get file information " + path);
        }
        catch (AlfrescoRuntimeException ex)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Get file info error" + path, ex);
            }
            
            // Convert to a general I/O exception
            throw new IOException("Get file information " + path, ex);
        }
    }

    /**
     * Start a new search on the filesystem using the specified searchPath that may contain
     * wildcards.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param searchPath File(s) to search for, may include wildcards.
     * @param attrib Attributes of the file(s) to search for, see class SMBFileAttribute.
     * @return SearchContext
     * @exception java.io.FileNotFoundException If the search could not be started.
     */
    public SearchContext startSearch(SrvSession session, TreeConnection tree, String searchPath, int attributes) throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("startSearch: "+ searchPath + ", session:" + session.getUniqueId());
        }
        // Access the device context
        
        ContentContext ctx = (ContentContext) tree.getContext();

        try
        {
            String searchFileSpec = searchPath;
                
            NodeRef searchRootNodeRef = ctx.getRootNode();
                        
            String[] paths = FileName.splitPath(searchPath);
            String dotPath = paths[0];
            
            // lookup parent directory
            NodeRef dirNodeRef = getNodeForPath(tree, dotPath);
            if(dirNodeRef != null)
            {
                searchRootNodeRef = dirNodeRef;
                searchFileSpec    = paths[1];
            }
            
            // Convert the all files wildcard
            if ( searchFileSpec.equals( "*.*"))
            {
            	searchFileSpec = "*";
            }
            
            // Debug
            long startTime = 0L;
            if ( logger.isDebugEnabled())
            {
            	startTime = System.currentTimeMillis();
            }
            
            // Perform the search
            
            logger.debug("Call repo to do search");
            
            List<NodeRef> results = getCifsHelper().getNodeRefs(searchRootNodeRef, searchFileSpec);
            // Debug
            if ( logger.isDebugEnabled()) 
            {
            	long endTime = System.currentTimeMillis();
            	if (( endTime - startTime) > 500)
            	{
            		logger.debug("Search for searchPath=" + searchPath + ", searchSpec=" + searchFileSpec + ", searchRootNode=" + searchRootNodeRef + " took "
            				     + ( endTime - startTime) + "ms results=" + results.size());
            	}
            }
            
            /**
             * Search pseudo files if they are enabled
             */
            PseudoFileList pseudoList = null;
            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {   
                logger.debug("search pseudo files");
                pseudoList = ctx.getPseudoFileOverlay().searchPseudoFiles(dirNodeRef, searchFileSpec);
            }
            
            DotDotContentSearchContext searchCtx = new DotDotContentSearchContext(getCifsHelper(), results, searchFileSpec, pseudoList, paths[0]);          

            FileInfo dotInfo = getCifsHelper().getFileInformation(searchRootNodeRef, false, isLockedFilesAsOffline);
            
            if ( searchPath.equals( FileName.DOS_SEPERATOR_STR)) {
                // Searching the root folder, re-use the search folder file information for the '..' pseudo entry    
                FileInfo dotDotInfo = new FileInfo();
                dotDotInfo.copyFrom(dotInfo);
                searchCtx.setDotInfo(dotInfo);
                searchCtx.setDotDotInfo( dotDotInfo);
            }
            else
            {
                String[] parent = FileName.splitPath(dotPath);
                NodeRef parentNodeRef = getNodeForPath(tree, parent[0]);
                if(parentNodeRef != null)
                {
                    FileInfo dotDotInfo = getCifsHelper().getFileInformation(parentNodeRef, false, isLockedFilesAsOffline);
                    searchCtx.setDotDotInfo(dotDotInfo);
                }
                
                // Searching a normal, non root, folder
                // Need to set dot and dotdot
                searchCtx.setDotInfo(dotInfo);
               
            }
            
            // Debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Started search: search path=" + searchPath + " attributes=" + attributes + ", ctx=" + searchCtx);
            }
            
            // TODO -- 
            // Need to resolve the file info here so it's within the transaction boundary.
                       
            return searchCtx;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Start search - access denied, " + searchPath);
            }
            
            // Convert to a file not found status
            
            throw new FileNotFoundException("Start search " + searchPath);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // This is an error even though we "handle" it here.
            
            if ( logger.isErrorEnabled())
            {
                logger.error("Exception in Start search", ex);
            }
            
            // Convert to a file not found status
            
            throw new FileNotFoundException("Start search " + searchPath);
        }
    }

    /**
     * Check if the specified file exists, and whether it is a file or directory.
     * 
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param name the path of the file 
     * @return FileStatus (0: NotExist, 1 : FileExist, 2: DirectoryExists) 
     * @see FileStatus
     */
    public int fileExists(SrvSession session, TreeConnection tree, String name)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("fileExists:" + name + ", session:" + session.getUniqueId());
        }
        ContentContext ctx = (ContentContext) tree.getContext();
        int status = FileStatus.Unknown;        
        try
        {
            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(name);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);
                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    return FileStatus.FileExists;
                }
            }
            
	        // Get the file information to check if the file/folder exists           
	        FileInfo info = getFileInformation(session, tree, name);
	        if (info.isDirectory())
	        {
	            if(logger.isDebugEnabled())
	            {
	                logger.debug("is directory");
	            }
	            status = FileStatus.DirectoryExists;
	        }
	        else
	        {
	            if(logger.isDebugEnabled())
	            {
	                logger.debug("is file");
	            }
	            status = FileStatus.FileExists;
	        }
	        
	        if (logger.isDebugEnabled())
	        {
	            logger.debug("File status determined: name=" + name + " status=" + status);
	        }
	      
	        return status;
        }
        catch (FileNotFoundException e)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("file does not exist");
            }
            status = FileStatus.NotExist;
            return status;
        }
        catch (IOException e)
        {
            // Debug

        	if ( logger.isDebugEnabled())
        	{
        		logger.debug("File exists error, " + name, e);
        	}
            
            status = FileStatus.NotExist;
            return status;
        }

    }
    
    /**
     * Open a file or folder
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param params FileOpenParams
     * @return NetworkFile
     * @exception IOException
     */
    public NetworkFile openFile(SrvSession session, TreeConnection tree, FileOpenParams params) throws IOException
    {
        ContentContext ctx = (ContentContext) tree.getContext();

        if(logger.isDebugEnabled())
        {
            logger.debug("openFile :" + params + ", session:" + session.getUniqueId() + ", params:" + params);
        }
        try
        {  
            String name = params.getPath();

            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(name);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);

                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    PseudoFile pfile =  ctx.getPseudoFileOverlay().getPseudoFile(dirNodeRef, paths[1]);
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Opened pseudo file :" + pfile);
                    }
                    return pfile.getFile( params.getPath());
                }
            }

            // not a psudo file

            NodeRef nodeRef = getNodeForPath(tree, params.getPath());

            // Check permissions on the file/folder node
            //
            // Check for read access

            if ( params.hasAccessMode(AccessMode.NTRead) &&
                    permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("about to throw an no read access denied exception path:" +params.getFullPath());
                }
                throw new AccessDeniedException("No read access to " + params.getFullPath());
            }

            // Check for write access
            if ( params.hasAccessMode(AccessMode.NTWrite) &&
                    permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("about to throw an no write access denied exception path:" +params.getFullPath());
                }
 
                throw new AccessDeniedException("No write access to " + params.getFullPath());
            }

            // Check for delete access

            //            if ( params.hasAccessMode(AccessMode.NTDelete) &&
            //                    permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.DENIED)
            //                throw new AccessDeniedException("No delete access to " + params.getFullPath());

            // will throw a NodeLockedException is locked by somebody else
            if ( params.hasAccessMode(AccessMode.NTWrite))
            {
                lockService.checkForLock(nodeRef);
            }
            
            // Check if the node is a link node            
            NodeRef linkRef = (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
            NetworkFile netFile = null;

            if ( linkRef == null)
            {
                // A normal node, not a link node
                
                // TODO MER REWRITE HERE
                FileInfo fileInfo = cifsHelper.getFileInformation(nodeRef, "", false, false);

                // TODO this is wasteful - the isDirectory is in the params.   We should split off an openDirectory method.
                if(fileInfo.isDirectory())
                {
                    logger.debug("open file - is a directory!");
                    netFile = new AlfrescoFolder(params.getFullPath(), fileInfo, params.isReadOnlyAccess()); 
                }
                else
                {
                    // A normal file
                    if(params.isReadOnlyAccess())
                    {
                        logger.debug("open file for read only");
                        netFile = ContentNetworkFile.createFile(nodeService, contentService, mimetypeService, getCifsHelper(), nodeRef, params, session);

                    }
                    else if(params.isReadWriteAccess())
                    {
                        logger.debug("open file for read write");
                        File file = TempFileProvider.createTempFile("cifs", ".bin");

                        if(! params.isOverwrite())
                        {
                            // Need to open a temp file with a copy of the content.
                            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                            if(reader != null)
                            {
                                reader.getContent(file);
                            }
                        }

                        netFile = new TempNetworkFile(file, name);

                        // Generate a file id for the file

                        if ( netFile != null) 
                        {
                            long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
                            netFile.setFileId((int) (id & 0xFFFFFFFFL));
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Created file: path=" + name + " node=" + nodeRef + " network file=" + netFile);
                        }
                    }
                    else
                    {
                        // Write only or AttributesOnly
                        logger.debug("open file write or attributes only");
                        File file = TempFileProvider.createTempFile("cifs", ".bin");

                        netFile = new TempNetworkFile(file, name);

                        // Generate a file id for the file

                        if ( netFile != null) 
                        {
                            long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
                            netFile.setFileId((int) (id & 0xFFFFFFFFL));
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Created temporary file: path=" + name + " node=" + nodeRef + " network file=" + netFile);
                        }
                    }
                    
                    if ( params.isReadOnlyAccess())
                    {
                        netFile.setGrantedAccess( NetworkFile.READONLY);
                    }
                    else if ( params.isWriteOnlyAccess())
                    {
                        // Needs to be READWRITE for JavaNetworkFile - there's no such thing as WRITEONLY!
                        netFile.setGrantedAccess( NetworkFile.READWRITE);
                    }
                    else
                    {
                        netFile.setGrantedAccess( NetworkFile.READWRITE);
                    }
                    
                } // end of a normal file
            }
            else
            {
                // This is a link node

                // Get the CIFS server name

                String srvName = null;
                SMBServer cifsServer = (SMBServer) session.getServer().getConfiguration().findServer( "CIFS");

                if ( cifsServer != null)
                {
                    // Use the CIFS server name in the URL

                    srvName = cifsServer.getServerName();
                }
                else
                {
                    // Use the local server name in the URL
                    srvName = InetAddress.getLocalHost().getHostName();
                }

                // Convert the target node to a path, convert to URL format

                String path = getPathForNode( tree, linkRef);
                path = path.replace( FileName.DOS_SEPERATOR, '/');

                // Build the URL file data

                StringBuilder urlStr = new StringBuilder();

                urlStr.append("[InternetShortcut]\r\n");
                urlStr.append("URL=file://");
                urlStr.append( srvName);
                urlStr.append("/");
                urlStr.append( tree.getSharedDevice().getName());
                urlStr.append( path);
                urlStr.append("\r\n");

                // Create the in memory pseudo file for the URL link

                byte[] urlData = urlStr.toString().getBytes();

                // Get the file information for the link node

                FileInfo fInfo = getCifsHelper().getFileInformation( nodeRef, false, isLockedFilesAsOffline);

                // Set the file size to the actual data length

                fInfo.setFileSize( urlData.length);

                // Create the network file using the in-memory file data

                netFile = new LinkMemoryNetworkFile( fInfo.getFileName(), urlData, fInfo, nodeRef);
                netFile.setFullName( params.getPath());
            }

            // Generate a file id for the file

            if ( netFile != null) 
            {
                long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
                netFile.setFileId(( int) ( id & 0xFFFFFFFFL));
            }

            // If the file has been opened for overwrite then truncate the file to zero length, this will
            // also prevent the existing content data from being copied to the new version of the file

            if ( params.isOverwrite() && netFile != null)
            {
                // Truncate the file to zero length
                if(logger.isDebugEnabled())
                {
                    logger.debug("truncate file");
                }

                netFile.truncateFile( 0L);
            }

            // Debug

            if (logger.isDebugEnabled())
            {
                logger.debug("Opened network file: path=" + params.getPath() + " file open parameters=" + params + " network file=" + netFile);
            }

            // Return the network file

            return netFile;
        }
        catch (NodeLockedException nle)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Open file - node is locked, " + params.getFullPath());
            }
            throw new AccessDeniedException("File is locked, no write access to " + params.getFullPath());
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug

            if ( logger.isDebugEnabled())
            {
                logger.debug("Open file - access denied, " + params.getFullPath());
            } 
            // Convert to a filesystem access denied status

            throw new AccessDeniedException("Open file " + params.getFullPath());
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug

            if (logger.isDebugEnabled())
            {
                logger.debug("Open file error", ex);
            }
            // Convert to a general I/O exception

            throw new IOException("Open file " + params.getFullPath(), ex);
        }
    }
    
    /**
     * Create a new file on the file system.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param params File create parameters
     * @return NetworkFile
     * @exception java.io.IOException If an error occurs.
     */
    public NetworkFile createFile(SrvSession sess, final TreeConnection tree, final FileOpenParams params) throws IOException
    {
        // Obsolete
        return null;
    }

    /**
     * Create a new directory on this file system.
     *
     *
     * @param sess Server session
     * @param tree Tree connection.
     * @param params Directory create parameters
     * @exception java.io.IOException If an error occurs.
     */
    public void createDirectory(SrvSession sess, final TreeConnection tree, final FileOpenParams params) throws IOException
    {
        final ContentContext ctx = (ContentContext) tree.getContext();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("createDirectory :" + params);
        }
        
        try
        {
            NodeRef dirNodeRef;
            String folderName;
            
            String path = params.getPath();

            String[] paths = FileName.splitPath(path);
            
            if (paths[0] != null && paths[0].length() > 1)
            {  
                // lookup parent directory
                dirNodeRef = getNodeForPath(tree, paths[0]);
                folderName = paths[1];
            }
            else
            {
                dirNodeRef =  ctx.getRootNode();
                folderName = path;  
            }
            
            if(dirNodeRef == null)
            {
                throw new IOException("Create directory parent folder not found" + params.getFullPath());
            }
            
            NodeRef nodeRef = getCifsHelper().createNode(dirNodeRef, folderName, ContentModel.TYPE_FOLDER);

                
            if (logger.isDebugEnabled())
            {
                logger.debug("Created directory: path=" + params.getPath() + " file open params=" + params + " node=" + nodeRef);
            }
            
            // void return
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create directory - access denied, " + params.getFullPath());
            }
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Create directory " + params.getFullPath());
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create directory error", ex);
            }
            
            // Convert to a general I/O exception
            
            throw new IOException("Create directory " + params.getFullPath(), ex);
        }
    }

    /**
     * Delete the directory from the filesystem.
     * <p>
     * The directory must be empty in order to be able to delete ity
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param dir Directory name.
     * @exception java.io.IOException The exception description.
     */
    public void deleteDirectory(SrvSession session, TreeConnection tree, final String dir) throws IOException
    {
        // get the device root
        
        if (logger.isDebugEnabled())
        {
            logger.debug("deleteDirectory: " + dir + ", session:" + session.getUniqueId());
        }
        
        ContentContext ctx = (ContentContext) tree.getContext();
        final NodeRef deviceRootNodeRef = ctx.getRootNode();
        
        try
        {
            // Get the node for the folder                    
        	
            NodeRef nodeRef = getCifsHelper().getNodeRef(deviceRootNodeRef, dir);
            if (fileFolderService.exists(nodeRef))
            {
                // Check if the folder is empty                        
            	
                if ( getCifsHelper().isFolderEmpty( nodeRef))
                {                            
                    // Delete the folder node           
                    fileFolderService.delete(nodeRef);
                }
                else
                {
                    throw new DirectoryNotEmptyException( dir);                            
                }      
            }
            
            // Debug
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted directory: directory=" + dir + " node=" + nodeRef);
            }
            // void return
        }
        catch (FileNotFoundException e)
        {
            // Debug
        	
            if (logger.isDebugEnabled())
            {
                logger.debug("Delete directory - file not found, " + dir);
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Delete directory - access denied, " + dir);
            }
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Delete directory, access denied :" + dir);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled() && ctx.hasDebug(AlfrescoContext.DBG_FILE))
                logger.debug("Delete directory", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Delete directory " + dir, ex);
        }
    }

    /**
     * Flush any buffered output for the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file Network file context.
     * @exception java.io.IOException The exception description.
     */
    public void flushFile(SrvSession session, TreeConnection tree, NetworkFile file) throws IOException
    {
    	// Debug
    	
    	ContentContext ctx = (ContentContext) tree.getContext();
    	
    	if ( logger.isDebugEnabled())
    	{
    		logger.debug("Flush file=" + file.getFullName()+ ", session:" + session.getUniqueId());
    	}
    	
        // Flush the file data
        file.flushFile();
    }

    /**
     * Close the file.
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param param Network file context.
     * 
     * @exception java.io.IOException If an error occurs.
     */
    public void closeFile(SrvSession session, TreeConnection tree, final NetworkFile file) throws IOException
    {   
//        if ( logger.isDebugEnabled())
//        {
//            logger.debug("Close file=" + file.getFullName() + ", session:" + session.getUniqueId());
//        }
//        // Get the associated file state
//        
//       final ContentContext ctx = (ContentContext) tree.getContext();
//        
//        if( file instanceof PseudoNetworkFile)
//        {
//            file.close();
//            return;
//        }
//        
//        /**
//         * Delete on close attribute - node needs to be deleted.
//         */
//        if(file.hasDeleteOnClose())
//        {
//            if(logger.isDebugEnabled())
//            {
//                logger.debug("closeFile has delete on close set");
//            }
// 
//            NodeRef target = getCifsHelper().getNodeRef(ctx.getRootNode(), file.getFullName());
//            if(target!=null)
//            {
//                nodeService.deleteNode(target);
//            }
//            
//            //TODO Needs to be post-commit
//            if(file instanceof TempNetworkFile)
//            {
//                TempNetworkFile tnf = (TempNetworkFile)file;
//                final QuotaManager quotaMgr = ctx.getQuotaManager();
//                if (quotaMgr != null)
//                {
//                    quotaMgr.releaseSpace(session, tree, file.getFileId(), file.getName(), tnf.getFileSizeInt());
//                }
//            }
//            
//            // Still need to close the open file handle.
//            file.close();
//            
//            if (logger.isDebugEnabled())
//            {
//                logger.debug("Closed file: network file=" + file + " delete on close=" + file.hasDeleteOnClose());
//            }
//            
//  
//            return;
//        }
//        
//        // Check for a temp file - which will be a new file or a read/write file
//        if ( file instanceof TempNetworkFile) 
//        {   
//            if(logger.isDebugEnabled())
//            {
//                logger.debug("Got a temp network file ");
//            }
//            
//            TempNetworkFile tempFile =(TempNetworkFile)file;
//            
//            tempFile.flushFile();
//            tempFile.close();
//            
//            // Take an initial guess at the mimetype (if it has not been set by something already)
//            String mimetype = mimetypeService.guessMimetype(tempFile.getFullName(), new FileContentReader(tempFile.getFile()));
//            logger.debug("guesssed mimetype:" + mimetype);
//            
//            String encoding;
//            // Take a guess at the locale
//            InputStream is = new BufferedInputStream(new FileInputStream(tempFile.getFile()));
//            try
//            {
//                ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
//                Charset charset = charsetFinder.getCharset(is, mimetype);
//                encoding = charset.name();
//            }
//            finally
//            {
//                if(is != null)
//                {
//                    is.close();
//                }
//            }
//     
//            NodeRef target = getCifsHelper().getNodeRef(ctx.getRootNode(), tempFile.getFullName());
//            ContentWriter writer = contentService.getWriter(target, ContentModel.PROP_CONTENT, true);
//            writer.setMimetype(mimetype);
//            writer.setEncoding(encoding);
//            writer.putContent(tempFile.getFile());
//            
//            long size = writer.getSize();
//            if(nodeService.hasAspect(target, ContentModel.ASPECT_NO_CONTENT) && size > 0)
//            {
//                if(logger.isDebugEnabled())
//                {
//                    logger.debug("removed no content aspect");
//                }
//                nodeService.removeAspect(target, ContentModel.ASPECT_NO_CONTENT);
//            }
//        }
//        
//        try
//        {                                           
//            // Defer to the network file to close the stream and remove the content
//                       
//            file.close();                    
//            
//            // DEBUG
//            
//            if (logger.isDebugEnabled())
//            {
//                logger.debug("Closed file: network file=" + file + " delete on close=" + file.hasDeleteOnClose());
//                
//                if ( file.hasDeleteOnClose() == false && file instanceof ContentNetworkFile) 
//                {
//                    ContentNetworkFile cFile = (ContentNetworkFile) file;
//                    logger.debug("  File " + file.getFullName() + ", version=" + nodeService.getProperty( cFile.getNodeRef(), ContentModel.PROP_VERSION_LABEL));
//                }
//            }
//        }
//        // Make sure we clean up before propagating exceptions
//        catch (IOException e)
//        {
//            if ( logger.isDebugEnabled())
//            {   
//                logger.debug("Exception in closeFile - ", e);
//            }
//            throw e;
//        }
//        catch (Error e)
//        {
//            if ( logger.isDebugEnabled())
//            {   
//                logger.debug("Exception in closeFile - ", e);
//            }
//            
//            throw e;
//        }
    }

    /**
     * Delete the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file NetworkFile
     * @exception java.io.IOException The exception description.
     */
    public void deleteFile(final SrvSession session, final TreeConnection tree, final String name) throws IOException
    {
        // Get the device context
        
        final ContentContext ctx = (ContentContext) tree.getContext();
        
        if(logger.isDebugEnabled())
        {
            logger.debug("deleteFile:" + name + ", session:" + session.getUniqueId());
        }
        
        try
        {
            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(name);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);
              
                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    return;
                }
            }
            
            // Check if there is a quota manager enabled, if so then we need to save the current file size
            
            final QuotaManager quotaMgr = ctx.getQuotaManager();

            // Get the node and delete it
            final NodeRef nodeRef = getNodeForPath(tree, name);
                    
            if (fileFolderService.exists(nodeRef))
            {
                // Get the size of the file being deleted        
                final FileInfo fInfo = quotaMgr == null ? null : getFileInformation(session, tree, name);

                if(logger.isDebugEnabled())
                {
                    logger.debug("deleted file" + name);
                }
                fileFolderService.delete(nodeRef);
                
                //TODO Needs to be post-commit
                if (quotaMgr != null)
                {
                    quotaMgr.releaseSpace(session, tree, fInfo.getFileId(), name, fInfo.getSize());
                }
         
                // Debug
                    
                if (logger.isDebugEnabled())
                {
                   logger.debug("Deleted file: " + name + ", nodeRef=" + nodeRef);
                }

                // void return
                return;
            }
        }
        catch (NodeLockedException ex)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Delete file - access denied (locked)", ex);
            }
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Unable to delete " + name);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {   
                logger.debug("Delete file - access denied", ex);
            }
            
            // Convert to a filesystem access denied status
            throw new AccessDeniedException("Unable to delete " + name);
        }
        catch (IOException ex)
        {
            // Allow I/O Exceptions to pass through
            throw ex;
        }
        catch (Exception ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Delete file error", ex);
            }
            
            // Convert to a general I/O exception
            IOException ioe = new IOException("Delete file " + name);
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Rename the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param oldName java.lang.String
     * @param newName java.lang.String
     * @exception java.io.IOException The exception description.
     */
    public void renameFile(final SrvSession session, final TreeConnection tree, final String oldName, final String newName)
            throws IOException
    {
        // Get the device context
        final ContentContext ctx = (ContentContext) tree.getContext();

        // DEBUG

        if (logger.isDebugEnabled())
        {
            logger.debug("RenameFile oldName=" + oldName + ", newName=" + newName + ", session:" + session.getUniqueId());
        }
        
        try
        {
            // Get the file/folder to move

            final NodeRef nodeToMoveRef = getNodeForPath(tree, oldName);

            // Check if the node is a link node

            if ( nodeToMoveRef != null && nodeService.getProperty(nodeToMoveRef, ContentModel.PROP_LINK_DESTINATION) != null)
            {
                throw new AccessDeniedException("Cannot rename link nodes");
            }

            // Get the new target folder - it must be a folder
            String[] splitPaths = FileName.splitPath(newName);
            String[] oldPaths = FileName.splitPath(oldName);

            final NodeRef targetFolderRef = getNodeForPath(tree, splitPaths[0]);
            final NodeRef sourceFolderRef = getNodeForPath(tree, oldPaths[0]);
            final String name = splitPaths[1];

            // Check if this is a rename within the same folder
            
            final boolean sameFolder = splitPaths[0].equalsIgnoreCase(oldPaths[0]);

            // Check if we are renaming a folder, or the rename is to a different folder
            boolean isFolder = getCifsHelper().isDirectory(nodeToMoveRef);

            if ( isFolder == true || sameFolder == false) {
                
                // Rename or move the file/folder to another folder
                if (sameFolder == true)
                {
                    getCifsHelper().rename(nodeToMoveRef, name);
                }
                else
                {
                    getCifsHelper().move(nodeToMoveRef, sourceFolderRef, targetFolderRef, name);
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("  Renamed " + (isFolder ? "folder" : "file") + " using "
                            + (sameFolder ? "rename" : "move"));
                }
            }
            else 
            {
                // Rename a file within the same folder
                //
                // Check if the target file already exists

                final int newExists = fileExists(session, tree, newName);
                if (logger.isDebugEnabled())
                {
                            logger.debug(
                                    "Rename file within same folder: \n" +
                                    "   Old name:      " + oldName + "\n" +
                                    "   New name:      " + newName + "\n" +
                                    "   Source folder: " + sourceFolderRef + "\n" +
                                    "   Target folder: " + targetFolderRef + "\n" +
                                    "   Node:          " + nodeToMoveRef + "\n" +
                                    "   Aspects:       " + nodeService.getAspects(nodeToMoveRef));                             
                }
                
                getCifsHelper().rename(nodeToMoveRef, name);
                       
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file - access denied, " + oldName);
            }
            // Convert to a filesystem access denied status
            throw new AccessDeniedException("Rename file " + oldName);
        }
        catch (NodeLockedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file", ex);
            }

            // Convert to an filesystem access denied exception
            throw new AccessDeniedException("Node locked " + oldName);
        }
        catch (AlfrescoRuntimeException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file", ex);
            }

            // Convert to a general I/O exception
            throw new AccessDeniedException("Rename file " + oldName);
        }
    }

    /**
     * Set file information
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param name String
     * @param info FileInfo
     * @exception IOException
     */
    public void setFileInformation(SrvSession sess, final TreeConnection tree, final String name, final FileInfo info) throws IOException
    {
        // Get the device context
        
        final ContentContext ctx = (ContentContext) tree.getContext();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("setFileInformation name=" + name + ", info=" + info);
        }
        
        NetworkFile networkFile = info.getNetworkFile();
        
        try
        {
            
            if(sess.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(name);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);
                
                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("pseudo file so do nothing");
                    }
                    return;
                }
            }
            
            // Get the file/folder node        
            NodeRef nodeRef = getNodeForPath(tree, name);
                    
            // Check permissions on the file/folder node
                    
            if ( permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("write access denied to :" + name);
                }
                throw new AccessDeniedException("No write access to " + name);
            }
                                        
            // Inhibit versioning for this transaction        
            getPolicyFilter().disableBehaviour( ContentModel.ASPECT_VERSIONABLE);
                    
            // Check if the file is being marked for deletion, if so then check if the file is locked       
            if ( info.hasSetFlag(FileInfo.SetDeleteOnClose) && info.hasDeleteOnClose())
            {
               if(logger.isDebugEnabled())
                {
                    logger.debug("Set Delete On Close for :" + name);
                } 
                // Check for delete permission
                if ( permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.DENIED)
                {
                    throw new AccessDeniedException("No delete access to :" + name);
                }

                // Check if the node is locked
                lockService.checkForLock(nodeRef);
                                     
                // Get the node for the folder
                        
                if (fileFolderService.exists(nodeRef))
                {
                    // Check if it is a folder that is being deleted, make sure it is empty
                            
                    boolean isFolder = true;
                            
                    ContentFileInfo cInfo = getCifsHelper().getFileInformation(nodeRef, false, isLockedFilesAsOffline);
                    
                    if ( cInfo != null && cInfo.isDirectory() == false)
                    {
                        isFolder = false;
                    }

                    // Check if the folder is empty        
                    if ( isFolder == true && getCifsHelper().isFolderEmpty( nodeRef) == false)
                    {
                        throw new DirectoryNotEmptyException( name);
                    }                                                   
                }
            }
            
            if(info.isHidden())
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set hidden attribute" + name);
                }
                // Not yet implemented
            }
            
            if( info.hasSetFlag(FileInfo.SetAllocationSize))
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set allocation size" + name + info.getAllocationSize());
                }
                // Not yet implemented
            }
                    
            // Set the creation and modified date/time
            Map<QName, Serializable> auditableProps = new HashMap<QName, Serializable>(5);
            
            // Which DeleteOnClose flag has priority?
            // SetDeleteOnClose is not set or used in this method.   
            // The NTProtocolHandler sets the deleteOnClose in both
            // info and the NetworkFile - it's the one in NetworkFile that works.
            
            if ( info.hasSetFlag(FileInfo.SetCreationDate) && info.hasCreationDateTime())
            {
                // Set the creation date on the file/folder node
                Date createDate = new Date(info.getCreationDateTime());
                auditableProps.put(ContentModel.PROP_CREATED, createDate);
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set creation date" + name + ", " + createDate);
                }
            }
            if ( info.hasSetFlag(FileInfo.SetModifyDate) && info.hasModifyDateTime()) 
            {                
                 // Set the modification date on the file/folder node
                Date modifyDate = new Date( info.getModifyDateTime());
                auditableProps.put(ContentModel.PROP_MODIFIED, modifyDate);
                
                // Set the network file so we don't reverse this change in close file.
                if(networkFile != null && !networkFile.isReadOnly())
                {
                    networkFile.setModifyDate(info.getModifyDateTime());
                }
                
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set modification date" + name + ", " + modifyDate);
                }
                
            }

            // Did we have any cm:auditable properties?
            if (auditableProps.size() > 0)
            {
                getPolicyFilter().disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                nodeService.addProperties(nodeRef, auditableProps);
                        
                // DEBUG
                if ( logger.isDebugEnabled())
                {
                   logger.debug("Set auditable props: " + auditableProps + " file=" + name);
                }
             }
                   
            return;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Set file information - access denied, " + name);
            }
            // Convert to a filesystem access denied status
            throw new AccessDeniedException("Set file information " + name);
        }
        catch (AlfrescoRuntimeException ex)
        {   
            if ( logger.isDebugEnabled())
            {
                logger.debug("Open file error", ex);
            }
            // Convert to a general I/O exception
            
            throw new IOException("Set file information " + name, ex);
        }
    }

    /**
     * Truncate a file to the specified size
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file Network file details
     * @param size New file length
     * @exception java.io.IOException The exception description.
     */
    public void truncateFile(SrvSession sess, TreeConnection tree, NetworkFile file, long size) throws IOException
    {
        //  Keep track of the allocation/release size in case the file resize fails
        
        ContentContext ctx = (ContentContext) tree.getContext();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("truncateFile file:" + file + ", size: "+ size);
        }
        
        long allocSize   = 0L;
        long releaseSize = 0L;
        
        //  Check if there is a quota manager
        QuotaManager quotaMgr = ctx.getQuotaManager();
              
        if ( ctx.hasQuotaManager()) {
          
            // Check if the file content has been opened, we need the content to be opened to get the
            // current file size
            
            if ( file instanceof ContentNetworkFile) {
                ContentNetworkFile contentFile = (ContentNetworkFile) file;
                if ( contentFile.hasContent() == false)
                {
                    contentFile.openContent( false, false);
                }
            }
            else if( file instanceof TempNetworkFile)
            {
                
            }
            else
            {
                throw new IOException("Invalid file class type, " + file.getClass().getName());
            }
            //  Determine if the new file size will release space or require space allocating
          
            if ( size > file.getFileSize()) 
            {
            
                //  Calculate the space to be allocated
            
                allocSize = size - file.getFileSize();
            
                //  Allocate space to extend the file
            
                quotaMgr.allocateSpace(sess, tree, file, allocSize);
            }
            else 
            {
            
                //  Calculate the space to be released as the file is to be truncated, release the space if
                //  the file truncation is successful
                releaseSize = file.getFileSize() - size;
            }
        }
        
        //  Check if this is a file extend, update the cached allocation size if necessary
        
        if ( file instanceof ContentNetworkFile) {
            
            // Get the cached state for the file
            ContentNetworkFile contentFile = (ContentNetworkFile) file;
            FileState fstate = contentFile.getFileState();
            if ( fstate != null && size > fstate.getAllocationSize())
            {
                fstate.setAllocationSize(size);
            }
        }
        
        if( file instanceof TempNetworkFile)
        {
            TempNetworkFile contentFile = (TempNetworkFile) file;
            FileState fstate = contentFile.getFileState();
            if ( fstate != null && size > fstate.getAllocationSize())
            {
                fstate.setAllocationSize(size);
            }            
        }
        
        //  Set the file length

        try 
        {
            file.truncateFile(size);
        }
        catch (IOException ex) 
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("unable to truncate the file + :" + file.getFullName(), ex);
            }
            //  Check if we allocated space to the file
          
            if ( allocSize > 0 && quotaMgr != null)
            {
                quotaMgr.releaseSpace(sess, tree, file.getFileId(), null, allocSize);
            }

            //  Rethrow the exception 
            throw ex;       
        }
        
        //  Check if space has been released by the file resizing
        
        if ( releaseSize > 0 && quotaMgr != null)
        {
            quotaMgr.releaseSpace(sess, tree, file.getFileId(), null, releaseSize);
        }
        // Debug

        if (logger.isDebugEnabled())
        {
            logger.debug("Truncated file: network file=" + file + " size=" + size);
    
        }
    }

    /**
     * Read a block of data from the specified file.
     * 
     * @param sess Session details
     * @param tree Tree connection
     * @param file Network file
     * @param buf Buffer to return data to
     * @param bufPos Starting position in the return buffer
     * @param siz Maximum size of data to return
     * @param filePos File offset to read data
     * @return Number of bytes read
     * @exception java.io.IOException The exception description.
     */
    public int readFile(
            SrvSession sess, TreeConnection tree, NetworkFile file,
            byte[] buffer, int bufferPosition, int size, long fileOffset) throws IOException
    {
        // Check if the file is a directory
         
        if(readLogger.isDebugEnabled())
        {
            readLogger.debug("read File:" + file + ", size" + size);
        }
        
        if(file.isDirectory())
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("read file called for a directory - throw AccessDeniedException");
            }
            throw new AccessDeniedException("read called for a directory");
        }
        
        // Read a block of data from the file
        
        int count = file.readFile(buffer, size, bufferPosition, fileOffset);
        
        if ( count == -1)
        {
            // Read count of -1 indicates a read past the end of file
            
            count = 0;
        }
        
        // Debug

        //ContentContext ctx = (ContentContext) tree.getContext();
        
        if (readLogger.isDebugEnabled())
        {
            readLogger.debug("Read bytes from file: network file=" + file + " buffer size=" + buffer.length + " buffer pos=" + bufferPosition +
                    " size=" + size + " file offset=" + fileOffset + " bytes read=" + count);
        }
        
       return count;
    }

    /**
     * Seek to the specified file position.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file Network file.
     * @param pos Position to seek to.
     * @param typ Seek type.
     * @return New file position, relative to the start of file.
     */
    public long seekFile(SrvSession sess, TreeConnection tree, NetworkFile file, long pos, int typ) throws IOException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("seek File");
        }
        
  	  	// Check if the file is a directory
    	
		if ( file.isDirectory())
		{
			throw new AccessDeniedException();
		}
    	
		// Set the file position

		return file.seekFile(pos, typ);
    }

    /**
     * Write a block of data to the file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file Network file details
     * @param buf byte[] Data to be written
     * @param bufoff Offset within the buffer that the data starts
     * @param siz int Data length
     * @param fileoff Position within the file that the data is to be written.
     * @return Number of bytes actually written
     * @exception java.io.IOException The exception description.
     */
    public int writeFile(SrvSession sess, TreeConnection tree, NetworkFile file,
            byte[] buffer, int bufferOffset, int size, long fileOffset) throws IOException
    {
        if(writeLogger.isDebugEnabled())
        {
            writeLogger.debug("write File:" + file + " size:" + size);
        }
    	
        //  Check if there is a quota manager
        
        ContentContext ctx = (ContentContext) tree.getContext();
        QuotaManager quotaMgr = ctx.getQuotaManager();
        
        long curSize = file.getFileSize();
        
        if ( quotaMgr != null) 
        {
          
            //  Check if the file requires extending
          
            long extendSize = 0L;
            long endOfWrite = fileOffset + size;
          
            if ( endOfWrite > curSize) 
            {
                //  Calculate the amount the file must be extended

                extendSize = endOfWrite - file.getFileSize();
            
                //  Allocate space for the file extend
                if(logger.isDebugEnabled())
                {
                    logger.debug("writeFile: allocate more space fileName:" + file.getName());
                }
               
            
                long alloc = quotaMgr.allocateSpace(sess, tree, file, extendSize);
                
                if(file instanceof TempNetworkFile)
                {
                    TempNetworkFile tnf = (TempNetworkFile)file;
                    FileState fstate = tnf.getFileState();
                    if(fstate != null)
                    {
                        fstate.setAllocationSize(alloc);
                    }     
                }
             }
        }
    	
    	// Write to the file
        
        file.writeFile(buffer, size, bufferOffset, fileOffset);

        // Check if the file size was reduced by the write, may have been extended previously
        
        if ( quotaMgr != null) 
        {
            
            // Check if the file size reduced
            
            if ( file.getFileSize() < curSize) 
            {
                
                // Release space that was freed by the write
                
                quotaMgr.releaseSpace( sess, tree, file.getFileId(), file.getFullName(), curSize - file.getFileSize());
            }
        }
        
        // Debug

        if (writeLogger.isDebugEnabled())
        {
            writeLogger.debug("Wrote bytes to file: network file=" + file + " buffer size=" + buffer.length + " size=" + size + " file offset=" + fileOffset);
        }
        
        return size;
    }

    /**
     * Get the node for the specified path
     * 
     * @param tree TreeConnection
     * @param path String
     * @return NodeRef
     * @exception FileNotFoundException
     */
    private NodeRef getNodeForPath(TreeConnection tree, String path)
        throws FileNotFoundException
    {   
        ContentContext ctx = (ContentContext) tree.getContext();   
        return getCifsHelper().getNodeRef(ctx.getRootNode(), path);
    }
    
    /**
     * Get the node for the specified path
     * 
     * @param tree TreeConnection
     * @param path String
     * @return NodeRef
     * @exception FileNotFoundException
     */
    public NodeRef getNodeForPath(NodeRef rootNode, String path)
        throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getNodeRefForPath:" + path);
        }
        
        return getCifsHelper().getNodeRef(rootNode, path);
    }
    
    /**
     * Convert a node into a share relative path
     * 
     * @param tree TreeConnection
     * @param nodeRef NodeRef
     * @return String
     * @exception FileNotFoundException
     */
    private String getPathForNode( TreeConnection tree, NodeRef nodeRef)
    	throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getPathForNode:" + nodeRef);
        }
        
    	// Convert the target node to a path
        ContentContext ctx = (ContentContext) tree.getContext();
    	List<org.alfresco.service.cmr.model.FileInfo> linkPaths = null;
    	
    	try 
    	{
    		linkPaths = fileFolderService.getNamePath( ctx.getRootNode(), nodeRef);
    	}
    	catch ( org.alfresco.service.cmr.model.FileNotFoundException ex)
    	{
    		throw new FileNotFoundException();
    	}

    	// Build the share relative path to the node
    	
    	StringBuilder pathStr = new StringBuilder();
    	
    	for ( org.alfresco.service.cmr.model.FileInfo fInfo : linkPaths) 
    	{
    		pathStr.append( FileName.DOS_SEPERATOR);
    		pathStr.append( fInfo.getName());
    	}
    	
    	// Return the share relative path
    	
    	return pathStr.toString();
    }
    
	/**
	 * Return the lock manager used by this filesystem
	 * 
	 * @param sess SrvSession
	 * @param tree TreeConnection
	 * @return LockManager
	 */
	public LockManager getLockManager(SrvSession sess, TreeConnection tree) 
	{
        AlfrescoContext alfCtx = (AlfrescoContext) tree.getContext();
        return alfCtx.getLockManager();  
	}	
	
	/**
	 * Disk Size Interface implementation
	 */
	private interface DiskSizeInterfaceConsts
	{ 
        static final int DiskBlockSize          = 512;  // bytes per block
        static final long DiskAllocationUnit    = 32 * MemorySize.KILOBYTE;
        static final long DiskBlocksPerUnit     = DiskAllocationUnit / DiskBlockSize;
    
        // Disk size returned in the content store does not support free/total size
    
        static final long DiskSizeDefault     = 1 * MemorySize.TERABYTE;
        static final long DiskFreeDefault     = DiskSizeDefault / 2;
	}
	
    /**
     * Get the disk information for this shared disk device.
     *
     * @param ctx		DiskDeviceContext
     * @param diskDev 	SrvDiskInfo
     * @exception IOException
     */
    public void getDiskInformation(DiskDeviceContext ctx, SrvDiskInfo diskDev) throws IOException 
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getDiskInformation");
        }
    	
    	// Set the block size and blocks per allocation unit
    	diskDev.setBlockSize( DiskSizeInterfaceConsts.DiskBlockSize);
    	diskDev.setBlocksPerAllocationUnit( DiskSizeInterfaceConsts.DiskBlocksPerUnit);
    	
    	// Get the free and total disk size in bytes from the content store
    	
    	long freeSpace = contentService.getStoreFreeSpace();
    	long totalSpace= contentService.getStoreTotalSpace();
    	
    	if ( totalSpace == -1L) {
    		
    		// Use a fixed value for the total space, content store does not support size information
    		
    		totalSpace = DiskSizeInterfaceConsts.DiskSizeDefault;
    		freeSpace  = DiskSizeInterfaceConsts.DiskFreeDefault;
    	}

    	// Convert the total/free space values to allocation units
    	
    	diskDev.setTotalUnits( totalSpace / DiskSizeInterfaceConsts.DiskAllocationUnit);
    	diskDev.setFreeUnits( freeSpace / DiskSizeInterfaceConsts.DiskAllocationUnit);
    }

    public void setCifsHelper(CifsHelper cifsHelper)
    {
        this.cifsHelper = cifsHelper;
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
       // Nothing to do
    }

    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        // Nothing to do
    }
    
// Implementation of IOCtlInterface    
    
    /**
     * Process a filesystem I/O control request
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param ctrlCode I/O control code
     * @param fid File id
     * @param dataBuf I/O control specific input data
     * @param isFSCtrl true if this is a filesystem control, or false for a device control
     * @param filter if bit0 is set indicates that the control applies to the share root handle
     * @return DataBuffer
     * @exception IOControlNotImplementedException
     * @exception SMBException
     */
    public org.alfresco.jlan.util.DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter)
        throws IOControlNotImplementedException, SMBException
    {
        // Validate the file id
        if(logger.isDebugEnabled())
        {
            logger.debug("processIOControl ctrlCode: 0x" + Integer.toHexString(ctrlCode) + ", fid:" + fid);
        }
        
        NetworkFile netFile = tree.findFile(fid);
        if ( netFile == null || netFile.isDirectory() == false)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("net file is null or not a directory");
            }
            throw new SMBException(SMBStatus.NTErr, SMBStatus.NTInvalidParameter);
        }
        
        final ContentContext ctx = (ContentContext) tree.getContext();
        try
        {
            org.alfresco.jlan.util.DataBuffer buff = ioControlHandler.processIOControl(sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter, this, ctx);
            
            return buff;
        }
        catch(SMBException smbException)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("SMB Exception fid:" + fid, smbException);
            }
            throw smbException;
        }
    
    }
        
          
    public void setCheckOutCheckInService(CheckOutCheckInService service)
    {
        this.checkOutCheckInService = service;
    } 
    
    /**
     * @return              the service to provide check-in and check-out data
     */
    public final CheckOutCheckInService getCheckOutCheckInService()
    {
        return checkOutCheckInService;
    }

    // Implementation of RepositoryDiskInterface
    @Override
    public void copyContent(NodeRef rootNode, String fromPath, String toPath) throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("copyContent from:" + fromPath + " to:" + toPath);
        }
        
        NodeRef sourceNodeRef = getNodeForPath(rootNode, fromPath);
        NodeRef targetNodeRef = getNodeForPath(rootNode, toPath);
        
        Serializable prop = nodeService.getProperty(sourceNodeRef, ContentModel.PROP_CONTENT);
        if(prop != null)
        { 
            if(prop instanceof ContentData)
            {
                ContentData data = (ContentData)prop;
                if(data.getMimetype().equalsIgnoreCase(MimetypeMap.MIMETYPE_BINARY))
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("mimetype is binary - guess mimetype has failed");
                    }
                    Serializable targetProp = nodeService.getProperty(targetNodeRef, ContentModel.PROP_CONTENT);
                    
                    if(targetProp != null && targetProp instanceof ContentData)
                    {
                        ContentData targetData = (ContentData)targetProp;
                        logger.debug("copy the existing mimetype");
                        prop = ContentData.setMimetype(data, targetData.getMimetype());
                    }
               
                    
                }
            }
            
            nodeService.setProperty(targetNodeRef, ContentModel.PROP_CONTENT, prop);
        }
        else
        {
            // No content to set
            logger.debug("no content to save");
        }
     
    }

    @Override
    public NetworkFile createFile(NodeRef rootNode, String path, long allocationSize)
            throws IOException
    {
          
        if (logger.isDebugEnabled())
        {
            logger.debug("createFile :" + path);
        }
        
        try
        {
            NodeRef dirNodeRef;
            String folderName;
            
            String[] paths = FileName.splitPath(path);
            
            if (paths[0] != null && paths[0].length() > 1)
            {  
                // lookup parent directory
                dirNodeRef = getNodeForPath(rootNode, paths[0]);
                folderName = paths[1];
            }
            else
            {
                dirNodeRef =  rootNode;
                folderName = path;  
            }
                             
            NodeRef nodeRef = cifsHelper.createNode(dirNodeRef, folderName, ContentModel.TYPE_CONTENT);

            nodeService.addAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT, null);
                                    
            File file = TempFileProvider.createTempFile("cifs", ".bin");
            
            TempNetworkFile netFile = new TempNetworkFile(file, path);
            
            // Always allow write access to a newly created file
            netFile.setGrantedAccess(NetworkFile.READWRITE);
            netFile.setAllowedAccess(NetworkFile.READWRITE);
            
         

            // Generate a file id for the file
            
            if ( netFile != null) 
            {
                long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
                netFile.setFileId((int) (id & 0xFFFFFFFFL));
            }
           
            if (logger.isDebugEnabled())
            {
                logger.debug("Created file: path=" + path + " node=" + nodeRef + " network file=" + netFile);
            }
            
            // Return the new network file
            
            return netFile;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create file - access denied, " + path);
            }
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Unable to create file " + path);
        }
        catch (IOException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create file - content I/O error, " + path);
            }
            
            throw ex;
        }
        catch (ContentIOException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create file - content I/O error, " + path);
            }
            // Convert to a filesystem disk full status
            
            throw new DiskFullException("Unable to create file " + path);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Create file error", ex);
            }
            
            // Convert to a general I/O exception
            
            throw new IOException("Unable to create file " + path, ex);
        } 
    }
    
    /**
     * Open the file
     */
    public NetworkFile openFile(String path, OpenFileMode mode, boolean truncate)
    {
        return null;
        
    }
    
    /**
     * Close the file.
     * 
     * @exception java.io.IOException If an error occurs.
     */
    public void closeFile(NodeRef rootNode, String path, NetworkFile file) throws IOException
    {   
        if ( logger.isDebugEnabled())
        {
            logger.debug("Close file:" + path);
        }
        
        if( file instanceof PseudoNetworkFile)
        {
            file.close();
            return;
        }
        
        /**
         * Delete on close attribute - node needs to be deleted.
         */
        if(file.hasDeleteOnClose())
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("closeFile has delete on close set");
            }
            try
            {
                NodeRef target = getCifsHelper().getNodeRef(rootNode, path);
                if(target!=null)
                {
                    nodeService.deleteNode(target);
                }
            }
            catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
            {
                if ( logger.isDebugEnabled())
                {   
                    logger.debug("Delete file from close file- access denied", ex);
                }
                // Convert to a filesystem access denied status
                throw new AccessDeniedException("Unable to delete " + path);
            }
            
            // Still need to close the open file handle.
            file.close();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Closed file: network file=" + file + " delete on close=" + file.hasDeleteOnClose());
            }
          
            return;
        }
        
        // Check for a temp file - which will be a new file or a read/write file
        if ( file instanceof TempNetworkFile) 
        {   
            if(logger.isDebugEnabled())
            {
                logger.debug("Got a temp network file ");
            }
            
            TempNetworkFile tempFile =(TempNetworkFile)file;
            
            NodeRef target = getCifsHelper().getNodeRef(rootNode, tempFile.getFullName());
            
            if(nodeService.hasAspect(target, ContentModel.ASPECT_NO_CONTENT))
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("removed no content aspect");
                }
                nodeService.removeAspect(target, ContentModel.ASPECT_NO_CONTENT);
            }
            
            if(tempFile.getWriteCount() > 0) 
            {
                // Some content was written to the temp file.
                tempFile.flushFile();
                tempFile.close();
                
                /**
                 * Take over the behaviour of the auditable aspect         
                 */
                getPolicyFilter().disableBehaviour(target, ContentModel.ASPECT_AUDITABLE);
                nodeService.setProperty(target, ContentModel.PROP_MODIFIER, authService.getCurrentUserName());
                nodeService.setProperty(target, ContentModel.PROP_MODIFIED, new Date(tempFile.getModifyDate()));               
            
                // Take an initial guess at the mimetype (if it has not been set by something already)
                String mimetype = mimetypeService.guessMimetype(tempFile.getFullName(), new FileContentReader(tempFile.getFile()));
                logger.debug("guesssed mimetype:" + mimetype);
            
                String encoding;
                // Take a guess at the locale
                InputStream is = new BufferedInputStream(new FileInputStream(tempFile.getFile()));
                try
                {
                    ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
                    Charset charset = charsetFinder.getCharset(is, mimetype);
                    encoding = charset.name();
                }
                finally
                {
                    if(is != null)
                    {
                        is.close();
                    }
                }
                ContentWriter writer = contentService.getWriter(target, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetype);
                writer.setEncoding(encoding);
                writer.putContent(tempFile.getFile());
            }
        }
        
        try
        {                                           
            // Defer to the network file to close the stream and remove the content
                       
            file.close();                    
            
            // DEBUG
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Closed file: network file=" + file + " delete on close=" + file.hasDeleteOnClose());
                
                if ( file.hasDeleteOnClose() == false && file instanceof ContentNetworkFile) 
                {
                    ContentNetworkFile cFile = (ContentNetworkFile) file;
                    logger.debug("  File " + file.getFullName() + ", version=" + nodeService.getProperty( cFile.getNodeRef(), ContentModel.PROP_VERSION_LABEL));
                }
            }
        }
        catch (IOException e)
        {
            if ( logger.isDebugEnabled())
            {   
                logger.debug("Exception in closeFile - path:" + path, e);
            }
            throw new IOException("Unable to closeFile :" + path + e.toString(), e);
        }
        catch (Error e)
        {
            if ( logger.isDebugEnabled())
            {   
                logger.debug("Exception in closeFile - path:" + path, e);
            }
            
            throw e;
        }
    }
    
    /**
     * 
     * @param session
     * @param tree
     * @param file
     */
    public void reduceQuota(SrvSession session, TreeConnection tree, NetworkFile file)
    {
        if(file.hasDeleteOnClose())
        {
            final ContentContext ctx = (ContentContext) tree.getContext();
            
            if(logger.isDebugEnabled())
            {
                logger.debug("closeFile has delete on close set");
            }

            if(file instanceof TempNetworkFile)
            {
                TempNetworkFile tnf = (TempNetworkFile)file;
                final QuotaManager quotaMgr = ctx.getQuotaManager();
                if (quotaMgr != null)
                {
                    try
                    {
                        quotaMgr.releaseSpace(session, tree, file.getFileId(), file.getName(), tnf.getFileSizeInt());
                    } 
                    catch (IOException e)
                    {
                        logger.error(e);
                    }
                }
            }
        }
    }
    
    public void deleteEmptyFile(NodeRef rootNode, String path)
    {
        try
        {
            NodeRef target = getCifsHelper().getNodeRef(rootNode, path);
                if(target!=null)
            {
                if (nodeService.hasAspect(target, ContentModel.ASPECT_NO_CONTENT))
                {
                    nodeService.deleteNode(target);
                }
            }
        }
        catch(IOException ne)
        {
            // Do nothing
            if ( logger.isDebugEnabled())
            {   
                logger.debug("Unable to delete empty file:" + path, ne);
            }
      
        }
    }

    @Override
    public OpLockManager getOpLockManager(SrvSession sess, TreeConnection tree)
    {
        AlfrescoContext alfCtx = (AlfrescoContext) tree.getContext();
        return alfCtx.getOpLockManager();        
    }

    @Override
    public boolean isOpLocksEnabled(SrvSession sess, TreeConnection tree)
    {
        if(getOpLockManager(sess, tree) != null) 
        {
            return true;
        }
        return false;
    }        
}
