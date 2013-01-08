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
package org.alfresco.repo.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Generates HTTP response for "Range" scoped HTTP requests for content.
 */
public class HttpRangeProcessor
{
    private static final Log logger = LogFactory.getLog(HttpRangeProcessor.class);
    private static final String HEADER_CONTENT_TYPE   = "Content-Type";
    private static final String HEADER_CONTENT_RANGE  = "Content-Range";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String MULTIPART_BYTERANGES_BOUNDRY = "<ALF4558907921887235966L>";
    private static final String MULTIPART_BYTERANGES_HEADER  = "multipart/byteranges; boundary=" + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_SEP = "--" + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_END = MULTIPART_BYTERANGES_BOUNDRY_SEP + "--";
    /** size of a multi-part byte range output buffer */
    private static final int CHUNKSIZE = 64*1024;
    private ContentService contentService;
    
    
    /**
     * Constructor.
     * 
     * @param contentService
     */
    public HttpRangeProcessor(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Process a range header for a HttpServletResponse - handles single and multiple range requests.
     * 
     * @param res the HTTP servlet response
     * @param reader the content reader
     * @param range the byte range
     * @param ref the content NodeRef
     * @param property the content property
     * @param mimetype the content mimetype
     * @param userAgent the user agent string
     * @return whether or not the range could be processed
     * @throws IOException
     */
    public boolean processRange(HttpServletResponse res, ContentReader reader, String range,
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
     * Process a range header for a WebScriptResponse - handles single and multiple range requests.
     * 
     * @param res the webscript response
     * @param reader the content reader
     * @param range the byte range
     * @param ref the content NodeRef
     * @param property the content property
     * @param mimetype the content mimetype
     * @param userAgent the user agent string
     * @return whether or not the range could be processed
     * @throws IOException
     */
    public boolean processRange(WebScriptResponse res, ContentReader reader, String range,
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
    private boolean processSingleRange(Object res, ContentReader reader, String range, String mimetype)
       throws IOException
    {
        // Handle either HttpServletResponse or WebScriptResponse
        HttpServletResponse httpServletResponse = null;
        WebScriptResponse webScriptResponse = null;
        if (res instanceof HttpServletResponse)
        {
           httpServletResponse = (HttpServletResponse) res;
        }
        else if (res instanceof WebScriptResponse)
        {
           webScriptResponse = (WebScriptResponse) res;
        }
        if (httpServletResponse == null && webScriptResponse == null)
        {
            // Unknown response object type
            return false;
        }
        
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
          
          if (httpServletResponse != null)
          {
             httpServletResponse.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
             httpServletResponse.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
             httpServletResponse.getOutputStream().close();
          }
          else if (webScriptResponse != null)
          {
             webScriptResponse.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
             webScriptResponse.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
             webScriptResponse.getOutputStream().close();
          }
          return true;
       }
       
       // set Partial Content status and range headers
       String contentRange = "bytes " + Long.toString(r.start) + 
               "-" + Long.toString(r.end) + "/" + Long.toString(reader.getSize());
       if (httpServletResponse != null)
       {
          httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
          httpServletResponse.setContentType(mimetype);
          httpServletResponse.setHeader(HEADER_CONTENT_RANGE, contentRange);
          httpServletResponse.setHeader(HEADER_CONTENT_LENGTH, Long.toString((r.end - r.start) + 1L));
       }
       else if (webScriptResponse != null)
       {
          webScriptResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
          webScriptResponse.setContentType(mimetype);
          webScriptResponse.setHeader(HEADER_CONTENT_RANGE, contentRange);
          webScriptResponse.setHeader(HEADER_CONTENT_LENGTH, Long.toString((r.end - r.start) + 1L));
       }
       
       if (getLogger().isDebugEnabled())
          getLogger().debug("Processing: Content-Range: " + contentRange);
       
       InputStream is = null;
       try
       {
          // output the binary data for the range
          OutputStream os = null;
          if (httpServletResponse != null)
          {
             os = httpServletResponse.getOutputStream();
          }
          else if (webScriptResponse != null)
          {
             os = webScriptResponse.getOutputStream();
          }
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
          Object res, String range, NodeRef ref, QName property, String mimetype, String userAgent)
       throws IOException
    {
       final Log logger = getLogger();
       
       // Handle either HttpServletResponse or WebScriptResponse
       HttpServletResponse httpServletResponse = null;
       WebScriptResponse webScriptResponse = null;
       if (res instanceof HttpServletResponse)
       {
          httpServletResponse = (HttpServletResponse) res;
       }
       else if (res instanceof WebScriptResponse)
       {
          webScriptResponse = (WebScriptResponse) res;
       }
       if (httpServletResponse == null && webScriptResponse == null)
       {
           // Unknown response object type
           return false;
       }
       
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
             
             if (httpServletResponse != null)
             {
                httpServletResponse.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                httpServletResponse.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
                httpServletResponse.getOutputStream().close();
             }
             else if (webScriptResponse != null)
             {
                webScriptResponse.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                webScriptResponse.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
                webScriptResponse.getOutputStream().close();
             }
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
          OutputStream os = null;
          if (httpServletResponse != null)
          {
             httpServletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
             httpServletResponse.setHeader(HEADER_CONTENT_TYPE, MULTIPART_BYTERANGES_HEADER);
             httpServletResponse.setHeader(HEADER_CONTENT_LENGTH, Long.toString(length));
             os = httpServletResponse.getOutputStream();
          }
          else if (webScriptResponse != null)
          {
             webScriptResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
             webScriptResponse.setHeader(HEADER_CONTENT_TYPE, MULTIPART_BYTERANGES_HEADER);
             webScriptResponse.setHeader(HEADER_CONTENT_LENGTH, Long.toString(length));
             os =webScriptResponse.getOutputStream();
          }
          
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
                   if (os instanceof ServletOutputStream)
                       r.outputHeader((ServletOutputStream) os);
                   
                   // output the binary data for the range
                   // need a new reader for each new InputStream
                   is = contentService.getReader(ref, property).getContentInputStream();
                   streamRangeBytes(r, is, os, 0L);
                   is.close();
                   is = null;
                   
                   // section marker and flush stream
                   if (os instanceof ServletOutputStream)
                       ((ServletOutputStream) os).println();
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
          if (os instanceof ServletOutputStream)
              ((ServletOutputStream) os).println(MULTIPART_BYTERANGES_BOUNDRY_END);
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
    private void streamRangeBytes(final Range r, final InputStream is, final OutputStream os, long offset)
       throws IOException
    {
       final Log logger = getLogger();
       final boolean trace = logger.isTraceEnabled();
       
       // TODO: investigate using getFileChannel() on ContentReader
       
       if (r.start != 0L && r.start > offset)
       {
          long skipped = offset + is.skip(r.start - offset);
          if (skipped < r.start)
          {
              // Nothing left to download!
              return;
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
     * @return the logger
     */
    private static Log getLogger()
    {
        return logger;
    }
}
