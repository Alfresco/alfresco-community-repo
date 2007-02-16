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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc;

import java.io.*;
import java.net.*;

import org.alfresco.filesys.server.DatagramSessionHandler;
import org.alfresco.filesys.server.NetworkServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * UDP RPC Datagram Handler Class
 * 
 * <p>Receives RPC requests via a datagram and passes the request to the registered RPC server.
 * 
 * @author GKSpencer
 */
public class UdpRpcDatagramHandler extends DatagramSessionHandler
{
	// Debug logging

	protected static final Log logger = LogFactory.getLog(UdpRpcDatagramHandler.class);
	
	//	RPC server implementation that handles the RPC processing

	private RpcProcessor m_rpcProcessor;

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param protocol String
	 * @param rpcServer RpcProcessor
	 * @param server NetworkServer
	 * @param addr InetAddress
	 * @param port int
	 * @param maxSize int
	 */
	public UdpRpcDatagramHandler(String name, String protocol, RpcProcessor rpcServer, NetworkServer server,
			InetAddress addr, int port, int maxSize)
	{
		super(name, protocol, server, addr, port);

		//	Set the RPC server implementation that will handle the actual requests

		m_rpcProcessor = rpcServer;

		//	Set the maximum RPC request size allowed

		setMaximumDatagramSize(maxSize);
	}

	/**
	 * Return the RPC server used to process the requests
	 * 
	 * @return RpcProcessor
	 */
	protected final RpcProcessor getRpcProcessor()
	{
		return m_rpcProcessor;
	}

	/**
	 * Process the RPC datagram
	 * 
	 * @param pkt DatagramPacket
	 * @return boolean
	 * @throws IOException
	 */
	protected boolean processDatagram(DatagramPacket pkt)
		throws IOException
	{

		//	The default implementation processes the RPC immediately then returns to the main datagram handler
		//	to wait for the next datagram to be received. In this case the datagram packet can be re-used as
		//	processing is done sequentially.

		//	Wrap the datagram data up as an RPC request

		RpcPacket rpcPkt = new RpcPacket(pkt.getData(), 0, pkt.getLength());

		//	Set the client details

		rpcPkt.setClientDetails(pkt.getAddress(), pkt.getPort(), Rpc.UDP);

		//	Validate the RPC header

		if (rpcPkt.getRpcVersion() != Rpc.RpcVersion)
		{

			//	Build/send an error response

			rpcPkt.buildRpcMismatchResponse();
			pkt.setData(rpcPkt.getBuffer(), rpcPkt.getOffset(), RpcPacket.ResponseMismatchLen);

			sendDatagram(pkt);
		}
		else
		{

			//	Pass the request to the registered RPC server to process

			RpcPacket response = m_rpcProcessor.processRpc(rpcPkt);

			//	Send the RPC response

			if (response != null)
			{
				pkt.setData(response.getBuffer(), response.getOffset(), response.getLength());
				sendDatagram(pkt);
			}
		}

		//	Indicate that the existing datagram packet can be re-used for the next request

		return true;
	}
}
