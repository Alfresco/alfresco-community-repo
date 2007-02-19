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

import java.io.*;
import java.net.*;

/**
 * TCP RPC Client Connection Class
 * 
 * @author GKSpencer
 */
public class TcpRpcClient extends RpcClient {

	//	TCP RPC client connection

	private TcpRpcPacketHandler m_client;

	/**
	 * Class constructor 
	 *
	 * @param addr InetAddress
	 * @param port int
	 * @param maxRpcSize int
	 * @throws IOException
	 */
	public TcpRpcClient(InetAddress addr, int port, int maxRpcSize) throws IOException
	{
		super(addr, port, Rpc.TCP, maxRpcSize);

		//	Connect a socket to the remote server

		Socket sock = new Socket(getServerAddress(), getServerPort());

		//	Create the TCP RPC packet handler for the client connection

		m_client = new TcpRpcPacketHandler(sock, maxRpcSize);
	}

	/**
	 * Send an RPC request using the socket connection, and receive a response
	 * 
	 * @param rpc RpcPacket
	 * @param rxRpc RpcPacket
	 * @return RpcPacket
	 * @throws IOException
	 */
	public RpcPacket sendRPC(RpcPacket rpc, RpcPacket rxRpc)
		throws IOException
	{

		//	Use the TCP packet handler to send the RPC

		m_client.sendRpc(rpc);

		//	Receive a response RPC

		RpcPacket rxPkt = rxRpc;
		if (rxPkt == null)
			rxPkt = new RpcPacket(getMaximumRpcSize());

		m_client.receiveRpc(rxPkt);

		//	Return the RPC response

		return rxPkt;
	}

	/**
	 * Close the connection to the remote RPC server 
	 */
	public void closeConnection()
	{

		//	Close the packet handler

		if (m_client != null)
		{
			m_client.closePacketHandler();
			m_client = null;
		}
	}
}
