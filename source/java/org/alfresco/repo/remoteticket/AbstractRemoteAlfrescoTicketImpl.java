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

import java.nio.charset.Charset;

import org.alfresco.service.cmr.remoteticket.RemoteAlfrescoTicketInfo;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.springframework.extensions.surf.util.Base64;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Parent class for implementations of {@link RemoteAlfrescoTicketInfo},
 *  which provides common helpers for working with tickets 
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public abstract class AbstractRemoteAlfrescoTicketImpl implements RemoteAlfrescoTicketInfo
{
    protected static final Charset utf8 = Charset.forName("UTF-8");
    
    /**
     * Returns the Ticket as a URL Parameter fragment, such as 
     *  "ticket=123&sig=13". No escaping is done 
     */
    public abstract String getAsUrlParameters();
    
    /**
     * Returns the Ticket as a URL Escaped Parameter fragment, such as 
     *  "ticket=12%20xx&sig=2". Special characters in the URL are escaped 
     *  suitable for using as full URL, but any ampersands are not escaped 
     *  (it's not HTML escaped)  
     */
    public String getAsEscapedUrlParameters()
    {
        String unescaped = getAsUrlParameters();
        return URLEncoder.encodeUri(unescaped);
    }
    
    /**
     * Returns the Ticket in the form used for HTTP Basic Authentication. 
     * This should be added as the value to a HTTP Request Header with 
     *  key Authorization
     */
    public String getAsHTTPAuthorization()
    {
        // Build from the Username and Password
        Pair<String,String> userPass = getAsUsernameAndPassword();
        Credentials credentials = new UsernamePasswordCredentials(userPass.getFirst(), userPass.getSecond());

        // Encode it into the required format
        String credentialsEncoded = Base64.encodeBytes(
                credentials.toString().getBytes(utf8), Base64.DONT_BREAK_LINES );
        
        // Mark it as Basic, and we're done
        return "Basic " + credentialsEncoded;
    }
    
    /**
     * Returns the Ticket in the form of a pseudo username and password. 
     * The Username is normally a special ticket identifier, and the password 
     *  is the ticket in a suitably encoded form. 
     */
    public abstract Pair<String,String> getAsUsernameAndPassword();
}
