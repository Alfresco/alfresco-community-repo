/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
    private static ThreadLocal<Transformer> transformer = new ThreadLocal<Transformer>()
    {
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
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
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
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
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
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
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
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
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
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
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
        res.setContentType("text/plain;charset=UTF-8");
        
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
        res.setContentType("text/plain;charset=UTF-8");
        
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
}
