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

/**
 * Session Handler Interface
 * 
 * <p>Implemented by classes that wait for an incoming session request.
 * 
 * @author GKSpencer
 */
public interface SessionHandlerInterface
{
	/**
	 * Return the protocol name
	 * 
	 * @return String
	 */
	public String getHandlerName();

	/**
	 * Initialize the session handler
	 * 
	 * @param server
	 *            NetworkServer
	 * @exception IOException
	 */
	public void initializeSessionHandler(NetworkServer server)
		throws IOException;

	/**
	 * Close the session handler
	 */
	public void closeSessionHandler(NetworkServer server);
}
