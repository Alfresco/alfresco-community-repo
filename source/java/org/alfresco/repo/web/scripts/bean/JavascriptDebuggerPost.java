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
package org.alfresco.repo.web.scripts.bean;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.AlfrescoRhinoScriptDebugger;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.Status;


/**
 * Javascript Debugger
 * 
 * @author davidc
 */
public class JavascriptDebuggerPost extends DeclarativeWebScript
{
    // dependencies
    private AlfrescoRhinoScriptDebugger debugger;
    
    /**
     * @param ticketComponent
     */
    public void setDebugger(AlfrescoRhinoScriptDebugger debugger)
    {
        this.debugger = debugger;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String visibleStr = req.getParameter("visible");
        boolean visible = Boolean.valueOf(visibleStr);
        
        if (visible)
        {
            debugger.show();
        }
        else
        {
            debugger.hide();
        }

        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("visible", debugger.isVisible());
        return model;
    }

}
