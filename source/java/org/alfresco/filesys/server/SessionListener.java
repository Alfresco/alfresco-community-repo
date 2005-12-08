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
 * <p>
 * The session listener interface provides a hook into the server so that an application is notified
 * when a new session is created and closed by a network server.
 */
public interface SessionListener
{

    /**
     * Called when a network session is closed.
     * 
     * @param sess Network session details.
     */
    public void sessionClosed(SrvSession sess);

    /**
     * Called when a new network session is created by a network server.
     * 
     * @param sess Network session that has been created for the new connection.
     */
    public void sessionCreated(SrvSession sess);

    /**
     * Called when a user logs on to a network server
     * 
     * @param sess Network session that has been logged on.
     */
    public void sessionLoggedOn(SrvSession sess);
}