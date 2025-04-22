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
package org.alfresco.repo.web.scripts.action;

import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionDelete extends AbstractActionWebscript
{
    @Override
    protected Map<String, Object> buildModel(
            RunningActionModelBuilder modelBuilder, WebScriptRequest req,
            Status status, Cache cache)
    {
        // Which action did they ask for?
        String actionTrackingId = req.getServiceMatch().getTemplateVars().get("action_tracking_id");

        // Check it exists
        ExecutionSummary action = getSummaryFromKey(actionTrackingId);
        if (action == null)
        {
            throw new WebScriptException(
                    Status.STATUS_NOT_FOUND,
                    "No Running Action found with that tracking id");
        }

        ExecutionDetails details = actionTrackingService.getExecutionDetails(action);
        if (details == null)
        {
            throw new WebScriptException(
                    Status.STATUS_NOT_FOUND,
                    "No Running Action found with that tracking id");
        }

        // Request the cancel
        actionTrackingService.requestActionCancellation(action);

        // Report it as having been cancelled
        status.setCode(Status.STATUS_NO_CONTENT);
        status.setMessage("Action cancellation requested");
        status.setRedirect(true);
        return null;
    }
}
