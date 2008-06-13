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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.SocketException;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.servlet.WebScriptServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AVM Remote Store service.
 * 
 * @see BaseRemoteStore for API methods.
 * 
 * @author Kevin Roast
 */
public class AVMRemoteStore extends BaseRemoteStore
{
    private static final Log logger = LogFactory.getLog(AVMRemoteStore.class);
    
    private String rootPath; 
    private AVMService avmService;
    
    
    /**
     * @param rootPath  the root path under which to process store requests
     */
    public void setRootPath(String rootPath)
    {
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
     * Gets the last modified timestamp for the document.
     * 
     * @param path  document path to an existing document
     */
    @Override
    protected void lastModified(WebScriptResponse res, String path)
        throws IOException
    {
        String avmPath = buildAVMPath(path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            throw new WebScriptException("Unable to locate AVM file: " + avmPath);
        }
        
        Writer out = res.getWriter();
        out.write(Long.toString(desc.getModDate()));
        out.close();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#getDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String)
     */
    @Override
    protected void getDocument(WebScriptResponse res, String path) throws IOException
    {
        String avmPath = buildAVMPath(path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            throw new WebScriptException("Unable to locate file: " + avmPath);
        }
        
        ContentReader reader;
        try
        {
            reader = this.avmService.getContentReader(-1, avmPath);
            
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
                    String mt = this.mimetypeService.getMimetypesByExtension().get(ext);
                    if (mt != null)
                    {
                        mimetype = mt;
                    }
                }
            }
    
            // set mimetype for the content and the character encoding + length for the stream
            WebScriptServletResponse httpRes = (WebScriptServletResponse)res;
            httpRes.setContentType(mimetype);
            httpRes.getHttpServletResponse().setCharacterEncoding(reader.getEncoding());
            httpRes.getHttpServletResponse().setDateHeader("Last-Modified", desc.getModDate());
            httpRes.setHeader("Content-Length", Long.toString(reader.getSize()));
            
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
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#hasDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String)
     */
    @Override
    protected void hasDocument(WebScriptResponse res, String path) throws IOException
    {
        String avmPath = buildAVMPath(path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        
        Writer out = res.getWriter();
        out.write(Boolean.toString(desc != null));
        out.close();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#createDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.io.InputStream)
     */
    @Override
    protected void createDocument(WebScriptResponse res, String path, InputStream content)
    {
        String avmPath = buildAVMPath(path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc != null)
        {
            throw new WebScriptException("Unable to create, file already exists: " + avmPath);
        }
        
        String[] parts = AVMNodeConverter.SplitBase(avmPath);
        try
        {
            this.avmService.createFile(parts[0], parts[1], content);
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.bean.BaseRemoteStore#updateDocument(org.alfresco.web.scripts.WebScriptResponse, java.lang.String, java.io.InputStream)
     */
    @Override
    protected void updateDocument(WebScriptResponse res, String path, InputStream content)
    {
        String avmPath = buildAVMPath(path);
        AVMNodeDescriptor desc = this.avmService.lookup(-1, avmPath);
        if (desc == null)
        {
            throw new WebScriptException("Unable to locate file for update: " + avmPath);
        }
        
        try
        {
            ContentWriter writer = this.avmService.getContentWriter(avmPath);
            writer.putContent(content);
        }
        catch (AccessDeniedException ae)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
    }
    
    /**
     * @param path      root path relative
     * 
     * @return full AVM path to document including store and root path components
     */
    private String buildAVMPath(String path)
    {
        return this.store + ":/" + this.rootPath + "/" + path;
    }
}
