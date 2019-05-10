/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.chemistry.opencmis.commons.server.TempStoreOutputStream;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.StringBuilderWriter;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * Transactional Buffered Response
 */
public class BufferedResponse implements WrappingWebScriptResponse
{
    // Logger
    protected static final Log logger = LogFactory.getLog(BufferedResponse.class);

    private TempStoreOutputStreamFactory streamFactory;
    private WebScriptResponse res;
    private int bufferSize;
    private TempStoreOutputStream outputStream = null;
    private StringBuilderWriter outputWriter = null;
    

    /**
     * Construct
     * 
     * @param res WebScriptResponse
     * @param bufferSize int
     */
    public BufferedResponse(WebScriptResponse res, int bufferSize, TempStoreOutputStreamFactory streamFactory)
    {
        this.res = res;
        this.bufferSize = bufferSize;
        this.streamFactory = streamFactory;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WrappingWebScriptResponse#getNext()
     */
    public WebScriptResponse getNext()
    {
        return res;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String name, String value)
    {
        res.addHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#encodeScriptUrl(java.lang.String)
     */
    public String encodeScriptUrl(String url)
    {
        return res.encodeScriptUrl(url);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getEncodeScriptUrlFunction(java.lang.String)
     */
    public String getEncodeScriptUrlFunction(String name)
    {
        return res.getEncodeScriptUrlFunction(name);
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptResponse#encodeResourceUrl(java.lang.String)
     */
    public String encodeResourceUrl(String url)
    {
        return res.encodeResourceUrl(url);
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptResponse#getEncodeResourceUrlFunction(java.lang.String)
     */
    public String getEncodeResourceUrlFunction(String name)
    {
        return res.getEncodeResourceUrlFunction(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException
    {
        if (outputStream == null)
        {
            if (outputWriter != null)
            {
                throw new AlfrescoRuntimeException("Already buffering output writer");
            }
            outputStream = streamFactory.newOutputStream();
        }
        return outputStream;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getRuntime()
     */
    public Runtime getRuntime()
    {
        return res.getRuntime();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#getWriter()
     */
    public Writer getWriter() throws IOException
    {
        if (outputWriter == null)
        {
            if (outputStream != null)
            {
                throw new AlfrescoRuntimeException("Already buffering output stream");
            }
            outputWriter = new StringBuilderWriter(bufferSize);
        }
        return outputWriter;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#reset()
     */
    public void reset()
    {
        if (outputStream != null)
        {
            outputStream = null;
        }
        else if (outputWriter != null)
        {
            outputWriter = null;
        }
        res.reset();
    }

    /* (non-Javadoc)
     * @see org./alfresco.web.scripts.WebScriptResponse#resetjava.lang.String)
     */
    public void reset(String preserveHeadersPattern)
    {
        if (outputStream != null)
        {
            outputStream = null;
        }
        else if (outputWriter != null)
        {
            outputWriter = null;
        }
        res.reset(preserveHeadersPattern);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setCache(org.alfresco.web.scripts.Cache)
     */
    public void setCache(Cache cache)
    {
        res.setCache(cache);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setContentType(java.lang.String)
     */
    public void setContentType(String contentType)
    {
        res.setContentType(contentType);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setContentEncoding(java.lang.String)
     */
    public void setContentEncoding(String contentEncoding)
    {
        res.setContentEncoding(contentEncoding);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String value)
    {
        res.setHeader(name, value);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.WebScriptResponse#setStatus(int)
     */
    public void setStatus(int status)
    {
        res.setStatus(status);
    }

    /**
     * Write buffered response to underlying response
     */
    public void writeResponse()
    {
        try
        {
            if (logger.isDebugEnabled() && outputStream != null)
            {
                logger.debug("Writing Transactional response: size=" + outputStream.getLength());
            }
            
            if (outputWriter != null)
            {
                outputWriter.flush();
                res.getWriter().write(outputWriter.toString());
            }
            else if (outputStream != null)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Writing Transactional response: size=" + outputStream.getLength());
                
                outputStream.flush();
                FileCopyUtils.copy(outputStream.getInputStream(), res.getOutputStream());
            }
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("Failed to commit buffered response", e);
        }
    }
}
