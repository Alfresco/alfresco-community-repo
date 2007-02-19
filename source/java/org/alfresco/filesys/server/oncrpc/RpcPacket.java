/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc;

import java.net.*;

import org.alfresco.filesys.util.DataPacker;

/**
 * ONC/RPC Request/Response Packet Class
 * 
 * @author GKSpencer
 */
public class RpcPacket {

  //	Constants
	//
	//	Default buffer size to allocate

	private static final int DefaultBufferSize = 8192;

	//	Fragment header length

	public static final int FragHeaderLen = 4;

	//	Fixed packet lengths

	public static final int ResponseMismatchLen = 24;
	public static final int ResponseAuthFailLen = 20;

	//	RPC data buffer

	private byte[] m_buffer;
	private int m_offset;

	//	Current buffer pack/unpack position and end of buffer position

	private int m_pos;
	private int m_endPos;

	//	Callers address, port and protocol

	private InetAddress m_clientAddr;
	private int m_clientPort;
	private int m_protocol;

	//	RPC packet handler interface used to send an RPC response

	private RpcPacketHandler m_pktHandler;

	//	Packet pool that owns this packet, if allocated from a pool

	private RpcPacketPool m_ownerPool;

	/**
	 * Default constructor
	 */
	public RpcPacket()
	{
		//	Allocate the RPC buffer

		m_buffer = new byte[DefaultBufferSize];
		m_offset = FragHeaderLen;

		m_pos = FragHeaderLen;
		m_endPos = m_buffer.length;
	}

	/**
	 * Class constructor
	 * 
	 * @param len int
	 */
	public RpcPacket(int len)
	{
		//	Allocate the RPC buffer

		m_buffer = new byte[len + FragHeaderLen];
		m_offset = FragHeaderLen;

		m_pos = FragHeaderLen;
		m_endPos = m_buffer.length;
	}

	/**
	 * Class constructor
	 * 
	 * @param len int
	 * @param owner RpcPacketPool
	 */
	protected RpcPacket(int len, RpcPacketPool owner)
	{
		this(len);

		//	Set the owner

		setOwnerPacketPool(owner);
	}

	/**
	 * Class constructor
	 * 
	 * @param buf byte[]
	 */
	public RpcPacket(byte[] buf)
	{
		m_buffer = buf;
		m_offset = FragHeaderLen;
		m_pos = FragHeaderLen;
		m_endPos = buf.length;
	}

	/**
	 * Class constructor
	 * 
	 * @param buf byte[]
	 * @param offset int
	 * @param len int
	 */
	public RpcPacket(byte[] buf, int offset, int len)
	{
		m_buffer = buf;
		m_offset = offset;
		m_pos = offset;
		m_endPos = offset + len;
	}

	/**
	 * Determine if the packet handler is valid
	 * 
	 * @return boolean
	 */
	public final boolean hasPacketHandler()
	{
		return m_pktHandler != null ? true : false;
	}

	/**
	 * Return the packet handler interface used to send/receive a packet
	 * 
	 * @return RpcPacketHandler
	 */
	public final RpcPacketHandler getPacketHandler()
	{
		return m_pktHandler;
	}

	/**
	 * Detemrine if the packet is allocated from a packet pool
	 * 
	 * @return boolean
	 */
	public final boolean isAllocatedFromPool()
	{
		return m_ownerPool != null ? true : false;
	}

	/**
	 * Return the packet pool that owns this packet
	 * 
	 * @return RpcPacketPool
	 */
	public final RpcPacketPool getOwnerPacketPool()
	{
		return m_ownerPool;
	}

	/**
	 * Determine if the client address has been set
	 * 
	 * @return boolean
	 */
	public final boolean hasClientAddress()
	{
		return m_clientAddr != null ? true : false;
	}

	/**
	 * Return the client network address
	 * 
	 * @return InetAddress
	 */
	public final InetAddress getClientAddress()
	{
		return m_clientAddr;
	}

	/**
	 * Return the client port
	 * 
	 * @return int
	 */
	public final int getClientPort()
	{
		return m_clientPort;
	}

	/**
	 * Return the client protocol
	 * 
	 * @return int
	 */
	public final int getClientProtocol()
	{
		return m_protocol;
	}

	/**
	 * Return the client details as a string
	 * 
	 * @return String
	 */
	public final String getClientDetails()
	{
		if (hasClientAddress() == false)
			return "<Unknown>";

		StringBuffer str = new StringBuffer(32);
		str.append(getClientProtocol() == Rpc.TCP ? "T" : "U");
		str.append(getClientAddress().getHostAddress());
		str.append(":");
		str.append(getClientPort());

		return str.toString();
	}

	/**
	 * Return the current buffer position
	 * 
	 * @return int
	 */
	public final int getPosition()
	{
		return m_pos;
	}

	/**
	 * Return the buffer
	 * 
	 * @return byte[]
	 */
	public final byte[] getBuffer()
	{
		return m_buffer;
	}

	/**
	 * Return the available buffer size
	 * 
	 * @return int
	 */
	public final int getAvailableLength()
	{
		return m_buffer.length - m_pos;
	}

	/**
	 * Return the used buffer length
	 * 
	 * @return int
	 */
	public final int getLength()
	{
		return m_endPos - m_offset;
	}

	/**
	 * Return the RPC + fragment header length
	 * 
	 * @return int
	 */
	public final int getTxLength()
	{
		if (m_offset == 0)
			return m_endPos;
		else
			return (m_endPos - m_offset) + FragHeaderLen;
	}

	/**
	 * Return the start of data offset
	 * 
	 * @return int
	 */
	public final int getOffset()
	{
		return m_offset;
	}

	/**
	 * Return the message type
	 * 
	 * @return int
	 */
	public final int getMessageType()
	{
		return DataPacker.getInt(m_buffer, m_offset + 4);
	}

	/**
	 * Return the RPC version
	 * 
	 * @return int
	 */
	public final int getRpcVersion()
	{
		return DataPacker.getInt(m_buffer, m_offset + 8);
	}

	/**
	 * Return the program id
	 * 
	 * @return int
	 */
	public final int getProgramId()
	{
		return DataPacker.getInt(m_buffer, m_offset + 12);
	}

	/**
	 * Return the program version
	 * 
	 * @return int
	 */
	public final int getProgramVersion()
	{
		return DataPacker.getInt(m_buffer, m_offset + 16);
	}

	/**
	 * Return the procedure id 
	 * 
	 * @return int
	 */
	public final int getProcedureId()
	{
		return DataPacker.getInt(m_buffer, m_offset + 20);
	}

	/**
	 * Return the credentials type 
	 * 
	 * @return int
	 */
	public final int getCredentialsType()
	{
		return DataPacker.getInt(m_buffer, m_offset + 24);
	}

	/**
	 * Return the credentials length 
	 * 
	 * @return int
	 */
	public final int getCredentialsLength()
	{
		return DataPacker.getInt(m_buffer, m_offset + 28);
	}

	/**
	 * Return the verifier type  
	 * 
	 * @return int
	 */
	public final int getVerifierType()
	{
		return DataPacker.getInt(m_buffer, m_offset + getCredentialsLength() + 32);
	}

	/**
	 * Return the verifier length  
	 * 
	 * @return int
	 */
	public final int getVerifierLength()
	{
		return DataPacker.getInt(m_buffer, m_offset + getCredentialsLength() + 36);
	}

	/**
	 * Return the buffer offset to the verifier
	 * 
	 * @return int
	 */
	public final int getVerifierOffset()
	{
		return m_offset + getCredentialsLength() + 40;
	}

	/**
	 * Return the procedure specific parameters offset
	 * 
	 * @return int
	 */
	public final int getProcedureParameterOffset()
	{
		return m_offset + getCredentialsLength() + getVerifierLength() + 40;
	}

	/**
	 * Return the procedure parameters length
	 * 
	 * @return int
	 */
	public final int getProcedureParameterLength()
	{
		return m_endPos - getProcedureParameterOffset();
	}

	/**
	 * Return the XID
	 * 
	 * @return int
	 */
	public final int getXID()
	{
		return DataPacker.getInt(m_buffer, m_offset);
	}

	/**
	 * Check if the response has a success status
	 * 
	 * @return boolean
	 */
	public final boolean hasSuccessStatus()
	{
		return getAcceptStatus() == Rpc.StsSuccess ? true : false;
	}

	/**
	 * Return the reply state
	 *
	 * @return int
	 */
	public final int getReplyState()
	{
		return DataPacker.getInt(m_buffer, 8);
	}

	/**
	 * Return the reject reply status
	 *
	 * @return int
	 */
	public final int getRejectStatus()
	{
		return DataPacker.getInt(m_buffer, 12);
	}

	/**
	 * Return the version mismatch low version
	 *
	 * @return int
	 */
	public final int getMismatchVersionLow()
	{
		return DataPacker.getInt(m_buffer, 16);
	}

	/**
	 * Return the version mismatch high version
	 *
	 * @return int
	 */
	public final int getMismatchVersionHigh()
	{
		return DataPacker.getInt(m_buffer, 20);
	}

	/**
	 * Return the authentication failure status
	 *
	 * @return int
	 */
	public final int getAuthFailStatus()
	{
		return DataPacker.getInt(m_buffer, 16);
	}

	/**
	 * Return the accept status for the RPC response
	 * 
	 * @return int
	 */
	public final int getAcceptStatus()
	{
		int pos = DataPacker.getInt(m_buffer, 16) + 20;
		return DataPacker.getInt(m_buffer, pos);
	}

	/**
	 * Align the buffer position on a longword/32bit boundary
	 * 
	 * @param ival
	 */
	protected final void alignPosition()
	{

		//	Align the buffer position on the required boundary

		m_pos = (m_pos + 3) & 0xFFFFFFFC;
	}

	/**
	 * Pack a byte value
	 * 
	 * @param bval int
	 */
	public final void packByte(int bval)
	{
		m_buffer[m_pos++] = (byte) (bval & 0xFF);
	}

	/**
	 * Pack nulls
	 * 
	 * @param len int
	 */
	public final void packNulls(int len)
	{
		for (int i = 0; i < len; i++)
			m_buffer[m_pos++] = (byte) 0;
	}

	/**
	 * Pack an integer value
	 *
	 * @param ival int
	 */
	public final void packInt(int ival)
	{
		DataPacker.putInt(ival, m_buffer, m_pos);
		m_pos += 4;
	}

	/**
	 * Pack a long value
	 *
	 * @param lval long
	 */
	public final void packLong(long lval)
	{
		DataPacker.putLong(lval, m_buffer, m_pos);
		m_pos += 8;
	}

	/**
	 * Pack a byte array with a length
	 * 
	 * @param buf byte[]
	 */
	public final void packByteArrayWithLength(byte[] buf)
	{
		DataPacker.putInt(buf.length, m_buffer, m_pos);
		m_pos += 4;
		System.arraycopy(buf, 0, m_buffer, m_pos, buf.length);
		m_pos += buf.length;
		alignPosition();
	}

	/**
	 * Pack a byte array
	 * 
	 * @param buf byte[]
	 */
	public final void packByteArray(byte[] buf)
	{
		System.arraycopy(buf, 0, m_buffer, m_pos, buf.length);
		m_pos += buf.length;
		alignPosition();
	}

	/**
	 * Pack an integer array
	 * 
	 * @param iarray int[]
	 */
	public final void packIntArrayWithLength(int[] iarray)
	{
		DataPacker.putInt(iarray.length, m_buffer, m_pos);
		m_pos += 4;
		for (int i = 0; i < iarray.length; i++)
		{
			DataPacker.putInt(iarray[i], m_buffer, m_pos);
			m_pos += 4;
		}
	}

	/**
	 * Pack a string
	 * 
	 * @param str String
	 */
	public final void packString(String str)
	{
		DataPacker.putInt(str != null ? str.length() : 0, m_buffer, m_pos);
		m_pos += 4;
		if (str != null)
		{
			m_pos = DataPacker.putString(str, m_buffer, m_pos, false);
			alignPosition();
		}
	}

	/**
	 * Pack a port mapping structure
	 * 
	 * @param portMap PortMapping
	 */
	public final void packPortMapping(PortMapping portMap)
	{
		DataPacker.putInt(portMap.getProgramId(), m_buffer, m_pos);
		DataPacker.putInt(portMap.getVersionId(), m_buffer, m_pos + 4);
		DataPacker.putInt(portMap.getProtocol(), m_buffer, m_pos + 8);
		DataPacker.putInt(portMap.getPort(), m_buffer, m_pos + 12);

		m_pos += 16;
	}

	/**
	 * Unpack an integer value
	 * 
	 * @return int
	 */
	public final int unpackInt()
	{
		int val = DataPacker.getInt(m_buffer, m_pos);
		m_pos += 4;
		return val;
	}

	/**
	 * Unpack a long value
	 * 
	 * @return long
	 */
	public final long unpackLong()
	{
		long val = DataPacker.getLong(m_buffer, m_pos);
		m_pos += 8;
		return val;
	}

	/**
	 * Unpack a string
	 * 
	 * @return String
	 */
	public final String unpackString()
	{
		int len = unpackInt();

		String str = "";
		if (len > 0)
		{
			str = DataPacker.getString(m_buffer, m_pos, len);
			m_pos += len;
			alignPosition();
		}

		return str;
	}

	/**
	 * Unpack a byte array with a length
	 * 
	 * @param buf byte[]
	 */
	public final void unpackByteArrayWithLength(byte[] buf)
	{
		int len = DataPacker.getInt(m_buffer, m_pos);
		m_pos += 4;
		if (len > 0)
		{
			System.arraycopy(m_buffer, m_pos, buf, 0, len);
			m_pos += len;
		}
		alignPosition();
	}

	/**
	 * Unpack a byte array, using the buffer length
	 * 
	 * @param buf byte[]
	 */
	public final void unpackByteArray(byte[] buf)
	{
		System.arraycopy(m_buffer, m_pos, buf, 0, buf.length);
		m_pos += buf.length;
		alignPosition();
	}

	/**
	 * Unpack an integer array, using the buffer length
	 * 
	 * @param buf int[]
	 */
	public final void unpackIntArray(int[] buf)
	{
		for (int i = 0; i < buf.length; i++)
			buf[i] = unpackInt();
	}

	/**
	 * Position the read pointer at the credentials data
	 */
	public final void positionAtCredentialsData()
	{
		m_pos = m_offset + 32;
	}

	/**
	 * Position the read pointer at the verifier data
	 */
	public final void positionAtVerifierData()
	{
		m_pos = getVerifierOffset();
	}

	/**
	 * Position the read pointer at the procedure specific parameters
	 */
	public final void positionAtParameters()
	{
		m_pos = getProcedureParameterOffset();
	}

	/**
	 * Skip a number of bytes in the buffer, rounded to the next int boundary
	 * 
	 * @param cnt int
	 */
	public final void skipBytes(int cnt)
	{
		m_pos += (cnt + 3) & 0xFFFC;
	}

	/**
	 * Set the client details
	 * 
	 * @param addr InetAddress
	 * @param port int
	 * @param protocol int
	 */
	public final void setClientDetails(InetAddress addr, int port, int protocol)
	{
		m_clientAddr = addr;
		m_clientPort = port;
		m_protocol = protocol;
	}

	/**
	 * Reset the buffer details
	 * 
	 * @param buf byte[]
	 * @param offset int
	 * @param len int
	 */
	public final void setBuffer(byte[] buf, int offset, int len)
	{
		m_buffer = buf;
		m_offset = offset;
		m_pos = offset;
		m_endPos = offset + len;
	}

	/**
	 * Reset the buffer details
	 * 
	 * @param offset int
	 * @param len int
	 */
	public final void setBuffer(int offset, int len)
	{
		m_offset = offset;
		m_pos = offset;
		m_endPos = offset + len;
	}

	/**
	 * Set the used buffer length
	 * 
	 * @param len int
	 */
	public final void setLength(int len)
	{
		m_endPos = len + m_offset;

		//	Set the fragment header, if the offset is non-zero

		if (m_offset == FragHeaderLen)
			DataPacker.putInt(getLength() + Rpc.LastFragment, m_buffer, 0);
	}

	/**
	 * Set the used buffer length
	 */
	public final void setLength()
	{
		m_endPos = m_pos;

		//	Set the fragment header, if the offset is non-zero

		if (m_offset == FragHeaderLen)
			DataPacker.putInt(getLength() + Rpc.LastFragment, m_buffer, 0);
	}

	/**
	 * Set the buffer position
	 * 
	 * @param pos int
	 */
	public final void setPosition(int pos)
	{
		m_pos = pos;
	}

	/**
	 * Set the message type
	 * 
	 * @param msgType int
	 */
	public final void setMessageType(int msgType)
	{
		DataPacker.putInt(msgType, m_buffer, m_offset + 4);
	}

	/**
	 * Set the RPC version
	 * 
	 * @param rpcVer int
	 */
	public final void setRpcVersion(int rpcVer)
	{
		DataPacker.putInt(rpcVer, m_buffer, m_offset + 8);
	}

	/**
	 * Set the program id
	 * 
	 * @param progId int
	 */
	public final void setProgramId(int progId)
	{
		DataPacker.putInt(progId, m_buffer, m_offset + 12);
	}

	/**
	 * Set the program version
	 * 
	 * @param progVer int
	 */
	public final void setProgramVersion(int progVer)
	{
		DataPacker.putInt(progVer, m_buffer, m_offset + 16);
	}

	/**
	 * Set the procedure id 
	 * 
	 * @param procId int
	 */
	public final void setProcedureId(int procId)
	{
		DataPacker.putInt(procId, m_buffer, m_offset + 20);
	}

	/**
	 * Set the credentials type 
	 * 
	 * @param credtype int
	 */
	public final void setCredentialsType(int credtype)
	{
		DataPacker.putInt(credtype, m_buffer, m_offset + 24);
	}

	/**
	 * Set the credentials length 
	 * 
	 * @param credlen int
	 */
	public final void setCredentialsLength(int credlen)
	{
		DataPacker.putInt(credlen, m_buffer, m_offset + 28);
	}

	/**
	 * Set the reply state
	 * 
	 * @param replySts int
	 */
	public final void setReplyState(int replySts)
	{
		DataPacker.putInt(replySts, m_buffer, m_offset + 8);
	}

	/**
	 * Set the reject status
	 * 
	 * @param rejSts int
	 */
	public final void setRejectStatus(int rejSts)
	{
		DataPacker.putInt(rejSts, m_buffer, m_offset + 8);
	}

	/**
	 * Set the RPC mismatch values
	 * 
	 * @param rpcLow int
	 * @param rpcHigh int
	 */
	public final void setRpcMismatch(int rpcLow, int rpcHigh)
	{
		DataPacker.putInt(rpcLow, m_buffer, m_offset + 12);
		DataPacker.putInt(rpcHigh, m_buffer, m_offset + 16);
	}

	/**
	 * Set the authentication failure status
	 * 
	 * @param authSts int
	 */
	public final void setAuthFailStatus(int authSts)
	{
		DataPacker.putInt(authSts, m_buffer, m_offset + 8);
	}

	/**
	 * Set the verifier type  
	 * 
	 * @param verftype int
	 */
	public final void setVerifierType(int verftype)
	{
		DataPacker.putInt(verftype, m_buffer, m_offset + getCredentialsLength() + 32);
	}

	/**
	 * Set the verifier length  
	 * 
	 * @param verflen int
	 */
	public final void setVerifierLength(int verflen)
	{
		DataPacker.putInt(verflen, m_buffer, m_offset + getCredentialsLength() + 36);
	}

	/**
	 * Set the associated packet handler interface for the packet
	 * 
	 * @param pktHandler RpcPacketHandler
	 */
	public final void setPacketHandler(RpcPacketHandler pktHandler)
	{
		m_pktHandler = pktHandler;
	}

	/**
	 * Set the XID
	 * 
	 * @param xid int
	 */
	public final void setXID(int xid)
	{
		DataPacker.putInt(xid, m_buffer, m_offset);
	}

	/**
	 * Set the owner packet pool, if the packet was allocated from a pool
	 * 
	 * @param pool RpcPacketPool
	 */
	protected final void setOwnerPacketPool(RpcPacketPool pool)
	{
		m_ownerPool = pool;
	}

	/**
	 * Build an RPC request header, and set the buffer pointer ready to stream data into the parameter
	 * area of the request
	 *
	 * @param progId int
	 * @param verId int
	 * @param procId int
	 * @param credType int
	 * @param cred byte[]
	 * @param verfType int
	 * @param verf byte[] 
	 */
	public final void buildRequestHeader(int progId, int verId, int procId, int credType, byte[] cred, int verfType,
			byte[] verf)
	{

		//	Generate an id for the request

		setXID((int) (System.currentTimeMillis() & 0xFFFFFFFFL));

		//	Set the message type and RPC version (always version 2)

		setMessageType(Rpc.Call);
		setRpcVersion(Rpc.RpcVersion);

		//	Set the request details

		setProgramId(progId);
		setProgramVersion(verId);
		setProcedureId(procId);

		//	Set the credentials type, length and value

		setCredentialsType(credType);
		setCredentialsLength(cred != null ? cred.length : 0);
		if (cred != null)
			System.arraycopy(cred, 0, m_buffer, m_offset + 32, cred.length);

		//	Set the verifier type, length and value

		setVerifierType(verfType);
		setVerifierLength(verf != null ? verf.length : 0);
		if (verf != null)
		{
			int pos = getVerifierOffset();
			System.arraycopy(verf, 0, m_buffer, pos, verf.length);
		}

		//	Position the buffer pointer at the request parameter area

		positionAtParameters();
	}

	/**
	 * Build a response header for a valid RPC response and set the buffer pointer ready to stream data
	 * into the parameter area of the response.
	 */
	public final void buildResponseHeader()
	{
		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallAccepted);

		//	Copy the verifier from the request

		DataPacker.putInt(getVerifierType(), m_buffer, m_offset + 12);

		int verfLen = getVerifierLength();
		DataPacker.putInt(verfLen, m_buffer, m_offset + 16);

		if (verfLen > 0)
			System.arraycopy(m_buffer, getVerifierOffset(), m_buffer, m_offset + 20, verfLen);

		//	Indicate a success status

		DataPacker.putInt(Rpc.StsSuccess, m_buffer, m_offset + 20 + verfLen);

		//	Set the buffer pointer for streaming the response parameters

		m_pos = m_offset + 24 + verfLen;
		setLength();
	}

	/**
	 * Build an error response packet where the RPC has been accepted but returns a status code in the parameter area.
	 * 
	 * @param stsCode int
	 */
	public final void buildErrorResponse(int stsCode)
	{

		// Check if the RPC is a request or reply

		boolean isReply = getMessageType() == Rpc.Reply;

		// Set the reply header

		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallAccepted);

		//	Copy the verifier from the request

		int verfLen = 0;

		if (isReply == false)
		{
			DataPacker.putInt(getVerifierType(), m_buffer, m_offset + 12);

			verfLen = getVerifierLength();
			DataPacker.putInt(verfLen, m_buffer, m_offset + 16);

			if (verfLen > 0)
				System.arraycopy(m_buffer, getVerifierOffset(), m_buffer, m_offset + 20, verfLen);
		} else
		{

			// Get the verifier length from the reply

			verfLen = DataPacker.getInt(m_buffer, m_offset + 16);
		}

		//	Indicate a success status

		DataPacker.putInt(Rpc.StsSuccess, m_buffer, m_offset + 20 + verfLen);

		//	Set the buffer pointer for streaming the response parameters

		m_pos = m_offset + 24 + verfLen;

		//	Pack the service status code

		DataPacker.putInt(stsCode, m_buffer, m_pos);
		m_pos += 4;
		setLength();
	}

	/**
	 * Build an RPC version mismatch response
	 */
	public final void buildRpcMismatchResponse()
	{
		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallDenied);
		setRejectStatus(Rpc.StsRpcMismatch);
		setRpcMismatch(Rpc.RpcVersion, Rpc.RpcVersion);

		setLength(ResponseMismatchLen);
	}

	/**
	 * Build an RPC authentication failure response
	 * 
	 * @param stsCode int
	 */
	public final void buildAuthFailResponse(int stsCode)
	{
		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallDenied);
		setRejectStatus(Rpc.StsAuthError);
		setAuthFailStatus(stsCode);

		setLength(ResponseAuthFailLen);
	}

	/**
	 * Build an RPC accept error response
	 * 
	 * @param stsCode int
	 */
	public final void buildAcceptErrorResponse(int stsCode)
	{
		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallAccepted);

		//	Copy the verifier from the request

		DataPacker.putInt(getVerifierType(), m_buffer, m_offset + 12);

		int verfLen = getVerifierLength();
		DataPacker.putInt(verfLen, m_buffer, m_offset + 16);

		if (verfLen > 0)
			System.arraycopy(m_buffer, getVerifierOffset(), m_buffer, m_offset + 20, verfLen);

		//	Pack the status code

		DataPacker.putInt(stsCode, m_buffer, m_offset + 20 + verfLen);

		//	Set the response length

		setLength(m_offset + 24 + verfLen);
	}

	/**
	 * Build a program mismatch error response
	 * 
	 * @param verLow int
	 * @param verHigh int
	 */
	public final void buildProgramMismatchResponse(int verLow, int verHigh)
	{
		setMessageType(Rpc.Reply);
		setReplyState(Rpc.CallAccepted);

		//	Copy the verifier from the request

		DataPacker.putInt(getVerifierType(), m_buffer, m_offset + 12);

		int verfLen = getVerifierLength();
		DataPacker.putInt(verfLen, m_buffer, m_offset + 16);

		if (verfLen > 0)
			System.arraycopy(m_buffer, getVerifierOffset(), m_buffer, m_offset + 20, verfLen);

		//	Pack the status code, and low/high version numbers

		int pos = m_offset + 20 + verfLen;
		DataPacker.putInt(Rpc.StsProgMismatch, m_buffer, pos);
		DataPacker.putInt(verLow, m_buffer, pos + 4);
		DataPacker.putInt(verHigh, m_buffer, pos + 8);

		//	Set the response length

		setLength(pos + 12);
	}

	/**
	 * Return the RPC packet as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuffer str = new StringBuffer(128);

		//	Dump the client details

		str.append("[");
		if (hasClientAddress())
		{
			str.append(getClientProtocol() == Rpc.TCP ? "T" : "U");
			str.append(getClientAddress().getHostAddress());
			str.append(":");
			str.append(getClientPort());
		} else
			str.append("<Unknown>");

		//	Dump the call/response header

		if (getMessageType() == Rpc.Call)
		{

			//	Request packet

			str.append("-Call,XID=0x");
			str.append(Integer.toHexString(getXID()));

			str.append(",RpcVer=");
			str.append(getRpcVersion());

			str.append(",ProgId=");
			str.append(getProgramId());
			str.append(",ProgVer=");
			str.append(getProgramVersion());

			str.append(",Proc=");
			str.append(getProcedureId());

			str.append(",CredType=");
			str.append(getCredentialsType());
			str.append(",CredLen=");
			str.append(getCredentialsLength());

			str.append(",VerfType");
			str.append(getVerifierType());
			str.append(",VerfLen=");
			str.append(getVerifierLength());

			str.append(",ParamLen=");
			str.append(getProcedureParameterLength());
		} else
		{

			//	Response packet

			str.append("-Reply,XID=0x");
			str.append(Integer.toHexString(getXID()));

			if (getReplyState() == Rpc.CallAccepted)
			{

				//	Request accepted response

				str.append(",Accepted");
			} else
			{

				//	Request denied response

				str.append(",Denied");

				if (getRejectStatus() == Rpc.StsRpcMismatch)
				{
					str.append(",RpcMismatch, Low=");
					str.append(getMismatchVersionLow());
					str.append("/High=");
					str.append(getMismatchVersionHigh());
				} else
				{
					str.append(",AuthError, Status=");
					;
					str.append(getAuthFailStatus());
				}
			}
		}

		//	Check if the packet is allocated from a pool

		if (isAllocatedFromPool())
			str.append(",Pool");
		str.append("]");

		//	Return the string

		return str.toString();
	}
}
