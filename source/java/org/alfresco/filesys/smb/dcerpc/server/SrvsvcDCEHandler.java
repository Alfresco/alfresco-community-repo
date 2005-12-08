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
package org.alfresco.filesys.smb.dcerpc.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.alfresco.filesys.server.auth.acl.AccessControlManager;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.smb.Dialect;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.Srvsvc;
import org.alfresco.filesys.smb.dcerpc.info.ServerInfo;
import org.alfresco.filesys.smb.dcerpc.info.ShareInfo;
import org.alfresco.filesys.smb.dcerpc.info.ShareInfoList;
import org.alfresco.filesys.smb.server.SMBServer;
import org.alfresco.filesys.smb.server.SMBSrvException;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Srvsvc DCE/RPC Handler Class
 */
public class SrvsvcDCEHandler implements DCEHandler
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    /**
     * Process a SrvSvc DCE/RPC request
     * 
     * @param sess SMBSrvSession
     * @param inBuf DCEBuffer
     * @param pipeFile DCEPipeFile
     * @exception IOException
     * @exception SMBSrvException
     */
    public void processRequest(SMBSrvSession sess, DCEBuffer inBuf, DCEPipeFile pipeFile) throws IOException,
            SMBSrvException
    {

        // Get the operation code and move the buffer pointer to the start of the request data

        int opNum = inBuf.getHeaderValue(DCEBuffer.HDR_OPCODE);
        try
        {
            inBuf.skipBytes(DCEBuffer.OPERATIONDATA);
        }
        catch (DCEBufferException ex)
        {
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("DCE/RPC SrvSvc request=" + Srvsvc.getOpcodeName(opNum));

        // Create the output DCE buffer and add the response header

        DCEBuffer outBuf = new DCEBuffer();
        outBuf.putResponseHeader(inBuf.getHeaderValue(DCEBuffer.HDR_CALLID), 0);

        // Process the request

        boolean processed = false;

        switch (opNum)
        {

        // Enumerate shares

        case Srvsvc.NetrShareEnum:
            processed = netShareEnum(sess, inBuf, outBuf);
            break;

        // Enumerate all shares

        case Srvsvc.NetrShareEnumSticky:
            processed = netShareEnum(sess, inBuf, outBuf);
            break;

        // Get share information

        case Srvsvc.NetrShareGetInfo:
            processed = netShareGetInfo(sess, inBuf, outBuf);
            break;

        // Get server information

        case Srvsvc.NetrServerGetInfo:
            processed = netServerGetInfo(sess, inBuf, outBuf);
            break;

        // Unsupported function

        default:
            break;
        }

        // Return an error status if the request was not processed

        if (processed == false)
        {
            sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            return;
        }

        // Set the allocation hint for the response

        outBuf.setHeaderValue(DCEBuffer.HDR_ALLOCHINT, outBuf.getLength());

        // Attach the output buffer to the pipe file

        pipeFile.setBufferedData(outBuf);
    }

    /**
     * Handle a share enumeration request
     * 
     * @param sess SMBSrvSession
     * @param inBuf DCEPacket
     * @param outBuf DCEPacket
     * @return boolean
     */
    protected final boolean netShareEnum(SMBSrvSession sess, DCEBuffer inBuf, DCEBuffer outBuf)
    {

        // Decode the request

        String srvName = null;
        ShareInfoList shrInfo = null;

        try
        {
            inBuf.skipPointer();
            srvName = inBuf.getString(DCEBuffer.ALIGN_INT);
            shrInfo = new ShareInfoList(inBuf);
        }
        catch (DCEBufferException ex)
        {
            return false;
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("NetShareEnum srvName=" + srvName + ", shrInfo=" + shrInfo.toString());

        // Get the share list from the server

        SharedDeviceList shareList = sess.getServer().getShareMapper().getShareList(srvName, sess, false);

        // Check if there is an access control manager configured

        if (sess.getServer().hasAccessControlManager())
        {

            // Filter the list of available shares by applying any access control rules

            AccessControlManager aclMgr = sess.getServer().getAccessControlManager();

            shareList = aclMgr.filterShareList(sess, shareList);
        }

        // Create a list of share information objects of the required information level

        Vector infoList = new Vector();
        Enumeration<SharedDevice> enm = shareList.enumerateShares();

        while (enm.hasMoreElements())
        {

            // Get the current shared device details

            SharedDevice share = enm.nextElement();

            // Determine the share type

            int shrTyp = ShareInfo.Disk;

            if (share.getType() == ShareType.PRINTER)
                shrTyp = ShareInfo.PrintQueue;
            else if (share.getType() == ShareType.NAMEDPIPE)
                shrTyp = ShareInfo.IPC;
            else if (share.getType() == ShareType.ADMINPIPE)
                shrTyp = ShareInfo.IPC + ShareInfo.Hidden;

            // Create a share information object with the basic information

            ShareInfo info = new ShareInfo(shrInfo.getInformationLevel(), share.getName(), shrTyp, share.getComment());
            infoList.add(info);

            // Add additional information

            switch (shrInfo.getInformationLevel())
            {

            // Level 2

            case 2:
                if (share.getContext() != null)
                    info.setPath(share.getContext().getDeviceName());
                break;

            // Level 502

            case 502:
                if (share.getContext() != null)
                    info.setPath(share.getContext().getDeviceName());
                break;
            }
        }

        // Set the share information list in the server share information and write the
        // share information to the output DCE buffer.

        shrInfo.setShareList(infoList);
        try
        {
            shrInfo.writeList(outBuf);
            outBuf.putInt(0); // status code
        }
        catch (DCEBufferException ex)
        {
        }

        // Indicate that the request was processed successfully

        return true;
    }

    /**
     * Handle a get share information request
     * 
     * @param sess SMBSrvSession
     * @param inBuf DCEPacket
     * @param outBuf DCEPacket
     * @return boolean
     */
    protected final boolean netShareGetInfo(SMBSrvSession sess, DCEBuffer inBuf, DCEBuffer outBuf)
    {

        // Decode the request

        String srvName = null;
        String shrName = null;
        int infoLevel = 0;

        try
        {
            inBuf.skipPointer();
            srvName = inBuf.getString(DCEBuffer.ALIGN_INT);
            shrName = inBuf.getString(DCEBuffer.ALIGN_INT);
            infoLevel = inBuf.getInt();
        }
        catch (DCEBufferException ex)
        {
            return false;
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("netShareGetInfo srvname=" + srvName + ", share=" + shrName + ", infoLevel=" + infoLevel);

        // Find the required shared device

        SharedDevice share = null;

        try
        {

            // Get the shared device details

            share = sess.getServer().findShare(srvName, shrName, ShareType.UNKNOWN, sess, false);
        }
        catch (Exception ex)
        {
        }

        // Check if the share details are valid

        if (share == null)
            return false;

        // Determine the share type

        int shrTyp = ShareInfo.Disk;

        if (share.getType() == ShareType.PRINTER)
            shrTyp = ShareInfo.PrintQueue;
        else if (share.getType() == ShareType.NAMEDPIPE)
            shrTyp = ShareInfo.IPC;
        else if (share.getType() == ShareType.ADMINPIPE)
            shrTyp = ShareInfo.IPC + ShareInfo.Hidden;

        // Create the share information

        ShareInfo shrInfo = new ShareInfo(infoLevel, share.getName(), shrTyp, share.getComment());

        // Pack the information level, structure pointer and share information

        outBuf.putInt(infoLevel);
        outBuf.putPointer(true);

        shrInfo.writeObject(outBuf, outBuf);

        // Add the status and return a success status

        outBuf.putInt(0);
        return true;
    }

    /**
     * Handle a get server information request
     * 
     * @param sess SMBSrvSession
     * @param inBuf DCEPacket
     * @param outBuf DCEPacket
     * @return boolean
     */
    protected final boolean netServerGetInfo(SMBSrvSession sess, DCEBuffer inBuf, DCEBuffer outBuf)
    {

        // Decode the request

        String srvName = null;
        int infoLevel = 0;

        try
        {
            inBuf.skipPointer();
            srvName = inBuf.getString(DCEBuffer.ALIGN_INT);
            infoLevel = inBuf.getInt();
        }
        catch (DCEBufferException ex)
        {
            return false;
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("netServerGetInfo srvname=" + srvName + ", infoLevel=" + infoLevel);

        // Create the server information and set the common values

        ServerInfo srvInfo = new ServerInfo(infoLevel);

        SMBServer srv = sess.getSMBServer();
        srvInfo.setServerName(srv.getServerName());
        srvInfo.setComment(srv.getComment());
        srvInfo.setServerType(srv.getServerType());

        // Determine if the server is using the NT SMB dialect and set the platofmr id accordingly

        ServerConfiguration srvConfig = srv.getConfiguration();
        if (srvConfig != null && srvConfig.getEnabledDialects().hasDialect(Dialect.NT) == true)
        {
            srvInfo.setPlatformId(ServerInfo.PLATFORM_NT);
            srvInfo.setVersion(5, 1);
        }
        else
        {
            srvInfo.setPlatformId(ServerInfo.PLATFORM_OS2);
            srvInfo.setVersion(4, 0);
        }

        // Write the server information to the DCE response

        srvInfo.writeObject(outBuf, outBuf);
        outBuf.putInt(0);

        // Indicate that the request was processed successfully

        return true;
    }
}
