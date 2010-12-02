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
package org.alfresco.util.json;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.transfer.TransferException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExceptionJsonSerializer implements JsonSerializer<Throwable, JSONObject>
{
    private final static Log log = LogFactory.getLog(ExceptionJsonSerializer.class);
    
    @Override
    public Throwable deserialize(JSONObject errorJSON)
    {
        if (errorJSON == null)
        {
            return null;
        }
        
        Throwable result = null;
        Object createdObject = null;
        
        try
        {
            //errorType and errorMessage should always be reported
            String errorType = errorJSON.getString("errorType");
            String errorMessage = errorJSON.getString("errorMessage");
            
            if (errorType == null)
            {
                errorType = Exception.class.getName();
            }
            if (errorMessage == null)
            {
                errorMessage = "";
            }
            //alfrescoErrorId and alfrescoErrorParams will only appear if the
            //throwable object was of a subclass of AlfrescoRuntimeException
            String errorId = errorJSON.optString("alfrescoMessageId", null);
            Object[] errorParams = new Object[0];
            JSONArray errorParamArray = errorJSON.optJSONArray("alfrescoMessageParams");
            if (errorParamArray != null)
            {
                int length = errorParamArray.length();
                errorParams = new Object[length];
                for (int i = 0; i < length; ++i) 
                {
                    errorParams[i] = errorParamArray.getString(i);
                }
            }
            Class<?> errorClass;
            try
            {
                errorClass = Class.forName(errorType);
            }
            catch (ClassNotFoundException e)
            {
                errorClass = Exception.class;
            }
            Constructor<?> constructor = null;
            try
            {
                try
                {
                    constructor = errorClass.getConstructor(String.class, Object[].class);
                    createdObject = constructor.newInstance(errorId, errorParams);
                }
                catch (NoSuchMethodException e)
                {
                    try
                    {
                        constructor = errorClass.getConstructor(String.class);
                        createdObject = constructor.newInstance(errorId == null ? errorMessage : errorId);
                    }
                    catch (NoSuchMethodException e1)
                    {
                        try
                        {
                            constructor = errorClass.getConstructor();
                            createdObject = constructor.newInstance();
                        }
                        catch (NoSuchMethodException e2)
                        {
                        }
                    }
                }
            }
            catch(Exception ex)
            {
                //We don't need to do anything here. Code below will fix things up
            }
            if (createdObject == null || !Throwable.class.isAssignableFrom(createdObject.getClass()))
            {
                result = new TransferException(errorId == null ? errorMessage : errorId, errorParams);
            }
            else
            {
                result = (Throwable)createdObject;
            }
        }
        catch(JSONException ex)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Failed to deserialize Throwable object from JSON object", ex);
            }
        }
        return result;
    }

    @Override
    public JSONObject serialize(Throwable object)
    {
        JSONObject errorObject = new JSONObject();
        
        try
        {
            errorObject.put("errorType", object.getClass().getName());
            errorObject.put("errorMessage", object.getMessage());
            if (AlfrescoRuntimeException.class.isAssignableFrom(object.getClass()))
            {
                AlfrescoRuntimeException alfEx = (AlfrescoRuntimeException)object;
                errorObject.put("alfrescoMessageId", alfEx.getMsgId());
                Object[] msgParams = alfEx.getMsgParams();
                List<Object> params = msgParams == null ? Collections.emptyList() : Arrays.asList(msgParams);
                errorObject.put("alfrescoMessageParams", params);
            }
        }
        catch (JSONException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Failed to serialize Throwable object into JSON object", e);
            }
        }
        return errorObject;
    }
}
