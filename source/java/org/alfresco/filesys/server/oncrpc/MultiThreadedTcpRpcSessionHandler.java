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

import org.alfresco.filesys.server.NetworkServer;

/**
 * Multi-threaded TCP RPC Session Handler Class
 * 
 * <p>Extend the basic TCP RPC handler class to process RPC requests using a thread pool.
 * 
 * @author GKSpencer
 */
public class MultiThreadedTcpRpcSessionHandler extends TcpRpcSessionHandler {

	//	Constants
	//
	//	Default packet pool size

	public static final int DefaultPacketPoolSize 	= 50;
	public static final int DefaultSmallPacketSize 	= 512;

	//	RPC packet pool

	private RpcPacketPool m_packetPool;

	//	Request handler thread pool

	private RpcRequestThreadPool m_threadPool;

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
	public MultiThreadedTcpRpcSessionHandler(String name, String protocol, RpcProcessor rpcServer,
			NetworkServer server, InetAddress addr, int port, int maxSize)
	{
		super(name, protocol, rpcServer, server, addr, port, maxSize);
	}

	/**
	 * Initialize the session socket handler
	 * 
	 * @param server
	 * @throws IOException
	 */
	public void initializeSessionHandler(NetworkServer server)
		throws IOException
	{

		//	If the packet pool has not been created, create a default packet pool

		if (m_packetPool == null)
			m_packetPool = new RpcPacketPool(DefaultSmallPacketSize, DefaultPacketPoolSize, getMaximumRpcSize(),
					DefaultPacketPoolSize);

		//	Create the RPC request handling thread pool, if not already created

		if (m_threadPool == null)
			m_threadPool = new RpcRequestThreadPool(getHandlerName(), getRpcProcessor());

		//	Call the base class initialization

		super.initializeSessionHandler(server);
	}

	/**
	 * Allocate an RPC packet from the packet pool
	 * 
	 * @param size int
	 * @return RpcPacket
	 */
	protected final RpcPacket allocateRpcPacket(int size)
	{

		//	Allocate an RPC packet from the packet pool

		return m_packetPool.allocatePacket(size);
	}

	/**
	 * Queue an RPC request to the thread pool for processing
	 * 
	 * @param rpc RpcPacket
	 */
	protected final void queueRpcRequest(RpcPacket rpc)
	{

		//	DEBUG

		//    Debug.println("MTRpcSessHandler Queue rpc=" + rpc.toString());

		//	Queue the RPC request to the thread pool for processing

		m_threadPool.queueRpcRequest(rpc);
	}

	/**
	 * Create a multi-threaded packet handler for the new session
	 * 
	 * @param sessId int
	 * @param sock Socket
	 * @return TcpRpcPacketHandler
	 * @throws IOException
	 */
	protected TcpRpcPacketHandler createPacketHandler(int sessId, Socket sock)
		throws IOException
	{

		//	Create a multi-threaded packet handler to use the session handlers thread pool to
		//	process the RPC requests

		return new MultiThreadedTcpRpcPacketHandler(this, sessId, getRpcProcessor(), sock, getMaximumRpcSize());
	}

	/**
	 * Set the packet pool size
	 * 
	 * @param smallSize int
	 * @param smallPool int
	 * @param largeSize int
	 * @param largePool int
	 */
	public final void setPacketPool(int smallSize, int smallPool, int largeSize, int largePool)
	{

		//	Create the packet pool, if not already initialized

		if (m_packetPool == null)
		{

			//	Create the packet pool

			m_packetPool = new RpcPacketPool(smallSize, smallPool, largeSize, largePool);
		}
	}

	/**
	 * Set the packet pool size
	 * 
	 * @param poolSize int
	 */
	public final void setPacketPool(int poolSize)
	{

		//	Create the packet pool, if not already initialized

		if (m_packetPool == null)
		{

			//	Create the packet pool

			m_packetPool = new RpcPacketPool(DefaultSmallPacketSize, poolSize, getMaximumRpcSize(), poolSize);
		}
	}

	/**
	 * Set the packet pool
	 * 
	 * @param pktPool RpcPacketPool
	 */
	public final void setPacketPool(RpcPacketPool pktPool)
	{

		//	Set the packet pool, if not already initialized

		if (m_packetPool == null)
			m_packetPool = pktPool;
	}

	/**
	 * Set the thread pool size
	 * 
	 * @param numThreads int
	 */
	public final void setThreadPool(int numThreads)
	{

		//	Create the thread pool, if not already initialized

		if (m_threadPool == null)
		{

			//	Create the thread pool

			m_threadPool = new RpcRequestThreadPool(getHandlerName(), numThreads, getRpcProcessor());
		}
	}

	/**
	 * Set the thread pool
	 * 
	 * @param threadPool RpcRequestThreadPool
	 */
	public final void setThreadPool(RpcRequestThreadPool threadPool)
	{

		//	Set the thread pool, if not already initialized

		if (m_threadPool == null)
			m_threadPool = threadPool;
	}
}
