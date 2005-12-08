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
package org.alfresco.filesys.ftp;

import java.util.*;

/**
 * FTP Server Session List Class
 * 
 * @author GKSpencer
 */
public class FTPSessionList
{

    // Session list

    private Hashtable<Integer, FTPSrvSession> m_sessions;

    /**
     * Class constructor
     */
    public FTPSessionList()
    {
        m_sessions = new Hashtable<Integer, FTPSrvSession>();
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
     * @param sess FTPSrvSession
     */
    public final void addSession(FTPSrvSession sess)
    {
        m_sessions.put(new Integer(sess.getSessionId()), sess);
    }

    /**
     * Find the session using the unique session id
     * 
     * @param id int
     * @return FTPSrvSession
     */
    public final FTPSrvSession findSession(int id)
    {
        return findSession(new Integer(id));
    }

    /**
     * Find the session using the unique session id
     * 
     * @param id Integer
     * @return FTPSrvSession
     */
    public final FTPSrvSession findSession(Integer id)
    {
        return m_sessions.get(id);
    }

    /**
     * Remove a session from the list
     * 
     * @param id int
     * @return FTPSrvSession
     */
    public final FTPSrvSession removeSession(int id)
    {
        return removeSession(new Integer(id));
    }

    /**
     * Remove a session from the list
     * 
     * @param sess FTPSrvSession
     * @return FTPSrvSession
     */
    public final FTPSrvSession removeSession(FTPSrvSession sess)
    {
        return removeSession(sess.getSessionId());
    }

    /**
     * Remove a session from the list
     * 
     * @param id Integer
     * @return FTPSrvSession
     */
    public final FTPSrvSession removeSession(Integer id)
    {

        // Find the required session

        FTPSrvSession sess = findSession(id);

        // Remove the session and return the removed session

        m_sessions.remove(id);
        return sess;
    }

    /**
     * Enumerate the session ids
     * 
     * @return Enumeration
     */
    public final Enumeration enumerate()
    {
        return m_sessions.keys();
    }
}
