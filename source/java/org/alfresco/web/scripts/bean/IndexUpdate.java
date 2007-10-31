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
package org.alfresco.web.scripts.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptStatus;
import org.alfresco.web.scripts.facebook.FacebookService;


/**
 * Retrieves the list of available Web Scripts
 * 
 * @author davidc
 */
public class IndexUpdate extends DeclarativeWebScript
{
    // component dependencies
    private FacebookService facebookService;
    
    /**
     * @param facebookService facebook service
     */
    public void setFacebookService(FacebookService facebookService)
    {
        this.facebookService = facebookService;
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status)
    {
        List<String> tasks = new ArrayList<String>();

        // reset index
        String reset = req.getParameter("reset");
        if (reset != null && reset.equals("on"))
        {
            // reset list of web scripts
            int previousCount = getWebScriptRegistry().getWebScripts().size();
            getWebScriptRegistry().reset();
            tasks.add("Reset Web Scripts Registry; found " + getWebScriptRegistry().getWebScripts().size() + " Web Scripts.  Previously, there were " + previousCount + ".");
            
            // reset facebook service
            // TODO: Determine more appropriate place to put this
            int appCount = facebookService.getAppModels().size();
            if (appCount > 0)
            {
                facebookService.reset();
                tasks.add("Reset " + appCount + " Facebook Applications.");
            }
        }

        // create model for rendering
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("tasks", tasks);
        model.put("webscripts",  getWebScriptRegistry().getWebScripts());
        return model;
    }

}
