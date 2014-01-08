/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import org.alfresco.repo.web.util.HttpRangeProcessor;
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
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.apache.commons.logging.Log;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.surf.util.URLEncoder;

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
   private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

   private static final long serialVersionUID = -4558907921887235967L;
   
   private static final String POWER_POINT_DOCUMENT_MIMETYPE = "application/vnd.ms-powerpoint";
   private static final String POWER_POINT_2007_DOCUMENT_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
   
   private static final String HEADER_CONTENT_RANGE  = "Content-Range";
   private static final String HEADER_CONTENT_LENGTH = "Content-Length";
   private static final String HEADER_ACCEPT_RANGES  = "Accept-Ranges";
   private static final String HEADER_RANGE          = "Range";
   private static final String HEADER_ETAG           = "ETag";
   private static final String HEADER_CACHE_CONTROL  = "Cache-Control";
   private static final String HEADER_LAST_MODIFIED  = "Last-Modified";
   private static final String HEADER_USER_AGENT     = "User-Agent";
   private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
   
   protected static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
   
   protected static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
   protected static final String MSG_ERROR_NOT_FOUND = "error_not_found";
   
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
    * Processes the download request using the current context i.e. no authentication checks are made, it is presumed
    * they have already been done.
    * 
    * @param req
    *           The HTTP request
    * @param res
    *           The HTTP response
    * @param allowLogIn
    *           Indicates whether guest users without access to the content should be redirected to the log in page. If
    *           <code>false</code>, a status 403 forbidden page is displayed instead.
    */
   protected void processDownloadRequest(HttpServletRequest req, HttpServletResponse res,
         boolean allowLogIn, boolean transmitContent)
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
         try
         {
            PathRefInfo pathInfo = resolveNamePath(getServletContext(), path);
            nodeRef = pathInfo.NodeRef;
            filename = pathInfo.Filename;
         }
         catch (IllegalArgumentException e)
         {
            Application.handleSystemError(getServletContext(), req, res, MSG_ERROR_NOT_FOUND,
                  HttpServletResponse.SC_NOT_FOUND, logger);
            return;
         }         
      }
      else
      {
         // a NodeRef must have been specified if no path has been found
         if (tokenCount < 6)
         {
            throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
         }
         
         // assume 'workspace' or other NodeRef based protocol for remaining URL elements
         StoreRef storeRef = new StoreRef(URLDecoder.decode(t.nextToken()), URLDecoder.decode(t.nextToken()));
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
               Application.handleSystemError(getServletContext(), req, res, MSG_ERROR_NOT_FOUND,
                     HttpServletResponse.SC_NOT_FOUND, logger);
               return;
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
      
      // Check that the node still exists
      if (!nodeService.exists(nodeRef))
      {
         Application.handleSystemError(getServletContext(), req, res, MSG_ERROR_NOT_FOUND,
               HttpServletResponse.SC_NOT_FOUND, logger);
         return;         
      }

      try
      {
         // check that the user has at least READ_CONTENT access - else redirect to an error or login page
         if (!checkAccess(req, res, nodeRef, PermissionService.READ_CONTENT, allowLogIn))
         {
            return;
         }
         
         // check If-Modified-Since header and set Last-Modified header as appropriate
         Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
         if (modified != null)
         {
            long modifiedSince = req.getDateHeader(HEADER_IF_MODIFIED_SINCE);
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
            res.setDateHeader(HEADER_LAST_MODIFIED, modified.getTime());
            res.setHeader(HEADER_CACHE_CONTROL, "must-revalidate, max-age=0");
            res.setHeader(HEADER_ETAG, "\"" + Long.toString(modified.getTime()) + "\"");
         }
         
         if (attachment == true)
         {
             setHeaderContentDisposition(req, res, filename);
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
               mimetype = mimetypeMap.getMimetype(ext);
            }
         }
         
         // explicitly set the content disposition header if the content is powerpoint
         if (!attachment && (mimetype.equals(POWER_POINT_2007_DOCUMENT_MIMETYPE) || 
                             mimetype.equals(POWER_POINT_DOCUMENT_MIMETYPE)))
         {
            setHeaderContentDisposition(req, res, filename);
         }
         
         // get the content and stream directly to the response output stream
         // assuming the repo is capable of streaming in chunks, this should allow large files
         // to be streamed directly to the browser response stream.
         res.setHeader(HEADER_ACCEPT_RANGES, "bytes");
         
         // for a GET request, transmit the content else just the headers are sent
         if (transmitContent)
         {
            try
            {
               boolean processedRange = false;
               String range = req.getHeader(HEADER_CONTENT_RANGE);
               if (range == null)
               {
                  range = req.getHeader(HEADER_RANGE);
               }
               if (range != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Found content range header: " + range);
                  
                  // ensure the range header is starts with "bytes=" and process the range(s)
                  if (range.length() > 6)
                  {
                     HttpRangeProcessor rangeProcessor = new HttpRangeProcessor(contentService);
                     processedRange = rangeProcessor.processRange(
                           res, reader, range.substring(6), nodeRef, propertyQName,
                           mimetype, req.getHeader(HEADER_USER_AGENT));
                  }
               }
               if (processedRange == false)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Sending complete file content...");
                  
                  // set mimetype for the content and the character encoding for the stream
                  res.setContentType(mimetype);
                  res.setCharacterEncoding(reader.getEncoding());
                  
                  // return the complete entity range
                  long size = reader.getSize();
                  res.setHeader(HEADER_CONTENT_RANGE, "bytes 0-" + Long.toString(size-1L) + "/" + Long.toString(size));
                  res.setHeader(HEADER_CONTENT_LENGTH, Long.toString(size));
                  reader.getContent( res.getOutputStream() );
               }
            }
            catch (SocketException e1)
            {
               // the client cut the connection - our mission was accomplished apart from a little error message
               if (logger.isDebugEnabled())
                  logger.debug("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
            }
            catch (ContentIOException e2)
            {
               if (logger.isInfoEnabled())
                  logger.info("Failed stream read:\n\tnode: " + nodeRef + " due to: " + e2.getMessage());
            }
            catch (Throwable err)
            {
               if (err.getCause() instanceof SocketException)
               {
                  // the client cut the connection - our mission was accomplished apart from a little error message
                  if (logger.isDebugEnabled())
                     logger.debug("Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
               }
               else throw err;
            }
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("HEAD request processed - no content sent.");
            res.getOutputStream().close();
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during download content servlet processing: " + err.getMessage(), err);
      }
   }

   private void setHeaderContentDisposition(HttpServletRequest req, HttpServletResponse res, String filename)
   {
      // set header based on filename - will force a Save As from the browse if it doesn't recognise it
      // this is better than the default response of the browser trying to display the contents

      // IE requires that "Content-Disposition" header in case of "attachment" type should include
      // "filename" part. See MNT-9900
      String userAgent = req.getHeader(HEADER_USER_AGENT);
      if (userAgent != null && (userAgent.toLowerCase().contains("firefox") || userAgent.toLowerCase().contains("safari")))
      {
         res.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + URLDecoder.decode(filename) + "\"");
      }
      else
      {
         res.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
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
