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
package org.alfresco.filesys.smb.server;

import java.io.IOException;

import org.alfresco.filesys.netbios.RFCNetBIOSProtocol;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.DataType;
import org.alfresco.filesys.smb.PacketType;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.TransactBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.DCECommand;
import org.alfresco.filesys.smb.dcerpc.DCEDataPacker;
import org.alfresco.filesys.smb.dcerpc.DCEPipeType;
import org.alfresco.filesys.smb.dcerpc.UUID;
import org.alfresco.filesys.smb.dcerpc.server.DCEPipeFile;
import org.alfresco.filesys.smb.dcerpc.server.DCESrvPacket;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DCE/RPC Protocol Handler Class
 */
public class DCERPCHandler
{
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    /**
     * Process a DCE/RPC request
     * 
     * @param sess SMBSrvSession
     * @param srvTrans SMBSrvTransPacket
     * @param outPkt SMBSrvPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void processDCERPCRequest(SMBSrvSession sess, SMBSrvTransPacket srvTrans, SMBSrvPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = srvTrans.getTreeId();
        TreeConnection conn = sess.findConnection(treeId);

        if (conn == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Get the file id and validate

        int fid = srvTrans.getSetupParameter(1);
        int maxData = srvTrans.getParameter(3) - DCEBuffer.OPERATIONDATA;

        // Get the IPC pipe file for the specified file id

        DCEPipeFile pipeFile = (DCEPipeFile) conn.findFile(fid);
        if (pipeFile == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Create a DCE/RPC buffer from the received data

        DCEBuffer dceBuf = new DCEBuffer(srvTrans.getBuffer(), srvTrans.getParameter(10)
                + RFCNetBIOSProtocol.HEADER_LEN);

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("TransactNmPipe pipeFile=" + pipeFile.getName() + ", fid=" + fid + ", dceCmd=0x"
                    + Integer.toHexString(dceBuf.getHeaderValue(DCEBuffer.HDR_PDUTYPE)));

        // Process the received DCE buffer

        processDCEBuffer(sess, dceBuf, pipeFile);

        // Check if there is a reply buffer to return to the caller

        if (pipeFile.hasBufferedData() == false)
            return;

        DCEBuffer txBuf = pipeFile.getBufferedData();

        // Initialize the reply

        DCESrvPacket dcePkt = new DCESrvPacket(outPkt.getBuffer());

        // Always only one fragment as the data either fits into the first reply fragment or the
        // client will read the remaining data by issuing read requests on the pipe

        int flags = DCESrvPacket.FLG_ONLYFRAG;

        dcePkt.initializeDCEReply();
        txBuf.setHeaderValue(DCEBuffer.HDR_FLAGS, flags);

        // Build the reply data

        byte[] buf = dcePkt.getBuffer();
        int pos = DCEDataPacker.longwordAlign(dcePkt.getByteOffset());

        // Set the DCE fragment size and send the reply DCE/RPC SMB

        int dataLen = txBuf.getLength();
        txBuf.setHeaderValue(DCEBuffer.HDR_FRAGLEN, dataLen);

        // Copy the data from the DCE output buffer to the reply SMB packet

        int len = txBuf.getLength();
        int sts = SMBStatus.NTSuccess;

        if (len > maxData)
        {

            // Write the maximum transmit fragment to the reply

            len = maxData + DCEBuffer.OPERATIONDATA;
            dataLen = maxData + DCEBuffer.OPERATIONDATA;

            // Indicate a buffer overflow status

            sts = SMBStatus.NTBufferOverflow;
        }
        else
        {

            // Clear the DCE/RPC pipe buffered data, the reply will fit into a single response
            // packet

            pipeFile.setBufferedData(null);
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("Reply DCEbuf flags=0x" + Integer.toHexString(flags) + ", len=" + len + ", status=0x"
                    + Integer.toHexString(sts));

        // Copy the reply data to the reply packet

        try
        {
            pos += txBuf.copyData(buf, pos, len);
        }
        catch (DCEBufferException ex)
        {
            sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            return;
        }

        // Set the SMB transaction data length

        int byteLen = pos - dcePkt.getByteOffset();
        dcePkt.setParameter(1, dataLen);
        dcePkt.setParameter(6, dataLen);
        dcePkt.setByteCount(byteLen);
        dcePkt.setFlags2(SMBPacket.FLG2_LONGERRORCODE);
        dcePkt.setLongErrorCode(sts);

        sess.sendResponseSMB(dcePkt);
    }

    /**
     * Process a DCE/RPC request
     * 
     * @param sess SMBSrvSession
     * @param tbuf TransactBuffer
     * @param outPkt SMBSrvPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void processDCERPCRequest(SMBSrvSession sess, TransactBuffer tbuf, SMBSrvPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Check if the transaction buffer has setup and data buffers

        if (tbuf.hasSetupBuffer() == false || tbuf.hasDataBuffer() == false)
        {
            sess.sendErrorResponseSMB(SMBStatus.SRVUnrecognizedCommand, SMBStatus.ErrSrv);
            return;
        }

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = tbuf.getTreeId();
        TreeConnection conn = sess.findConnection(treeId);

        if (conn == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Get the file id and validate

        DataBuffer setupBuf = tbuf.getSetupBuffer();

        setupBuf.skipBytes(2);
        int fid = setupBuf.getShort();
        int maxData = tbuf.getReturnDataLimit() - DCEBuffer.OPERATIONDATA;

        // Get the IPC pipe file for the specified file id

        DCEPipeFile pipeFile = (DCEPipeFile) conn.findFile(fid);
        if (pipeFile == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Create a DCE/RPC buffer from the received transaction data

        DCEBuffer dceBuf = new DCEBuffer(tbuf);

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("TransactNmPipe pipeFile=" + pipeFile.getName() + ", fid=" + fid + ", dceCmd=0x"
                    + Integer.toHexString(dceBuf.getHeaderValue(DCEBuffer.HDR_PDUTYPE)));

        // Process the received DCE buffer

        processDCEBuffer(sess, dceBuf, pipeFile);

        // Check if there is a reply buffer to return to the caller

        if (pipeFile.hasBufferedData() == false)
            return;

        DCEBuffer txBuf = pipeFile.getBufferedData();

        // Initialize the reply

        DCESrvPacket dcePkt = new DCESrvPacket(outPkt.getBuffer());

        // Always only one fragment as the data either fits into the first reply fragment or the
        // client will read the remaining data by issuing read requests on the pipe

        int flags = DCESrvPacket.FLG_ONLYFRAG;

        dcePkt.initializeDCEReply();
        txBuf.setHeaderValue(DCEBuffer.HDR_FLAGS, flags);

        // Build the reply data

        byte[] buf = dcePkt.getBuffer();
        int pos = DCEDataPacker.longwordAlign(dcePkt.getByteOffset());

        // Set the DCE fragment size and send the reply DCE/RPC SMB

        int dataLen = txBuf.getLength();
        txBuf.setHeaderValue(DCEBuffer.HDR_FRAGLEN, dataLen);

        // Copy the data from the DCE output buffer to the reply SMB packet

        int len = txBuf.getLength();
        int sts = SMBStatus.NTSuccess;

        if (len > maxData)
        {

            // Write the maximum transmit fragment to the reply

            len = maxData + DCEBuffer.OPERATIONDATA;
            dataLen = maxData + DCEBuffer.OPERATIONDATA;

            // Indicate a buffer overflow status

            sts = SMBStatus.NTBufferOverflow;
        }
        else
        {

            // Clear the DCE/RPC pipe buffered data, the reply will fit into a single response
            // packet

            pipeFile.setBufferedData(null);
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("Reply DCEbuf flags=0x" + Integer.toHexString(flags) + ", len=" + len + ", status=0x"
                    + Integer.toHexString(sts));

        // Copy the reply data to the reply packet

        try
        {
            pos += txBuf.copyData(buf, pos, len);
        }
        catch (DCEBufferException ex)
        {
            sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            return;
        }

        // Set the SMB transaction data length

        int byteLen = pos - dcePkt.getByteOffset();
        dcePkt.setParameter(1, dataLen);
        dcePkt.setParameter(6, dataLen);
        dcePkt.setByteCount(byteLen);
        dcePkt.setFlags2(SMBPacket.FLG2_LONGERRORCODE);
        dcePkt.setLongErrorCode(sts);

        sess.sendResponseSMB(dcePkt);
    }

    /**
     * Process a DCE/RPC write request to the named pipe file
     * 
     * @param sess SMBSrvSession
     * @param inPkt SMBSrvPacket
     * @param outPkt SMBSrvPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void processDCERPCRequest(SMBSrvSession sess, SMBSrvPacket inPkt, SMBSrvPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = inPkt.getTreeId();
        TreeConnection conn = sess.findConnection(treeId);

        if (conn == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Determine if this is a write or write andX request

        int cmd = inPkt.getCommand();

        // Get the file id and validate

        int fid = -1;
        if (cmd == PacketType.WriteFile)
            fid = inPkt.getParameter(0);
        else
            fid = inPkt.getParameter(2);

        // Get the IPC pipe file for the specified file id

        DCEPipeFile pipeFile = (DCEPipeFile) conn.findFile(fid);
        if (pipeFile == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Create a DCE buffer for the received data

        DCEBuffer dceBuf = null;
        byte[] buf = inPkt.getBuffer();
        int pos = 0;
        int len = 0;

        if (cmd == PacketType.WriteFile)
        {

            // Get the data offset

            pos = inPkt.getByteOffset();

            // Check that the received data is valid

            if (buf[pos++] != DataType.DataBlock)
            {
                sess.sendErrorResponseSMB(SMBStatus.DOSInvalidData, SMBStatus.ErrDos);
                return;
            }

            len = DataPacker.getIntelShort(buf, pos);
            pos += 2;

        }
        else
        {

            // Get the data offset and length

            len = inPkt.getParameter(10);
            pos = inPkt.getParameter(11) + RFCNetBIOSProtocol.HEADER_LEN;
        }

        // Create a DCE buffer mapped to the received packet

        dceBuf = new DCEBuffer(buf, pos);

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_IPC))
            logger.debug("Write pipeFile=" + pipeFile.getName() + ", fid=" + fid + ", dceCmd=0x"
                    + Integer.toHexString(dceBuf.getHeaderValue(DCEBuffer.HDR_PDUTYPE)));

        // Process the DCE buffer

        processDCEBuffer(sess, dceBuf, pipeFile);

        // Check if there is a valid reply buffered

        int bufLen = 0;
        if (pipeFile.hasBufferedData())
            bufLen = pipeFile.getBufferedData().getLength();

        // Send the write/write andX reply

        if (cmd == PacketType.WriteFile)
        {

            // Build the write file reply

            outPkt.setParameterCount(1);
            outPkt.setParameter(0, len);
            outPkt.setByteCount(0);
        }
        else
        {

            // Build the write andX reply

            outPkt.setParameterCount(6);

            outPkt.setAndXCommand(0xFF);
            outPkt.setParameter(1, 0);
            outPkt.setParameter(2, len);
            outPkt.setParameter(3, bufLen);
            outPkt.setParameter(4, 0);
            outPkt.setParameter(5, 0);
            outPkt.setByteCount(0);
        }

        // Send the write reply

        outPkt.setFlags2(SMBPacket.FLG2_LONGERRORCODE);
        sess.sendResponseSMB(outPkt);
    }

    /**
     * Process a DCE/RPC pipe read request
     * 
     * @param sess SMBSrvSession
     * @param inPkt SMBSrvPacket
     * @param outPkt SMBSrvPacket
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void processDCERPCRead(SMBSrvSession sess, SMBSrvPacket inPkt, SMBSrvPacket outPkt)
            throws IOException, SMBSrvException
    {

        // Get the tree id from the received packet and validate that it is a valid
        // connection id.

        int treeId = inPkt.getTreeId();
        TreeConnection conn = sess.findConnection(treeId);

        if (conn == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidDrive, SMBStatus.ErrDos);
            return;
        }

        // Determine if this is a read or read andX request

        int cmd = inPkt.getCommand();

        // Get the file id and read length, and validate

        int fid = -1;
        int rdLen = -1;

        if (cmd == PacketType.ReadFile)
        {
            fid = inPkt.getParameter(0);
            rdLen = inPkt.getParameter(1);
        }
        else
        {
            fid = inPkt.getParameter(2);
            rdLen = inPkt.getParameter(5);
        }

        // Get the IPC pipe file for the specified file id

        DCEPipeFile pipeFile = (DCEPipeFile) conn.findFile(fid);
        if (pipeFile == null)
        {
            sess.sendErrorResponseSMB(SMBStatus.DOSInvalidHandle, SMBStatus.ErrDos);
            return;
        }

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_IPC))
            logger.debug("Read pipeFile=" + pipeFile.getName() + ", fid=" + fid + ", rdLen=" + rdLen);

        // Check if there is a valid reply buffered

        if (pipeFile.hasBufferedData())
        {

            // Get the buffered data

            DCEBuffer bufData = pipeFile.getBufferedData();
            int bufLen = bufData.getAvailableLength();

            // Debug

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_IPC))
                logger.debug("  Buffered data available=" + bufLen);

            // Check if there is less data than the read size

            if (rdLen > bufLen)
                rdLen = bufLen;

            // Build the read response

            if (cmd == PacketType.ReadFile)
            {

                // Build the read response

                outPkt.setParameterCount(5);
                outPkt.setParameter(0, rdLen);
                for (int i = 1; i < 5; i++)
                    outPkt.setParameter(i, 0);
                outPkt.setByteCount(rdLen + 3);

                // Copy the data to the response

                byte[] buf = outPkt.getBuffer();
                int pos = outPkt.getByteOffset();

                buf[pos++] = (byte) DataType.DataBlock;
                DataPacker.putIntelShort(rdLen, buf, pos);
                pos += 2;

                try
                {
                    bufData.copyData(buf, pos, rdLen);
                }
                catch (DCEBufferException ex)
                {
                    logger.error("DCR/RPC read", ex);
                }
            }
            else
            {

                // Build the read andX response

                outPkt.setParameterCount(12);
                outPkt.setAndXCommand(0xFF);
                for (int i = 1; i < 12; i++)
                    outPkt.setParameter(i, 0);

                // Copy the data to the response

                byte[] buf = outPkt.getBuffer();
                int pos = DCEDataPacker.longwordAlign(outPkt.getByteOffset());

                outPkt.setParameter(5, rdLen);
                outPkt.setParameter(6, pos - RFCNetBIOSProtocol.HEADER_LEN);
                outPkt.setByteCount((pos + rdLen) - outPkt.getByteOffset());

                try
                {
                    bufData.copyData(buf, pos, rdLen);
                }
                catch (DCEBufferException ex)
                {
                    logger.error("DCE/RPC error", ex);
                }
            }
        }
        else
        {

            // Debug

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_IPC))
                logger.debug("  No buffered data available");

            // Return a zero length read response

            if (cmd == PacketType.ReadFile)
            {

                // Initialize the read response

                outPkt.setParameterCount(5);
                for (int i = 0; i < 5; i++)
                    outPkt.setParameter(i, 0);
                outPkt.setByteCount(0);
            }
            else
            {

                // Return a zero length read andX response

                outPkt.setParameterCount(12);

                outPkt.setAndXCommand(0xFF);
                for (int i = 1; i < 12; i++)
                    outPkt.setParameter(i, 0);
                outPkt.setByteCount(0);
            }
        }

        // Clear the status code

        outPkt.setLongErrorCode(SMBStatus.NTSuccess);

        // Send the read reply

        outPkt.setFlags2(SMBPacket.FLG2_LONGERRORCODE);
        sess.sendResponseSMB(outPkt);
    }

    /**
     * Process the DCE/RPC request buffer
     * 
     * @param sess SMBSrvSession
     * @param buf DCEBuffer
     * @param pipeFile DCEPipeFile
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void processDCEBuffer(SMBSrvSession sess, DCEBuffer dceBuf, DCEPipeFile pipeFile)
            throws IOException, SMBSrvException
    {

        // Process the DCE/RPC request

        switch (dceBuf.getHeaderValue(DCEBuffer.HDR_PDUTYPE))
        {

        // DCE Bind

        case DCECommand.BIND:
            procDCEBind(sess, dceBuf, pipeFile);
            break;

        // DCE Request

        case DCECommand.REQUEST:
            procDCERequest(sess, dceBuf, pipeFile);
            break;

        default:
            sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
            break;
        }
    }

    /**
     * Process a DCE bind request
     * 
     * @param sess SMBSrvSession
     * @param dceBuf DCEBuffer
     * @param pipeFile DCEPipeFile
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void procDCEBind(SMBSrvSession sess, DCEBuffer dceBuf, DCEPipeFile pipeFile)
            throws IOException, SMBSrvException
    {

        try
        {

            // DEBUG

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
                logger.debug("DCE Bind");

            // Get the call id and skip the DCE header

            int callId = dceBuf.getHeaderValue(DCEBuffer.HDR_CALLID);
            dceBuf.skipBytes(DCEBuffer.DCEDATA);

            // Unpack the bind request

            int maxTxSize = dceBuf.getShort();
            int maxRxSize = dceBuf.getShort();
            int groupId = dceBuf.getInt();
            int ctxElems = dceBuf.getByte(DCEBuffer.ALIGN_INT);
            int presCtxId = dceBuf.getByte(DCEBuffer.ALIGN_SHORT);
            int trfSyntax = dceBuf.getByte(DCEBuffer.ALIGN_SHORT);

            UUID uuid1 = dceBuf.getUUID(true);
            UUID uuid2 = dceBuf.getUUID(true);

            // Debug

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            {
                logger.debug("Bind: maxTx=" + maxTxSize + ", maxRx=" + maxRxSize + ", groupId=" + groupId
                        + ", ctxElems=" + ctxElems + ", presCtxId=" + presCtxId + ", trfSyntax=" + trfSyntax);
                logger.debug("      uuid1=" + uuid1.toString());
                logger.debug("      uuid2=" + uuid2.toString());
            }

            // Update the IPC pipe file

            pipeFile.setMaxTransmitFragmentSize(maxTxSize);
            pipeFile.setMaxReceiveFragmentSize(maxRxSize);

            // Create an output DCE buffer for the reply and add the bind acknowledge header

            DCEBuffer txBuf = new DCEBuffer();
            txBuf.putBindAckHeader(dceBuf.getHeaderValue(DCEBuffer.HDR_CALLID));
            txBuf.setHeaderValue(DCEBuffer.HDR_FLAGS, DCEBuffer.FLG_ONLYFRAG);

            // Pack the bind acknowledge DCE reply

            txBuf.putShort(maxTxSize);
            txBuf.putShort(maxRxSize);
            txBuf.putInt(0x53F0);

            String srvPipeName = DCEPipeType.getServerPipeName(pipeFile.getPipeId());
            txBuf.putShort(srvPipeName.length() + 1);
            txBuf.putASCIIString(srvPipeName, true, DCEBuffer.ALIGN_INT);
            txBuf.putInt(1);
            txBuf.putShort(0);
            txBuf.putShort(0);
            txBuf.putUUID(uuid2, true);

            txBuf.setHeaderValue(DCEBuffer.HDR_FRAGLEN, txBuf.getLength());

            // Attach the reply buffer to the pipe file

            pipeFile.setBufferedData(txBuf);
        }
        catch (DCEBufferException ex)
        {
            sess.sendErrorResponseSMB(SMBStatus.SRVNotSupported, SMBStatus.ErrSrv);
            return;
        }
    }

    /**
     * Process a DCE request
     * 
     * @param sess SMBSrvSession
     * @param dceBuf DCEBuffer
     * @param pipeFile DCEPipeFile
     * @exception IOException
     * @exception SMBSrvException
     */
    public static final void procDCERequest(SMBSrvSession sess, DCEBuffer inBuf, DCEPipeFile pipeFile)
            throws IOException, SMBSrvException
    {

        // Debug

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_DCERPC))
            logger.debug("DCE Request opNum=0x" + Integer.toHexString(inBuf.getHeaderValue(DCEBuffer.HDR_OPCODE)));

        // Pass the request to the DCE pipe request handler

        if (pipeFile.hasRequestHandler())
            pipeFile.getRequestHandler().processRequest(sess, inBuf, pipeFile);
        else
            sess.sendErrorResponseSMB(SMBStatus.SRVNoAccessRights, SMBStatus.ErrSrv);
    }
}
