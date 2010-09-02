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
package org.alfresco.repo.web.scripts.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditApplication;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Derek Hulley
 * @since 3.4
 */
public class AuditQueryGet extends AbstractAuditWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        final Map<String, Object> model = new HashMap<String, Object>(7);
        
        String appName = getParamAppName(req);
        String path = getParamPath(req);
        
        Serializable value = getParamValue(req);
        String valueType = getParamValueType(req);
        Long fromTime = getParamFromTime(req);
        Long toTime = getParamToTime(req);
        Long fromId = getParamFromId(req);
        Long toId = getParamToId(req);
        String user = getParamUser(req);
        boolean forward = getParamForward(req);
        int limit = getParamLimit(req);
        final boolean verbose = getParamVerbose(req);
        
        if (appName == null)
        {
            Map<String, AuditApplication> appsByName = auditService.getAuditApplications();
            AuditApplication app = appsByName.get(appName);
            if (app == null)
            {
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "audit.err.app.notFound", appName);
            }
        }
        
        // Transform the value to the correct type
        if (value != null && valueType != null)
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<? extends Serializable> clazz = (Class<? extends Serializable>) Class.forName(valueType);
                value = DefaultTypeConverter.INSTANCE.convert(clazz, value);
            }
            catch (ClassNotFoundException e)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.value.classNotFound", valueType);
            }
            catch (Throwable e)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.value.convertFailed", value, valueType);
            }
        }
        
        // Execute the query
        AuditQueryParameters params = new AuditQueryParameters();
        params.setApplicationName(appName);
        params.setFromTime(fromTime);
        params.setToTime(toTime);
        params.setFromId(fromId);
        params.setToId(toId);
        params.setUser(user);
        params.setForward(forward);
        if (path != null || value != null)
        {
            params.addSearchKey(path, value);
        }
        
        final List<Map<String, Object>> entries = new ArrayList<Map<String,Object>>(limit);
        AuditQueryCallback callback = new AuditQueryCallback()
        {
            @Override
            public boolean valuesRequired()
            {
                return verbose;
            }
            
            @Override
            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                return true;
            }
            
            @Override
            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                Map<String, Object> entry = new HashMap<String, Object>(11);
                entry.put(JSON_KEY_ENTRY_ID, entryId);
                entry.put(JSON_KEY_ENTRY_APPLICATION, applicationName);
                if (user != null)
                {
                    entry.put(JSON_KEY_ENTRY_USER, user);
                }
                entry.put(JSON_KEY_ENTRY_TIME, new Date(time));
                if (values != null)
                {
                    // Convert values to Strings
                    Map<String, String> valueStrings = new HashMap<String, String>(values.size() * 2);
                    for (Map.Entry<String, Serializable> mapEntry : values.entrySet())
                    {
                        String key = mapEntry.getKey();
                        Serializable value = mapEntry.getValue();
                        try
                        {
                            String valueString = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                            valueStrings.put(key, valueString);
                        }
                        catch (TypeConversionException e)
                        {
                            // Use the toString()
                            valueStrings.put(key, value.toString());
                        }
                        
                    }
                    entry.put(JSON_KEY_ENTRY_VALUES, valueStrings);
                }
                entries.add(entry);
                
                return true;
            }
        };
        
        auditService.auditQuery(callback, params, limit);
        
        model.put(JSON_KEY_ENTRY_COUNT, entries.size());
        model.put(JSON_KEY_ENTRIES, entries);
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        return model;
    }
}