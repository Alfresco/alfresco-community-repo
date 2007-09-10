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

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import net.sf.acegisecurity.Authentication;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static final String ERR_NO_CONNECTION = "content.http_reader.err.no_connection";
    private static final String ERR_NO_AUTHENTICATION = "content.http_reader.err.no_authentication";
    private static final String ERR_CHECK_CLUSTER = "content.http_reader.err.check_cluster";
    private static final String ERR_UNRECOGNIZED = "content.http_reader.err.unrecognized";

    private static final String DEFAULT_URL  = "{0}/dr?contentUrl={1}&ticket={2}";
    private static final String INFO_ONLY = "&infoOnly=true";
    
    private static Log logger = LogFactory.getLog(HttpAlfrescoContentReader.class);

    private TransactionService transactionService;
    private AuthenticationService authenticationService;
    private String baseHttpUrl;
    // Helpers
    private HttpClient httpClient;
    private PropagateTicketCallback ticketCallback;
    // Cached values
    private boolean isInfoCached;
    private boolean cachedExists;
    private long cachedLastModified;
    private long cachedSize;
    
    public HttpAlfrescoContentReader(
            TransactionService transactionService,
            AuthenticationService authenticationService,
            String baseHttpUrl,
            String contentUrl)
    {
        super(contentUrl);
        this.transactionService = transactionService;
        this.authenticationService = authenticationService;
        this.baseHttpUrl = baseHttpUrl;
        // Helpers
        this.httpClient = new HttpClient();
        this.ticketCallback = new PropagateTicketCallback();
        // A trip to the remote server has not been made
        cachedExists = false;
        cachedSize = 0L;
        cachedLastModified = 0L;
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
    
    /**
     * Helper class to wrap the ticket creation in order to force propagation of the
     * authentication ticket around the cluster.
     * 
     * @since 2.1
     * @author Derek Hulley
     */
    private class PropagateTicketCallback implements RetryingTransactionCallback<String>
    {
        public String execute() throws Throwable
        {
            return authenticationService.getCurrentTicket();
        }
    }
    
    private void getInfo()
    {
        String contentUrl = getContentUrl();
        // Info will be cached
        isInfoCached = true;
        // Authenticate as the system user for the call
        Authentication authentication = null;
        GetMethod method = null;
        try
        {
            authentication = AuthenticationUtil.setCurrentUser(AuthenticationUtil.SYSTEM_USER_NAME);
            String ticket = transactionService.getRetryingTransactionHelper().doInTransaction(ticketCallback, false, true);
            String url = HttpAlfrescoContentReader.generateURL(baseHttpUrl, contentUrl, ticket, true);

            method = new GetMethod(url);
            int statusCode = httpClient.executeMethod(method);
            if (statusCode == HttpServletResponse.SC_OK)
            {
                // Get the information values from the request
                String responseSize = method.getResponseHeader("alfresco.dr.size").getValue();
                String responseLastModified = method.getResponseHeader("alfresco.dr.lastModified").getValue();
                String responseMimetype = method.getResponseHeader("alfresco.dr.mimetype").getValue();
                String responseEncoding = method.getResponseHeader("alfresco.dr.encoding").getValue();
                String responseLocale = method.getResponseHeader("alfresco.dr.locale").getValue();
                // Fill in this reader's values
                cachedSize = DefaultTypeConverter.INSTANCE.convert(Long.class, responseSize);
                cachedLastModified = DefaultTypeConverter.INSTANCE.convert(Date.class, responseLastModified).getTime();
                setMimetype(DefaultTypeConverter.INSTANCE.convert(String.class, responseMimetype));
                setEncoding(DefaultTypeConverter.INSTANCE.convert(String.class, responseEncoding));
                setLocale(DefaultTypeConverter.INSTANCE.convert(Locale.class, responseLocale));
                // It exists
                cachedExists = true;
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "HttpReader content found: \n" +
                            "   Reader: " + this + "\n" +
                            "   Server: " + baseHttpUrl);
                }
            }
            else
            {
                // Check the return codes
                if (statusCode == HttpServletResponse.SC_NOT_FOUND)
                {
                    // It doesn't exist, which is not an error.  The defaults are fine.
                }
                else if (statusCode == HttpServletResponse.SC_FORBIDDEN)
                {
                    // If the authentication fails, then the server is there, but probably not
                    // clustered correctly.
                    logger.error(I18NUtil.getMessage(ERR_NO_AUTHENTICATION, baseHttpUrl));
                    logger.error(I18NUtil.getMessage(ERR_CHECK_CLUSTER));
                }
                else
                {
                    // What is this?
                    logger.error(I18NUtil.getMessage(ERR_UNRECOGNIZED, baseHttpUrl, contentUrl, statusCode));
                    logger.error(I18NUtil.getMessage(ERR_CHECK_CLUSTER));
                }
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "HttpReader content not found: \n" +
                            "   Reader: " + this + "\n" +
                            "   Server: " + baseHttpUrl);
                }
            }
        }
        catch (ConnectException e)
        {
            logger.error(I18NUtil.getMessage(ERR_NO_CONNECTION, baseHttpUrl));
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Reader exists check failed: " + this, e);
        }
        finally
        {
            if (method != null)
            {
                try { method.releaseConnection(); } catch (Throwable e) {}
            }
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    public synchronized boolean exists()
    {
        if (!isInfoCached)
        {
            getInfo();
        }
        return cachedExists;
    }
    public synchronized long getLastModified()
    {
        if (!isInfoCached)
        {
            getInfo();
        }
        return cachedLastModified;
    }

    public synchronized long getSize()
    {
        if (!isInfoCached)
        {
            getInfo();
        }
        return cachedSize;
    }
    
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new HttpAlfrescoContentReader(transactionService, authenticationService, baseHttpUrl, getContentUrl());
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        String contentUrl = getContentUrl();
        
        Authentication authentication = null;
        try
        {
            if (!exists())
            {
                throw new IOException("Content doesn't exist");
            }
            authentication = AuthenticationUtil.setCurrentUser(AuthenticationUtil.SYSTEM_USER_NAME);
            String ticket = transactionService.getRetryingTransactionHelper().doInTransaction(ticketCallback, false, true);
            String url = HttpAlfrescoContentReader.generateURL(baseHttpUrl, contentUrl, ticket, false);

            GetMethod method = new GetMethod(url);
            int statusCode = httpClient.executeMethod(method);
            if (statusCode == HttpServletResponse.SC_OK)
            {
                // Get the stream from the request
                InputStream contentStream = method.getResponseBodyAsStream();
                // Attach a listener to the stream to ensure that the HTTP request is cleaned up
                this.addListener(new StreamCloseListener(method));
                // Done
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "HttpReader retrieve intput stream: \n" +
                            "   Reader: " + this + "\n" +
                            "   Server: " + baseHttpUrl);
                }
                return Channels.newChannel(contentStream);
            }
            else
            {
                // The content exists, but we failed to get it
                throw new IOException("Failed to get content remote content that supposedly exists.");
            }
        }
        catch (Throwable e)
        {
            throw new ContentIOException(
                    "Failed to open stream: \n" +
                    "   Reader:        " + this + "\n" +
                    "   Remote server: " + baseHttpUrl,
                    e);
        }
        finally
        {
            AuthenticationUtil.setCurrentAuthentication(authentication);
        }
    }
    
    /**
     * A listener to ensure that the HTTP method gets closed when the content stream it
     * is serving to the reader is closed.
     * 
     * @since 2.1
     * @author Derek Hulley
     */
    private static class StreamCloseListener implements ContentStreamListener
    {
        private GetMethod getMethod;
        private StreamCloseListener(GetMethod getMethod)
        {
            this.getMethod = getMethod;
        }
        public void contentStreamClosed() throws ContentIOException
        {
            try { getMethod.releaseConnection(); } catch (Throwable e) {}
        }
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
