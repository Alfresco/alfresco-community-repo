/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.server.oncrpc;

import java.io.*;
import java.net.*;

import org.alfresco.filesys.server.SocketPacketHandler;
import org.alfresco.filesys.util.DataPacker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TCP RPC Packet Handler Class
 * 
 * <p>Processes RPC requests received via TCP session.
 * 
 * @author GKSpencer
 */
public class TcpRpcPacketHandler extends SocketPacketHandler implements Runnable {

	// Debug logging

	private static final Log logger = LogFactory.getLog(TcpRpcPacketHandler.class);
	
	//	Session handler that owns this session

	private TcpRpcSessionHandler m_handler;

	//	RPC server implementation used to process the requests

	private RpcProcessor m_rpcProcessor;

	//	Session id

	private int m_sessId;

	//	RPC processing thread shutdown flag

	private boolean m_shutdown;

	//	Maximum RPC size accepted

	private int m_maxRpcSize;

	//	Packet buffer for receiving incoming RPC requests

	private RpcPacket m_rxPkt;

	//	Fragment header buffer

	private byte[] m_fragBuf;

	/**
	 * Class constructor to create a TCP RPC handler for a server.
	 *
	 * @param handler TcpRpcSessionHandler
	 * @param sessId int 
	 * @param server RpcProcessor 
	 * @param socket Socket
	 * @param maxRpcSize int
	 * @throws IOException
	 */
	public TcpRpcPacketHandler(TcpRpcSessionHandler handler, int sessId, RpcProcessor server, Socket socket,
			int maxRpcSize) throws IOException
	{
		super(socket);

		//	Set the session handler that owns this session

		m_handler = handler;

		//	set the session id

		m_sessId = sessId;

		//	Set the RPC server to be used to process requests

		m_rpcProcessor = server;

		//	Set the maximum RPC size accepted

		m_maxRpcSize = maxRpcSize;

		//	Allocate the RPC fragment header buffer

		m_fragBuf = new byte[4];

		//	Create a thread to run the RPC processing for this session

		Thread th = new Thread(this);
		th.setName(handler.getProtocolName() + "_" + getSessionId());
		th.start();
	}

	/**
	 * Class constructor to create a TCP RPC handler for a client.
	 *
	 * @param socket Socket
	 * @param maxRpcSize int
	 * @throws IOException
	 */
	public TcpRpcPacketHandler(Socket socket, int maxRpcSize) throws IOException
	{
		super(socket);

		//	Allocate the RPC fragment header buffer

		m_maxRpcSize = maxRpcSize;
		m_fragBuf = new byte[4];
	}

	/**
	 * Return the protocol name
	 * 
	 * @return String
	 */
	public String getProtocolName()
	{
		return "TCP RPC";
	}

	/**
	 * Return the session id
	 * 
	 * @return int
	 */
	public final int getSessionId()
	{
		return m_sessId;
	}

	/**
	 * Return the maximum RPC size accepted
	 * 
	 * @return int
	 */
	public final int getMaximumRpcSize()
	{
		return m_maxRpcSize;
	}

	/**
	 * Return the associated session handler
	 *
	 * @return TcpRpcSessionHandler
	 */
	protected final TcpRpcSessionHandler getHandler()
	{
		return m_handler;
	}

	/**
	 * Thread to read and process the RPC requests for this session
	 */
	public void run()
	{

		//	Loop until shutdown

		int rxLen = 0;
		RpcPacket rpcPkt = null;

		while (m_shutdown == false)
		{

			try
			{

				//	allocate an RPC packet to receive an incoming request

				rpcPkt = allocateRpcPacket(getMaximumRpcSize());

				//	Read an RPC request

				rxLen = receiveRpc(rpcPkt);

				if (rxLen == -1)
				{

					//  Release the packet

					deallocateRpcPacket(rpcPkt);

					//	Receive error, client has closed the socket

					m_handler.closeSession(getSessionId());
					break;
				}
			} catch (SocketException ex)
			{

				//  Release the packet

				if (rpcPkt != null)
					deallocateRpcPacket(rpcPkt);

				// Socket error, close the session

				m_handler.closeSession(getSessionId());
				break;
			} catch (IOException ex)
			{

				//	Only dump errors if not shutting down

				if (m_shutdown == false)
					logger.debug(ex);
			}

			//	Process the RPC request

			try
			{

				//	Validate the RPC header

				if (rpcPkt.getRpcVersion() != Rpc.RpcVersion)
				{

					//	Build/send an error response

					rpcPkt.buildRpcMismatchResponse();
					sendRpc(rpcPkt);
				} else
				{

					//	Process the RPC request

					processRpc(rpcPkt);
				}
			} catch (IOException ex)
			{

				//	Only dump errors if not shutting down

				if (m_shutdown == false)
					logger.debug(ex);
			}
		}
	}

	/**
	 * Close the session
	 */
	public void closePacketHandler()
	{

		//	Request the RPC processing thread to shutdown

		m_shutdown = true;

		//	Close the input/output streams and socket

		super.closePacketHandler();
	}

	/**
	 * Send an RPC request/response packet
	 * 
	 * @param rpc RpcPacket
	 * @exception IOException
	 */
	protected final void sendRpc(RpcPacket rpc)
		throws IOException
	{

		//	Write the RPC response, this includes the fragment header
		//
		//	If the fragment header is written seperately to the main RPC response packet trace tools
		//	such as Ethereal will not display the details properly.

		writePacket(rpc.getBuffer(), 0, rpc.getTxLength());
	}

	/**
	 * Read an RPC request/response
	 * 
	 * @param rpc RpcPacket
	 * @return int
	 * @throws IOException
	 */
	protected final int receiveRpc(RpcPacket rpc)
		throws IOException
	{

		//	Use the main receive method

		int rxLen = receiveRpc(rpc.getBuffer(), RpcPacket.FragHeaderLen, rpc.getBuffer().length
				- RpcPacket.FragHeaderLen);
		if (rxLen > 0)
		{

			//	Set the received length

			rpc.setBuffer(RpcPacket.FragHeaderLen, rxLen + RpcPacket.FragHeaderLen);

			//	Set the client details

			rpc.setClientDetails(getSocket().getInetAddress(), getSocket().getPort(), Rpc.TCP);
		}

		//	Return the received data length

		return rxLen;
	}

	/**
	 * Read an RPC request/response
	 * 
	 * @param buffer byte[]
	 * @param offset int
	 * @param maxLen int
	 * @return int
	 * @throws IOException
	 */
	protected final int receiveRpc(byte[] buffer, int offset, int maxLen)
		throws IOException
	{

		//	Fill the buffer until the last fragment is received

		int rxLen = 0;
		int totLen = 0;
		int rxOffset = offset;
		int fragLen = 0;
		boolean lastFrag = false;

		while (lastFrag == false)
		{

			//	Read in a header to get the fragment length

			rxLen = readPacket(m_fragBuf, 0, 4);
			if (rxLen == -1)
				return rxLen;

			//	Check if we received the last fragment

			fragLen = DataPacker.getInt(m_fragBuf, 0);

			if ((fragLen & Rpc.LastFragment) != 0)
			{
				lastFrag = true;
				fragLen = fragLen & Rpc.LengthMask;
			}

			//	Check if the buffer is large enough to receive the request

			if (fragLen > (buffer.length - rxOffset))
				throw new IOException("Receive RPC buffer overflow, fragment len = " + fragLen);

			//  Read the data part of the packet into the users buffer, this may take
			//  several reads

			while (fragLen > 0)
			{

				//  Read the data

				rxLen = readPacket(buffer, offset, fragLen);

				//	Check if the connection has been closed

				if (rxLen == -1)
					return -1;

				//  Update the received length and remaining data length

				totLen += rxLen;
				fragLen -= rxLen;

				//  Update the user buffer offset as more reads will be required
				//  to complete the data read

				offset += rxLen;

			} // end while reading data

		} // end while fragments

		//	Return the total length read

		return totLen;
	}

	/**
	 * Allocate an RPC packet for receiving an incoming request. This method must be overridden for
	 * multi-threaded implementations.
	 *
	 * @param maxSize int 
	 * @return RpcPacket
	 */
	protected RpcPacket allocateRpcPacket(int maxSize)
	{

		//	Check if the receive packet has been allocated

		if (m_rxPkt == null)
			m_rxPkt = new RpcPacket(maxSize);

		//	Return the RPC receive packet

		return m_rxPkt;
	}

	/**
	 * Deallocate an RPC packet, default method does nothing but a pooled implementation may
	 * return the packet to the pool.
	 * 
	 * @param pkt RpcPacket
	 */
	protected void deallocateRpcPacket(RpcPacket pkt)
	{
	}

	/**
	 * Process an RPC request. This method must be overridden for multi-threaded implementations.
	 * 
	 * @param rpc RpcPacket
	 * @exception IOException
	 */
	protected void processRpc(RpcPacket rpc)
		throws IOException
	{

		//	Process the RPC request in the current thread

		RpcPacket response = m_rpcProcessor.processRpc(rpc);

		//	Send the RPC response

		if (response != null)
			sendRpc(response);
	}
}
