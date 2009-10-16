/*
 * Copyright (C) 2006-2008 Alfresco Software Limited.
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

package org.alfresco.filesys.repo;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.filesys.state.FileState;
import org.alfresco.filesys.state.FileStateTable;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NotifyChange;
import org.alfresco.jlan.smb.server.notify.NotifyChangeHandler;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Node Monitor Class
 * 
 * <p>Monitor node events from the node service to update the file state cache and feed notification events into
 * the file server change notification handler.
 * 
 * @author gkspencer
 */
public class NodeMonitor extends TransactionListenerAdapter 
						 implements  NodeServicePolicies.OnCreateNodePolicy,
									 NodeServicePolicies.OnUpdatePropertiesPolicy,
									 NodeServicePolicies.BeforeDeleteNodePolicy,
									 NodeServicePolicies.OnMoveNodePolicy,
									 Runnable
{
    // Logging
    
    private static final Log logger = LogFactory.getLog(NodeMonitor.class);

    // Transaction object binding keys
    
    public static final String FileSysNodeEvent  = "FileSysNodeEvent";
    public static final String FileSysNodeEvent2 = "FileSysNodeEvent2";
    
    // Services/components
	
	private PolicyComponent m_policyComponent;
	private NodeService m_nodeService;
	private FileFolderService m_fileFolderService;
	private PermissionService m_permissionService;
	private TransactionService m_transService;
	
	// Filesystem driver and context
	
	private ContentDiskDriver m_filesysDriver;
	private ContentContext m_filesysCtx;
	
	// File state table and change notification handler
	
	private FileStateTable m_stateTable;
	private NotifyChangeHandler m_changeHandler;
	
	// Root node path and store
	
	private String m_rootPath;
	private StoreRef m_storeRef;
	
	// Queue of node update events
	
	private NodeEventQueue m_eventQueue;

	// Thread for the main event processing
	
	private Thread m_thread;
	private boolean m_shutdown;
	
	/**
	 * Class constructor
	 * 
	 * @param filesysDriver ContentDiskDriver
	 * @param filesysCtx ContentContext
	 */
	protected NodeMonitor( ContentDiskDriver filesysDriver, ContentContext filesysCtx, NodeService nodeService, PolicyComponent policyComponent,
			FileFolderService fileFolderService, PermissionService permissionService, TransactionService transService) {
		m_filesysDriver = filesysDriver;
		m_filesysCtx    = filesysCtx;
		
		// Set various services

		m_nodeService       = nodeService;
		m_policyComponent   = policyComponent;
		m_fileFolderService = fileFolderService;
		m_permissionService = permissionService;
		m_transService      = transService;
		
		// Initialize the node monitor
		
		init();
	}

	/**
	 * Initialize the node monitor
	 */
	public final void init() {

		// Disable change notifications from the file server
		
		m_filesysCtx.setFileServerNotifications( false);
		
        // Register for node service events
		
        m_policyComponent.bindClassBehaviour( QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                this, new JavaBehaviour(this, "onCreateNode"));   
        m_policyComponent.bindClassBehaviour( QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                this, new JavaBehaviour(this, "beforeDeleteNode"));   
        m_policyComponent.bindClassBehaviour( QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                this, new JavaBehaviour(this, "onDeleteNode"));   
        m_policyComponent.bindClassBehaviour( QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"),
                this, new JavaBehaviour(this, "onMoveNode"));
        m_policyComponent.bindClassBehaviour( QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
    		  	this, new JavaBehaviour(this, "onUpdateProperties"));

        // Get the store
        
        m_storeRef = m_filesysCtx.getRootNode().getStoreRef();
        
        // Get the root node path
        
        String rootPath = (String) m_nodeService.getProperty( m_filesysCtx.getRootNode(), ContentModel.PROP_NAME);
        
        StringBuilder pathBuilder= new StringBuilder();
        pathBuilder.append("/");
        if ( rootPath != null && rootPath.length() > 0)
        	pathBuilder.append( rootPath);
        
        m_rootPath = pathBuilder.toString();
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
        	logger.debug("Node monitor filesystem=" + m_filesysCtx.getDeviceName() + ", rootPath=" + m_rootPath);
        
        // Create the node event queue
        
        m_eventQueue = new NodeEventQueue();
        
        // DEBUG

        if ( logger.isDebugEnabled())
        	logger.debug("Node monitor installed for " + m_filesysCtx.getDeviceName());
	}

	/**
	 * Start the node monitor thread
	 */
	public void startMonitor() {
		
        // Get the file state table and change notification handler, if enabled
        
        m_stateTable = m_filesysCtx.getStateTable();
        m_changeHandler = m_filesysCtx.getChangeHandler();
        
        // Start the event processing thread
        
        m_thread = new Thread( this);
        m_thread.setName( "NodeMonitor_" + m_filesysCtx.getDeviceName());
        m_thread.setDaemon( true);
        
        m_thread.start();
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
        	logger.debug("NodeMonitor started, " + m_thread.getName());
	}
	
	/**
	 * Create node event
	 * 
	 * @param childAssocRef ChildAssociationRef
	 */
    public void onCreateNode(ChildAssociationRef childAssocRef) {
    	
    	// Check if the node is a file/folder
    	
    	NodeRef nodeRef = childAssocRef.getChildRef();
    	if ( nodeRef.getStoreRef().equals( m_storeRef) == false)
    		return;
    	
    	QName nodeType = m_nodeService.getType( nodeRef);
    	FileFolderServiceType fType = m_fileFolderService.getType( nodeType);
    	
    	if ( fType != FileFolderServiceType.INVALID) {
    		
    		// DEBUG
    		
    		if ( logger.isDebugEnabled()) {

    			// Get the full path to the file/folder node
    		
	    		Path nodePath = m_nodeService.getPath( nodeRef);
	    		String fName = (String) m_nodeService.getProperty( nodeRef, ContentModel.PROP_NAME);

    			logger.debug("OnCreateNode: nodeRef=" + nodeRef + ", name=" + fName + ", path=" + nodePath.toDisplayPath(m_nodeService, m_permissionService));
    		}
    	
    		// Create an event to process the node creation
    		
    		NodeEvent nodeEvent = new CreateNodeEvent( fType, nodeRef);
    		
    		// Store the event in the transaction until committed, and register the transaction listener
    		
    		AlfrescoTransactionSupport.bindListener( this);
    		AlfrescoTransactionSupport.bindResource( FileSysNodeEvent, nodeEvent);
    	}
    }

    /**
     * Update properties event
     * 
     * @param nodeRef NodeRef
     * @param before Map<QName, Serializable>
     * @param after Map<QName, Serializable>
     */
    public void onUpdateProperties( NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    	
    	// Check that the node is in our store
    	
    	if ( nodeRef.getStoreRef().equals( m_storeRef) == false)
    		return;

    	// Check if the node is a file/folder
    	
    	QName nodeType = m_nodeService.getType( nodeRef);
    	FileFolderServiceType fType = m_fileFolderService.getType( nodeType);
    	
    	if ( fType != FileFolderServiceType.INVALID) {

    		// Check if there has been a lock change
    		
    		NodeEvent nodeEvent = null;
    		
    		String beforeLock = (String) before.get( ContentModel.PROP_LOCK_TYPE);
    		String afterLock  = (String) after.get( ContentModel.PROP_LOCK_TYPE);
    		
    		if (( beforeLock != null && afterLock == null) ||
    				( beforeLock == null && afterLock != null)) {
    			
    			// Process the update

    			nodeEvent = new LockNodeEvent( fType, nodeRef, beforeLock, afterLock);
    		}
    		
    		// Check if a node event has been created
    		
    		if ( nodeEvent != null) {

    			// Check for an existing event
    			
    			String eventKey = FileSysNodeEvent;
    			if ( AlfrescoTransactionSupport.getResource( FileSysNodeEvent) != null)
    				eventKey = FileSysNodeEvent2;
    			
	    		// Store the event in the transaction until committed, and register the transaction listener
	    		
	    		AlfrescoTransactionSupport.bindListener( this);
	    		AlfrescoTransactionSupport.bindResource( eventKey, nodeEvent);
    		}
    	}
    }
    
    /**
     * Delete node event
     * 
     * @param childAssocRef ChildAssociationRef
     * @param isArchiveNode boolean
     */
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isArchivedNode) {
    	
    	// Check if there is a node event stored in the transaction
    	
    	NodeEvent nodeEvent = (NodeEvent) AlfrescoTransactionSupport.getResource( FileSysNodeEvent);
    	
    	if ( nodeEvent != null && nodeEvent instanceof DeleteNodeEvent) {
    		
    		// Should be the same node id

    		DeleteNodeEvent deleteEvent = (DeleteNodeEvent) nodeEvent;
        	NodeRef nodeRef = childAssocRef.getChildRef();
        	
    		if ( nodeRef.equals( deleteEvent.getNodeRef())) {
    			
    			// Confirm the node delete
    			
    			deleteEvent.setDeleteConfirm( true);

    			// DEBUG
        		
        		if ( logger.isDebugEnabled())
        			logger.debug("OnDeleteNode: confirm delete nodeRef=" + nodeRef);
    		}
    	}
    }

	/**
	 * Move node event
	 * 
	 * @param oldChildAssocRef ChildAssociationRef
	 * @param newChildAssocRef ChildAssociationRef
	 */
	public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
		
    	// Check if the node is a file/folder, and for our store
    	
    	NodeRef oldNodeRef = oldChildAssocRef.getChildRef();
    	if ( oldNodeRef.getStoreRef().equals( m_storeRef) == false)
    		return;
    	
    	QName nodeType = m_nodeService.getType( oldNodeRef);
    	FileFolderServiceType fType = m_fileFolderService.getType( nodeType);
    	
    	if ( fType != FileFolderServiceType.INVALID) {
    		
    		// Get the full path to the file/folder node
    		
    		Path nodePath = m_nodeService.getPath( oldNodeRef);
    		String fName = (String) m_nodeService.getProperty( oldNodeRef, ContentModel.PROP_NAME);
    		
    		// Build the share relative path to the node
    		
    		StringBuilder pathStr = new StringBuilder();
    		pathStr.append( nodePath.toDisplayPath(m_nodeService, m_permissionService));
    		if ( pathStr.charAt(pathStr.length() - 1) != '/' && pathStr.charAt(pathStr.length() - 1) != '\\')
    			pathStr.append("\\");
    		pathStr.append( fName);

    		String relPath = pathStr.toString();
    		
    		// DEBUG
    		
    		if ( logger.isDebugEnabled())
    			logger.debug("OnMoveNode: nodeRef=" + oldNodeRef + ", relPath=" + relPath);

    		// Queue an event to process the node move
    		
    		if ( relPath.startsWith( m_rootPath)) {

	    		// Create a move event
		 		
				NodeEvent nodeEvent = new MoveNodeEvent( fType, oldNodeRef, relPath, newChildAssocRef.getChildRef());
			
	    		// Store the event in the transaction until committed, and register the transaction listener
	    		
	    		AlfrescoTransactionSupport.bindListener( this);
	    		AlfrescoTransactionSupport.bindResource( FileSysNodeEvent, nodeEvent);
    		}
    	}
	}

	/**
	 * Before delete node event
	 * 
	 * @param nodeRef NodeRef
	 */
	public void beforeDeleteNode(NodeRef nodeRef) {

		// Check if the node is in the filesystem store
		
    	if ( nodeRef.getStoreRef().equals( m_storeRef) == false)
    		return;
    	
    	// Check if the node is a file/folder
    	
    	QName nodeType = m_nodeService.getType( nodeRef);
    	FileFolderServiceType fType = m_fileFolderService.getType( nodeType);
    	
    	if ( fType != FileFolderServiceType.INVALID) {
    		
    		// Get the full path to the file/folder node
    		
    		Path nodePath = m_nodeService.getPath( nodeRef);
    		String fName = (String) m_nodeService.getProperty( nodeRef, ContentModel.PROP_NAME);
    		
    		// Build the share relative path to the node
    		
    		StringBuilder pathStr = new StringBuilder();
    		pathStr.append( nodePath.toDisplayPath(m_nodeService, m_permissionService));
    		if ( pathStr.length() == 0 || (pathStr.charAt(pathStr.length() - 1) != '/' && pathStr.charAt(pathStr.length() - 1) != '\\'))
    			pathStr.append("\\");
    		pathStr.append( fName);

    		String relPath = pathStr.toString();
    		
    		// Create an event to process the node deletion
    		
    	 	if ( relPath.startsWith( m_rootPath)) {
    	 		
    	 		// Create a delete event
    	 		
    			NodeEvent nodeEvent = new DeleteNodeEvent( fType, nodeRef, relPath);
    		
	    		// Store the event in the transaction until committed, and register the transaction listener
	    		
	    		AlfrescoTransactionSupport.bindListener( this);
	    		AlfrescoTransactionSupport.bindResource( FileSysNodeEvent, nodeEvent);

	    		// DEBUG
	    		
	    		if ( logger.isDebugEnabled())
	    			logger.debug("BeforeDeleteNode: nodeRef=" + nodeRef + ", relPath=" + relPath);
    	 	}
    	}
	}

	/**
	 * Request the node monitor thread to shut down
	 */
	public final void shutdownRequest() {
		
		if ( m_thread != null) {
			
			// Set the shutdown request flag
			
			m_shutdown = true;
			
			// Interrupt the event processing thread
			
			try {
				m_thread.interrupt();
			}
			catch ( Exception ex) {
			}
		}
	}

	/**
	 * Transaction processing hook 
	 */
	public void afterCommit() {
		
		// Get the node event that was stored in the transaction
		
		NodeEvent nodeEvent = (NodeEvent) AlfrescoTransactionSupport.getResource( FileSysNodeEvent);
		if ( nodeEvent != null) {
			
			// Queue the primary event for processing
		
			m_eventQueue.addEvent( nodeEvent);
			
			// Unbind the resource from the transaction
			
			AlfrescoTransactionSupport.unbindResource(FileSysNodeEvent);
			
			// Check for a secondary event
			
			nodeEvent = (NodeEvent) AlfrescoTransactionSupport.getResource(FileSysNodeEvent2);
			if ( nodeEvent != null) {
				
				// Queue the secondary event
			
				m_eventQueue.addEvent( nodeEvent);
				
				// Unbind the resource from the transaction
				
				AlfrescoTransactionSupport.unbindResource(FileSysNodeEvent2);
			}
		}
	}

	
	/**
	 * Event queue processing
	 */
	public void run() {
		
		// Clear the shutdown flag
		
		m_shutdown = false;
		
        // Use the system user as the authenticated context for the node monitor
        
		AuthenticationContext authenticationContext = m_filesysDriver.getAuthenticationContext();
        authenticationContext.setSystemUserAsCurrentUser();

		// Loop until shutdown
		
		while ( m_shutdown == false)
		{
			
			try
			{
	            // Wait for an event to process
				
			    final NodeEvent nodeEvent = m_eventQueue.removeEvent();

			    // DEBUG
			    
                if ( logger.isDebugEnabled())
                    logger.debug("Processing event " + nodeEvent);
                
                // Check for a shutdown
                
                if ( m_shutdown == true)
                    continue;
                
                RetryingTransactionCallback<Object> processEventCallback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        // Process the event
                    	
                        if (nodeEvent == null)
                        {
                            return null;
                        }
                        
                        // check for a node delete
                        
                        if ( nodeEvent instanceof DeleteNodeEvent) {

                            // Node deleted
                            
                            processDeleteNode((DeleteNodeEvent) nodeEvent);
                        }
                        
                        // Check that the node is still valid
                        
                        else if (!m_nodeService.exists(nodeEvent.getNodeRef()))
                        {
                            return null;
                        }
                        
                        // Process the node event, for an existing node
                        
                        if ( nodeEvent instanceof CreateNodeEvent) {
                            
                            // Node created
                            
                            processCreateNode((CreateNodeEvent) nodeEvent);
                        }
                        else if ( nodeEvent instanceof MoveNodeEvent) {
                            
                            // Node moved
                                
                            processMoveNode((MoveNodeEvent) nodeEvent);
                        }
                        else if ( nodeEvent instanceof LockNodeEvent) {
                            
                            // Node locked/unlocked
                            
                            processLockNode(( LockNodeEvent) nodeEvent);
                        }
                        
                        // Done
                        
                        return null;
                    }
                };
                
                // Execute in a read-only transaction
                
                m_transService.getRetryingTransactionHelper().doInTransaction(processEventCallback, true, true);
			}
			catch ( InterruptedException ex)
			{
			}
			catch (Throwable e)
			{
			    logger.error(e);
			}
		}
	}
	
	/**
	 * Process a create node event
	 * 
	 * @param createEvent CreateNodeEvent
	 */
	private final void processCreateNode(NodeEvent createEvent) {

		// Get the full path to the file/folder node
		
		Path nodePath = m_nodeService.getPath( createEvent.getNodeRef());
		String relPath = nodePath.toDisplayPath(m_nodeService, m_permissionService);
		String fName = (String) m_nodeService.getProperty( createEvent.getNodeRef(), ContentModel.PROP_NAME);
		
		// Check if the path is within the filesystem view
		
		if ( relPath.startsWith( m_rootPath)) {
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug("CreateNode nodeRef=" + createEvent.getNodeRef() + ", fName=" + fName + ", path=" + relPath);
			
			// Build the full file path
			
			StringBuilder fullPath = new StringBuilder();
			fullPath.append( relPath.substring( m_rootPath.length()));
			fullPath.append( "/");
			fullPath.append( fName);
			
			relPath = fullPath.toString();
			
			// Update an existing file state to indicate that the file exists, may have been marked as deleted
			
			if ( m_stateTable != null) {
				
				// Check if there is file state for this file
				
				FileState fState = m_stateTable.findFileState( relPath);
				if ( fState != null && fState.exists() == false) {

					// Check if the new node is a file or folder
					
					if ( createEvent.getFileType() == FileFolderServiceType.FILE)
						fState.setFileStatus(FileStatus.FileExists);
					else
						fState.setFileStatus(FileStatus.DirectoryExists);
					
					// DEBUG
					
					if ( logger.isDebugEnabled())
						logger.debug("CreateNode updated file state - " + fState);
				}
			}
			
			// If change notifications are enabled then send an event to registered listeners
			
			if ( m_changeHandler != null) {
				
				// Check if there are any active notifications
				
				if ( m_changeHandler.getGlobalNotifyMask() != 0) {
					
					// Send a file created event to the change notification handler
					
					if ( createEvent.getFileType() == FileFolderServiceType.FILE)
						m_changeHandler.notifyFileChanged(NotifyChange.ActionAdded, relPath);
					else
						m_changeHandler.notifyDirectoryChanged(NotifyChange.ActionAdded, relPath);

					// DEBUG
					
					if ( logger.isDebugEnabled())
						logger.debug("CreateNode queued change notification");
				}
			}
		}
		else {

			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug("CreateNode ignored nodeRef=" + createEvent.getNodeRef() + ", path=" + relPath);
		}
	
	}
	
	/**
	 * Process a node delete event
	 * 
	 * @param deleteEvent DeleteNodeEvent
	 */
	private final void processDeleteNode(DeleteNodeEvent deleteEvent) {

		// Check if the delete was confirmed
		
		if ( deleteEvent.hasDeleteConfirm() == false) {
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug("DeleteNode not confirmed, nodeRef=" + deleteEvent.getNodeRef() + ", path=" + deleteEvent.getPath());
		
			return;
		}
		
		// Strip the root path
		
		String relPath = deleteEvent.getPath().substring( m_rootPath.length()).replace( '/', '\\');
		
		// DEBUG
		
		if ( logger.isDebugEnabled())
			logger.debug("DeleteNode nodeRef=" + deleteEvent.getNodeRef() + ", path=" + relPath);
		
		// Update an existing file state to indicate that the file does not exist
		
		if ( m_stateTable != null) {
			
			// Check if there is file state for this file
			
			FileState fState = m_stateTable.findFileState( relPath);
			if ( fState != null && fState.exists() == true) {

				// Mark the file/folder as no longer existing
				
				fState.setFileStatus(FileStatus.NotExist);
				
				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("DeleteNode updated file state - " + fState);
			}
		}
		
		// If change notifications are enabled then send an event to registered listeners
		
		if ( m_changeHandler != null) {
			
			// Check if there are any active notifications
			
			if ( m_changeHandler.getGlobalNotifyMask() != 0) {
				
				// Send a file deleted event to the change notification handler
				
				if ( deleteEvent.getFileType() == FileFolderServiceType.FILE)
					m_changeHandler.notifyFileChanged(NotifyChange.ActionRemoved, relPath);
				else
					m_changeHandler.notifyDirectoryChanged(NotifyChange.ActionRemoved, relPath);

				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("DeleteNode queued change notification");
			}
		}
	}

	/**
	 * Process a node move event
	 * 
	 * @param moveEvent MoveNodeEvent
	 */
	private final void processMoveNode(MoveNodeEvent moveEvent) {

		// Strip the root path
		
		String fromPath = moveEvent.getPath().substring( m_rootPath.length()).replace( '/', '\\');

		// Get the destination relative path
		
		Path nodePath = m_nodeService.getPath( moveEvent.getMoveToNodeRef());
		String fName = (String) m_nodeService.getProperty( moveEvent.getMoveToNodeRef(), ContentModel.PROP_NAME);
		
		// Build the share relative path to the destination
		
		StringBuilder pathStr = new StringBuilder();
		pathStr.append( nodePath.toDisplayPath(m_nodeService, m_permissionService));
		if ( pathStr.charAt(pathStr.length() - 1) != '/' && pathStr.charAt(pathStr.length() - 1) != '\\')
			pathStr.append("\\");
		pathStr.append( fName);

		String toPath = pathStr.toString().substring( m_rootPath.length()).replace( '/', '\\');
		
		// DEBUG
		
		if ( logger.isDebugEnabled())
			logger.debug("MoveNode fromPath=" + fromPath + ", toPath=" + toPath);
		
		// Update an existing file state to indicate that the file does not exist
		
		if ( m_stateTable != null) {
			
			// Check if there is file state for the orginal file/folder
			
			FileState fState = m_stateTable.findFileState( fromPath);
			if ( fState != null && fState.exists() == true) {

				// Mark the file/folder as no longer existing
				
				fState.setFileStatus(FileStatus.NotExist);
				
				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("MoveNode updated state for fromPath=" + fromPath);
			}
			
			// Check if there is a file state for the destination file/folder
			
			fState = m_stateTable.findFileState( toPath);
			if ( fState != null && fState.exists() == false) {
				
				// Indicate the the file or folder exists
				
				if ( moveEvent.getFileType() == FileFolderServiceType.FILE)
					fState.setFileStatus(FileStatus.FileExists);
				else
					fState.setFileStatus(FileStatus.DirectoryExists);
				
				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("MoveNode updated state for toPath=" + toPath);
			}
		}
		
		// If change notifications are enabled then send an event to registered listeners
		
		if ( m_changeHandler != null) {
			
			// Check if there are any active notifications
			
			if ( m_changeHandler.getGlobalNotifyMask() != 0) {
				
				// Send a file renamed event to the change notification handler

				m_changeHandler.notifyRename( fromPath, toPath);

				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("MoveNode queued change notification");
			}
		}
	}

	/**
	 * Process a node lock/unlock event
	 * 
	 * @param lockEvent LockNodeEvent
	 */
	private final void processLockNode(LockNodeEvent lockEvent) {
		
		// Get the full path to the file/folder node
		
		Path nodePath = m_nodeService.getPath( lockEvent.getNodeRef());
		String relPath = nodePath.toDisplayPath(m_nodeService, m_permissionService);
		String fName = (String) m_nodeService.getProperty( lockEvent.getNodeRef(), ContentModel.PROP_NAME);
		
		// Check if the path is within the filesystem view
		
		if ( relPath.startsWith( m_rootPath)) {
			
			// DEBUG
			
			if ( logger.isDebugEnabled())
				logger.debug("LockNode nodeRef=" + lockEvent.getNodeRef() + ", fName=" + fName + ", path=" + relPath);
			
			// Build the full file path
			
			StringBuilder fullPath = new StringBuilder();
			fullPath.append( relPath.substring( m_rootPath.length()));
			fullPath.append( "/");
			fullPath.append( fName);
			
			relPath = fullPath.toString().replace( '/', '\\');
			
			// Node has been locked or unlocked, send a change notification to indicate the file attributes have changed

			if ( m_changeHandler != null) {
				
				// Send out a change of attributes notification
				
				m_changeHandler.notifyAttributesChanged( relPath, lockEvent.getFileType() == FileFolderServiceType.FILE ? false : true);

				// DEBUG
				
				if ( logger.isDebugEnabled())
					logger.debug("LockNode queued change notification");
			}
		}
	}
}
