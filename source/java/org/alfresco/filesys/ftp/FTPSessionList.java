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
 * http://www.alfresco.com/legal/licensing"
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
