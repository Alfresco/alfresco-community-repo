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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Server Session List Class
 */
public class SrvSessionList
{

    // Session list

    private Hashtable<Integer, SrvSession> m_sessions;

    /**
     * Class constructor
     */
    public SrvSessionList()
    {
        m_sessions = new Hashtable<Integer, SrvSession>();
    }

    /**
     * Return the number of sessions in the list
     * 
     * @return int
     */
    public final int numberOfSessions()
    {
        return m_sessions.size();
    }

    /**
     * Add a session to the list
     * 
     * @param sess SrvSession
     */
    public final void addSession(SrvSession sess)
    {
        m_sessions.put(sess.getSessionId(), sess);
    }

    /**
     * Find the session using the unique session id
     * 
     * @param id int
     * @return SrvSession
     */
    public final SrvSession findSession(int id)
    {
        return findSession(id);
    }

    /**
     * Find the session using the unique session id
     * 
     * @param id Integer
     * @return SrvSession
     */
    public final SrvSession findSession(Integer id)
    {
        return m_sessions.get(id);
    }

    /**
     * Remove a session from the list
     * 
     * @param id int
     * @return SrvSession
     */
    public final SrvSession removeSession(int id)
    {
        return removeSession(new Integer(id));
    }

    /**
     * Remove a session from the list
     * 
     * @param sess SrvSession
     * @return SrvSession
     */
    public final SrvSession removeSession(SrvSession sess)
    {
        return removeSession(sess.getSessionId());
    }

    /**
     * Remove a session from the list
     * 
     * @param id Integer
     * @return SrvSession
     */
    public final SrvSession removeSession(Integer id)
    {

        // Find the required session

        SrvSession sess = findSession(id);

        // Remove the session and return the removed session

        m_sessions.remove(id);
        return sess;
    }

    /**
     * Enumerate the session ids
     * 
     * @return Enumeration<Integer>
     */
    public final Enumeration<Integer> enumerate()
    {
        return m_sessions.keys();
    }
}
