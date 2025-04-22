/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.audit;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.audit.AuditService;

/**
 * Abstract implementation for scripts that access the {@link AuditService}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractAuditWebScript extends DeclarativeWebScript
{
    public static final String PARAM_APPLICATION = "application";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_ENABLE = "enable";
    public static final String PARAM_VALUE = "value";
    public static final String PARAM_VALUE_TYPE = "valueType";
    public static final String PARAM_FROM_TIME = "fromTime";
    public static final String PARAM_TO_TIME = "toTime";
    public static final String PARAM_FROM_ID = "fromId";
    public static final String PARAM_TO_ID = "toId";
    public static final String PARAM_USER = "user";
    public static final String PARAM_FORWARD = "forward";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_VERBOSE = "verbose";

    public static final Long DEFAULT_FROM_TIME = null;
    public static final Long DEFAULT_TO_TIME = null;
    public static final Long DEFAULT_FROM_ID = null;
    public static final Long DEFAULT_TO_ID = null;
    public static final String DEFAULT_USER = null;
    public static final boolean DEFAULT_FORWARD = true;
    public static final int DEFAULT_LIMIT = 100;
    public static final boolean DEFAULT_VERBOSE = false;
    public static final boolean DEFAULT_ENABLE = false;

    public static final String JSON_KEY_ENABLED = "enabled";
    public static final String JSON_KEY_APPLICATIONS = "applications";
    public static final String JSON_KEY_NAME = "name";
    public static final String JSON_KEY_PATH = "path";
    public static final String JSON_KEY_CLEARED = "cleared";
    public static final String JSON_KEY_DELETED = "deleted";

    public static final String JSON_KEY_ENTRY_COUNT = "count";
    public static final String JSON_KEY_ENTRIES = "entries";
    public static final String JSON_KEY_ENTRY_ID = "id";
    public static final String JSON_KEY_ENTRY_APPLICATION = "application";
    public static final String JSON_KEY_ENTRY_USER = "user";
    public static final String JSON_KEY_ENTRY_TIME = "time";
    public static final String JSON_KEY_ENTRY_VALUES = "values";

    /**
     * Logger that can be used by subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());

    protected AuditService auditService;

    /**
     * @param auditService
     *            the service that provides the actual data
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Return an I18N'd message for the given key or the key itself if not present
     * 
     * @param args
     *            arguments to replace the variables in the message
     */
    protected String getI18NMessage(String key, Object... args)
    {
        return I18NUtil.getMessage(key, args);
    }

    /**
     * Get the application name from the request.
     * 
     * @return Returns the application name or <tt>null</tt> if not present
     */
    protected final String getParamAppName(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String app = templateVars.get(PARAM_APPLICATION);
        if (app == null || app.length() == 0)
        {
            return null;
        }
        else
        {
            return app;
        }
    }

    /**
     * Get the entry id from the request.
     * 
     * @return Returns the id or <tt>null</tt> if not present
     */
    protected Long getId(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");
        if (id == null || id.length() == 0)
        {
            return null;
        }
        else
        {
            try
            {
                return Long.parseLong(id);
            }
            catch (NumberFormatException e)
            {
                return null;
            }
        }
    }

    /**
     * Get the path from the request.
     * 
     * @return Returns the path or <tt>null</tt> if not present
     */
    protected String getParamPath(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String paramPath = templateVars.get(PARAM_PATH);
        if (paramPath == null || paramPath.length() == 0)
        {
            paramPath = null;
        }
        else if (!paramPath.startsWith("/"))
        {
            // It won't ever, so we can expect to be here all the time
            paramPath = "/" + paramPath;
        }
        return paramPath;
    }

    protected boolean getParamEnableDisable(WebScriptRequest req)
    {
        return getBooleanParam(req.getParameter(PARAM_ENABLE), DEFAULT_ENABLE);
    }

    protected String getParamValue(WebScriptRequest req)
    {
        return req.getParameter(PARAM_VALUE);
    }

    protected String getParamValueType(WebScriptRequest req)
    {
        return req.getParameter(PARAM_VALUE_TYPE);
    }

    /**
     * @see #DEFAULT_FROM_TIME
     */
    protected Long getParamFromTime(WebScriptRequest req)
    {
        return getLongParam(req.getParameter(PARAM_FROM_TIME), DEFAULT_FROM_TIME);
    }

    /**
     * @see #DEFAULT_TO_TIME
     */
    protected Long getParamToTime(WebScriptRequest req)
    {
        return getLongParam(req.getParameter(PARAM_TO_TIME), DEFAULT_TO_TIME);
    }

    /**
     * @see #DEFAULT_FROM_ID
     */
    protected Long getParamFromId(WebScriptRequest req)
    {
        return getLongParam(req.getParameter(PARAM_FROM_ID), DEFAULT_FROM_ID);
    }

    /**
     * @see #DEFAULT_TO_ID
     */
    protected Long getParamToId(WebScriptRequest req)
    {
        return getLongParam(req.getParameter(PARAM_TO_ID), DEFAULT_TO_ID);
    }

    /**
     * @see #DEFAULT_USER
     */
    protected String getParamUser(WebScriptRequest req)
    {
        return req.getParameter(PARAM_USER);
    }

    /**
     * @see #DEFAULT_FORWARD
     */
    protected boolean getParamForward(WebScriptRequest req)
    {
        return getBooleanParam(req.getParameter(PARAM_FORWARD), DEFAULT_FORWARD);
    }

    /**
     * @see #DEFAULT_LIMIT
     */
    protected int getParamLimit(WebScriptRequest req)
    {
        return getIntParam(req.getParameter(PARAM_LIMIT), DEFAULT_LIMIT);
    }

    /**
     * @see #DEFAULT_VERBOSE
     */
    protected boolean getParamVerbose(WebScriptRequest req)
    {
        return getBooleanParam(req.getParameter(PARAM_VERBOSE), DEFAULT_VERBOSE);
    }

    private Long getLongParam(String paramStr, Long defaultVal)
    {
        if (paramStr == null)
        {
            // note: defaultVal can be null
            return defaultVal;
        }
        try
        {
            return Long.parseLong(paramStr);
        }
        catch (NumberFormatException e)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage());
        }
    }

    private boolean getBooleanParam(String paramStr, boolean defaultVal)
    {
        if (paramStr == null)
        {
            return defaultVal;
        }

        // note: will return false if paramStr does not equals "true" (ignoring case)
        return Boolean.parseBoolean(paramStr);
    }

    private int getIntParam(String paramStr, int defaultVal)
    {
        if (paramStr == null)
        {
            return defaultVal;
        }
        try
        {
            return Integer.parseInt(paramStr);
        }
        catch (NumberFormatException e)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage());
        }
    }
}
