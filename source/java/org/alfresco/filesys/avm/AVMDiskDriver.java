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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;

import javax.transaction.UserTransaction;

import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.DirectoryNotEmptyException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileExistsException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.PathNotFoundException;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFolderNetworkFile;
import org.alfresco.jlan.util.StringList;
import org.alfresco.jlan.util.WildCard;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.CreateStoreTxnListener;
import org.alfresco.repo.avm.CreateVersionTxnListener;
import org.alfresco.repo.avm.PurgeStoreTxnListener;
import org.alfresco.repo.avm.PurgeVersionTxnListener;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

/**
 * AVM Repository Filesystem Driver Class
 * <p>
 * Provides a filesystem interface for various protocols such as SMB/CIFS and FTP.
 * 
 * @author GKSpencer
 */
public class AVMDiskDriver extends AlfrescoDiskDriver implements DiskInterface
{
    // Logging

    private static final Log logger = LogFactory.getLog(AVMDiskDriver.class);

    // Configuration key names

    private static final String KEY_STORE = "storePath";
    private static final String KEY_VERSION = "version";
    private static final String KEY_CREATE = "createStore";

    // AVM path seperator

    public static final char AVM_SEPERATOR = '/';
    public static final String AVM_SEPERATOR_STR = "/";

    // Define client role names
    
    public static final String RoleContentManager = "ContentManager";
    public static final String RoleWebProject     = "WebProject";
    public static final String RoleNotWebAuthor   = "NotWebAuthor";

    // Content manager web project role
    
    private static final String ROLE_CONTENT_MANAGER = "ContentManager";
    
    // File status values used in the file state cache
    
    public static final int FileUnknown     = FileStatus.Unknown;
    public static final int FileNotExist    = FileStatus.NotExist;
    public static final int FileExists      = FileStatus.FileExists;
    public static final int DirectoryExists = FileStatus.DirectoryExists;
    
    public static final int CustomFileStatus= FileStatus.MaxStatus + 1;
    
    // Services and helpers

    private AVMService m_avmService;
    private MimetypeService m_mimetypeService;
    private AuthenticationComponent m_authComponent;
    private AuthenticationService m_authService;
    private NodeService m_nodeService;

    // AVM listeners

    private CreateStoreTxnListener m_createStoreListener;
    private PurgeStoreTxnListener m_purgeStoreListener;
    private CreateVersionTxnListener m_createVerListener;
    private PurgeVersionTxnListener m_purgeVerListener;

    // Web project store
    
    private String m_webProjectStore;
    
    /**
     * Default constructor
     */
    public AVMDiskDriver()
    {
    }

    /**
     * Return the AVM service
     * 
     * @return AVMService
     */
    public final AVMService getAvmService()
    {
        return m_avmService;
    }

    /**
     * Return the authentication service
     * 
     * @return AuthenticationService
     */
    public final AuthenticationService getAuthenticationService()
    {
        return m_authService;
    }

    /**
     * Set the AVM service
     * 
     * @param avmService
     *            AVMService
     */
    public void setAvmService(AVMService avmService)
    {
        m_avmService = avmService;
    }

    /**
     * Set the authentication component
     * 
     * @param authComponent
     *            AuthenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authComponent)
    {
        m_authComponent = authComponent;
    }

    /**
     * Set the authentication service
     * 
     * @param authService
     *            AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        m_authService = authService;
    }

    /**
     * Set the mimetype service
     * 
     * @param mimetypeService
     *            MimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        m_mimetypeService = mimetypeService;
    }

    /**
     * Set the node service
     * 
     * @param nodeService NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
    	m_nodeService = nodeService;
    }
    
    /**
     * Set the create store listener
     * 
     * @param createStoreListener
     *            CreateStoreTxnListener
     */
    public void setCreateStoreListener(CreateStoreTxnListener createStoreListener)
    {
        m_createStoreListener = createStoreListener;
    }

    /**
     * Set the purge store listener
     * 
     * @param purgeStoreListener
     *            PurgeStoreTxnListener
     */
    public void setPurgeStoreListener(PurgeStoreTxnListener purgeStoreListener)
    {
        m_purgeStoreListener = purgeStoreListener;
    }

    /**
     * Set the create version listener
     * 
     * @param createVersionListener
     *            CreateVersionTxnListener
     */
    public void setCreateVersionListener(CreateVersionTxnListener createVersionListener)
    {
        m_createVerListener = createVersionListener;
    }

    /**
     * Set the purge version listener
     * 
     * @param purgeVersionListener
     *            PurgeVersionTxnListener
     */
    public void setPurgeVersionListener(PurgeVersionTxnListener purgeVersionListener)
    {
        m_purgeVerListener = purgeVersionListener;
    }

    /**
     * Set the web project store
     * 
     * @param webStore String
     */
    public void setWebProjectStore(String webStore)
    {
    	m_webProjectStore = webStore;
    }
    
    /**
     * Parse and validate the parameter string and create a device context object for this instance of the shared
     * device.
     * 
     * @param shareName String
     * @param cfg ConfigElement
     * @return DeviceContext
     * @exception DeviceContextException
     */
    public DeviceContext createContext(String shareName, ConfigElement cfg)
            throws DeviceContextException
    {
        AVMContext context = null;

        try
        {
            // Check if the share is a virtualization view

            ConfigElement virtElem = cfg.getChild("virtualView");
            if (virtElem != null)
            {
                // Check if virtualization view show options have been specified
            	
            	int showOptions = AVMContext.ShowStagingStores + AVMContext.ShowAuthorStores;
            	
            	String showAttr = virtElem.getAttribute( "stores");
            	if ( showAttr != null)
            	{
            		// Split the show options string
            		
            		StringTokenizer tokens = new StringTokenizer( showAttr, ",");
            		StringList optList = new StringList();
            		
            		while ( tokens.hasMoreTokens())
            			optList.addString( tokens.nextToken().trim().toLowerCase());
            		
            		// Build the show options mask
            		
            		showOptions = 0;
            		
            		if ( optList.containsString("normal"))
            			showOptions += AVMContext.ShowNormalStores;
            		
                    if ( optList.containsString("site"))
                        showOptions += AVMContext.ShowSiteStores;
                    
            		if ( optList.containsString("author"))
            			showOptions += AVMContext.ShowAuthorStores;
            		
            		if ( optList.containsString("preview"))
            			showOptions += AVMContext.ShowPreviewStores;
            		
            		if ( optList.containsString("staging"))
            			showOptions += AVMContext.ShowStagingStores;
            	}
            	else if ( cfg.getChild("showAllSandboxes") != null)
            	{
            		// Old style show options
            		
            		showOptions = AVMContext.ShowNormalStores + AVMContext.ShowSiteStores +
                                  AVMContext.ShowAuthorStores + AVMContext.ShowPreviewStores +
                                  AVMContext.ShowStagingStores;
            	}

                // Create the context

                context = new AVMContext(shareName, showOptions, this);

                // Check if the admin user should be allowed to write to the web project staging stores
                
                if ( cfg.getChild("adminWriteable") != null)
                	context.setAllowAdminStagingWrites( true);
                
            }
            else
            {
                // Get the store path

                ConfigElement storeElement = cfg.getChild(KEY_STORE);
                if (storeElement == null
                        || storeElement.getValue() == null || storeElement.getValue().length() == 0)
                    throw new DeviceContextException("Device missing init value: " + KEY_STORE);

                String storePath = storeElement.getValue();

                // Get the version if specified, or default to the head version

                int version = AVMContext.VERSION_HEAD;

                ConfigElement versionElem = cfg.getChild(KEY_VERSION);
                if (versionElem != null)
                {
                    // Check if the version is valid

                    if (versionElem.getValue() == null || versionElem.getValue().length() == 0)
                        throw new DeviceContextException("Store version not specified");

                    // Validate the version id

                    try
                    {
                        version = Integer.parseInt(versionElem.getValue());
                    }
                    catch (NumberFormatException ex)
                    {
                        throw new DeviceContextException("Invalid store version specified, "
                                + versionElem.getValue());
                    }

                    // Range check the version id

                    if (version < 0 && version != AVMContext.VERSION_HEAD)
                        throw new DeviceContextException("Invalid store version id specified, " + version);
                }

                // Create the context

                context = new AVMContext(shareName, storePath, version);
                
                // Check if the create flag is enabled

                ConfigElement createStore = cfg.getChild(KEY_CREATE);
                context.setCreateStore(createStore != null);

                // Enable file state caching

                context.enableStateCache( true);
            }

        }
        catch (Exception ex)
        {
            logger.error("Error during create context", ex);

            // Rethrow the exception

            throw new DeviceContextException("Driver setup error, " + ex.getMessage());
        }
        
        // Register the context bean
        registerContext(context, null);
        
        // Return the context for this shared filesystem            
        return context;
    }

    /**
     * Register a device context object for this instance of the shared
     * device.
     * 
     * @param context the device context
     * @param serverConfig ServerConfigurationBean
     * @exception DeviceContextException
     */
    @Override
    public void registerContext(DeviceContext ctx, ServerConfigurationBean serverConfig)
            throws DeviceContextException
    {
        super.registerContext(ctx, serverConfig);        

        AVMContext context = (AVMContext)ctx;
        // Use the system user as the authenticated context for the filesystem initialization

        try
        {
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

            // Wrap the initialization in a transaction

            UserTransaction tx = getTransactionService().getUserTransaction(false);

            try
            {
                // Start the transaction

                if (tx != null)
                    tx.begin();

                // Check if the share is a virtualization view

                if (context.isVirtualizationView())
                {
                    // Enable file state caching

                    context.enableStateCache(true);

                    // Plug the virtualization view context into the various store/version call back listeners
                    // so that store/version pseudo folders can be kept in sync with AVM

                    m_createStoreListener.addCallback(context);
                    m_purgeStoreListener.addCallback(context);

                    m_createVerListener.addCallback(context);
                    m_purgeVerListener.addCallback(context);
                    
                    // Create the file state for the root path, this will build the store pseudo folder list
                    
                    findPseudoState( new AVMPath( ""), context);
                }
                else
                {
                    // Get the store path
                    String storePath = context.getStorePath();

                    // Get the version
                    int version = context.isVersion();

                    // Validate the store path

                    AVMNodeDescriptor rootNode = m_avmService.lookup(version, storePath);
                    if (rootNode == null)
                    {
                        // Check if the store should be created

                        if (!context.getCreateStore()|| version != AVMContext.VERSION_HEAD)
                            throw new DeviceContextException("Invalid store path/version, "
                                    + storePath + " (" + version + ")");

                        // Parse the store path

                        String storeName = null;
                        String path = null;

                        int pos = storePath.indexOf(":/");
                        if (pos != -1)
                        {
                            storeName = storePath.substring(0, pos);
                            if (storePath.length() > pos)
                                path = storePath.substring(pos + 2);
                        }
                        else
                            storeName = storePath;

                        // Check if the store exists

                        AVMStoreDescriptor storeDesc = null;

                        try
                        {
                            storeDesc = m_avmService.getStore(storeName);
                        }
                        catch (AVMNotFoundException ex)
                        {
                        }

                        // Create a new store if it does not exist

                        if (storeDesc == null)
                            m_avmService.createStore(storeName);

                        // Check if there is an optional path

                        if (path != null)
                        {
                            // Split the path

                            StringTokenizer tokens = new StringTokenizer(path, AVMPath.AVM_SEPERATOR_STR);
                            StringList paths = new StringList();

                            while (tokens.hasMoreTokens())
                                paths.addString(tokens.nextToken());

                            // Create the path, or folders that do not exist

                            AVMPath curPath = new AVMPath(storeName, version, FileName.DOS_SEPERATOR_STR);
                            AVMNodeDescriptor curDesc = m_avmService.lookup(curPath.getVersion(), curPath.getAVMPath());

                            // Walk the path checking creating each folder as required

                            for (int i = 0; i < paths.numberOfStrings(); i++)
                            {
                                AVMNodeDescriptor nextDesc = null;

                                try
                                {
                                    // Check if the child folder exists

                                    nextDesc = m_avmService.lookup(curDesc, paths.getStringAt(i));
                                }
                                catch (AVMNotFoundException ex)
                                {
                                }

                                // Check if the folder exists

                                if (nextDesc == null)
                                {
                                    // Create the new folder

                                    m_avmService.createDirectory(curPath.getAVMPath(), paths.getStringAt(i));

                                    // Get the details of the new folder

                                    nextDesc = m_avmService.lookup(curDesc, paths.getStringAt(i));
                                }
                                else if (nextDesc.isFile())
                                    throw new DeviceContextException("Path element error, not a folder, "
                                            + paths.getStringAt(i));

                                // Step to the next level

                                curPath.parsePath(storeName, version, curPath.getRelativePath()
                                        + paths.getStringAt(i) + FileName.DOS_SEPERATOR_STR);
                                curDesc = nextDesc;
                            }
                        }

                        // Validate the store path again

                        rootNode = m_avmService.lookup(version, storePath);
                        if (rootNode == null)
                            throw new DeviceContextException("Failed to create new store " + storePath);
                    }

                    // Enable file state caching

                    context.enableStateCache(true);
                }

                // Commit the transaction

                tx.commit();
                tx = null;
            }
            catch (Exception ex)
            {
                logger.error("Error during create context", ex);

                // Rethrow the exception

                throw new DeviceContextException("Driver setup error, " + ex.getMessage(), ex);
            }
            finally
            {
                // If there is an active transaction then roll it back

                if (tx != null)
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
            
            // Return the context for this shared filesystem
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Return a list of the available AVM store names
     * 
     * @return StringList
     */
    public final StringList getAVMStoreNames()
    {
        // Use the system user as the authenticated context to get the AVM store list

        String currentUser = m_authComponent.getCurrentUserName();
        try
        {
            m_authComponent.setCurrentUser(m_authComponent.getSystemUserName());

            // Wrap the service request in a transaction

            UserTransaction tx = getTransactionService().getUserTransaction(false);

            StringList storeNames = new StringList();

            try
            {
                // Start the transaction

                if (tx != null)
                    tx.begin();

                // Get the list of AVM stores

                List<AVMStoreDescriptor> storeList = m_avmService.getStores();

                if (storeList != null)
                {
                    for (AVMStoreDescriptor storeDesc : storeList)
                        storeNames.addString(storeDesc.getName());
                }

                // Commit the transaction

                tx.commit();
                tx = null;
            }
            catch (Exception ex)
            {
                logger.error("Error getting store names", ex);
            }
            finally
            {
                // If there is an active transaction then roll it back

                if (tx != null)
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

            // Return the list of AVM store names

            return storeNames;
        }
        finally
        {
            m_authComponent.setCurrentUser(currentUser);
        }
    }

    /**
     * Get the properties for a store
     * 
     * @param storeName
     *            String
     * @return Map<QName, PropertyValue>
     */
    protected final Map<QName, PropertyValue> getAVMStoreProperties(String storeName)
    {
        // Use the system user as the authenticated context to get the AVM store properties

        String currentUser = m_authComponent.getCurrentUserName();
        try
        {
            m_authComponent.setCurrentUser(m_authComponent.getSystemUserName());

            // Wrap the service request in a transaction

            UserTransaction tx = getTransactionService().getUserTransaction(false);

            Map<QName, PropertyValue> properties = null;

            try
            {
                // Start the transaction

                if (tx != null)
                    tx.begin();

                // Get the list of properties for AVM store

                properties = m_avmService.getStoreProperties(storeName);

                // Commit the transaction

                tx.commit();
                tx = null;
            }
            catch (Exception ex)
            {
                logger.error("Error getting store properties", ex);
            }
            finally
            {
                // If there is an active transaction then roll it back

                if (tx != null)
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

            // Return the list of AVM store properties

            return properties;
        }
        finally
        {
            m_authComponent.setCurrentUser(currentUser);
        }
    }

    /**
     * Build the full store path for a file/folder using the share relative path
     * 
     * @param ctx AVMContext
     * @param path String
     * @param sess SrvSession
     * @return AVMPath
     * @exception AccessDeniedException
     */
    protected final AVMPath buildStorePath(AVMContext ctx, String path, SrvSession sess)
    	throws AccessDeniedException
    {
        // Check if the AVM filesystem is a normal or virtualization view

        AVMPath avmPath = null;

        if (ctx.isVirtualizationView())
        {
            // Create a path for the virtualization view

            avmPath = new AVMPath(path);
            
            // Check that the user has access to the path
            
            checkPathAccess( avmPath, ctx, sess);
        }
        else
        {
            // Create a path to a single store/version

            avmPath = new AVMPath(ctx.getStorePath(), ctx.isVersion(), path);
        }

        // Return the path

        return avmPath;
    }

    /**
     * Close the file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection.
     * @param file
     *            Network file context.
     * @exception java.io.IOException
     *                If an error occurs.
     */
    public void closeFile(final SrvSession sess, final TreeConnection tree, final NetworkFile file) throws java.io.IOException
    {
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Close file " + file.getFullName());
        
        doInWriteTransaction(sess, new CallableIO<Void>(){

            public Void call() throws IOException
            {
                //  Close the file
                
                file.closeFile();

                // Check if the file/directory is marked for delete

                if (file.hasDeleteOnClose())
                {

                    // Check for a file or directory

                    if (file.isDirectory())
                        deleteDirectory(sess, tree, file.getFullName());
                    else
                        deleteFile(sess, tree, file.getFullName());
                }
                return null;
            }});        
    }

    /**
     * Create a new directory on this file system.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection.
     * @param params
     *            Directory create parameters
     * @exception java.io.IOException
     *                If an error occurs.
     */
    public void createDirectory(SrvSession sess, TreeConnection tree, FileOpenParams params) throws java.io.IOException
    {
        // Check if the filesystem is writable

        AVMContext ctx = (AVMContext) tree.getContext();
        if (ctx.isVersion() != AVMContext.VERSION_HEAD)
            throw new AccessDeniedException("Cannot create " + params.getPath() + ", filesys not writable");

        // Split the path to get the new folder name and relative path

        final String[] paths = FileName.splitPath(params.getPath());

        // Convert the relative path to a store path

        final AVMPath storePath = buildStorePath(ctx, paths[0], sess);

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Create directory params=" + params + ", storePath=" + storePath + ", name=" + paths[1]);

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView())
        {
        	if (storePath.isReadOnlyPseudoPath())
        		throw new AccessDeniedException("Cannot create folder in store/version layer, " + params.getPath());
        	else if ( storePath.isReadOnlyAccess())
        		throw new AccessDeniedException("Cannot create folder " + params.getPath() + ", read-only path");
        }

        // Create a new file

        try
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    // Create the new file entry

                    m_avmService.createDirectory(storePath.getAVMPath(), paths[1]);
                    
                    return null;
                }});
        }
        catch (AVMExistsException ex)
        {
            throw new FileExistsException(params.getPath());
        }
        catch (AVMNotFoundException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMWrongTypeException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMBadArgumentException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMLockingException ex)
        {
        	throw new AccessDeniedException(params.getPath());
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
        	throw new AccessDeniedException(params.getPath());
        }
    }

    /**
     * Create a new file on the file system.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param params
     *            File create parameters
     * @return NetworkFile
     * @exception java.io.IOException
     *                If an error occurs.
     */
    public NetworkFile createFile(final SrvSession sess, TreeConnection tree, final FileOpenParams params)
            throws java.io.IOException
    {
        // Check if the filesystem is writable

        final AVMContext ctx = (AVMContext) tree.getContext();

        // Split the path to get the file name and relative path

        final String[] paths = FileName.splitPath(params.getPath());

        // Convert the relative path to a store path

        final AVMPath storePath = buildStorePath(ctx, paths[0], sess);

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Create file params=" + params + ", storePath=" + storePath + ", name=" + paths[1]);

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView())
        {
        	if (storePath.isReadOnlyPseudoPath())
        		throw new AccessDeniedException("Cannot create file in store/version layer, " + params.getPath());
        	else if ( storePath.isReadOnlyAccess())
        		throw new AccessDeniedException("Cannot create file " + params.getPath() + ", read-only path");
        }
        else if (storePath.getVersion() != AVMContext.VERSION_HEAD)
        {
            throw new AccessDeniedException("Cannot create " + params.getPath() + ", filesys not writable");
        }


        try
        {
            // Create a new file
            return doInWriteTransaction(sess, new CallableIO<NetworkFile>(){

                public NetworkFile call() throws IOException
                {
                    // Create the new file entry

                    m_avmService.createFile(storePath.getAVMPath(), paths[1]).close();

                    // Get the new file details

                    AVMPath fileStorePath = buildStorePath(ctx, params.getPath(), sess);
                    AVMNodeDescriptor nodeDesc = m_avmService.lookup(fileStorePath.getVersion(), fileStorePath.getAVMPath());

                    if (nodeDesc != null)
                    {
                        // Create the network file object for the new file

                        AVMNetworkFile netFile = new AVMNetworkFile(nodeDesc, fileStorePath.getAVMPath(), fileStorePath.getVersion(),
                                m_nodeService, m_avmService);
                        netFile.setGrantedAccess(NetworkFile.READWRITE);
                        netFile.setFullName(params.getPath());

                        netFile.setFileId(fileStorePath.generateFileId());

                        // Set the mime-type for the new file

                        netFile.setMimeType(m_mimetypeService.guessMimetype(paths[1]));
                        return netFile;
                    }
                    return null;
                }});
        }
        catch (AVMExistsException ex)
        {
            throw new FileExistsException(params.getPath());
        }
        catch (AVMNotFoundException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMWrongTypeException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMBadArgumentException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMLockingException ex)
        {
        	throw new AccessDeniedException(params.getPath());
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
        	throw new AccessDeniedException(params.getPath());
        }
    }

    /**
     * Delete the directory from the filesystem.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param dir
     *            Directory name.
     * @exception java.io.IOException
     *                The exception description.
     */
    public void deleteDirectory(SrvSession sess, TreeConnection tree, final String dir) throws java.io.IOException
    {
        // Convert the relative path to a store path

        AVMContext ctx = (AVMContext) tree.getContext();

        final String[] paths = FileName.splitPath(dir);
        final AVMPath parentPath = buildStorePath(ctx, paths[0], sess);
        final AVMPath dirPath = buildStorePath(ctx, dir, sess);

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Delete directory, path=" + dir + ", dirPath=" + dirPath);

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView())
        {
        	if (parentPath.isPseudoPath())
        		throw new AccessDeniedException("Cannot delete folder in store/version layer, " + dir);
        	else if ( parentPath.isReadOnlyAccess())
        		throw new AccessDeniedException("Cannot delete folder " + dir + ", read-only path");
        }

        // Make sure the path is to a folder before deleting it

        try
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    AVMNodeDescriptor nodeDesc = m_avmService.lookup(dirPath.getVersion(), dirPath.getAVMPath());
                    if (nodeDesc != null)
                    {
                        // Check that we are deleting a folder

                        if (nodeDesc.isDirectory())
                        {
                            // Make sure the directory is empty

                            SortedMap<String, AVMNodeDescriptor> fileList = m_avmService.getDirectoryListing(nodeDesc);
                            if (fileList != null && fileList.size() > 0)
                                throw new DirectoryNotEmptyException(dir);

                            // Delete the folder

                            m_avmService.removeNode(dirPath.getAVMPath());
                        }
                        else
                            throw new IOException("Delete directory path is not a directory, " + dir);
                    }
                    return null;
                }});
        }
        catch (AVMNotFoundException ex)
        {
            throw new IOException("Directory not found, " + dir);
        }
        catch (AVMWrongTypeException ex)
        {
            throw new IOException("Invalid path, " + dir);
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
        	throw new AccessDeniedException("Access denied, " + dir);
        }
    }

    /**
     * Delete the specified file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param file
     *            NetworkFile
     * @exception java.io.IOException
     *                The exception description.
     */
    public void deleteFile(SrvSession sess, TreeConnection tree, final String name) throws java.io.IOException
    {
        // Convert the relative path to a store path

        AVMContext ctx = (AVMContext) tree.getContext();

        final String[] paths = FileName.splitPath(name);
        final AVMPath parentPath = buildStorePath(ctx, paths[0], sess);
        final AVMPath filePath = buildStorePath(ctx, name, sess);
        
        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Delete file, path=" + name + ", filePath=" + filePath);

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView())
        {
        	if (parentPath.isPseudoPath())
        		throw new AccessDeniedException("Cannot delete file in store/version layer, " + name);
        	else if ( parentPath.isReadOnlyAccess())
        		throw new AccessDeniedException("Cannot delete file " + name + ", read-only path");
        }

        // Make sure the path is to a file before deleting it

        try
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    AVMNodeDescriptor nodeDesc = m_avmService.lookup(filePath.getVersion(), filePath.getAVMPath());
                    if (nodeDesc != null)
                    {
                        // Check that we are deleting a file

                        if (nodeDesc.isFile())
                        {
                            // Delete the file

                            m_avmService.removeNode(filePath.getAVMPath());
                        }
                        else
                            throw new IOException("Delete file path is not a file, " + name);
                    }
                    return null;
                }});
        }
        catch (AVMNotFoundException ex)
        {
            throw new IOException("File not found, " + name);
        }
        catch (AVMWrongTypeException ex)
        {
            throw new IOException("Invalid path, " + name);
        }
        catch (AVMLockingException ex)
        {
        	throw new AccessDeniedException("File locked, " + name);
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
        	throw new AccessDeniedException("Access denied, " + name);
        }
    }

    /**
     * Check if the specified file exists, and whether it is a file or directory.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param name
     *            java.lang.String
     * @return int
     * @see FileStatus
     */
    public int fileExists(SrvSession sess, TreeConnection tree, String name)
    {
        // Convert the relative path to a store path

        AVMContext ctx = (AVMContext) tree.getContext();
        AVMPath storePath = null;
        
        try
        {
        	storePath = buildStorePath(ctx, name, sess);
        }
        catch ( AccessDeniedException ex)
        {
        	// DEBUG
        	
        	if ( logger.isDebugEnabled())
        		logger.debug("File exists check, path=" + name + " Access denied");
        	
        	return FileStatus.NotExist;
        }
        
        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("File exists check, path=" + name + ", storePath=" + storePath);

        // Check if the path is valid

        int status = FileStatus.NotExist;

        if (storePath.isValid() == false)
            return status;

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView() && storePath.isReadOnlyPseudoPath())
        {
            // Find the file state for the pseudo folder

            FileState fstate = findPseudoState(storePath, ctx);

            if (fstate != null)
            {
                // DEBUG

                if (logger.isDebugEnabled())
                    logger.debug("  Found pseudo file " + fstate);

                // Check if the pseudo file is a file or folder

                if (fstate.isDirectory())
                    status = FileStatus.DirectoryExists;
                else
                    status = FileStatus.FileExists;
            }
            else
            {
                // Invalid pseudo file path

                status = FileStatus.NotExist;
            }

            // Return the file status

            return status;
        }

        // Search for the file/folder

        beginReadTransaction( sess);

        AVMNodeDescriptor nodeDesc = m_avmService.lookup(storePath.getVersion(), storePath.getAVMPath());

        if (nodeDesc != null)
        {
            // Check if the path is to a file or folder

            if (nodeDesc.isDirectory())
                status = FileStatus.DirectoryExists;
            else
                status = FileStatus.FileExists;
        }

        // Return the file status

        return status;
    }

    /**
     * Flush any buffered output for the specified file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param file
     *            Network file context.
     * @exception java.io.IOException
     *                The exception description.
     */
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file) throws java.io.IOException
    {
        // Flush the file

        file.flushFile();
    }

    /**
     * Get the file information for the specified file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param name
     *            File name/path that information is required for.
     * @return File information if valid, else null
     * @exception java.io.IOException
     *                The exception description.
     */
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree, String name) throws java.io.IOException
    {
        // Convert the relative path to a store path
        
        AVMContext ctx = (AVMContext) tree.getContext();
        AVMPath storePath = null;
        
        try
        {
        	storePath = buildStorePath( ctx, name, sess);
        }
        catch ( Exception ex)
        {
        	throw new FileNotFoundException( name);
        }

        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Get file information, path=" + name + ", storePath=" + storePath);

        // Check if hte path is valid
        
        if ( storePath.isValid() == false)
            throw new FileNotFoundException( name);
        
        // Check if the filesystem is the virtualization view
        
        if ( ctx.isVirtualizationView() && storePath.isReadOnlyPseudoPath())
        {
            // Search for the pseudo path, to check for any new stores
            
            FileState fstate = findPseudoState( storePath, ctx);
            
            // Check if the search path is for the root, a store or version folder
            
            if ( storePath.isRootPath())
            {
                // Return dummy file informatiom for the root folder, use cached timestamps
                
                FileInfo finfo = new FileInfo( name, 0L, FileAttribute.Directory);
                
                if ( fstate != null) {
                    finfo.setModifyDateTime( fstate.getModifyDateTime());
                    finfo.setChangeDateTime( fstate.getModifyDateTime());
                }
                
                // Return the root folder file information
                
                return finfo;
            }
            else
            {
                // Find the pseudo file for the store/version folder
                
                PseudoFile psFile = findPseudoFolder( storePath, ctx);
                if ( psFile != null)
                {
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug( "  Found pseudo file " + psFile);
                    return psFile.getFileInfo();
                }
                else
                    throw new FileNotFoundException( name);
            }
        }
        
        // Search for the file/folder
        
        beginReadTransaction( sess);
        
        FileInfo info = null;
        
        try
        {
            AVMNodeDescriptor nodeDesc = m_avmService.lookup( storePath.getVersion(), storePath.getAVMPath());
            
            if ( nodeDesc != null)
            {
                // Create, and fill in, the file information
                
                info = new FileInfo();
                
                info.setFileName( nodeDesc.getName());
                
                if ( nodeDesc.isFile())
                {
                    info.setFileSize( nodeDesc.getLength());
                    info.setAllocationSize((nodeDesc.getLength() + 512L) & 0xFFFFFFFFFFFFFE00L);
                }
                else
                    info.setFileSize( 0L);
    
                info.setAccessDateTime( nodeDesc.getAccessDate());
                info.setCreationDateTime( nodeDesc.getCreateDate());
                info.setModifyDateTime( nodeDesc.getModDate());
                info.setChangeDateTime( nodeDesc.getModDate());
    
                // Build the file attributes
                
                int attr = 0;
                
                if ( nodeDesc.isDirectory())
                    attr += FileAttribute.Directory;
                
                if ( nodeDesc.getName().startsWith( ".") ||
                        nodeDesc.getName().equalsIgnoreCase( "Desktop.ini") ||
                        nodeDesc.getName().equalsIgnoreCase( "Thumbs.db"))
                    attr += FileAttribute.Hidden;
                
                // Mark the file/folder as read-only if not the head version
                
                if ( ctx.isVersion() != AVMContext.VERSION_HEAD || storePath.isReadOnlyAccess())
                    attr += FileAttribute.ReadOnly;
                
                if ( attr == 0)
                	attr = FileAttribute.NTNormal;
                
                info.setFileAttributes( attr);

                // Set the file id
                
                info.setFileId( storePath.generateFileId());
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("  File info=" + info);
            }
        }
        catch ( AVMNotFoundException ex)
        {
            throw new FileNotFoundException( name);
        }
        catch ( AVMWrongTypeException ex)
        {
            throw new PathNotFoundException( name);
        }
        
        // Return the file information
        
        return info;
    }

    /**
     * Determine if the disk device is read-only.
     * 
     * @param sess
     *            Server session
     * @param ctx
     *            Device context
     * @return boolean
     * @exception java.io.IOException
     *                If an error occurs.
     */
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx) throws java.io.IOException
    {
        // Check if the version indicates the head version, only the head is writable

        AVMContext avmCtx = (AVMContext) ctx;
        return avmCtx.isVersion() == AVMContext.VERSION_HEAD ? true : false;
    }

    /**
     * Open a file on the file system.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param params
     *            File open parameters
     * @return NetworkFile
     * @exception java.io.IOException
     *                If an error occurs.
     */
    public NetworkFile openFile(SrvSession sess, TreeConnection tree, FileOpenParams params) throws java.io.IOException
    {
        // Convert the relative path to a store path

        AVMContext ctx = (AVMContext) tree.getContext();
        AVMPath storePath = buildStorePath(ctx, params.getPath(), sess);

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Open file params=" + params + ", storePath=" + storePath);

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView() && storePath.isReadOnlyPseudoPath())
        {
            // Check if the path is for the root, a store or version folder

            if (storePath.isRootPath())
            {
                // Return a dummy file for the root folder

                return new PseudoFolderNetworkFile(FileName.DOS_SEPERATOR_STR);
            }
            else
            {
                // Find the pseudo file for the store/version folder

                PseudoFile psFile = findPseudoFolder(storePath, ctx);
                if (psFile != null)
                {
                    // DEBUG

                    if (logger.isDebugEnabled())
                        logger.debug("  Found pseudo file " + psFile);
                    return psFile.getFile(params.getPath());
                }
                else
                    return null;
            }
        }

        // Search for the file/folder

        beginReadTransaction( sess);

        AVMNetworkFile netFile = null;

        try
        {
            // Get the details of the file/folder

            AVMNodeDescriptor nodeDesc = m_avmService.lookup(storePath.getVersion(), storePath.getAVMPath());

            if (nodeDesc != null)
            {
                // Check if the filesystem is read-only and write access has been requested

                if (storePath.getVersion() != AVMContext.VERSION_HEAD
                        && (params.isReadWriteAccess() || params.isWriteOnlyAccess()))
                    throw new AccessDeniedException("File " + params.getPath() + " is read-only");

                // Create the network file object for the opened file/folder

                netFile = new AVMNetworkFile(nodeDesc, storePath.getAVMPath(), storePath.getVersion(), m_nodeService, m_avmService);

                if (params.isReadOnlyAccess() || storePath.getVersion() != AVMContext.VERSION_HEAD)
                    netFile.setGrantedAccess(NetworkFile.READONLY);
                else
                    netFile.setGrantedAccess(NetworkFile.READWRITE);

                netFile.setFullName(params.getPath());
                netFile.setFileId(storePath.generateFileId());

                // Set the mime-type for the new file

                netFile.setMimeType(m_mimetypeService.guessMimetype(params.getPath()));
            }
            else
                throw new FileNotFoundException(params.getPath());
        }
        catch (AVMNotFoundException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch (AVMWrongTypeException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
            throw new FileNotFoundException(params.getPath());
        }

        // Return the file

        return netFile;
    }

    /**
     * Read a block of data from the specified file.
     * 
     * @param sess
     *            Session details
     * @param tree
     *            Tree connection
     * @param file
     *            Network file
     * @param buf
     *            Buffer to return data to
     * @param bufPos
     *            Starting position in the return buffer
     * @param siz
     *            Maximum size of data to return
     * @param filePos
     *            File offset to read data
     * @return Number of bytes read
     * @exception java.io.IOException
     *                The exception description.
     */
    public int readFile(SrvSession sess, TreeConnection tree, NetworkFile file, byte[] buf, int bufPos, int siz,
            long filePos) throws java.io.IOException
    {
        // Check if the file is a directory

        if (file.isDirectory())
            throw new AccessDeniedException();

        // If the content channel is not open for the file then start a transaction

        AVMNetworkFile avmFile = (AVMNetworkFile) file;

        if (avmFile.hasContentChannel() == false)
            beginReadTransaction( sess);

        // Read the file

        int rdlen = file.readFile(buf, siz, bufPos, filePos);

        // If we have reached end of file return a zero length read

        if (rdlen == -1)
            rdlen = 0;

        // Return the actual read length

        return rdlen;
    }

    /**
     * Rename the specified file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param oldName
     *            java.lang.String
     * @param newName
     *            java.lang.String
     * @exception java.io.IOException
     *                The exception description.
     */
    public void renameFile(SrvSession sess, TreeConnection tree, String oldName, String newName)
            throws java.io.IOException
    {
        // Split the relative paths into parent and file/folder name pairs

        AVMContext ctx = (AVMContext) tree.getContext();

        final String[] oldPaths = FileName.splitPath(oldName);
        final String[] newPaths = FileName.splitPath(newName);

        // Convert the parent paths to store paths

        final AVMPath oldAVMPath = buildStorePath(ctx, oldPaths[0], sess);
        final AVMPath newAVMPath = buildStorePath(ctx, newPaths[0], sess);

        // DEBUG

        if (logger.isDebugEnabled())
        {
            logger.debug("Rename from path=" + oldPaths[0] + ", name=" + oldPaths[1]);
            logger.debug("        new path=" + newPaths[0] + ", name=" + newPaths[1]);
        }

        // Check if the filesystem is the virtualization view

        if (ctx.isVirtualizationView())
        {
        	if ( oldAVMPath.isReadOnlyPseudoPath())
        		throw new AccessDeniedException("Cannot rename folder in store/version layer, " + oldName);
        	else if ( newAVMPath.isReadOnlyPseudoPath())
        		throw new AccessDeniedException("Cannot rename folder to store/version layer, " + newName);
            else if ( oldAVMPath.isReadOnlyAccess() )
                throw new AccessDeniedException("Cannot rename read-only folder, " + oldName);
        	else if ( newAVMPath.isReadOnlyAccess() )
        		throw new AccessDeniedException("Cannot rename folder to read-only folder, " + newName);
        }

        try
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    // Rename the file/folder

                    m_avmService.rename(oldAVMPath.getAVMPath(), oldPaths[1], newAVMPath.getAVMPath(), newPaths[1]);
                    return null;
                }});
        }
        catch (AVMNotFoundException ex)
        {
            throw new IOException("Source not found, " + oldName);
        }
        catch (AVMWrongTypeException ex)
        {
            throw new IOException("Invalid path, " + oldName);
        }
        catch (AVMExistsException ex)
        {
            throw new FileExistsException("Destination exists, " + newName);
        }
        catch ( org.alfresco.repo.security.permissions.AccessDeniedException ex)
        {
        	throw new AccessDeniedException("Access denied, " + oldName);
        }
    }

    /**
     * Seek to the specified file position.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param file
     *            Network file.
     * @param pos
     *            Position to seek to.
     * @param typ
     *            Seek type.
     * @return New file position, relative to the start of file.
     */
    public long seekFile(SrvSession sess, TreeConnection tree, NetworkFile file, long pos, int typ)
            throws java.io.IOException
    {
        // Check if the file is a directory

        if (file.isDirectory())
            throw new AccessDeniedException();

        // If the content channel is not open for the file then start a transaction

        AVMNetworkFile avmFile = (AVMNetworkFile) file;

        if (avmFile.hasContentChannel() == false)
            beginReadTransaction( sess);

        // Set the file position

        return file.seekFile(pos, typ);
    }

    /**
     * Set the file information for the specified file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param name
     *            java.lang.String
     * @param info
     *            FileInfo
     * @exception java.io.IOException
     *                The exception description.
     */
    public void setFileInformation(SrvSession sess, TreeConnection tree, String name, FileInfo info)
            throws java.io.IOException
    {
        // Check if the file is being marked for deletion, check if the file is writable

        if (info.hasSetFlag(FileInfo.SetDeleteOnClose) && info.hasDeleteOnClose())
        {
            // If this is not the head version then it's not writable

            AVMContext avmCtx = (AVMContext) tree.getContext();

            // Parse the path
        	
            AVMPath storePath = buildStorePath(avmCtx, name, sess);
        	
            if (avmCtx.isVersion() != AVMContext.VERSION_HEAD || storePath.isReadOnlyAccess())
                throw new AccessDeniedException("Store not writable, cannot set delete on close");
        }
    }

    /**
     * Start a new search on the filesystem using the specified searchPath that may contain wildcards.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param searchPath
     *            File(s) to search for, may include wildcards.
     * @param attrib
     *            Attributes of the file(s) to search for, see class SMBFileAttribute.
     * @return SearchContext
     * @exception java.io.FileNotFoundException
     *                If the search could not be started.
     */
    public SearchContext startSearch(SrvSession sess, TreeConnection tree, String searchPath, int attrib)
            throws java.io.FileNotFoundException
    {
        // Access the AVM context

        AVMContext avmCtx = (AVMContext) tree.getContext();

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("Start search path=" + searchPath);

        // Split the search path into relative path and search name

        String[] paths = FileName.splitPath(searchPath);

        // Build the store path to the folder being searched

        AVMPath storePath = null;
        
        try
        {
        	storePath = buildStorePath(avmCtx, paths[0], sess);
        }
        catch ( AccessDeniedException ex)
        {
        	// DEBUG
        	
        	if ( logger.isDebugEnabled())
        		logger.debug("Start search access denied");
        	
        	throw new FileNotFoundException("Access denied");
        }

        // Check if the filesystem is the virtualization view

        if (avmCtx.isVirtualizationView())
        {
            // Check for a search of a pseudo folder

            if (storePath.isReadOnlyPseudoPath())
            {
                // Get the file state for the folder being searched

                FileState fstate = findPseudoState(storePath, avmCtx);

                if (fstate != null)
                {
                    // Get the pseudo file list for the parent directory

                	PseudoFileList searchList = null;
                	
                	if ( storePath.isLevel() == AVMPath.LevelId.Root)
                		searchList = filterPseudoFolders(avmCtx, sess, storePath, fstate);
                	else
                		searchList = fstate.getPseudoFileList();

                    // Check if the pseudo file list is valid

                    if (searchList == null)
                        searchList = new PseudoFileList();

                    // Check if this is a single file or wildcard search

                    if (WildCard.containsWildcards(searchPath))
                    {
                        // Create the search context, wildcard filter will take care of secondary filtering of the
                        // folder listing

                        WildCard wildCardFilter = new WildCard(paths[1], false);
                        return new PseudoFileListSearchContext(searchList, attrib, wildCardFilter, storePath.isReadOnlyAccess());
                    }
                    else
                    {
                        // Search the pseudo file list for the required file

                        PseudoFile pseudoFile = searchList.findFile(paths[1], false);
                        if (pseudoFile != null)
                        {
                            // Create a search context using the single file details

                            PseudoFileList singleList = new PseudoFileList();
                            singleList.addFile(pseudoFile);

                            return new PseudoFileListSearchContext(singleList, attrib, null, storePath.isReadOnlyAccess());
                        }
                    }
                }

                // File not found

                throw new FileNotFoundException(searchPath);
            }
            else if (storePath.isLevel() == AVMPath.LevelId.HeadMetaData
                    || storePath.isLevel() == AVMPath.LevelId.VersionMetaData)
            {
                // Return an empty file list for now

                PseudoFileList metaFiles = new PseudoFileList();

                return new PseudoFileListSearchContext(metaFiles, attrib, null, storePath.isReadOnlyAccess());
            }
        }

        // Check if the path is a wildcard search

        beginReadTransaction( sess);
        SearchContext context = null;

        if (WildCard.containsWildcards(searchPath))
        {
            // Get the file listing for the folder

            AVMNodeDescriptor[] fileList = m_avmService.getDirectoryListingArray(storePath.getVersion(), storePath.getAVMPath(), false);

            // Create the search context

            if (fileList != null)
            {

                // DEBUG

                if (logger.isDebugEnabled())
                    logger.debug("  Wildcard search returned " + fileList.length + " files");

                // Create the search context, wildcard filter will take care of secondary filtering of the
                // folder listing

                WildCard wildCardFilter = new WildCard(paths[1], false);
                context = new AVMSearchContext(fileList, attrib, wildCardFilter, storePath.getRelativePath(), storePath.isReadOnlyAccess());
            }
        }
        else
        {
            // Single file/folder search, convert the path to a store path

        	try
        	{
        		storePath = buildStorePath(avmCtx, searchPath, sess);
        	}
        	catch ( AccessDeniedException ex)
        	{
            	// DEBUG
            	
            	if ( logger.isDebugEnabled())
            		logger.debug("Start search access denied");
            	
            	throw new FileNotFoundException("Access denied");
        	}

            // Get the single file/folder details

            AVMNodeDescriptor nodeDesc = m_avmService.lookup(storePath.getVersion(), storePath.getAVMPath());

            if (nodeDesc != null)
            {
                // Create the search context for the single file/folder

                context = new AVMSingleFileSearchContext(nodeDesc, storePath.getRelativePath(), storePath.isReadOnlyAccess());
            }

        }

        // Return the search context

        return context;
    }

    /**
     * Truncate a file to the specified size
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param file
     *            Network file details
     * @param siz
     *            New file length
     * @exception java.io.IOException
     *                The exception description.
     */
    public void truncateFile(SrvSession sess, TreeConnection tree, final NetworkFile file, final long siz)
            throws java.io.IOException
    {
        // Check if the file is a directory, or only has read access

        if (file.getGrantedAccess() == NetworkFile.READONLY)
            throw new AccessDeniedException();

        // If the content channel is not open for the file then start a transaction

        AVMNetworkFile avmFile = (AVMNetworkFile) file;

        // Truncate or extend the file
        if (avmFile.hasContentChannel() == false || avmFile.isWritable() == false)
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    file.truncateFile(siz);
                    file.flushFile();
                    return null;
                }});
        }
        else
        {
            file.truncateFile(siz);
            file.flushFile();
        }


    }

    /**
     * Write a block of data to the file.
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     * @param file
     *            Network file details
     * @param buf
     *            byte[] Data to be written
     * @param bufoff
     *            Offset within the buffer that the data starts
     * @param siz
     *            int Data length
     * @param fileoff
     *            Position within the file that the data is to be written.
     * @return Number of bytes actually written
     * @exception java.io.IOException
     *                The exception description.
     */
    public int writeFile(SrvSession sess, TreeConnection tree, final NetworkFile file, final byte[] buf, final int bufoff, final int siz,
            final long fileoff) throws java.io.IOException
    {
        // Check if the file is a directory, or only has read access

        if (file.isDirectory() || file.getGrantedAccess() == NetworkFile.READONLY)
            throw new AccessDeniedException();

        // If the content channel is not open for the file, or the channel is not writable, then start a transaction

        AVMNetworkFile avmFile = (AVMNetworkFile) file;

        // Write the data to the file
        if (avmFile.hasContentChannel() == false || avmFile.isWritable() == false)
        {
            doInWriteTransaction(sess, new CallableIO<Void>(){

                public Void call() throws IOException
                {
                    file.writeFile(buf, siz, bufoff, fileoff);            
                    return null;
                }});
        }
        else
        {
            file.writeFile(buf, siz, bufoff, fileoff);            
        }


        // Return the actual write length

        return siz;
    }

    /**
     * Connection opened to this disk device
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     */
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        // Nothing to do
    }

    /**
     * Connection closed to this device
     * 
     * @param sess
     *            Server session
     * @param tree
     *            Tree connection
     */
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        // Nothing to do
    }

    /**
     * Find the pseudo file for a virtual path
     * 
     * @param avmPath
     *            AVMPath
     * @param avmCtx
     *            AVMContext
     * @return PseudoFile
     */
    private final PseudoFile findPseudoFolder(AVMPath avmPath, AVMContext avmCtx)
    {
        return findPseudoFolder(avmPath, avmCtx, true);
    }

    /**
     * Find the pseudo file for a virtual path
     * 
     * @param avmPath
     *            AVMPath
     * @param avmCtx
     *            AVMContext
     * @param generateStates
     *            boolean
     * @return PseudoFile
     */
    private final PseudoFile findPseudoFolder(AVMPath avmPath, AVMContext avmCtx, boolean generateStates)
    {
        // Check if the path is to a store pseudo folder

        if (avmPath.isRootPath())
            return null;

        // Get the file state for the parent of the required folder

        FileState fstate = null;
        StringBuilder str = null;
        PseudoFile psFile = null;

        switch (avmPath.isLevel())
        {
        // Store root folder

        case StoreRoot:

            // Get the root folder file state

            fstate = avmCtx.getStateCache().findFileState(FileName.DOS_SEPERATOR_STR);

            if (fstate != null && fstate.hasPseudoFiles())
                psFile = fstate.getPseudoFileList().findFile(avmPath.getStoreName(), false);
            break;

        // Versions root or Head folder

        case VersionRoot:
        case Head:

            // Create a path to the parent store

            str = new StringBuilder();

            str.append(FileName.DOS_SEPERATOR);
            str.append(avmPath.getStoreName());

            // Find/create the file state for the store

            AVMPath storePath = new AVMPath(str.toString());
            fstate = findPseudoState(storePath, avmCtx);

            // Find the version root or head pseudo folder

            if (fstate != null)
            {
                if (avmPath.isLevel() == AVMPath.LevelId.Head)
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.VersionNameHead, true);
                else
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.VersionsFolder, true);
            }
            break;

        // Version folder

        case Version:

            // Create a path to the versions folder

            str = new StringBuilder();

            str.append(FileName.DOS_SEPERATOR);
            str.append(avmPath.getStoreName());
            str.append(FileName.DOS_SEPERATOR);
            str.append(AVMPath.VersionsFolder);

            // Find/create the file state for the store

            AVMPath verrootPath = new AVMPath(str.toString());
            fstate = findPseudoState(verrootPath, avmCtx);

            // Find the version pseudo file

            if (fstate != null)
            {
                // Build the version folder name string

                str.setLength(0);

                str.append(AVMPath.VersionFolderPrefix);
                str.append(avmPath.getVersion());

                // find the version folder pseduo file

                psFile = fstate.getPseudoFileList().findFile(str.toString(), true);
            }
            break;

        // Head data or metadata folder

        case HeadData:
        case HeadMetaData:

            // Create a path to the head folder

            str = new StringBuilder();

            str.append(FileName.DOS_SEPERATOR);
            str.append(avmPath.getStoreName());
            str.append(FileName.DOS_SEPERATOR);
            str.append(AVMPath.VersionNameHead);

            // Find/create the file state for the store

            AVMPath headPath = new AVMPath(str.toString());
            fstate = findPseudoState(headPath, avmCtx);

            // Find the data or metadata pseudo folder

            if (fstate != null)
            {
                // Find the pseudo folder

                if (avmPath.isLevel() == AVMPath.LevelId.HeadData)
                {
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.DataFolder, true);
                }
                else
                {
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.MetaDataFolder, true);
                }
            }
            break;

        // Version data or metadata folder

        case VersionData:
        case VersionMetaData:

            // Create a path to the version folder

            str = new StringBuilder();

            str.append(FileName.DOS_SEPERATOR);
            str.append(avmPath.getStoreName());
            str.append(FileName.DOS_SEPERATOR);
            str.append(AVMPath.VersionFolderPrefix);
            str.append(avmPath.getVersion());

            // Find/create the file state for the store

            AVMPath verPath = new AVMPath(str.toString());
            fstate = findPseudoState(verPath, avmCtx);

            // Find the data or metadata pseudo folder

            if (fstate != null)
            {
                // Find the pseudo folder

                if (avmPath.isLevel() == AVMPath.LevelId.VersionData)
                {
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.DataFolder, true);
                }
                else
                {
                    psFile = fstate.getPseudoFileList().findFile(AVMPath.MetaDataFolder, true);
                }
            }
            break;
        }

        // Check if the pseudo file was not found but file states should be generated

        if (psFile == null && generateStates == true)
        {
            // Generate the file states for the path, this is required if a request is made to a path without
            // walking the folder tree

            generatePseudoFolders(avmPath, avmCtx);

            // Try and find the pseudo file again

            psFile = findPseudoFolder(avmPath, avmCtx, false);
        }

        // Return the pseudo file, or null if not found

        return psFile;
    }

    /**
     * Find the file state for a pseudo folder path
     * 
     * @param avmPath
     *            AVMPath
     * @param avmCtx
     *            AVMContext
     * @return FileState
     */
    protected final FileState findPseudoState(AVMPath avmPath, AVMContext avmCtx)
    {
        // Make sure the is to a pseudo file/folder
        
        if ( avmPath.isPseudoPath() == false)
            return null;
        
        // Check if there are any new stores to be added to the virtualization view
        
        if ( avmCtx.hasNewStoresQueued()) {
        	
        	// Get the new stores list, there is a chance another thread might get the queue, if the queue is empty
        	// another thread is processing it
        	
        	StringList storeNames = avmCtx.getNewStoresQueue();
        	
        	while ( storeNames.numberOfStrings() > 0) {

        		// Get the current store name
        		
        		String curStoreName = storeNames.removeStringAt( 0);

        		// DEBUG
        		
        		if ( logger.isDebugEnabled())
        			logger.debug("Adding new store " + curStoreName);
        		
        		// Add the current store to the virtualization view
        		
        		addNewStore( avmCtx, curStoreName);
        	}

        	// Get the root folder file state, update the modification timestamp
        	
            FileState rootState = avmCtx.getStateCache().findFileState( FileName.DOS_SEPERATOR_STR);
            if  ( rootState != null)
                rootState.updateModifyDateTime();
        }
        
        // Check if the path is to a store pseudo folder
        
        FileState fstate = null;
        StringBuilder str = null;
        String relPath = null;
        
        switch ( avmPath.isLevel())
        {
            // Root of the hieararchy
            
            case Root:

                // Get the root path file state
                
                fstate = avmCtx.getStateCache().findFileState( FileName.DOS_SEPERATOR_STR);
                
                // Check if the root file state is valid
                
                if ( fstate == null)
                {
                    // Create a file state for the root folder
                    
                    fstate = avmCtx.getStateCache().findFileState( FileName.DOS_SEPERATOR_STR, true);
                    fstate.setExpiryTime( FileState.NoTimeout);
                    fstate.setFileStatus( DirectoryExists);
                    
                    // Set the modification timestamp for the root folder
                    
                    fstate.updateModifyDateTime();
                    
                    // Get a list of the available AVM stores
                    
                    List<AVMStoreDescriptor> storeList = m_avmService.getStores();
                    
                    if ( storeList != null && storeList.size() > 0)
                    {
                        // Add pseudo files for the stores
                        
                        for ( AVMStoreDescriptor storeDesc : storeList)
                        {
                            // Get the properties for the current store

                        	String storeName = storeDesc.getName();
                            Map<QName, PropertyValue> props = m_avmService.getStoreProperties( storeName);
                            
                            // Check if the store is a main web project
                            
                            if ( props.containsKey( SandboxConstants.PROP_SANDBOX_STAGING_MAIN))
                            {
                            	// Get the noderef for the web project
                            	
                            	PropertyValue prop = props.get( SandboxConstants.PROP_WEB_PROJECT_NODE_REF);
                            	if ( prop != null) {
                            		
                            		// Get the web project noderef
                            		
                            		NodeRef webNodeRef = new NodeRef( prop.getStringValue());
                            		
                            		if (m_nodeService.exists(webNodeRef))
                            		{
                                		// Create the web project pseudo folder
                                		
                                		WebProjectStorePseudoFile webProjFolder = new WebProjectStorePseudoFile( storeDesc, FileName.DOS_SEPERATOR_STR + storeName, webNodeRef);
                                		fstate.addPseudoFile( webProjFolder);
    
                                		// DEBUG
                                		
                                		if ( logger.isDebugEnabled())
                                			logger.debug( "Found web project " + webProjFolder.getFileName());
                                		
                                		// Get the list of content managers for this web project
    
                                		List<ChildAssociationRef> mgrAssocs = m_nodeService.getChildAssocs( webNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
                                		
                                		for ( ChildAssociationRef mgrRef : mgrAssocs)
                                		{
                                			// Get the child node and see if it is a content manager association
                                			
                                			NodeRef childRef = mgrRef.getChildRef();
                                			
                                			if ( m_nodeService.getProperty( childRef, WCMAppModel.PROP_WEBUSERROLE).equals(ROLE_CONTENT_MANAGER))
                                			{
                                				// Get the user name add it to the web project pseudo folder
                                				
                                				String userName = (String) m_nodeService.getProperty( childRef, WCMAppModel.PROP_WEBUSERNAME);
                                				
                                				webProjFolder.addUserRole( userName, WebProjectStorePseudoFile.RoleContentManager);
                                				
                                				// DEBUG
                                				
                                				if ( logger.isDebugEnabled())
                                					logger.debug("  Added content manager " + userName);
                                			}
                                		}
                                    }
                                    else
                                    {
                                        logger.warn("AVM Store '"+storeName+"' with webProjectNodeRef that does not exist: "+webNodeRef);
                                    }
                            	}
                            }
                            else
                            {
                            	// Check if this store is a web project sandbox
                            	
                            	int storeType = StoreType.Normal;
                            	String webProjName = null;
                            	String userName    = null;
                            	
	                            if ( props.containsKey( SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
	                            {
	                            	// Sandbox store, linked to a web project
	                            	
	                                storeType = StoreType.WebAuthorMain;
	                                
	                                // Get the associated web project name

	                                webProjName = props.get( SandboxConstants.PROP_WEBSITE_NAME).getStringValue();

	                                // Get the user name from the store name

	                                userName = storeName.substring( webProjName.length() + 2);
	                            }
	                            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW))
	                            {
	                            	// Author preview sandbox store, linked to a web project
	                            	
	                                storeType = StoreType.WebAuthorPreview;
	                                
	                                // Get the associated web project name

	                                String projPlusUser = storeName.substring( 0, storeName.length() - "--preview".length());
	                                int pos = projPlusUser.lastIndexOf("--");
	                                if ( pos != -1)
	                                {
	                                	webProjName = projPlusUser.substring( 0, pos);
	                                	userName    = projPlusUser.substring(pos + 2);
	                                }
	                            }
	                            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW))
	                            {
	                            	// Staging preview sandbox store, linked to a web project
	                            	
	                                storeType = StoreType.WebStagingPreview;
	                            }
	                            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW))
	                            {
	                            	// Staging preview sandbox store, linked to a web project
	                            	
	                                storeType = StoreType.WebStagingPreview;
	                                
	                                // Get the associated web project name

	                                webProjName = storeName.substring( 0, storeName.length() - "--preview".length());
	                            }
                                else if ( props.containsKey(QName.createQName(null, ".sitestore")))
                                {
                                    // Site data store type
                                    
                                    storeType = StoreType.SiteStore;
                                }
	                            
	                            // DEBUG
	                            
	                            if ( logger.isDebugEnabled())
	                                logger.debug( "Store " + storeDesc.getName() + ", type=" + StoreType.asString( storeType) + ", webproj=" + webProjName + ", username=" + userName);
	                            
	                            // Add a pseudo file for the current store
	
	                            if ( avmCtx.showStoreType( storeType))
	                            {
	                            	// Create the pseudo folder for the store
	                            	
	                            	StorePseudoFile storeFolder = new StorePseudoFile( storeDesc, FileName.DOS_SEPERATOR_STR + storeName, storeType);
	                            	if (storeType == StoreType.WebAuthorMain || storeType == StoreType.WebAuthorPreview ||
                                        storeType == StoreType.WebStagingMain || storeType == StoreType.WebStagingPreview)
	                            	{
	                            		storeFolder.setWebProject( webProjName);
	                            		storeFolder.setUserName( userName);
	                            	}
	                            	
	                            	// Add the store pseudo folder to the root folder file list
	                            	
	                                fstate.addPseudoFile( storeFolder);
	                            }
                            }
                        }
                    }
                    
                    // Scan the pseudo folder list and add all publisher/reviewer user names to the web project roles list
                    
                    PseudoFileList folderList = fstate.getPseudoFileList();
                    if ( folderList != null && folderList.numberOfFiles() > 0)
                    {
                    	// Scan the pseudo folder list
                    	
                    	for ( int i = 0; i < folderList.numberOfFiles(); i++)
                    	{
                    		// Check if the current pseduo file is a store folder
                    		
                    		if ( folderList.getFileAt( i) instanceof StorePseudoFile)
                    		{
                    			// Check if the store has an associated web project
                    			
                    			StorePseudoFile curFile = (StorePseudoFile) folderList.getFileAt( i);
                    			if ( curFile.hasWebProject())
                    			{
                    				// Find the associated web project pseudo folder
                    				
                    				WebProjectStorePseudoFile webProj = (WebProjectStorePseudoFile) folderList.findFile( curFile.getWebProject(), true);
                    				
                    				if (webProj == null)
                    				{
                    				    logger.warn("Missing web project for: "+curFile.getFileName()+" ("+curFile.getWebProject()+")");
                    				}
                    				else
                    				{
                        				// Strip the web project name from the sandbox store name and extract the user name.
                        				// Add the user as a publisher/reviewer to the web project roles list
                        				
                        				String userName = curFile.getFileName().substring( webProj.getFileName().length() + 2);
                        				
                        				// If the user does not have a content manager role then add as a publisher
                        				
                        				if ( webProj.getUserRole( userName) == WebProjectStorePseudoFile.RoleNone)
                        				{
                        					webProj.addUserRole( userName, WebProjectStorePseudoFile.RolePublisher);
                        				
    	                    				// DEBUG
    	                    				
    	                    				if ( logger.isDebugEnabled())
    	                    					logger.debug( "Added publisher " + userName + " to " + webProj.getFileName());
                        				}
                    				}
                    			}
                    		}
                    	}
                    }
                }
                break;
                
            // Store folder
                
            case StoreRoot:
                
                // Build the path to the parent store folder
                
                str = new StringBuilder();
                
                str.append( FileName.DOS_SEPERATOR);
                str.append( avmPath.getStoreName());
                
                // Search for the file state for the store pseudo folder
                
                relPath = str.toString();
                fstate = avmCtx.getStateCache().findFileState( relPath);
                
                if ( fstate == null)
                {
                    // Create a file state for the store path
                    
                    fstate = avmCtx.getStateCache().findFileState( str.toString(), true);
                    fstate.setFileStatus( DirectoryExists);
                    
                    // Add a pseudo file for the head version
                    
                    str.append( FileName.DOS_SEPERATOR);
                    str.append( AVMPath.VersionNameHead);
                    
                    fstate.addPseudoFile( new VersionPseudoFile( AVMPath.VersionNameHead, str.toString()));
                    
                    // Add a pseudo file for the version root folder

                    str.setLength( relPath.length() + 1);
                    str.append( AVMPath.VersionsFolder);
                    
                    fstate.addPseudoFile( new DummyFolderPseudoFile( AVMPath.VersionsFolder, str.toString()));
                }
                break;

            // Head folder
                
            case Head:

                // Build the path to the store head version folder
                
                str = new StringBuilder();
                
                str.append( FileName.DOS_SEPERATOR);
                str.append( avmPath.getStoreName());
                str.append( FileName.DOS_SEPERATOR);
                str.append( AVMPath.VersionNameHead);
                
                // Search for the file state for the store head version pseudo folder
                
                relPath = str.toString();
                
                fstate = avmCtx.getStateCache().findFileState( relPath);
                
                if ( fstate == null)
                {
                    // Create a file state for the store head folder path
                    
                    fstate = avmCtx.getStateCache().findFileState( str.toString(), true);
                    fstate.setFileStatus( DirectoryExists);
                    
                    // Add a pseudo file for the data pseudo folder
                    
                    str.append( FileName.DOS_SEPERATOR);
                    str.append( AVMPath.DataFolder);
                    
                    fstate.addPseudoFile( new DummyFolderPseudoFile( AVMPath.DataFolder, str.toString()));
                    
                    // Add a pseudo file for the metadata pseudo folder
                    
                    str.setLength( relPath.length() + 1);
                    str.append( AVMPath.MetaDataFolder);
                    
                    fstate.addPseudoFile( new DummyFolderPseudoFile( AVMPath.MetaDataFolder, str.toString()));
                }
                break;
                
            // Version root folder
                
            case VersionRoot:

                // Get the list of AVM store versions

                try
                {
                    // Build the path to the parent store folder
                    
                    str = new StringBuilder();
                    
                    str.append( FileName.DOS_SEPERATOR);
                    str.append( avmPath.getStoreName());
                    str.append( FileName.DOS_SEPERATOR);
                    str.append( AVMPath.VersionsFolder);
                    
                    // Create a file state for the store path
                    
                    relPath = str.toString();
                    fstate = avmCtx.getStateCache().findFileState( relPath, true);
                    fstate.setFileStatus( DirectoryExists);
                    
                    // Add pseudo folders if the list is empty
                    
                    if ( fstate.hasPseudoFiles() == false)
                    {
                        // Build the version folder name for the head version
                        
                        StringBuilder verStr = new StringBuilder( AVMPath.VersionFolderPrefix);
                        verStr.append( "-1");
                        
                        // Add a pseudo file for the head version

                        str.append( FileName.DOS_SEPERATOR);
                        str.append( verStr.toString());
                        
                        fstate.addPseudoFile( new VersionPseudoFile( verStr.toString(), str.toString()));
                        
                        // Get the list of versions for the store
                        
                        List<VersionDescriptor> verList = m_avmService.getStoreVersions( avmPath.getStoreName());
                        
                        // Add pseudo files for the versions to the store state
    
                        if ( verList.size() > 0)
                        {
                            for ( VersionDescriptor verDesc : verList)
                            {
                                // Generate the version string
                                
                                String verName = null;
                                
                                verStr.setLength( AVMPath.VersionFolderPrefix.length());
                                verStr.append( verDesc.getVersionID());
                                
                                verName = verStr.toString();

                                str.setLength( relPath.length() + 1);
                                str.append( verName);
                                
                                // Add the version pseudo folder
                                
                                fstate.addPseudoFile( new VersionPseudoFile ( verName, verDesc, str.toString()));
                            }
                        }
                    }
                }
                catch ( AVMNotFoundException ex)
                {
                    // Invalid store name
                }
                break;

            // Version folder
                
            case Version:

                // Build the path to the store version folder
                
                str = new StringBuilder();
                
                str.append( FileName.DOS_SEPERATOR);
                str.append( avmPath.getStoreName());
                str.append( FileName.DOS_SEPERATOR);
                str.append( AVMPath.VersionFolderPrefix);
                str.append( avmPath.getVersion());
                
                // Search for the file state for the version pseudo folder
                
                relPath = str.toString();
                fstate = avmCtx.getStateCache().findFileState( relPath);
                
                if ( fstate == null)
                {
                    // Create a file state for the version folder path
                    
                    fstate = avmCtx.getStateCache().findFileState( str.toString(), true);
                    fstate.setFileStatus( DirectoryExists);
                    
                    // Add a pseudo file for the data pseudo folder
                    
                    str.append( FileName.DOS_SEPERATOR);
                    str.append( AVMPath.DataFolder);
                    
                    fstate.addPseudoFile( new DummyFolderPseudoFile( AVMPath.DataFolder, str.toString()));
                    
                    // Add a pseudo file for the metadata pseudo folder
                    
                    str.setLength( relPath.length() + 1);
                    str.append( AVMPath.MetaDataFolder);
                    
                    fstate.addPseudoFile( new DummyFolderPseudoFile( AVMPath.MetaDataFolder, str.toString()));
                }
                break;
        }

        // Return the file state
        
        return fstate;
    }

    /**
     * Generate the pseudo folders for the specified path
     * 
     * @param avmPath
     *            AVMPath
     * @param avmCtx
     *            AVMContext
     */
    private final void generatePseudoFolders(AVMPath avmPath, AVMContext avmCtx)
    {
        // Create the root file state

        AVMPath createPath = new AVMPath();
        StringBuilder pathStr = new StringBuilder();

        pathStr.append(FileName.DOS_SEPERATOR);
        createPath.parsePath(pathStr.toString());

        FileState rootState = findPseudoState(createPath, avmCtx);

        // Check if the path has a store name

        if (avmPath.getStoreName() != null)
        {
            // Check if the store name is valid

            if (rootState.hasPseudoFiles()
                    && rootState.getPseudoFileList().findFile(avmPath.getStoreName(), false) != null)
            {
                // Create the store file state

                pathStr.append(avmPath.getStoreName());
                pathStr.append(FileName.DOS_SEPERATOR);

                createPath.parsePath(pathStr.toString());

                findPseudoState(createPath, avmCtx);

                // Add the head and version root pseudo folders

                createPath.parsePath(pathStr.toString() + AVMPath.VersionNameHead);
                findPseudoState(createPath, avmCtx);

                createPath.parsePath(pathStr.toString() + AVMPath.VersionsFolder);
                findPseudoState(createPath, avmCtx);

                // Check if the path is to a version folder

                if (avmPath.isLevel().ordinal() >= AVMPath.LevelId.Version.ordinal())
                {
                    // Build the path

                    pathStr.append(AVMPath.VersionsFolder);
                    pathStr.append(FileName.DOS_SEPERATOR);
                    pathStr.append(AVMPath.VersionFolderPrefix);
                    pathStr.append(avmPath.getVersion());

                    createPath.parsePath(pathStr.toString());

                    // Generate the version folders

                    findPseudoState(createPath, avmCtx);
                }
            }
        }
    }
    
    /**
     * Check that the user has access to the path
     * 
     * @param avmPath AVMPath
     * @param avmCtx AVMContext
     * @param sess SrvSession
     * @exception AccessDeniedException
     */
    private final void checkPathAccess( AVMPath avmPath, AVMContext avmCtx, SrvSession sess)
    	throws AccessDeniedException {
    	
    	// Only enforce access checks on virtualization views
    	
    	if ( avmCtx.isVirtualizationView() == false)
    		return;
    	
    	// Get the client details for the session
    	
    	ClientInfo cInfo = sess.getClientInformation();
    	if ( cInfo == null || cInfo.getUserName() == null || cInfo.getUserName().length() == 0)
    		throw new AccessDeniedException();

    	// Allow access to the root folder
    	
    	if ( avmPath.isLevel() == AVMPath.LevelId.Root || avmPath.isLevel() == AVMPath.LevelId.HeadData || avmPath.isLevel() == AVMPath.LevelId.StoreRootPath ) {
    		
    		// Allow read only access to the root, www and avm_webapps folders
    		avmPath.setReadOnlyAccess(true);
    		return;
    	}
    	
    	// Get root file state, get the store pseudo folder details

    	FileState rootState = avmCtx.getStateCache().findFileState( FileName.DOS_SEPERATOR_STR);
    	if ( rootState == null){
    	
    		// Recreate the root file state, new stores may have been added
    		
    		rootState = findPseudoState( new AVMPath( FileName.DOS_SEPERATOR_STR), avmCtx);
    	}
    	
    	// Check if there are any store pseudo folders
    	
    	if ( rootState != null && rootState.hasPseudoFiles())
    	{
    		PseudoFile pseudoFolder = rootState.getPseudoFileList().findFile( avmPath.getStoreName(), false);
    		if ( pseudoFolder != null)
    		{
    			// Check if the pseudo folder is a web project folder or sandbox within a web project

    			String curUserName = m_authComponent.getCurrentUserName();
    			
    			if ( pseudoFolder instanceof WebProjectStorePseudoFile)
    			{
    				// Check the users role within the web project
    				
    				WebProjectStorePseudoFile webFolder = (WebProjectStorePseudoFile) pseudoFolder;
    				
    				int role = webFolder.getUserRole( curUserName);
    				
    				if ( role == WebProjectStorePseudoFile.RoleNone)
    				{
    					// DEBUG
    					
    					if ( logger.isDebugEnabled())
    						logger.debug("User " + curUserName + " has no access to web project, " + webFolder.getFileName());
    					
	    				// User does not have access to this web project
	    				
	    				throw new AccessDeniedException("User " + curUserName + " has no access to web project, " + webFolder.getFileName());
    				}
    				else if ( avmCtx.allowAdminStagingWrites() && cInfo.isAdministrator())
    				{
    					// DEBUG
    					
    					if ( logger.isDebugEnabled())
    						logger.debug("User " + curUserName + " granted write access to web project, " + webFolder.getFileName());
    					
    					// Allow admin write access
    					
    					avmPath.setReadOnlyAccess( false);
    				}
    				else
    				{
    					// DEBUG
    					
    					if ( logger.isDebugEnabled())
    						logger.debug("User " + curUserName + " granted read-only access to web project, " + webFolder.getFileName());
    					
    					// Only allow read-only access to the staging area
    					
    					avmPath.setReadOnlyAccess( true);
    				}
    			}
    			else if ( pseudoFolder instanceof StorePseudoFile)
    			{
    				// Check the store type
    				
    				StorePseudoFile storeFolder = (StorePseudoFile) pseudoFolder;
    				if ( storeFolder.isStoreType() == StoreType.Normal)
    					return;
    				else if ( storeFolder.hasWebProject())
    				{
    					// Get the web project that the sandbox is linked to
    					
    					WebProjectStorePseudoFile webFolder = (WebProjectStorePseudoFile) rootState.getPseudoFileList().findFile( storeFolder.getWebProject(), false);
    					
    					int role = webFolder.getUserRole( curUserName);
    					
        				if ( role == WebProjectStorePseudoFile.RoleNone)
        				{
	        				// User does not have access to this web project
	        				
	        				throw new AccessDeniedException("User " + curUserName + " has no access to web project, " + webFolder.getFileName() + "/" + storeFolder.getFileName());
        				}
        				else if ( role == WebProjectStorePseudoFile.RolePublisher &&
        						  storeFolder.getUserName().equalsIgnoreCase( curUserName) == false)
        				{
	        				// User does not have access to this web project
	        				
	        				throw new AccessDeniedException("User " + curUserName + " has no access to web project, " + webFolder.getFileName() + "/" + storeFolder.getFileName());
        				}
    				}
    			}
    		}
    	}
    	else
    	{
	    	// Store does not exist
	    	
	    	throw new AccessDeniedException("Store does not exist, " + avmPath.getStoreName());
    	}
    	
    	// DEBUG
    	
    	if (logger.isDebugEnabled())
    		logger.debug( "Check access " + avmPath);
    }
    
    /**
     * Filter the list of pseudo folders returned in a search
     * 
     * @param avmCtx AVMContext
     * @param sess SrvSession
     * @param avmPath AVMPath
     * @param fstate FileState
     * @return PseudoFileList
     */
    private final PseudoFileList filterPseudoFolders( AVMContext avmCtx, SrvSession sess, AVMPath avmPath, FileState fstate)
    {
    	// Check if the root folder file state has any store pseudo folders 

    	if ( fstate.hasPseudoFiles() == false)
    		return null;

    	// Get the client details for the session
    	
    	ClientInfo cInfo = sess.getClientInformation();
    	if ( cInfo == null || cInfo.getUserName() == null || cInfo.getUserName().length() == 0)
    		return null;
    	
    	// Check for the admin user, no need to filter the list

    	PseudoFileList fullList   = fstate.getPseudoFileList();
    	if ( cInfo.isAdministrator())
    		return fullList;
    	
    	// Create a filtered list of store pseudo folders that the user has access to
    	
    	PseudoFileList filterList = new PseudoFileList();
    	String userName = m_authComponent.getCurrentUserName();
    	
    	for ( int i = 0; i < fullList.numberOfFiles(); i++)
    	{
    		// Get the current store pseudo folder
    		
    		PseudoFile pseudoFolder = fullList.getFileAt( i);
    		
			// Check if the pseudo folder is a web project folder or sandbox within a web project
			
			if ( pseudoFolder instanceof WebProjectStorePseudoFile)
			{
				// Check the users role within the web project
				
				WebProjectStorePseudoFile webFolder = (WebProjectStorePseudoFile) pseudoFolder;
				
				if ( avmCtx.showStagingStores() && webFolder.getUserRole( userName) != WebProjectStorePseudoFile.RoleNone)
				{
					// User has access to this store
					
					filterList.addFile( pseudoFolder);
				}
			}
			else if ( pseudoFolder instanceof StorePseudoFile)
			{
				// Check if the store type should be included in the visible list
				
				StorePseudoFile storeFolder = (StorePseudoFile) pseudoFolder;
				if ( avmCtx.showStoreType( storeFolder.isStoreType()))
				{
					// Check if the user has access to this store
					
					if ( storeFolder.hasWebProject())
					{
						// Get the web project that the sandbox is linked to
						
						WebProjectStorePseudoFile webFolder = (WebProjectStorePseudoFile) fullList.findFile( storeFolder.getWebProject(), false);
						
						if ( webFolder != null) {
							int role = webFolder.getUserRole( userName);
							
							if ( role == WebProjectStorePseudoFile.RoleContentManager && avmCtx.showStoreType( storeFolder.isStoreType()))
		    				{
		    					// User is a content manager, allow access to the store
		    					
		    					filterList.addFile( storeFolder);
		    				}
							else if ( role == WebProjectStorePseudoFile.RolePublisher && avmCtx.showStoreType( storeFolder.isStoreType()))
							{
								// Allow access if the user owns the current folder
								
								if ( storeFolder.getUserName().equalsIgnoreCase( userName))
									filterList.addFile( storeFolder);
							}
						}
						else if ( logger.isDebugEnabled())
							logger.debug("Cannot find associated web folder for store " + storeFolder.getFileName());
							
					}
					else if ( avmCtx.showNormalStores() || avmCtx.showSiteStores())
					{
						// Store is not linked to a web project, allow access to the store
						
						filterList.addFile( storeFolder);
					}
				}
			}
    	}
    	
    	// Return the filtered list
    	
    	return filterList;
    }
    
    /**
     * Add a new store to the top level folder list
     * 
     * @param avmCtx AVMContext
     * @param storeName String
     */
    protected void addNewStore( AVMContext avmCtx, String storeName) {

    	// Get the root folder file state
    	
        FileState fstate = avmCtx.getStateCache().findFileState( FileName.DOS_SEPERATOR_STR, true);
        if ( fstate == null)
        	return;
        
        // Get the properties for the store

    	AVMStoreDescriptor storeDesc = m_avmService.getStore( storeName);
    	if ( storeDesc == null)
    		return;
    	
        Map<QName, PropertyValue> props = m_avmService.getStoreProperties( storeName);
        
        // Check if the store is a main web project
        
        if ( props.containsKey( SandboxConstants.PROP_SANDBOX_STAGING_MAIN))
        {
        	// Get the noderef for the web project
        	
        	PropertyValue prop = props.get( SandboxConstants.PROP_WEB_PROJECT_NODE_REF);
        	if ( prop != null) {
        		
        		// Get the web project noderef
        		
        		NodeRef webNodeRef = new NodeRef( prop.getStringValue());
        		
        		if (m_nodeService.exists(webNodeRef))
                {
            		// Create the web project pseudo folder
            		
            		WebProjectStorePseudoFile webProjFolder = new WebProjectStorePseudoFile( storeDesc, FileName.DOS_SEPERATOR_STR + storeName, webNodeRef);
            		fstate.addPseudoFile( webProjFolder);
    
            		// DEBUG
            		
            		if ( logger.isDebugEnabled())
            			logger.debug( " Found web project " + webProjFolder.getFileName());
            		
            		// Get the list of content managers for this web project
    
            		List<ChildAssociationRef> mgrAssocs = m_nodeService.getChildAssocs( webNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            		
            		for ( ChildAssociationRef mgrRef : mgrAssocs)
            		{
            			// Get the child node and see if it is a content manager association
            			
            			NodeRef childRef = mgrRef.getChildRef();
            			
            			if ( m_nodeService.getProperty( childRef, WCMAppModel.PROP_WEBUSERROLE).equals(ROLE_CONTENT_MANAGER))
            			{
            				// Get the user name add it to the web project pseudo folder
            				
            				String userName = (String) m_nodeService.getProperty( childRef, WCMAppModel.PROP_WEBUSERNAME);
            				
            				webProjFolder.addUserRole( userName, WebProjectStorePseudoFile.RoleContentManager);
            				
            				// DEBUG
            				
            				if ( logger.isDebugEnabled())
            					logger.debug("  Added content manager " + userName);
            			}
            		}
        	    }
        		else
        		{
        		    logger.warn("AVM Store '"+storeName+"' with webProjectNodeRef that does not exist: "+webNodeRef);
        		}
        	}
        }
        else
        {
        	// Check if this store is a web project sandbox
        	
        	int storeType = StoreType.Normal;
        	String webProjName = null;
        	String userName    = null;
        	
            if ( props.containsKey( SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
            {
            	// Sandbox store, linked to a web project
            	
                storeType = StoreType.WebAuthorMain;
                
                // Get the associated web project name

                webProjName = props.get( SandboxConstants.PROP_WEBSITE_NAME).getStringValue();

                // Get the user name from teh store name

                userName = storeName.substring( webProjName.length() + 2);
            }
            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW))
            {
            	// Author preview sandbox store, linked to a web project
            	
                storeType = StoreType.WebAuthorPreview;
                
                // Get the associated web project name

                String projPlusUser = storeName.substring( 0, storeName.length() - "--preview".length());
                int pos = projPlusUser.lastIndexOf("--");
                if ( pos != -1)
                {
                	webProjName = projPlusUser.substring( 0, pos);
                	userName    = projPlusUser.substring(pos + 2);
                }
            }
            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW))
            {
            	// Staging preview sandbox store, linked to a web project
            	
                storeType = StoreType.WebStagingPreview;
            }
            else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW))
            {
            	// Staging preview sandbox store, linked to a web project
            	
                storeType = StoreType.WebStagingPreview;
                
                // Get the associated web project name

                webProjName = storeName.substring( 0, storeName.length() - "--preview".length());
            }
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug( " Store " + storeDesc.getName() + ", type=" + StoreType.asString( storeType) + ", webproj=" + webProjName + ", username=" + userName);
            
            // Add a pseudo file for the current store

            if ( avmCtx.showStoreType( storeType))
            {
            	// Create the pseudo folder for the store
            	
            	StorePseudoFile storeFolder = new StorePseudoFile( storeDesc, FileName.DOS_SEPERATOR_STR + storeName, storeType);
            	if ( storeType != StoreType.Normal)
            	{
            		storeFolder.setWebProject( webProjName);
            		storeFolder.setUserName( userName);

            		// Add all publisher/reviewer user names to the web project roles list
            		
            		if ( storeFolder.hasWebProject())
        			{
        				// Find the associated web project pseudo folder
        				
                        PseudoFileList folderList = fstate.getPseudoFileList();
                        if ( folderList != null) {

                        	// Find the associated web project
                        	
	        				WebProjectStorePseudoFile webProj = (WebProjectStorePseudoFile) folderList.findFile( storeFolder.getWebProject(), true);

	        				if ( webProj != null) {
	        					
		        				// Strip the web project name from the sandbox store name and extract the user name.
		        				// Add the user as a publisher/reviewer to the web project roles list
		        				
		        				userName = storeFolder.getFileName().substring( webProj.getFileName().length() + 2);
		        				
		        				// If the user does not have a content manager role then add as a publisher
		        				
		        				if ( webProj.getUserRole( userName) == WebProjectStorePseudoFile.RoleNone)
		        				{
		        					webProj.addUserRole( userName, WebProjectStorePseudoFile.RolePublisher);
		        				
		            				// DEBUG
		            				
		            				if ( logger.isDebugEnabled())
		            					logger.debug( " Added publisher " + userName + " to " + webProj.getFileName());
		        				}
	        				}
                        }
        			}
            	}

            	// Add the store pseudo folder to the root folder file list
            	
                fstate.addPseudoFile( storeFolder);
            }
        }
    }
}
