/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.jlan.server.filesys.DirectoryNotEmptyException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskFullException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSizeInterface;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.PermissionDeniedException;
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
import org.alfresco.jlan.smb.server.SMBServer;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.jlan.util.MemorySize;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
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
    private ContentComparator contentComparator;
    private NodeArchiveService nodeArchiveService;
    private HiddenAspect hiddenAspect;
    private LockKeeper lockKeeper;

    // TODO Should not be here - should be specific to a context.
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
        PropertyCheck.mandatory(this, "contentComparator", getContentComparator());
        PropertyCheck.mandatory(this, "nodeArchiveService", nodeArchiveService);
        PropertyCheck.mandatory(this, "hiddenAspect", hiddenAspect);
        PropertyCheck.mandatory(this, "lockKeeper", lockKeeper);
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
    
    /**
     * @param hiddenAspect
     */
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }
    
    /**
     * @param hiddenAspect
     */
    public void setAlfrescoLockKeeper(LockKeeper lockKeeper)
    {
        this.lockKeeper = lockKeeper;
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
        logger.error("Obsolete method called");
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
        
        boolean readOnly = !m_transactionService.getAllowWrite();
        
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
                
                finfo = getCifsHelper().getFileInformation(nodeRef, readOnly, isLockedFilesAsOffline);
                
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
            
            DotDotContentSearchContext searchCtx = new DotDotContentSearchContext(getCifsHelper(), results, searchFileSpec, pseudoList, paths[0], isLockedFilesAsOffline);          

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
     * Open a file or folder - obsolete implementation.
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param params FileOpenParams
     * @return NetworkFile
     * @exception IOException
     */
    public NetworkFile openFile(SrvSession session, TreeConnection tree, FileOpenParams params) throws IOException
    {
        // obsolete
        logger.error("Obsolete method called");
        throw new AlfrescoRuntimeException("obsolete method called");
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
        logger.error("Obsolete method called");
        throw new AlfrescoRuntimeException("obsolete method called");
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
        throw new AlfrescoRuntimeException("obsolete method called");
    }

    public void deleteFile(final SrvSession session, final TreeConnection tree, final String name) throws IOException
    {
        throw new AlfrescoRuntimeException("obsolete method called");
    }

    /**
     * Delete the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file NetworkFile
     * @exception java.io.IOException The exception description.
     * @return NodeRef of deletedFile
     */
    public NodeRef deleteFile2(final SrvSession session, final TreeConnection tree, NodeRef rootNode, String path) throws IOException
    {
        // Get the device context
        
        final ContentContext ctx = (ContentContext) tree.getContext();
        
        if(logger.isDebugEnabled())
        {
            logger.debug("deleteFile:" + path + ", session:" + session.getUniqueId());
        }
        
        try
        {
            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(path);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(tree, paths[0]);
              
                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    return null;
                }
            }
            
            // Check if there is a quota manager enabled, if so then we need to save the current file size
            
            final QuotaManager quotaMgr = ctx.getQuotaManager();

            // Get the node and delete it
            final NodeRef nodeRef = getNodeForPath(tree, path);
                    
            if (fileFolderService.exists(nodeRef))
            {
            	lockKeeper.removeLock(nodeRef);
            	
                // Get the size of the file being deleted        
                final FileInfo fInfo = quotaMgr == null ? null : getFileInformation(session, tree, path);

                if(logger.isDebugEnabled())
                {
                    logger.debug("deleted file" + path);
                }
                fileFolderService.delete(nodeRef);
                
                //TODO Needs to be post-commit
                if (quotaMgr != null)
                {
                    quotaMgr.releaseSpace(session, tree, fInfo.getFileId(), path, fInfo.getSize());
                }
         
                // Debug
                    
                if (logger.isDebugEnabled())
                {
                   logger.debug("Deleted file: " + path + ", nodeRef=" + nodeRef);
                }

                // void return
                return nodeRef;
            }
        }
        catch (NodeLockedException ex)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Delete file - access denied (locked)", ex);
            }
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Unable to delete " + path);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
            {   
                logger.debug("Delete file - access denied", ex);
            }
            
            // Convert to a filesystem access denied status
            throw new AccessDeniedException("Unable to delete " + path);
        }
        catch (IOException ex)
        {
            // Allow I/O Exceptions to pass through
            if ( logger.isDebugEnabled())
            {
                logger.debug("Delete file error - pass through IO Exception", ex);
            }
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
            IOException ioe = new IOException("Delete file " + path);
            ioe.initCause(ex);
            throw ioe;
        }
        return null;
    }
    
    public void renameFile(final SrvSession session, final TreeConnection tree, final String oldName, final String newName)
    throws IOException
    {
        throw new AlfrescoRuntimeException("obsolete method called");
    }
    
    /**
     * Rename the specified file.
     * 
     * @param rootNode
     * @param oldName path/name of old file
     * @param newName path/name of new file
     * @exception java.io.IOException The exception description.
     */
    public void renameFile(NodeRef rootNode, final String oldName, final String newName, boolean soft, boolean moveAsSystem)
            throws IOException
    {
 
        if (logger.isDebugEnabled())
        {
            logger.debug("RenameFile oldName=" + oldName + ", newName=" + newName + ", soft" + soft);
        }
        
        try
        {
            // Get the file/folder to move
            final NodeRef nodeToMoveRef = getCifsHelper().getNodeRef(rootNode, oldName);

            // Check if the node is a link node

            if ( nodeToMoveRef != null && nodeService.getProperty(nodeToMoveRef, ContentModel.PROP_LINK_DESTINATION) != null)
            {
                throw new AccessDeniedException("Cannot rename link nodes");
            }

            // Get the new target folder - it must be a folder
            String[] splitPaths = FileName.splitPath(newName);
            String[] oldPaths = FileName.splitPath(oldName);

            final NodeRef targetFolderRef = getCifsHelper().getNodeRef(rootNode, splitPaths[0]);
            final NodeRef sourceFolderRef = getCifsHelper().getNodeRef(rootNode, oldPaths[0]);
            final String name = splitPaths[1];

            // Check if this is a rename within the same folder
            
            final boolean sameFolder = splitPaths[0].equalsIgnoreCase(oldPaths[0]);

            // Check if we are renaming a folder, or the rename is to a different folder
            boolean isFolder = getCifsHelper().isDirectory(nodeToMoveRef);

            if ( isFolder == true || sameFolder == false) 
            {
                
                // Rename or move the file/folder to another folder
                if (sameFolder == true)
                {
                    fileFolderService.rename(nodeToMoveRef, name);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("  Renamed " + (isFolder ? "folder" : "file") + ":" +
                                "   Old name:      " + oldName + "\n" +
                                "   New name:      " + newName + "\n" );
                    }
                    
                }
                else
                {
                    if (moveAsSystem)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Run move as System for: " + oldName);
                        }
                        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                return fileFolderService.moveFrom(nodeToMoveRef, sourceFolderRef, targetFolderRef, name);
                            }
                        }, AuthenticationUtil.getSystemUserName());
                    }
                    else
                    {
                        fileFolderService.moveFrom(nodeToMoveRef, sourceFolderRef, targetFolderRef, name);
                    }

                    logger.debug(
                            "Moved between different folders: \n" +
                            "   Old name:      " + oldName + "\n" +
                            "   New name:      " + newName + "\n" +
                            "   Source folder: " + sourceFolderRef + "\n" +
                            "   Target folder: " + targetFolderRef + "\n" +
                            "   Node:          " + nodeToMoveRef + "\n" +
                            "   Aspects:       " + nodeService.getAspects(nodeToMoveRef));
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
                if(soft)
                {
                    logger.debug("this is a soft delete - use copy rather than rename");
                    fileFolderService.copy(nodeToMoveRef, null, name);
                    nodeService.addAspect(nodeToMoveRef, ContentModel.ASPECT_SOFT_DELETE, null);
                }
                else
                {
                    fileFolderService.rename(nodeToMoveRef, name);
                }        
            }
        } 
        catch (org.alfresco.service.cmr.model.FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file - about to throw file not exists exception file:" + oldName, e);
            }
            throw new java.io.FileNotFoundException("renameFile: file not found file: + oldName");
        }
        catch (org.alfresco.service.cmr.model.FileExistsException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file - about to throw file exists exception", e);
            }
            throw new org.alfresco.jlan.server.filesys.FileExistsException(newName);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file - about to throw permissions denied exception", ex);
            }
            throw new org.alfresco.jlan.server.filesys.PermissionDeniedException("renameFile: No permissions to rename file:" + oldName);
        }
        catch (NodeLockedException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file - about to throw access denied exception", ex);
            }
            // Convert to an filesystem access denied exception
            throw new AccessDeniedException("renameFile:  Access Denied - Node locked file:" + oldName);
        }      
        catch (AlfrescoRuntimeException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Rename file about to throw access denied exception", ex);
            }
            throw new AlfrescoRuntimeException("renameFile failed: \n" +
                    "   Old name:      " + oldName + "\n" +
                    "   New name:      " + newName + "\n" +
                    ex);

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
            
           /* 
            * Which DeleteOnClose flag has priority?
            * SetDeleteOnClose is not set or used in this method.   
            * The NTProtocolHandler sets the deleteOnClose in both
            * info and the NetworkFile - it's the one in NetworkFile that results in the file being deleted.
            */ 
            if ( info.hasSetFlag(FileInfo.SetDeleteOnClose) && info.hasDeleteOnClose())
            {
               if(logger.isDebugEnabled())
                {
                    logger.debug("Set Delete On Close for :" + name);
                } 
                // Check for delete permission
                if ( permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.DENIED)
                {
                    throw new PermissionDeniedException("No delete access to :" + name);
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
            
            if(info.hasSetFlag(FileInfo.SetAttributes))
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set attributes" + name + ", file attrs = " + info.getFileAttributes());
                }
                
                //TODO MER Think we may need to implement, Temporary, Hidden, System, Archive
                if(info.isSystem())
                {
                    logger.debug("Set system aspect (not yet implemented)" + name);
                }
                if(info.isTemporary())
                {
                    logger.debug("Set temporary aspect (not yet implemented)" + name);
                }
                        
                if(info.isHidden())
                {
                    // yes is hidden
                    if ( logger.isDebugEnabled())
                    {
                            logger.debug("Set hidden aspect" + name);
                    }
                    hiddenAspect.hideNodeExplicit(nodeRef);
                }
                else
                {
                    // not hidden 
                    if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
                    {
                        if ( logger.isDebugEnabled())
                        {
                            logger.debug("Reset hidden aspect" + name);
                        }
                        hiddenAspect.unhideExplicit(nodeRef);
                    }
                }
            } // End of setting attributes
            
            if( info.hasSetFlag(FileInfo.SetAllocationSize))
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set allocation size" + name + info.getAllocationSize());
                }
                // Not yet implemented
            }
            
            if( info.hasSetFlag(FileInfo.SetFileSize))
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set file size" + name + info.getSize());
                }
                // Not yet implemented
            }
            
            if( info.hasSetFlag(FileInfo.SetMode))
            {
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set Mode" + name + info.getMode());
                }
                // Not yet implemented - set the unix mode e.g. 777
            }
                    
            // Set the creation and modified date/time
            Map<QName, Serializable> auditableProps = new HashMap<QName, Serializable>(5);
            

            
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
                    if(networkFile instanceof TempNetworkFile)
                    {
                        TempNetworkFile tnf = (TempNetworkFile)networkFile;
                        tnf.setModificationDateSetDirectly(true);
                    }
                }
                
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set modification date" + name + ", " + modifyDate);
                }
                
            }
            
            // Change Date is last write ?
            if ( info.hasSetFlag(FileInfo.SetChangeDate) && info.hasChangeDateTime()) 
            {                
                Date changeDate = new Date( info.getChangeDateTime());
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set change date (Not implemented)" + name + ", " + changeDate);
                }
                
            }
            if ( info.hasSetFlag(FileInfo.SetAccessDate) && info.hasAccessDateTime()) 
            {
                Date accessDate = new Date( info.getAccessDateTime());
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Set access date (Not implemented)" + name + ", " + accessDate);
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
                if(writeLogger.isDebugEnabled())
                {
                    writeLogger.debug("writeFile: allocate more space fileName:" + file.getName() + ", extendTo:"+ extendSize);
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
//    private String getPathForNode( TreeConnection tree, NodeRef nodeRef)
//    	throws FileNotFoundException
//    {
//     	// Convert the target node to a path
//        ContentContext ctx = (ContentContext) tree.getContext();
//    	
//    	return getPathForNode(ctx.getRootNode(), nodeRef);
//    	
//    }
//    
    /**
     * Convert a node into a share relative path
     * 
     * @param tree rootNode
     * @param nodeRef NodeRef
     * @return String
     * @exception FileNotFoundException
     */
    private String getPathForNode( NodeRef rootNode, NodeRef nodeRef)
        throws FileNotFoundException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("getPathForNode:" + nodeRef);
        }
        
        List<org.alfresco.service.cmr.model.FileInfo> linkPaths = null;
        
        try 
        {
            linkPaths = fileFolderService.getNamePath( rootNode, nodeRef);
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
    	
        if(logger.isDebugEnabled())
        {
            logger.debug("getDiskInformation returning diskDev:" + diskDev);
        }
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
        catch(IOControlNotImplementedException ioException)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("IO Control Not Implemented Exception fid:" + fid, ioException);
            }
            throw ioException;
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
            logger.debug("no content to save");
            // No content to set - need to remove old content
            ContentWriter writer = contentService.getWriter(targetNodeRef, ContentModel.PROP_CONTENT, true);
            writer.putContent("");
        }
     
    }

    @Override
    public NetworkFile createFile(NodeRef rootNode, String path, long allocationSize, boolean isHidden)
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
            
            boolean soft = false;
            
            NodeRef existing = fileFolderService.searchSimple(dirNodeRef, folderName);
            if (existing != null)
            {
                if(nodeService.hasAspect(existing, ContentModel.ASPECT_SOFT_DELETE))
                {
                    logger.debug("existing node has soft delete aspect");
                    soft = true;
                }
            }
            
            NodeRef nodeRef = null;
            
            if(soft)
            {
                nodeRef = existing;
            }
            else
            {
                nodeRef = cifsHelper.createNode(dirNodeRef, folderName, ContentModel.TYPE_CONTENT);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT, null);
                lockKeeper.addLock(nodeRef);
            }
            
            if(isHidden)
            {
                // yes is hidden
                if ( logger.isDebugEnabled())
                {
                        logger.debug("Set hidden aspect, nodeRef:" + nodeRef);
                }
                hiddenAspect.hideNodeExplicit(nodeRef);
            }
            
            File file = TempFileProvider.createTempFile("cifs", ".bin");
            
            TempNetworkFile netFile = new TempNetworkFile(file, path);
            netFile.setChanged(true);
            
            Serializable created = nodeService.getProperty(nodeRef, ContentModel.PROP_CREATED); 
            if(created != null && created instanceof Date)
            {
                Date d = (Date)created;
                if(logger.isDebugEnabled())
                {
                    logger.debug("replacing create date to date:" + d);
                }
                netFile.setCreationDate(d.getTime());
                netFile.setModifyDate(d.getTime());
            }
            
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
     * Open the file - Repo Specific implementation 
     */
    public NetworkFile openFile(SrvSession session, TreeConnection tree, NodeRef rootNode, String path, OpenFileMode mode, boolean truncate) throws IOException
    {
        ContentContext ctx = (ContentContext) tree.getContext();

        if(logger.isDebugEnabled())
        {
            logger.debug("openFile :" + path + ", mode:" + mode );
        }
        try
        {  
            String name = path;

            if(session.isPseudoFilesEnabled() && ctx.isPseudoFilesEnabled())
            {
                String[] paths = FileName.splitPath(name);
                // lookup parent directory
                NodeRef dirNodeRef = getNodeForPath(rootNode, paths[0]);

                // Check whether we are opening a pseudo file
                if(ctx.getPseudoFileOverlay().isPseudoFile(dirNodeRef, paths[1]))
                {
                    PseudoFile pfile =  ctx.getPseudoFileOverlay().getPseudoFile(dirNodeRef, paths[1]);
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Opened pseudo file :" + pfile);
                    }
                    return pfile.getFile( path);
                }
            }

            // not a psudo file

            NodeRef nodeRef = getNodeForPath(rootNode, path);
            
            boolean readOnly=false;

            // Check permissions on the file/folder
            switch(mode)
            {
            case READ_ONLY:
                // follow through
            case ATTRIBUTES_ONLY:
                if(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("about to throw an no read access denied exception path:" +path);
                    }
                    throw new AccessDeniedException("No read access to " + path);
                }
                readOnly = true;
                break;

            case READ_WRITE:
            case WRITE_ONLY:
            	if(!m_transactionService.getAllowWrite())
            	{
            		 throw new AccessDeniedException("Repo is write only, No write access to " + path);
            	}
                if(permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("about to throw an no write access denied exception path:" + path);
                    }
     
                    throw new AccessDeniedException("No write access to " + path);
                }
                lockService.checkForLock(nodeRef);
                readOnly=false;
                break;
            case DELETE:  
            	if(!m_transactionService.getAllowWrite())
            	{
            		 throw new AccessDeniedException("Repo is write only, No write access to " + path);
            	}
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
                    netFile = new AlfrescoFolder(path, fileInfo, readOnly);              
                }
                else
                {
                    // A normal file
                    switch (mode)
                    {
                        case READ_ONLY:
                                               
                            logger.debug("open file for read only");
                            netFile = ContentNetworkFile.createFile(nodeService, contentService, mimetypeService, getCifsHelper(), nodeRef, path, true, false, session);
                            netFile.setGrantedAccess( NetworkFile.READONLY);
                            break;
                    
                        case READ_WRITE:
                        {
                            logger.debug("open file for read write");
                            File file = TempFileProvider.createTempFile("cifs", ".bin");
                            
                            lockKeeper.addLock(nodeRef);

                            if(!truncate)
                            {
                                // Need to open a temp file with a copy of the content.
                                ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                                if(reader != null)
                                {
                                    reader.getContent(file);
                                }
                            }

                            netFile = new TempNetworkFile(file, name);
                            netFile.setCreationDate(fileInfo.getCreationDateTime());
                            netFile.setModifyDate(fileInfo.getModifyDateTime());
                            
                            netFile.setGrantedAccess( NetworkFile.READWRITE);
                             
                            if(truncate)
                            {
                                netFile.truncateFile(0);
                            }

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Created file: path=" + name + " node=" + nodeRef + " network file=" + netFile);
                            }

                        }
                            break;
                        
                        case ATTRIBUTES_ONLY:
                            logger.debug("open file for attributes only");
                            netFile = ContentNetworkFile.createFile(nodeService, contentService, mimetypeService, getCifsHelper(), nodeRef, path, true, true, session);
                            netFile.setGrantedAccess( NetworkFile.READONLY);
                            break;
                        
                        case DELETE:
                            //TODO Not sure about this one.
                            logger.debug("open file for delete");
                            netFile = ContentNetworkFile.createFile(nodeService, contentService, mimetypeService, getCifsHelper(), nodeRef, path, true, false, session);
                            netFile.setGrantedAccess( NetworkFile.READONLY);
                            break;
                            
                        case WRITE_ONLY:
                          {
                              // consider this as open read/write/truncate)
                            logger.debug("open file write only");
                            File file = TempFileProvider.createTempFile("cifs", ".bin");

                            netFile = new TempNetworkFile(file, name);
                            
                            // Needs to be READWRITE for JavaNetworkFile - there's no such thing as WRITEONLY!
                            netFile.setGrantedAccess( NetworkFile.READWRITE);

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Created temporary file: path=" + name + " node=" + nodeRef + " network file=" + netFile);
                            }
                        }
                    }
                } // end of a normal file
            }
            else
            {
                // This is a link node

                // TODO - This server name stuff should be replaced In particular the 
                // See PseudoFileOverlayImp
                // Get the CIFS server name

                String srvName = null;
                SMBServer cifsServer = (SMBServer) session.getServer().getConfiguration().findServer( "CIFS");

                if(session instanceof SMBSrvSession)
                {
                    SMBSrvSession smbSess = (SMBSrvSession)session;
                    srvName = smbSess.getShareHostName();
                }
                else if ( cifsServer != null)
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

                String pathl = getPathForNode( rootNode, linkRef);
                path = pathl.replace( FileName.DOS_SEPERATOR, '/');

                String lnkForWinPath = convertStringToUnicode(path);
                
                // Build the URL file data

                StringBuilder urlStr = new StringBuilder();

                urlStr.append("[InternetShortcut]\r\n");
                urlStr.append("URL=file://");
                urlStr.append( srvName);
                urlStr.append("/");
                urlStr.append( tree.getSharedDevice().getName());
                urlStr.append( lnkForWinPath);
                urlStr.append("\r\n");

                // Create the in memory pseudo file for the URL link

                byte[] urlData = urlStr.toString().getBytes();

                // Get the file information for the link node

                FileInfo fInfo = getCifsHelper().getFileInformation( nodeRef, false, isLockedFilesAsOffline);

                // Set the file size to the actual data length

                fInfo.setFileSize( urlData.length);

                // Create the network file using the in-memory file data

                netFile = new LinkMemoryNetworkFile( fInfo.getFileName(), urlData, fInfo, nodeRef);
                netFile.setFullName( pathl);
            }

            // Generate a file id for the file

            if ( netFile != null) 
            {
                long id = DefaultTypeConverter.INSTANCE.convert(Long.class, nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
                netFile.setFileId(( int) ( id & 0xFFFFFFFFL));
                
                // Indicate the file is open
                
                netFile.setClosed( false);
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Opened network file: path=" + path + " network file=" + netFile);
            }

            // Return the network file

            return netFile;
        }
        catch (NodeLockedException nle)
        {
            if ( logger.isDebugEnabled())
            {
                logger.debug("Open file - node is locked, " + path);
            }
            throw new AccessDeniedException("File is locked, no write access to " + path);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug

            if ( logger.isDebugEnabled())
            {
                logger.debug("Open file - access denied, " + path);
            } 
            // Convert to a filesystem access denied status

            throw new AccessDeniedException("Open file " + path);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug

            if (logger.isDebugEnabled())
            {
                logger.debug("Open file error", ex);
            }
            // Convert to a general I/O exception

            throw new IOException("Open file " + path, ex);
        }        
    }
    
    private String convertStringToUnicode(String str)
    {
        StringBuffer ostr = new StringBuffer();
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            // Does the char need to be converted to unicode?
            if ((ch >= 0x0020) && (ch <= 0x007e))
            {
                // No
                ostr.append(ch);
            }
            else if (ch > 0xFF)
            {
                // No
                ostr.append(ch);
            }
            // Yes.
            else
            {
                ostr.append("%");
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);
                hex.length();
                // Prepend zeros because unicode requires 2 digits
                for (int j = 0; j < 2 - hex.length(); j++)

                    ostr.append("0");
                ostr.append(hex.toLowerCase());
            }
        }
        return (new String(ostr));
    }
    
    /**
     * Close the file.
     * 
     * @exception java.io.IOException If an error occurs.
     * @return node ref of deleted file
     */
    public NodeRef closeFile(NodeRef rootNode, String path, NetworkFile file) throws IOException
    {   
        if ( logger.isDebugEnabled())
        {
            logger.debug("Close file:" + path + ", readOnly=" + file.isReadOnly() );
        }
        
        if( file instanceof PseudoNetworkFile)
        {
            file.close();
            return null;
        }
        
        /**
         * Delete on close attribute - node needs to be deleted.
         */
        if(file.hasDeleteOnClose())
        {
            NodeRef target = null;
            
            if(logger.isDebugEnabled())
            {
                logger.debug("closeFile has delete on close set path:" + path);
            }
            try
            {
                target = getCifsHelper().getNodeRef(rootNode, path);
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
          
            return target;
        }
        
        // Check for a temp file - which will be a new file or a read/write file
        if ( file instanceof TempNetworkFile) 
        {   
            if(logger.isDebugEnabled())
            {
                logger.debug("Got a temp network file to close path:" + path);
            }
            
            // Some content was written to the temp file.
            TempNetworkFile tempFile =(TempNetworkFile)file;
            
            NodeRef target = getCifsHelper().getNodeRef(rootNode, tempFile.getFullName());
            
            lockKeeper.removeLock(target);
            
            if(nodeService.hasAspect(target, ContentModel.ASPECT_NO_CONTENT))
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("removed no content aspect");
                }
                nodeService.removeAspect(target, ContentModel.ASPECT_NO_CONTENT);
            }
            
            if(tempFile.isChanged()) 
            {
                tempFile.flushFile();
                tempFile.close();
                
                /*
                 * Need to work out whether content has changed.  Some odd situations do not change content.
                 */
                boolean contentChanged = true;
                
                ContentReader existingContent = contentService.getReader(target, ContentModel.PROP_CONTENT);
                if(existingContent != null)
                {
                    existingContent.getSize();
                    existingContent.getMimetype();
                    contentChanged = isContentChanged(existingContent, tempFile);
                
                    /* 
                     * MNT-248 fix
                     * No need to create a version of a zero byte file
                     */
                    if (file.getFileSize() > 0 && existingContent.getSize() == 0 && nodeService.hasAspect(target, ContentModel.ASPECT_VERSIONABLE))
                    {
                        getPolicyFilter().disableBehaviour(target, ContentModel.ASPECT_VERSIONABLE);
                    }
                }
                      
                if(contentChanged)
                {
                    logger.debug("content has changed, need to create a new content item");
                
                    /**
                     * Take over the behaviour of the auditable aspect         
                     */
                    getPolicyFilter().disableBehaviour(target, ContentModel.ASPECT_AUDITABLE);
                    nodeService.setProperty(target, ContentModel.PROP_MODIFIER, authService.getCurrentUserName());
                    if(tempFile.isModificationDateSetDirectly())
                    {
                        logger.debug("modification date set directly");
                        nodeService.setProperty(target, ContentModel.PROP_MODIFIED, new Date(tempFile.getModifyDate()));
                    }
                    else
                    {
                        logger.debug("modification date not set directly");
                        nodeService.setProperty(target, ContentModel.PROP_MODIFIED, new Date());
                    }
            
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
                            try
                            {
                               is.close();
                            }
                            catch (IOException e)
                            {
                               // Ignore
                            }
                        }
                    }
                    ContentWriter writer = contentService.getWriter(target, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(mimetype);
                    writer.setEncoding(encoding);
                    writer.putContent(tempFile.getFile());
                } // if content changed
            }
        }
        
        try
        {                                           
            // Defer to the network file to close the stream and remove the content
                       
            file.close();                    
            
            // DEBUG
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Closed file: network file=" + file + " delete on close=" + file.hasDeleteOnClose() + ", write count" + file.getWriteCount());
                
                if ( file.hasDeleteOnClose() == false && file instanceof ContentNetworkFile) 
                {
                    ContentNetworkFile cFile = (ContentNetworkFile) file;
                    logger.debug("  File " + file.getFullName() + ", version=" + nodeService.getProperty( cFile.getNodeRef(), ContentModel.PROP_VERSION_LABEL));
                }
            }
            
            return null;
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
    
    /**
     * Compare the content for significant changes.  For example Project and Excel play with headers, 
     * which should not result in new versions being created.
     * @param existingContent
     * @param newFile
     * @return true the content has changed, false the content has not changed significantly.
     */
    private boolean isContentChanged(ContentReader existingContent, TempNetworkFile newFile)
    {
        return !contentComparator.isContentEqual(existingContent, newFile.getFile());
    }

    public void setContentComparator(ContentComparator contentComparator)
    {
        this.contentComparator = contentComparator;
    }

    public ContentComparator getContentComparator()
    {
        return contentComparator;
    }

    @Override
    public NetworkFile restoreFile(
            SrvSession sess, 
            TreeConnection tree, 
            NodeRef rootNode, 
            String path,
            long allocationSize, 
            NodeRef originalNodeRef) throws IOException
    {
        // First attempt to restore the node
        
        if(logger.isDebugEnabled())
        {
            logger.debug("restore node:" + originalNodeRef + ", path:" + path);
        }
        
        NodeRef archivedNodeRef = getNodeArchiveService().getArchivedNode(originalNodeRef);
        
        if(nodeService.exists(archivedNodeRef))
        {
            NodeRef restoredNodeRef = nodeService.restoreNode(archivedNodeRef, null, null, null);
            if (logger.isDebugEnabled())
            {
                logger.debug("node has been restored nodeRef," + restoredNodeRef + ", path " + path);
            }
            
            return openFile(sess, tree, rootNode, path, OpenFileMode.READ_WRITE, true);
        }
        else
        {
            return createFile(rootNode, path, allocationSize, false);
        }
    }

    public void setNodeArchiveService(NodeArchiveService nodeArchiveService)
    {
        this.nodeArchiveService = nodeArchiveService;
    }

    public NodeArchiveService getNodeArchiveService()
    {
        return nodeArchiveService;
    }
}
