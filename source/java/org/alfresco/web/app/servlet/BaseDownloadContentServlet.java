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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
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
   
   private static final String MULTIPART_BYTERANGES_BOUNDRY = "<ALF4558907921887235966L>";
   private static final String MULTIPART_BYTERANGES_HEADER  = "multipart/byteranges; boundary=" + MULTIPART_BYTERANGES_BOUNDRY;
   private static final String MULTIPART_BYTERANGES_BOUNDRY_SEP = "--" + MULTIPART_BYTERANGES_BOUNDRY;
   private static final String MULTIPART_BYTERANGES_BOUNDRY_END = MULTIPART_BYTERANGES_BOUNDRY_SEP + "--";
   
   private static final String HEADER_CONTENT_TYPE   = "Content-Type";
   private static final String HEADER_CONTENT_RANGE  = "Content-Range";
   private static final String HEADER_CONTENT_LENGTH = "Content-Length";
   private static final String HEADER_ACCEPT_RANGES  = "Accept-Ranges";
   private static final String HEADER_RANGE          = "Range";
   private static final String HEADER_ETAG           = "ETag";
   private static final String HEADER_CACHE_CONTROL  = "Cache-Control";
   private static final String HEADER_LAST_MODIFIED  = "Last-Modified";
   private static final String HEADER_USER_AGENT     = "User-Agent";
   private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
   
   /** size of a multi-part byte range output buffer */
   private static final int CHUNKSIZE = 64*1024;
   
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
                     processedRange = processRange(
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
       // "filename" part.
       StringBuilder attachmentValue = new StringBuilder(128).append("attachment");
       String userAgent = req.getHeader(HEADER_USER_AGENT);
       if (userAgent != null && userAgent.toLowerCase().contains("msie"))
       {
           attachmentValue.append("; filename=\"").append(res.encodeURL(filename)).append("\"");
       }
       res.setHeader(HEADER_CONTENT_DISPOSITION, attachmentValue.toString());
   }
   
   /**
    * Process a range header - handles single and multiple range requests.
    */
   private boolean processRange(HttpServletResponse res, ContentReader reader, String range,
         NodeRef ref, QName property, String mimetype, String userAgent)
      throws IOException
   {
      // test for multiple byte ranges present in header
      if (range.indexOf(',') == -1)
      {
         return processSingleRange(res, reader, range, mimetype);
      }
      else
      {
         return processMultiRange(res, range, ref, property, mimetype, userAgent);
      }
   }

   /**
    * Process a single range request.
    * 
    * @param res        HttpServletResponse
    * @param reader     ContentReader to retrieve content
    * @param range      Range header value
    * @param mimetype   Content mimetype
    * 
    * @return true if processed range, false otherwise
    */
   private boolean processSingleRange(HttpServletResponse res, ContentReader reader, String range, String mimetype)
      throws IOException
   {
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
      
      boolean processedRange = false;
      Range r = null;
      try
      {
         r = Range.constructRange(range, mimetype, reader.getSize());
      }
      catch (IllegalArgumentException err)
      {
         if (getLogger().isDebugEnabled())
            getLogger().debug("Failed to parse range header - returning 416 status code: " + err.getMessage());
         
         res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
         res.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
         res.getOutputStream().close();
         return true;
      }
      
      // set Partial Content status and range headers
      res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
      res.setContentType(mimetype);
      String contentRange = "bytes " + Long.toString(r.start) + "-" + Long.toString(r.end) + "/" + Long.toString(reader.getSize());
      res.setHeader(HEADER_CONTENT_RANGE, contentRange);
      res.setHeader(HEADER_CONTENT_LENGTH, Long.toString((r.end - r.start) + 1L));
      
      if (getLogger().isDebugEnabled())
         getLogger().debug("Processing: Content-Range: " + contentRange);
      
      InputStream is = null;
      try
      {
         // output the binary data for the range
         ServletOutputStream os = res.getOutputStream();
         is = reader.getContentInputStream();
         
         streamRangeBytes(r, is, os, 0L);
         
         os.close();
         processedRange = true;
      }
      catch (IOException err)
      {
         if (getLogger().isDebugEnabled())
            getLogger().debug("Unable to process single range due to IO Exception: " + err.getMessage());
         throw err;
      }
      finally
      {
         if (is != null) is.close();
      }
      
      return processedRange;
   }

   /**
    * Process multiple ranges.
    * 
    * @param res        HttpServletResponse
    * @param range      Range header value
    * @param ref        NodeRef to the content for streaming
    * @param property   Content Property for the content
    * @param mimetype   Mimetype of the content
    * @param userAgent  User Agent of the caller
    * 
    * @return true if processed range, false otherwise
    */
   private boolean processMultiRange(
         HttpServletResponse res, String range, NodeRef ref, QName property, String mimetype, String userAgent)
      throws IOException
   {
      final Log logger = getLogger();
      
      // return the sets of bytes as requested in the content-range header
      // the response will be formatted as multipart/byteranges media type message
      
      /* Examples of byte-ranges-specifier values (assuming an entity-body of length 10000):

      - The first 500 bytes (byte offsets 0-499, inclusive):  bytes=0-499
      - The second 500 bytes (byte offsets 500-999, inclusive):
        bytes=500-999
      - The final 500 bytes (byte offsets 9500-9999, inclusive):
        bytes=-500
      - Or bytes=9500-
      - The first and last bytes only (bytes 0 and 9999):  bytes=0-0,-1
      - Several legal but not canonical specifications of byte offsets 500-999, inclusive:
         bytes=500-600,601-999
         bytes=500-700,601-999 */
      
      boolean processedRange = false;
      
      // get the content reader
      ContentService contentService = getServiceRegistry(getServletContext()).getContentService();
      ContentReader reader = contentService.getReader(ref, property);
      
      final List<Range> ranges = new ArrayList<Range>(8);
      long entityLength = reader.getSize();
      for (StringTokenizer t=new StringTokenizer(range, ", "); t.hasMoreTokens(); /**/)
      {
         try
         {
            ranges.add(Range.constructRange(t.nextToken(), mimetype, entityLength));
         }
         catch (IllegalArgumentException err)
         {
            if (getLogger().isDebugEnabled())
               getLogger().debug("Failed to parse range header - returning 416 status code: " + err.getMessage());
            
            res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            res.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
            res.getOutputStream().close();
            return true;
         }
      }
      
      if (ranges.size() != 0)
      {
         // merge byte ranges if possible - IE handles this well, FireFox not so much
         if (userAgent == null || userAgent.indexOf("MSIE ") != -1)
         {
            Collections.sort(ranges);
            
            for (int i=0; i<ranges.size() - 1; i++)
            {
               Range first = ranges.get(i);
               Range second = ranges.get(i + 1);
               if (first.end + 1 >= second.start)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Merging byte range: " + first + " with " + second);
                  
                  if (first.end < second.end)
                  {
                     // merge second range into first
                     first.end = second.end;
                  }
                  // else we simply discard the second range - it is contained within the first
                  
                  // delete second range
                  ranges.remove(i + 1);
                  // reset loop index
                  i--;
               }
            }
         }
         
         // calculate response content length
         long length = MULTIPART_BYTERANGES_BOUNDRY_END.length() + 2;
         for (Range r : ranges)
         {
            length += r.getLength();
         }
         
         // output headers as we have at least one range to process
         res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
         res.setHeader(HEADER_CONTENT_TYPE, MULTIPART_BYTERANGES_HEADER);
         res.setHeader(HEADER_CONTENT_LENGTH, Long.toString(length));
         
         ServletOutputStream os = res.getOutputStream();
         
         InputStream is = null;
         try
         {
            for (Range r : ranges)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Processing: " + r.getContentRange());
               
               try
               {
                  // output the header bytes for the range
                  r.outputHeader(os);
                  
                  // output the binary data for the range
                  // need a new reader for each new InputStream
                  is = contentService.getReader(ref, property).getContentInputStream();
                  streamRangeBytes(r, is, os, 0L);
                  is.close();
                  is = null;
                  
                  // section marker and flush stream
                  os.println();
                  os.flush();
               }
               catch (IOException err)
               {
                  if (getLogger().isDebugEnabled())
                     getLogger().debug("Unable to process multiple range due to IO Exception: " + err.getMessage());
                  throw err;
               }
            }
         }
         finally
         {
            if (is != null)
            {
               is.close();
            }
         }
         
         // end marker
         os.println(MULTIPART_BYTERANGES_BOUNDRY_END);
         os.close();
         processedRange = true;
      }
      
      return processedRange;
   }
   
   /**
    * Stream a range of bytes from the given InputStream to the ServletOutputStream
    * 
    * @param r       Byte Range to process
    * @param is      InputStream
    * @param os      ServletOutputStream
    * @param offset  Assumed InputStream position - to calculate skip bytes from
    * 
    * @return current InputStream position - so the stream can be reused if required 
    */
   private void streamRangeBytes(final Range r, final InputStream is, final ServletOutputStream os, long offset)
      throws IOException
   {
      final Log logger = getLogger();
      final boolean trace = logger.isTraceEnabled();
      
      // TODO: investigate using getFileChannel() on ContentReader
      
      if (r.start != 0L)
      {
         long skipped = offset;
         while (skipped < r.start)
         {
            skipped += is.skip(r.start - skipped);
         }
      }
      long span = (r.end - r.start) + 1L;
      long bytesLeft = span;
      int read = 0;
      byte[] buf = new byte[((int)bytesLeft) < CHUNKSIZE ? (int)bytesLeft : CHUNKSIZE];
      while ((read = is.read(buf)) > 0 && bytesLeft != 0L)
      {
         os.write(buf, 0, read);
         
         bytesLeft -= (long)read;
         
         if (bytesLeft != 0L)
         {
            int resize = ((int)bytesLeft) < CHUNKSIZE ? (int)bytesLeft : CHUNKSIZE;
            if (resize != buf.length)
            {
               buf = new byte[resize];
            }
         }
         if (trace) logger.trace("...wrote " + read + " bytes, with " + bytesLeft + " to go...");
      }
   }
   
   
   /**
    * Representation of a single byte range.
    */
   private static class Range implements Comparable<Range>
   {
      private long start;
      private long end;
      private long entityLength;
      private String contentType;
      private String contentRange;
      
      /**
       * Constructor
       * 
       * @param contentType      Mimetype of the range content
       * @param start            Start position in the parent entity
       * @param end              End position in the parent entity
       * @param entityLength     Length of the parent entity
       */
      Range(String contentType, long start, long end, long entityLength)
      {
         this.contentType = HEADER_CONTENT_TYPE + ": " + contentType;
         this.start = start;
         this.end = end;
         this.entityLength = entityLength;
      }
      
      /**
       * Factory method to construct a byte range from a range header value.
       * 
       * @param range         Range header value
       * @param contentType   Mimetype of the range
       * @param entityLength  Length of the parent entity
       * 
       * @return Range
       * 
       * @throws IllegalArgumentException for an invalid range
       */
      static Range constructRange(String range, String contentType, long entityLength)
      {
         if (range == null)
         {
            throw new IllegalArgumentException("Range argument is mandatory");
         }
         
         // strip total if present - it does not give us anything useful
         if (range.indexOf('/') != -1)
         {
            range = range.substring(0, range.indexOf('/'));
         }
         
         // find the separator
         int separator = range.indexOf('-');
         if (separator == -1)
         {
            throw new IllegalArgumentException("Invalid range: " + range);
         }
         
         try
         {
            // split range and parse values
            long start = 0L;
            if (separator != 0)
            {
               start = Long.parseLong(range.substring(0, separator));
            }
            long end = entityLength - 1L;
            if (separator != range.length() - 1)
            {
               end = Long.parseLong(range.substring(separator + 1));
            }
            
            // return object to represent the byte-range
            return new Range(contentType, start, end, entityLength);
         }
         catch (NumberFormatException err)
         {
            throw new IllegalArgumentException("Unable to parse range value: " + range);
         }
      }
      
      /**
       * Output the header bytes for a multi-part byte range header
       */
      void outputHeader(ServletOutputStream os) throws IOException
      {
         // output multi-part boundry separator
         os.println(MULTIPART_BYTERANGES_BOUNDRY_SEP);
         // output content type and range size sub-header for this part
         os.println(this.contentType);
         os.println(getContentRange());
         os.println();
      }
      
      /**
       * @return the length in bytes of the byte range content including the header bytes
       */
      int getLength()
      {
         // length in bytes of range plus it's header plus section marker and line feed bytes
         return MULTIPART_BYTERANGES_BOUNDRY_SEP.length() + 2 +
                this.contentType.length() + 2 +
                getContentRange().length() + 4 + (int)(this.end - this.start + 1L) + 2;
      }
      
      /**
       * @return the Content-Range header string value for this byte range
       */
      private String getContentRange()
      {
         if (this.contentRange == null)
         {
            this.contentRange = "Content-Range: bytes " + Long.toString(this.start) + "-" +
               Long.toString(this.end) + "/" + Long.toString(this.entityLength);
         }
         return this.contentRange;
      }

      @Override
      public String toString()
      {
         return this.start + "-" + this.end;
      }

      /**
       * @see java.lang.Comparable#compareTo(java.lang.Object)
       */
      public int compareTo(Range o)
      {
         return this.start > o.start ? 1 : -1;
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
