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

import org.alfresco.util.Pair;

/**
 * An implementation of {@link RemoteAlfrescoTicketInfo} which authenticates
 *  as the Guest user
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class GuestRemoteAlfrescoTicketImpl extends AbstractRemoteAlfrescoTicketImpl
{
    private static final String GUEST_USERNAME = "guest";
    private static final String GUEST_URL_PARAM = "guest=true";
    
    public GuestRemoteAlfrescoTicketImpl() {}

    /**
     * Returns the Ticket as a URL Parameter fragment, of the form
     *  "guest=true"
     */
    public String getAsUrlParameters()
    {
        return GUEST_URL_PARAM;
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
        return new Pair<String,String>(GUEST_USERNAME, "");
    }
}
