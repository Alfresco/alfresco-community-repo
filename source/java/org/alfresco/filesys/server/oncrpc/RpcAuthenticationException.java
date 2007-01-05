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

/**
 * RPC Authentication Exception Class
 * 
 * @author GKSpencer
 */
public class RpcAuthenticationException extends Exception {

	// Object version id
	
	private static final long serialVersionUID = 7599358351809146330L;

	//	Authentication failure error code
  
	private int m_authError;

	/**
	 * Class constructor
	 * 
	 * @param authError int
	 */
	public RpcAuthenticationException(int authError)
	{
		m_authError = authError;
	}

	/**
	 * Class constructor
	 * 
	 * @param authError int
	 * @param msg String
	 */
	public RpcAuthenticationException(int authError, String msg)
	{
		super(msg);
		m_authError = authError;
	}

	/**
	 * Get the authentication error code
	 * 
	 * @return int
	 */
	public final int getAuthenticationErrorCode()
	{
		return m_authError;
	}
}
