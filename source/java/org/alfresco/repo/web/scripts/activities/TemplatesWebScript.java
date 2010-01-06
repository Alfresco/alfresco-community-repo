/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.activities;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.Store;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
 
/**
 * Java-backed WebScript to get list of Activity Templates from a Template Store
 */
public class TemplatesWebScript extends DeclarativeWebScript
{
    private SearchPath searchPath;
    
    public void setSearchPath(SearchPath searchPath)
    {
        this.searchPath = searchPath;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String path = "/";
        String templatePattern = "*.ftl";
        
        // process extension
        String p = req.getExtensionPath(); // optional
        
        if ((p != null) && (p.length() > 0))
        {
            int idx = p.lastIndexOf("/");
            if (idx != -1)
            {
                path = p.substring(0, idx);
                templatePattern = p.substring(idx+1) + ".ftl";
            }
        }
        
        Set<String> templatePaths = new HashSet<String>();
        for (Store apiStore : searchPath.getStores())
        {
            try
            {
                for (String templatePath : apiStore.getDocumentPaths(path, false, templatePattern))
                {
                    templatePaths.add(templatePath);
                }
            }
            catch (IOException e)
            {
                throw new WebScriptException("Failed to search for templates from store " + apiStore, e);
            }
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("paths", templatePaths);
        return model;
    }
}
