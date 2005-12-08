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

/**
 * Server Listener Interface
 * <p>
 * The server listener allows external components to receive notification of server startup,
 * shutdown and error events.
 */
public interface ServerListener
{
    // Server event types

    public static final int ServerStartup = 0;
    public static final int ServerActive = 1;
    public static final int ServerShutdown = 2;
    public static final int ServerError = 3;

    /**
     * Receive a server event notification
     * 
     * @param server NetworkServer
     * @param event int
     */
    public void serverStatusEvent(NetworkServer server, int event);
}
