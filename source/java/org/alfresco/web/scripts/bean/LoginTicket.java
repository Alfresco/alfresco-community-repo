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
package org.alfresco.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptStatus;


/**
 * Login Ticket
 * 
 * @author davidc
 */
public class LoginTicket extends DeclarativeWebScript
{
    // dependencies
    private TicketComponent ticketComponent;
    
    /**
     * @param ticketComponent
     */
    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        // retrieve ticket from request and current ticket
        String ticket = req.getExtensionPath();
        if (ticket == null && ticket.length() == 0)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Ticket not specified");
        }
        
        // construct model for ticket
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("ticket",  ticket);

        try
        {
            String ticketUser = ticketComponent.validateTicket(ticket);

            // do not go any further if tickets are different
            if (!AuthenticationUtil.getCurrentUserName().equals(ticketUser))
            {
                status.setRedirect(true);
                status.setCode(HttpServletResponse.SC_NOT_FOUND);
                status.setMessage("Ticket not found");
            }
        }
        catch(AuthenticationException e)
        {
            status.setRedirect(true);
            status.setCode(HttpServletResponse.SC_NOT_FOUND);
            status.setMessage("Ticket not found");
        }
        
        return model;
    }

}
