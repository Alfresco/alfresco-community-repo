/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script.classification;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.module.org_alfresco_module_rm.script.AbstractRmWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for Java backed webscript to set users security clearance.
 *
 * @author David Webster
 * @author Tom Page
 * @since 3.0
 */
public class UserSecurityClearancePut extends AbstractRmWebScript
{
    /** Constants */
    private static final String USERNAME = "username";
    private static final String CLEARANCE_ID = "clearanceId";
    private static final String ITEM = "item";

    /** Security clearance service */
    private SecurityClearanceService securityClearanceService;

    /**
     * @param securityClearanceService the securityClearanceService to set
     */
    public void setSecurityClearanceService(SecurityClearanceService securityClearanceService)
    {
        this.securityClearanceService = securityClearanceService;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String username = req.getParameter(USERNAME);
        String clearanceId = req.getParameter(CLEARANCE_ID);
        SecurityClearance securityClearance;

        try
        {
            securityClearance = securityClearanceService.setUserSecurityClearance(username,
                clearanceId);
        }
        catch (ClassificationException.LevelIdNotFound exception)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unable to find clearance level");
        }

        Map<String, Object> model = new HashMap<>();
        model.put(ITEM, securityClearance);

        return model;
    }
}
