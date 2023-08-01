/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.workflow;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 * @since 3.4
 */
public class TaskInstancePut extends AbstractWorkflowWebscript
{

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        
        // getting task id from request parameters
        String taskId = params.get("task_instance_id");
        
        JSONObject json = null;
        
        try
        {
            WorkflowTask workflowTask = workflowService.getTaskById(taskId);
            String currentUser = authenticationService.getCurrentUserName();

            // read request json            
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
                
            // update task properties
            workflowTask = workflowService.updateTask(taskId, parseTaskProperties(json, workflowTask), null, null);
                
            // task was not found -> return 404
            if (workflowTask == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Failed to find workflow task with id: " + taskId);
            }
                
            // build the model for ftl
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("workflowTask", modelBuilder.buildDetailed(workflowTask));
                
            return model;
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from request.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request.", je);
        }
        catch (AccessDeniedException ade)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Failed to update workflow task with id: " + taskId, ade);
        }
        catch (WorkflowException we)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Failed to update workflow task with id: " + taskId, we);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<QName, Serializable> parseTaskProperties(JSONObject json, WorkflowTask workflowTask) throws JSONException
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        
        // gets the array of properties names
        String[] names = JSONObject.getNames(json);
        
        if (names != null)
        {
            // array is not empty
            for (String name : names)
            {
                // build the qname of property
                QName key = QName.createQName(name.replaceFirst("_", ":"), namespaceService);
                Object jsonValue = json.get(name);
                
                Serializable value = null;
                
                // process null values 
                if (jsonValue.equals(JSONObject.NULL))
                {
                    props.put(key, null);
                }
                else
                {
                    // gets the property definition from dictionary
                    PropertyDefinition prop = dictionaryService.getProperty(key);
                    
                    if (prop != null)
                    {
                        if (prop.isMultiValued() && jsonValue instanceof JSONArray)
                        {
                            value = new ArrayList<Serializable>();
                            
                            for (int i = 0; i < ((JSONArray)jsonValue).length(); i++)
                            {
                                ((List<Serializable>)value).add((Serializable) DefaultTypeConverter.INSTANCE.convert(prop.getDataType(),((JSONArray)jsonValue).get(i)));
                            }
                        }
                        else
                        {
                            // convert property using its data type specified in model
                            value = (Serializable) DefaultTypeConverter.INSTANCE.convert(prop.getDataType(), jsonValue);
                        }
                    }
                    else
                    {
                        // property definition was not found in dictionary
                        if (jsonValue instanceof JSONArray)
                        {
                            value = new ArrayList<String>();
                            
                            for (int i = 0; i < ((JSONArray)jsonValue).length(); i++)
                            {
                                ((List<String>)value).add(((JSONArray)jsonValue).getString(i));
                            }
                        }
                        else
                        {
                            // If the JSON returns an Object which is not a String, we use that type.
                            // Otherwise, we try to convert the string
                            if (jsonValue instanceof String)
                            {
                                // Check if the task already has the property, use that type.
                                Serializable existingValue = workflowTask.getProperties().get(key);
                                if (existingValue != null)
                                {
                                    try
                                    {
                                        value = DefaultTypeConverter.INSTANCE.convert(existingValue.getClass(), jsonValue);
                                    }
                                    catch(TypeConversionException tce)
                                    {
                                        // TODO: is this the right approach, ignoring exception?
                                        // Ignore the exception, revert to using String-value
                                    }
                                }
                                else
                                {
                                    // Revert to using string-value
                                    value = (String) jsonValue;
                                }
                            }
                            else
                            {
                                // Use the value provided by JSON
                                value = (Serializable) jsonValue;
                            }
                        }
                    }
                }
                
                props.put(key, value);
            }
        }
        return props;
    }
}