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
