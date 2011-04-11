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
package org.alfresco.repo.preference.script;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.mozilla.javascript.NativeObject;

/**
 * @author Roy Wetherall
 */
public class ScriptPreferenceService extends BaseScopableProcessorExtension
{
    @SuppressWarnings("unused")
    private ServiceRegistry services;
    
    /** Preference Service */
    private PreferenceService preferenceService;
    
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }
    
    public NativeObject getPreferences(String userName)
    {
        return getPreferences(userName, null);
    }
    
    public NativeObject getPreferences(String userName, String preferenceFilter)
    {
        Map<String, Serializable> prefs = this.preferenceService.getPreferences(userName, preferenceFilter);        
        NativeObject result = new NativeObject();
        
        for (Map.Entry<String, Serializable> entry : prefs.entrySet())
        {
            String[] keys = entry.getKey().replace(".", "+").split("\\+");            
            setPrefValue(keys, entry.getValue(), result);
        }        
        
        return result;
    }
    
    private void setPrefValue(String[] keys, Serializable value, NativeObject object)
    {
        NativeObject currentObject = object;
        int index = 0;
        for (String key : keys)
        {
            if (index == keys.length-1)
            {
                currentObject.put(key, currentObject, value);
            }
            else
            {
                NativeObject newObject = null;
                Object temp = currentObject.get(key, currentObject);
                if (temp == null || temp instanceof NativeObject == false)
                {
                    newObject = new NativeObject();
                    currentObject.put(key, currentObject, newObject);
                }
                else
                {
                    newObject = (NativeObject)temp;
                }
                currentObject = newObject;
            }
         
            index ++;
        }
    }
    
    public void setPreferences(String userName, NativeObject preferences)
    {
        Map<String, Serializable> values = new HashMap<String, Serializable>(10);
        getPrefValues(preferences, null, values);
        
        this.preferenceService.setPreferences(userName, values);
    }
    
    private void getPrefValues(NativeObject currentObject, String currentKey, Map<String, Serializable> values)
    {
        Object[] ids = currentObject.getIds();
        for (Object id : ids)
        {
            String key = getAppendedKey(currentKey, id.toString());
            Object value = currentObject.get(id.toString(), currentObject);
            if (value instanceof NativeObject)
            {
                getPrefValues((NativeObject)value, key, values);
            }
            else
            {
                values.put(key, (Serializable)value);
            }
        }
    }
    
    public void clearPreferences(String userName)
    {
        this.preferenceService.clearPreferences(userName, null);
    }
    
    
    /**
     * Clear the preference values
     * 
     * @param userName
     * @param preferenceFilter
     */
    public void clearPreferences(String userName, String preferenceFilter)
    {
        this.preferenceService.clearPreferences(userName, preferenceFilter);
    }
    
    private String getAppendedKey(String currentKey, String key)
    {
        StringBuffer buffer = new StringBuffer(64);
        if (currentKey != null && currentKey.length() != 0)
        {
            buffer.append(currentKey).append(".").append(key);
        }
        else
        {    
            buffer.append(key);
        }
        return buffer.toString();
    }
}
