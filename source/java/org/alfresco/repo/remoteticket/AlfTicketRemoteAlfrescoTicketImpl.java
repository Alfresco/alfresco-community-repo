/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.remoteticket;

import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.util.Pair;

/**
 * An implementation of {@link RemoteAlfrescoTicketInfo} which works
 *  with the regular Alfresco alf_ticket ticket system
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class AlfTicketRemoteAlfrescoTicketImpl extends AbstractRemoteAlfrescoTicketImpl
{
    private static final String TICKET_USERNAME = Authorization.TICKET_USERID;
    private static final String TICKET_URL_PARAM = "alf_ticket";
    
    private final String ticket;

    public AlfTicketRemoteAlfrescoTicketImpl(String ticket)
    {
        this.ticket = ticket;
    }

    /**
     * Returns the Ticket as a URL Parameter fragment, of the form
     *  "alf_ticket=XXXX"
     */
    public String getAsUrlParameters()
    {
        return TICKET_URL_PARAM + "=" + ticket;
    }
    
    /**
     * Returns the Ticket as a URL Escaped Parameter fragment, which is the
     *  same as the un-escaped due to the format of Alfresco Tickets
     */
    public String getAsEscapedUrlParameters()
    {
        return getAsUrlParameters();
    }
    
    /**
     * Returns the Ticket in the form of a pseudo username and password. 
     * The Username is a special ticket identifier, and the password
     *  is the ticket
     */
    public Pair<String,String> getAsUsernameAndPassword()
    {
        return new Pair<String,String>(TICKET_USERNAME, ticket);
    }
    
    public String toString()
    {
        return "Remote Alfresco Ticket: " + ticket;
    }
}
