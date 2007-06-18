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
package org.alfresco.repo.content.http;

import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;

import net.sf.acegisecurity.Authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * The reader that does the actual communication with the Alfresco HTTP
 * application.
 * 
 * @see HttpAlfrescoStore
 * @since 2.1
 * @author Derek Hulley
 */
public class HttpAlfrescoContentReader extends AbstractContentReader
{
    private static final String DEFAULT_URL  = "{0}/dr?contentUrl={1}?ticket={2}";
    private static final String INFO_ONLY = "?infoOnly=true";

    AuthenticationService authenticationService;
    AuthenticationComponent authenticationComponent;
    private String baseHttpUrl;
    private String contentUrl;
    // Helpers
    private HttpClient httpClient;
    
    public HttpAlfrescoContentReader(
            AuthenticationService authenticationService,
            AuthenticationComponent authenticationComponent,
            String baseHttpUrl,
            String contentUrl)
    {
        super(contentUrl);
        this.authenticationService = authenticationService;
        this.authenticationComponent = authenticationComponent;
        this.baseHttpUrl = baseHttpUrl;
        this.contentUrl = contentUrl;
        // Helpers
        this.httpClient = new HttpClient();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append("HttpAlfrescoContentReader")
          .append("[ contentUrl=").append(getContentUrl())
          .append("]");
        return sb.toString();
    }
    
    public boolean exists()
    {
        // Authenticate as the system user for the call
        Authentication authentication = null;
        GetMethod method = null;
        try
        {
            authenticationComponent.setSystemUserAsCurrentUser();
            String ticket = authenticationService.getCurrentTicket();
            String url = HttpAlfrescoContentReader.generateURL(baseHttpUrl, contentUrl, ticket, true);

            method = new GetMethod(url);
            int statusCode = httpClient.executeMethod(method);
            if (statusCode == HttpServletResponse.SC_NO_CONTENT)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Reader exists check failed: " + this);
        }
        finally
        {
            if (method != null)
            {
                try { method.releaseConnection(); } catch (Throwable e) {}
            }
            authenticationComponent.setCurrentAuthentication(authentication);
        }
    }

    public long getLastModified()
    {
        throw new UnsupportedOperationException();
    }

    public long getSize()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Helper to generate a URL based on the ContentStore URL and ticket.
     * 
     * @param baseHttpUrl   the first part of the URL pointing to the Alfresoc Web Application
     * @param contentUrl    the content URL - never null
     * @param ticket        the authentication ticket
     * @param infoOnly      <tt>true</tt> to add the info-only flag
     * 
     * @return              Returns the URL with which to access the servlet
     */
    public final static String generateURL(String baseHttpUrl, String contentUrl, String ticket, boolean infoOnly)
    {
       String url = MessageFormat.format(
             DEFAULT_URL,
             baseHttpUrl, contentUrl, ticket);
       if (infoOnly)
       {
           url += INFO_ONLY;
       }
       return url;
    }
}
