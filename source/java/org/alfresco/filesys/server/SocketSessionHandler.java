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
 * Socket Session Handler Class
 * 
 * <p>Implementation of a session handler that uses a Java socket to listen for incoming session requests.
 * 
 * @author GKSpencer
 */
public abstract class SocketSessionHandler implements SessionHandlerInterface, Runnable {

	// Debug logging

	protected static final Log logger = LogFactory.getLog(SocketSessionHandler.class);

	//	Constants
	//
	//	Default socket listen back log limit

	public static final int ListenBacklog = 10;

	//	Server that the handler is associated with

	private NetworkServer m_server;

	//	Address/port to use

	private int m_port;
	private InetAddress m_bindAddr;

	//	Socket listen back log limit

	private int m_backLog = ListenBacklog;

	//	Server socket to listen for incoming connections

	private ServerSocket m_srvSock;

	//	Session id

	private int m_sessId;

	//	Session handler name, protocol name

	private String m_name;
	private String m_protocol;

	//	Shutdown request flag

	private boolean m_shutdown;

	//	Debug enable

	private boolean m_debug;

	/**
	 * Class constructor
	 * 
	 * @param name String
	 * @param protocol String
	 * @param server NetworkServer
	 * @param addr InetAddress
	 * @param port int
	 */
	public SocketSessionHandler(String name, String protocol, NetworkServer server, InetAddress addr, int port) {
		m_name = name;
		m_protocol = protocol;
		m_server = server;

		m_bindAddr = addr;
		m_port = port;
	}

	/**
	 * Return the session handler name
	 * 
	 * @return String
	 */
	public final String getHandlerName() {
		return m_name;
	}

	/**
	 * Return the short protocol name
	 * 
	 * @return String
	 */
	public final String getProtocolName() {
		return m_protocol;
	}

	/**
	 * Check if the server should bind to a specific network address
	 * 
	 * @return boolean
	 */
	public final boolean hasBindAddress() {
		return m_bindAddr != null ? true : false;
	}

	/**
	 * Return the network address that the server should bind to
	 * 
	 * @return InetAddress
	 */
	public final InetAddress getBindAddress() {
		return m_bindAddr;
	}

	/**
	 * Return the port that the server should bind to
	 * 
	 * @return int
	 */
	public final int getPort() {
		return m_port;
	}

	/**
	 * Return the socket listen backlog limit
	 * 
	 * @return int
	 */
	public final int getListenBacklog() {
		return m_backLog;
	}

	/**
	 * Determine if debug output is enabled
	 * 
	 * @return boolean
	 */
	public final boolean hasDebug() {
		return m_debug;
	}

	/**
	 * Clear the shutdown flag
	 */
	protected final void clearShutdown() {
		m_shutdown = false;
	}

	/**
	 * Determine if the shutdown flag has been set
	 * 
	 * @return boolean
	 */
	protected final boolean hasShutdown() {
		return m_shutdown;
	}

	/**
	 * Get the next available session id
	 * 
	 * @return int
	 */
	protected synchronized int getNextSessionId() {
		return m_sessId++;
	}

	/**
	 * Enable/disable debug output
	 * 
	 * @param dbg boolean
	 */
	public final void setDebug(boolean dbg) {
		m_debug = dbg;
	}

	/**
	 * Set the local port that the session handler is using
	 * 
	 * @param port int
	 */
	protected final void setPort(int port) {
		m_port = port;
	}

	/**
	 * Initialize the session handler 
	 * 
	 * @param server NetworkServer
	 */
	public void initializeSessionHandler(NetworkServer server) throws IOException {

		//	Open the server socket

		if (hasBindAddress())
			m_srvSock = new ServerSocket(getPort(), getListenBacklog(), getBindAddress());
		else
			m_srvSock = new ServerSocket(getPort(), getListenBacklog());

		//	Set the allocated port

		if (getPort() == 0)
			setPort(m_srvSock.getLocalPort());

		//	DEBUG

		if (logger.isDebugEnabled()) {
			String bindAddr = hasBindAddress() ? getBindAddress().getHostAddress() : "ALL";
			logger.debug("[" + getProtocolName() + "] Binding " + getHandlerName() + " session handler to address : " + bindAddr);
		}
	}

	/**
	 * Close the session handler 
	 * 
	 * @param server NetworkServer
	 */
	public void closeSessionHandler(NetworkServer server) {

		//	Request the main listener thread shutdown

		m_shutdown = true;

		try {

			//	Close the server socket to release any pending listen

			if (m_srvSock != null)
				m_srvSock.close();
		} catch (SocketException ex) {
		} catch (Exception ex) {
		}
	}

	/**
	 * Accept a new connection on the specified socket
	 *
	 * @param sock Socket
	 */
	protected abstract void acceptConnection(Socket sock);

	/**
	 * Socket listener thread 
	 */
	public void run() {

		try {

			//	Clear the shutdown flag

			clearShutdown();

			//  Wait for incoming connection requests

			while (hasShutdown() == false) {

				//  Debug

				if (logger.isDebugEnabled())
					logger.debug("[" + getProtocolName() + "] Waiting for session request ...");

				//  Wait for a connection

				Socket sessSock = m_srvSock.accept();

				//  Debug

				if (logger.isDebugEnabled())
					logger.debug("[" + getProtocolName() + "] Session request received from "
							+ sessSock.getInetAddress().getHostAddress());

				try {

					//	Process the new connection request

					acceptConnection(sessSock);
				}
				catch (Exception ex) {

					//	Debug

					if (logger.isDebugEnabled())
						logger.debug("[" + getProtocolName() + "] Failed to create session, " + ex.toString());
				}
			}
		}
		catch (SocketException ex) {

			//	Do not report an error if the server has shutdown, closing the server socket
			//	causes an exception to be thrown.

			if (hasShutdown() == false) {
				logger.debug("[" + getProtocolName() + "] Socket error : " + ex.toString());
				logger.debug(ex);
			}
		}
		catch (Exception ex) {

			//	Do not report an error if the server has shutdown, closing the server socket
			//	causes an exception to be thrown.

			if (hasShutdown() == false) {
				logger.debug("[" + getProtocolName() + "] Server error : " + ex.toString());
				logger.debug(ex);
			}
		}

		//	Debug

		if (logger.isDebugEnabled())
			logger.debug("[" + getProtocolName() + "] " + getHandlerName() + " session handler closed");
	}
}
