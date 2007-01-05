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
import java.util.*;

import org.alfresco.filesys.server.NetworkServer;
import org.alfresco.filesys.server.PacketHandlerInterface;
import org.alfresco.filesys.server.SocketSessionHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TCP RPC Session Handler Class
 * 
 * <p>Receives session requests via a TCP socketRPC requests via a datagram and passes the request to the registered RPC server.
 * 
 * @author GKSpencer
 */
public class TcpRpcSessionHandler extends SocketSessionHandler {

	// Debug logging

	private static final Log logger = LogFactory.getLog(TcpRpcSessionHandler.class);
	
	//	RPC server implementation that handles the RPC processing

	private RpcProcessor m_rpcProcessor;

	//	Maximum request size allowed

	private int m_maxRpcSize;

	//	List of active sessions

	private Hashtable m_sessions;

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
	public TcpRpcSessionHandler(String name, String protocol, RpcProcessor rpcServer, NetworkServer server,
			InetAddress addr, int port, int maxSize)
	{
		super(name, protocol, server, addr, port);

		//	Set the RPC server implementation that will handle the actual requests

		m_rpcProcessor = rpcServer;

		//	Set the maximum RPC request size allowed

		m_maxRpcSize = maxSize;

		//	Create the active session list

		m_sessions = new Hashtable();
	}

	/**
	 * Return the maximum RPC size allowed
	 * 
	 * @return int
	 */
	protected int getMaximumRpcSize()
	{
		return m_maxRpcSize;
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
	 * Accept an incoming session request
	 * 
	 * @param sock Socket
	 */
	protected void acceptConnection(Socket sock)
	{

		try
		{

			//	Set the socket for no delay

			sock.setTcpNoDelay(true);

			//	Create a packet handler for the new session and add to the active session list

			int sessId = getNextSessionId();
			TcpRpcPacketHandler pktHandler = createPacketHandler(sessId, sock);

			//	Add the packet handler to the active session table

			m_sessions.put(new Integer(sessId), pktHandler);

			//	DEBUG

			if (logger.isDebugEnabled())
				logger.debug("[" + getProtocolName() + "] Created new session id = " + sessId + ", from = "
						+ sock.getInetAddress().getHostAddress() + ":" + sock.getPort());
		} catch (IOException ex)
		{
		}
	}

	/**
	 * Remove a session from the active session list
	 * 
	 * @param sessId int
	 */
	protected final void closeSession(int sessId)
	{

		//	Remove the specified session from the active session table

		PacketHandlerInterface pktHandler = (PacketHandlerInterface) m_sessions.remove(new Integer(sessId));
		if (pktHandler != null)
		{

			//	Close the session

			pktHandler.closePacketHandler();
		}
	}

	/**
	 * Close the session handler, close all active sessions.
	 * 
	 * @param server NetworkServer
	 */
	public void closeSessionHandler(NetworkServer server)
	{
		super.closeSessionHandler(server);

		//	Close all active sessions

		if (m_sessions.size() > 0)
		{

			//	Enumerate the sessions

			Enumeration enm = m_sessions.elements();

			while (enm.hasMoreElements())
			{

				//	Get the current packet handler

				PacketHandlerInterface handler = (PacketHandlerInterface) enm.nextElement();
				handler.closePacketHandler();
			}

			//	Clear the session list

			m_sessions.clear();
		}
	}

	/**
	 * Create a packet handler for a new session
	 * 
	 * @param sessId int
	 * @param sock Socket
	 * @return TcpRpcPacketHandler
	 * @exception IOException
	 */
	protected TcpRpcPacketHandler createPacketHandler(int sessId, Socket sock)
		throws IOException
	{

		//	Create a single threaded TCP RPC packet handler

		return new TcpRpcPacketHandler(this, sessId, m_rpcProcessor, sock, getMaximumRpcSize());
	}
}
