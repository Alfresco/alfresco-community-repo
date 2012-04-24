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
package org.alfresco.service.cmr.remoteticket;

import org.alfresco.util.Pair;

/**
 * Holds details on a Ticket from a Remote Alfresco System,
 *  and provides ways to get it into different forms
 *  suitable for sending back to the Remote System.
 * 
 * Currently, only regular Tickets are supported, but this
 *  is designed to handle things like OAuth later
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public interface RemoteAlfrescoTicketInfo
{
    /**
     * Returns the Ticket as a URL Parameter fragment, such as 
     *  "ticket=123&sig=13". No escaping is done 
     */
    String getAsUrlParameters();
    
    /**
     * Returns the Ticket as a URL Escaped Parameter fragment, such as 
     *  "ticket=12%20xx&sig=2". Special characters in the URL are escaped 
     *  suitable for using as full URL, but any ampersands are not escaped 
     *  (it's not HTML escaped)  
     */
    String getAsEscapedUrlParameters();
    
    /**
     * Returns the Ticket in the form used for HTTP Basic Authentication. 
     * This should be added as the value to a HTTP Request Header with 
     *  key Authorization
     */
    String getAsHTTPAuthorization();
    
    /**
     * Returns the Ticket in the form of a pseudo username and password. 
     * The Username is normally a special ticket identifier, and the password 
     *  is the ticket in a suitably encoded form. 
     */
    Pair<String,String> getAsUsernameAndPassword();
}
