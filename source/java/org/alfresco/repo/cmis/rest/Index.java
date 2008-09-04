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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Description;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptRequest;


/**
 * Index of CMIS Scripts
 * 
 * @author davidc
 */
public class Index extends DeclarativeWebScript
{

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        List<WebScript> cmisScripts = new ArrayList<WebScript>();
        
        // scan through all web scripts looking for CMIS specific ones
        Collection<WebScript> webscripts = getContainer().getRegistry().getWebScripts();
        for (WebScript webscript : webscripts)
        {
            Description desc = webscript.getDescription();
            Map<String, Serializable> extensions = desc.getExtensions();
            if (extensions != null && extensions.get("cmis_version") != null)
            {
                cmisScripts.add(webscript);
            }
        }
            
        Map<String, Object> model = new HashMap<String, Object>(7, 1.0f);
        model.put("services",  cmisScripts);
        return model;
    }

}
