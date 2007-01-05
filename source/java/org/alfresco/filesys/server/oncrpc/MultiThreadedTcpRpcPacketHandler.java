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
 * Multi-Threaded Tcp Rpc Packet Handler Class
 * 
 * <p>Adds multi-threaded processing of RPC requests to the standard TCP RPC handler.
 * 
 * @author GKSpencer
 */
public class MultiThreadedTcpRpcPacketHandler extends TcpRpcPacketHandler implements RpcPacketHandler {

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
	public MultiThreadedTcpRpcPacketHandler(TcpRpcSessionHandler handler, int sessId, RpcProcessor server,
			Socket socket, int maxRpcSize) throws IOException
	{
		super(handler, sessId, server, socket, maxRpcSize);
	}

	/**
	 * Return the multi-threaded RPC session handler
	 * 
	 * @return MultiThreadedTcpRpcSessionHandler
	 */
	protected final MultiThreadedTcpRpcSessionHandler getSessionHandler()
	{
		return (MultiThreadedTcpRpcSessionHandler) getHandler();
	}

	/**
	 * Allocate an RPC packet from the packet pool
	 * 
	 * @param maxSize int
	 * @return RpcPacket
	 */
	protected RpcPacket allocateRpcPacket(int maxSize)
	{

		//	Use the session handler to allocate the RPC packet

		return getSessionHandler().allocateRpcPacket(maxSize);
	}

	/**
	 * Deallocate an RPC packet, return the packet to the pool.
	 * 
	 * @param pkt RpcPacket
	 */
	protected void deallocateRpcPacket(RpcPacket pkt)
	{

		// Return the packet to the pool

		if (pkt.isAllocatedFromPool())
			pkt.getOwnerPacketPool().releasePacket(pkt);
	}

	/**
	 * Process an RPC request by passing the request to a pool of worker threads.
	 * 
	 * @param rpc RpcPacket
	 * @throws IOException
	 */
	protected void processRpc(RpcPacket rpc)
		throws IOException
	{

		//	Link the RPC request to this handler

		rpc.setPacketHandler(this);

		//	Queue the RPC request to the session handlers thread pool for processing

		getSessionHandler().queueRpcRequest(rpc);
	}

	/**
	 * Send an RPC response using the TCP socket connection
	 * 
	 * @param rpc RpcPacket
	 * @throws IOException
	 */
	public void sendRpcResponse(RpcPacket rpc)
		throws IOException
	{

		//	Send the RPC response

		sendRpc(rpc);
	}
}
