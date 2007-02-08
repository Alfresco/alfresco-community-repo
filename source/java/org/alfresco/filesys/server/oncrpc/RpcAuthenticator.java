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

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;

/**
 * RPC Authenticator Interface
 * 
 * <p>Provides authentication support for ONC/RPC requests.
 * 
 * @author GKSpencer
 */
public interface RpcAuthenticator
{
	/**
	 * Initialize the RPC authenticator
	 * 
	 * @param config ServerConfiguration
	 * @param params NameValueList
	 * @exception InvalidConfigurationException
	 */
	public void initialize(ServerConfiguration config, ConfigElement params)
		throws InvalidConfigurationException;

	/**
	 * Authenticate an RPC client using the credentials within the RPC request.
	 * The object that is returned is used as the key to find the associated
	 * session object.
	 * 
	 * @param authType int
	 * @param rpc RpcPacket
	 * @return Object
	 * @exception RpcAuthenticationException
	 */
	public Object authenticateRpcClient(int authType, RpcPacket rpc)
		throws RpcAuthenticationException;

	/**
	 * Get RPC client information from the RPC request.
	 * 
	 * <p>
	 * This method is called when a new session object is created by an RPC
	 * server.
	 * 
	 * @param sessKey Object
	 * @param rpc RpcPacket
	 * @return ClientInfo
	 */
	public ClientInfo getRpcClientInformation(Object sessKey, RpcPacket rpc);

	/**
	 * Return a list of the authentication types that the RPC authenticator
	 * implementation supports. The authentication types are specified in the
	 * AuthType class.
	 * 
	 * @return int[]
	 */
	public int[] getRpcAuthenticationTypes();
	
    /**
     * Set the current authenticated user context for this thread
     * 
     * @param sess SrvSession
     * @param client ClientInfo
     */
    public void setCurrentUser( SrvSession sess, ClientInfo client);
}
