/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
}
