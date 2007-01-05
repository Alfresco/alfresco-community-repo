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
