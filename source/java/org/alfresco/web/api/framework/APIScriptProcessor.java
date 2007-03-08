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
package org.alfresco.web.api.framework;

import java.util.Map;

import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;


/**
 * Script Processor for use in Web API
 * 
 * @author davidc
 */
public class APIScriptProcessor
{
    // dependencies
    private ScriptService scriptService;
    
    // api store script loader
    private ScriptLoader scriptLoader;

    /**
     * Sets the script service
     * 
     * @param scriptService
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }
    
    /**
     * Sets the script loader
     * 
     * @param scriptLoader
     */
    public void setScriptLoader(ScriptLoader scriptLoader)
    {
        this.scriptLoader = scriptLoader;
    }
    
    /**
     * Find a script at the specified path (within registered API stores)
     * 
     * @param path   script path
     * @return  script location (or null, if not found)
     */
    public ScriptLocation findScript(String path)
    {
        return scriptLoader.getScriptLocation(path);
    }
    
    /**
     * Execute script
     * 
     * @param path  script path
     * @param model  model
     * @return  script result
     * @throws ScriptException
     */
    public Object executeScript(String path, Map<String, Object> model)
        throws ScriptException
    {
        // locate script within api stores
        ScriptLocation scriptLocation = findScript(path);
        if (scriptLocation == null)
        {
            throw new APIException("Unable to locate script " + path);
        }
        // execute script
        return executeScript(scriptLocation, model);
    }

    /**
     * Execute script
     *  
     * @param location  script location
     * @param model  model
     * @return  script result
     */
    public Object executeScript(ScriptLocation location, Map<String, Object> model)
    {
        return scriptService.executeScript(location, model);        
    }
    
}
