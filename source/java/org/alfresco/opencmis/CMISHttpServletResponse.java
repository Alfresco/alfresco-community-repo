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
package org.alfresco.opencmis;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * Wraps an OpenCMIS HttpServletResponse for specific mapping to the Alfresco implementation of OpenCMIS.
 * 
 * @author janv
 */
public class CMISHttpServletResponse implements HttpServletResponse
{
    protected HttpServletResponse httpResp;

    protected Set<String> nonAttachContentTypes = Collections.emptySet(); // pre-configured whitelist, eg. images & pdf

    private final static String HDR_CONTENT_DISPOSITION = "Content-Disposition";
    
	public CMISHttpServletResponse(WebScriptResponse res, Set<String> nonAttachContentTypes)
	{
		httpResp = WebScriptServletRuntime.getHttpServletResponse(res);
        this.nonAttachContentTypes = nonAttachContentTypes;
	}

    @Override
    public void addCookie(Cookie cookie)
    {
        httpResp.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name)
    {
        return httpResp.containsHeader(name);
    }

    @Override
    public String encodeURL(String url)
    {
        return httpResp.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url)
    {
        return httpResp.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url)
    {
        return encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(String url)
    {
        return httpResp.encodeRedirectUrl(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException
    {
        httpResp.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException
    {
        httpResp.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException
    {
        httpResp.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date)
    {
        httpResp.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date)
    {
        httpResp.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value)
    {
        httpResp.setHeader(name, getStringHeaderValue(name, value, httpResp.getContentType()));
    }

    @Override
    public void addHeader(String name, String value)
    {
        httpResp.addHeader(name, getStringHeaderValue(name, value, httpResp.getContentType()));
    }

    private String getStringHeaderValue(String name, String value, String contentType)
    {
        if (HDR_CONTENT_DISPOSITION.equals(name))
        {
            if (! nonAttachContentTypes.contains(contentType))
            {
                if (value.startsWith("inline"))
                {
                    // force attachment
                    value = value.replace("inline", "attachment");
                }
                else if (! value.startsWith("attachment"))
                {
                    throw new AlfrescoRuntimeException("Unexpected - attachment header could not be set: "+name+" = "+value);
                }
            }
        }

        return value;
    }

    @Override
    public void setIntHeader(String name, int value)
    {
        httpResp.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value)
    {
        httpResp.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc)
    {
        httpResp.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm)
    {
        httpResp.setStatus(sc, sm);
    }

    @Override
    public int getStatus()
    {
        return httpResp.getStatus();
    }

    @Override
    public String getHeader(String name)
    {
        return httpResp.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name)
    {
        return httpResp.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return httpResp.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding()
    {
        return httpResp.getCharacterEncoding();
    }

    @Override
    public String getContentType()
    {
        return httpResp.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        return httpResp.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return httpResp.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset)
    {
        httpResp.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len)
    {
        httpResp.setContentLength(len);
    }

    @Override
    public void setContentType(String type)
    {
        httpResp.setContentType(type);
    }

    @Override
    public void setBufferSize(int size)
    {
        httpResp.setBufferSize(size);
    }

    @Override
    public int getBufferSize()
    {
        return httpResp.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException
    {
        httpResp.flushBuffer();
    }

    @Override
    public void resetBuffer()
    {
        httpResp.resetBuffer();
    }

    @Override
    public boolean isCommitted()
    {
        return httpResp.isCommitted();
    }

    @Override
    public void reset()
    {
        httpResp.reset();
    }

    @Override
    public void setLocale(Locale loc)
    {
        httpResp.setLocale(loc);
    }

    @Override
    public Locale getLocale()
    {
        return httpResp.getLocale();
    }
}