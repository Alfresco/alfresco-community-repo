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
package org.alfresco.filesys.server;

import java.io.*;
import java.net.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Datagram Session Handler Class
 * 
 * <p>Implementation of a session handler that uses a Java datagram socket to listen for incoming requests.
 * 
 * @author GKSpencer
 */
public abstract class DatagramSessionHandler implements SessionHandlerInterface, Runnable {

	// Debug logging

	protected static final Log logger = LogFactory.getLog(DatagramSessionHandler.class);
	
	//	Server that the handler is associated with

	private NetworkServer m_server;

	//	Address/port to use

	private int m_port;

	private InetAddress m_bindAddr;

	//	Datagram socket to listen for incoming requests

	private DatagramSocket m_srvSock;

	//	Maximum datagram size

	private int m_maxDgramSize;

	//	Session id

	private int m_sessId;

	//	Session handler name, protocol name

	private String m_name;

	private String m_protocol;

	//	Shutdown request flag

	private boolean m_shutdown;

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param protocol String
	 * @param server NetworkServer
	 * @param addr InetAddress
	 * @param port int
	 */
	protected DatagramSessionHandler(String name, String protocol, NetworkServer server, InetAddress addr, int port)
	{
		m_name = name;
		m_protocol = protocol;
		m_server = server;

		m_bindAddr = addr;
		m_port = port;
	}

	/**
	 * Return the maximum datagram size allowed
	 * 
	 * @return int
	 */
	public final int getMaximumDatagramSize()
	{
		return m_maxDgramSize;
	}

	/**
	 * Return the session handler name
	 * 
	 * @return String
	 */
	public final String getHandlerName()
	{
		return m_name;
	}

	/**
	 * Return the short protocol name
	 * 
	 * @return String
	 */
	public final String getProtocolName()
	{
		return m_protocol;
	}

	/**
	 * Check if the server should bind to a specific network address
	 * 
	 * @return boolean
	 */
	public final boolean hasBindAddress()
	{
		return m_bindAddr != null ? true : false;
	}

	/**
	 * Return the network address that the server should bind to
	 * 
	 * @return InetAddress
	 */
	public final InetAddress getBindAddres()
	{
		return m_bindAddr;
	}

	/**
	 * Return the port that the server should bind to
	 * 
	 * @return int
	 */
	public final int getPort()
	{
		return m_port;
	}

	/**
	 * Clear the shutdown flag
	 */
	protected final void clearShutdown()
	{
		m_shutdown = false;
	}

	/**
	 * Determine if the shutdown flag has been set
	 * 
	 * @return boolean
	 */
	protected final boolean hasShutdown()
	{
		return m_shutdown;
	}

	/**
	 * Get the next available session id
	 * 
	 * @return int
	 */
	protected synchronized int getNextSessionId()
	{
		return m_sessId++;
	}

	/**
	 * Set the local port that the datagram handler is using
	 * 
	 * @param port int
	 */
	protected final void setPort(int port)
	{
		m_port = port;
	}

	/**
	 * Return the datagrma socket
	 * 
	 * @return DatagramSocket
	 */
	protected final DatagramSocket getDatagramSocket()
	{
		return m_srvSock;
	}

	/**
	 * Initialize the session handler 
	 * 
	 * @param server NetworkServer
	 */
	public void initializeSessionHandler(NetworkServer server)
		throws IOException
	{

		//	Open the server socket

		if (hasBindAddress())
			m_srvSock = new DatagramSocket(getPort(), getBindAddres());
		else
			m_srvSock = new DatagramSocket(getPort());

		//	Set the datagram receive buffer size

		if (m_srvSock.getReceiveBufferSize() < getMaximumDatagramSize())
			m_srvSock.setReceiveBufferSize(getMaximumDatagramSize());

		//	Set the allocated port

		if (getPort() == 0)
			setPort(m_srvSock.getLocalPort());

		//	DEBUG

		if (logger.isDebugEnabled())
		{
			String bindAddr = hasBindAddress() ? getBindAddres().getHostAddress() : "ALL";
			logger.debug("[" + getProtocolName() + "] Binding " + getHandlerName() + " session handler to address : " + bindAddr);
		}
	}

	/**
	 * Close the session handler 
	 * 
	 * @param server NetworkServer
	 */
	public void closeSessionHandler(NetworkServer server)
	{

		//	Request the main listener thread shutdown

		m_shutdown = true;

		//	Close the server socket to release any pending listen

		if (m_srvSock != null)
			m_srvSock.close();
	}

	/**
	 * Set the maximum datagram size
	 * 
	 * @param maxSize int
	 */
	protected final void setMaximumDatagramSize(int maxSize)
	{
		m_maxDgramSize = maxSize;
	}

	/**
	 * Process a received datagram packet
	 *
	 * @param pkt DatagramPacket
	 * @return boolean	Return true to reuse the DatagramPacket, else false to allocate a new packet
	 * @exception IOException
	 */
	protected abstract boolean processDatagram(DatagramPacket pkt)
		throws IOException;

	/**
	 * Allocate a buffer for the datagram receive
	 * 
	 * @param bufSize int
	 * @return byte[]
	 */
	protected byte[] allocateBuffer(int bufSize)
	{

		//	Allocate a buffer for the datagram

		return new byte[bufSize];
	}

	/**
	 * Send a datagram
	 * 
	 * @param pkt DatagramPacket
	 * @exception IOException
	 */
	protected void sendDatagram(DatagramPacket pkt)
		throws IOException
	{

		//	Check if the datagram socket is valid

		if (m_srvSock == null)
			throw new IOException("Datagram socket is null");

		//	Default implementation sends the datagram immediately via the datagram socket

		m_srvSock.send(pkt);
	}

	/**
	 * Socket listener thread 
	 */
	public void run()
	{

		try
		{

			//	Set the thread name

			Thread.currentThread().setName(getProtocolName() + "_" + getHandlerName());

			//	Clear the shutdown flag

			clearShutdown();

			//  Debug

			if (logger.isDebugEnabled())
				logger.debug("[" + getProtocolName() + "] Waiting for datagrams ...");

			//  Wait for incoming connection requests

			byte[] buf = null;
			DatagramPacket pkt = null;
			boolean reusePkt = false;

			while (hasShutdown() == false)
			{

				//	Allocate the datagram buffer and packet

				if (reusePkt == false)
				{

					//	Allocate a new datagram packet and buffer

					buf = allocateBuffer(getMaximumDatagramSize());
					if (pkt == null)
					{

						//	Allocate the datagram packet

						pkt = new DatagramPacket(buf, buf.length);
					} else
					{

						//	Re-use the existing datagram packet

						pkt.setData(buf, 0, buf.length);
					}
				} else
				{

					//	Re-use the existing datagram packet and buffer.
					//
					//	Reset to use our buffer as the datagram packet may have been reused to send a response.

					pkt.setData(buf, 0, buf.length);
				}

				//  Wait for an incoming datagram

				m_srvSock.receive(pkt);

				try
				{

					//	Process the datagram packet

					reusePkt = processDatagram(pkt);
				} catch (Exception ex)
				{

					//	Debug

					if (logger.isDebugEnabled())
						logger.debug("[" + getProtocolName() + "] Error processing datagram, " + ex.toString());
				}
			}
		} catch (SocketException ex)
		{

			//	Do not report an error if the server has shutdown, closing the server socket
			//	causes an exception to be thrown.

			if (hasShutdown() == false)
			{
				logger.debug("[" + getProtocolName() + "] Socket error : " + ex.toString());
				logger.debug(ex);
			}
		} catch (Exception ex)
		{

			//	Do not report an error if the server has shutdown, closing the server socket
			//	causes an exception to be thrown.

			if (hasShutdown() == false)
			{
				logger.debug("[" + getProtocolName() + "] Server error : " + ex.toString());
				logger.debug(ex);
			}
		}

		//	Debug

		if (logger.isDebugEnabled())
			logger.debug("[" + getProtocolName() + "] " + getHandlerName() + " session handler closed");
	}
}
