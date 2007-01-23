/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    private Map<String, Map<String, String>> agentFormats;
    

    /**
     * Construct
     */
    public FormatRegistry()
    {
        formats = new HashMap<String, String>();
        agentFormats = new HashMap<String, Map<String, String>>();
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
        if (agent != null)
        {
            formatsForAgent = agentFormats.get(agent);
            if (formatsForAgent == null)
            {
                formatsForAgent = new HashMap<String, String>();
                agentFormats.put(agent, formatsForAgent);
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
    
}
