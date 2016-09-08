/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService;
import org.alfresco.module.org_alfresco_module_rm.email.CustomMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return 
 * custom email field mappings
 */
public class EmailMapPost extends DeclarativeWebScript
{   
     /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {

        try
        {
            JSONObject json = null;
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            if(json.has("delete"))
            {
                JSONArray toDelete = json.getJSONArray("delete");
                for(int i = 0 ; i < toDelete.length(); i++)
                {
                    JSONObject val = toDelete.getJSONObject(i);
                    customEmailMappingService.deleteCustomMapping(val.getString("from"), val.getString("to"));
                }
            }
            
            if(json.has("add"))
            {
                JSONArray toAdd = json.getJSONArray("add");
                for(int i = 0 ; i < toAdd.length(); i++)
                {
                    JSONObject val = toAdd.getJSONObject(i);
                    customEmailMappingService.addCustomMapping(val.getString("from"), val.getString("to"));
                    
                }
            }
            
            // Set the return value.
            Set<CustomMapping> emailMap = customEmailMappingService.getCustomMappings();
            // create model object with the lists model
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("emailmap", emailMap);
            return model;
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
    }
    
    private CustomEmailMappingService customEmailMappingService;

    public void setCustomEmailMappingService(CustomEmailMappingService customEmailMappingService)
    {
        this.customEmailMappingService = customEmailMappingService;
    }

    public CustomEmailMappingService getCustomEmailMappingService()
    {
        return customEmailMappingService;
    }
    
    
}