/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webdav.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.SessionUser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * WebDAV Kerberos Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class KerberosAuthenticationFilter extends BaseKerberosAuthenticationFilter
{
    // Debug logging
    
    private static Log logger = LogFactory.getLog(KerberosAuthenticationFilter.class);


    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseKerberosAuthenticationFilter#init()
     */
    @Override
    protected void init() throws ServletException
    {
        // Call the base Kerberos filter initialization

        super.init();
    	
    	// Enable ticket based logons
    	
    	setTicketLogons(true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onValidateFailed(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession)
     */
    @Override
    protected void onValidateFailed(HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException
    {
        // Restart the login challenge process if validation fails
    	
        restartLoginChallenge(res, session);
    }
    
	/* (non-Javadoc)
	 * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#getLogger()
	 */
	@Override
	protected Log getLogger() {
		return logger;
	}
}
