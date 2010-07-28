/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

import org.alfresco.service.cmr.audit.AuditService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Abstract implementation for scripts that access the {@link AuditService}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractAuditWebScript extends AbstractWebScript
{
    public static final String PARAM_APP = "app";
    public static final String PARAM_PATH="path";
    
    /**
     * Logger that can be used by subclasses.
     */
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    protected AuditService auditService;
    
    /**
     * @param auditService      the service that provides the actual data
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Return an I18N'd message for the given key or the key itself if not present
     * 
     * @param args              arguments to replace the variables in the message
     */
    protected String getI18NMessage(String key, Object ... args)
    {
        return I18NUtil.getMessage(key, args);
    }
    
    /**
     * Get the application name from the request.
     * 
     * @param mandatory         <tt>true</tt> if the application name is expected
     * @return                  Returns the application name or <tt>null</tt> if not present
     */
    protected final String getApp(WebScriptRequest req, boolean mandatory)
    {
        // All URLs must contain the application
        String paramApp = req.getParameter(PARAM_APP);
        if (paramApp == null && mandatory)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.app.mandatory");
        }
        return paramApp;
    }
    /**
     * Get the path from the request.  If it is mandatory, then a value must have been supplied
     * otherwise, at the very least, '/' is returned.
     * @param mandatory         <tt>true</tt> if the parameter is expected
     * @return                  Returns the path or at least '/' (never <tt>null</tt>)
     */
    protected String getPath(WebScriptRequest req)
    {
        String paramPath = req.getParameter(PARAM_PATH);
        if (paramPath == null || paramPath.length() == 0)
        {
            paramPath = "/";
        }
        else if (!paramPath.startsWith("/"))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "audit.err.path.startsWith");
        }
        return paramPath;
    }
}
