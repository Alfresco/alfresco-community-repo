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

import jakarta.servlet.http.HttpServletResponseWrapper;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

import java.util.Collections;
import java.util.Set;

/**
 * Wraps an OpenCMIS HttpServletResponse for specific mapping to the Alfresco implementation of OpenCMIS.
 * 
 * @author janv
 */
public class CMISHttpServletResponse extends HttpServletResponseWrapper
{
    protected Set<String> nonAttachContentTypes = Collections.emptySet(); // pre-configured whitelist, eg. images & pdf

    private final static String HDR_CONTENT_DISPOSITION = "Content-Disposition";

    private final static String ATTACHMENT = "attachment";
    private final static String INLINE = "inline";

	public CMISHttpServletResponse(WebScriptResponse res, Set<String> nonAttachContentTypes)
	{
		super(WebScriptServletRuntime.getHttpServletResponse(res));
        this.nonAttachContentTypes = nonAttachContentTypes;
	}

    @Override
    public void setHeader(String name, String value)
    {
        super.setHeader(name, getStringHeaderValue(name, value, super.getContentType()));
    }

    @Override
    public void addHeader(String name, String value)
    {
        super.addHeader(name, getStringHeaderValue(name, value, super.getContentType()));
    }

    private String getStringHeaderValue(String name, String value, String contentType)
    {
        if (HDR_CONTENT_DISPOSITION.equals(name))
        {
            if (! nonAttachContentTypes.contains(contentType))
            {
                if (value.startsWith(INLINE))
                {
                    // force attachment
                    value = ATTACHMENT+value.substring(INLINE.length());
                }
                else if (! value.startsWith(ATTACHMENT))
                {
                    throw new AlfrescoRuntimeException("Unexpected - header could not be set: "+name+" = "+value);
                }
            }
        }

        return value;
    }
}