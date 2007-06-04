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
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;

/**
 * Base class for the download content servlets. Provides common
 * processing for the request.
 * 
 * @see org.alfresco.web.app.servlet.DownloadContentServlet
 * @see org.alfresco.web.app.servlet.GuestDownloadContentServlet
 * 
 * @author Kevin Roast
 * @author gavinc
 */
public abstract class BaseDownloadContentServlet extends BaseServlet
{
   private static final long serialVersionUID = -4558907921887235966L;
   
   protected static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
   
   protected static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
   
   protected static final String URL_DIRECT        = "d";
   protected static final String URL_DIRECT_LONG   = "direct";
   protected static final String URL_ATTACH        = "a";
   protected static final String URL_ATTACH_LONG   = "attach";
   protected static final String ARG_PROPERTY = "property";
   protected static final String ARG_PATH     = "path";

   /**
    * Gets the logger to use for this request.
    * <p>
    * This will show all debug entries from this class as though they
    * came from the subclass.
    * 
    * @return The logger
    */
   protected abstract Log getLogger();
   
   /**
    * Processes the download request using the current context i.e. no
    * authentication checks are made, it is presumed they have already
    * been done.
    * 
    * @param req The HTTP request
    * @param res The HTTP response
    * @param redirectToLogin Flag to determine whether to redirect to the login
    *                        page if the user does not have the correct permissions
    */
   protected void processDownloadRequest(HttpServletRequest req, HttpServletResponse res,
         boolean redirectToLogin)
         throws ServletException, IOException
   {   
      Log logger = getLogger();
      String uri = req.getRequestURI();
      
      if (logger.isDebugEnabled())
      {
         String queryString = req.getQueryString();
         logger.debug("Processing URL: " + uri + 
               ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
      }
      
      // TODO: add compression here?
      //       see http://servlets.com/jservlet2/examples/ch06/ViewResourceCompress.java for example
      //       only really needed if we don't use the built in compression of the servlet container
      uri = uri.substring(req.getContextPath().length());
      StringTokenizer t = new StringTokenizer(uri, "/");
      int tokenCount = t.countTokens();
      
      t.nextToken();    // skip servlet name
      
      // attachment mode (either 'attach' or 'direct')
      String attachToken = t.nextToken();
      boolean attachment = URL_ATTACH.equals(attachToken) || URL_ATTACH_LONG.equals(attachToken);
      
      // get or calculate the noderef and filename to download as
      NodeRef nodeRef;
      String filename;
      
      // do we have a path parameter instead of a NodeRef?
      String path = req.getParameter(ARG_PATH);
      if (path != null && path.length() != 0)
      {
         // process the name based path to resolve the NodeRef and the Filename element
         PathRefInfo pathInfo = resolveNamePath(getServletContext(), path); 
         
         nodeRef = pathInfo.NodeRef;
         filename = pathInfo.Filename;
      }
      else
      {
         // a NodeRef must have been specified if no path has been found
         if (tokenCount < 6)
         {
            throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
         }
         
         // assume 'workspace' or other NodeRef based protocol for remaining URL elements
         StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
         String id = URLDecoder.decode(t.nextToken(), "UTF-8");
         // build noderef from the appropriate URL elements
         nodeRef = new NodeRef(storeRef, id);
         
         // filename is last remaining token
         filename = t.nextToken();
      }
      
      // get qualified of the property to get content from - default to ContentModel.PROP_CONTENT
      QName propertyQName = ContentModel.PROP_CONTENT;
      String property = req.getParameter(ARG_PROPERTY);
      if (property != null && property.length() != 0)
      {
          propertyQName = QName.createQName(property);
      }
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Found NodeRef: " + nodeRef);
         logger.debug("Will use filename: " + filename);
         logger.debug("For property: " + propertyQName);
         logger.debug("With attachment mode: " + attachment);
      }
      
      // get the services we need to retrieve the content
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      NodeService nodeService = serviceRegistry.getNodeService();
      ContentService contentService = serviceRegistry.getContentService();
      PermissionService permissionService = serviceRegistry.getPermissionService();
      
      try
      {
         // check that the user has at least READ_CONTENT access - else redirect to the login page
         if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
         {
            if (logger.isDebugEnabled())
               logger.debug("User does not have permissions to read content for NodeRef: " + nodeRef.toString());
            
            if (redirectToLogin)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Redirecting to login page...");
               
               redirectToLoginPage(req, res, getServletContext());
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("Returning 403 Forbidden error...");
               
               res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }  
            return;
         }
         
         // check If-Modified-Since header and set Last-Modified header as appropriate
         Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
         long modifiedSince = req.getDateHeader("If-Modified-Since");
         if (modifiedSince > 0L)
         {
             // round the date to the ignore millisecond value which is not supplied by header
             long modDate = (modified.getTime() / 1000L) * 1000L;
             if (modDate <= modifiedSince)
             {
                 res.setStatus(304);
                 return;
             }
         }
         res.setDateHeader("Last-Modified", modified.getTime());
         
         if (attachment == true)
         {
            // set header based on filename - will force a Save As from the browse if it doesn't recognise it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", "attachment");
         }
         
         // get the content reader
         ContentReader reader = contentService.getReader(nodeRef, propertyQName);
         // ensure that it is safe to use
         reader = FileContentReader.getSafeContentReader(
                    reader,
                    Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                    nodeRef, reader);
         
         String mimetype = reader.getMimetype();
         // fall back if unable to resolve mimetype property
         if (mimetype == null || mimetype.length() == 0)
         {
            MimetypeService mimetypeMap = serviceRegistry.getMimetypeService();
            mimetype = MIMETYPE_OCTET_STREAM;
            int extIndex = filename.lastIndexOf('.');
            if (extIndex != -1)
            {
               String ext = filename.substring(extIndex + 1);
               String mt = mimetypeMap.getMimetypesByExtension().get(ext);
               if (mt != null)
               {
                  mimetype = mt;
               }
            }
         }
         // set mimetype for the content and the character encoding for the stream
         res.setContentType(mimetype);
         res.setCharacterEncoding(reader.getEncoding());
         
         // get the content and stream directly to the response output stream
         // assuming the repo is capable of streaming in chunks, this should allow large files
         // to be streamed directly to the browser response stream.
         try
         {
            reader.getContent( res.getOutputStream() );
         }
         catch (SocketException e1)
         {
            // the client cut the connection - our mission was accomplished apart from a little error message
            if (logger.isInfoEnabled())
               logger.info("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
         }
         catch (ContentIOException e2)
         {
            if (logger.isInfoEnabled())
               logger.info("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during download content servlet processing: " + err.getMessage(), err);
      }
   }
   
   /**
    * Helper to generate a URL to a content node for downloading content from the server.
    * 
    * @param pattern The pattern to use for the URL
    * @param ref     NodeRef of the content node to generate URL for (cannot be null)
    * @param name    File name to return in the URL (cannot be null)
    * 
    * @return URL to download the content from the specified node
    */
   protected final static String generateUrl(String pattern, NodeRef ref, String name)
   {
      String url = null;
      
      try
      {
         url = MessageFormat.format(pattern, new Object[] {
                  ref.getStoreRef().getProtocol(),
                  ref.getStoreRef().getIdentifier(),
                  ref.getId(),
                  Utils.replace(URLEncoder.encode(name, "UTF-8"), "+", "%20") } );
      }
      catch (UnsupportedEncodingException uee)
      {
         throw new AlfrescoRuntimeException("Failed to encode content URL for node: " + ref, uee);
      }
      
      return url;
   }
}
