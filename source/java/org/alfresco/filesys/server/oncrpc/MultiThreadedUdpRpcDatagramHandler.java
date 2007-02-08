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

import org.alfresco.filesys.server.NetworkServer;

/**
 * Multi-Threaded UDP RPC Datagram Handler Class
 * 
 * <p>Extend the basic UDP RPC handler class to process RPC requests using a thread pool.
 * 
 * @author GKSpencer
 */
public class MultiThreadedUdpRpcDatagramHandler extends UdpRpcDatagramHandler implements RpcPacketHandler {

	//	Constants
	//
	//	Default packet pool size

	public static final int DefaultPacketPoolSize 	= 50;
	public static final int DefaultSmallPacketSize 	= 512;

	//	RPC packet pool

	private RpcPacketPool m_packetPool;

	//	Request handler thread pool

	private RpcRequestThreadPool m_threadPool;

	//	RPC response queue

	private RpcRequestQueue m_txQueue;

	//	Datagram sender thread

	private DatagramSender m_txThread;

	//	Current receive RPC packet

	private RpcPacket m_rxPkt;

	/**
	 * Datagram Sender Thread Inner Class
	 */
	protected class DatagramSender implements Runnable
	{

		//	Worker thread

		private Thread mi_thread;

		//	RPC sender thread datagram packet

		private DatagramPacket mi_txPkt;

		//	Shutdown flag

		private boolean mi_shutdown = false;

		/**
		 * Class constructor
		 * 
		 * @param name String
		 */
		public DatagramSender(String name)
		{

			//	Create the worker thread

			mi_thread = new Thread(this);
			mi_thread.setName(name);
			mi_thread.setDaemon(true);
			mi_thread.start();
		}

		/**
		 * Request the worker thread to shutdown
		 */
		public final void shutdownRequest()
		{
			mi_shutdown = true;
			try
			{
				mi_thread.interrupt();
			} catch (Exception ex)
			{
			}
		}

		/**
		 * Run the thread
		 */
		public void run()
		{

			//	Allocate the datagram packet for sending the RPC responses

			mi_txPkt = new DatagramPacket(new byte[4], 4);

			//	Loop until shutdown

			RpcPacket rpc = null;

			while (mi_shutdown == false)
			{

				try
				{

					//	Wait for an RPC response to be queued

					rpc = m_txQueue.removeRequest();
				} catch (InterruptedException ex)
				{

					//	Check for shutdown

					if (mi_shutdown == true)
						break;
				}

				//	If the request is valid process it

				if (rpc != null)
				{

					try
					{

						//	Initialize the datagram packet for this response

						mi_txPkt.setAddress(rpc.getClientAddress());
						mi_txPkt.setPort(rpc.getClientPort());
						mi_txPkt.setData(rpc.getBuffer(), rpc.getOffset(), rpc.getLength());

						//	Send the RPC response

						getDatagramSocket().send(mi_txPkt);
					} catch (Throwable ex)
					{

						//	Do not display errors if shutting down

						if (mi_shutdown == false)
						{
							logger.debug("DatagramSender " + Thread.currentThread().getName() + ":");
							logger.debug(ex);
						}
					} finally
					{

						//	Release the RPC packet back to the packet pool

						if (rpc.isAllocatedFromPool())
							rpc.getOwnerPacketPool().releasePacket(rpc);
					}
				}
			}
		}
	};

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
	public MultiThreadedUdpRpcDatagramHandler(String name, String protocol, RpcProcessor rpcServer,
			NetworkServer server, InetAddress addr, int port, int maxSize)
	{
		super(name, protocol, rpcServer, server, addr, port, maxSize);
	}

	/**
	 * Initialize the session handler
	 * 
	 * @param server NetworkServer
	 * @throws IOException
	 */
	public void initializeSessionHandler(NetworkServer server)
		throws IOException
	{

		//	Create the RPC response queue

		m_txQueue = new RpcRequestQueue();

		//	Create the datagram sender thread

		m_txThread = new DatagramSender("UDP_Tx_" + getProtocolName());

		//	If the packet pool has not been created, create a default packet pool

		if (m_packetPool == null)
			m_packetPool = new RpcPacketPool(DefaultSmallPacketSize, DefaultPacketPoolSize, getMaximumDatagramSize(),
					DefaultPacketPoolSize);

		//	Create the RPC request handling thread pool, if not already created

		if (m_threadPool == null)
			m_threadPool = new RpcRequestThreadPool(getHandlerName(), getRpcProcessor());

		// Call the base class initialization

		super.initializeSessionHandler(server);
	}

	/**
	 * Process the RPC request
	 * 
	 * @param pkt DatagramPacket
	 * @return boolean
	 * @throws IOException
	 */
	protected boolean processDatagram(DatagramPacket pkt)
		throws IOException
	{

		//	Make sure that the received data is using the same buffer that we allocated in the
		//	allocateBuffer() method, if not the buffer did not come from the packet pool.

		if (pkt.getData() != m_rxPkt.getBuffer())
			throw new IOException("Received datagram is not in expected buffer");

		//	Update the RPC packet details

		m_rxPkt.setBuffer(pkt.getData(), 0, pkt.getLength());

		//	Set the client details

		m_rxPkt.setClientDetails(pkt.getAddress(), pkt.getPort(), Rpc.UDP);

		//	Set the packet handler interface to be used to send the RPC reply

		m_rxPkt.setPacketHandler(this);

		//	Queue the request to the thread pool for processing

		queueRpcRequest(m_rxPkt);

		//	Indicate that the datagram buffer cannot be re-used, the main datagram receiving thread must
		//	allocate a new buffer for the next request.

		return false;
	}

	/**
	 * Queue an RPC request to the thread pool for processing
	 * 
	 * @param rpc RpcPacket
	 */
	protected final void queueRpcRequest(RpcPacket rpc)
	{

		//	Queue the RPC request to the thread pool for processing

		m_threadPool.queueRpcRequest(rpc);
	}

	/**
	 * Allocate a buffer for the next datagram
	 * 
	 * @param bufSize int
	 * @return byte[]
	 */
	protected byte[] allocateBuffer(int bufSize)
	{

		//	Allocate an RPC packet from the packet pool

		m_rxPkt = m_packetPool.allocatePacket(bufSize);

		//	Return the buffer from the RPC packet

		return m_rxPkt.getBuffer();
	}

	/**
	 * Send an RPC response using the datagram socket
	 * 
	 * @param rpc RpcPacket
	 * @throws IOException
	 */
	public void sendRpcResponse(RpcPacket rpc)
		throws IOException
	{

		//	Queue the RPC response to the datagram sender thread

		m_txQueue.addRequest(rpc);
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

			m_packetPool = new RpcPacketPool(DefaultSmallPacketSize, poolSize, getMaximumDatagramSize(), poolSize);
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

	/**
	 * Close the session handler
	 * 
	 * @param server NetworkServer
	 */
	public void closeSessionHandler(NetworkServer server)
	{

		//	Shutdown the datagram sender thread

		m_txThread.shutdownRequest();

		//	Call the base class

		super.closeSessionHandler(server);
	}
}
