/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

import java.io.FileNotFoundException;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.IOControlNotImplementedException;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.NTIOCtl;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.server.repo.CifsHelper;
import org.alfresco.filesys.smb.server.repo.ContentDiskDriver;
import org.alfresco.filesys.smb.server.repo.IOControlHandler;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content Disk Driver I/O Control Handler Class
 *
 * <p>Provides the custom I/O control code handling used by the CIFS client interface application.
 * 
 * @author gkspencer
 */
public class ContentIOControlHandler implements IOControlHandler
{
    // Logging
    
    private static final Log logger = LogFactory.getLog(ContentIOControlHandler.class);
    
    // Filesystem driver and context
    
    private ContentDiskDriver contentDriver;
    private ContentContext contentContext;
    
    /**
     * Default constructor
     */
    public ContentIOControlHandler()
    {
    }
    
    /**
     * Initalize the I/O control handler
     *
     * @param contentDriver ContentDiskDriver
     * @param contentContext ContentContext
     */
    public void initialize( ContentDiskDriver contentDriver, ContentContext contentContext)
    {
        this.contentDriver  = contentDriver;
        this.contentContext = contentContext;
    }

    /**
     * Return the CIFS helper
     * 
     * @return CifsHelper
     */
    public final CifsHelper getCifsHelper()
    {
    	return contentDriver.getCifsHelper();
    }
    
    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    public final TransactionService getTransactionService()
    {
    	return contentDriver.getTransactionService();
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    public final NodeService getNodeService()
    {
    	return contentDriver.getNodeService();
    }
    
    /**
     * Return the filesystem driver
     * 
     * @return ContentDiskDriver
     */
    public final ContentDiskDriver getContentDriver()
    {
    	return contentDriver;
    }
    
    /**
     * Return the filesystem context
     * 
     * @return ContentContext
     */
    public final ContentContext getContentContext()
    {
    	return contentContext;
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
        
        // Split the control code
        
        int devType = NTIOCtl.getDeviceType(ctrlCode);
        int ioFunc  = NTIOCtl.getFunctionCode(ctrlCode);
        
        if ( devType != NTIOCtl.DeviceFileSystem || dataBuf == null)
            throw new IOControlNotImplementedException();
        
        // Check if the request has a valid signature for an Alfresco CIFS server I/O control
        
        if ( dataBuf.getLength() < IOControl.Signature.length())
            throw new IOControlNotImplementedException("Bad request length");
        
        String sig = dataBuf.getString(IOControl.Signature.length(), false);
        
        if ( sig == null || sig.compareTo(IOControl.Signature) != 0)
            throw new IOControlNotImplementedException("Bad request signature");
        
        // Get the node for the parent folder, make sure it is a folder
        
        NodeRef folderNode = null;
        
        try
        {
            folderNode = contentDriver.getNodeForPath(tree, netFile.getFullName());
            
            if ( getCifsHelper().isDirectory( folderNode) == false)
                folderNode = null;
        }
        catch ( FileNotFoundException ex)
        {
            folderNode = null;
        }

        // If the folder node is not valid return an error
        
        if ( folderNode == null)
            throw new SMBException(SMBStatus.NTErr, SMBStatus.NTAccessDenied);
        
        // Debug
        
        if ( logger.isDebugEnabled()) {
            logger.debug("IO control func=0x" + Integer.toHexString(ioFunc) + ", fid=" + fid + ", buffer=" + dataBuf);
            logger.debug("  Folder nodeRef=" + folderNode);
        }

        // Check if the I/O control code is one of our custom codes

        DataBuffer retBuffer = null;
        
        switch ( ioFunc)
        {
	        // Probe to check if this is an Alfresco CIFS server
	        
	        case IOControl.CmdProbe:
	            
	            // Return a buffer with the signature and protocol version
	            
	            retBuffer = new DataBuffer(IOControl.Signature.length());
	            retBuffer.putFixedString(IOControl.Signature, IOControl.Signature.length());
	            retBuffer.putInt(DesktopAction.StsSuccess);
	            retBuffer.putInt(IOControl.Version);
	            break;
	            
	        // Get file information for a file within the current folder
	            
	        case IOControl.CmdFileStatus:
	
	            // Process the file status request
	            
	            retBuffer = procIOFileStatus( sess, tree, dataBuf, folderNode);
	            break;
	
	        // Get action information for the specified executable path
	            
	        case IOControl.CmdGetActionInfo:
	        	
	        	// Process the get action information request
	        	
	        	retBuffer = procGetActionInfo(sess, tree, dataBuf, folderNode, netFile);
	        	break;
	        	
	        // Run the named action
	        	
	        case IOControl.CmdRunAction:
	
	        	// Process the run action request
	        	
	        	retBuffer = procRunAction(sess, tree, dataBuf, folderNode, netFile);
	        	break;
	        	
	        // Unknown I/O control code
	            
	        default:
	            throw new IOControlNotImplementedException();
        }
        
        // Return the reply buffer, may be null
        
        return retBuffer;
    }
    
    /**
     * Process the file status I/O request
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param reqBuf Request buffer
     * @param folderNode NodeRef of parent folder
     * @return DataBuffer
     */
    private final DataBuffer procIOFileStatus( SrvSession sess, TreeConnection tree, DataBuffer reqBuf, NodeRef folderNode)
    {
        // Start a transaction
        
        sess.beginTransaction( getTransactionService(), true);
        
        // Get the file name from the request
        
        String fName = reqBuf.getString( true);

        if ( logger.isDebugEnabled())
        	logger.debug("  File status, fname=" + fName);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
        // Get the node for the file/folder
        
        NodeRef childNode = null;
        
        try
        {
            childNode = getCifsHelper().getNodeRef( folderNode, fName);
        }
        catch (FileNotFoundException ex)
        {
        }

        // Check if the file/folder was found
        
        if ( childNode == null)
        {
            // Return an error response
            
            respBuf.putInt(DesktopAction.StsFileNotFound);
            return respBuf;
        }

        // Check if this is a file or folder node
        
        if ( getCifsHelper().isDirectory( childNode))
        {
            // Only return the status and node type for folders
            
            respBuf.putInt(DesktopAction.StsSuccess);
            respBuf.putInt(IOControl.TypeFolder);
        }
        else
        {
            // Indicate that this is a file node
            
            respBuf.putInt(DesktopAction.StsSuccess);
            respBuf.putInt(IOControl.TypeFile);

            // Check if this file is a working copy
            
            if ( getNodeService().hasAspect( childNode, ContentModel.ASPECT_WORKING_COPY))
            {
                // Indicate that this is a working copy
                
                respBuf.putInt(IOControl.True);
                
                // Get the owner username and file it was copied from
                
                String owner = (String) getNodeService().getProperty( childNode, ContentModel.PROP_WORKING_COPY_OWNER);
                String copiedFrom = null;
                
                if ( getNodeService().hasAspect( childNode, ContentModel.ASPECT_COPIEDFROM))
                {
                    // Get the path of the file the working copy was generated from
                    
                    NodeRef fromNode = (NodeRef) getNodeService().getProperty( childNode, ContentModel.PROP_COPY_REFERENCE);
                    if ( fromNode != null)
                        copiedFrom = (String) getNodeService().getProperty( fromNode, ContentModel.PROP_NAME);
                }
                
                // Pack the owner and copied from values
                
                respBuf.putString(owner != null ? owner : "", true, true);
                respBuf.putString(copiedFrom != null ? copiedFrom : "", true, true);
            }
            else
            {
                // Not a working copy
                
                respBuf.putInt(IOControl.False);
            }
            
            // Check the lock status of the file
            
            if ( getNodeService().hasAspect( childNode, ContentModel.ASPECT_LOCKABLE))
            {
                // Get the lock type and owner
                
                String lockTypeStr = (String) getNodeService().getProperty( childNode, ContentModel.PROP_LOCK_TYPE);
                String lockOwner = null;
                
                if ( lockTypeStr != null)
                    lockOwner = (String) getNodeService().getProperty( childNode, ContentModel.PROP_LOCK_OWNER);
                
                // Pack the lock type, and owner if there is a lock on the file
                
                if ( lockTypeStr == null)
                    respBuf.putInt(IOControl.LockNone);
                else
                {
                    LockType lockType = LockType.valueOf( lockTypeStr);
                    
                    respBuf.putInt(lockType == LockType.READ_ONLY_LOCK ? IOControl.LockRead : IOControl.LockWrite);
                    respBuf.putString(lockOwner != null ? lockOwner : "", true, true);
                }
            }
            else
            {
                // File is not lockable
                
                respBuf.putInt(IOControl.LockNone);
            }
            
            // Get the content data details for the file
            
            ContentData contentData = (ContentData) getNodeService().getProperty( childNode, ContentModel.PROP_CONTENT);
            
            if ( contentData != null)
            {
                // Get the content mime-type
                
                String mimeType = contentData.getMimetype();
                
                // Pack the content length and mime-type

                respBuf.putInt( IOControl.True);
                respBuf.putLong( contentData.getSize());
                respBuf.putString( mimeType != null ? mimeType : "", true, true);
            }
            else
            {
                // File does not have any content
                
                respBuf.putInt( IOControl.False);
            }
        }
        
        // Return the response
        
        return respBuf;
    }

    /**
     * Process the get action information request
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param reqBuf Request buffer
     * @param folderNode NodeRef of parent folder
     * @param netFile NetworkFile for the folder
     * @return DataBuffer
     */
    private final DataBuffer procGetActionInfo( SrvSession sess, TreeConnection tree, DataBuffer reqBuf, NodeRef folderNode,
            NetworkFile netFile)
    {
        // Get the executable file name from the request
        
        String exeName = reqBuf.getString( true);

        if ( logger.isDebugEnabled())
        	logger.debug("  Get action info, exe=" + exeName);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
        // Get the desktop actions list
        
        DesktopActionTable deskActions = contentContext.getDesktopActions();
        if ( deskActions == null)
        {
        	respBuf.putInt(DesktopAction.StsNoSuchAction);
        	return respBuf;
        }
        
        // Convert the executable name to an action name
        
        DesktopAction deskAction = deskActions.getActionViaPseudoName(exeName);
        if ( deskAction == null)
        {
        	respBuf.putInt(DesktopAction.StsNoSuchAction);
        	return respBuf;
        }
        
        // Return the desktop action details
        
        respBuf.putInt(DesktopAction.StsSuccess);
        respBuf.putString(deskAction.getName(), true);
        respBuf.putInt(deskAction.getAttributes());
        respBuf.putInt(deskAction.getPreProcessActions());
        
        String confirmStr = deskAction.getConfirmationString();
        respBuf.putString(confirmStr != null ? confirmStr : "", true);
        
        // Return the response
        
        return respBuf;
    }
    
    /**
     * Process the run action request
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param reqBuf Request buffer
     * @param folderNode NodeRef of parent folder
     * @param netFile NetworkFile for the folder
     * @return DataBuffer
     */
    private final DataBuffer procRunAction( SrvSession sess, TreeConnection tree, DataBuffer reqBuf, NodeRef folderNode,
            NetworkFile netFile)
    {
    	// Get the name of the action to run
    	
    	String actionName = reqBuf.getString(true);
    	
    	if ( logger.isDebugEnabled())
    		logger.debug("  Run action, name=" + actionName);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
    	// Find the action handler
    	
        DesktopActionTable deskActions = contentContext.getDesktopActions();
        DesktopAction action = null;
        
        if ( deskActions != null)
        	action = deskActions.getAction(actionName);

        if ( action == null)
        {
        	respBuf.putInt(DesktopAction.StsNoSuchAction);
        	respBuf.putString("", true);
        	return respBuf;
        }

        // Start a transaction
        
        sess.beginTransaction( getTransactionService(), true);
        
        // Get the list of targets for the action
        
        int targetCnt = reqBuf.getInt();
        DesktopParams deskParams = new DesktopParams(sess, folderNode, netFile);
        
        while ( reqBuf.getAvailableLength() > 4 && targetCnt > 0)
        {
        	// Get the desktop target details
        	
        	int typ = reqBuf.getInt();
        	String path = reqBuf.getString(true);
        	
        	DesktopTarget target = new DesktopTarget(typ, path);
        	deskParams.addTarget(target);
        	
        	// Find the node for the target path
        	
            NodeRef childNode = null;
            
            try
            {
            	// Check if the target path is relative to the folder we are working in or the root of the filesystem
            	
            	if ( path.startsWith("\\"))
            	{
            		// Path is relative to the root of the filesystem
            		
            		childNode = getCifsHelper().getNodeRef(contentContext.getRootNode(), path);
            	}
            	else
            	{
            		// Path is relative to the folder we are working in
            	
            		childNode = getCifsHelper().getNodeRef( folderNode, path);
            	}
            }
            catch (FileNotFoundException ex)
            {
            }

            // If the node is not valid then return an error status
            
            if (childNode != null)
            {
            	// Set the node ref for the target
            	
            	target.setNode(childNode);
            }
            else
            {
            	// Build an error response
            	
            	respBuf.putInt(DesktopAction.StsFileNotFound);
            	respBuf.putString("Cannot find noderef for path " + path, true);
            	
            	return respBuf;
            }
            
        	// Update the target count
        	
        	targetCnt--;
        }
        
        // DEBUG
        
        if (logger.isDebugEnabled())
        {
        	logger.debug("    Desktop params: " + deskParams.numberOfTargetNodes());
        	for ( int i = 0; i < deskParams.numberOfTargetNodes(); i++) {
        		DesktopTarget target = deskParams.getTarget(i);
        		logger.debug("      " + target);
        	}
        }
        
        // Run the desktop action
        
        DesktopResponse deskResponse = null;
        
        try
        {
        	// Run the desktop action
        	
        	deskResponse = action.runAction(deskParams);
        }
        catch (Exception ex)
        {
        	// Create an error response
        	
        	deskResponse = new DesktopResponse(DesktopAction.StsError, ex.getMessage());
        }
        
        // Pack the action response
        
        if ( deskResponse != null)
        {
        	// Pack the status
        	
        	respBuf.putInt(deskResponse.getStatus());
        	respBuf.putString(deskResponse.hasStatusMessage() ? deskResponse.getStatusMessage() : "", true);
        }
        else
        {
        	// Pack an error response
        	
        	respBuf.putInt(DesktopAction.StsError);
        	respBuf.putString("Action did not return response", true);
        }
        
        // Return the response
        
    	return respBuf;
    }
}
