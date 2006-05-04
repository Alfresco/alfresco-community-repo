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

import java.io.IOException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.InvalidUserException;
import org.alfresco.filesys.server.core.InvalidDeviceInterfaceException;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.filesys.AccessDeniedException;
import org.alfresco.filesys.server.filesys.AccessMode;
import org.alfresco.filesys.server.filesys.DirectoryNotEmptyException;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileAccess;
import org.alfresco.filesys.server.filesys.FileAction;
import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileExistsException;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileOpenParams;
import org.alfresco.filesys.server.filesys.FileSharingException;
import org.alfresco.filesys.server.filesys.FileStatus;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.TooManyConnectionsException;
import org.alfresco.filesys.server.filesys.TooManyFilesException;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.server.filesys.VolumeInfo;
import org.alfresco.filesys.smb.Capability;
import org.alfresco.filesys.smb.DataType;
import org.alfresco.filesys.smb.InvalidUNCPathException;
import org.alfresco.filesys.smb.PCShare;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBDate;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.WildCard;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Core SMB protocol handler class.
 */
class CoreProtocolHandler extends ProtocolHandler
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Special resume ids for '.' and '..' pseudo directories

    private static final int RESUME_START = 0x00008003;
    private static final int RESUME_DOT = 0x00008002;
    private static final int RESUME_DOTDOT = 0x00008001;

    // Maximum value that can be stored in a parameter word

    private static final int MaxWordValue = 0x0000FFFF;

    // SMB packet class

    protected SMBSrvPacket m_smbPkt;

    /**
     * Create a new core SMB protocol handler.
     */
    protected CoreProtocolHandler()
    {
    }

    /**
     * Class constructor
     * 
     * @param sess SMBSrvSession
     */
    protected CoreProtocolHandler(SMBSrvSession sess)
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
        return "Core Protocol";
    }

    /**
     * Map a Java exception class to an SMB error code, and return an error response to the caller.
     * 
     * @param ex java.lang.Exception
     */
    protected final void MapExceptionToSMBError(Exception ex)
    {

    }

    /**
     * Pack file information for a search into the specified buffer.
     * 
     * @param buf byte[] Buffer to store data.
     * @param bufpos int Position to start storing data.
     * @param searchStr Search context string.
     * @param resumeId int Resume id
     * @param searchId Search context id
     * @param info File data to be packed.
     * @return int Next available buffer position.
     */
    protected final int packSearchInfo(byte[] buf, int bufPos, String searchStr, int resumeId, int searchId,
            FileInfo info)
    {

        // Pack the resume key

        CoreResumeKey.putResumeKey(buf, bufPos, searchStr, resumeId + (searchId << 16));
        bufPos += CoreResumeKey.LENGTH;

        // Pack the file information

        buf[bufPos++] = (byte) (info.getFileAttributes() & 0x00FF);

        SMBDate dateTime = new SMBDate(info.getModifyDateTime());
        if (dateTime != null)
        {
            DataPacker.putIntelShort(dateTime.asSMBTime(), buf, bufPos);
            DataPacker.putIntelShort(dateTime.asSMBDate(), buf, bufPos + 2);
        }
        else
        {
            DataPacker.putIntelShort(0, buf, bufPos);
            DataPacker.putIntelShort(0, buf, bufPos + 2);
        }
        bufPos += 4;

        DataPacker.putIntelInt((int) info.getSize(), buf, bufPos);
        bufPos += 4;

        StringBuffer strBuf = new StringBuffer();
        strBuf.append(info.getFileName());

        while (strBuf.length() < 13)
            strBuf.append('\0');

        if (strBuf.length() > 12)
            strBuf.setLength(12);

        DataPacker.putString(strBuf.toString().toUpperCase(), buf, bufPos, true);
        bufPos += 13;

        // Return the new buffer position

        return bufPos;
    }

    /**
     * Check if the specified path exists, and is a directory.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException if an SMB protocol error occurs
     */
    protected void procCheckDirectory(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid check directory request

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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the directory name

        String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (dirName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Directory Check [" + treeId + "] name=" + dirName);

        // Access the disk interface and check for the directory

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Check that the specified path exists, and it is a directory

            if (disk.fileExists(m_sess, conn, dirName) == FileStatus.DirectoryExists)
            {

                // The path exists and is a directory, build the valid path response.

                outPkt.setParameterCount(0);
                outPkt.setByteCount(0);

                // Send the response packet

                m_sess.sendResponseSMB(outPkt);
            }
            else
            {

                // The path does not exist, or is not a directory.
                //
                // DOS clients depend on the 'Directory Invalid' (SMB_ERR_BAD_PATH) message being
                // returned.

                m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid, SMBStatus.ErrDos);
            }
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to delete the directory

            m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid, SMBStatus.ErrDos);
            return;
        }
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        int ftime = m_smbPkt.getParameter(1);
        int fdate = m_smbPkt.getParameter(2);

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
    }

    /**
     * Create a new directory.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procCreateDirectory(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid create directory request

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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the directory name

        String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (dirName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Directory Create [" + treeId + "] name=" + dirName);

        // Access the disk interface and create the new directory

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Directory creation parameters

            FileOpenParams params = new FileOpenParams(dirName, FileAction.CreateNotExist, AccessMode.ReadWrite,
                    FileAttribute.NTDirectory);

            // Create the new directory

            disk.createDirectory(m_sess, conn, params);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (FileExistsException ex)
        {

            // Failed to create the directory

            m_sess.sendErrorResponseSMB(SMBStatus.NTObjectNameCollision, SMBStatus.DOSFileAlreadyExists,
                    SMBStatus.ErrDos);
            return;
        }
        catch (AccessDeniedException ex)
        {

            // Not allowed to create directory

            m_sess.sendErrorResponseSMB(SMBStatus.NTAccessDenied, SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to create the directory

            m_sess.sendErrorResponseSMB(SMBStatus.DOSDirectoryInvalid, SMBStatus.ErrDos);
            return;
        }

        // Build the create directory response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Create a new file on the server.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procCreateFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file create request

        if (m_smbPkt.checkPacketIsValid(3, 2) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the file name

        String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Get the required file attributes for the new file

        int attr = m_smbPkt.getParameter(0);

        // Create the file parameters to be passed to the disk interface

        FileOpenParams params = new FileOpenParams(fileName, FileAction.CreateNotExist, AccessMode.ReadWrite, attr);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Create [" + treeId + "] params=" + params);

        // Access the disk interface and create the new file

        int fid;
        NetworkFile netFile = null;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Create the new file

            netFile = disk.createFile(m_sess, conn, params);

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
        catch (FileExistsException ex)
        {

            // File with the requested name already exists

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileAlreadyExists, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the create file response

        outPkt.setParameterCount(1);
        outPkt.setParameter(0, fid);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Create a temporary file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procCreateTemporaryFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

    }

    /**
     * Delete a directory.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the directory name

        String dirName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());

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
    }

    /**
     * Delete a file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the file name

        String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Delete [" + treeId + "] name=" + fileName);

        // Access the disk interface and delete the file(s)

        int fid;
        NetworkFile netFile = null;

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
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the delete file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Get disk attributes processing.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procDiskAttributes(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("Get disk attributes");

        // Parameter and byte count should be zero

        if (m_smbPkt.getParameterCount() != 0 && m_smbPkt.getByteCount() != 0)
        {

            // Send an error response

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the disk interface from the shared device

        DiskInterface disk = null;
        DiskDeviceContext diskCtx = null;

        try
        {
            disk = (DiskInterface) conn.getSharedDevice().getInterface();
            diskCtx = (DiskDeviceContext) conn.getContext();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Create a disk information object and ask the disk interface to fill in the details

        SrvDiskInfo diskInfo = getDiskInformation(disk, diskCtx);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_INFO))
            logger.debug("  Disk info - total=" + diskInfo.getTotalUnits() + ", free=" + diskInfo.getFreeUnits()
                    + ", blocksPerUnit=" + diskInfo.getBlocksPerAllocationUnit() + ", blockSize="
                    + diskInfo.getBlockSize());

        // Check if the disk size information needs scaling to fit into 16bit values

        long totUnits = diskInfo.getTotalUnits();
        long freeUnits = diskInfo.getFreeUnits();
        int blocksUnit = diskInfo.getBlocksPerAllocationUnit();

        while (totUnits > MaxWordValue && blocksUnit <= MaxWordValue)
        {

            // Increase the blocks per unit and decrease the total/free units

            blocksUnit *= 2;

            totUnits = totUnits / 2L;
            freeUnits = freeUnits / 2L;
        }

        // Check if the total/free units fit into a 16bit value

        if (totUnits > MaxWordValue || blocksUnit > MaxWordValue)
        {

            // Just use dummy values, cannot fit the disk size into 16bits

            totUnits = MaxWordValue;

            if (freeUnits > MaxWordValue)
                freeUnits = MaxWordValue / 2;

            if (blocksUnit > MaxWordValue)
                blocksUnit = MaxWordValue;
        }

        // Build the reply SMB

        outPkt.setParameterCount(5);

        outPkt.setParameter(0, (int) totUnits);
        outPkt.setParameter(1, blocksUnit);
        outPkt.setParameter(2, diskInfo.getBlockSize());
        outPkt.setParameter(3, (int) freeUnits);
        outPkt.setParameter(4, 0);

        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Echo packet request.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procEcho(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid echo request

        if (m_smbPkt.checkPacketIsValid(1, 0) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the echo count from the request

        int echoCnt = m_smbPkt.getParameter(0);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_ECHO))
            logger.debug("Echo - Count = " + echoCnt);

        // Loop until all echo packets have been sent

        int echoSeq = 1;

        while (echoCnt > 0)
        {

            // Set the echo response sequence number

            outPkt.setParameter(0, echoSeq++);

            // Echo the received packet

            m_sess.sendResponseSMB(outPkt);
            echoCnt--;

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_ECHO))
                logger.debug("Echo Packet, Seq = " + echoSeq);
        }
    }

    /**
     * Flush the specified file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procFlushFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file flush request

        if (m_smbPkt.checkPacketIsValid(1, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Flush [" + netFile.getFileId() + "]");

        // Flush the file

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Flush the file

            disk.flushFile(m_sess, conn, netFile);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
                logger.debug("File Flush Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to read the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }

        // Send the flush response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Get the file attributes for the specified file.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procGetFileAttributes(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid query file information request

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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the file name

        String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Get File Information [" + treeId + "] name=" + fileName);

        // Access the disk interface and get the file information

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Get the file information for the specified file/directory

            FileInfo finfo = disk.getFileInformation(m_sess, conn, fileName);
            if (finfo != null)
            {

                // Check if the share is read-only, if so then force the read-only flag for the file

                if (conn.getSharedDevice().isReadOnly() && finfo.isReadOnly() == false)
                {

                    // Make sure the read-only attribute is set

                    finfo.setFileAttributes(finfo.getFileAttributes() + FileAttribute.ReadOnly);
                }

                // Return the file information

                outPkt.setParameterCount(10);
                outPkt.setParameter(0, finfo.getFileAttributes());
                if (finfo.getModifyDateTime() != 0L)
                {
                    SMBDate dateTime = new SMBDate(finfo.getModifyDateTime());
                    outPkt.setParameter(1, dateTime.asSMBTime());
                    outPkt.setParameter(2, dateTime.asSMBDate());
                }
                else
                {
                    outPkt.setParameter(1, 0);
                    outPkt.setParameter(2, 0);
                }
                outPkt.setParameter(3, (int) finfo.getSize() & 0x0000FFFF);
                outPkt.setParameter(4, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

                for (int i = 5; i < 10; i++)
                    outPkt.setParameter(i, 0);

                outPkt.setByteCount(0);

                // Send the response packet

                m_sess.sendResponseSMB(outPkt);
                return;
            }
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

        // Failed to get the file information

        m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
    }

    /**
     * Get file information.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procGetFileInformation(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid query file information2 request

        if (m_smbPkt.checkPacketIsValid(1, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Get File Information 2 [" + netFile.getFileId() + "]");

        // Access the disk interface and get the file information

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Get the file information for the specified file/directory

            FileInfo finfo = disk.getFileInformation(m_sess, conn, netFile.getFullName());
            if (finfo != null)
            {

                // Check if the share is read-only, if so then force the read-only flag for the file

                if (conn.getSharedDevice().isReadOnly() && finfo.isReadOnly() == false)
                {

                    // Make sure the read-only attribute is set

                    finfo.setFileAttributes(finfo.getFileAttributes() + FileAttribute.ReadOnly);
                }

                // Initialize the return packet, no data bytes

                outPkt.setParameterCount(11);
                outPkt.setByteCount(0);

                // Return the file information
                //
                // Creation date/time

                SMBDate dateTime = new SMBDate(0);

                if (finfo.getCreationDateTime() != 0L)
                {
                    dateTime.setTime(finfo.getCreationDateTime());
                    outPkt.setParameter(0, dateTime.asSMBDate());
                    outPkt.setParameter(1, dateTime.asSMBTime());
                }
                else
                {
                    outPkt.setParameter(0, 0);
                    outPkt.setParameter(1, 0);
                }

                // Access date/time

                if (finfo.getAccessDateTime() != 0L)
                {
                    dateTime.setTime(finfo.getAccessDateTime());
                    outPkt.setParameter(2, dateTime.asSMBDate());
                    outPkt.setParameter(3, dateTime.asSMBTime());
                }
                else
                {
                    outPkt.setParameter(2, 0);
                    outPkt.setParameter(3, 0);
                }

                // Modify date/time

                if (finfo.getModifyDateTime() != 0L)
                {
                    dateTime.setTime(finfo.getModifyDateTime());
                    outPkt.setParameter(4, dateTime.asSMBDate());
                    outPkt.setParameter(5, dateTime.asSMBTime());
                }
                else
                {
                    outPkt.setParameter(4, 0);
                    outPkt.setParameter(5, 0);
                }

                // File data size

                outPkt.setParameter(6, (int) finfo.getSize() & 0x0000FFFF);
                outPkt.setParameter(7, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

                // File allocation size

                outPkt.setParameter(8, (int) finfo.getSize() & 0x0000FFFF);
                outPkt.setParameter(9, (int) (finfo.getSize() & 0xFFFF0000) >> 16);

                // File attributes

                outPkt.setParameter(10, finfo.getFileAttributes());

                // Send the response packet

                m_sess.sendResponseSMB(outPkt);
                return;
            }
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

        // Failed to get the file information

        m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
    }

    /**
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procLockFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid lock file request

        if (m_smbPkt.checkPacketIsValid(5, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        long lockcnt = m_smbPkt.getParameterLong(1);
        long lockoff = m_smbPkt.getParameterLong(3);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Lock [" + netFile.getFileId() + "] : Offset=" + lockoff + " ,Count=" + lockcnt);

        // ***** Always return a success status, simulated locking ****
        //
        // Build the lock file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Open a file on the server.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procOpenFile(SMBSrvPacket outPkt) throws IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file open request

        if (m_smbPkt.checkPacketIsValid(2, 2) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the file name

        String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Get the required access mode and the file attributes

        int mode = m_smbPkt.getParameter(0);
        int attr = m_smbPkt.getParameter(1);

        // Create the file open parameters to be passed to the disk interface

        FileOpenParams params = new FileOpenParams(fileName, mode, AccessMode.ReadWrite, attr);
        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Open [" + treeId + "] params=" + params);

        // Access the disk interface and open the requested file

        int fid;
        NetworkFile netFile = null;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Open the requested file

            netFile = disk.openFile(m_sess, conn, params);

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

            // File is not accessible, or file is actually a directory

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (FileSharingException ex)
        {

            // Return a sharing violation error

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileSharingConflict, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the open file response

        outPkt.setParameterCount(7);

        outPkt.setParameter(0, fid);
        outPkt.setParameter(1, 0); // file attributes

        if (netFile.hasModifyDate())
        {
            outPkt.setParameterLong(2, (int) (netFile.getModifyDate() / 1000L));

            // SMBDate smbDate = new SMBDate(netFile.getModifyDate());
            // outPkt.setParameter(2, smbDate.asSMBTime()); // last write time
            // outPkt.setParameter(3, smbDate.asSMBDate()); // last write date
        }
        else
            outPkt.setParameterLong(2, 0);

        outPkt.setParameterLong(4, netFile.getFileSizeInt()); // file size
        outPkt.setParameter(6, netFile.getGrantedAccess());

        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process exit, close all open files.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procProcessExit(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid process exit request

        if (m_smbPkt.checkPacketIsValid(0, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Process Exit - Open files = " + conn.openFileCount());

        // Close all open files

        if (conn.openFileCount() > 0)
        {

            // Close all files on the connection

            conn.closeConnection(getSession());
        }

        // Build the process exit response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Read from a file that has been opened on the server.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procReadFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file read request

        if (m_smbPkt.checkPacketIsValid(5, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        int reqcnt = m_smbPkt.getParameter(1);
        int reqoff = m_smbPkt.getParameter(2) + (m_smbPkt.getParameter(3) << 16);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Read [" + netFile.getFileId() + "] : Size=" + reqcnt + " ,Pos=" + reqoff);

        // Read data from the file

        byte[] buf = outPkt.getBuffer();
        int rdlen = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Check if the required read size will fit into the reply packet

            int dataOff = outPkt.getByteOffset() + 3;
            int availCnt = buf.length - dataOff;
            if (m_sess.hasClientCapability(Capability.LargeRead) == false)
                availCnt = m_sess.getClientMaximumBufferSize() - dataOff;

            if (availCnt < reqcnt)
            {

                // Limit the file read size

                reqcnt = availCnt;

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                    logger.debug("File Read [" + netFile.getFileId() + "] Limited to " + availCnt);
            }

            // Read from the file

            rdlen = disk.readFile(m_sess, conn, netFile, buf, outPkt.getByteOffset() + 3, reqcnt, reqoff);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("File Read Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to read the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the data block

        int bytOff = outPkt.getByteOffset();
        buf[bytOff] = (byte) DataType.DataBlock;
        DataPacker.putIntelShort(rdlen, buf, bytOff + 1);
        outPkt.setByteCount(rdlen + 3); // data type + 16bit length

        outPkt.setParameter(0, rdlen);
        outPkt.setParameter(1, 0);
        outPkt.setParameter(2, 0);
        outPkt.setParameter(3, 0);
        outPkt.setParameter(4, 0);

        // Send the read response

        m_sess.sendResponseSMB(outPkt);
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the old file name

        boolean isUni = m_smbPkt.isUnicode();
        String oldName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, isUni);
        if (oldName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Update the data position

        if (isUni)
        {
            int len = (oldName.length() * 2) + 2;
            dataPos = DataPacker.wordAlign(dataPos + 1) + len;
            dataLen -= len;
        }
        else
        {
            dataPos += oldName.length() + 2; // string length + null + data type
            dataLen -= oldName.length() + 2;
        }

        // Extract the new file name

        String newName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, isUni);
        if (newName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Rename [" + treeId + "] old name=" + oldName + ", new name=" + newName);

        // Access the disk interface and rename the requested file

        int fid;
        NetworkFile netFile = null;

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
        catch (java.io.IOException ex)
        {

            // Failed to open the file

            m_sess.sendErrorResponseSMB(SMBStatus.DOSFileNotFound, SMBStatus.ErrDos);
            return;
        }

        // Build the rename file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Start/continue a directory search operation.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procSearch(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid search request

        if (m_smbPkt.checkPacketIsValid(2, 5) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the maximum number of entries to return and the search file attributes

        int maxFiles = m_smbPkt.getParameter(0);
        int srchAttr = m_smbPkt.getParameter(1);

        // Check if this is a volume label request

        if ((srchAttr & FileAttribute.Volume) != 0)
        {

            // Process the volume label request

            procSearchVolumeLabel(outPkt);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the search file name

        String srchPath = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());

        if (srchPath == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidFunc, SMBStatus.ErrDos);
            return;
        }

        // Update the received data position

        dataPos += srchPath.length() + 2;
        dataLen -= srchPath.length() + 2;

        int resumeLen = 0;

        if (buf[dataPos++] == DataType.VariableBlock)
        {

            // Extract the resume key length

            resumeLen = DataPacker.getIntelShort(buf, dataPos);

            // Adjust remaining the data length and position

            dataLen -= 3; // block type + resume key length short
            dataPos += 2; // resume key length short

            // Check that we received enough data

            if (resumeLen > dataLen)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
                return;
            }
        }

        // Access the shared devices disk interface

        SearchContext ctx = null;
        DiskInterface disk = null;

        try
        {
            disk = (DiskInterface) conn.getSharedDevice().getInterface();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check if this is the start of a new search

        byte[] resumeKey = null;
        int searchId = -1;

        // Default resume point is at the start of the directory, at the '.' directory if
        // directories are
        // being returned.

        int resumeId = RESUME_START;

        if (resumeLen == 0 && srchPath.length() > 0)
        {

            // Allocate a search slot for the new search

            searchId = m_sess.allocateSearchSlot();
            if (searchId == -1)
            {

                // Try and find any 'leaked' searches, ie. searches that have been started but not
                // closed.
                //
                // Windows Explorer seems to leak searches after a new folder has been created, a
                // search for '????????.???'
                // is started but never continued.

                int idx = 0;
                ctx = m_sess.getSearchContext(idx);

                while (ctx != null && searchId == -1)
                {

                    // Check if the current search context looks like a leaked search.

                    if (ctx.getSearchString().compareTo("????????.???") == 0)
                    {

                        // Debug

                        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                            logger.debug("Release leaked search [" + idx + "]");

                        // Deallocate the search context

                        m_sess.deallocateSearchSlot(idx);

                        // Allocate the slot for the new search

                        searchId = m_sess.allocateSearchSlot();
                    }
                    else
                    {

                        // Update the search index and get the next search context

                        ctx = m_sess.getSearchContext(++idx);
                    }
                }

                // Check if we freed up a search slot

                if (searchId == -1)
                {

                    // Failed to allocate a slot for the new search

                    m_sess.sendErrorResponseSMB(SMBStatus.SRVNoResourcesAvailable, SMBStatus.ErrSrv);
                    return;
                }
            }

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Start search [" + searchId + "] - " + srchPath + ", attr=0x"
                        + Integer.toHexString(srchAttr) + ", maxFiles=" + maxFiles);

            // Start a new search

            ctx = disk.startSearch(m_sess, conn, srchPath, srchAttr);
            if (ctx != null)
            {

                // Store details of the search in the context

                ctx.setTreeId(treeId);
                ctx.setMaximumFiles(maxFiles);
            }

            // Save the search context

            m_sess.setSearchContext(searchId, ctx);
        }
        else
        {

            // Take a copy of the resume key

            resumeKey = new byte[CoreResumeKey.LENGTH];
            CoreResumeKey.getResumeKey(buf, dataPos, resumeKey);

            // Get the search context slot id from the resume key, and get the search context.

            int id = CoreResumeKey.getServerArea(resumeKey, 0);
            searchId = (id & 0xFFFF0000) >> 16;
            ctx = m_sess.getSearchContext(searchId);

            // Check if the search context is valid

            if (ctx == null)
            {
                m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
                return;
            }

            // Get the resume id from the resume key

            resumeId = id & 0x0000FFFF;

            // Restart the search at the resume point, check if the resume point is already set, ie.
            // we are just continuing the search.

            if (resumeId < RESUME_DOTDOT && ctx.getResumeId() != resumeId)
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("Search resume at " + resumeId);

                // Restart the search at the specified point

                if (ctx.restartAt(resumeId) == false)
                {

                    // Debug

                    if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                        logger.debug("Search restart failed");

                    // Failed to restart the search

                    m_sess.sendErrorResponseSMB(SMBStatus.DOSNoMoreFiles, SMBStatus.ErrDos);

                    // Release the search context

                    m_sess.deallocateSearchSlot(searchId);
                    return;
                }
            }
        }

        // Check if the search context is valid

        if (ctx == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Check that the search context and tree connection match

        if (ctx.getTreeId() != treeId)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
            return;
        }

        // Start building the search response packet

        outPkt.setParameterCount(1);
        int bufPos = outPkt.getByteOffset();
        buf[bufPos] = (byte) DataType.VariableBlock;
        bufPos += 3; // save two bytes for the actual block length
        int fileCnt = 0;

        // Check if this is the start of a wildcard search and includes directories

        if ((srchAttr & FileAttribute.Directory) != 0 && resumeId >= RESUME_DOTDOT
                && WildCard.containsWildcards(srchPath))
        {

            // The first entries in the search should be the '.' and '..' entries for the
            // current/parent
            // directories.
            //
            // Remove the file name from the search path, and get the file information for the
            // search
            // directory.

            String workDir = FileName.removeFileName(srchPath);
            FileInfo dirInfo = disk.getFileInformation(m_sess, conn, workDir);

            // Check if we have valid information for the working directory

            if (dirInfo != null)
                dirInfo = new FileInfo(".", 0, FileAttribute.Directory);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Search adding . and .. entries:  " + dirInfo.toString());

            // Reset the file name to '.' and pack the directory information

            if (resumeId == RESUME_START)
            {

                // Pack the '.' file information

                dirInfo.setFileName(".");
                resumeId = RESUME_DOT;
                bufPos = packSearchInfo(buf, bufPos, ctx.getSearchString(), RESUME_DOT, searchId, dirInfo);

                // Update the file count

                fileCnt++;
            }

            // Reset the file name to '..' and pack the directory information

            if (resumeId == RESUME_DOT)
            {

                // Pack the '..' file information

                dirInfo.setFileName("..");
                bufPos = packSearchInfo(buf, bufPos, ctx.getSearchString(), RESUME_DOTDOT, searchId, dirInfo);

                // Update the file count

                fileCnt++;
            }
        }

        // Get files from the search and pack into the return packet

        FileInfo fileInfo = new FileInfo();

        while (fileCnt < ctx.getMaximumFiles() && ctx.nextFileInfo(fileInfo) == true)
        {

            // Check for . files, ignore them.
            //
            // ** Should check for . and .. file names **

            if (fileInfo.getFileName().startsWith("."))
                continue;

            // Get the resume id for the current file/directory

            resumeId = ctx.getResumeId();

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("Search return file " + fileInfo.toString() + ", resumeId=" + resumeId);

            // Check if the share is read-only, if so then force the read-only flag for the file

            if (conn.getSharedDevice().isReadOnly() && fileInfo.isReadOnly() == false)
            {

                // Make sure the read-only attribute is set

                fileInfo.setFileAttributes(fileInfo.getFileAttributes() + FileAttribute.ReadOnly);
            }

            // Pack the file information

            bufPos = packSearchInfo(buf, bufPos, ctx.getSearchString(), resumeId, searchId, fileInfo);

            // Update the file count, reset the current file information

            fileCnt++;
            fileInfo.resetInfo();
        }

        // Check if any files were found

        if (fileCnt == 0)
        {

            // Send a repsonse that indicates that the search has finished

            outPkt.setParameterCount(1);
            outPkt.setParameter(0, 0);
            outPkt.setByteCount(0);

            outPkt.setErrorClass(SMBStatus.ErrDos);
            outPkt.setErrorCode(SMBStatus.DOSNoMoreFiles);

            m_sess.sendResponseSMB(outPkt);

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                logger.debug("End search [" + searchId + "]");

            // Release the search context

            m_sess.deallocateSearchSlot(searchId);
        }
        else
        {

            // Set the actual data length

            dataLen = bufPos - outPkt.getByteOffset();
            outPkt.setByteCount(dataLen);

            // Set the variable data block length and returned file count parameter

            bufPos = outPkt.getByteOffset() + 1;
            DataPacker.putIntelShort(dataLen - 3, buf, bufPos);
            outPkt.setParameter(0, fileCnt);

            // Send the search response packet

            m_sess.sendResponseSMB(outPkt);

            // Check if the search string contains wildcards and this is the start of a new search,
            // if not then
            // release the search context now as the client will not continue the search.

            if (fileCnt == 1 && resumeLen == 0 && WildCard.containsWildcards(srchPath) == false)
            {

                // Debug

                if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
                    logger.debug("End search [" + searchId + "] (Not wildcard)");

                // Release the search context

                m_sess.deallocateSearchSlot(searchId);
            }
        }
    }

    /**
     * Process a search request that is for the volume label.
     * 
     * @param outPkt SMBSrvPacket
     */
    protected final void procSearchVolumeLabel(SMBSrvPacket outPkt) throws IOException, SMBSrvException
    {

        // Get the tree connection details

        int treeId = m_smbPkt.getTreeId();
        TreeConnection conn = m_sess.findConnection(treeId);

        if (conn == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidTID, SMBStatus.ErrSrv);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
            logger.debug("Start Search - Volume Label");

        // Access the shared devices disk interface

        DiskInterface disk = null;
        DiskDeviceContext diskCtx = null;

        try
        {
            disk = (DiskInterface) conn.getSharedDevice().getInterface();
            diskCtx = (DiskDeviceContext) conn.getContext();
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Get the volume label

        VolumeInfo volInfo = diskCtx.getVolumeInformation();
        String volLabel = "";
        if (volInfo != null)
            volLabel = volInfo.getVolumeLabel();

        // Start building the search response packet

        outPkt.setParameterCount(1);
        int bufPos = outPkt.getByteOffset();
        byte[] buf = outPkt.getBuffer();
        buf[bufPos++] = (byte) DataType.VariableBlock;

        // Calculate the data length

        int dataLen = CoreResumeKey.LENGTH + 22;
        DataPacker.putIntelShort(dataLen, buf, bufPos);
        bufPos += 2;

        // Pack the resume key

        CoreResumeKey.putResumeKey(buf, bufPos, volLabel, -1);
        bufPos += CoreResumeKey.LENGTH;

        // Pack the file information

        buf[bufPos++] = (byte) (FileAttribute.Volume & 0x00FF);

        // Zero the date/time and file length fields

        for (int i = 0; i < 8; i++)
            buf[bufPos++] = (byte) 0;

        StringBuffer volBuf = new StringBuffer();
        volBuf.append(volLabel);

        while (volBuf.length() < 13)
            volBuf.append(" ");

        if (volBuf.length() > 12)
            volBuf.setLength(12);

        bufPos = DataPacker.putString(volBuf.toString().toUpperCase(), buf, bufPos, true);

        // Set the actual data length

        dataLen = bufPos - m_smbPkt.getByteOffset();
        outPkt.setByteCount(dataLen);

        // Send the search response packet

        m_sess.sendResponseSMB(outPkt);

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_SEARCH))
            logger.debug("Volume label for " + conn.toString() + " is " + volLabel);
        return;
    }

    /**
     * Seek to the specified file position within the open file.
     * 
     * @param pkt SMBSrvPacket
     */
    protected final void procSeekFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file seek request

        if (m_smbPkt.checkPacketIsValid(4, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        int seekMode = m_smbPkt.getParameter(1);
        long seekPos = (long) m_smbPkt.getParameterLong(2);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("File Seek [" + netFile.getFileId() + "] : Mode = " + seekMode + ", Pos = " + seekPos);

        // Seek to the specified position within the file

        byte[] buf = outPkt.getBuffer();
        long pos = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Seek to the file position

            pos = disk.seekFile(m_sess, conn, netFile, seekPos, seekMode);
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
                logger.debug("File Seek Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to seek the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDReadFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the new file position

        outPkt.setParameterCount(2);
        outPkt.setParameterLong(0, (int) (pos & 0x0FFFFFFFFL));
        outPkt.setByteCount(0);

        // Send the seek response

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process the SMB session setup request.
     * 
     * @param outPkt Response SMB packet.
     */

    protected void procSessionSetup(SMBSrvPacket outPkt) throws SMBSrvException, IOException,
            TooManyConnectionsException
    {

        // Build the session setup response SMB

        outPkt.setParameterCount(3);
        outPkt.setParameter(0, 0);
        outPkt.setParameter(1, 0);
        outPkt.setParameter(2, 8192);
        outPkt.setByteCount(0);

        outPkt.setTreeId(0);
        outPkt.setUserId(0);

        // Pack the OS, dialect and domain name strings.

        int pos = outPkt.getByteOffset();
        byte[] buf = outPkt.getBuffer();

        pos = DataPacker.putString("Java", buf, pos, true);
        pos = DataPacker.putString("JLAN Server " + m_sess.getServer().isVersion(), buf, pos, true);
        pos = DataPacker.putString(m_sess.getServer().getConfiguration().getDomainName(), buf, pos, true);

        outPkt.setByteCount(pos - outPkt.getByteOffset());

        // Send the negotiate response

        m_sess.sendResponseSMB(outPkt);

        // Update the session state

        m_sess.setState(SMBSrvSessionState.SMBSESSION);
    }

    /**
     * Set the file attributes for a file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procSetFileAttributes(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid set file attributes request

        if (m_smbPkt.checkPacketIsValid(8, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the file name

        String fileName = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, m_smbPkt.isUnicode());
        if (fileName == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Get the file attributes

        int fattr = m_smbPkt.getParameter(0);
        int setFlags = FileInfo.SetAttributes;

        FileInfo finfo = new FileInfo(fileName, 0, fattr);

        int fdate = m_smbPkt.getParameter(1);
        int ftime = m_smbPkt.getParameter(2);

        if (fdate != 0 && ftime != 0)
        {
            finfo.setModifyDateTime(new SMBDate(fdate, ftime).getTime());
            setFlags += FileInfo.SetModifyDate;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Set File Attributes [" + treeId + "] name=" + fileName + ", attr=0x"
                    + Integer.toHexString(fattr) + ", fdate=" + fdate + ", ftime=" + ftime);

        // Access the disk interface and set the file attributes

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Get the file information for the specified file/directory

            finfo.setFileInformationFlags(setFlags);
            disk.setFileInformation(m_sess, conn, fileName, finfo);
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

        // Return the set file attributes response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Set file information.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procSetFileInformation(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid set file information2 request

        if (m_smbPkt.checkPacketIsValid(7, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request, and get the network file details.

        int fid = m_smbPkt.getParameter(0);
        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Get the creation date/time from the request

        int setFlags = 0;
        FileInfo finfo = new FileInfo(netFile.getName(), 0, 0);

        int fdate = m_smbPkt.getParameter(1);
        int ftime = m_smbPkt.getParameter(2);

        if (fdate != 0 && ftime != 0)
        {
            finfo.setCreationDateTime(new SMBDate(fdate, ftime).getTime());
            setFlags += FileInfo.SetCreationDate;
        }

        // Get the last access date/time from the request

        fdate = m_smbPkt.getParameter(3);
        ftime = m_smbPkt.getParameter(4);

        if (fdate != 0 && ftime != 0)
        {
            finfo.setAccessDateTime(new SMBDate(fdate, ftime).getTime());
            setFlags += FileInfo.SetAccessDate;
        }

        // Get the last write date/time from the request

        fdate = m_smbPkt.getParameter(5);
        ftime = m_smbPkt.getParameter(6);

        if (fdate != 0 && ftime != 0)
        {
            finfo.setModifyDateTime(new SMBDate(fdate, ftime).getTime());
            setFlags += FileInfo.SetModifyDate;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILE))
            logger.debug("Set File Information 2 [" + netFile.getFileId() + "] " + finfo.toString());

        // Access the disk interface and set the file information

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Get the file information for the specified file/directory

            finfo.setFileInformationFlags(setFlags);
            disk.setFileInformation(m_sess, conn, netFile.getFullName(), finfo);
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

        // Return the set file information response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Process the SMB tree connect request.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     * @exception TooManyConnectionsException Too many concurrent connections on this session.
     */

    protected void procTreeConnect(SMBSrvPacket outPkt) throws SMBSrvException, TooManyConnectionsException,
            java.io.IOException
    {

        // Check that the received packet looks like a valid tree connect request

        if (m_smbPkt.checkPacketIsValid(0, 4) == false)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the data bytes position and length

        int dataPos = m_smbPkt.getByteOffset();
        int dataLen = m_smbPkt.getByteCount();
        byte[] buf = m_smbPkt.getBuffer();

        // Extract the requested share name, as a UNC path

        boolean isUni = m_smbPkt.isUnicode();
        String uncPath = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, isUni);
        if (uncPath == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Extract the password string

        if (isUni)
        {
            dataPos = DataPacker.wordAlign(dataPos + 1) + (uncPath.length() * 2) + 2;
            dataLen -= (uncPath.length() * 2) + 2;
        }
        else
        {
            dataPos += uncPath.length() + 2;
            dataLen -= uncPath.length() + 2;
        }

        String pwd = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, isUni);
        if (pwd == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Extract the service type string

        if (isUni)
        {
            dataPos = DataPacker.wordAlign(dataPos + 1) + (pwd.length() * 2) + 2;
            dataLen -= (pwd.length() * 2) + 2;
        }
        else
        {
            dataPos += pwd.length() + 2;
            dataLen -= pwd.length() + 2;
        }

        String service = DataPacker.getDataString(DataType.ASCII, buf, dataPos, dataLen, isUni);
        if (service == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Convert the service type to a shared device type

        int servType = ShareType.ServiceAsType(service);
        if (servType == ShareType.UNKNOWN)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
            logger.debug("Tree connect - " + uncPath + ", " + service);

        // Parse the requested share name

        PCShare share = null;

        try
        {
            share = new PCShare(uncPath);
        }
        catch (InvalidUNCPathException ex)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        // Map the IPC$ share to the admin pipe type

        if (servType == ShareType.NAMEDPIPE && share.getShareName().compareTo("IPC$") == 0)
            servType = ShareType.ADMINPIPE;

        // Find the requested shared device

        SharedDevice shareDev = null;

        try
        {

            // Get/create the shared device

            shareDev = m_sess.getSMBServer().findShare(share.getNodeName(), share.getShareName(), servType,
                    getSession(), true);
        }
        catch (InvalidUserException ex)
        {

            // Return a logon failure status

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }
        catch (Exception ex)
        {

            // Return a general status, bad network name

            m_sess.sendErrorResponseSMB(SMBStatus.SRVInvalidNetworkName, SMBStatus.ErrSrv);
            return;
        }

        // Check if the share is valid

        if (shareDev == null || shareDev.getType() != servType)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Allocate a tree id for the new connection

        int treeId = m_sess.addConnection(shareDev);

        // Authenticate the share connection depending upon the security mode the server is running
        // under

        CifsAuthenticator auth = getSession().getSMBServer().getAuthenticator();
        int filePerm = FileAccess.Writeable;

        if (auth != null)
        {

            // Validate the share connection

            filePerm = auth.authenticateShareConnect(m_sess.getClientInformation(), shareDev, pwd, m_sess);
            if (filePerm < 0)
            {

                // Invalid share connection request

                m_sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
                return;
            }
        }

        // Set the file permission that this user has been granted for this share

        TreeConnection tree = m_sess.findConnection(treeId);
        tree.setPermission(filePerm);

        // Build the tree connect response

        outPkt.setParameterCount(2);

        outPkt.setParameter(0, buf.length - RFCNetBIOSProtocol.HEADER_LEN);
        outPkt.setParameter(1, treeId);
        outPkt.setByteCount(0);

        // Clear any chained request

        outPkt.setAndXCommand(0xFF);
        m_sess.sendResponseSMB(outPkt);

        // Inform the driver that a connection has been opened

        if (tree.getInterface() != null)
            tree.getInterface().treeOpened(m_sess, tree);
    }

    /**
     * Process the SMB tree disconnect request.
     * 
     * @param outPkt Response SMB packet.
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procTreeDisconnect(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid tree disconnect request

        if (m_smbPkt.checkPacketIsValid(0, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_TREE))
            logger.debug("Tree disconnect - " + treeId + ", " + conn.toString());

        // Remove the specified connection from the session

        m_sess.removeConnection(treeId);

        // Build the tree disconnect response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        m_sess.sendResponseSMB(outPkt);

        // Inform the driver that a connection has been closed

        if (conn.getInterface() != null)
            conn.getInterface().treeClosed(m_sess, conn);
    }

    /**
     * Unlock a byte range in the specified file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procUnLockFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid unlock file request

        if (m_smbPkt.checkPacketIsValid(5, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasReadAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        long lockcnt = m_smbPkt.getParameterLong(1);
        long lockoff = m_smbPkt.getParameterLong(3);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File UnLock [" + netFile.getFileId() + "] : Offset=" + lockoff + " ,Count=" + lockcnt);

        // ***** Always return a success status, simulated locking ****
        //
        // Build the unlock file response

        outPkt.setParameterCount(0);
        outPkt.setByteCount(0);

        // Send the response packet

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Unsupported SMB procesing.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected final void procUnsupported(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Send an unsupported error response

        m_sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
    }

    /**
     * Write to a file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procWriteFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file write request

        if (m_smbPkt.checkPacketIsValid(5, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid     = m_smbPkt.getParameter(0);
        int wrtcnt  = m_smbPkt.getParameter(1);
        long wrtoff = (m_smbPkt.getParameter(2) + (m_smbPkt.getParameter(3) << 16)) & 0xFFFFFFFFL;

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Write [" + netFile.getFileId() + "] : Size=" + wrtcnt + " ,Pos=" + wrtoff);

        // Write data to the file

        byte[] buf = m_smbPkt.getBuffer();
        int pos = m_smbPkt.getByteOffset();
        int wrtlen = 0;

        // Check that the data block is valid

        if (buf[pos] != DataType.DataBlock)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Update the buffer position to the start of the data to be written

            pos += 3;

            // Check for a zero length write, this should truncate/extend the file to the write
            // offset position

            if (wrtcnt == 0)
            {

                // Truncate/extend the file to the write offset

                disk.truncateFile(m_sess, conn, netFile, wrtoff);
            }
            else
            {

                // Write to the file

                wrtlen = disk.writeFile(m_sess, conn, netFile, buf, pos, wrtcnt, wrtoff);
            }
        }
        catch (InvalidDeviceInterfaceException ex)
        {

            // Failed to get/initialize the disk interface

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("File Write Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to read the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the count of bytes actually written

        outPkt.setParameterCount(1);
        outPkt.setParameter(0, wrtlen);
        outPkt.setByteCount(0);

        // Send the write response

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Write to a file then close the file.
     * 
     * @param outPkt SMBSrvPacket
     * @exception java.io.IOException If an I/O error occurs
     * @exception SMBSrvException If an SMB protocol error occurs
     */
    protected void procWriteAndCloseFile(SMBSrvPacket outPkt) throws java.io.IOException, SMBSrvException
    {

        // Check that the received packet looks like a valid file write and close request

        if (m_smbPkt.checkPacketIsValid(6, 0) == false)
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
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Check if the user has the required access permission

        if (conn.hasWriteAccess() == false)
        {

            // User does not have the required access rights

            m_sess.sendErrorResponseSMB(SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            return;
        }

        // Get the file id from the request

        int fid = m_smbPkt.getParameter(0);
        int wrtcnt = m_smbPkt.getParameter(1);
        int wrtoff = m_smbPkt.getParameterLong(2);

        NetworkFile netFile = conn.findFile(fid);

        if (netFile == null)
        {
            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
            logger.debug("File Write And Close [" + netFile.getFileId() + "] : Size=" + wrtcnt + " ,Pos=" + wrtoff);

        // Write data to the file

        byte[] buf = m_smbPkt.getBuffer();
        int pos = m_smbPkt.getByteOffset() + 1; // word align
        int wrtlen = 0;

        try
        {

            // Access the disk interface that is associated with the shared device

            DiskInterface disk = (DiskInterface) conn.getSharedDevice().getInterface();

            // Write to the file

            wrtlen = disk.writeFile(m_sess, conn, netFile, buf, pos, wrtcnt, wrtoff);

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

            m_sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
            return;
        }
        catch (java.io.IOException ex)
        {

            // Debug

            if (logger.isDebugEnabled() && m_sess.hasDebug(SMBSrvSession.DBG_FILEIO))
                logger.debug("File Write Error [" + netFile.getFileId() + "] : " + ex.toString());

            // Failed to read the file

            m_sess.sendErrorResponseSMB(SMBStatus.HRDWriteFault, SMBStatus.ErrHrd);
            return;
        }

        // Return the count of bytes actually written

        outPkt.setParameterCount(1);
        outPkt.setParameter(0, wrtlen);
        outPkt.setByteCount(0);

        outPkt.setError(0, 0);

        // Send the write response

        m_sess.sendResponseSMB(outPkt);
    }

    /**
     * Run the core SMB protocol handler.
     * 
     * @return boolean true if the packet was processed, else false
     */
    public boolean runProtocol() throws java.io.IOException, SMBSrvException, TooManyConnectionsException
    {

        // Check if the SMB packet is initialized

        if (m_smbPkt == null)
            m_smbPkt = new SMBSrvPacket(m_sess.getBuffer());

        // Determine the SMB command type

        boolean handledOK = true;
        SMBSrvPacket outPkt = m_smbPkt;

        switch (m_smbPkt.getCommand())
        {

        // Session setup

        case PacketType.SessionSetupAndX:
            procSessionSetup(outPkt);
            break;

        // Tree connect

        case PacketType.TreeConnect:
            procTreeConnect(outPkt);
            break;

        // Tree disconnect

        case PacketType.TreeDisconnect:
            procTreeDisconnect(outPkt);
            break;

        // Search

        case PacketType.Search:
            procSearch(outPkt);
            break;

        // Get disk attributes

        case PacketType.DiskInformation:
            procDiskAttributes(outPkt);
            break;

        // Get file attributes

        case PacketType.GetFileAttributes:
            procGetFileAttributes(outPkt);
            break;

        // Set file attributes

        case PacketType.SetFileAttributes:
            procSetFileAttributes(outPkt);
            break;

        // Get file information

        case PacketType.QueryInformation2:
            procGetFileInformation(outPkt);
            break;

        // Set file information

        case PacketType.SetInformation2:
            procSetFileInformation(outPkt);
            break;

        // Open a file

        case PacketType.OpenFile:
            procOpenFile(outPkt);
            break;

        // Read from a file

        case PacketType.ReadFile:
            procReadFile(outPkt);
            break;

        // Seek file

        case PacketType.SeekFile:
            procSeekFile(outPkt);
            break;

        // Close a file

        case PacketType.CloseFile:
            procCloseFile(outPkt);
            break;

        // Create a new file

        case PacketType.CreateFile:
        case PacketType.CreateNew:
            procCreateFile(outPkt);
            break;

        // Write to a file

        case PacketType.WriteFile:
            procWriteFile(outPkt);
            break;

        // Write to a file, then close the file

        case PacketType.WriteAndClose:
            procWriteAndCloseFile(outPkt);
            break;

        // Flush file

        case PacketType.FlushFile:
            procFlushFile(outPkt);
            break;

        // Rename a file

        case PacketType.RenameFile:
            procRenameFile(outPkt);
            break;

        // Delete a file

        case PacketType.DeleteFile:
            procDeleteFile(outPkt);
            break;

        // Create a new directory

        case PacketType.CreateDirectory:
            procCreateDirectory(outPkt);
            break;

        // Delete a directory

        case PacketType.DeleteDirectory:
            procDeleteDirectory(outPkt);
            break;

        // Check if a directory exists

        case PacketType.CheckDirectory:
            procCheckDirectory(outPkt);
            break;

        // Unsupported requests

        case PacketType.IOCtl:
            procUnsupported(outPkt);
            break;

        // Echo request

        case PacketType.Echo:
            procEcho(outPkt);
            break;

        // Process exit request

        case PacketType.ProcessExit:
            procProcessExit(outPkt);
            break;

        // Create temoporary file request

        case PacketType.CreateTemporary:
            procCreateTemporaryFile(outPkt);
            break;

        // Lock file request

        case PacketType.LockFile:
            procLockFile(outPkt);
            break;

        // Unlock file request

        case PacketType.UnLockFile:
            procUnLockFile(outPkt);
            break;

        // Default

        default:

            // Indicate that the protocol handler did not process the SMB request

            handledOK = false;
            break;
        }

        // Return the handled status

        return handledOK;
    }
}