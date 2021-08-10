/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Description.FormatStyle;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WrappingWebScriptRequest;
import org.springframework.util.FileCopyUtils;

public class BufferedRequest implements WrappingWebScriptRequest, AutoCloseable
{
    private final Supplier<TempOutputStream> streamFactory;
    private final WebScriptRequest req;
    private TempOutputStream bufferStream;
    private InputStream contentStream;
    private BufferedReader contentReader;
    
    public BufferedRequest(WebScriptRequest req, Supplier<TempOutputStream> streamFactory)
    {
        this.req = req;
        this.streamFactory = streamFactory;
    }

    private TempOutputStream getBufferedBodyAsTempStream() throws IOException
    {
        if (bufferStream == null)
        {
            bufferStream = streamFactory.get();

            try
            {
                // Copy the stream
                FileCopyUtils.copy(req.getContent().getInputStream(), bufferStream);
            }
            catch (IOException e)
            {
                bufferStream.destroy();
                throw e;
            }
        }

        return bufferStream;
    }

    private InputStream bufferInputStream() throws IOException
    {
        if (contentReader != null)
        {
            throw new IllegalStateException("Reader in use");
        }
        if (contentStream == null)
        {
            contentStream = getBufferedBodyAsTempStream().toNewInputStream();
        }

        return contentStream;
    }

    public void reset()
    {
        if (contentStream != null)
        {
            try
            {
                contentStream.close();
            }
            catch (Exception ignore)
            {
            }
            contentStream = null;
        }
        if (contentReader != null)
        {
            try
            {
                contentReader.close();
            }
            catch (Exception ignore)
            {
            }
            contentReader = null;
        }
    }

    @Override
    public void close()
    {
        reset();
        if (bufferStream != null)
        {
            try
            {
                bufferStream.destroy();
            }
            catch (Exception ignore)
            {
            }
            bufferStream = null;
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WrappingWebScriptRequest#getNext()
     */
    @Override
    public WebScriptRequest getNext()
    {
        return req;
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#forceSuccessStatus()
     */
    @Override
    public boolean forceSuccessStatus()
    {
        return req.forceSuccessStatus();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getAgent()
     */
    @Override
    public String getAgent()
    {
        return req.getAgent();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getContent()
     */
    @Override
    public Content getContent()
    {
        final Content wrapped = req.getContent();
        return new Content(){

            @Override
            public String getContent() throws IOException
            {
                return wrapped.getContent();
            }

            @Override
            public String getEncoding()
            {
                return wrapped.getEncoding();
            }

            @Override
            public String getMimetype()
            {
                return wrapped.getMimetype();
            }


            @Override
            public long getSize()
            {
                return wrapped.getSize();
            }
     
            @Override
            public InputStream getInputStream()
            {
                if (BufferedRequest.this.contentReader != null)
                {
                    throw new IllegalStateException("Reader in use");
                }
                if (BufferedRequest.this.contentStream == null)
                {
                    try
                    {
                        BufferedRequest.this.contentStream = bufferInputStream();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                return BufferedRequest.this.contentStream;
            }

            @Override
            public BufferedReader getReader() throws IOException
            {
                if (BufferedRequest.this.contentStream != null)
                {
                    throw new IllegalStateException("Input Stream in use");
                }
                if (BufferedRequest.this.contentReader == null)
                {
                    String encoding = wrapped.getEncoding();
                    InputStream in = bufferInputStream();
                    BufferedRequest.this.contentReader = new BufferedReader(new InputStreamReader(in, encoding == null ? "ISO-8859-1" : encoding));
                }
                return BufferedRequest.this.contentReader;
            }
        };
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getContentType()
     */
    @Override
    public String getContentType()
    {
        return req.getContentType();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getContextPath()
     */
    @Override
    public String getContextPath()
    {
        return req.getContextPath();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getExtensionPath()
     */
    @Override
    public String getExtensionPath()
    {
        return req.getExtensionPath();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getFormat()
     */
    @Override
    public String getFormat()
    {
        return req.getFormat();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getFormatStyle()
     */
    @Override
    public FormatStyle getFormatStyle()
    {
        return req.getFormatStyle();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String name)
    {
        return req.getHeader(name);
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getHeaderNames()
     */
    @Override
    public String[] getHeaderNames()
    {
        return req.getHeaderNames();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getHeaderValues(java.lang.String)
     */
    @Override
    public String[] getHeaderValues(String name)
    {
        return req.getHeaderValues(name);
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getJSONCallback()
     */
    @Override
    public String getJSONCallback()
    {
        return req.getJSONCallback();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name)
    {
        return req.getParameter(name);
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getParameterNames()
     */
    @Override
    public String[] getParameterNames()
    {
        return req.getParameterNames();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name)
    {
        return req.getParameterValues(name);
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getPathInfo()
     */
    @Override
    public String getPathInfo()
    {
        return req.getPathInfo();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getQueryString()
     */
    @Override
    public String getQueryString()
    {
        return req.getQueryString();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getRuntime()
     */
    @Override
    public Runtime getRuntime()
    {
        return req.getRuntime();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getServerPath()
     */
    @Override
    public String getServerPath()
    {
        return req.getServerPath();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getServiceContextPath()
     */
    @Override
    public String getServiceContextPath()
    {
        return req.getServiceContextPath();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getServiceMatch()
     */
    @Override
    public Match getServiceMatch()
    {
        return req.getServiceMatch();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getServicePath()
     */
    @Override
    public String getServicePath()
    {
        return req.getServicePath();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#getURL()
     */
    @Override
    public String getURL()
    {
        return req.getURL();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#isGuest()
     */
    @Override
    public boolean isGuest()
    {
        return req.isGuest();
    }
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.WebScriptRequest#parseContent()
     */
    @Override
    public Object parseContent()
    {
        return req.parseContent();
    }
}
