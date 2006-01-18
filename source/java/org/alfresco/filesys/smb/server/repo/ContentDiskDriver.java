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
package org.alfresco.filesys.smb.server.repo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.transaction.UserTransaction;

import org.alfresco.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.core.DeviceContext;
import org.alfresco.filesys.server.core.DeviceContextException;
import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.AccessMode;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileSharingException;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.FileSystem;
import org.alfresco.filesys.server.filesys.IOControlNotImplementedException;
import org.alfresco.filesys.server.filesys.IOCtlInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.SharingMode;
import org.alfresco.filesys.smb.server.repo.FileState.FileStateStatus;
import org.alfresco.filesys.smb.server.repo.pseudo.ContentPseudoFileImpl;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFile;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFileInterface;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoNetworkFile;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content repository filesystem driver class
 * 
 * <p>Provides a filesystem interface for various protocols such as SMB/CIFS and FTP.
 * 
 * @author Derek Hulley
 */
public class ContentDiskDriver implements DiskInterface, IOCtlInterface
{
    // Logging
    
    private static final Log logger = LogFactory.getLog(ContentDiskDriver.class);
    
    // Configuration key names
    
    private static final String KEY_STORE = "store";
    private static final String KEY_ROOT_PATH = "rootPath";
    private static final String KEY_RELATIVE_PATH = "relativePath";

    // Services and helpers
    
    private CifsHelper cifsHelper;
    private TransactionService transactionService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private PermissionService permissionService;
    private CheckOutCheckInService checkInOutService;
    
    private AuthenticationComponent authComponent;
    
    // I/O control handler
    
    private IOControlHandler m_ioHandler;
    
    // Pseudo files interface
    
    private PseudoFileInterface m_pseudoFiles;
    
    /**
     * Class constructor
     * 
     * @param serviceRegistry to connect to the repository services
     */
    public ContentDiskDriver(CifsHelper cifsHelper)
    {
        this.cifsHelper = cifsHelper;
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
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
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
     * Set the check in/out service
     * 
     * @param checkInOutService CheckOutCheckInService
     */
    public void setCheckInOutService(CheckOutCheckInService checkInOutService)
    {
        this.checkInOutService = checkInOutService;
    }
    
    /**
     * Set the authentication component
     * 
     * @param authComponent AuthenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authComponent)
    {
        this.authComponent = authComponent;
    }
    
    /**
     * Parse and validate the parameter string and create a device context object for this instance
     * of the shared device. The same DeviceInterface implementation may be used for multiple
     * shares.
     * 
     * @param args ConfigElement
     * @return DeviceContext
     * @exception DeviceContextException
     */
    public DeviceContext createContext(ConfigElement cfg) throws DeviceContextException
    {
        // Use the system user as the authenticated context for the filesystem initialization
        
        authComponent.setCurrentUser( authComponent.getSystemUserName());
        
        // Wrap the initialization in a transaction
        
        UserTransaction tx = transactionService.getUserTransaction(true);

        ContentContext context = null;
        
        try
        {
            // Start the transaction
            
            if ( tx != null)
                tx.begin();
            
            // Get the store
            
            ConfigElement storeElement = cfg.getChild(KEY_STORE);
            if (storeElement == null || storeElement.getValue() == null || storeElement.getValue().length() == 0)
            {
                throw new DeviceContextException("Device missing init value: " + KEY_STORE);
            }
            String storeValue = storeElement.getValue();
            StoreRef storeRef = new StoreRef(storeValue);
            
            // Connect to the repo and ensure that the store exists
            
            if (! nodeService.exists(storeRef))
            {
                throw new DeviceContextException("Store not created prior to application startup: " + storeRef);
            }
            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
            
            // Get the root path
            
            ConfigElement rootPathElement = cfg.getChild(KEY_ROOT_PATH);
            if (rootPathElement == null || rootPathElement.getValue() == null || rootPathElement.getValue().length() == 0)
            {
                throw new DeviceContextException("Device missing init value: " + KEY_ROOT_PATH);
            }
            String rootPath = rootPathElement.getValue();
            
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
                // nothing found
                throw new DeviceContextException("No root found for device: \n" +
                        "   root path: " + rootPath);
            }
            else
            {
                // we found a node
                rootNodeRef = nodeRefs.get(0);
            }

            // Check if a relative path has been specified
            
            ConfigElement relativePathElement = cfg.getChild(KEY_RELATIVE_PATH);
            
            if ( relativePathElement != null)
            {
                // Make sure the path is in CIFS format
                
                String relPath = relativePathElement.getValue().replace( '/', FileName.DOS_SEPERATOR);
                
                // Find the node and validate that the relative path is to a folder
                
                NodeRef relPathNode = cifsHelper.getNodeRef( rootNodeRef, relPath);
                if ( cifsHelper.isDirectory( relPathNode) == false)
                    throw new DeviceContextException("Relative path is not a folder, " + relativePathElement.getValue());
                
                // Use the relative path node as the root of the filesystem
                
                rootNodeRef = relPathNode;
            }
            
            // Commit the transaction
            
            tx.commit();
            tx = null;
            
            // Create the context
            
            context = new ContentContext(storeValue, rootPath, rootNodeRef);

            // Default the filesystem to look like an 80Gb sized disk with 90% free space
            
            context.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));
            
            // Set parameters
            
            context.setFilesystemAttributes(FileSystem.CasePreservedNames);
        }
        catch (Exception ex)
        {
            logger.error("Error during create context", ex);
        }
        finally
        {
            // If there is an active transaction then roll it back
            
            if ( tx != null)
            {
                try
                {
                    tx.rollback();
                }
                catch (Exception ex)
                {
                    logger.warn("Failed to rollback transaction", ex);
                }
            }                
        }

        // Load the I/O control handler, if available
        
        try
        {
            // Load the I/O control handler class
            
            Object ioctlObj = Class.forName("org.alfresco.filesys.server.smb.repo.ContentIOControlHandler").newInstance();
            
            // Verify that the class is an I/O control interface
            
            if ( ioctlObj instanceof IOControlHandler)
            {
                // Set the I/O control handler, and initialize
                
                m_ioHandler = (IOControlHandler) ioctlObj;
                m_ioHandler.initialize( this, cifsHelper, transactionService, nodeService, checkInOutService);
            }
            
            // Initialize the drag and drop pseudo application, if the I/O support has been enabled
            
            if ( m_ioHandler != null)
            {
                ConfigElement dragDropElem = cfg.getChild( "dragAndDrop");
                if ( dragDropElem != null)
                {
                    // Get the pseudo file name and path to the actual file on the local filesystem
                    
                    ConfigElement pseudoName = dragDropElem.getChild( "filename");
                    ConfigElement appPath    = dragDropElem.getChild( "path");
                    
                    if ( pseudoName != null && appPath != null)
                    {
                        // Check that the application exists on the local filesystem
                        
                        URL appURL = this.getClass().getClassLoader().getResource(appPath.getValue());
                        if ( appURL == null)
                            throw new DeviceContextException("Failed to find drag and drop application, " + appPath.getValue());
                        File appFile = new File(appURL.getFile());
                        if ( appFile.exists() == false)
                            throw new DeviceContextException("Drag and drop application not found, " + appPath.getValue());
                        
                        // Create the pseudo file for the drag and drop application
                        
                        PseudoFile dragDropPseudo = new PseudoFile( pseudoName.getValue(), appFile.getAbsolutePath());
                        context.setDragAndDropApp( dragDropPseudo);
                        
                        // Enable pseudo file support
                        
                        m_pseudoFiles = new ContentPseudoFileImpl();
                    }
                }
            }            
        }
        catch (Exception ex)
        {
            if ( logger.isDebugEnabled())
                logger.debug("No I/O control handler available");
        }
        
        // Check if locked files should be marked as offline
        
        ConfigElement offlineFiles = cfg.getChild( "offlineFiles");
        if ( offlineFiles != null)
        {
            // Enable marking locked files as offline
            
            cifsHelper.setMarkLockedFilesAsOffline( true);
            
            // Logging
            
            logger.info("Locked files will be marked as offline");
        }
        
        // Return the context for this shared filesystem
        
        return context;
    }

    /**
     * Check if pseudo file support is enabled
     * 
     * @return boolean
     */
    public final boolean hasPseudoFileInterface()
    {
        return m_pseudoFiles != null ? true : false;
    }
    
    /**
     * Return the pseudo file support implementation
     * 
     * @return PseudoFileInterface
     */
    public final PseudoFileInterface getPseudoFileInterface()
    {
        return m_pseudoFiles;
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
        return false;
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
        // Get the device root
        
        ContentContext ctx = (ContentContext) tree.getContext();
        NodeRef infoParentNodeRef = ctx.getRootNode();
        
        if ( path == null)
            path = "";
        
        String infoPath = path;
        
        try
        {
            // Check if the path is to a pseudo file

            FileInfo finfo = null;
            
            if ( hasPseudoFileInterface())
            {
                // Get the pseudo file
                
                PseudoFile pfile = getPseudoFileInterface().getPseudoFile( session, tree, path);
                if ( pfile != null)
                {
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("getInfo using pseudo file info for " + path);
                    return pfile.getFileInfo();
                }
            }
            
            // Get the node ref for the path, chances are there is a file state in the cache
            
            NodeRef nodeRef = getNodeForPath(tree, infoPath);
            if ( nodeRef != null)
            {
                // Get the file information for the node
                
                finfo = cifsHelper.getFileInformation(nodeRef);

                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("getInfo using cached noderef for path " + path);
            }
            
            // If the required node was not in the state cache, the parent folder node might be
            
            session.beginTransaction(transactionService, true);
            
            if ( finfo == null)
            {
                String[] paths = FileName.splitPath( path);
                
                if ( paths[0] != null && paths[0].length() > 1)
                {
                    // Find the node ref for the folder being searched
                    
                    nodeRef = getNodeForPath(tree, paths[0]);
                    
                    if ( nodeRef != null)
                    {
                        infoParentNodeRef = nodeRef;
                        infoPath          = paths[1];
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("getInfo using cached noderef for parent " + path);
                    }
                }
            
                // Access the repository to get the file information
                
                finfo = cifsHelper.getFileInformation(infoParentNodeRef, infoPath);
                
                // DEBUG
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Getting file information: \n" +
                            "   path: " + path + "\n" +
                            "   file info: " + finfo);
                }
            }

            // Return the file information
            
            return finfo;
        }
        catch (FileNotFoundException e)
        {
            // a valid use case
            if (logger.isDebugEnabled())
            {
                logger.debug("Getting file information - File not found: \n" +
                        "   path: " + path);
            }
            throw e;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Get file info - access denied, " + path);
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Get file information " + path);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Get file info error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Get file information " + path);
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
    public SearchContext startSearch(SrvSession sess, TreeConnection tree, String searchPath, int attributes) throws FileNotFoundException
    {
        try
        {
            // Access the device context
            
            ContentContext ctx = (ContentContext) tree.getContext();

            String searchFileSpec = searchPath;
            NodeRef searchRootNodeRef = ctx.getRootNode();
            FileState searchFolderState = null;
            
            // Create the transaction
            
            sess.beginTransaction(transactionService, true);
            
            // If the state table is available see if we can speed up the search using either cached
            // file information or find the folder node to be searched without having to walk the path
          
            if ( ctx.hasStateTable())
            {
                // See if the folder to be searched has a file state, we can avoid having to walk the path
                
                String[] paths = FileName.splitPath(searchPath);
                if ( paths[0] != null && paths[0].length() > 1)
                {
                    // Get the file state for the folder being searched
                    
                    searchFolderState = getStateForPath(tree, paths[0]);
                    if ( searchFolderState == null)
                        searchFolderState = ctx.getStateTable().findFileState( paths[0], true, true);
                    
                    // Add pseudo files to the folder being searched

                    if ( hasPseudoFileInterface())
                        getPseudoFileInterface().addPseudoFilesToFolder( sess, tree, paths[0]);
                    
                    // Find the node ref for the folder being searched
                    
                    NodeRef nodeRef = getNodeForPath(tree, paths[0]);
                    
                    if ( nodeRef != null)
                    {
                        searchRootNodeRef = nodeRef;
                        searchFileSpec    = paths[1];
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Search using cached noderef for path " + searchPath);
                    }
                }
            }
            
            // Start the search
            
            SearchContext searchCtx = ContentSearchContext.search(cifsHelper, searchRootNodeRef,
                    searchFileSpec, attributes, searchFolderState);
            
            // done
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Started search: \n" +
                        "   search path: " + searchPath + "\n" +
                        "   attributes: " + attributes);
            }
            return searchCtx;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Start search - access denied, " + searchPath);
            
            // Convert to a file not found status
            
            throw new FileNotFoundException("Start search " + searchPath);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Start search", ex);
            
            // Convert to a file not found status
            
            throw new FileNotFoundException("Start search " + searchPath);
        }
    }

    /**
     * Check if the specified file exists, and whether it is a file or directory.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param name java.lang.String
     * @return int
     * @see FileStatus
     */
    public int fileExists(SrvSession sess, TreeConnection tree, String name)
    {
        
        int status = FileStatus.Unknown;
        
        try
        {
            // Check for a cached file state
            
            ContentContext ctx = (ContentContext) tree.getContext();
            FileState fstate = null;
            
            if ( ctx.hasStateTable())
                ctx.getStateTable().findFileState(name);
            
            if ( fstate != null)
            {
                FileStateStatus fsts = fstate.getFileStatus();

                if ( fsts == FileStateStatus.FileExists)
                    status = FileStatus.FileExists;
                else if ( fsts == FileStateStatus.FolderExists)
                    status = FileStatus.DirectoryExists;
                else if ( fsts == FileStateStatus.NotExist || fsts == FileStateStatus.Renamed)
                    status = FileStatus.NotExist;
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Cache hit - fileExists() " + name + ", sts=" + status);
            }
            else
            {
                // Create the transaction
                
                sess.beginTransaction(transactionService, true);
                
                // Get the file information to check if the file/folder exists
                
                FileInfo info = getFileInformation(sess, tree, name);
                if (info.isDirectory())
                {
                    status = FileStatus.DirectoryExists;
                }
                else
                {
                    status = FileStatus.FileExists;
                }
            }
        }
        catch (FileNotFoundException e)
        {
            status = FileStatus.NotExist;
        }
        catch (IOException e)
        {
            // Debug
            
            logger.debug("File exists error, " + name, e);
            
            status = FileStatus.NotExist;
        }

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("File status determined: \n" +
                    "   name: " + name + "\n" +
                    "   status: " + status);
        }
        return status;
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
    public NetworkFile openFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        try
        {
            // Get the node for the path
            
            ContentContext ctx = (ContentContext) tree.getContext();
            
            // Check if pseudo files are enabled
            
            if ( hasPseudoFileInterface())
            {
                // Check if the path is to a pseudo file
                
                PseudoFile pfile = getPseudoFileInterface().getPseudoFile( sess, tree, params.getPath());
                if ( pfile != null)
                {
                    // Create a network file to access the pseudo file data
                    
                    return new PseudoNetworkFile( pfile.getFileName(), pfile.getFilePath(), params.getPath());
                }
            }
            
            // Not a pseudo file, try and open a normal file/folder node
            
            NodeRef nodeRef = getNodeForPath(tree, params.getPath());
            
            // Check permissions on the file/folder node
            //
            // Check for read access
            
            if ( params.hasAccessMode(AccessMode.NTRead) &&
                    permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
                throw new AccessDeniedException("No read access to " + params.getFullPath());
                
            // Check for write access
            
            if ( params.hasAccessMode(AccessMode.NTWrite) &&
                    permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
                throw new AccessDeniedException("No write access to " + params.getFullPath());
            
            // Check for delete access
            
            if ( params.hasAccessMode(AccessMode.NTDelete) &&
                    permissionService.hasPermission(nodeRef, PermissionService.DELETE) == AccessStatus.DENIED)
                throw new AccessDeniedException("No delete access to " + params.getFullPath());
            
            //  Check if there is a file state for the file

            FileState fstate = null;
            
            if ( ctx.hasStateTable())
            {
                // Check if there is a file state for the file

                fstate = ctx.getStateTable().findFileState( params.getPath());
            
                if ( fstate != null)
                {                
                    // Check if the file exists
                    
                    if ( fstate.exists() == false)
                        throw new FileNotFoundException();
                    
                    // Check if the open request shared access indicates exclusive file access
                    
                    if ( fstate != null && params.getSharedAccess() == SharingMode.NOSHARING &&
                            fstate.getOpenCount() > 0)
                        throw new FileSharingException("File already open, " + params.getPath());
                }
            }
            
            // Create the network file
            
            NetworkFile netFile = ContentNetworkFile.createFile(nodeService, contentService, cifsHelper, nodeRef, params);
            
            // Create a file state for the open file
            
            if ( ctx.hasStateTable())
            {
                if ( fstate  == null)
                    fstate = ctx.getStateTable().findFileState(params.getPath(), params.isDirectory(), true);
            
                // Update the file state, cache the node
                
                fstate.incrementOpenCount();
                fstate.setNodeRef(nodeRef);
            }
            
            // Debug
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Opened network file: \n" +
                        "   path: " + params.getPath() + "\n" +
                        "   file open parameters: " + params + "\n" +
                        "   network file: " + netFile);
            }

            // Return the network file
            
            return netFile;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Open file - access denied, " + params.getFullPath());
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Open file " + params.getFullPath());
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Open file error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Open file " + params.getFullPath());
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
    public NetworkFile createFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        try
        {
            // get the device root

            ContentContext ctx = (ContentContext) tree.getContext();
            NodeRef deviceRootNodeRef = ctx.getRootNode();
            
            String path = params.getPath();
            
            // If the state table is available then try to find the parent folder node for the new file
            // to save having to walk the path
          
            if ( ctx.hasStateTable())
            {
                // See if the parent folder has a file state, we can avoid having to walk the path
                
                String[] paths = FileName.splitPath(path);
                if ( paths[0] != null && paths[0].length() > 1)
                {
                    // Find the node ref for the folder being searched
                    
                    NodeRef nodeRef = getNodeForPath(tree, paths[0]);
                    
                    if ( nodeRef != null)
                    {
                        deviceRootNodeRef = nodeRef;
                        path              = paths[1];
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Create file using cached noderef for path " + paths[0]);
                    }
                }
            }
            
            // Create it - the path will be created, if necessary
            
            NodeRef nodeRef = cifsHelper.createNode(deviceRootNodeRef, path, true);
            
            // create the network file
            NetworkFile netFile = ContentNetworkFile.createFile(nodeService, contentService, cifsHelper, nodeRef, params);
            
            // Add a file state for the new file/folder
            
            if ( ctx.hasStateTable())
            {
                FileState fstate = ctx.getStateTable().findFileState(path, false, true);
                if ( fstate != null)
                {
                    // Indicate that the file is open
    
                    fstate.setFileStatus(FileStateStatus.FileExists);
                    fstate.incrementOpenCount();
                    fstate.setNodeRef(nodeRef);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Creaste file, state=" + fstate);
                }
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created file: \n" +
                        "   path: " + path + "\n" +
                        "   file open parameters: " + params + "\n" +
                        "   node: " + nodeRef + "\n" +
                        "   network file: " + netFile);
            }
            return netFile;
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Create file - access denied, " + params.getFullPath());
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Create file " + params.getFullPath());
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Create file error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Create file " + params.getFullPath());
        }
        
    }

    /**
     * Create a new directory on this file system.
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param params Directory create parameters
     * @exception java.io.IOException If an error occurs.
     */
    public void createDirectory(SrvSession sess, TreeConnection tree, FileOpenParams params) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        try
        {
            // get the device root
            
            ContentContext ctx = (ContentContext) tree.getContext();
            NodeRef deviceRootNodeRef = ctx.getRootNode();
            
            String path = params.getPath(); 
            
            // If the state table is available then try to find the parent folder node for the new folder
            // to save having to walk the path
          
            if ( ctx.hasStateTable())
            {
                // See if the parent folder has a file state, we can avoid having to walk the path
                
                String[] paths = FileName.splitPath(path);
                if ( paths[0] != null && paths[0].length() > 1)
                {
                    // Find the node ref for the folder being searched
                    
                    NodeRef nodeRef = getNodeForPath(tree, paths[0]);
                    
                    if ( nodeRef != null)
                    {
                        deviceRootNodeRef = nodeRef;
                        path              = paths[1];
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Create file using cached noderef for path " + paths[0]);
                    }
                }
            }
            
            // Create it - the path will be created, if necessary
            
            NodeRef nodeRef = cifsHelper.createNode(deviceRootNodeRef, path, false);

            // Add a file state for the new folder
            
            if ( ctx.hasStateTable())
            {
                FileState fstate = ctx.getStateTable().findFileState(path, true, true);
                if ( fstate != null)
                {
                    // Indicate that the file is open
    
                    fstate.setFileStatus(FileStateStatus.FolderExists);
                    fstate.incrementOpenCount();
                    fstate.setNodeRef(nodeRef);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Creaste folder, state=" + fstate);
                }
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created directory: \n" +
                        "   path: " + path + "\n" +
                        "   file open params: " + params + "\n" +
                        "   node: " + nodeRef);
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Create directory - access denied, " + params.getFullPath());
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Create directory " + params.getFullPath());
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Create directory error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Create directory " + params.getFullPath());
        }
    }

    /**
     * Delete the directory from the filesystem.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param dir Directory name.
     * @exception java.io.IOException The exception description.
     */
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        // get the device root
        
        ContentContext ctx = (ContentContext) tree.getContext();
        NodeRef deviceRootNodeRef = ctx.getRootNode();
        
        try
        {
            // get the node
            NodeRef nodeRef = cifsHelper.getNodeRef(deviceRootNodeRef, dir);
            if (nodeService.exists(nodeRef))
            {
                nodeService.deleteNode(nodeRef);
                
                // Remove the file state
                
                if ( ctx.hasStateTable())
                    ctx.getStateTable().removeFileState(dir);
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted directory: \n" +
                        "   directory: " + dir + "\n" +
                        "   node: " + nodeRef);
            }
        }
        catch (FileNotFoundException e)
        {
            // already gone
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted directory <alfready gone>: \n" +
                        "   directory: " + dir);
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Delete directory - access denied, " + dir);
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Delete directory " + dir);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Delete directory", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Delete directory " + dir);
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
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file) throws IOException
    {
        // Flush the file data
        
        file.flushFile();
    }

    /**
     * Close the file.
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param param Network file context.
     * @exception java.io.IOException If an error occurs.
     */
    public void closeFile(SrvSession sess, TreeConnection tree, NetworkFile file) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        // Get the associated file state
        
        ContentContext ctx = (ContentContext) tree.getContext();
        
        if ( ctx.hasStateTable())
        {
            FileState fstate = ctx.getStateTable().findFileState(file.getFullName());
            if ( fstate != null)
                fstate.decrementOpenCount();
        }
        
        // Defer to the network file to close the stream and remove the content
           
        file.closeFile();
        
        // Remove the node if marked for delete
        
        if (file.hasDeleteOnClose() && file instanceof ContentNetworkFile)
        {
            ContentNetworkFile contentNetFile = (ContentNetworkFile) file;
            NodeRef nodeRef = contentNetFile.getNodeRef();
            
            // We don't know how long the network file has had the reference, so check for existence
            
            if (nodeService.exists(nodeRef))
            {
                try
                {
                    // Delete the file
                    
                    nodeService.deleteNode(nodeRef);

                    // Remove the file state
                    
                    if ( ctx.hasStateTable())
                        ctx.getStateTable().removeFileState(file.getFullName());
                }
                catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Delete on close - access denied, " + file.getFullName());
                    
                    // Convert to a filesystem access denied exception
                    
                    throw new AccessDeniedException("Delete on close " + file.getFullName());
                }
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Closed file: \n" +
                    "   network file: " + file + "\n" +
                    "   deleted on close: " + file.hasDeleteOnClose());
        }
    }

    /**
     * Delete the specified file.
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file NetworkFile
     * @exception java.io.IOException The exception description.
     */
    public void deleteFile(SrvSession sess, TreeConnection tree, String name) throws IOException
    {
        // Create the transaction
        
        sess.beginTransaction(transactionService, false);
        
        // Get the device context
        
        ContentContext ctx = (ContentContext) tree.getContext();
        
        try
        {
            // get the node
            NodeRef nodeRef = getNodeForPath(tree, name);
            if (nodeService.exists(nodeRef))
            {
                nodeService.deleteNode(nodeRef);
                
                // Remove the file state
                
                if ( ctx.hasStateTable())
                    ctx.getStateTable().removeFileState(name);
            }
            
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted file: \n" +
                        "   file: " + name + "\n" +
                        "   node: " + nodeRef);
            }
        }
        catch (FileNotFoundException e)
        {
            // already gone
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted file <alfready gone>: \n" +
                        "   file: " + name);
            }
        }
        catch (NodeLockedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Delete file - access denied (locked)");
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Delete " + name);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Delete file - access denied");
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Delete " + name);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Delete file error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Delete file " + name);
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
    public void renameFile(SrvSession sess, TreeConnection tree, String oldName, String newName) throws IOException
    {
        // Create the transaction
        sess.beginTransaction(transactionService, false);
        
        try
        {
            // Get the device context
            
            ContentContext ctx = (ContentContext) tree.getContext();
            
            // Get the file/folder to move
            NodeRef nodeToMoveRef = getNodeForPath(tree, oldName);
            
            // Get the new target folder - it must be a folder
            String[] splitPaths = FileName.splitPath(newName);
            NodeRef targetFolderRef = getNodeForPath(tree, splitPaths[0]);
            String name = splitPaths[1];                                    // the new file or folder name

            // Update the state table
            boolean relinked = false;
            if ( ctx.hasStateTable())
            {
                // Check if the file rename can be relinked to a previous version
                
                if ( !cifsHelper.isDirectory(nodeToMoveRef) )
                {
                    // Check if there is a renamed file state for the new file name
                    
                    FileState renState = ctx.getStateTable().removeFileState(newName);
                    
                    if ( renState != null && renState.getFileStatus() == FileStateStatus.Renamed)
                    {
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug(" Found rename state, relinking, " + renState);
                        
                        // Relink the new version of the file data to the previously renamed node so that it
                        // picks up version history and other settings.
                        
                        cifsHelper.relinkNode( renState.getNodeRef(), nodeToMoveRef, targetFolderRef, name);
                        relinked = true;

                        // Link the node ref for the associated rename state
                        
                        if ( renState.hasRenameState())
                            renState.getRenameState().setNodeRef(nodeToMoveRef);
                        
                        // Remove the file state for the old file name
                        
                        ctx.getStateTable().removeFileState(oldName);
                        
                        // Get, or create, a file state for the new file path
                        
                        FileState fstate = ctx.getStateTable().findFileState(newName, false, true);
                        
                        fstate.setNodeRef(renState.getNodeRef());
                        fstate.setFileStatus(FileStateStatus.FileExists);
                    }
                    else
                    {
                        // Get or create a new file state for the old file path
                        
                        FileState fstate = ctx.getStateTable().findFileState(oldName, false, true);
                        
                        // Make sure the file state is cached for a short while, the file may not be open so the
                        // file state could be expired
                        
                        fstate.setExpiryTime(System.currentTimeMillis() + FileState.RenameTimeout);
                        
                        // Indicate that this is a renamed file state, set the node ref of the file that was renamed
                        
                        fstate.setFileStatus(FileStateStatus.Renamed);
                        fstate.setNodeRef(nodeToMoveRef);
                        
                        // Get, or create, a file state for the new file path
                        
                        FileState newState = ctx.getStateTable().findFileState(newName, false, true);
                        
                        newState.setNodeRef(nodeToMoveRef);
                        newState.setFileStatus(FileStateStatus.FileExists);
                        
                        // Link the renamed state to the new state
                        
                        fstate.setRenameState(newState);
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Cached rename state for " + oldName + ", state=" + fstate);
                    }
                }
                else
                {
                    // Get the file state for the folder, if available
                    
                    FileState fstate = ctx.getStateTable().findFileState(oldName);
                    
                    if ( fstate != null)
                    {
                        // Update the file state index to use the new name
                        
                        ctx.getStateTable().renameFileState(newName, fstate);
                    }
                }
            }
            
            if (!relinked)
            {
                cifsHelper.move(nodeToMoveRef, targetFolderRef, name);
            }

            // DEBUG
            
            if (logger.isDebugEnabled())
                logger.debug("Moved node: " + " from: " + oldName + " to: " + newName);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Rename file - access denied, " + oldName);
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Rename file " + oldName);
        }
        catch (NodeLockedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Rename file", ex);
            
            // Convert to an filesystem access denied exception
            
            throw new AccessDeniedException("Node locked " + oldName);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Rename file", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Rename file " + oldName);
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
    public void setFileInformation(SrvSession sess, TreeConnection tree, String name, FileInfo info) throws IOException
    {
        try
        {
            // Get the file/folder node
            
            NodeRef nodeRef = getNodeForPath(tree, name);
            
            // Check permissions on the file/folder node
            
            if ( permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.DENIED)
                throw new AccessDeniedException("No write access to " + name);
            
            // Check if the file is being marked for deletion, if so then check if the file is locked
            
            if ( info.hasSetFlag(FileInfo.SetDeleteOnClose) && info.hasDeleteOnClose())
            {
                // Check if the node is locked
                
                if ( nodeService.hasAspect( nodeRef, ContentModel.ASPECT_LOCKABLE))
                {
                    // Get the lock type, if any
                    
                    String lockTypeStr = (String) nodeService.getProperty( nodeRef, ContentModel.PROP_LOCK_TYPE);
                    
                    if ( lockTypeStr != null)
                        throw new AccessDeniedException("Node locked, cannot mark for delete");
                }
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Set file information - access denied, " + name);
            
            // Convert to a filesystem access denied status
            
            throw new AccessDeniedException("Set file information " + name);
        }
        catch (AlfrescoRuntimeException ex)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Open file error", ex);
            
            // Convert to a general I/O exception
            
            throw new IOException("Set file information " + name);
        }
    }

    /**
     * Truncate a file to the specified size
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param file Network file details
     * @param siz New file length
     * @exception java.io.IOException The exception description.
     */
    public void truncateFile(SrvSession sess, TreeConnection tree, NetworkFile file, long size) throws IOException
    {
        // Truncate or extend the file to the required size
        
        file.truncateFile(size);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Truncated file: \n" +
                    "   network file: " + file + "\n" +
                    "   size: " + size);
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
        
        if(file.isDirectory())
            throw new AccessDeniedException();
            
        // Read a block of data from the file
        
        int count = file.readFile(buffer, size, bufferPosition, fileOffset);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Read bytes from file: \n" +
                    "   network file: " + file + "\n" +
                    "   buffer size: " + buffer.length + "\n" +
                    "   buffer pos: " + bufferPosition + "\n" +
                    "   size: " + size + "\n" +
                    "   file offset: " + fileOffset + "\n" +
                    "   bytes read: " + count);
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
        throw new UnsupportedOperationException("Unsupported: " + file + " (seek)");
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
        // Write to the file
        
        file.writeFile(buffer, size, bufferOffset, fileOffset);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Wrote bytes to file: \n" +
                    "   network file: " + file + "\n" +
                    "   buffer size: " + buffer.length + "\n" +
                    "   size: " + size + "\n" +
                    "   file offset: " + fileOffset);
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
    public NodeRef getNodeForPath(TreeConnection tree, String path)
        throws FileNotFoundException
    {
        // Check if there is a cached state for the path
        
        ContentContext ctx = (ContentContext) tree.getContext();
        
        if ( ctx.hasStateTable())
        {
            // Try and get the node ref from an in memory file state
            
            FileState fstate = ctx.getStateTable().findFileState(path);
            if ( fstate != null && fstate.hasNodeRef() && fstate.exists() )
            {
                // check that the node exists
                if (nodeService.exists(fstate.getNodeRef()))
                {
                    return fstate.getNodeRef();
                }
                else
                {
                    ctx.getStateTable().removeFileState(path);
                }
            }
        }
        
        // Search the repository for the node
        
        return cifsHelper.getNodeRef(ctx.getRootNode(), path);
    }
    
    /**
     * Get the file state for the specified path
     * 
     * @param tree TreeConnection
     * @param path String
     * @return FileState
     * @exception FileNotFoundException
     */
    public FileState getStateForPath(TreeConnection tree, String path)
        throws FileNotFoundException
    {
        // Check if there is a cached state for the path
        
        ContentContext ctx = (ContentContext) tree.getContext();
        FileState fstate = null;
        
        if ( ctx.hasStateTable())
        {
            // Get the file state for a file/folder
            
            fstate = ctx.getStateTable().findFileState(path);
        }
        
        // Return the file state
        
        return fstate;
    }
    
    /**
     * Connection opened to this disk device
     * 
     * @param sess Server session
     * @param tree Tree connection
     */
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        // Nothing to do
    }

    /**
     * Connection closed to this device
     * 
     * @param sess Server session
     * @param tree Tree connection
     */
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        // Nothing to do
    }
    
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
    public DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter)
        throws IOControlNotImplementedException, SMBException
    {
        // Validate the file id
        
        NetworkFile netFile = tree.findFile(fid);
        if ( netFile == null || netFile.isDirectory() == false)
            throw new SMBException(SMBStatus.NTErr, SMBStatus.NTInvalidParameter);
        
        // Check if the I/O control handler is enabled
        
        if ( m_ioHandler != null)
            return m_ioHandler.processIOControl( sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter);
        else
            throw new IOControlNotImplementedException();
    }
}
