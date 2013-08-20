package org.alfresco.repo.web.scripts.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.web.util.HttpRangeProcessor;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * Can be used when the binary data of a content property needs to be streamed back to the client
 * as the result of executing a web script.
 * 
 * These methods are taken from the StreamContent class so they can be reused by other webscripts.
 *
 */

public class ContentStreamer implements ResourceLoaderAware
{
        // Logger
        private static final Log logger = LogFactory.getLog(ContentStreamer.class);

        /**
         * format definied by RFC 822, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3 
         */
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
        
        private static final String HEADER_CONTENT_RANGE  = "Content-Range";
        private static final String HEADER_CONTENT_LENGTH = "Content-Length";
        private static final String HEADER_ACCEPT_RANGES  = "Accept-Ranges";
        private static final String HEADER_RANGE          = "Range";
        private static final String HEADER_USER_AGENT     = "User-Agent";
        
        /** Services */
       // protected PermissionService permissionService;
        protected NodeService nodeService;
        protected ContentService contentService;
        protected MimetypeService mimetypeService;
        protected ResourceLoader resourceLoader;
   
        /**
         * @param mimetypeService
         */
        public void setMimetypeService(MimetypeService mimetypeService)
        {
            this.mimetypeService = mimetypeService; 
        }

        /**
         * @param nodeService
         */
        public void setNodeService(NodeService nodeService)
        {
            this.nodeService = nodeService; 
        }
        
        @Override
        public void setResourceLoader(ResourceLoader resourceLoader)
        {
            this.resourceLoader = resourceLoader;
        }
        
        /**
         * @param contentService
         */
        public void setContentService(ContentService contentService)
    
        {
            this.contentService = contentService;
        }  


    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 File file, 
                                 boolean attach) throws IOException
    {
        streamContent(req, res, file, attach, null, null);
    }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 File file, 
                                 boolean attach,
                                 Map<String, Object> model) 
                throws IOException
    {
        streamContent(req, res, file, attach, null, model);
    }

    /**
     * Streams content back to client from a given File. The Last-Modified header will reflect the
     * given file's modification timestamp.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req,
                                 WebScriptResponse res, 
                                 File file, 
                                 boolean attach,
                                 String attachFileName) throws IOException
    {
        streamContent(req, res, file, attach, attachFileName, null);
    }

    /**
     * Streams content back to client from a given File. The Last-Modified header will reflect the
     * given file's modification timestamp.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req,
                                 WebScriptResponse res, 
                                 File file, 
                                 boolean attach,
                                 String attachFileName,
                                 Map<String, Object> model) throws IOException
    {
        streamContent(req, res, file, null, attach, attachFileName, model);
    }

    /**
     * Streams content back to client from a given File.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @param modifiedTime      The modified datetime to use for the streamed content. If <tt>null</tt> the
     *                          file's timestamp will be used.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req, 
            WebScriptResponse res, 
            File file, 
            Long modifiedTime,
            boolean attach, 
            String attachFileName) throws IOException
    {
        streamContent(req, res, file, modifiedTime, attach, attachFileName, null);
    }

    /**
     * Streams content back to client from a given File.
     * 
     * @param req               The request
     * @param res               The response
     * @param file              The file whose content is to be streamed.
     * @param modifiedTime      The modified datetime to use for the streamed content. If <tt>null</tt> the
     *                          file's timestamp will be used.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    public void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 File file, 
                                 Long modifiedTime,
                                 boolean attach, 
                                 String attachFileName,
                                 Map<String, Object> model) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from file " + file.getAbsolutePath() + " (attach: " + attach + ")");
        
        // determine mimetype from file extension
        String filePath = file.getAbsolutePath();
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        int extIndex = filePath.lastIndexOf('.');
        if (extIndex != -1)
        {
            mimetype = mimetypeService.getMimetype(filePath.substring(extIndex + 1));
        }
        
        // setup file reader and stream
        FileContentReader reader = new FileContentReader(file);
        reader.setMimetype(mimetype);
        reader.setEncoding("UTF-8");
        
        long lastModified = modifiedTime == null ? file.lastModified() : modifiedTime;
        Date lastModifiedDate = new Date(lastModified);
        
        streamContentImpl(req, res, reader, null, null, attach, lastModifiedDate, String.valueOf(lastModifiedDate.getTime()), attachFileName, model);
    }

    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req               Request
     * @param res               Response
     * @param nodeRef           The node reference
     * @param propertyQName     The content property name
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException 
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 NodeRef nodeRef, 
                                 QName propertyQName,
                                 boolean attach) throws IOException
    {
        streamContent(req, res, nodeRef, propertyQName, attach, null, null);
    }

    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req               Request
     * @param res               Response
     * @param nodeRef           The node reference
     * @param propertyQName     The content property name
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException 
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 NodeRef nodeRef, 
                                 QName propertyQName,
                                 boolean attach,
                                 Map<String, Object> model) throws IOException
    {
        streamContent(req, res, nodeRef, propertyQName, attach, null, model);
    }

    /**
     * Streams the content on a given node's content property to the response of the web script.
     * 
     * @param req               Request
     * @param res               Response
     * @param nodeRef           The node reference
     * @param propertyQName     The content property name
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException 
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 NodeRef nodeRef, 
                                 QName propertyQName,
                                 boolean attach, 
                                 String attachFileName) throws IOException
    {
        streamContent(req, res, nodeRef, propertyQName, attach, attachFileName, null);
    }

    /**
         * Streams the content on a given node's content property to the response of the web script.
         * 
         * @param req               Request
         * @param res               Response
         * @param nodeRef           The node reference
         * @param propertyQName     The content property name
         * @param attach            Indicates whether the content should be streamed as an attachment or not
         * @param attachFileName    Optional file name to use when attach is <code>true</code>
         * @throws IOException 
         */
        public void streamContent(WebScriptRequest req, 
                                     WebScriptResponse res, 
                                     NodeRef nodeRef, 
                                     QName propertyQName,
                                     boolean attach, 
                                     String attachFileName,
                                     Map<String, Object> model) throws IOException
        {
            if (logger.isDebugEnabled())
                logger.debug("Retrieving content from node ref " + nodeRef.toString() + " (property: " + propertyQName.toString() + ") (attach: " + attach + ")");
    
            // TODO 
            // This was commented out to accomadate records management permissions.  We need to review how we cope with this
            // hard coded permission checked.
            
            // check that the user has at least READ_CONTENT access - else redirect to the login page
    //        if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
    //        {
    //            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
    //        }
           
            // check If-Modified-Since header and set Last-Modified header as appropriate
            Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
            if (modified != null)
            {
                long modifiedSince = -1;
                String modifiedSinceStr = req.getHeader("If-Modified-Since");
                if (modifiedSinceStr != null)
                {
                    try
                    {
                        modifiedSince = dateFormat.parse(modifiedSinceStr).getTime();
                    }
                    catch (Throwable e)
                    {
                        if (logger.isInfoEnabled())
                            logger.info("Browser sent badly-formatted If-Modified-Since header: " + modifiedSinceStr);
                    }
                    
                    if (modifiedSince > 0L)
                    {
                        // round the date to the ignore millisecond value which is not supplied by header
                        long modDate = (modified.getTime() / 1000L) * 1000L;
                        if (modDate <= modifiedSince)
                        {
                            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            return;
                        }
                    }
                }
            }
    
            // get the content reader
            ContentReader reader = contentService.getReader(nodeRef, propertyQName);
            if (reader == null || !reader.exists())
            {
                throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to locate content for node ref " + nodeRef + " (property: " + propertyQName.toString() + ")");
            }
            
            // Stream the content
            streamContentImpl(req, res, reader, nodeRef, propertyQName, attach, modified, modified == null ? null : String.valueOf(modified.getTime()), attachFileName, model);
        }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The classpath resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 String resourcePath,
                                 boolean attach) throws IOException
    {
        streamContent(req, res, resourcePath, attach, null, null);
    }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The classpath resource path the content is required for
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @throws IOException
     */
    public void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 String resourcePath,
                                 boolean attach,
                                 Map<String, Object> model) throws IOException
    {
        streamContent(req, res, resourcePath, attach, null, model);
    }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The classpath resource path the content is required for.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    private void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 String resourcePath,
                                 boolean attach, 
                                 String attachFileName) throws IOException
    {
        streamContent(req, res, resourcePath, attach, attachFileName, null);
    }

    /**
     * Streams content back to client from a given resource path.
     * 
     * @param req               The request
     * @param res               The response
     * @param resourcePath      The classpath resource path the content is required for.
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    protected void streamContent(WebScriptRequest req, 
                                 WebScriptResponse res, 
                                 String resourcePath,
                                 boolean attach, 
                                 String attachFileName,
                                 Map<String, Object> model) throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving content from resource path " + resourcePath + " (attach: " + attach + ")");
        
        // get extension of resource
        String ext = "";
        int extIndex = resourcePath.lastIndexOf('.');
        if (extIndex != -1)
        {
            ext = resourcePath.substring(extIndex);
        }
        
        // We need to retrieve the modification date/time from the resource itself.
        StringBuilder sb = new StringBuilder("classpath:").append(resourcePath);
        final String classpathResource = sb.toString();
        
        long resourceLastModified = resourceLoader.getResource(classpathResource).lastModified();
        
        // create temporary file 
        File file = TempFileProvider.createTempFile("streamContent-", ext);
    
        InputStream is = resourceLoader.getResource(classpathResource).getInputStream();
        OutputStream os = new FileOutputStream(file);
        FileCopyUtils.copy(is, os);
        
        // stream the contents of the file, but using the modifiedDate of the original resource.
        streamContent(req, res, file, resourceLastModified, attach, attachFileName, model);
    }

    /**
     * Stream content implementation
     * 
     * @param req               The request
     * @param res               The response
     * @param reader            The reader
     * @param nodeRef           The content nodeRef if applicable
     * @param propertyQName     The content property if applicable
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param modified          Modified date of content
     * @param eTag              ETag to use
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    private void streamContentImpl(WebScriptRequest req, 
                                    WebScriptResponse res, 
                                    ContentReader reader, 
                                    NodeRef nodeRef,
                                    QName propertyQName,
                                    boolean attach,
                                    Date modified, 
                                    String eTag, 
                                    String attachFileName) throws IOException
    {
        streamContentImpl(req, res, reader, nodeRef, propertyQName, attach, modified, eTag, attachFileName, null);
    }

    /**
     * Stream content implementation
     * 
     * @param req               The request
     * @param res               The response
     * @param reader            The reader
     * @param nodeRef           The content nodeRef if applicable
     * @param propertyQName     The content property if applicable
     * @param attach            Indicates whether the content should be streamed as an attachment or not
     * @param modified          Modified date of content
     * @param eTag              ETag to use
     * @param attachFileName    Optional file name to use when attach is <code>true</code>
     * @throws IOException
     */
    public void streamContentImpl(WebScriptRequest req, 
                                    WebScriptResponse res, 
                                    ContentReader reader, 
                                    NodeRef nodeRef,
                                    QName propertyQName,
                                    boolean attach,
                                    Date modified, 
                                    String eTag, 
                                    String attachFileName, 
                                    Map<String, Object> model) throws IOException
    {
        setAttachment(res, attach, attachFileName);
    
        // establish mimetype
        String mimetype = reader.getMimetype();
        String extensionPath = req.getExtensionPath();
        if (mimetype == null || mimetype.length() == 0)
        {
            mimetype = MimetypeMap.MIMETYPE_BINARY;
            int extIndex = extensionPath.lastIndexOf('.');
            if (extIndex != -1)
            {
                String ext = extensionPath.substring(extIndex + 1);
                mimetype = mimetypeService.getMimetype(ext);
            }
        }
    
        res.setHeader(HEADER_ACCEPT_RANGES, "bytes");
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
                  if (range.indexOf(',') != -1 && (nodeRef == null || propertyQName == null))
                  {
                       if (logger.isInfoEnabled())
                           logger.info("Multi-range only supported for nodeRefs");
                  }
                  else {
                      HttpRangeProcessor rangeProcessor = new HttpRangeProcessor(contentService);
                      processedRange = rangeProcessor.processRange(
                            res, reader, range.substring(6), nodeRef, propertyQName,
                            mimetype, req.getHeader(HEADER_USER_AGENT));
                  }
               }
            }
            if (processedRange == false)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Sending complete file content...");
               
               // set mimetype for the content and the character encoding for the stream
               res.setContentType(mimetype);
               res.setContentEncoding(reader.getEncoding());
               
               // return the complete entity range
               long size = reader.getSize();
               res.setHeader(HEADER_CONTENT_RANGE, "bytes 0-" + Long.toString(size-1L) + "/" + Long.toString(size));
               res.setHeader(HEADER_CONTENT_LENGTH, Long.toString(size));
               
               // set caching
               setResponseCache(res, modified, eTag, model);
               
               // get the content and stream directly to the response output stream
               // assuming the repository is capable of streaming in chunks, this should allow large files
               // to be streamed directly to the browser response stream.
               reader.getContent( res.getOutputStream() );
            }
        }
        catch (SocketException e1)
        {
            // the client cut the connection - our mission was accomplished apart from a little error message
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent: " + reader);
        }
        catch (ContentIOException e2)
        {
            if (logger.isInfoEnabled())
                logger.info("Client aborted stream read:\n\tcontent: " + reader);
        }
    }

    /**
     * Set attachment header
     * 
     * @param res
     * @param attach
     * @param attachFileName
     */
    public void setAttachment(WebScriptResponse res, boolean attach, String attachFileName)
    {
        if (attach == true)
        {
            String headerValue = "attachment";
            if (attachFileName != null && attachFileName.length() > 0)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Attaching content using filename: " + attachFileName);
                
                headerValue += "; filename*=UTF-8''" + WebDAVHelper.encodeURL(attachFileName) + "; filename=\"" + attachFileName + "\"";
            }
            
            // set header based on filename - will force a Save As from the browse if it doesn't recognize it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", headerValue);
        }
    }

    /**
     * Set the cache settings on the response
     * 
     * @param res
     * @param modified
     * @param eTag
     */
    protected void setResponseCache(WebScriptResponse res, Date modified, String eTag, Map<String, Object> model)
    {
        Cache cache = new Cache();
        if (model == null || model.get("allowBrowserToCache") == null || ((String)model.get("allowBrowserToCache")).equals("false"))
        {
            cache.setNeverCache(false);
            cache.setMustRevalidate(true);
            cache.setMaxAge(0L);
            cache.setLastModified(modified);
            cache.setETag(eTag);
        }
        else
        {
            cache.setNeverCache(false);
            cache.setMustRevalidate(false);
            cache.setMaxAge(new Long(31536000));
            cache.setLastModified(modified);
            cache.setETag(eTag);
            res.setCache(cache);
        }
        res.setCache(cache);
    }

}
