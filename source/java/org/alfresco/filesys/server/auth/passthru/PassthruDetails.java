/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.filesys.server.auth.passthru;

import org.alfresco.filesys.server.SrvSession;

/**
 * Passthru Details Class
 * <p>
 * Contains the details of a passthru connection to a remote server and the local session that the
 * request originated from.
 */
class PassthruDetails
{
    // Server session

    private SrvSession m_sess;

    // Authentication session connected to the remote server

    private AuthenticateSession m_authSess;

    // Flag to indicate if session should be kept alive
    
    private boolean m_keepAlive;
    
    /**
     * Class constructor
     * 
     * @param sess SrvSession
     * @param authSess AuthenticateSession
     */
    public PassthruDetails(SrvSession sess, AuthenticateSession authSess)
    {
        m_sess = sess;
        m_authSess = authSess;
    }

    /**
     * Class constructor
     * 
     * @param sess SrvSession
     * @param authSess AuthenticateSession
     * @param keepAlive boolean
     */
    public PassthruDetails(SrvSession sess, AuthenticateSession authSess, boolean keepAlive)
    {
        m_sess = sess;
        m_authSess = authSess;
        
        m_keepAlive = keepAlive;
    }

    /**
     * Return the session details
     * 
     * @return SrvSession
     */
    public final SrvSession getSession()
    {
        return m_sess;
    }

    /**
     * Return the authentication session that is connected to the remote server
     * 
     * @return AuthenticateSession
     */
    public final AuthenticateSession getAuthenticateSession()
    {
        return m_authSess;
    }
    
    /**
     * Check if the authentication session should be kept alive
     * 
     * @return boolean
     */
    public final boolean hasKeepAlive()
    {
    	return m_keepAlive;
    }
}
