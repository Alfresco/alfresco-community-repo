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
package org.alfresco.repo.web.scripts.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * AVM Remote Store service.
 * 
 * @see BaseRemoteStore for the available API methods.
 * 
 * @author Kevin Roast
 */
public class AVMRemoteStore extends BaseRemoteStore
{
    private static final Log logger = LogFactory.getLog(AVMRemoteStore.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static ThreadLocal<Transformer> transformer = new ThreadLocal<Transformer>(){

        /* (non-Javadoc)
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected Transformer initialValue()
        {
            try
            {
                return TRANSFORMER_FACTORY.newTransformer();
            }
            catch (TransformerConfigurationException e)
            {
                throw new RuntimeException(e);
            }
        }        
        
    };
    private String rootPath = "/"; 
    private AVMService avmService;
    private SearchService searchService;
    
    
    /**
     * @param rootPath  the root path under which to process store requests
     */
    public void setRootPath(String rootPath)
    {
        if (rootPath == null || rootPath.length() == 0)
        {
            throw new IllegalArgumentException("Root path must be specified.");
        }
        
        this.rootPath = rootPath;
    }

    /**
     * @param avmService        the AVMService to set
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * @param searchService     the SearchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Gets the last modified timestamp for the document.
     * 
     * @param store the store id
     * @param path  document path to an existing document
     */
    @Override
    protected void lastModified(WebScriptResponse res, String store, String path)
        throws IOException
    {
        String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            throw new WebScriptException("Unable to locate AVM file: " + avmPath);
        }
        
        Writer out = res.getWriter();
        out.write(Long.toString(desc.getModDate()));
        out.close();
        
        if (logger.isDebugEnabled())
            logger.debug("AVMRemoteStore.lastModified() " + Long.toString(desc.getModDate()));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#getDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String)
     */
    @Override
    protected void getDocument(final WebScriptResponse res, final String store, final String path) throws IOException
    {
        final String avmPath = buildAVMPath(store, path);
        final AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                ContentReader reader;
                try
                {
                    reader = avmService.getContentReader(-1, avmPath);
                    
                    if (reader == null)
                    {
                        throw new WebScriptException("No content found for AVM file: " + avmPath);
                    }
                    
                    // establish mimetype
                    String mimetype = reader.getMimetype();
                    if (mimetype == null || mimetype.length() == 0)
                    {
                        mimetype = MimetypeMap.MIMETYPE_BINARY;
                        int extIndex = path.lastIndexOf('.');
                        if (extIndex != -1)
                        {
                            String ext = path.substring(extIndex + 1);
                            mimetype = mimetypeService.getMimetype(ext);
                        }
                    }
                    
                    // set mimetype for the content and the character encoding + length for the stream
                    res.setContentType(mimetype);
                    res.setContentEncoding(reader.getEncoding());
                    res.setHeader("Last-Modified", Long.toString(desc.getModDate()));
                    res.setHeader("Content-Length", Long.toString(reader.getSize()));
                    
                    if (logger.isDebugEnabled())
                        logger.debug("AVMRemoteStore.getDocument() " + mimetype + " of size: " + reader.getSize());
                    
                    // get the content and stream directly to the response output stream
                    // assuming the repository is capable of streaming in chunks, this should allow large files
                    // to be streamed directly to the browser response stream.
                    try
                    {
                        reader.getContent(res.getOutputStream());
                    }
                    catch (SocketException e1)
                    {
                        // the client cut the connection - our mission was accomplished apart from a little error message
                        if (logger.isInfoEnabled())
                            logger.info("Client aborted stream read:\n\tnode: " + avmPath + "\n\tcontent: " + reader);
                    }
                    catch (ContentIOException e2)
                    {
                        if (logger.isInfoEnabled())
                            logger.info("Client aborted stream read:\n\tnode: " + avmPath + "\n\tcontent: " + reader);
                    }
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                catch (AVMNotFoundException avmErr)
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#hasDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String)
     */
    @Override
    protected void hasDocument(WebScriptResponse res, String store, String path) throws IOException
    {
        String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        
        Writer out = res.getWriter();
        out.write(Boolean.toString(desc != null));
        out.close();
        
        if (logger.isDebugEnabled())
            logger.debug("AVMRemoteStore.hasDocument() " + Boolean.toString(desc != null));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#createDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.io.InputStream)
     */
    @Override
    protected void createDocument(final WebScriptResponse res, final String store, final String path, final InputStream content)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                String avmPath = buildAVMPath(store, path);
                try
                {
                    String[] parts = AVMNodeConverter.SplitBase(avmPath);
                    String[] dirs = parts[0].split("/");
                    String parentPath =  dirs[0] + "/" + dirs[1];
                    int index = 2;
                    while (index < dirs.length)
                    {
                        String dirPath = parentPath + "/" + dirs[index];
                        if (avmService.lookup(-1, dirPath) == null)
                        {
                            avmService.createDirectory(parentPath, dirs[index]);
                        }
                        parentPath = dirPath;
                        index++;
                    }
                    
                    avmService.createFile(parts[0], parts[1], content);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("AVMRemoteStore.createDocument() " + avmPath + " of size: " + avmService.lookup(-1, avmPath).getLength());
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                catch (AVMExistsException avmErr)
                {
                    res.setStatus(Status.STATUS_CONFLICT);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#createDocuments(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.io.InputStream)
     */
    @Override
    protected void createDocuments(final WebScriptResponse res, final String store, final InputStream in)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                try
                {
                    Set<String> checkedPaths = new HashSet<String>(19);
                    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
                    Document document = documentBuilder.parse(in);
                    Element docEl = document.getDocumentElement();
                    Transformer transformer = AVMRemoteStore.this.transformer.get();
                    for (Node n = docEl.getFirstChild(); n != null; n = n.getNextSibling())
                    {
                        if (!(n instanceof Element))
                        {
                            continue;
                        }
                        String avmPath = buildAVMPath(store, ((Element) n).getAttribute("path"));
                        String[] parts = AVMNodeConverter.SplitBase(avmPath);
                        String[] dirs = parts[0].split("/");
                        String parentPath = dirs[0] + "/" + dirs[1];
                        int index = 2;
                        while (index < dirs.length)
                        {
                            String dirPath = parentPath + "/" + dirs[index];
                            if (!checkedPaths.contains(dirPath))
                            {
                               if (avmService.lookup(-1, dirPath) == null)
                               {
                                   avmService.createDirectory(parentPath, dirs[index]);
                               }
                               checkedPaths.add(dirPath);
                            }
                            parentPath = dirPath;
                            index++;
                        }

                        // Turn the first element child into a document
                        Document content = documentBuilder.newDocument();
                        Node child;
                        for (child = n.getFirstChild(); child != null ; child=child.getNextSibling())
                        {
                           if (child instanceof Element)
                           {
                               content.appendChild(content.importNode(child, true));
                               break;
                           }
                        }
                        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                        transformer.transform(new DOMSource(content), new StreamResult(out));
                        out.close();
                        avmService.createFile(parts[0], parts[1], new ByteArrayInputStream(out.toByteArray()));

                        if (logger.isDebugEnabled())
                            logger.debug("AVMRemoteStore.createDocument() " + avmPath + " of size: "
                                    + avmService.lookup(-1, avmPath).getLength());
                    }
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                catch (AVMExistsException avmErr)
                {
                    res.setStatus(Status.STATUS_CONFLICT);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#updateDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.io.InputStream)
     */
    @Override
    protected void updateDocument(final WebScriptResponse res, final String store, final String path, final InputStream content)
    {
        final String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                try
                {
                    ContentWriter writer = avmService.getContentWriter(avmPath, true);
                    writer.putContent(content);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("AVMRemoteStore.updateDocument() " + avmPath + " of size: " + avmService.lookup(-1, avmPath).getLength());
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#deleteDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String)
     */
    @Override
    protected void deleteDocument(final WebScriptResponse res, final String store, final String path)
    {
        final String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @SuppressWarnings("synthetic-access")
            public Object doWork() throws Exception
            {
                try
                {
                    avmService.removeNode(avmPath);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("AVMRemoteStore.deleteDocument() " + avmPath);
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#listDocuments(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, boolean)
     */
    @Override
    protected void listDocuments(WebScriptResponse res, String store, String path, boolean recurse) throws IOException
    {
        String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor node = this.avmService.lookup(-1, avmPath);
        if (node == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        try
        {
            traverseNode(res.getWriter(), store, node, recurse);
            if (logger.isDebugEnabled())
                logger.debug("AVMRemoteStore.listDocuments() " + path + " Recursive: " + recurse);
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
        finally
        {
            res.getWriter().close();
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#listDocuments(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.lang.String)
     */
    @Override
    protected void listDocuments(WebScriptResponse res, final String store, String path, String pattern) throws IOException
    {
        String avmPath = buildAVMPath(store, path);
        AVMNodeDescriptor node = this.avmService.lookup(-1, avmPath);
        if (node == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        if (pattern == null || pattern.length() == 0)
        {
            pattern = "*";
        }
        
        try
        {
            final Writer out = res.getWriter();
            int cropPoint = store.length() + this.rootPath.length() + 1;
            // need to ensure match pattern is AVM file path encoded - but don't encode * character!
            StringBuilder buf = new StringBuilder(pattern.length() + 8);
            for (StringTokenizer t = new StringTokenizer(pattern, "*"); t.hasMoreTokens(); /**/)
            {
                buf.append(encodePath(t.nextToken()));
                if (t.hasMoreTokens())
                {
                    buf.append('*');
                }
            }
            // ensure the escape character is itself escaped
            String encpattern = buf.toString().replace("\\", "\\\\");
            boolean encoded = (encpattern.length() != pattern.length());
            SortedMap<String, AVMNodeDescriptor> listing = this.avmService.getDirectoryListing(node, encpattern);
            for (AVMNodeDescriptor n : listing.values())
            {
                if (n.isFile())
                {
                    String p = n.getPath().substring(cropPoint);
                    out.write(encoded ? URLDecoder.decode(p) : p);
                    out.write("\n");
                }
            }
            
            if (logger.isDebugEnabled())
                logger.debug("AVMRemoteStore.listDocuments() " + path + " Pattern: " + pattern);
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
        finally
        {
            res.getWriter().close();
        }
    }
    
    /**
     * @param store     the AVM store id
     * @param path      root path relative
     * 
     * @return full AVM path to document including store and root path components
     */
    private String buildAVMPath(String store, String path)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }
        return store + ":" + this.rootPath + (path != null ? encodePath(path) : "");
    }
    
    /**
     * Traverse a Node and recursively output the file paths it contains.
     * 
     * @param out       Writer for output - relative paths separated by newline characters
     * @param store     AVM Store name
     * @param node      The AVM Node to traverse
     * @param recurse   True to recurse sub-directories  
     * 
     * @throws IOException
     */
    private void traverseNode(Writer out, String store, AVMNodeDescriptor node, boolean recurse)
        throws IOException
    {
        /**
         * The node path appears as such:
         * project1:/www/avm_webapps/ROOT/WEB-INF/classes/alfresco/site-data/template-instances/file.xml
         */
        int cropPoint = store.length() + this.rootPath.length() + 1;
        SortedMap<String, AVMNodeDescriptor> listing = this.avmService.getDirectoryListing(node);
        for (AVMNodeDescriptor n : listing.values())
        {
            if (n.isFile())
            {
                out.write(n.getPath().substring(cropPoint));
                out.write("\n");
            }
            else if (recurse && n.isDirectory())
            {
                traverseNode(out, store, n, recurse);
            }
        }
    }
    
    
    private static String encodePath(final String s)
    {
        StringBuilder sb = null;      //create on demand
        char ch;
        final int len = s.length();
        for (int i = 0; i < len; i++)
        {
            ch = s.charAt(i);
            
            if (('A' <= ch && ch <= 'Z') ||             // 'A'..'Z'
                ('a' <= ch && ch <= 'z') ||             // 'a'..'z'
                ('0' <= ch && ch <= '9') ||             // '0'..'9'
                ch == '/' ||
                ch == '\'' || ch == ' ' ||
                ch == '.' || ch == '~' ||
                ch == '-' || ch == '_' ||
                ch == '@' || ch == '!' ||
                ch == '(' || ch == ')' ||
                ch == ';' || ch == ',' ||
                ch == '+' || ch == '$')
            {
                if (sb != null)
                {
                    sb.append(ch);
                }
            }
            else if ((int)ch <= 0x007f)                 // other ASCII
            {
                if (sb == null)
                {
                    final String soFar = s.substring(0, i);
                    sb = new StringBuilder(len + 16);
                    sb.append(soFar);
                }
                sb.append(hex[ch]);
            }
            else if ((int)ch <= 0x07FF)                 // non-ASCII <= 0x7FF
            {
                if (sb == null)
                {
                    final String soFar = s.substring(0, i);
                    sb = new StringBuilder(len + 16);
                    sb.append(soFar);
                }
                sb.append(hex[0xc0 | (ch >> 6)]);
                sb.append(hex[0x80 | (ch & 0x3F)]);
            }
            else                                        // 0x7FF < ch <= 0xFFFF
            {
                if (sb == null)
                {
                    final String soFar = s.substring(0, i);
                    sb = new StringBuilder(len + 16);
                    sb.append(soFar);
                }
                sb.append(hex[0xe0 | (ch >> 12)]);
                sb.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
                sb.append(hex[0x80 | (ch & 0x3F)]);
            }
        }
        return (sb != null ? sb.toString() : s);
    }
    
    private final static String[] hex = {
        "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
        "%08", "%09", "%0a", "%0b", "%0c", "%0d", "%0e", "%0f",
        "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
        "%18", "%19", "%1a", "%1b", "%1c", "%1d", "%1e", "%1f",
        "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
        "%28", "%29", "%2a", "%2b", "%2c", "%2d", "%2e", "%2f",
        "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
        "%38", "%39", "%3a", "%3b", "%3c", "%3d", "%3e", "%3f",
        "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
        "%48", "%49", "%4a", "%4b", "%4c", "%4d", "%4e", "%4f",
        "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
        "%58", "%59", "%5a", "%5b", "%5c", "%5d", "%5e", "%5f",
        "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
        "%68", "%69", "%6a", "%6b", "%6c", "%6d", "%6e", "%6f",
        "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
        "%78", "%79", "%7a", "%7b", "%7c", "%7d", "%7e", "%7f",
        "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
        "%88", "%89", "%8a", "%8b", "%8c", "%8d", "%8e", "%8f",
        "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
        "%98", "%99", "%9a", "%9b", "%9c", "%9d", "%9e", "%9f",
        "%a0", "%a1", "%a2", "%a3", "%a4", "%a5", "%a6", "%a7",
        "%a8", "%a9", "%aa", "%ab", "%ac", "%ad", "%ae", "%af",
        "%b0", "%b1", "%b2", "%b3", "%b4", "%b5", "%b6", "%b7",
        "%b8", "%b9", "%ba", "%bb", "%bc", "%bd", "%be", "%bf",
        "%c0", "%c1", "%c2", "%c3", "%c4", "%c5", "%c6", "%c7",
        "%c8", "%c9", "%ca", "%cb", "%cc", "%cd", "%ce", "%cf",
        "%d0", "%d1", "%d2", "%d3", "%d4", "%d5", "%d6", "%d7",
        "%d8", "%d9", "%da", "%db", "%dc", "%dd", "%de", "%df",
        "%e0", "%e1", "%e2", "%e3", "%e4", "%e5", "%e6", "%e7",
        "%e8", "%e9", "%ea", "%eb", "%ec", "%ed", "%ee", "%ef",
        "%f0", "%f1", "%f2", "%f3", "%f4", "%f5", "%f6", "%f7",
        "%f8", "%f9", "%fa", "%fb", "%fc", "%fd", "%fe", "%ff"
    };
}
