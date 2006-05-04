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
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.IOControlNotImplementedException;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.NTIOCtl;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.server.repo.CifsHelper;
import org.alfresco.filesys.smb.server.repo.ContentDiskDriver;
import org.alfresco.filesys.smb.server.repo.IOControlHandler;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
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
    
    // Services and helpers
    
    private CifsHelper cifsHelper;
    private TransactionService transactionService;
    private NodeService nodeService;
    private CheckOutCheckInService checkInOutService;
    
    private ContentDiskDriver contentDriver;
    
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
     * @param cifsHelper CifsHelper
     * @param transService TransactionService
     * @param nodeService NodeService
     * @param cociService CheckOutCheckInService
     */
    public void initialize( ContentDiskDriver contentDriver, CifsHelper cifsHelper,
        TransactionService transService, NodeService nodeService, CheckOutCheckInService cociService)
    {
        this.contentDriver = contentDriver;
        this.cifsHelper = cifsHelper;
        this.transactionService = transService;
        this.nodeService = nodeService;
        this.checkInOutService = cociService;
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
            
            if ( cifsHelper.isDirectory( folderNode) == false)
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
        
        if ( logger.isInfoEnabled()) {
            logger.info("IO control func=0x" + Integer.toHexString(ioFunc) + ", fid=" + fid + ", buffer=" + dataBuf);
            logger.info("  Folder nodeRef=" + folderNode);
        }

        // Check if the I/O control code is one of our custom codes

        DataBuffer retBuffer = null;
        
        switch ( ioFunc)
        {
        // Probe to check if this is an Alfresco CIFS server
        
        case IOControl.CmdProbe:
            
            // Return a buffer with the signature
            
            retBuffer = new DataBuffer(IOControl.Signature.length());
            retBuffer.putFixedString(IOControl.Signature, IOControl.Signature.length());
            retBuffer.putInt(IOControl.StsSuccess);
            break;
            
        // Get file information for a file within the current folder
            
        case IOControl.CmdFileStatus:

            // Process the file status request
            
            retBuffer = procIOFileStatus( sess, tree, dataBuf, folderNode);
            break;
            
        // Check-in file request
            
        case IOControl.CmdCheckIn:
            
            // Process the check-in request
            
            retBuffer = procIOCheckIn( sess, tree, dataBuf, folderNode, netFile);
            break;
            
        // Check-out file request
            
        case IOControl.CmdCheckOut:
            
            // Process the check-out request
            
            retBuffer = procIOCheckOut( sess, tree, dataBuf, folderNode, netFile);
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
        
        sess.beginTransaction( transactionService, true);
        
        // Get the file name from the request
        
        String fName = reqBuf.getString( true);
        logger.info("  File status, fname=" + fName);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
        // Get the node for the file/folder
        
        NodeRef childNode = null;
        
        try
        {
            childNode = cifsHelper.getNodeRef( folderNode, fName);
        }
        catch (FileNotFoundException ex)
        {
        }

        // Check if the file/folder was found
        
        if ( childNode == null)
        {
            // Return an error response
            
            respBuf.putInt(IOControl.StsFileNotFound);
            return respBuf;
        }

        // Check if this is a file or folder node
        
        if ( cifsHelper.isDirectory( childNode))
        {
            // Only return the status and node type for folders
            
            respBuf.putInt(IOControl.StsSuccess);
            respBuf.putInt(IOControl.TypeFolder);
        }
        else
        {
            // Indicate that this is a file node
            
            respBuf.putInt(IOControl.StsSuccess);
            respBuf.putInt(IOControl.TypeFile);

            // Check if this file is a working copy
            
            if ( nodeService.hasAspect( childNode, ContentModel.ASPECT_WORKING_COPY))
            {
                // Indicate that this is a working copy
                
                respBuf.putInt(IOControl.True);
                
                // Get the owner username and file it was copied from
                
                String owner = (String) nodeService.getProperty( childNode, ContentModel.PROP_WORKING_COPY_OWNER);
                String copiedFrom = null;
                
                if ( nodeService.hasAspect( childNode, ContentModel.ASPECT_COPIEDFROM))
                {
                    // Get the path of the file the working copy was generated from
                    
                    NodeRef fromNode = (NodeRef) nodeService.getProperty( childNode, ContentModel.PROP_COPY_REFERENCE);
                    if ( fromNode != null)
                        copiedFrom = (String) nodeService.getProperty( fromNode, ContentModel.PROP_NAME);
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
            
            if ( nodeService.hasAspect( childNode, ContentModel.ASPECT_LOCKABLE))
            {
                // Get the lock type and owner
                
                String lockTypeStr = (String) nodeService.getProperty( childNode, ContentModel.PROP_LOCK_TYPE);
                String lockOwner = null;
                
                if ( lockTypeStr != null)
                    lockOwner = (String) nodeService.getProperty( childNode, ContentModel.PROP_LOCK_OWNER);
                
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
            
            ContentData contentData = (ContentData) nodeService.getProperty( childNode, ContentModel.PROP_CONTENT);
            
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
     * Process the check in I/O request
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param reqBuf Request buffer
     * @param folderNode NodeRef of parent folder
     * @param netFile NetworkFile for the folder
     * @return DataBuffer
     */
    private final DataBuffer procIOCheckIn( SrvSession sess, TreeConnection tree, DataBuffer reqBuf, NodeRef folderNode,
            NetworkFile netFile)
    {
        // Start a transaction
        
        sess.beginTransaction( transactionService, false);
        
        // Get the file name from the request
        
        String fName = reqBuf.getString( true);
        boolean keepCheckedOut = reqBuf.getInt() == IOControl.True ? true : false;
        
        logger.info("  CheckIn, fname=" + fName + ", keepCheckedOut=" + keepCheckedOut);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
        // Get the node for the file/folder
        
        NodeRef childNode = null;
        
        try
        {
            childNode = cifsHelper.getNodeRef( folderNode, fName);
        }
        catch (FileNotFoundException ex)
        {
        }

        // Check if the file/folder was found
        
        if ( childNode == null)
        {
            // Return an error response
            
            respBuf.putInt(IOControl.StsFileNotFound);
            return respBuf;
        }

        // Check if this is a file or folder node
        
        if ( cifsHelper.isDirectory( childNode))
        {
            // Return an error status, attempt to check in a folder
            
            respBuf.putInt(IOControl.StsBadParameter);
        }
        else
        {
            // Check if this file is a working copy
            
            if ( nodeService.hasAspect( childNode, ContentModel.ASPECT_WORKING_COPY))
            {
                try
                {
                    // Check in the file
                    
                    checkInOutService.checkin( childNode, null, null, keepCheckedOut);

                    // Check in was successful
                    
                    respBuf.putInt( IOControl.StsSuccess);
                    
                    // Check if there are any file/directory change notify requests active

                    DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                    if (diskCtx.hasChangeHandler()) {
                        
                        // Build the relative path to the checked in file

                        String fileName = FileName.buildPath( netFile.getFullName(), null, fName, FileName.DOS_SEPERATOR);
                        
                        // Queue a file deleted change notification
                        
                        diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, fileName);
                    }
                }
                catch (Exception ex)
                {
                    // Return an error status and message
                    
                    respBuf.setPosition( IOControl.Signature.length());
                    respBuf.putInt(IOControl.StsError);
                    respBuf.putString( ex.getMessage(), true, true);
                }
            }
            else
            {
                // Not a working copy
                
                respBuf.putInt(IOControl.StsNotWorkingCopy);
            }
        }
        
        // Return the response
        
        return respBuf;
    }    

    /**
     * Process the check out I/O request
     * 
     * @param sess Server session
     * @param tree Tree connection
     * @param reqBuf Request buffer
     * @param folderNode NodeRef of parent folder
     * @param netFile NetworkFile for the folder
     * @return DataBuffer
     */
    private final DataBuffer procIOCheckOut( SrvSession sess, TreeConnection tree, DataBuffer reqBuf, NodeRef folderNode,
            NetworkFile netFile)
    {
        // Start a transaction
        
        sess.beginTransaction( transactionService, false);
        
        // Get the file name from the request
        
        String fName = reqBuf.getString( true);
        
        logger.info("  CheckOut, fname=" + fName);

        // Create a response buffer
        
        DataBuffer respBuf = new DataBuffer(256);
        respBuf.putFixedString(IOControl.Signature, IOControl.Signature.length());
        
        // Get the node for the file/folder
        
        NodeRef childNode = null;
        
        try
        {
            childNode = cifsHelper.getNodeRef( folderNode, fName);
        }
        catch (FileNotFoundException ex)
        {
        }

        // Check if the file/folder was found
        
        if ( childNode == null)
        {
            // Return an error response
            
            respBuf.putInt(IOControl.StsFileNotFound);
            return respBuf;
        }

        // Check if this is a file or folder node
        
        if ( cifsHelper.isDirectory( childNode))
        {
            // Return an error status, attempt to check in a folder
            
            respBuf.putInt(IOControl.StsBadParameter);
        }
        else
        {
            try
            {
                // Check out the file
                
                NodeRef workingCopyNode = checkInOutService.checkout( childNode);

                // Get the working copy file name
                
                String workingCopyName = (String) nodeService.getProperty( workingCopyNode, ContentModel.PROP_NAME);
                
                // Check out was successful, pack the working copy name
                
                respBuf.putInt( IOControl.StsSuccess);
                respBuf.putString( workingCopyName, true, true);
                
                // Check if there are any file/directory change notify requests active

                DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                if (diskCtx.hasChangeHandler()) {
                    
                    // Build the relative path to the checked in file

                    String fileName = FileName.buildPath( netFile.getFullName(), null, workingCopyName, FileName.DOS_SEPERATOR);
                    
                    // Queue a file added change notification
                    
                    diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, fileName);
                }
            }
            catch (Exception ex)
            {
                // Return an error status and message
                
                respBuf.setPosition( IOControl.Signature.length());
                respBuf.putInt(IOControl.StsError);
                respBuf.putString( ex.getMessage(), true, true);
            }
        }
        
        // Return the response
        
        return respBuf;
    }    
}
