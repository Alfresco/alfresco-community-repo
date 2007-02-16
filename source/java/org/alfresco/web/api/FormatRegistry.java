/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Maintains a registry of mimetypes (indexed by format and user agent)
 * 
 * @author davidc
 */
public class FormatRegistry
{
    // Logger
    private static final Log logger = LogFactory.getLog(FormatRegistry.class);

    private Map<String, String> formats;
    private Map<String, String> mimetypes;
    private Map<String, Map<String, String>> agentFormats;
    private Map<String, Map<String, String>> agentMimetypes;
    

    /**
     * Construct
     */
    public FormatRegistry()
    {
        formats = new HashMap<String, String>();
        mimetypes = new HashMap<String, String>();
        agentFormats = new HashMap<String, Map<String, String>>();
        agentMimetypes = new HashMap<String, Map<String, String>>();
    }
    
    /**
     * Add formats
     * 
     * @param agent
     * @param formatsToAdd
     */
    public void addFormats(String agent, Map<String, String> formatsToAdd)
    {
        // retrieve formats list for agent
        Map<String, String> formatsForAgent = formats; 
        Map<String, String> mimetypesForAgent = mimetypes; 
        if (agent != null)
        {
            formatsForAgent = agentFormats.get(agent);
            if (formatsForAgent == null)
            {
                formatsForAgent = new HashMap<String, String>();
                mimetypesForAgent = new HashMap<String, String>();
                agentFormats.put(agent, formatsForAgent);
                agentMimetypes.put(agent, mimetypesForAgent);
            }
        }
        
        for (Map.Entry<String, String> entry : formatsToAdd.entrySet())
        {
            if (logger.isWarnEnabled())
            {
                String mimetype = formatsForAgent.get(entry.getKey());
                if (mimetype != null)
                {
                    logger.warn("Replacing mime type '" + mimetype + "' with '" + entry.getValue() + "' for API format '" + entry.getKey() + "' (agent: " + agent + ")");
                }
            }
            
            formatsForAgent.put(entry.getKey(), entry.getValue());
            mimetypesForAgent.put(entry.getValue(), entry.getKey());

            if (logger.isDebugEnabled())
                logger.debug("Registered API format '" + entry.getKey() + "' with mime type '" + entry.getValue() + "' (agent: " + agent + ")");
        }
    }

    /**
     * Gets the mimetype for the specified user agent and format
     * 
     * @param agent
     * @param format
     * @return  mimetype (or null, if one is not registered)
     */
    public String getMimeType(String agent, String format)
    {
        String mimetype = null;
        
        if (agent != null)
        {
            Map<String, String> formatsForAgent = agentFormats.get(agent);
            if (formatsForAgent != null)
            {
                mimetype = formatsForAgent.get(format);
            }
        }
        
        if (mimetype == null)
        {
            mimetype = formats.get(format);
        }

        return mimetype;
    }

    /**
     * Gets the format for the specified user agent and mimetype
     * 
     * @param agent
     * @param mimetype
     * @return  format (or null, if one is not registered)
     */
    public String getFormat(String agent, String mimetype)
    {
        String format = null;
        
        if (agent != null)
        {
            Map<String, String> mimetypesForAgent = agentMimetypes.get(agent);
            if (mimetypesForAgent != null)
            {
                format = mimetypesForAgent.get(mimetype);
            }
        }
        
        if (format == null)
        {
            format = mimetypes.get(mimetype);
        }

        return format;
    }

}
