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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default RPC Authenticator Class
 * 
 * <p>RPC authenticator implementation that allows any client to access the RPC servers.
 * 
 * @author GKSpencer
 */
public class DefaultRpcAuthenticator implements RpcAuthenticator {

	// Debug logging

	private static final Log logger = LogFactory.getLog("org.alfresco.nfs.protocol.auth");

	// Authentication types aupported by this implementation

	private int[] _authTypes = { AuthType.Null, AuthType.Unix };

	/**
	 * Authenticate an RPC client and create a unique session id key.
	 * 
	 * @param authType int
	 * @param rpc RpcPacket
	 * @return Object
	 * @throws RpcAuthenticationException
	 */
	public Object authenticateRpcClient(int authType, RpcPacket rpc)
			throws RpcAuthenticationException {

		// Create a unique session key depending on the authentication type

		Object sessKey = null;

		switch (authType) {

		// Null authentication

		case AuthType.Null:
			sessKey = new Integer(rpc.getClientAddress().hashCode());
			break;

		// Unix authentication

		case AuthType.Unix:

			// Get the gid and uid from the credentials data in the request

			rpc.positionAtCredentialsData();
			rpc.skipBytes(4);
			int nameLen = rpc.unpackInt();
			rpc.skipBytes(nameLen);

			int gid = rpc.unpackInt();
			int uid = rpc.unpackInt();

			// Check if the Unix authentication session table is valid

			sessKey = new Long((((long) rpc.getClientAddress().hashCode()) << 32) + (gid << 16) + uid);
			break;
		}

		// Check if the session key is valid, if not then the authentication
		// type is unsupported

		if (sessKey == null)
			throw new RpcAuthenticationException(Rpc.AuthBadCred, "Unsupported auth type, " + authType);

		// DEBUG

		if (logger.isDebugEnabled())
			logger.debug("RpcAuth: RPC from " + rpc.getClientDetails()
					+ ", authType=" + AuthType.getTypeAsString(authType)
					+ ", sessKey=" + sessKey);

		// Return the session key

		return sessKey;
	}

	/**
	 * Return the authentication types that are supported by this
	 * implementation.
	 * 
	 * @return int[]
	 */
	public int[] getRpcAuthenticationTypes() {
		return _authTypes;
	}

	/**
	 * Return the client information for the specified RPC request
	 * 
	 * @param sessKey
	 *            Object
	 * @param rpc
	 *            RpcPacket
	 * @return ClientInfo
	 */
	public ClientInfo getRpcClientInformation(Object sessKey, RpcPacket rpc) {

		// Create a client information object to hold the client details

		ClientInfo cInfo = new ClientInfo("", null);

		// Get the authentication type

		int authType = rpc.getCredentialsType();
		cInfo.setNFSAuthenticationType(authType);

		// Unpack the client details from the RPC request

		switch (authType) {

		// Null authentication

		case AuthType.Null:
			cInfo.setClientAddress(rpc.getClientAddress().getHostAddress());

			// DEBUG

			if (logger.isDebugEnabled())
				logger.debug("RpcAuth: Client info, type=" + AuthType.getTypeAsString(authType) + ", addr="
						+ rpc.getClientAddress().getHostAddress());
			break;

		// Unix authentication

		case AuthType.Unix:

			// Unpack the credentials data

			rpc.positionAtCredentialsData();
			rpc.skipBytes(4); // stamp id

			cInfo.setClientAddress(rpc.unpackString());
			cInfo.setUid(rpc.unpackInt());
			cInfo.setGid(rpc.unpackInt());

			// Check for an additional groups list

			int grpLen = rpc.unpackInt();
			if (grpLen > 0) {
				int[] groups = new int[grpLen];
				rpc.unpackIntArray(groups);

				cInfo.setGroupsList(groups);
			}

			// DEBUG

			if (logger.isDebugEnabled())
				logger.debug("RpcAuth: Client info, type=" + AuthType.getTypeAsString(authType) + ", name="
						+ cInfo.getClientAddress() + ", uid=" + cInfo.getUid() + ", gid=" + cInfo.getGid() + ", groups=" + grpLen);
			break;
		}

		// Return the client information

		return cInfo;
	}

	/**
	 * Initialize the RPC authenticator
	 * 
	 * @param config ServerConfiguration
	 * @param params NameValueList
	 * @throws InvalidConfigurationException
	 */
	public void initialize(ServerConfiguration config, ConfigElement params)
			throws InvalidConfigurationException {
	}
	
    /**
     * Set the current authenticated user context for this thread
     * 
     * @param sess SrvSession
     * @param client ClientInfo
     */
    public void setCurrentUser( SrvSession sess, ClientInfo client)
    {
    }	
}
