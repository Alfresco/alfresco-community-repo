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
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.oncrpc.portmap.PortMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RPC Network Server Abstract Class
 * 
 * <p>Provides the base class for RPC servers (such as mount and NFS).
 * 
 * @author GKSpencer
 */
public abstract class RpcNetworkServer extends NetworkServer implements RpcProcessor {

	// Debug logging

	protected static final Log logger = LogFactory.getLog("org.alfresco.nfs.protocol");

	/**
	 * Class constructor
	 *
	 * @param name String
	 * @param config ServerConfiguration
	 */
	public RpcNetworkServer(String name, ServerConfiguration config)
	{
		super(name, config);
	}

	/**
	 * Register a port/protocol for the RPC server
	 * 
	 * @param mapping PortMapping
	 * @throws IOException
	 */
	protected final void registerRPCServer(PortMapping mapping)
		throws IOException
	{

		//	Call the main registration method

		PortMapping[] mappings = new PortMapping[1];
		mappings[0] = mapping;

		registerRPCServer(mappings);
	}

	/**
	 * Register a set of ports/protocols for the RPC server
	 * 
	 * @param mappings PortMapping[]
	 * @throws IOException
	 */
	protected final void registerRPCServer(PortMapping[] mappings)
		throws IOException
	{

		//	Connect to the local portmapper service to register the RPC service

		InetAddress localHost = InetAddress.getByName("127.0.0.1");

		TcpRpcClient rpcClient = new TcpRpcClient(localHost, PortMapper.DefaultPort, 512);

		//	Allocate RPC request and response packets

		RpcPacket setPortRpc = new RpcPacket(512);
		RpcPacket rxRpc = new RpcPacket(512);

		//	Loop through the port mappings and register each port with the portmapper service

		for (int i = 0; i < mappings.length; i++)
		{

			//	Build the RPC request header  

			setPortRpc.buildRequestHeader(PortMapper.ProgramId, PortMapper.VersionId, PortMapper.ProcSet, 0, null, 0,
					null);

			//	Pack the request parameters and set the request length

			setPortRpc.packPortMapping(mappings[i]);
			setPortRpc.setLength();

			//	Send the RPC request and receive a response

			rxRpc = rpcClient.sendRPC(setPortRpc, rxRpc);
		}
	}

	/**
	 * Unregister a port/protocol for the RPC server
	 * 
	 * @param mapping PortMapping
	 * @throws IOException
	 */
	protected final void unregisterRPCServer(PortMapping mapping)
		throws IOException
	{

		//	Call the main unregister ports method

		PortMapping[] mappings = new PortMapping[1];
		mappings[0] = mapping;

		unregisterRPCServer(mappings);
	}

	/**
	 * Unregister a set of ports/protocols for the RPC server
	 * 
	 * @param mappings PortMapping[]
	 * @throws IOException
	 */
	protected final void unregisterRPCServer(PortMapping[] mappings)
		throws IOException
	{

		//  Connect to the local portmapper service to unregister the RPC service

		InetAddress localHost = InetAddress.getByName("127.0.0.1");

		TcpRpcClient rpcClient = new TcpRpcClient(localHost, PortMapper.DefaultPort, 512);

		//  Allocate RPC request and response packets

		RpcPacket setPortRpc = new RpcPacket(512);
		RpcPacket rxRpc = new RpcPacket(512);

		//  Loop through the port mappings and unregister each port with the portmapper service

		for (int i = 0; i < mappings.length; i++)
		{

			//  Build the RPC request header  

			setPortRpc.buildRequestHeader(PortMapper.ProgramId, PortMapper.VersionId, PortMapper.ProcUnSet, 0, null, 0,
					null);

			//  Pack the request parameters and set the request length

			setPortRpc.packPortMapping(mappings[i]);
			setPortRpc.setLength();

			//  DEBUG

			if (logger.isDebugEnabled())
				logger.debug("[" + getProtocolName() + "] UnRegister server RPC " + setPortRpc.toString());

			//  Send the RPC request and receive a response

			rxRpc = rpcClient.sendRPC(setPortRpc, rxRpc);

			//  DEBUG

			if (logger.isDebugEnabled())
				logger.debug("[" + getProtocolName() + "] UnRegister response " + rxRpc.toString());
		}
	}

	/**
	 * Start the RPC server
	 */
	public abstract void startServer();

	/**
	 * Shutdown the RPC server
	 * 
	 * @param immediate boolean
	 */
	public abstract void shutdownServer(boolean immediate);

	/**
	 * Process an RPC request
	 * 
	 * @param rpc RpcPacket
	 * @return RpcPacket
	 * @throws IOException
	 */
	public abstract RpcPacket processRpc(RpcPacket rpc)
		throws IOException;
}
