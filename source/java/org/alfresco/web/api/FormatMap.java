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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.api;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;


/**
 * A map of mimetypes indexed by format.
 * 
 * @author davidc
 */
public class FormatMap implements InitializingBean
{
    private FormatRegistry registry;
    private String agent;
    private Map<String, String> formats;
    

    /**
     * Sets the Format Registry
     * 
     * @param registry
     */
    public void setRegistry(FormatRegistry registry)
    {
        this.registry = registry;
    }
    
    /**
     * Sets the User Agent for which the formats apply
     * 
     * @param agent
     */
    public void setAgent(String agent)
    {
        this.agent = agent;
    }
    
    /**
     * Sets the formats
     * 
     * @param formats
     */
    public void setFormats(Map<String, String> formats)
    {
        this.formats = formats;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // Add formats to format registry
        registry.addFormats(agent, formats);
    }
    
}
