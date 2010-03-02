/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
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
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.surf.util.URLEncoder;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
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
   
   private static final String POWER_POINT_DOCUMENT_MIMETYPE = "application/vnd.powerpoint";

   private static final String POWER_POINT_2007_DOCUMENT_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
   
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
      
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      
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
         String id = URLDecoder.decode(t.nextToken());
         
         // build noderef from the appropriate URL elements
         nodeRef = new NodeRef(storeRef, id);
         
         if (tokenCount > 6)
         {
            // found additional relative path elements i.e. noderefid/images/file.txt
            // this allows a url to reference siblings nodes via a cm:name based relative path
            // solves the issue with opening HTML content containing relative URLs in HREF or IMG tags etc.
            List<String> paths = new ArrayList<String>(tokenCount - 5);
            while (t.hasMoreTokens())
            {
               paths.add(URLDecoder.decode(t.nextToken()));
            }
            filename = paths.get(paths.size() - 1);

            try
            {
               NodeRef parentRef = serviceRegistry.getNodeService().getPrimaryParent(nodeRef).getParentRef();
               FileInfo fileInfo = serviceRegistry.getFileFolderService().resolveNamePath(parentRef, paths);
               nodeRef = fileInfo.getNodeRef();
            }
            catch (FileNotFoundException e)
            {
               throw new AlfrescoRuntimeException("Unable to find node reference by relative path:" + uri);
            }
         }
         else
         {
            // filename is last remaining token
            filename = t.nextToken();
         }
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
               
               // TODO: replace with serviceRegistry.getAuthorityService().hasGuestAuthority() from 3.1E
               if (!AuthenticationUtil.getFullyAuthenticatedUser().equals(AuthenticationUtil.getGuestUserName()))
               {
                   req.getSession().setAttribute(LoginBean.LOGIN_NOPERMISSIONS, Boolean.TRUE);
               }
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
         if (modified != null)
         {
            long modifiedSince = req.getDateHeader("If-Modified-Since");
            if (modifiedSince > 0L)
            {
               // round the date to the ignore millisecond value which is not supplied by header
               long modDate = (modified.getTime() / 1000L) * 1000L;
               if (modDate <= modifiedSince)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Returning 304 Not Modified.");
                  res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                  return;
               }
            }
            res.setDateHeader("Last-Modified", modified.getTime());
            res.setHeader("Cache-Control", "must-revalidate");
            res.setHeader("ETag", "\"" + Long.toString(modified.getTime()) + "\"");
         }
         
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

         // explicitly set the content disposition header if the content is powerpoint
         if (!attachment && (mimetype.equals(POWER_POINT_2007_DOCUMENT_MIMETYPE) || 
                             mimetype.equals(POWER_POINT_DOCUMENT_MIMETYPE)))
         {
            res.setHeader("Content-Disposition", "attachment");
         }

         // set mimetype for the content and the character encoding for the stream
         res.setContentType(mimetype);
         res.setCharacterEncoding(reader.getEncoding());
         
         // get the content and stream directly to the response output stream
         // assuming the repo is capable of streaming in chunks, this should allow large files
         // to be streamed directly to the browser response stream.
         res.setHeader("Accept-Ranges", "bytes");
         try
         {
            boolean processedRange = false;
            String range = req.getHeader("Content-Range");
            if (range == null)
            {
               range = req.getHeader("Range");
            }
            if (range != null)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Found content range header: " + range);
               // return the specific set of bytes as requested in the content-range header
               /* Examples of byte-content-range-spec values, assuming that the entity contains total of 1234 bytes:
                     The first 500 bytes:
                      bytes 0-499/1234

                     The second 500 bytes:
                      bytes 500-999/1234

                     All except for the first 500 bytes:
                      bytes 500-1233/1234 */
               /* 'Range' header example:
                      bytes=10485760-20971519 */
               try
               {
                  if (range.length() > 6)
                  {
                     StringTokenizer r = new StringTokenizer(range.substring(6), "-/");
                     if (r.countTokens() >= 2)
                     {
                        long start = Long.parseLong(r.nextToken());
                        long end = Long.parseLong(r.nextToken());
                        
                        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                        res.setHeader("Content-Range", range);
                        res.setHeader("Content-Length", Long.toString(((end-start)+1L)));
                        
                        InputStream is = null;
                        try
                        {
                           is = reader.getContentInputStream();
                           if (start != 0) is.skip(start);
                           long span = (end-start)+1;
                           long total = 0;
                           int read = 0;
                           byte[] buf = new byte[((int)span) < 8192 ? (int)span : 8192];
                           while ((read = is.read(buf)) != 0 && total < span)
                           {
                              total += (long)read;
                              res.getOutputStream().write(buf, 0, (int)read);
                           }
                           res.getOutputStream().close();
                           processedRange = true;
                        }
                        finally
                        {
                           if (is != null) is.close();
                        }
                     }
                  }
               }
               catch (NumberFormatException nerr)
               {
                  // processedRange flag will stay false if this occurs
               }
            }
            if (processedRange == false)
            {
               // As per the spec:
               //  If the server ignores a byte-range-spec because it is syntactically
               //  invalid, the server SHOULD treat the request as if the invalid Range
               //  header field did not exist.
               long size = reader.getSize();
               res.setHeader("Content-Range", "bytes 0-" + Long.toString(size-1L) + "/" + Long.toString(size));
               res.setHeader("Content-Length", Long.toString(size));
               reader.getContent( res.getOutputStream() );
            }
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
      return MessageFormat.format(pattern, new Object[] {
              ref.getStoreRef().getProtocol(),
              ref.getStoreRef().getIdentifier(),
              ref.getId(),
              URLEncoder.encode(name) } );
   }
}
