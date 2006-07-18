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
package org.alfresco.filesys.smb.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.filesys.locking.FileLock;
import org.alfresco.filesys.locking.LockConflictException;
import org.alfresco.filesys.locking.NotLockedException;
import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.InvalidUserException;
import org.alfresco.filesys.server.auth.acl.AccessControl;
import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.core.InvalidDeviceInterfaceException;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.DirectoryNotEmptyException;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskFullException;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileAccess;
import org.alfresco.filesys.server.filesys.FileAction;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileExistsException;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileOfflineException;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileSharingException;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.FileSystem;
import org.alfresco.filesys.server.filesys.IOControlNotImplementedException;
import org.alfresco.filesys.server.filesys.IOCtlInterface;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.server.filesys.PathNotFoundException;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.TooManyConnectionsException;
import org.alfresco.filesys.server.filesys.TooManyFilesException;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.filesys.UnsupportedInfoLevelException;
import org.alfresco.filesys.server.filesys.VolumeInfo;
import org.alfresco.filesys.server.locking.FileLockingInterface;
import org.alfresco.filesys.server.locking.LockManager;
import org.alfresco.filesys.smb.DataType;
import org.alfresco.filesys.smb.FileInfoLevel;
import org.alfresco.filesys.smb.FindFirstNext;
import org.alfresco.filesys.smb.InvalidUNCPathException;
import org.alfresco.filesys.smb.LockingAndX;
import org.alfresco.filesys.smb.NTIOCtl;
import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.smb.PCShare;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBDate;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.WinNT;
import org.alfresco.filesys.smb.server.notify.NotifyChangeEventList;
import org.alfresco.filesys.smb.server.notify.NotifyChangeHandler;
import org.alfresco.filesys.smb.server.notify.NotifyRequest;
import org.alfresco.filesys.smb.server.ntfs.NTFSStreamsInterface;
import org.alfresco.filesys.smb.server.ntfs.StreamInfoList;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.WildCard;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NT SMB Protocol Handler Class
 * <p>
 * The NT protocol handler processes the additional SMBs that were added to the protocol in the NT
 * SMB dialect.
 */
public class NTProtocolHandler extends CoreProtocolHandler
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Constants
    //
    // Flag to enable returning of '.' and '..' directory information in FindFirst request

    public static final boolean ReturnDotFiles = true;

    // Flag to enable faking of oplock requests when opening files

    public static final boolean FakeOpLocks = false;

    // Number of write requests per file to report file size change notifications

    public static final int FileSizeChangeRate = 10;

    // Security descriptor to allow Everyone access, returned by the QuerySecurityDescrptor NT
    // transaction when NTFS streams are enabled for a virtual filesystem.

    private static byte[] _sdEveryOne = { 0x01, 0x00, 0x04, (byte) 0x80, 0x14, 0x00, 0x00, 0x00,
                                          0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                          0x2c, 0x00, 0x00, 0x00, 0x01, 0x01, 0x00, 0x00,
                                          0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                                          0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
                                          0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x1c, 0x00,
                                          0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x14, 0x00,
                                          (byte) 0xff, 0x01, 0x1f, 0x00, 0x01, 0x01, 0x00, 0x00,
                                          0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00
    };

    /**
     * Class constructor.
     */
    protected NTProtocolHandler()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param sess SMBSrvSession
     */
    protected NTProtocolHandler(SMBSrvSession sess)
    {
        super(sess);
    }

    /**
     * Return the protocol name
     * 
     * @return String
     */
    public String getName()
    {
        return "NT";
    }

    /**
     * Run the NT SMB protocol handler to process the received SMB packet
     * 
     * @exception IOException
     * @exception SMBSrvException
     * @exception TooManyConnectionsException
     */
    public boolean runProtocol() throws java.io.IOException, SMBSrvException, TooManyConnectionsException
    {

        // Check if the SMB packet is initialized

        if (m_smbPkt == null)
            m_smbPkt = m_sess.getReceivePacket();

        // Check if the received packet has a valid SMB signature

        if (m_smbPkt.checkPacketSignature() == false)
            throw new IOException("Invalid SMB signature");

        // Determine if the request has a chained command, if so then we will copy the incoming
        // request so that
        // a chained reply can be built.

        SMBSrvPacket outPkt = m_smbPkt;
        boolean chainedCmd = hasChainedCommand(m_smbPkt);

        if (chainedCmd)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STATE))
                logger.debug("AndX Command = 0x" + Integer.toHexString(m_smbPkt.getAndXCommand()));

            // Copy the request packet into a new packet for the reply

            outPkt = new SMBSrvPacket(m_smbPkt, m_smbPkt.getPacketLength());
        }

        // Reset the byte unpack offset

        m_smbPkt.resetBytePointer();

        // Set the process id from the received packet, this can change for the same session and
        // needs to be set
        // for lock ownership checking

        m_sess.setProcessId(m_smbPkt.getProcessId());

        // Determine the SMB command type

        boolean handledOK = true;

        switch (m_smbPkt.getCommand())
        {

        // NT Session setup

        case PacketType.SessionSetupAndX:
            procSessionSetup(outPkt);
            break;

        // Tree connect

        case PacketType.TreeConnectAndX:
            procTreeConnectAndX(outPkt);
            break;

        // Transaction/transaction2

        case PacketType.Transaction:
        case PacketType.Transaction2:
            procTransact2(outPkt);
            break;

        // Transaction/transaction2 secondary

        case PacketType.TransactionSecond:
        case PacketType.Transaction2Second:
            procTransact2Secondary(outPkt);
            break;

        // Close a search started via the FindFirst transaction2 command

        case PacketType.FindClose2:
            procFindClose(outPkt);
            break;

        // Open a file

        case PacketType.OpenAndX:
            procOpenAndX(outPkt);
            break;

        // Close a file

        case PacketType.CloseFile:
            procCloseFile(outPkt);
            break;

        // Read a file

        case PacketType.ReadAndX:
            procReadAndX(outPkt);
            break;

        // Write to a file

        case PacketType.WriteAndX:
            procWriteAndX(outPkt);
            break;

        // Rename file

        case PacketType.RenameFile:
            procRenameFile(outPkt);
            break;

        // Delete file

        case PacketType.DeleteFile:
            procDeleteFile(outPkt);
            break;

        // Delete directory

        case PacketType.DeleteDirectory:
            procDeleteDirectory(outPkt);
            break;

        // Tree disconnect

        case PacketType.TreeDisconnect:
            procTreeDisconnect(outPkt);
            break;

        // Lock/unlock regions of a file

        case PacketType.LockingAndX:
            procLockingAndX(outPkt);
            break;

        // Logoff a user

        case PacketType.LogoffAndX:
            procLogoffAndX(outPkt);
            break;

        // NT Create/open file

        case PacketType.NTCreateAndX:
            procNTCreateAndX(outPkt);
            break;

        // Tree connection (without AndX batching)

        case PacketType.TreeConnect:
            super.runProtocol();
            break;

        // NT cancel

        case PacketType.NTCancel:
            procNTCancel(outPkt);
            break;

        // NT transaction

        case PacketType.NTTransact:
            procNTTransaction(outPkt);
            break;

        // NT transaction secondary

        case PacketType.NTTransactSecond:
            procNTTransactionSecondary(outPkt);
            break;

        // Echo request

        case PacketType.Echo:
            super.procEcho(outPkt);
            break;

        // Default

        default:

            // Get the tree connection details, if it is a disk or printer type connection then pass
            // the request to the
            // core protocol handler

            int treeId = m_smbPkt.getTreeId();
            TreeConnection conn = null;
            if (treeId != -1)
                conn = m_sess.findConnection(treeId);

            if (conn != null)
            {

                // Check if this is a disk or print connection, if so then send the request to the
                // core protocol handler

                if (conn.getSharedDevice().getType() == ShareType.DISK
                        || conn.getSharedDevice().getType() == ShareType.PRINTER)
                {

                    // Chain to the core protocol handler

                    handledOK = super.runProtocol();
                }
                else if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
                {

                    // Send the request to IPC$ remote admin handler

                    IPCHandler.processIPCRequest(m_sess, outPkt);
                    handledOK = true;
                }
            }
            break;
        }

        // Return the handled status

        return handledOK;
    }

    /**
     * Process the NT SMB session setup request.
     * 
     * @param outPkt Response SMB packet.
     */
    protected void procSessionSetup(SMBSrvPacket outPkt) throws SMBSrvException, IOException,
            TooManyConnectionsException
    {

        // Call the authenticator to process the session setup
        
        CifsAuthenticator cifsAuthenticator = m_sess.getServer().getAuthenticator();
        
        try
        {
            // Process the session setup request, build the response
            
            cifsAuthenticator.processSessionSetup( m_sess, m_smbPkt, outPkt);
        }
        catch (SMBSrvException ex)
        {
            // Return an error response to the client
            
            m_sess.sendErrorResponseSMB( ex.getNTErrorCode(), ex.getErrorCode(), ex.getErrorClass());
            return;
        }
        
        // Check if there is a chained command, or commands

        int pos = outPkt.getLength();
        
        if (m_smbPkt.hasAndXCommand() && m_smbPkt.getPosition() < m_smbPkt.getReceivedLength())
        {

            // Process any chained commands, AndX

            pos = procAndXCommands(outPkt);
            pos -= RFCNetBIOSProtocol.HEADER_LEN;
        }
        else
        {
            // Indicate that there are no chained replies

            outPkt.setAndXCommand(SMBSrvPacket.NO_ANDX_CMD);
        }

        // Send the session setup response

        m_sess.sendResponseSMB(outPkt, pos);

        // Update the session state if the response indicates a success status. A multi stage session setup
        // response returns a warning status.
        
        if ( outPkt.getLongErrorCode() == SMBStatus.NTSuccess)
        {
            // Update the session state

            m_sess.setState(SMBSrvSessionState.SMBSESSION);

            // Notify listeners that a user has logged onto the session
    
            m_sess.getSMBServer().sessionLoggedOn(m_sess);
        }
    }

    /**
     * Process the chained SMB commands (AndX).
     * 
     * @param outPkt Reply packet.
     * @return New offset to the end of the reply packet
     */
    protected final int procAndXCommands(SMBSrvPacket outPkt)
    {

        // Use the byte offset plus length to calculate the current output packet end position

        return procAndXCommands(outPkt, outPkt.getByteOffset() + outPkt.getByteCount(), null);
    }

    /**
     * Process the chained SMB commands (AndX).
     * 
     * @param outPkt Reply packet.
     * @param endPos Current end of packet position
     * @param file Current file , or null if no file context in chain
     * @return New offset to the end of the reply packet
     */
    protected final int procAndXCommands(SMBSrvPacket outPkt, int endPos, NetworkFile file)
    {

        // Get the chained command and command block offset

        int andxCmd = m_smbPkt.getAndXCommand();
        int andxOff = m_smbPkt.getParameter(1) + RFCNetBIOSProtocol.HEADER_LEN;

        // Set the initial chained command and offset

        outPkt.setAndXCommand(andxCmd);
        outPkt.setParameter(1, andxOff - RFCNetBIOSProtocol.HEADER_LEN);

        // Pointer to the last parameter block, starts with the main command parameter block

        int paramBlk = SMBSrvPacket.WORDCNT;

        // Get the current end of the reply packet offset

        int endOfPkt = endPos;
        boolean andxErr = false;

        while (andxCmd != SMBSrvPacket.NO_ANDX_CMD && andxErr == false)
        {

            // Determine the chained command type

            int prevEndOfPkt = endOfPkt;
            boolean endOfChain = false;

            switch (andxCmd)
            {

            // Tree connect

            case PacketType.TreeConnectAndX:
                endOfPkt = procChainedTreeConnectAndX(andxOff, outPkt, endOfPkt);
                break;

            // Close file

            case PacketType.CloseFile:
                endOfPkt = procChainedClose(andxOff, outPkt, endOfPkt);
                endOfChain = true;
                break;

            // Read file

            case PacketType.ReadAndX:
                endOfPkt = procChainedReadAndX(andxOff, outPkt, endOfPkt, file);
                break;

            // Chained command was not handled

            default:
                break;
            }

            // Set the next chained command details in the current parameter block

            outPkt.setAndXCommand(paramBlk, andxCmd);
            outPkt.setAndXParameter(paramBlk, 1, prevEndOfPkt - RFCNetBIOSProtocol.HEADER_LEN);

            // Check if the end of chain has been reached, if not then look for the next
            // chained command in the request. End of chain might be set if the current command
            // is not an AndX SMB command.

            if (endOfChain == false)
            {

                // Advance to the next chained command block

                andxCmd = m_smbPkt.getAndXParameter(andxOff, 0) & 0x00FF;
                andxOff = m_smbPkt.getAndXParameter(andxOff, 1);

                // Advance the current parameter block

                paramBlk = prevEndOfPkt;
            }
            else
            {

                // Indicate that the end of the command chain has been reached

                andxCmd = SMBSrvPacket.NO_ANDX_CMD;
            }

            // Check if the chained command has generated an error status

            if (outPkt.getErrorCode() != SMBStatus.Success)
                andxErr = true;
        }

        // Return the offset to the end of the reply packet

        return endOfPkt;
    }

    /**
     * Process a chained tree connect request.
     * 
     * @return New end of reply offset.
     * @param cmdOff int Offset to the chained command within the request packet.
     * @param outPkt SMBSrvPacket Reply packet.
     * @param endOff int Offset to the current end of the reply packet.
     */
    protected final int procChainedTreeConnectAndX(int cmdOff, SMBSrvPacket outPkt, int endOff)
    {

        // Extract the parameters

//        int flags = m_smbPkt.getAndXParameter(cmdOff, 2);
        int pwdLen = m_smbPkt.getAndXParameter(cmdOff, 3);

        // Reset the byte pointer for data unpacking

        m_smbPkt.setBytePointer(m_smbPkt.getAndXByteOffset(cmdOff), m_smbPkt.getAndXByteCount(cmdOff));

        // Extract the password string

        String pwd = null;

        if (pwdLen > 0)
        {
            byte[] pwdByt = m_smbPkt.unpackBytes(pwdLen);
            pwd = new String(pwdByt);
        }

        // Extract the requested share name, as a UNC path

        boolean unicode = m_smbPkt.isUnicode();

        String uncPath = m_smbPkt.unpackString(unicode);
        if (uncPath == null)
        {
            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Extract the service type string

        String service = m_smbPkt.unpackString(false);
        if (service == null)
        {
            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Convert the service type to a shared device type, client may specify '?????' in which
        // case we ignore the error.

        int servType = ShareType.ServiceAsType(service);
        if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0)
        {
            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
            logger.debug("NT ANDX Tree Connect AndX - " + uncPath + ", " + service);

        // Parse the requested share name

        PCShare share = null;

        try
        {
            share = new PCShare(uncPath);
        }
        catch (InvalidUNCPathException ex)
        {
            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Map the IPC$ share to the admin pipe type

        if (servType == ShareType.NAMEDPIPE && share.getShareName().compareTo("IPC$") == 0)
            servType = ShareType.ADMINPIPE;

        // Check if the session is a null session, only allow access to the IPC$ named pipe share

        if (m_sess.hasClientInformation() && m_sess.getClientInformation().isNullSession()
                && servType != ShareType.ADMINPIPE)
        {

            // Return an error status

            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied,
                    SMBStatus.ErrDos);
            return endOff;
        }

        // Find the requested shared device

        SharedDevice shareDev = null;

        try
        {

            // Get/create the shared device

            shareDev = m_sess.getSMBServer().findShare(share.getNodeName(), share.getShareName(), servType, m_sess,
                    true);
        }
        catch (InvalidUserException ex)
        {

            // Return a logon failure status

            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied,
                    SMBStatus.ErrDos);
            return endOff;
        }
        catch (Exception ex)
        {

            // Log the generic error

            logger.error("Exception in TreeConnectAndX", ex);

            // Return a general status, bad network name

            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTBadNetName, SMBStatus.SRVInvalidNetworkName,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Check if the share is valid

        if (shareDev == null || (servType != ShareType.UNKNOWN && shareDev.getType() != servType))
        {

            // Set the error status

            outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTBadNetName, SMBStatus.SRVInvalidNetworkName,
                    SMBStatus.ErrSrv);
            return endOff;
        }

        // Authenticate the share connect, if the server is using share mode security

        CifsAuthenticator auth = getSession().getSMBServer().getAuthenticator();
        int sharePerm = FileAccess.Writeable;

        if (auth != null)
        {

            // Validate the share connection

            sharePerm = auth.authenticateShareConnect(m_sess.getClientInformation(), shareDev, pwd, m_sess);
            if (sharePerm < 0)
            {

                // Invalid share connection request

                outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied,
                        SMBStatus.ErrDos);
                return endOff;
            }
        }

        // Check if there is an access control manager, if so then run any access controls to
        // determine the
        // sessions access to the share.

        if (getSession().getServer().hasAccessControlManager() && shareDev.hasAccessControls())
        {

            // Get the access control manager

            AccessControlManager aclMgr = getSession().getServer().getAccessControlManager();

            // Update the access permission for this session by processing the access control list
            // for the
            // shared device

            int aclPerm = aclMgr.checkAccessControl(getSession(), shareDev);

            if (aclPerm == FileAccess.NoAccess)
            {

                // Invalid share connection request

                outPkt.setError(m_smbPkt.isLongErrorCode(), SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied,
                        SMBStatus.ErrDos);
                return endOff;
            }

            // If the access controls returned a new access type update the main permission

            if (aclPerm != AccessControl.Default)
                sharePerm = aclPerm;
        }

        // Allocate a tree id for the new connection

        TreeConnection tree = null;
        
        try
        {

            // Allocate the tree id for this connection

            int treeId = m_sess.addConnection(shareDev);
            outPkt.setTreeId(treeId);

            // Set the file permission that this user has been granted for this share

            tree = m_sess.findConnection(treeId);
            tree.setPermission(sharePerm);

            // Inform the driver that a connection has been opened

            if (tree.getInterface() != null)
                tree.getInterface().treeOpened(m_sess, tree);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
                logger.debug("ANDX Tree Connect AndX - Allocated Tree Id = " + treeId);
        }
        catch (TooManyConnectionsException ex)
        {

            // Too many connections open at the moment

            outPkt.setError(SMBStatus.SRVNoResourcesAvailable, SMBStatus.ErrSrv);
            return endOff;
        }

        // Build the tree connect response

        outPkt.setAndXParameterCount(endOff, 2);
        outPkt.setAndXParameter(endOff, 0, SMBSrvPacket.NO_ANDX_CMD);
        outPkt.setAndXParameter(endOff, 1, 0);

        // Pack the service type

        int pos = outPkt.getAndXByteOffset(endOff);
        byte[] outBuf = outPkt.getBuffer();
        pos = DataPacker.putString(ShareType.TypeAsService(shareDev.getType()), outBuf, pos, true);

        // Determine the filesystem type, for disk shares

        String devType = "";

        try
        {
            // Check if this is a disk shared device
            
            if ( shareDev.getType() == ShareType.DISK)
            {
                // Check if the filesystem driver implements the NTFS streams interface, and streams are
                // enabled
    
                if (shareDev.getInterface() instanceof NTFSStreamsInterface)
                {
    
                    // Check if NTFS streams are enabled
    
                    NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) shareDev.getInterface();
                    if (ntfsStreams.hasStreamsEnabled(m_sess, tree))
                        devType = FileSystem.TypeNTFS;
                }
                else
                {
                    // Get the filesystem type from the context

                    DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                    devType = diskCtx.getFilesystemType();
                }
            }
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Log the error

            logger.error("TreeConnectAndX error", ex);
        }

        // Pack the filesystem type
        
        pos = DataPacker.putString(devType, outBuf, pos, true, outPkt.isUnicode());
        
        int bytLen = pos - outPkt.getAndXByteOffset(endOff);
        outPkt.setAndXByteCount(endOff, bytLen);

        // Return the new end of packet offset

        return pos;
    }

    /**
     * Process a chained read file request
     * 
     * @param cmdOff Offset to the chained command within the request packet.
     * @param outPkt Reply packet.
     * @param endOff Offset to the current end of the reply packet.
     * @param netFile File to be read, passed down the chained requests
     * @return New end of reply offset.
     */
    protected final int procChainedReadAndX(int cmdOff, SMBSrvPacket outPkt, int endOff, NetworkFile netFile)
    {

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return endOff;
        }

        // Extract the read file parameters

        long offset = (long) m_smbPkt.getAndXParameterLong(cmdOff, 3); // bottom 32bits of read
                                                                        // offset
        offset &= 0xFFFFFFFFL;
        int maxCount = m_smbPkt.getAndXParameter(cmdOff, 5);

        // Check for the NT format request that has the top 32bits of the file offset

        if (m_smbPkt.getAndXParameterCount(cmdOff) == 12)
        {
            long topOff = (long) m_smbPkt.getAndXParameterLong(cmdOff, 10);
            offset += topOff << 32;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Chained File Read AndX : Size=" + maxCount + " ,Pos=" + offset);

        // Read data from the file

        byte[] buf = outPkt.getBuffer();
        int dataPos = 0;
        int rdlen = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Set the returned parameter count so that the byte offset can be calculated

            outPkt.setAndXParameterCount(endOff, 12);
            dataPos = outPkt.getAndXByteOffset(endOff);
            dataPos = DataPacker.wordAlign(dataPos); // align the data buffer

            // Check if the requested data length will fit into the buffer

            int dataLen = buf.length - dataPos;
            if (dataLen < maxCount)
                maxCount = dataLen;

            // Read from the file

            rdlen = disk.readFile(m_sess, conn, netFile, buf, dataPos, maxCount, offset);

            // Return the data block

            outPkt.setAndXParameter(endOff, 0, SMBSrvPacket.NO_ANDX_CMD);
            outPkt.setAndXParameter(endOff, 1, 0);

            outPkt.setAndXParameter(endOff, 2, 0); // bytes remaining, for pipes only
            outPkt.setAndXParameter(endOff, 3, 0); // data compaction mode
            outPkt.setAndXParameter(endOff, 4, 0); // reserved
            outPkt.setAndXParameter(endOff, 5, rdlen); // data length
            outPkt.setAndXParameter(endOff, 6, dataPos - RFCNetBIOSProtocol.HEADER_LEN); // offset
                                                                                            // to
                                                                                            // data

            // Clear the reserved parameters

            for (int i = 7; i < 12; i++)
                outPkt.setAndXParameter(endOff, i, 0);

            // Set the byte count

            outPkt.setAndXByteCount(endOff, (dataPos + rdlen) - outPkt.getAndXByteOffset(endOff));

            // Update the end offset for the new end of packet

            endOff = dataPos + rdlen;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return endOff;
        }
        catch (java.io.IOException ex)
        {
        }

        // Return the new end of packet offset

        return endOff;
    }

    /**
     * Process a chained close file request
     * 
     * @param cmdOff int Offset to the chained command within the request packet.
     * @param outPkt SMBSrvPacket Reply packet.
     * @param endOff int Offset to the current end of the reply packet.
     * @return New end of reply offset.
     */
    protected final int procChainedClose(int cmdOff, SMBSrvPacket outPkt, int endOff)
    {

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return endOff;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getAndXParameter(cmdOff, 0);
//        int ftime = m_smbPkt.getAndXParameter(cmdOff, 1);
//        int fdate = m_smbPkt.getAndXParameter(cmdOff, 2);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            outPkt.setError(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return endOff;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Chained File Close [" + treeId + "] fid=" + fid);

        // Close the file

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Close the file
            //
            // The disk interface may be null if the file is a named pipe file

            if (disk != null)
                disk.closeFile(m_sess, conn, netFile);

            // Indicate that the file has been closed

            netFile.setClosed(true);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            outPkt.setError(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return endOff;
        }
        catch (java.io.IOException ex)
        {
        }

        // Clear the returned parameter count and byte count

        outPkt.setAndXParameterCount(endOff, 0);
        outPkt.setAndXByteCount(endOff, 0);

        endOff = outPkt.getAndXByteOffset(endOff) - RFCNetBIOSProtocol.HEADER_LEN;

        // Remove the file from the connections list of open files

        conn.removeFile(fid, getSession());

        // Return the new end of packet offset

        return endOff;
    }

    /**
     * Process the SMB tree connect request.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     * @exception TooManyConnectionsException Too many concurrent connections on this session.
     */

    protected void procTreeConnectAndX(SMBSrvPacket outPkt) throws SMBSrvException, TooManyConnectionsException,
            java.io.IOException
    {

        // Check that the received packet looks like a valid tree connect request

        if (m_smbPkt.checkPacketIsValid(4, 3) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Extract the parameters

//        int flags = m_smbPkt.getParameter(2);
        int pwdLen = m_smbPkt.getParameter(3);

        // Initialize the byte area pointer

        m_smbPkt.resetBytePointer();

        // Determine if ASCII or unicode strings are being used

        boolean unicode = m_smbPkt.isUnicode();

        // Extract the password string

        String pwd = null;

        if (pwdLen > 0)
        {
            byte[] pwdByts = m_smbPkt.unpackBytes(pwdLen);
            pwd = new String(pwdByts);
        }

        // Extract the requested share name, as a UNC path

        String uncPath = m_smbPkt.unpackString(unicode);
        if (uncPath == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Extract the service type string, always seems to be ASCII

        String service = m_smbPkt.unpackString(false);
        if (service == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Convert the service type to a shared device type, client may specify '?????' in which
        // case we ignore the error.

        int servType = ShareType.ServiceAsType(service);
        if (servType == ShareType.UNKNOWN && service.compareTo("?????") != 0)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
            logger.debug("NT Tree Connect AndX - " + uncPath + ", " + service);

        // Parse the requested share name

        String shareName = null;
        String hostName = null;

        if (uncPath.startsWith("\\"))
        {

            try
            {
                PCShare share = new PCShare(uncPath);
                shareName = share.getShareName();
                hostName = share.getNodeName();
            }
            catch (InvalidUNCPathException ex)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                        SMBStatus.ErrSrv);
                return;
            }
        }
        else
            shareName = uncPath;

        // Map the IPC$ share to the admin pipe type

        if (servType == ShareType.NAMEDPIPE && shareName.compareTo("IPC$") == 0)
            servType = ShareType.ADMINPIPE;

        // Check if the session is a null session, only allow access to the IPC$ named pipe share

        if (m_sess.hasClientInformation() && m_sess.getClientInformation().isNullSession()
                && servType != ShareType.ADMINPIPE)
        {

            // Return an error status

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Find the requested shared device

        SharedDevice shareDev = null;

        try
        {

            // Get/create the shared device

            shareDev = m_sess.getSMBServer().findShare(hostName, shareName, servType, m_sess, true);
        }
        catch (InvalidUserException ex)
        {

            // Return a logon failure status

            m_sess.sendErrorResponseSMB(SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (Exception ex)
        {

            // Log the generic error

            logger.error("TreeConnectAndX error", ex);

            // Return a general status, bad network name

            m_sess.sendErrorResponseSMB(SMBStatus.NTBadNetName, SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
            return;
        }

        // Check if the share is valid

        if (shareDev == null || (servType != ShareType.UNKNOWN && shareDev.getType() != servType))
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTBadNetName, SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
            return;
        }

        // Authenticate the share connection depending upon the security mode the server is running
        // under

        CifsAuthenticator auth = getSession().getSMBServer().getAuthenticator();
        int sharePerm = FileAccess.Writeable;

        if (auth != null)
        {

            // Validate the share connection

            sharePerm = auth.authenticateShareConnect(m_sess.getClientInformation(), shareDev, pwd, m_sess);
            if (sharePerm < 0)
            {

                // DEBUG

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
                    logger.debug("Tree connect to " + shareName + ", access denied");

                // Invalid share connection request

                m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                return;
            }
        }

        // Check if there is an access control manager, if so then run any access controls to
        // determine the
        // sessions access to the share.

        if (getSession().getServer().hasAccessControlManager() && shareDev.hasAccessControls())
        {

            // Get the access control manager

            AccessControlManager aclMgr = getSession().getServer().getAccessControlManager();

            // Update the access permission for this session by processing the access control list
            // for the
            // shared device

            int aclPerm = aclMgr.checkAccessControl(getSession(), shareDev);

            if (aclPerm == FileAccess.NoAccess)
            {

                // Invalid share connection request

                m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                return;
            }

            // If the access controls returned a new access type update the main permission

            if (aclPerm != AccessControl.Default)
                sharePerm = aclPerm;
        }

        // Allocate a tree id for the new connection

        int treeId = m_sess.addConnection(shareDev);
        outPkt.setTreeId(treeId);

        // Set the file permission that this user has been granted for this share

        TreeConnection tree = m_sess.findConnection(treeId);
        tree.setPermission(sharePerm);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
            logger.debug("Tree Connect AndX - Allocated Tree Id = " + treeId + ", Permission = "
                    + FileAccess.asString(sharePerm));

        // Build the tree connect response

        outPkt.setParameterCount(3);
        outPkt.setAndXCommand(0xFF); // no chained reply
        outPkt.setParameter(1, 0);
        outPkt.setParameter(2, 0);

        // Pack the service type

        int pos = outPkt.getByteOffset();
        pos = DataPacker.putString(ShareType.TypeAsService(shareDev.getType()), m_smbPkt.getBuffer(), pos, true);

        // Determine the filesystem type, for disk shares

        String devType = "";

        try
        {
            // Check if this is a disk shared device
            
            if ( shareDev.getType() == ShareType.DISK)
            {
                // Check if the filesystem driver implements the NTFS streams interface, and streams are
                // enabled
    
                if (shareDev.getInterface() instanceof NTFSStreamsInterface)
                {
    
                    // Check if NTFS streams are enabled
    
                    NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) shareDev.getInterface();
                    if (ntfsStreams.hasStreamsEnabled(m_sess, tree))
                        devType = FileSystem.TypeNTFS;
                }
                else
                {
                    // Get the filesystem type from the context

                    DiskDeviceContext diskCtx = (DiskDeviceContext) tree.getContext();
                    devType = diskCtx.getFilesystemType();
                }
            }
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Log the error

            logger.error("TreeConnectAndX error", ex);
        }

        // Pack the filesystem type
        
        pos = DataPacker.putString(devType, m_smbPkt.getBuffer(), pos, true, outPkt.isUnicode());
        outPkt.setByteCount(pos - outPkt.getByteOffset());

        // Send the response

        m_sess.sendResponseSMB(outPkt);

        // Inform the driver that a connection has been opened

        if (tree.getInterface() != null)
            tree.getInterface().treeOpened(m_sess, tree);
    }

    /**
     * Close a file that has been opened on the server.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procCloseFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file close request

        if (m_smbPkt.checkPacketIsValid(3, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
//        int ftime = m_smbPkt.getParameter(1);
//        int fdate = m_smbPkt.getParameter(2);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File close [" + treeId + "] fid=" + fid);

        // Close the file

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Close the file
            //
            // The disk interface may be null if the file is a named pipe file

            if (disk != null)
                disk.closeFile(m_sess, conn, netFile);

            // Indicate that the file has been closed

            netFile.setClosed(true);
        }
        catch (AccessDeniedException ex)
        {
            // Not allowed to delete the file, when delete on close flag is set

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {
        }

        // Remove the file from the connections list of open files

        conn.removeFile(fid, getSession());

        // Build the close file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (netFile.getWriteCount() > 0 && diskCtx.hasChangeHandler())
            diskCtx.getChangeHandler().notifyFileSizeChanged(netFile.getFullName());

        if (netFile.hasDeleteOnClose() && diskCtx.hasChangeHandler())
            diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, netFile.getFullName());
    }

    /**
     * Process a transact2 request. The transact2 can contain many different sub-requests.
     * 
     * @param outPkt SMBSrvPacket
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procTransact2(SMBSrvPacket outPkt) throws IOException, SMBSrvException
    {

        // Check that we received enough parameters for a transact2 request

        if (m_smbPkt.checkPacketIsValid(14, 0) == false)
        {

            // Not enough parameters for a valid transact2 request

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Create a transact packet using the received SMB packet

        SMBSrvTransPacket tranPkt = new SMBSrvTransPacket(m_smbPkt.getBuffer());

        // Create a transact buffer to hold the transaction setup, parameter and data blocks

        SrvTransactBuffer transBuf = null;
        int subCmd = tranPkt.getSubFunction();

        if (tranPkt.getTotalParameterCount() == tranPkt.getRxParameterBlockLength()
                && tranPkt.getTotalDataCount() == tranPkt.getRxDataBlockLength())
        {

            // Create a transact buffer using the packet buffer, the entire request is contained in
            // a single
            // packet

            transBuf = new SrvTransactBuffer(tranPkt);
        }
        else
        {

            // Create a transact buffer to hold the multiple transact request parameter/data blocks

            transBuf = new SrvTransactBuffer(tranPkt.getSetupCount(), tranPkt.getTotalParameterCount(), tranPkt
                    .getTotalDataCount());
            transBuf.setType(tranPkt.getCommand());
            transBuf.setFunction(subCmd);

            // Append the setup, parameter and data blocks to the transaction data

            byte[] buf = tranPkt.getBuffer();

            transBuf.appendSetup(buf, tranPkt.getSetupOffset(), tranPkt.getSetupCount() * 2);
            transBuf.appendParameter(buf, tranPkt.getRxParameterBlock(), tranPkt.getRxParameterBlockLength());
            transBuf.appendData(buf, tranPkt.getRxDataBlock(), tranPkt.getRxDataBlockLength());
        }

        // Set the return data limits for the transaction

        transBuf.setReturnLimits(tranPkt.getMaximumReturnSetupCount(), tranPkt.getMaximumReturnParameterCount(),
                tranPkt.getMaximumReturnDataCount());

        // Check for a multi-packet transaction, for a multi-packet transaction we just acknowledge
        // the receive with
        // an empty response SMB

        if (transBuf.isMultiPacket())
        {

            // Save the partial transaction data

            m_sess.setTransaction(transBuf);

            // Send an intermediate acknowedgement response

            m_sess.sendSuccessResponseSMB();
            return;
        }

        // Check if the transaction is on the IPC$ named pipe, the request requires special
        // processing

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {
            IPCHandler.procTransaction(transBuf, m_sess, outPkt);
            return;
        }

        // DEBUG

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("Transaction [" + treeId + "] tbuf=" + transBuf);

        // Process the transaction buffer

        processTransactionBuffer(transBuf, outPkt);
    }

    /**
     * Process a transact2 secondary request.
     * 
     * @param outPkt SMBSrvPacket
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procTransact2Secondary(SMBSrvPacket outPkt) throws IOException, SMBSrvException
    {

        // Check that we received enough parameters for a transact2 request

        if (m_smbPkt.checkPacketIsValid(8, 0) == false)
        {

            // Not enough parameters for a valid transact2 request

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Check if there is an active transaction, and it is an NT transaction

        if (m_sess.hasTransaction() == false
                || (m_sess.getTransaction().isType() == PacketType.Transaction && m_smbPkt.getCommand() != PacketType.TransactionSecond)
                || (m_sess.getTransaction().isType() == PacketType.Transaction2 && m_smbPkt.getCommand() != PacketType.Transaction2Second))
        {

            // No transaction to continue, or packet does not match the existing transaction, return
            // an error

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Create an NT transaction using the received packet

        SMBSrvTransPacket tpkt = new SMBSrvTransPacket(m_smbPkt.getBuffer());
        byte[] buf = tpkt.getBuffer();
        SrvTransactBuffer transBuf = m_sess.getTransaction();

        // Append the parameter data to the transaction buffer, if any

        int plen = tpkt.getSecondaryParameterBlockCount();
        if (plen > 0)
        {

            // Append the data to the parameter buffer

            DataBuffer paramBuf = transBuf.getParameterBuffer();
            paramBuf.appendData(buf, tpkt.getSecondaryParameterBlockOffset(), plen);
        }

        // Append the data block to the transaction buffer, if any

        int dlen = tpkt.getSecondaryDataBlockCount();
        if (dlen > 0)
        {

            // Append the data to the data buffer

            DataBuffer dataBuf = transBuf.getDataBuffer();
            dataBuf.appendData(buf, tpkt.getSecondaryDataBlockOffset(), dlen);
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("Transaction Secondary [" + treeId + "] paramLen=" + plen + ", dataLen=" + dlen);

        // Check if the transaction has been received or there are more sections to be received

        int totParam = tpkt.getTotalParameterCount();
        int totData = tpkt.getTotalDataCount();

        int paramDisp = tpkt.getParameterBlockDisplacement();
        int dataDisp = tpkt.getDataBlockDisplacement();

        if ((paramDisp + plen) == totParam && (dataDisp + dlen) == totData)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
                logger.debug("Transaction complete, processing ...");

            // Clear the in progress transaction

            m_sess.setTransaction(null);

            // Check if the transaction is on the IPC$ named pipe, the request requires special
            // processing

            if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
            {
                IPCHandler.procTransaction(transBuf, m_sess, outPkt);
                return;
            }

            // DEBUG

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
                logger.debug("Transaction second [" + treeId + "] tbuf=" + transBuf);

            // Process the transaction

            processTransactionBuffer(transBuf, outPkt);
        }
        else
        {

            // There are more transaction parameter/data sections to be received, return an
            // intermediate response

            m_sess.sendSuccessResponseSMB();
        }
    }

    /**
     * Process a transaction buffer
     * 
     * @param tbuf TransactBuffer
     * @param outPkt SMBSrvPacket
     * @exception IOException If a network error occurs
     * @exception SMBSrvException If an SMB error occurs
     */
    private final void processTransactionBuffer(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws IOException,
            SMBSrvException
    {

        // Get the transact2 sub-command code and process the request

        switch (tbuf.getFunction())
        {

        // Start a file search

        case PacketType.Trans2FindFirst:
            procTrans2FindFirst(tbuf, outPkt);
            break;

        // Continue a file search

        case PacketType.Trans2FindNext:
            procTrans2FindNext(tbuf, outPkt);
            break;

        // Query file system information

        case PacketType.Trans2QueryFileSys:
            procTrans2QueryFileSys(tbuf, outPkt);
            break;

        // Query path

        case PacketType.Trans2QueryPath:
            procTrans2QueryPath(tbuf, outPkt);
            break;

        // Query file information via handle

        case PacketType.Trans2QueryFile:
            procTrans2QueryFile(tbuf, outPkt);
            break;

        // Set file information via handle

        case PacketType.Trans2SetFile:
            procTrans2SetFile(tbuf, outPkt);
            break;

        // Set file information via path

        case PacketType.Trans2SetPath:
            procTrans2SetPath(tbuf, outPkt);
            break;

        // Unknown transact2 command

        default:

            // Return an unrecognized command error

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            break;
        }
    }

    /**
     * Close a search started via the transact2 find first/next command.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procFindClose(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid find close request

        if (m_smbPkt.checkPacketIsValid(1, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Get the search id

        int searchId = m_smbPkt.getParameter(0);

        // Get the search context

        SearchContext ctx = m_sess.getSearchContext(searchId);

        if (ctx == null)
        {

            // Invalid search handle

            m_sess.sendSuccessResponseSMB();
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
            logger.debug("Close trans search [" + searchId + "]");

        // Deallocate the search slot, close the search.

        m_sess.deallocateSearchSlot(searchId);

        // Return a success status SMB

        m_sess.sendSuccessResponseSMB();
    }

    /**
     * Process the file lock/unlock request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procLockingAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid locking andX request

        if (m_smbPkt.checkPacketIsValid(8, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Extract the file lock/unlock parameters

        int fid = m_smbPkt.getParameter(2);
        int lockType = m_smbPkt.getParameter(3);
        long lockTmo = m_smbPkt.getParameterLong(4);
        int unlockCnt = m_smbPkt.getParameter(6);
        int lockCnt = m_smbPkt.getParameter(7);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.Win32InvalidHandle, SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_LOCK))
            logger.debug("File Lock [" + netFile.getFileId() + "] : type=0x" + Integer.toHexString(lockType) + ", tmo="
                    + lockTmo + ", locks=" + lockCnt + ", unlocks=" + unlockCnt);

        DiskInterface disk = null;
        try
        {

            // Get the disk interface for the share

            disk = (DiskInterface) conn.getSharedDevice().getInterface();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check if the virtual filesystem supports file locking

        if (disk instanceof FileLockingInterface)
        {

            // Get the lock manager

            FileLockingInterface lockInterface = (FileLockingInterface) disk;
            LockManager lockMgr = lockInterface.getLockManager(m_sess, conn);

            // Unpack the lock/unlock structures

            m_smbPkt.resetBytePointer();
            boolean largeFileLock = LockingAndX.hasLargeFiles(lockType);

            // Optimize for a single lock/unlock structure

            if ((unlockCnt + lockCnt) == 1)
            {

                // Get the unlock/lock structure

                int pid = m_smbPkt.unpackWord();
                long offset = -1;
                long length = -1;

                if (largeFileLock == false)
                {

                    // Get the lock offset and length, short format

                    offset = m_smbPkt.unpackInt();
                    length = m_smbPkt.unpackInt();
                }
                else
                {

                    // Get the lock offset and length, large format

                    m_smbPkt.skipBytes(2);

                    offset = ((long) m_smbPkt.unpackInt()) << 32;
                    offset += (long) m_smbPkt.unpackInt();

                    length = ((long) m_smbPkt.unpackInt()) << 32;
                    length += (long) m_smbPkt.unpackInt();
                }

                // Create the lock/unlock details

                FileLock fLock = lockMgr.createLockObject(m_sess, conn, netFile, offset, length, pid);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_LOCK))
                    logger.debug("  Single " + (lockCnt == 1 ? "Lock" : "UnLock") + " lock=" + fLock.toString());

                // Perform the lock/unlock request

                try
                {

                    // Check if the request is an unlock

                    if (unlockCnt > 0)
                    {

                        // Unlock the file

                        lockMgr.unlockFile(m_sess, conn, netFile, fLock);
                    }
                    else
                    {

                        // Lock the file

                        lockMgr.lockFile(m_sess, conn, netFile, fLock);
                    }
                }
                catch (NotLockedException ex)
                {

                    // Return an error status

                    m_sess.sendErrorResponseSMB(SMBStatus.DOSNotLocked, SMBStatus.ErrDos);
                    return;
                }
                catch (LockConflictException ex)
                {

                    // Return an error status

                    m_sess
                            .sendErrorResponseSMB(SMBStatus.NTLockNotGranted, SMBStatus.DOSLockConflict,
                                    SMBStatus.ErrDos);
                    return;
                }
                catch (IOException ex)
                {

                    // Return an error status

                    m_sess.sendErrorResponseSMB(SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);
                    return;
                }
            }
            else
            {

                // Unpack the lock/unlock structures

            }
        }
        else
        {

            // Return a 'not locked' status if there are unlocks in the request else return a
            // success status

            if (unlockCnt > 0)
            {

                // Return an error status

                m_sess.sendErrorResponseSMB(SMBStatus.DOSNotLocked, SMBStatus.ErrDos);
                return;
            }
        }

        // Return a success response

        outPkt.setParameterCount(2);
        outPkt.setAndXCommand(0xFF);
        outPkt.setParameter(1, 0);
        outPkt.setByteCount(0);

        // Send the lock request response

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process the logoff request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procLogoffAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Return a success status SMB

        m_sess.sendSuccessResponseSMB();
    }

    /**
     * Process the file open request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procOpenAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid open andX request

        if (m_smbPkt.checkPacketIsValid(15, 1) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // If the connection is to the IPC$ remote admin named pipe pass the request to the IPC
        // handler. If the device is
        // not a disk type device then return an error.

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {

            // Use the IPC$ handler to process the request

            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }
        else if (conn.getSharedDevice().getType() != ShareType.DISK)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Extract the open file parameters

//        int flags = m_smbPkt.getParameter(2);
        int access = m_smbPkt.getParameter(3);
        int srchAttr = m_smbPkt.getParameter(4);
        int fileAttr = m_smbPkt.getParameter(5);
        int crTime = m_smbPkt.getParameter(6);
        int crDate = m_smbPkt.getParameter(7);
        int openFunc = m_smbPkt.getParameter(8);
        int allocSiz = m_smbPkt.getParameterLong(9);

        // Extract the filename string

        String fileName = m_smbPkt.unpackString(m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Create the file open parameters

        long crDateTime = 0L;
        if (crTime > 0 && crDate > 0)
            crDateTime = new SMBDate(crDate, crTime).getTime();

        FileOpenParams params = new FileOpenParams(fileName, openFunc, access, srchAttr, fileAttr, allocSiz, crDateTime);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Open AndX [" + treeId + "] params=" + params);

        // Access the disk interface and open the requested file

        int fid;
        NetworkFile netFile = null;
        int respAction = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Check if the requested file already exists

            int fileSts = disk.fileExists(m_sess, conn, fileName);

            if (fileSts == FileStatus.NotExist)
            {

                // Check if the file should be created if it does not exist

                if (FileAction.createNotExists(openFunc))
                {

                    // Create a new file

                    netFile = disk.createFile(m_sess, conn, params);

                    // Indicate that the file did not exist and was created

                    respAction = FileAction.FileCreated;
                }
                else
                {

                    // Check if the path is a directory

                    if (fileSts == FileStatus.DirectoryExists)
                    {

                        // Return an access denied error

                        m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    }
                    else
                    {

                        // Return a file not found error

                        m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
                    }
                    return;
                }
            }
            else
            {

                // Open the requested file

                netFile = disk.openFile(m_sess, conn, params);

                // Set the file action response

                if (FileAction.truncateExistingFile(openFunc))
                    respAction = FileAction.FileTruncated;
                else
                    respAction = FileAction.FileExisted;
            }

            // Add the file to the list of open files for this tree connection

            fid = conn.addFile(netFile, getSession());

        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (TooManyFilesException ex)
        {

            // Too many files are open on this connection, cannot open any more files.

            m_sess.sendErrorResponseSMB(SMBStatus.DOSTooManyOpenFiles, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileSharingException ex)
        {

            // Return a sharing violation error

            m_sess.sendErrorResponseSMB(SMBStatus.NTSharingViolation, SMBStatus.DOSFileSharingConflict,
                    SMBStatus.ErrDos);
            return;
        }
        catch (FileOfflineException ex)
        {

            // File data is unavailable

            m_sess.sendErrorResponseSMB(SMBStatus.NTFileOffline, SMBStatus.HRDDriveNotReady, SMBStatus.ErrHrd);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the open file response

        outPkt.setParameterCount(15);

        outPkt.setAndXCommand(0xFF);
        outPkt.setParameter(1, 0); // AndX offset

        outPkt.setParameter(2, fid);
        outPkt.setParameter(3, netFile.getFileAttributes()); // file attributes

        SMBDate modDate = null;

        if (netFile.hasModifyDate())
            modDate = new SMBDate(netFile.getModifyDate());

        outPkt.setParameter(4, modDate != null ? modDate.asSMBTime() : 0); // last write time
        outPkt.setParameter(5, modDate != null ? modDate.asSMBDate() : 0); // last write date
        outPkt.setParameterLong(6, netFile.getFileSizeInt()); // file size
        outPkt.setParameter(8, netFile.getGrantedAccess());
        outPkt.setParameter(9, OpenAndX.FileTypeDisk);
        outPkt.setParameter(10, 0); // named pipe state
        outPkt.setParameter(11, respAction);
        outPkt.setParameter(12, 0); // server FID (long)
        outPkt.setParameter(13, 0);
        outPkt.setParameter(14, 0);

        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process the file read request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procReadAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid read andX request

        if (m_smbPkt.checkPacketIsValid(10, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // If the connection is to the IPC$ remote admin named pipe pass the request to the IPC
        // handler.

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {

            // Use the IPC$ handler to process the request

            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }

        // Extract the read file parameters

        int fid = m_smbPkt.getParameter(2);
        long offset = (long) m_smbPkt.getParameterLong(3); // bottom 32bits of read offset
        offset &= 0xFFFFFFFFL;
        int maxCount = m_smbPkt.getParameter(5);

        // Check for the NT format request that has the top 32bits of the file offset

        if (m_smbPkt.getParameterCount() == 12)
        {
            long topOff = (long) m_smbPkt.getParameterLong(10);
            offset += topOff << 32;
        }

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Read AndX [" + netFile.getFileId() + "] : Size=" + maxCount + " ,Pos=" + offset);

        // Read data from the file

        byte[] buf = outPkt.getBuffer();
        int dataPos = 0;
        int rdlen = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Set the returned parameter count so that the byte offset can be calculated

            outPkt.setParameterCount(12);
            dataPos = outPkt.getByteOffset();
            dataPos = DataPacker.wordAlign(dataPos); // align the data buffer

            // Check if the requested data length will fit into the buffer

            int dataLen = buf.length - dataPos;
            if (dataLen < maxCount)
                maxCount = dataLen;

            // Read from the file

            rdlen = disk.readFile(m_sess, conn, netFile, buf, dataPos, maxCount, offset);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (FileOfflineException ex)
        {

            // File data is unavailable

            m_sess.sendErrorResponseSMB(SMBStatus.NTFileOffline, SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
            return;
        }
        catch (LockConflictException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_LOCK))
                logger.debug("Read Lock Error [" + netFile.getFileId() + "] : Size=" + maxCount + " ,Pos=" + offset);

            // File is locked

            m_sess.sendErrorResponseSMB(SMBStatus.NTLockConflict, SMBStatus.DOSLockConflict, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // User does not have the required access rights or file is not accessible

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to read the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the data block

        outPkt.setAndXCommand(0xFF); // no chained command
        outPkt.setParameter(1, 0);
        outPkt.setParameter(2, 0); // bytes remaining, for pipes only
        outPkt.setParameter(3, 0); // data compaction mode
        outPkt.setParameter(4, 0); // reserved
        outPkt.setParameter(5, rdlen); // data length
        outPkt.setParameter(6, dataPos - RFCNetBIOSProtocol.HEADER_LEN); // offset to data

        // Clear the reserved parameters

        for (int i = 7; i < 12; i++)
            outPkt.setParameter(i, 0);

        // Set the byte count

        outPkt.setByteCount((dataPos + rdlen) - outPkt.getByteOffset());

        // Check if there is a chained command, or commands

        if (m_smbPkt.hasAndXCommand())
        {

            // Process any chained commands, AndX

            int pos = procAndXCommands(outPkt, outPkt.getPacketLength(), netFile);

            // Send the read andX response

            m_sess.sendResponseSMB(outPkt, pos);
        }
        else
        {

            // Send the normal read andX response

            m_sess.sendResponseSMB(outPkt);
        }
    }

    /**
     * Rename a file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procRenameFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid rename file request

        if (m_smbPkt.checkPacketIsValid(1, 4) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the Unicode flag

        boolean isUni = m_smbPkt.isUnicode();

        // Read the data block

        m_smbPkt.resetBytePointer();

        // Extract the old file name

        if (m_smbPkt.unpackByte() != DataType.ASCII)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        String oldName = m_smbPkt.unpackString(isUni);
        if (oldName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Extract the new file name

        if (m_smbPkt.unpackByte() != DataType.ASCII)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        String newName = m_smbPkt.unpackString(isUni);
        if (newName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Rename [" + treeId + "] old name=" + oldName + ", new name=" + newName);

        // Access the disk interface and rename the requested file

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Rename the requested file

            disk.renameFile(m_sess, conn, oldName, newName);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (FileNotFoundException ex)
        {

            // Source file/directory does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (FileExistsException ex)
        {

            // Destination file/directory already exists

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                    SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to rename the file/directory

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileSharingException ex)
        {

            // Return a sharing violation error

            m_sess.sendErrorResponseSMB(SMBStatus.NTSharingViolation, SMBStatus.DOSFileSharingConflict,
                    SMBStatus.ErrDos);
            return;
        }

        // Build the rename file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler())
            diskCtx.getChangeHandler().notifyRename(oldName, newName);
    }

    /**
     * Delete a file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception IOException If an network error occurs
     * @exception SMBSrvException If an SMB error occurs
     */
    protected void procDeleteFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file delete request

        if (m_smbPkt.checkPacketIsValid(1, 2) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the Unicode flag

        boolean isUni = m_smbPkt.isUnicode();

        // Read the data block

        m_smbPkt.resetBytePointer();

        // Extract the old file name

        if (m_smbPkt.unpackByte() != DataType.ASCII)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        String fileName = m_smbPkt.unpackString(isUni);
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Delete [" + treeId + "] name=" + fileName);

        // Access the disk interface and delete the file(s)

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Delete file(s)

            disk.deleteFile(m_sess, conn, fileName);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to delete the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the delete file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler())
            diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, fileName);
    }

    /**
     * Delete a directory.
     * 
     * @param outPkt SMBSrvPacket
     * @exception IOException If a network error occurs
     * @exception SMBSrvException If an SMB error occurs
     */
    protected void procDeleteDirectory(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid delete directory request

        if (m_smbPkt.checkPacketIsValid(0, 2) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the Unicode flag

        boolean isUni = m_smbPkt.isUnicode();

        // Read the data block

        m_smbPkt.resetBytePointer();

        // Extract the old file name

        if (m_smbPkt.unpackByte() != DataType.ASCII)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        String dirName = m_smbPkt.unpackString(isUni);
        if (dirName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Directory Delete [" + treeId + "] name=" + dirName);

        // Access the disk interface and delete the directory

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Delete the directory

            disk.deleteDirectory(m_sess, conn, dirName);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to delete the directory

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (DirectoryNotEmptyException ex)
        {

            // Directory not empty

            m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryNotEmpty, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to delete the directory

            m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid, SMBStatus.ErrDos);
            return;
        }

        // Build the delete directory response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler())
            diskCtx.getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionRemoved, dirName);
    }

    /**
     * Process a transact2 file search request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt Packet to use for the reply.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procTrans2FindFirst(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            return;
        }

        // Get the search parameters

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int srchAttr = paramBuf.getShort();
        int maxFiles = paramBuf.getShort();
        int srchFlag = paramBuf.getShort();
        int infoLevl = paramBuf.getShort();
        paramBuf.skipBytes(4);

        String srchPath = paramBuf.getString(tbuf.isUnicode());

        // Check if the search path is valid

        if (srchPath == null || srchPath.length() == 0)
        {

            // Invalid search request

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        else if (srchPath.endsWith("\\"))
        {

            // Make the search a wildcard search

            srchPath = srchPath + "*.*";
        }

        // Check for the Macintosh information level, if the Macintosh extensions are not enabled
        // return an error

        if (infoLevl == FindInfoPacker.InfoMacHfsInfo && getSession().hasMacintoshExtensions() == false)
        {

            // Return an error status, Macintosh extensions are not enabled

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            return;
        }

        // Access the shared device disk interface

        SearchContext ctx = null;
        DiskInterface disk = null;
        int searchId = -1;
        boolean wildcardSearch = false;

        try
        {

            // Access the disk interface

            disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Allocate a search slot for the new search

            searchId = m_sess.allocateSearchSlot();
            if (searchId == -1)
            {

                // Failed to allocate a slot for the new search

                m_sess.sendErrorResponseSMB(SMBStatus.SRVNoResourcesAvailable, SMBStatus.ErrSrv);
                return;
            }

            // Check if this is a wildcard search or single file search

            if (WildCard.containsWildcards(srchPath) || WildCard.containsUnicodeWildcard(srchPath))
                wildcardSearch = true;

            // Check if the search contains Unicode wildcards

            if (tbuf.isUnicode() && WildCard.containsUnicodeWildcard(srchPath))
            {

                // Translate the Unicode wildcards to standard DOS wildcards

                srchPath = WildCard.convertUnicodeWildcardToDOS(srchPath);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("Converted Unicode wildcards to:" + srchPath);
            }

            // Start a new search

            ctx = disk.startSearch(m_sess, conn, srchPath, srchAttr);
            if (ctx != null)
            {

                // Store details of the search in the context

                ctx.setTreeId(treeId);
                ctx.setMaximumFiles(maxFiles);
            }
            else
            {

                // Failed to start the search, return a no more files error

                m_sess.sendErrorResponseSMB(SMBStatus.NTNoSuchFile, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
                return;
            }

            // Save the search context

            m_sess.setSearchContext(searchId, ctx);

            // Create the reply transact buffer

            SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
            DataBuffer dataBuf = replyBuf.getDataBuffer();

            // Determine the maximum return data length

            int maxLen = replyBuf.getReturnDataLimit();

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Start trans search [" + searchId + "] - " + srchPath + ", attr=0x"
                        + Integer.toHexString(srchAttr) + ", maxFiles=" + maxFiles + ", maxLen=" + maxLen
                        + ", infoLevel=" + infoLevl + ", flags=0x" + Integer.toHexString(srchFlag));

            // Loop until we have filled the return buffer or there are no more files to return

            int fileCnt = 0;
            int packLen = 0;
            int lastNameOff = 0;

            // Flag to indicate if resume ids should be returned

            boolean resumeIds = false;
            if (infoLevl == FindInfoPacker.InfoStandard && (srchFlag & FindFirstNext.ReturnResumeKey) != 0)
            {

                // Windows servers only seem to return resume keys for the standard information
                // level

                resumeIds = true;
            }

            // If this is a wildcard search then add the '.' and '..' entries

            if (wildcardSearch == true && ReturnDotFiles == true)
            {

                // Pack the '.' file information

                if (resumeIds == true)
                {
                    dataBuf.putInt(-1);
                    maxLen -= 4;
                }

                lastNameOff = dataBuf.getPosition();
                FileInfo dotInfo = new FileInfo(".", 0, FileAttribute.Directory);
                dotInfo.setFileId(dotInfo.getFileName().hashCode());

                packLen = FindInfoPacker.packInfo(dotInfo, dataBuf, infoLevl, tbuf.isUnicode());

                // Update the file count for this packet, update the remaining buffer length

                fileCnt++;
                maxLen -= packLen;

                // Pack the '..' file information

                if (resumeIds == true)
                {
                    dataBuf.putInt(-2);
                    maxLen -= 4;
                }

                lastNameOff = dataBuf.getPosition();
                dotInfo.setFileName("..");
                dotInfo.setFileId(dotInfo.getFileName().hashCode());

                packLen = FindInfoPacker.packInfo(dotInfo, dataBuf, infoLevl, tbuf.isUnicode());

                // Update the file count for this packet, update the remaining buffer length

                fileCnt++;
                maxLen -= packLen;
            }

            boolean pktDone = false;
            boolean searchDone = false;

            FileInfo info = new FileInfo();

            while (pktDone == false && fileCnt < maxFiles)
            {

                // Get file information from the search

                if (ctx.nextFileInfo(info) == false)
                {

                    // No more files

                    pktDone = true;
                    searchDone = true;
                }

                // Check if the file information will fit into the return buffer

                else if (FindInfoPacker.calcInfoSize(info, infoLevl, false, true) <= maxLen)
                {

                    // Pack the resume id, if required

                    if (resumeIds == true)
                    {
                        dataBuf.putInt(ctx.getResumeId());
                        maxLen -= 4;
                    }

                    // Save the offset to the last file information structure

                    lastNameOff = dataBuf.getPosition();

                    // Pack the file information

                    packLen = FindInfoPacker.packInfo(info, dataBuf, infoLevl, tbuf.isUnicode());

                    // Update the file count for this packet

                    fileCnt++;

                    // Recalculate the remaining buffer space

                    maxLen -= packLen;
                }
                else
                {

                    // Set the search restart point

                    ctx.restartAt(info);

                    // No more buffer space

                    pktDone = true;
                }
            }

            // Check for a single file search and the file was not found, in this case return an
            // error status

            if (wildcardSearch == false && fileCnt == 0)
                throw new FileNotFoundException(srchPath);

            // Check for a search where the maximum files is set to one, close the search
            // immediately.

            if (maxFiles == 1 && fileCnt == 1)
                searchDone = true;

            // Clear the next structure offset, if applicable

            FindInfoPacker.clearNextOffset(dataBuf, infoLevl, lastNameOff);

            // Pack the parameter block

            paramBuf = replyBuf.getParameterBuffer();

            paramBuf.putShort(searchId);
            paramBuf.putShort(fileCnt);
            paramBuf.putShort(ctx.hasMoreFiles() ? 0 : 1);
            paramBuf.putShort(0);
            paramBuf.putShort(lastNameOff);

            // Send the transaction response

            SMBSrvTransPacket tpkt = new SMBSrvTransPacket(outPkt.getBuffer());
            tpkt.doTransactionResponse(m_sess, replyBuf);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Search [" + searchId + "] Returned " + fileCnt + " files, dataLen=" + dataBuf.getLength()
                        + ", moreFiles=" + ctx.hasMoreFiles());

            // Check if the search is complete

            if (searchDone == true || ctx.hasMoreFiles() == false)
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("End start search [" + searchId + "] (Search complete)");

                // Release the search context

                m_sess.deallocateSearchSlot(searchId);
            }
        }
        catch (FileNotFoundException ex)
        {

            // Search path does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTNoSuchFile, SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
        }
        catch (PathNotFoundException ex)
        {

            // Deallocate the search

            if (searchId != -1)
                m_sess.deallocateSearchSlot(searchId);

            // Requested path does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Deallocate the search

            if (searchId != -1)
                m_sess.deallocateSearchSlot(searchId);

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
        }
        catch (UnsupportedInfoLevelException ex)
        {

            // Deallocate the search

            if (searchId != -1)
                m_sess.deallocateSearchSlot(searchId);

            // Requested information level is not supported

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        }
    }

    /**
     * Process a transact2 file search continue request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procTrans2FindNext(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the search parameters

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int searchId = paramBuf.getShort();
        int maxFiles = paramBuf.getShort();
        int infoLevl = paramBuf.getShort();
        paramBuf.getInt();
        int srchFlag = paramBuf.getShort();

        String resumeName = paramBuf.getString(tbuf.isUnicode());

        // Access the shared device disk interface

        SearchContext ctx = null;

        try
        {
            // Retrieve the search context

            ctx = m_sess.getSearchContext(searchId);
            if (ctx == null)
            {

                // DEBUG

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("Search context null - [" + searchId + "]");

                // Invalid search handle

                m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
                return;
            }

            // Create the reply transaction buffer

            SrvTransactBuffer replyBuf = new SrvTransactBuffer(tbuf);
            DataBuffer dataBuf = replyBuf.getDataBuffer();

            // Determine the maximum return data length

            int maxLen = replyBuf.getReturnDataLimit();

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Continue search [" + searchId + "] - " + resumeName + ", maxFiles=" + maxFiles
                        + ", maxLen=" + maxLen + ", infoLevel=" + infoLevl + ", flags=0x"
                        + Integer.toHexString(srchFlag));

            // Loop until we have filled the return buffer or there are no more files to return

            int fileCnt = 0;
            int packLen = 0;
            int lastNameOff = 0;

            // Flag to indicate if resume ids should be returned

            boolean resumeIds = false;
            if (infoLevl == FindInfoPacker.InfoStandard && (srchFlag & FindFirstNext.ReturnResumeKey) != 0)
            {

                // Windows servers only seem to return resume keys for the standard information
                // level

                resumeIds = true;
            }

            // Flags to indicate packet full or search complete

            boolean pktDone = false;
            boolean searchDone = false;

            FileInfo info = new FileInfo();

            while (pktDone == false && fileCnt < maxFiles)
            {

                // Get file information from the search

                if (ctx.nextFileInfo(info) == false)
                {

                    // No more files

                    pktDone = true;
                    searchDone = true;
                }

                // Check if the file information will fit into the return buffer

                else if (FindInfoPacker.calcInfoSize(info, infoLevl, false, true) <= maxLen)
                {

                    // Pack the resume id, if required

                    if (resumeIds == true)
                    {
                        dataBuf.putInt(ctx.getResumeId());
                        maxLen -= 4;
                    }

                    // Save the offset to the last file information structure

                    lastNameOff = dataBuf.getPosition();

                    // Pack the file information

                    packLen = FindInfoPacker.packInfo(info, dataBuf, infoLevl, tbuf.isUnicode());

                    // Update the file count for this packet

                    fileCnt++;

                    // Recalculate the remaining buffer space

                    maxLen -= packLen;
                }
                else
                {

                    // Set the search restart point

                    ctx.restartAt(info);

                    // No more buffer space

                    pktDone = true;
                }
            }

            // Pack the parameter block

            paramBuf = replyBuf.getParameterBuffer();

            paramBuf.putShort(fileCnt);
            paramBuf.putShort(ctx.hasMoreFiles() ? 0 : 1);
            paramBuf.putShort(0);
            paramBuf.putShort(lastNameOff);

            // Send the transaction response

            SMBSrvTransPacket tpkt = new SMBSrvTransPacket(outPkt.getBuffer());
            tpkt.doTransactionResponse(m_sess, replyBuf);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Search [" + searchId + "] Returned " + fileCnt + " files, dataLen=" + dataBuf.getLength()
                        + ", moreFiles=" + ctx.hasMoreFiles());

            // Check if the search is complete

            if (searchDone == true || ctx.hasMoreFiles() == false)
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("End start search [" + searchId + "] (Search complete)");

                // Release the search context

                m_sess.deallocateSearchSlot(searchId);
            }
        }
        catch (FileNotFoundException ex)
        {

            // Deallocate the search

            if (searchId != -1)
                m_sess.deallocateSearchSlot(searchId);

            // Search path does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);
        }
        catch (UnsupportedInfoLevelException ex)
        {

            // Deallocate the search

            if (searchId != -1)
                m_sess.deallocateSearchSlot(searchId);

            // Requested information level is not supported

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        }
    }

    /**
     * Process a transact2 file system query request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procTrans2QueryFileSys(SrvTransactBuffer tbuf, SMBSrvPacket outPkt)
            throws java.io.IOException, SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the query file system required information level

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int infoLevl = paramBuf.getShort();

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Query File System Info - level = 0x" + Integer.toHexString(infoLevl));

        // Access the shared device disk interface

        try
        {

            // Access the disk interface and context

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();
            DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();

            // Set the return parameter count, so that the data area position can be calculated.

            outPkt.setParameterCount(10);

            // Pack the disk information into the data area of the transaction reply

            byte[] buf = outPkt.getBuffer();
            int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
            int dataPos = prmPos; // no parameters returned

            // Create a data buffer using the SMB packet. The response should always fit into a
            // single
            // reply packet.

            DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

            // Determine the information level requested

            SrvDiskInfo diskInfo = null;
            VolumeInfo volInfo = null;

            switch (infoLevl)
            {

            // Standard disk information

            case DiskInfoPacker.InfoStandard:

                // Get the disk information

                diskInfo = getDiskInformation(disk, diskCtx);

                // Pack the disk information into the return data packet

                DiskInfoPacker.packStandardInfo(diskInfo, replyBuf);
                break;

            // Volume label information

            case DiskInfoPacker.InfoVolume:

                // Get the volume label information

                volInfo = getVolumeInformation(disk, diskCtx);

                // Pack the volume label information

                DiskInfoPacker.packVolumeInfo(volInfo, replyBuf, tbuf.isUnicode());
                break;

            // Full volume information

            case DiskInfoPacker.InfoFsVolume:

                // Get the volume information

                volInfo = getVolumeInformation(disk, diskCtx);

                // Pack the volume information

                DiskInfoPacker.packFsVolumeInformation(volInfo, replyBuf, tbuf.isUnicode());
                break;

            // Filesystem size information

            case DiskInfoPacker.InfoFsSize:

                // Get the disk information

                diskInfo = getDiskInformation(disk, diskCtx);

                // Pack the disk information into the return data packet

                DiskInfoPacker.packFsSizeInformation(diskInfo, replyBuf);
                break;

            // Filesystem device information

            case DiskInfoPacker.InfoFsDevice:
                DiskInfoPacker.packFsDevice(NTIOCtl.DeviceDisk, diskCtx.getDeviceAttributes(), replyBuf);
                break;

            // Filesystem attribute information

            case DiskInfoPacker.InfoFsAttribute:
                String fsType = diskCtx.getFilesystemType();
                
                if (disk instanceof NTFSStreamsInterface)
                {

                    // Check if NTFS streams are enabled

                    NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                    if (ntfsStreams.hasStreamsEnabled(m_sess, conn))
                        fsType = "NTFS";
                }

                // Pack the filesystem type

                DiskInfoPacker.packFsAttribute(diskCtx.getFilesystemAttributes(), 255, fsType, tbuf.isUnicode(),
                        replyBuf);
                break;

            // Mac filesystem information

            case DiskInfoPacker.InfoMacFsInfo:

                // Check if the filesystem supports NTFS streams
                //
                // We should only return a valid response to the Macintosh information level if the
                // filesystem
                // does NOT support NTFS streams. By returning an error status the Thursby DAVE
                // software will treat
                // the filesystem as a WinXP/2K filesystem with full streams support.

                boolean ntfs = false;

                if (disk instanceof NTFSStreamsInterface)
                {

                    // Check if streams are enabled

                    NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                    ntfs = ntfsStreams.hasStreamsEnabled(m_sess, conn);
                }

                // If the filesystem does not support NTFS streams then send a valid response.

                if (ntfs == false)
                {

                    // Get the disk and volume information

                    diskInfo = getDiskInformation(disk, diskCtx);
                    volInfo = getVolumeInformation(disk, diskCtx);

                    // Pack the disk information into the return data packet

                    DiskInfoPacker.packMacFsInformation(diskInfo, volInfo, ntfs, replyBuf);
                }
                break;

            // Filesystem size information, including per user allocation limit

            case DiskInfoPacker.InfoFullFsSize:

                // Get the disk information

                diskInfo = getDiskInformation(disk, diskCtx);
                long userLimit = diskInfo.getTotalUnits();

                // Pack the disk information into the return data packet

                DiskInfoPacker.packFullFsSizeInformation(userLimit, diskInfo, replyBuf);
                break;
            }

            // Check if any data was packed, if not then the information level is not supported

            if (replyBuf.getPosition() == dataPos)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
                return;
            }

            int bytCnt = replyBuf.getPosition() - outPkt.getByteOffset();
            replyBuf.setEndOfBuffer();
            int dataLen = replyBuf.getLength();
            SMBSrvTransPacket.initTransactReply(outPkt, 0, prmPos, dataLen, dataPos);
            outPkt.setByteCount(bytCnt);

            // Send the transact reply

            m_sess.sendResponseSMB(outPkt);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
    }

    /**
     * Process a transact2 query path information request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procTrans2QueryPath(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the query path information level and file/directory name

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int infoLevl = paramBuf.getShort();
        paramBuf.skipBytes(4);

        String path = paramBuf.getString(tbuf.isUnicode());

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Query Path - level = 0x" + Integer.toHexString(infoLevl) + ", path = " + path);

        // Access the shared device disk interface

        try
        {

            // Access the disk interface

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Set the return parameter count, so that the data area position can be calculated.

            outPkt.setParameterCount(10);

            // Pack the file information into the data area of the transaction reply

            byte[] buf = outPkt.getBuffer();
            int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
            int dataPos = prmPos + 4;

            // Pack the return parametes, EA error offset

            outPkt.setPosition(prmPos);
            outPkt.packWord(0);

            // Create a data buffer using the SMB packet. The response should always fit into a
            // single
            // reply packet.

            DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

            // Check if the virtual filesystem supports streams, and streams are enabled

            boolean streams = false;

            if (disk instanceof NTFSStreamsInterface)
            {

                // Check if NTFS streams are enabled

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                streams = ntfsStreams.hasStreamsEnabled(m_sess, conn);
            }

            // Check if the path is for an NTFS stream, return an error if streams are not supported or not enabled
            
            if ( streams == false && path.indexOf(FileOpenParams.StreamSeparator) != -1)
            {
                // NTFS streams not supported, return an error status
                
                m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
                return;
            }
            
            // Check for the file streams information level

            int dataLen = 0;

            if (streams == true
                    && (infoLevl == FileInfoLevel.PathFileStreamInfo || infoLevl == FileInfoLevel.NTFileStreamInfo))
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STREAMS))
                    logger.debug("Get NTFS streams list path=" + path);

                // Get the list of streams from the share driver

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                StreamInfoList streamList = ntfsStreams.getStreamList(m_sess, conn, path);

                if (streamList == null)
                {
                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.SRVNonSpecificError,
                            SMBStatus.ErrSrv);
                    return;
                }

                // Pack the file streams information into the return data packet

                dataLen = QueryInfoPacker.packStreamFileInfo(streamList, replyBuf, true);
            }
            else
            {

                // Get the file information

                FileInfo fileInfo = disk.getFileInformation(m_sess, conn, path);

                if (fileInfo == null)
                {
                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound,
                                    SMBStatus.ErrDos);
                    return;
                }

                // Pack the file information into the return data packet

                dataLen = QueryInfoPacker.packInfo(fileInfo, replyBuf, infoLevl, true);
            }

            // Check if any data was packed, if not then the information level is not supported

            if (dataLen == 0)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                        SMBStatus.ErrSrv);
                return;
            }

            SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, dataLen, dataPos);
            outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

            // Send the transact reply

            m_sess.sendResponseSMB(outPkt);
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to access the file/folder

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileNotFoundException ex)
        {

            // Requested file does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (PathNotFoundException ex)
        {

            // Requested path does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
        catch (UnsupportedInfoLevelException ex)
        {

            // Requested information level is not supported

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
    }

    /**
     * Process a transact2 query file information (via handle) request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException SMB protocol exception
     */
    protected final void procTrans2QueryFile(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id and query path information level

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int fid = paramBuf.getShort();
        int infoLevl = paramBuf.getShort();

        // Get the file details via the file id

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Query File - level=0x" + Integer.toHexString(infoLevl) + ", fid=" + fid + ", stream="
                    + netFile.getStreamId() + ", name=" + netFile.getFullName());

        // Access the shared device disk interface

        try
        {

            // Access the disk interface

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Set the return parameter count, so that the data area position can be calculated.

            outPkt.setParameterCount(10);

            // Pack the file information into the data area of the transaction reply

            byte[] buf = outPkt.getBuffer();
            int prmPos = DataPacker.longwordAlign(outPkt.getByteOffset());
            int dataPos = prmPos + 4;

            // Pack the return parametes, EA error offset

            outPkt.setPosition(prmPos);
            outPkt.packWord(0);

            // Create a data buffer using the SMB packet. The response should always fit into a
            // single
            // reply packet.

            DataBuffer replyBuf = new DataBuffer(buf, dataPos, buf.length - dataPos);

            // Check if the virtual filesystem supports streams, and streams are enabled

            boolean streams = false;

            if (disk instanceof NTFSStreamsInterface)
            {

                // Check if NTFS streams are enabled

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                streams = ntfsStreams.hasStreamsEnabled(m_sess, conn);
            }

            // Check for the file streams information level

            int dataLen = 0;

            if (streams == true
                    && (infoLevl == FileInfoLevel.PathFileStreamInfo || infoLevl == FileInfoLevel.NTFileStreamInfo))
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STREAMS))
                    logger.debug("Get NTFS streams list fid=" + fid + ", name=" + netFile.getFullName());

                // Get the list of streams from the share driver

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                StreamInfoList streamList = ntfsStreams.getStreamList(m_sess, conn, netFile.getFullName());

                if (streamList == null)
                {
                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.SRVNonSpecificError,
                            SMBStatus.ErrSrv);
                    return;
                }

                // Pack the file streams information into the return data packet

                dataLen = QueryInfoPacker.packStreamFileInfo(streamList, replyBuf, true);
            }
            else
            {

                // Get the file information

                FileInfo fileInfo = disk.getFileInformation(m_sess, conn, netFile.getFullNameStream());

                if (fileInfo == null)
                {
                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.SRVNonSpecificError,
                            SMBStatus.ErrSrv);
                    return;
                }

                // Pack the file information into the return data packet

                dataLen = QueryInfoPacker.packInfo(fileInfo, replyBuf, infoLevl, true);
            }

            // Check if any data was packed, if not then the information level is not supported

            if (dataLen == 0)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError,
                        SMBStatus.ErrSrv);
                return;
            }

            SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, dataLen, dataPos);
            outPkt.setByteCount(replyBuf.getPosition() - outPkt.getByteOffset());

            // Send the transact reply

            m_sess.sendResponseSMB(outPkt);
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to access the file/folder

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileNotFoundException ex)
        {

            // Requested file does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (PathNotFoundException ex)
        {

            // Requested path does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectPathNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
        catch (UnsupportedInfoLevelException ex)
        {

            // Requested information level is not supported

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
    }

    /**
     * Process a transact2 set file information (via handle) request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException SMB protocol exception
     */
    protected final void procTrans2SetFile(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id and information level

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int fid = paramBuf.getShort();
        int infoLevl = paramBuf.getShort();

        // Get the file details via the file id

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Set File - level=0x" + Integer.toHexString(infoLevl) + ", fid=" + fid + ", name="
                    + netFile.getFullName());

        // Access the shared device disk interface

        try
        {

            // Access the disk interface

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Process the set file information request

            DataBuffer dataBuf = tbuf.getDataBuffer();
            FileInfo finfo = null;

            switch (infoLevl)
            {

            // Set basic file information (dates/attributes)

            case FileInfoLevel.SetBasicInfo:

                // Create the file information template

                int setFlags = 0;
                finfo = new FileInfo(netFile.getFullName(), 0, -1);

                // Set the creation date/time, if specified

                long timeNow = System.currentTimeMillis();

                long nttim = dataBuf.getLong();
                boolean hasSetTime = false;

                if (nttim != 0L)
                {
                    if (nttim != -1L)
                    {
                        finfo.setCreationDateTime(NTTime.toJavaDate(nttim));
                        setFlags += FileInfo.SetCreationDate;
                    }
                    hasSetTime = true;
                }

                // Set the last access date/time, if specified

                nttim = dataBuf.getLong();

                if (nttim != 0L)
                {
                    if (nttim != -1L)
                    {
                        finfo.setAccessDateTime(NTTime.toJavaDate(nttim));
                        setFlags += FileInfo.SetAccessDate;
                    }
                    else
                    {
                        finfo.setAccessDateTime(timeNow);
                        setFlags += FileInfo.SetAccessDate;
                    }
                    hasSetTime = true;
                }

                // Set the last write date/time, if specified

                nttim = dataBuf.getLong();

                if (nttim > 0L)
                {
                    if (nttim != -1L)
                    {
                        finfo.setModifyDateTime(NTTime.toJavaDate(nttim));
                        setFlags += FileInfo.SetModifyDate;
                    }
                    else
                    {
                        finfo.setModifyDateTime(timeNow);
                        setFlags += FileInfo.SetModifyDate;
                    }
                    hasSetTime = true;
                }

                // Set the modify date/time, if specified

                nttim = dataBuf.getLong();

                if (nttim > 0L)
                {
                    if (nttim != -1L)
                    {
                        finfo.setChangeDateTime(NTTime.toJavaDate(nttim));
                        setFlags += FileInfo.SetChangeDate;
                    }
                    hasSetTime = true;
                }

                // Set the attributes

                int attr = dataBuf.getInt();
                int unknown = dataBuf.getInt();

                if (hasSetTime == false && unknown == 0)
                {
                    finfo.setFileAttributes(attr);
                    setFlags += FileInfo.SetAttributes;
                }

                // Set the file information for the specified file/directory

                finfo.setFileInformationFlags(setFlags);
                disk.setFileInformation(m_sess, conn, netFile.getFullName(), finfo);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set Basic Info [" + treeId + "] name=" + netFile.getFullName() + ", attr=0x"
                            + Integer.toHexString(attr) + ", setTime=" + hasSetTime + ", setFlags=0x"
                            + Integer.toHexString(setFlags) + ", unknown=" + unknown);
                break;

            // Set end of file position for a file

            case FileInfoLevel.SetEndOfFileInfo:

                // Get the new end of file position

                long eofPos = dataBuf.getLong();

                // Set the new end of file position

                disk.truncateFile(m_sess, conn, netFile, eofPos);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set end of file position fid=" + fid + ", eof=" + eofPos);
                break;

            // Set the allocation size for a file

            case FileInfoLevel.SetAllocationInfo:

                // Get the new end of file position

                long allocSize = dataBuf.getLong();

                // Set the new end of file position

                disk.truncateFile(m_sess, conn, netFile, allocSize);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set allocation size fid=" + fid + ", allocSize=" + allocSize);
                break;

            // Rename a stream

            case FileInfoLevel.NTFileRenameInfo:

                // Check if the virtual filesystem supports streams, and streams are enabled

                boolean streams = false;

                if (disk instanceof NTFSStreamsInterface)
                {

                    // Check if NTFS streams are enabled

                    NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                    streams = ntfsStreams.hasStreamsEnabled(m_sess, conn);
                }

                // If streams are not supported or are not enabled then return an error status

                if (streams == false)
                {

                    // Return a not supported error status

                    m_sess.sendErrorResponseSMB(SMBStatus.NTNotSupported, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
                    return;
                }

                // Get the overwrite flag

                boolean overwrite = dataBuf.getByte() == 1 ? true : false;
                dataBuf.skipBytes(3);

                int rootFid = dataBuf.getInt();
                int nameLen = dataBuf.getInt();
                String newName = dataBuf.getString(nameLen, true);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set rename fid=" + fid + ", newName=" + newName + ", overwrite=" + overwrite
                            + ", rootFID=" + rootFid);

                // Check if the new path contains a directory, only rename of a stream on the same
                // file is supported

                if (newName.indexOf(FileName.DOS_SEPERATOR_STR) != -1)
                {

                    // Return a not supported error status

                    m_sess.sendErrorResponseSMB(SMBStatus.NTNotSupported, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
                    return;
                }

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_STREAMS))
                    logger.debug("Rename stream fid=" + fid + ", name=" + netFile.getFullNameStream() + ", newName="
                            + newName + ", overwrite=" + overwrite);

                // Rename the stream

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                ntfsStreams.renameStream(m_sess, conn, netFile.getFullNameStream(), newName, overwrite);
                break;

            // Mark or unmark a file/directory for delete

            case FileInfoLevel.SetDispositionInfo:
            case FileInfoLevel.NTFileDispositionInfo:

                // Get the delete flag

                int flag = dataBuf.getByte();
                boolean delFlag = flag == 1 ? true : false;

                // Call the filesystem driver set file information to see if the file can be marked
                // for
                // delete.

                FileInfo delInfo = new FileInfo();
                delInfo.setDeleteOnClose(delFlag);
                delInfo.setFileInformationFlags(FileInfo.SetDeleteOnClose);

                disk.setFileInformation(m_sess, conn, netFile.getFullName(), delInfo);

                // Mark/unmark the file/directory for deletion

                netFile.setDeleteOnClose(delFlag);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set file disposition fid=" + fid + ", name=" + netFile.getName() + ", delete="
                            + delFlag);
                break;
            }

            // Set the return parameter count, so that the data area position can be calculated.

            outPkt.setParameterCount(10);

            // Pack the return information into the data area of the transaction reply

            byte[] buf = outPkt.getBuffer();
            int prmPos = outPkt.getByteOffset();

            // Longword align the parameters, return an unknown word parameter
            //
            // Note: Make sure the data offset is on a longword boundary, NT has problems if this is
            // not done

            prmPos = DataPacker.longwordAlign(prmPos);
            DataPacker.putIntelShort(0, buf, prmPos);

            SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, 0, prmPos + 4);
            outPkt.setByteCount((prmPos - outPkt.getByteOffset()) + 4);

            // Send the transact reply

            m_sess.sendResponseSMB(outPkt);

            // Check if there are any file/directory change notify requests active

            DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();

            if (diskCtx.hasChangeHandler() && netFile.getFullName() != null)
            {

                // Get the change handler

                NotifyChangeHandler changeHandler = diskCtx.getChangeHandler();

                // Check for file attributes and last write time changes

                if (finfo != null)
                {

                    // File attributes changed

                    if (finfo.hasSetFlag(FileInfo.SetAttributes))
                        changeHandler.notifyAttributesChanged(netFile.getFullName(), netFile.isDirectory());

                    // Last write time changed

                    if (finfo.hasSetFlag(FileInfo.SetModifyDate))
                        changeHandler.notifyLastWriteTimeChanged(netFile.getFullName(), netFile.isDirectory());
                }
                else if (infoLevl == FileInfoLevel.SetAllocationInfo || infoLevl == FileInfoLevel.SetEndOfFileInfo)
                {

                    // File size changed

                    changeHandler.notifyFileSizeChanged(netFile.getFullName());
                }
            }
        }
        catch (FileNotFoundException ex)
        {

            // Requested file does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to change file attributes/settings

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (DiskFullException ex)
        {

            // Disk is full

            m_sess.sendErrorResponseSMB(SMBStatus.NTDiskFull, SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
    }

    /**
     * Process a transact2 set path information request.
     * 
     * @param tbuf Transaction request details
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException SMB protocol exception
     */
    protected final void procTrans2SetPath(SrvTransactBuffer tbuf, SMBSrvPacket outPkt) throws java.io.IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the path and information level

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int infoLevl = paramBuf.getShort();
        paramBuf.skipBytes(4);

        String path = paramBuf.getString(tbuf.isUnicode());

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Set Path - path=" + path + ", level=0x" + Integer.toHexString(infoLevl));

        // Access the shared device disk interface

        try
        {

            // Access the disk interface

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Process the set file information request

            DataBuffer dataBuf = tbuf.getDataBuffer();
            FileInfo finfo = null;

            switch (infoLevl)
            {

            // Set standard file information (dates/attributes)

            case FileInfoLevel.SetStandard:

                // Create the file information template

                int setFlags = 0;
                finfo = new FileInfo(path, 0, -1);

                // Set the creation date/time, if specified

                int smbDate = dataBuf.getShort();
                int smbTime = dataBuf.getShort();

                boolean hasSetTime = false;

                if (smbDate != 0 && smbTime != 0)
                {
                    finfo.setCreationDateTime(new SMBDate(smbDate, smbTime).getTime());
                    setFlags += FileInfo.SetCreationDate;
                    hasSetTime = true;
                }

                // Set the last access date/time, if specified

                smbDate = dataBuf.getShort();
                smbTime = dataBuf.getShort();

                if (smbDate != 0 && smbTime != 0)
                {
                    finfo.setAccessDateTime(new SMBDate(smbDate, smbTime).getTime());
                    setFlags += FileInfo.SetAccessDate;
                    hasSetTime = true;
                }

                // Set the last write date/time, if specified

                smbDate = dataBuf.getShort();
                smbTime = dataBuf.getShort();

                if (smbDate != 0 && smbTime != 0)
                {
                    finfo.setModifyDateTime(new SMBDate(smbDate, smbTime).getTime());
                    setFlags += FileInfo.SetModifyDate;
                    hasSetTime = true;
                }

                // Set the file size/allocation size

                int fileSize = dataBuf.getInt();
                if (fileSize != 0)
                {
                    finfo.setFileSize(fileSize);
                    setFlags += FileInfo.SetFileSize;
                }

                fileSize = dataBuf.getInt();
                if (fileSize != 0)
                {
                    finfo.setAllocationSize(fileSize);
                    setFlags += FileInfo.SetAllocationSize;
                }

                // Set the attributes

                int attr = dataBuf.getInt();
                int eaListLen = dataBuf.getInt();

                if (hasSetTime == false && eaListLen == 0)
                {
                    finfo.setFileAttributes(attr);
                    setFlags += FileInfo.SetAttributes;
                }

                // Set the file information for the specified file/directory

                finfo.setFileInformationFlags(setFlags);
                disk.setFileInformation(m_sess, conn, path, finfo);

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
                    logger.debug("  Set Standard Info [" + treeId + "] name=" + path + ", attr=0x"
                            + Integer.toHexString(attr) + ", setTime=" + hasSetTime + ", setFlags=0x"
                            + Integer.toHexString(setFlags) + ", eaListLen=" + eaListLen);
                break;
            }

            // Set the return parameter count, so that the data area position can be calculated.

            outPkt.setParameterCount(10);

            // Pack the return information into the data area of the transaction reply

            byte[] buf = outPkt.getBuffer();
            int prmPos = outPkt.getByteOffset();

            // Longword align the parameters, return an unknown word parameter
            //
            // Note: Make sure the data offset is on a longword boundary, NT has problems if this is
            // not done

            prmPos = DataPacker.longwordAlign(prmPos);
            DataPacker.putIntelShort(0, buf, prmPos);

            SMBSrvTransPacket.initTransactReply(outPkt, 2, prmPos, 0, prmPos + 4);
            outPkt.setByteCount((prmPos - outPkt.getByteOffset()) + 4);

            // Send the transact reply

            m_sess.sendResponseSMB(outPkt);

            // Check if there are any file/directory change notify requests active

            DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();

            if (diskCtx.hasChangeHandler() && path != null)
            {

                // Get the change handler

                NotifyChangeHandler changeHandler = diskCtx.getChangeHandler();

                // Check for file attributes and last write time changes

                if (finfo != null)
                {

                    // Check if the path refers to a file or directory

                    int fileSts = disk.fileExists(m_sess, conn, path);

                    // File attributes changed

                    if (finfo.hasSetFlag(FileInfo.SetAttributes))
                        changeHandler.notifyAttributesChanged(path, fileSts == FileStatus.DirectoryExists ? true
                                : false);

                    // Last write time changed

                    if (finfo.hasSetFlag(FileInfo.SetModifyDate))
                        changeHandler.notifyLastWriteTimeChanged(path, fileSts == FileStatus.DirectoryExists ? true
                                : false);
                }
            }
        }
        catch (FileNotFoundException ex)
        {

            // Requested file does not exist

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to change file attributes/settings

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (DiskFullException ex)
        {

            // Disk is full

            m_sess.sendErrorResponseSMB(SMBStatus.NTDiskFull, SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }
    }

    /**
     * Process the file write request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procWriteAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid write andX request

        if (m_smbPkt.checkPacketIsValid(12, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // If the connection is to the IPC$ remote admin named pipe pass the request to the IPC
        // handler.

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {

            // Use the IPC$ handler to process the request

            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }

        // Extract the write file parameters

        int fid = m_smbPkt.getParameter(2);
        long offset = (long) (((long) m_smbPkt.getParameterLong(3)) & 0xFFFFFFFFL); // bottom 32bits
                                                                                    // of file
                                                                                    // offset
        int dataPos = m_smbPkt.getParameter(11) + RFCNetBIOSProtocol.HEADER_LEN;

        int dataLen = m_smbPkt.getParameter(10);
        int dataLenHigh = 0;

        if (m_smbPkt.getReceivedLength() > 0xFFFF)
            dataLenHigh = m_smbPkt.getParameter(9) & 0x0001;

        if (dataLenHigh > 0)
            dataLen += (dataLenHigh << 16);

        // Check for the NT format request that has the top 32bits of the file offset

        if (m_smbPkt.getParameterCount() == 14)
        {
            long topOff = (long) (((long) m_smbPkt.getParameterLong(12)) & 0xFFFFFFFFL);
            offset += topOff << 32;
        }

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Write AndX [" + netFile.getFileId() + "] : Size=" + dataLen + " ,Pos=" + offset);

        // Write data to the file

        byte[] buf = m_smbPkt.getBuffer();
        int wrtlen = 0;

        // Access the disk interface and write to the file

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Write to the file

            wrtlen = disk.writeFile(m_sess, conn, netFile, buf, dataPos, dataLen, offset);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("File Write Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Not allowed to write to the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (LockConflictException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_LOCK))
                logger.debug("Write Lock Error [" + netFile.getFileId() + "] : Size=" + dataLen + " ,Pos=" + offset);

            // File is locked

            m_sess.sendErrorResponseSMB(SMBStatus.NTLockConflict, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (DiskFullException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("Write Quota Error [" + netFile.getFileId() + "] Disk full : Size=" + dataLen + " ,Pos="
                        + offset);

            // Disk is full

            m_sess.sendErrorResponseSMB(SMBStatus.NTDiskFull, SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("File Write Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to write the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTDiskFull, SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the count of bytes actually written

        outPkt.setParameterCount(6);
        outPkt.setAndXCommand(0xFF);
        outPkt.setParameter(1, 0); // AndX offset
        outPkt.setParameter(2, wrtlen);
        outPkt.setParameter(3, 0xFFFF);

        if (dataLenHigh > 0)
        {
            outPkt.setParameter(4, dataLen >> 16);
            outPkt.setParameter(5, 0);
        }
        else
        {
            outPkt.setParameterLong(4, 0);
        }

        outPkt.setByteCount(0);
        outPkt.setParameter(1, outPkt.getLength());

        // Send the write response

        m_sess.sendResponseSMB(outPkt);

        // Report file size change notifications every so often
        //
        // We do not report every write due to the increased overhead of change notifications

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();

        if (netFile.getWriteCount() % FileSizeChangeRate == 0 && diskCtx.hasChangeHandler()
                && netFile.getFullName() != null)
        {

            // Get the change handler

            NotifyChangeHandler changeHandler = diskCtx.getChangeHandler();

            // File size changed

            changeHandler.notifyFileSizeChanged(netFile.getFullName());
        }
    }

    /**
     * Process the file create/open request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procNTCreateAndX(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid NT create andX request

        if (m_smbPkt.checkPacketIsValid(24, 1) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // If the connection is to the IPC$ remote admin named pipe pass the request to the IPC
        // handler. If the device is
        // not a disk type device then return an error.

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {

            // Use the IPC$ handler to process the request

            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }
        else if (conn.getSharedDevice().getType() != ShareType.DISK)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Extract the NT create andX parameters

        NTParameterPacker prms = new NTParameterPacker(m_smbPkt.getBuffer(), SMBSrvPacket.PARAMWORDS + 5);

        int nameLen = prms.unpackWord();
        int flags = prms.unpackInt();
        int rootFID = prms.unpackInt();
        int accessMask = prms.unpackInt();
        long allocSize = prms.unpackLong();
        int attrib = prms.unpackInt();
        int shrAccess = prms.unpackInt();
        int createDisp = prms.unpackInt();
        int createOptn = prms.unpackInt();
        int impersonLev = prms.unpackInt();
        int secFlags = prms.unpackByte();

        // Extract the filename string

        String fileName = DataPacker.getUnicodeString(m_smbPkt.getBuffer(), DataPacker.wordAlign(m_smbPkt
                .getByteOffset()), nameLen / 2);
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Access the disk interface that is associated with the shared device

        DiskInterface disk = null;
        try
        {

            // Get the disk interface for the share

            disk = (DiskInterface) conn.getSharedDevice().getInterface();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check if the file name contains a file stream name. If the disk interface does not
        // implement the optional NTFS
        // streams interface then return an error status, not supported.

        if ( FileName.containsStreamName(fileName))
        {

            // Check if the driver implements the NTFS streams interface and it is enabled

            boolean streams = false;

            if (disk instanceof NTFSStreamsInterface)
            {

                // Check if streams are enabled

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                streams = ntfsStreams.hasStreamsEnabled(m_sess, conn);
            }

            // Check if streams are enabled/available

            if (streams == false)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameInvalid, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
                return;
            }
        }

        // Create the file open parameters to be passed to the disk interface

        FileOpenParams params = new FileOpenParams(fileName, createDisp, accessMask, attrib, shrAccess, allocSize,
                createOptn, rootFID, impersonLev, secFlags);
        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("NT Create AndX [" + treeId + "] params=" + params);

        // Access the disk interface and open the requested file

        int fid;
        NetworkFile netFile = null;
        int respAction = 0;

        try
        {

            // Check if the requested file already exists

            int fileSts = disk.fileExists(m_sess, conn, fileName);

            if (fileSts == FileStatus.NotExist)
            {

                // Check if the file should be created if it does not exist

                if (createDisp == FileAction.NTCreate || createDisp == FileAction.NTOpenIf
                        || createDisp == FileAction.NTOverwriteIf || createDisp == FileAction.NTSupersede)
                {

                    // Check if the user has the required access permission

                    if (conn.hasWriteAccess() == false)
                    {

                        // User does not have the required access rights

                        m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied,
                                SMBStatus.ErrDos);
                        return;
                    }

                    // Check if a new file or directory should be created

                    if ((createOptn & WinNT.CreateDirectory) == 0)
                    {

                        // Create a new file

                        netFile = disk.createFile(m_sess, conn, params);
                    }
                    else
                    {

                        // Create a new directory and open it

                        disk.createDirectory(m_sess, conn, params);
                        netFile = disk.openFile(m_sess, conn, params);
                    }

                    // Check if the delete on close option is set

                    if (netFile != null && (createOptn & WinNT.CreateDeleteOnClose) != 0)
                        netFile.setDeleteOnClose(true);

                    // Indicate that the file did not exist and was created

                    respAction = FileAction.FileCreated;
                }
                else
                {

                    // Check if the path is a directory

                    if (fileSts == FileStatus.DirectoryExists)
                    {

                        // Return an access denied error

                        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                                SMBStatus.ErrDos);
                        return;
                    }
                    else
                    {

                        // Return a file not found error

                        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound,
                                SMBStatus.ErrDos);
                        return;
                    }
                }
            }
            else if (createDisp == FileAction.NTCreate)
            {

                // Check for a file or directory

                if (fileSts == FileStatus.FileExists || fileSts == FileStatus.DirectoryExists)
                {

                    // Return a file exists error

                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                            SMBStatus.ErrDos);
                    return;
                }
                else
                {

                    // Return an access denied exception

                    m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    return;
                }
            }
            else
            {

                // Open the requested file/directory

                netFile = disk.openFile(m_sess, conn, params);

                // Check if the file should be truncated

                if (createDisp == FileAction.NTSupersede || createDisp == FileAction.NTOverwriteIf)
                {

                    // Truncate the file

                    disk.truncateFile(m_sess, conn, netFile, 0L);

                    // Debug

                    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
                        logger.debug("  [" + treeId + "] name=" + fileName + " truncated");
                }

                // Set the file action response

                respAction = FileAction.FileExisted;
            }

            // Add the file to the list of open files for this tree connection

            fid = conn.addFile(netFile, getSession());

        }
        catch (TooManyFilesException ex)
        {

            // Too many files are open on this connection, cannot open any more files.

            m_sess.sendErrorResponseSMB(SMBStatus.NTTooManyOpenFiles, SMBStatus.DOSTooManyOpenFiles, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileExistsException ex)
        {

            // File/directory already exists

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                    SMBStatus.ErrDos);
            return;
        }
        catch (FileSharingException ex)
        {

            // Return a sharing violation error

            m_sess.sendErrorResponseSMB(SMBStatus.NTSharingViolation, SMBStatus.DOSFileSharingConflict,
                    SMBStatus.ErrDos);
            return;
        }
        catch (FileOfflineException ex)
        {

            // File data is unavailable

            m_sess.sendErrorResponseSMB(SMBStatus.NTFileOffline, SMBStatus.HRDDriveNotReady, SMBStatus.ErrHrd);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the NT create andX response

        outPkt.setParameterCount( 34);

        outPkt.setAndXCommand(0xFF);
        outPkt.setParameter(1, 0); // AndX offset

        prms.reset(outPkt.getBuffer(), SMBSrvPacket.PARAMWORDS + 4);

        // Fake the oplock for certain file types
        
        boolean fakeOpLocks = FakeOpLocks;
        String fname = params.getPath().toUpperCase();
        
        if ( fname.endsWith( ".URL")){
        
            // Fake the oplock
            
            fakeOpLocks = true;
        }
        
        // Check if oplocks should be faked

        if (fakeOpLocks == true)
        {

            // If an oplock was requested indicate it was granted, for now

            if ((flags & WinNT.RequestBatchOplock) != 0)
            {

                // Batch oplock granted

                prms.packByte(2);
            }
            else if ((flags & WinNT.RequestOplock) != 0)
            {

                // Exclusive oplock granted

                prms.packByte(1);
            }
            else
            {

                // No oplock granted

                prms.packByte(0);
            }
        }
        else
            prms.packByte(0);

        // Pack the file id

        prms.packWord(fid);
        prms.packInt(respAction);

        // Pack the file/directory dates

        if (netFile.hasCreationDate())
            prms.packLong(NTTime.toNTTime(netFile.getCreationDate()));
        else
            prms.packLong(0);

        if ( netFile.hasAccessDate())
            prms.packLong(NTTime.toNTTime(netFile.getAccessDate()));
        else
            prms.packLong(0);
        
        if (netFile.hasModifyDate())
        {
            long modDate = NTTime.toNTTime(netFile.getModifyDate());
            prms.packLong(modDate);
            prms.packLong(modDate);
        }
        else
        {
            prms.packLong(0); // Last write time
            prms.packLong(0); // Change time
        }

        prms.packInt(netFile.getFileAttributes());

        // Pack the file size/allocation size

        long fileSize = netFile.getFileSize();
        if (fileSize > 0L)
            fileSize = (fileSize + 512L) & 0xFFFFFFFFFFFFFE00L;

        prms.packLong(fileSize); // Allocation size
        prms.packLong(netFile.getFileSize()); // End of file
        prms.packWord(0); // File type - disk file
        prms.packWord((flags & WinNT.ExtendedResponse) != 0 ? 7 : 0); // Device state
        prms.packByte(netFile.isDirectory() ? 1 : 0);

        prms.packWord(0); // byte count = 0

        // Set the AndX offset

        int endPos = prms.getPosition();
        outPkt.setParameter(1, endPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Check if there is a chained request

        if (m_smbPkt.hasAndXCommand())
        {

            // Process the chained requests

            endPos = procAndXCommands(outPkt, endPos, netFile);
        }

        // Send the response packet

        m_sess.sendResponseSMB(outPkt, endPos - RFCNetBIOSProtocol.HEADER_LEN);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler() && respAction == FileAction.FileCreated)
        {

            // Check if a file or directory has been created

            if (netFile.isDirectory())
                diskCtx.getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionAdded, fileName);
            else
                diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, fileName);
        }
    }

    /**
     * Process the cancel request.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procNTCancel(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid NT cancel request

        if (m_smbPkt.checkPacketIsValid(0, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Find the matching notify request and remove it

        NotifyRequest req = m_sess.findNotifyRequest(m_smbPkt.getMultiplexId(), m_smbPkt.getTreeId(), m_smbPkt
                .getUserId(), m_smbPkt.getProcessId());
        if (req != null)
        {

            // Remove the request

            m_sess.removeNotifyRequest(req);

            // Return a cancelled status

            m_smbPkt.setParameterCount(0);
            m_smbPkt.setByteCount(0);

            // Enable the long error status flag

            if (m_smbPkt.isLongErrorCode() == false)
                m_smbPkt.setFlags2(m_smbPkt.getFlags2() + SMBSrvPacket.FLG2_LONGERRORCODE);

            // Set the NT status code

            m_smbPkt.setLongErrorCode(SMBStatus.NTCancelled);

            // Set the Unicode strings flag

            if (m_smbPkt.isUnicode() == false)
                m_smbPkt.setFlags2(m_smbPkt.getFlags2() + SMBSrvPacket.FLG2_UNICODE);

            // Return the error response to the client

            m_sess.sendResponseSMB(m_smbPkt);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
            {
                DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
                logger.debug("NT Cancel notify mid=" + req.getMultiplexId() + ", dir=" + req.getWatchPath()
                        + ", queue=" + diskCtx.getChangeHandler().getRequestQueueSize());
            }
        }
        else
        {

            // Nothing to cancel

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }
    }

    /**
     * Process an NT transaction
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procNTTransaction(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that we received enough parameters for a transact2 request

        if (m_smbPkt.checkPacketIsValid(19, 0) == false)
        {

            // Not enough parameters for a valid transact2 request

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Check if the transaction request is for the IPC$ pipe

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {
            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }

        // Create an NT transaction using the received packet

        NTTransPacket ntTrans = new NTTransPacket(m_smbPkt.getBuffer());
        int subCmd = ntTrans.getNTFunction();

        // Check for a notfy change request, this needs special processing

        if (subCmd == PacketType.NTTransNotifyChange)
        {

            // Handle the notify change setup request

            procNTTransactNotifyChange(ntTrans, outPkt);
            return;
        }

        // Create a transact buffer to hold the transaction parameter block and data block

        SrvTransactBuffer transBuf = null;

        if (ntTrans.getTotalParameterCount() == ntTrans.getParameterBlockCount()
                && ntTrans.getTotalDataCount() == ntTrans.getDataBlockCount())
        {

            // Create a transact buffer using the packet buffer, the entire request is contained in
            // a single
            // packet

            transBuf = new SrvTransactBuffer(ntTrans);
        }
        else
        {

            // Create a transact buffer to hold the multiple transact request parameter/data blocks

            transBuf = new SrvTransactBuffer(ntTrans.getSetupCount(), ntTrans.getTotalParameterCount(), ntTrans
                    .getTotalDataCount());
            transBuf.setType(ntTrans.getCommand());
            transBuf.setFunction(subCmd);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
                logger.debug("NT Transaction [" + treeId + "] transbuf=" + transBuf);

            // Append the setup, parameter and data blocks to the transaction data

            byte[] buf = ntTrans.getBuffer();
            int cnt = ntTrans.getSetupCount();

            if (cnt > 0)
                transBuf.appendSetup(buf, ntTrans.getSetupOffset(), cnt * 2);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
                logger.debug("NT Transaction [" + treeId + "] pcnt=" + ntTrans.getNTParameter(4) + ", offset="
                        + ntTrans.getNTParameter(5));

            cnt = ntTrans.getParameterBlockCount();

            if (cnt > 0)
                transBuf.appendParameter(buf, ntTrans.getParameterBlockOffset(), cnt);

            cnt = ntTrans.getDataBlockCount();
            if (cnt > 0)
                transBuf.appendData(buf, ntTrans.getDataBlockOffset(), cnt);
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT Transaction [" + treeId + "] cmd=0x" + Integer.toHexString(subCmd) + ", multiPkt="
                    + transBuf.isMultiPacket());

        // Check for a multi-packet transaction, for a multi-packet transaction we just acknowledge
        // the receive with
        // an empty response SMB

        if (transBuf.isMultiPacket())
        {

            // Save the partial transaction data

            m_sess.setTransaction(transBuf);

            // Send an intermediate acknowedgement response

            m_sess.sendSuccessResponseSMB();
            return;
        }

        // Process the transaction buffer

        processNTTransactionBuffer(transBuf, ntTrans);
    }

    /**
     * Process an NT transaction secondary packet
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procNTTransactionSecondary(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that we received enough parameters for a transact2 request

        if (m_smbPkt.checkPacketIsValid(18, 0) == false)
        {

            // Not enough parameters for a valid transact2 request

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Check if the transaction request is for the IPC$ pipe

        if (conn.getSharedDevice().getType() == ShareType.ADMINPIPE)
        {
            IPCHandler.processIPCRequest(m_sess, outPkt);
            return;
        }

        // Check if there is an active transaction, and it is an NT transaction

        if (m_sess.hasTransaction() == false || m_sess.getTransaction().isType() != PacketType.NTTransact)
        {

            // No NT transaction to continue, return an error

            m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Create an NT transaction using the received packet

        NTTransPacket ntTrans = new NTTransPacket(m_smbPkt.getBuffer());
        byte[] buf = ntTrans.getBuffer();
        SrvTransactBuffer transBuf = m_sess.getTransaction();

        // Append the parameter data to the transaction buffer, if any

        int plen = ntTrans.getParameterBlockCount();
        if (plen > 0)
        {

            // Append the data to the parameter buffer

            DataBuffer paramBuf = transBuf.getParameterBuffer();
            paramBuf.appendData(buf, ntTrans.getParameterBlockOffset(), plen);
        }

        // Append the data block to the transaction buffer, if any

        int dlen = ntTrans.getDataBlockCount();
        if (dlen > 0)
        {

            // Append the data to the data buffer

            DataBuffer dataBuf = transBuf.getDataBuffer();
            dataBuf.appendData(buf, ntTrans.getDataBlockOffset(), dlen);
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT Transaction Secondary [" + treeId + "] paramLen=" + plen + ", dataLen=" + dlen);

        // Check if the transaction has been received or there are more sections to be received

        int totParam = ntTrans.getTotalParameterCount();
        int totData = ntTrans.getTotalDataCount();

        int paramDisp = ntTrans.getParameterBlockDisplacement();
        int dataDisp = ntTrans.getDataBlockDisplacement();

        if ((paramDisp + plen) == totParam && (dataDisp + dlen) == totData)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
                logger.debug("NT Transaction complete, processing ...");

            // Clear the in progress transaction

            m_sess.setTransaction(null);

            // Process the transaction

            processNTTransactionBuffer(transBuf, ntTrans);
        }

        // No response is sent for a transaction secondary
    }

    /**
     * Process an NT transaction buffer
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException If a network error occurs
     * @exception SMBSrvException If an SMB error occurs
     */
    private final void processNTTransactionBuffer(SrvTransactBuffer tbuf, NTTransPacket outPkt) throws IOException,
            SMBSrvException
    {

        // Process the NT transaction buffer

        switch (tbuf.getFunction())
        {

        // Create file/directory

        case PacketType.NTTransCreate:
            procNTTransactCreate(tbuf, outPkt);
            break;

        // I/O control

        case PacketType.NTTransIOCtl:
            procNTTransactIOCtl(tbuf, outPkt);
            break;

        // Query security descriptor

        case PacketType.NTTransQuerySecurityDesc:
            procNTTransactQuerySecurityDesc(tbuf, outPkt);
            break;

        // Set security descriptor

        case PacketType.NTTransSetSecurityDesc:
            procNTTransactSetSecurityDesc(tbuf, outPkt);
            break;

        // Rename file/directory via handle

        case PacketType.NTTransRename:
            procNTTransactRename(tbuf, outPkt);
            break;

        // Get user quota

        case PacketType.NTTransGetUserQuota:

            // Return a not implemented error status

            m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            break;

        // Set user quota

        case PacketType.NTTransSetUserQuota:

            // Return a not implemented error status

            m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            break;

        // Unknown NT transaction command

        default:
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            break;
        }
    }

    /**
     * Process an NT create file/directory transaction
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactCreate(SrvTransactBuffer tbuf, NTTransPacket outPkt) throws IOException,
            SMBSrvException
    {

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT TransactCreate");

        // Check that the received packet looks like a valid NT create transaction

        if (tbuf.hasParameterBuffer() && tbuf.getParameterBuffer().getLength() < 52)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = tbuf.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // If the connection is not a disk share then return an error.

        if (conn.getSharedDevice().getType() != ShareType.DISK)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Extract the file create parameters

        DataBuffer tparams = tbuf.getParameterBuffer();

        int flags = tparams.getInt();
        int rootFID = tparams.getInt();
        int accessMask = tparams.getInt();
        long allocSize = tparams.getLong();
        int attrib = tparams.getInt();
        int shrAccess = tparams.getInt();
        int createDisp = tparams.getInt();
        int createOptn = tparams.getInt();
        int sdLen = tparams.getInt();
        int eaLen = tparams.getInt();
        int nameLen = tparams.getInt();
        int impersonLev = tparams.getInt();
        int secFlags = tparams.getByte();

        // Extract the filename string

        tparams.wordAlign();
        String fileName = tparams.getString(nameLen, true);

        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Access the disk interface that is associated with the shared device

        DiskInterface disk = null;
        try
        {

            // Get the disk interface for the share

            disk = (DiskInterface) conn.getSharedDevice().getInterface();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check if the file name contains a file stream name. If the disk interface does not
        // implement the optional NTFS
        // streams interface then return an error status, not supported.

        if (fileName.indexOf(FileOpenParams.StreamSeparator) != -1)
        {

            // Check if the driver implements the NTFS streams interface and it is enabled

            boolean streams = false;

            if (disk instanceof NTFSStreamsInterface)
            {

                // Check if streams are enabled

                NTFSStreamsInterface ntfsStreams = (NTFSStreamsInterface) disk;
                streams = ntfsStreams.hasStreamsEnabled(m_sess, conn);
            }

            // Check if streams are enabled/available

            if (streams == false)
            {

                // Return a file not found error

                m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
                return;
            }
        }

        // Create the file open parameters to be passed to the disk interface

        FileOpenParams params = new FileOpenParams(fileName, createDisp, accessMask, attrib, shrAccess, allocSize,
                createOptn, rootFID, impersonLev, secFlags);
        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("NT TransactCreate [" + treeId + "] params=" + params + "  secDescLen=" + sdLen
                    + ", extAttribLen=" + eaLen);

        // Access the disk interface and open/create the requested file

        int fid;
        NetworkFile netFile = null;
        int respAction = 0;

        try
        {

            // Check if the requested file already exists

            int fileSts = disk.fileExists(m_sess, conn, fileName);

            if (fileSts == FileStatus.NotExist)
            {

                // Check if the file should be created if it does not exist

                if (createDisp == FileAction.NTCreate || createDisp == FileAction.NTOpenIf
                        || createDisp == FileAction.NTOverwriteIf || createDisp == FileAction.NTSupersede)
                {

                    // Check if a new file or directory should be created

                    if ((createOptn & WinNT.CreateDirectory) == 0)
                    {

                        // Create a new file

                        netFile = disk.createFile(m_sess, conn, params);
                    }
                    else
                    {

                        // Create a new directory and open it

                        disk.createDirectory(m_sess, conn, params);
                        netFile = disk.openFile(m_sess, conn, params);
                    }

                    // Indicate that the file did not exist and was created

                    respAction = FileAction.FileCreated;
                }
                else
                {

                    // Check if the path is a directory

                    if (fileSts == FileStatus.DirectoryExists)
                    {

                        // Return an access denied error

                        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                                SMBStatus.ErrDos);
                        return;
                    }
                    else
                    {

                        // Return a file not found error

                        m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound,
                                SMBStatus.ErrDos);
                        return;
                    }
                }
            }
            else if (createDisp == FileAction.NTCreate)
            {

                // Check for a file or directory

                if (fileSts == FileStatus.FileExists || fileSts == FileStatus.DirectoryExists)
                {

                    // Return a file exists error

                    m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                            SMBStatus.ErrDos);
                    return;
                }
                else
                {

                    // Return an access denied exception

                    m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    return;
                }
            }
            else
            {

                // Open the requested file/directory

                netFile = disk.openFile(m_sess, conn, params);

                // Check if the file should be truncated

                if (createDisp == FileAction.NTSupersede || createDisp == FileAction.NTOverwriteIf)
                {

                    // Truncate the file

                    disk.truncateFile(m_sess, conn, netFile, 0L);

                    // Debug

                    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
                        logger.debug("  [" + treeId + "] name=" + fileName + " truncated");
                }

                // Set the file action response

                respAction = FileAction.FileExisted;
            }

            // Add the file to the list of open files for this tree connection

            fid = conn.addFile(netFile, getSession());
        }
        catch (TooManyFilesException ex)
        {

            // Too many files are open on this connection, cannot open any more files.

            m_sess.sendErrorResponseSMB(SMBStatus.NTTooManyOpenFiles, SMBStatus.DOSTooManyOpenFiles, SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Return an access denied error

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileExistsException ex)
        {

            // File/directory already exists

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                    SMBStatus.ErrDos);
            return;
        }
        catch (FileSharingException ex)
        {

            // Return a sharing violation error

            m_sess.sendErrorResponseSMB(SMBStatus.NTSharingViolation, SMBStatus.DOSFileSharingConflict,
                    SMBStatus.ErrDos);
            return;
        }
        catch (FileOfflineException ex)
        {

            // File data is unavailable

            m_sess.sendErrorResponseSMB(SMBStatus.NTFileOffline, SMBStatus.HRDDriveNotReady, SMBStatus.ErrHrd);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNotFound, SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the NT transaction create response

        DataBuffer prms = new DataBuffer(128);

        // If an oplock was requested indicate it was granted, for now

        if ((flags & WinNT.RequestBatchOplock) != 0)
        {

            // Batch oplock granted

            prms.putByte(2);
        }
        else if ((flags & WinNT.RequestOplock) != 0)
        {

            // Exclusive oplock granted

            prms.putByte(1);
        }
        else
        {

            // No oplock granted

            prms.putByte(0);
        }
        prms.putByte(0); // alignment

        // Pack the file id

        prms.putShort(fid);
        prms.putInt(respAction);

        // EA error offset

        prms.putInt(0);

        // Pack the file/directory dates

        if (netFile.hasCreationDate())
            prms.putLong(NTTime.toNTTime(netFile.getCreationDate()));
        else
            prms.putLong(0);

        if (netFile.hasModifyDate())
        {
            long modDate = NTTime.toNTTime(netFile.getModifyDate());
            prms.putLong(modDate);
            prms.putLong(modDate);
            prms.putLong(modDate);
        }
        else
        {
            prms.putLong(0); // Last access time
            prms.putLong(0); // Last write time
            prms.putLong(0); // Change time
        }

        prms.putInt(netFile.getFileAttributes());

        // Pack the file size/allocation size

        prms.putLong(netFile.getFileSize()); // Allocation size
        prms.putLong(netFile.getFileSize()); // End of file
        prms.putShort(0); // File type - disk file
        prms.putShort(0); // Device state
        prms.putByte(netFile.isDirectory() ? 1 : 0);

        // Initialize the transaction response

        outPkt.initTransactReply(prms.getBuffer(), prms.getLength(), null, 0);

        // Send back the response

        m_sess.sendResponseSMB(outPkt);

        // Check if there are any file/directory change notify requests active

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler() && respAction == FileAction.FileCreated)
        {

            // Check if a file or directory has been created

            if (netFile.isDirectory())
                diskCtx.getChangeHandler().notifyDirectoryChanged(NotifyChange.ActionAdded, fileName);
            else
                diskCtx.getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, fileName);
        }
    }

    /**
     * Process an NT I/O control transaction
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactIOCtl(SrvTransactBuffer tbuf, NTTransPacket outPkt) throws IOException,
            SMBSrvException
    {
        
        // Get the tree connection details

        int treeId = tbuf.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null) {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Unpack the request details

        DataBuffer setupBuf = tbuf.getSetupBuffer();    
    
        int ctrlCode = setupBuf.getInt();
        int fid      = setupBuf.getShort();
        boolean fsctrl = setupBuf.getByte() == 1 ? true : false;
        int filter   = setupBuf.getByte();      

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT IOCtl code=" + NTIOCtl.asString(ctrlCode) + ", fid=" + fid + ", fsctrl=" + fsctrl + ", filter=" + filter);

        // Access the disk interface that is associated with the shared device

        DiskInterface disk = null;
        try {
            
            // Get the disk interface for the share
            
            disk = (DiskInterface) conn.getSharedDevice().getInterface();
        }
        catch (InvalidDeviceInterfaceException ex) {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check if the disk interface implements the optional IO control interface
    
        if ( disk instanceof IOCtlInterface) {

            // Access the IO control interface
      
            IOCtlInterface ioControl = (IOCtlInterface) disk;
      
            try {
        
                // Pass the request to the IO control interface for processing
                
                DataBuffer response = ioControl.processIOControl(m_sess, conn, ctrlCode, fid, tbuf.getDataBuffer(), fsctrl, filter);
                
                // Pack the response
                
                if ( response != null) {
                  
                    // Pack the response data block
                  
                    outPkt.initTransactReply(null, 0, response.getBuffer(), response.getLength(), 1);
                    outPkt.setSetupParameter(0, response.getLength());
                }
                else {
                  
                    // Pack an empty response data block
                  
                    outPkt.initTransactReply(null, 0, null, 0, 1);
                    outPkt.setSetupParameter(0, 0);
                }
            }
            catch (IOControlNotImplementedException ex) {
            
                // Return a not implemented error status
            
                m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented, SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);
                return;
            }
            catch (SMBException ex) {
            
                // Return the specified SMB status, this should be an NT status code
            
                m_sess.sendErrorResponseSMB(ex.getErrorCode(), SMBStatus.SRVInternalServerError, SMBStatus.ErrSrv);
                return;
            }
          
            // Send the IOCtl response
              
            m_sess.sendResponseSMB(outPkt);     
        }
        else {
          
            // Send back an error, IOctl not supported
        
            m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented, SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
        }
    }
    
    /**
     * Process an NT query security descriptor transaction
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactQuerySecurityDesc(SrvTransactBuffer tbuf, NTTransPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Get the tree connection details

        int treeId = tbuf.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Unpack the request details

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        int fid = paramBuf.getShort();
        int flags = paramBuf.getShort();

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT QuerySecurityDesc fid=" + fid + ", flags=" + flags);

        // Get the file details

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

    	// Return an empty security descriptor
    	
        byte[] paramblk = new byte[4];
        DataPacker.putIntelInt(0, paramblk, 0);

        outPkt.initTransactReply(paramblk, paramblk.length, null, 0);

        // Send back the response

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process an NT set security descriptor transaction
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactSetSecurityDesc(SrvTransactBuffer tbuf, NTTransPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Unpack the request details

        DataBuffer paramBuf = tbuf.getParameterBuffer();

        // Get the tree connection details

        int treeId = tbuf.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file details

        int fid = paramBuf.getShort();
        paramBuf.skipBytes(2);
        int flags = paramBuf.getInt();

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT SetSecurityDesc fid=" + fid + ", flags=" + flags);

        // Send back an error, security descriptors not supported

        m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
    }

    /**
     * Process an NT change notification transaction
     * 
     * @param ntpkt NTTransPacket
     * @param outPkt SMBSrvPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactNotifyChange(NTTransPacket ntpkt, SMBSrvPacket outPkt) throws IOException,
            SMBSrvException
    {

        // Get the tree connection details

        int treeId = ntpkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Make sure the tree connection is for a disk device

        if (conn.getContext() == null || conn.getContext() instanceof DiskDeviceContext == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Check if the device has change notification enabled

        DiskDeviceContext diskCtx = (DiskDeviceContext) conn.getContext();
        if (diskCtx.hasChangeHandler() == false)
        {

            // Return an error status, share does not have change notification enabled

            m_sess.sendErrorResponseSMB(SMBStatus.NTNotImplemented, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Unpack the request details

        ntpkt.resetSetupPointer();

        int filter = ntpkt.unpackInt();
        int fid = ntpkt.unpackWord();
        boolean watchTree = ntpkt.unpackByte() == 1 ? true : false;
        int mid = ntpkt.getMultiplexId();

        // Get the file details

        NetworkFile dir = conn.findFile(fid);
        if (dir == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            return;
        }

        // Get the maximum notifications to buffer whilst waiting for the request to be reset after
        // a notification
        // has been triggered

        int maxQueue = 0;

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
            logger.debug("NT NotifyChange fid=" + fid + ", mid=" + mid + ", filter=0x" + Integer.toHexString(filter)
                    + ", dir=" + dir.getFullName() + ", maxQueue=" + maxQueue);

        // Check if there is an existing request in the notify list that matches the new request and
        // is in a completed
        // state. If so then the client is resetting the notify request so reuse the existing
        // request.

        NotifyRequest req = m_sess.findNotifyRequest(dir, filter, watchTree);

        if (req != null && req.isCompleted())
        {

            // Reset the existing request with the new multiplex id

            req.setMultiplexId(mid);
            req.setCompleted(false);

            // Check if there are any buffered notifications for this session

            if (req.hasBufferedEvents() || req.hasNotifyEnum())
            {

                // Get the buffered events from the request, clear the list from the request

                NotifyChangeEventList bufList = req.getBufferedEventList();
                req.clearBufferedEvents();

                // Send the buffered events

                diskCtx.getChangeHandler().sendBufferedNotifications(req, bufList);

                // DEBUG

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
                {
                    if (bufList == null)
                        logger.debug("   Sent buffered notifications, req=" + req.toString() + ", Enum");
                    else
                        logger.debug("   Sent buffered notifications, req=" + req.toString() + ", count="
                                + bufList.numberOfEvents());
                }
            }
            else
            {

                // DEBUG

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
                    logger.debug("   Reset notify request, " + req.toString());
            }
        }
        else
        {

            // Create a change notification request

            req = new NotifyRequest(filter, watchTree, m_sess, dir, mid, ntpkt.getTreeId(), ntpkt.getProcessId(), ntpkt
                    .getUserId(), maxQueue);

            // Add the request to the pending notify change lists

            m_sess.addNotifyRequest(req, diskCtx);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_NOTIFY))
                logger.debug("   Added new request, " + req.toString());
        }

        // NOTE: If the change notification request is accepted then no reply is sent to the client.
        // A reply will be sent
        // asynchronously if the change notification is triggered.
    }

    /**
     * Process an NT rename via handle transaction
     * 
     * @param tbuf TransactBuffer
     * @param outPkt NTTransPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    protected final void procNTTransactRename(SrvTransactBuffer tbuf, NTTransPacket outPkt) throws IOException,
            SMBSrvException
    {

        // Unpack the request details

//        DataBuffer paramBuf = tbuf.getParameterBuffer();

        // Get the tree connection details

        int treeId = tbuf.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.NTInvalidParameter, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TRAN))
            logger.debug("NT TransactRename");

        // Send back an error, NT rename not supported

        m_sess.sendErrorResponseSMB(SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
    }
}