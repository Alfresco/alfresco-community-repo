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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A serlet that provides direct access to the content stores via a content URL.
 * <p>
 * Requests can be of the form:<br/>
 * <pre>
 * /alfresco/downloadDirect?contentUrl=some-url?ticket=auth
 *    <b>some-url</b> is a ContentStore-specific URL 
 *    <b>auth</b> is a valid authentication token for an admin user 
 * </pre>
 * This serlet is intended to be accessed to retrieve the binary content from
 * the content stores.  If you wish to retrieve content from a client, use
 * the other content download servlets available.
 * <p>
 * The following responses are generated:
 * <ul>
 *   <li><b>Contet not found:</b> 204 NO CONTENT</li>
 *   <li><b>Access denied:</b>    403 FORBIDDEN</li>
 * </ul>
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class DownloadRawContentServlet extends BaseServlet
{
   private static final long serialVersionUID = 2973080032044411358L;

   private static Log logger = LogFactory.getLog(DownloadRawContentServlet.class);
    
   private static final String DEFAULT_URL  = "/downloadDirect?contentUrl={0}?ticket=?";
   private static final String DEFAULT_MIMETYPE = MimetypeMap.MIMETYPE_BINARY;
   private static final String DEFAULT_ENCODING = "utf-8";

   private static final String ARG_CONTENT_URL = "contentUrl";
   
   protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Authenticating downloadDirect request: " + req.getRequestURI());
      }
      
      AuthenticationStatus status = servletAuthenticate(req, res);
      if (status == AuthenticationStatus.Failure)
      {
         return;
      }
      
      setNoCacheHeaders(res);
      
      processRequest(req, res);
   }

   /**
    * Processes the request using the current context i.e. no
    * authentication checks are made, it is presumed they have already
    * been done.
    * 
    * @param req The HTTP request
    * @param res The HTTP response
    */
   private void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String uri = req.getRequestURI();
      
      String contentUrl = req.getParameter(ARG_CONTENT_URL);
      if (contentUrl == null || contentUrl.length() == 0)
      {
         throw new IllegalArgumentException("Download URL did not contain parameter '" + ARG_CONTENT_URL + "':" + uri); 
      }

      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      ContentService contentService = serviceRegistry.getContentService();
      
      // Attempt to get the reader
      ContentReader reader = null;
      try
      {
         reader = contentService.getRawReader(contentUrl);
         // If the content doesn't exist, generate an error
         if (!reader.exists())
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Returning 204 Not Found error...");
            }
            res.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
         }
      }
      catch (AccessDeniedException e)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Returning 403 Forbidden error after exception: ", e);
         }
         res.sendError(HttpServletResponse.SC_FORBIDDEN);
         return;
      }
      
      // Fill repsonse details
      res.setContentType(DEFAULT_MIMETYPE);
      res.setCharacterEncoding(DEFAULT_ENCODING);
      
      // Pass the stream to the response
      try
      {
         OutputStream clientOs = res.getOutputStream();
         reader.getContent( clientOs );                     // Streams closed for us
      }
      catch (SocketException e1)
      {
         // Not a problem
         if (logger.isDebugEnabled())
         {
            logger.debug(
                  "Client aborted stream read:\n" +
                  "   Content URL: " + contentUrl);
         }
      }
      catch (ContentIOException e2)
      {
         // Not a problem
         if (logger.isDebugEnabled())
         {
            logger.debug(
                  "Client aborted stream read:\n" +
                  "   Content URL: " + contentUrl);
         }
      }
   }
   
   /**
    * Helper to generate a URL based on the ContentStore URL and ticket.
    * 
    * @param contentUrl    the content URL - never null
    * @param ticket        the authentication ticket
    * 
    * @return              Returns the URL with which to access the servlet
    */
   public final static String generateURL(String contentUrl, String ticket)
   {
      return MessageFormat.format(
            DEFAULT_URL,
            contentUrl, ticket);
   }
}
