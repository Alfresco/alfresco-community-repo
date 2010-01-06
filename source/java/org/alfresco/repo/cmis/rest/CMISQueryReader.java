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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.FormatReader;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.apache.chemistry.abdera.ext.CMISConstants;


/**
 * Convert application/cmisrequest+xml;type=query to class String.
 * 
 * @author davidc
 */
public class CMISQueryReader implements FormatReader<String>
{
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.FormatReader#getDestinationClass()
     */
    public Class<String> getDestinationClass()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.FormatReader#getSourceMimetype()
     */
    public String getSourceMimetype()
    {
        return CMISConstants.MIMETYPE_CMIS_QUERY;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.FormatReader#read(org.alfresco.web.scripts.WebScriptRequest)
     */
    public String read(WebScriptRequest req)
    {
        Content content = req.getContent();
        if (content == null)
        {
            throw new WebScriptException("Failed to convert request to String");
        }
        
        try
        {
            InputStreamReader reader;
            if (content.getEncoding() != null)
            {
                reader = new InputStreamReader(content.getInputStream(), content.getEncoding());
            }
            else
            {
                reader = new InputStreamReader(content.getInputStream());
            }
            StringWriter writer = new StringWriter();
            try
            {
                char[] buffer = new char[4096];
                int bytesRead = -1;
                while ((bytesRead = reader.read(buffer)) != -1)
                {
                    writer.write(buffer, 0, bytesRead);
                }
                writer.flush();
                return writer.toString();
            }
            finally
            {
                reader.close();
                writer.close();
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Failed to convert Query statement", e);
        }
        catch(IOException e)
        {
            throw new WebScriptException("Failed to convert Query statement", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.FormatReader#createScriptParameters(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    public Map<String, Object> createScriptParameters(WebScriptRequest req, WebScriptResponse res)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("query", read(req));
        return params;
    }
}
