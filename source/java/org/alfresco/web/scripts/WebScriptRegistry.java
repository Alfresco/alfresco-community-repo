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
package org.alfresco.web.scripts;

import java.util.Collection;

import javax.servlet.ServletContext;


/**
 * Web Scripts Registry
 * 
 * @author davidc
 */
public interface WebScriptRegistry
{
    /**
     * Gets all Web Scripts
     * 
     * @return  web scripts
     */
    public Collection<WebScript> getWebScripts();

    /**
     * Gets a Web Script by Id
     * 
     * @param id  web script id
     * @return  web script
     */
    public WebScript getWebScript(String id);

    /**
     * Gets a Web Script given an HTTP Method and URI
     * 
     * @param method  http method
     * @param uri  uri
     * @return  service match (pair of service and uri that matched)
     */
    public WebScriptMatch findWebScript(String method, String uri);
    
    /**
     * Gets the Servlet Context
     * 
     * @return  servlet context
     */
    public ServletContext getContext();

    /**
     * Gets the response format registry
     * 
     * @return  response format registry
     */
    public FormatRegistry getFormatRegistry();
    
    /**
     * Gets the Template Processor
     *  
     * @return  template processor
     */
    public TemplateProcessor getTemplateProcessor();
    
    /**
     * Gets the Script Processor
     * 
     * @return  script processor
     */
    public ScriptProcessor getScriptProcessor();
    
    /**
     * Resets the Registry
     */
    public void reset();
}
