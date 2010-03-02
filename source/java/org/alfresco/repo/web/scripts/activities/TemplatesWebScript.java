/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
