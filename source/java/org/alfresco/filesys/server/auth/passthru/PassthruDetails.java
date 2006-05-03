/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
