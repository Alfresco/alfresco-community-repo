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
package org.alfresco.filesys.server.oncrpc.portmap;

import java.io.*;
import java.util.*;

import org.alfresco.filesys.server.NetworkServer;
import org.alfresco.filesys.server.ServerListener;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.oncrpc.PortMapping;
import org.alfresco.filesys.server.oncrpc.Rpc;
import org.alfresco.filesys.server.oncrpc.RpcPacket;
import org.alfresco.filesys.server.oncrpc.RpcProcessor;
import org.alfresco.filesys.server.oncrpc.TcpRpcSessionHandler;
import org.alfresco.filesys.server.oncrpc.UdpRpcDatagramHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Port Mapper Server Class
 * 
 * @author GKSpencer
 */
public class PortMapperServer extends NetworkServer implements RpcProcessor {

	// Debug logging

	protected static final Log logger = LogFactory.getLog("org.alfresco.nfs.protocol");

	// Constants
	//
	// Default port mapper port

	public final static int DefaultPort = 111;

	// Maximum request size to accept

	public final static int MaxRequestSize = 1024;

	// Incoming datagram handler for UDP requests

	private UdpRpcDatagramHandler m_udpHandler;

	// Incoming session handler for TCP requests

	private TcpRpcSessionHandler m_tcpHandler;

	// Portmapper port

	private int m_port;

	// Table of active port mappings

	private Hashtable<Integer, PortMapping> m_mappings;
	private Hashtable<Integer, PortMapping> m_noVerMappings;

	/**
	 * Class constructor
	 * 
	 * @param config
	 *            ServerConfiguration
	 */
	public PortMapperServer(ServerConfiguration config) {
		super("Portmap", config);

		// Enable/disable debug output

		setDebug(config.hasPortMapperDebug());

		// Set the port to use

		if (config.getPortMapperPort() != 0)
			setPort(config.getPortMapperPort());
		else
			setPort(DefaultPort);

		// Create the mappings tables

		m_mappings = new Hashtable<Integer, PortMapping>();
		m_noVerMappings = new Hashtable<Integer, PortMapping>();
	}

	/**
	 * Return the server port
	 * 
	 * @return int
	 */
	public final int getPort() {
		return m_port;
	}

	/**
	 * Start the portmapper server
	 */
	public void startServer() {

		try {

			// Create the UDP RPC handler to accept incoming requests

			m_udpHandler = new UdpRpcDatagramHandler("PortMap", "Port", this, this, null, getPort(), MaxRequestSize);
			m_udpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread udpThread = new Thread(m_udpHandler);
			udpThread.setName("PortMap_UDP");
			udpThread.start();

			// Create the TCP RPC handler to accept incoming requests

			m_tcpHandler = new TcpRpcSessionHandler("PortMap", "Port", this, this, null, getPort(), MaxRequestSize);
			m_tcpHandler.initializeSessionHandler(this);

			// Start the UDP request listener is a seperate thread

			Thread tcpThread = new Thread(m_tcpHandler);
			tcpThread.setName("PortMap_TCP");
			tcpThread.start();

			// Add port mapper entries for the portmapper service

			PortMapping portMap = new PortMapping(PortMapper.ProgramId, PortMapper.VersionId, Rpc.UDP, getPort());
			addPortMapping(portMap);

			portMap = new PortMapping(PortMapper.ProgramId, PortMapper.VersionId, Rpc.TCP, getPort());
			addPortMapping(portMap);
		}
		catch (Exception ex) {
			logger.debug(ex);
		}
	}

	/**
	 * Shutdown the server
	 * 
	 * @param immediate
	 *            boolean
	 */
	public void shutdownServer(boolean immediate) {

		// Stop the RPC handlers

		if (m_udpHandler != null) {
			m_udpHandler.closeSessionHandler(this);
			m_udpHandler = null;
		}

		if (m_tcpHandler != null) {
			m_tcpHandler.closeSessionHandler(this);
			m_tcpHandler = null;
		}

		// Fire a shutdown notification event

		fireServerEvent(ServerListener.ServerShutdown);
	}

	/**
	 * Set the server port
	 * 
	 * @param port
	 *            int
	 */
	public final void setPort(int port) {
		m_port = port;
	}

	/**
	 * Process an RPC request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 * @throws IOException
	 */
	public RpcPacket processRpc(RpcPacket rpc) throws IOException {

		// Validate the request

		if (rpc.getProgramId() != PortMapper.ProgramId) {

			// Request is not for us

			rpc.buildAcceptErrorResponse(Rpc.StsProgUnavail);
			return rpc;
		} else if (rpc.getProgramVersion() != PortMapper.VersionId) {

			// Request is not for this version of portmapper

			rpc.buildProgramMismatchResponse(PortMapper.VersionId,
					PortMapper.VersionId);
			return rpc;
		}

		// Position the RPC buffer pointer at the start of the call parameters

		rpc.positionAtParameters();

		// Process the RPC request

		RpcPacket response = null;

		switch (rpc.getProcedureId()) {

		// Null request

		case PortMapper.ProcNull:
			response = procNull(rpc);
			break;

		// Set a port

		case PortMapper.ProcSet:
			response = procSet(rpc);
			break;

		// Release a port

		case PortMapper.ProcUnSet:
			response = procUnSet(rpc);
			break;

		// Get the port for a service

		case PortMapper.ProcGetPort:
			response = procGetPort(rpc);
			break;

		// Dump ports request

		case PortMapper.ProcDump:
			response = procDump(rpc);
			break;
		}

		// Return the RPC response

		return response;
	}

	/**
	 * Process the null request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procNull(RpcPacket rpc) {

		// Build the response

		rpc.buildResponseHeader();
		return rpc;
	}

	/**
	 * Process the set request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procSet(RpcPacket rpc) {

		// Get the call parameters

		int progId = rpc.unpackInt();
		int verId = rpc.unpackInt();
		int proto = rpc.unpackInt();
		int port = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[PortMap] Set port program=" + Rpc.getServiceName(progId) + ", version=" + verId
					+ ", protocol=" + (proto == Rpc.TCP ? "TCP" : "UDP") + ", port=" + port);

		// Check if the port is already mapped

		PortMapping portMap = findPortMapping(progId, verId, proto);
		int portAdded = Rpc.False;

		if (portMap == null) {

			// Add a mapping for the new service

			portMap = new PortMapping(progId, verId, proto, port);
			if (addPortMapping(portMap) == true)
				portAdded = Rpc.True;
		}

		// Check if the service is on the same port as the current port mapping,
		// and it is not
		// an attempt to set the port mapper service port.

		else if (progId != PortMapper.ProgramId && portMap.getPort() == port) {

			// Settings are the same as the existing service settings so accept
			// it

			portAdded = Rpc.True;
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Pack a boolean indicating if the port was added, or not

		rpc.packInt(portAdded);
		rpc.setLength();

		// Return the response

		return rpc;
	}

	/**
	 * Process the unset request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procUnSet(RpcPacket rpc) {

		// Get the call parameters

		int progId = rpc.unpackInt();
		int verId = rpc.unpackInt();
		int proto = rpc.unpackInt();
		int port = rpc.unpackInt();

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[PortMap] UnSet port program=" + Rpc.getServiceName(progId) + ", version=" + verId
					+ ", protocol=" + (proto == Rpc.TCP ? "TCP" : "UDP") + ", port=" + port);

		// Check if the port is mapped, and it is not an attempt to remove a
		// portmapper portt

		PortMapping portMap = findPortMapping(progId, verId, proto);
		int portRemoved = Rpc.False;

		if (portMap != null && progId != PortMapper.ProgramId) {

			// Remove the port mapping

			if (removePortMapping(portMap) == true)
				portRemoved = Rpc.True;
		}

		// Build the response header

		rpc.buildResponseHeader();

		// Pack a boolean indicating if the port was removed, or not

		rpc.packInt(portRemoved);
		rpc.setLength();

		// Return the response

		return rpc;
	}

	/**
	 * Process the get port request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procGetPort(RpcPacket rpc) {

		// Get the call parameters

		int progId = rpc.unpackInt();
		int verId = rpc.unpackInt();
		int proto = rpc.unpackInt();

		// Find the required port mapping

		PortMapping portMap = findPortMapping(progId, verId, proto);

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[PortMap] Get port program=" + Rpc.getServiceName(progId) + ", version=" + verId
					+ ", protocol=" + (proto == Rpc.TCP ? "TCP" : "UDP") + ", port=" + (portMap != null ? portMap.getPort() : 0));

		// Build the response header

		rpc.buildResponseHeader();

		// Pack the port number of the requested RPC service, or zero if not
		// found

		rpc.packInt(portMap != null ? portMap.getPort() : 0);
		rpc.setLength();

		// Return the response

		return rpc;
	}

	/**
	 * Process the dump request
	 * 
	 * @param rpc
	 *            RpcPacket
	 * @return RpcPacket
	 */
	private final RpcPacket procDump(RpcPacket rpc) {

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("[PortMap] Dump ports request from " + rpc.getClientDetails());

		// Build the response

		rpc.buildResponseHeader();

		// Pack the active port mappings structures

		Enumeration enm = m_mappings.elements();

		while (enm.hasMoreElements()) {

			// Get the current port mapping

			PortMapping portMap = (PortMapping) enm.nextElement();

			// Pack the port mapping structure

			rpc.packInt(Rpc.True);
			rpc.packPortMapping(portMap);
		}

		// Pack the end of list structure, set the response length

		rpc.packInt(Rpc.False);
		rpc.setLength();

		// Return the response

		return rpc;
	}

	/**
	 * Add a port mapping to the active list
	 * 
	 * @param portMap
	 *            PortMapping
	 * @return boolean
	 */
	private final boolean addPortMapping(PortMapping portMap) {

		// Check if there is an existing port mapping that matches the new port

		Integer key = new Integer(portMap.hashCode());
		if (m_mappings.get(key) != null)
			return false;

		// Add the port mapping

		m_mappings.put(key, portMap);

		// Add a port mapping with a version id of zero

		key = new Integer(PortMapping.generateHashCode(portMap.getProgramId(), 0, portMap.getProtocol()));
		m_noVerMappings.put(key, portMap);

		// Indicate that the mapping was added

		return true;
	}

	/**
	 * Remove a port mapping from the active list
	 * 
	 * @param portMap
	 *            PortMapping
	 * @return boolean
	 */
	private final boolean removePortMapping(PortMapping portMap) {

		// Remove the port mapping from the active lists

		Integer key = new Integer(portMap.hashCode());
		Object removedObj = m_mappings.remove(key);

		key = new Integer(PortMapping.generateHashCode(portMap.getProgramId(), 0, portMap.getProtocol()));
		m_noVerMappings.remove(key);

		// Return a status indicating if the mapping was removed

		return removedObj != null ? true : false;
	}

	/**
	 * Search for a port mapping
	 * 
	 * @param progId
	 *            int
	 * @param verId
	 *            int
	 * @param proto
	 *            int
	 * @return PortMapping
	 */
	private final PortMapping findPortMapping(int progId, int verId, int proto) {

		// Create a key for the RPC service

		Integer key = new Integer(PortMapping.generateHashCode(progId, verId,
				proto));

		// Search for the required port mapping, including the version id

		PortMapping portMap = (PortMapping) m_mappings.get(key);
		if (portMap == null && verId == 0) {

			// Search for the port mapping without the version id

			portMap = (PortMapping) m_noVerMappings.get(key);
		}

		// Return the port mapping, or null if not found

		return portMap;
	}
}
