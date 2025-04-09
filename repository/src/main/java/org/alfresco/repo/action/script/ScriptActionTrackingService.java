/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.action.script;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;

/**
 * Script object representing the action tracking service.
 * 
 * @author Nick Burch
 */
public class ScriptActionTrackingService extends BaseScopableProcessorExtension
{
    /** The Services registry */
    private ServiceRegistry serviceRegistry;
    private ActionTrackingService actionTrackingService;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     *            the service registry.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Set the action tracking service.
     * 
     * @param actionTrackingService
     *            the action tracking service
     */
    public void setActionTrackingService(ActionTrackingService actionTrackingService)
    {
        this.actionTrackingService = actionTrackingService;
    }

    /**
     * Requests that the specified Action cancel itself and aborts execution, as soon as possible.
     */
    public void requestActionCancellation(ScriptExecutionDetails action)
    {
        actionTrackingService.requestActionCancellation(
                action.getExecutionDetails().getExecutionSummary());
    }

    /**
     * Retrieve summary details of all the actions currently executing.
     */
    public ScriptExecutionDetails[] getAllExecutingActions()
    {
        List<ExecutionSummary> running = actionTrackingService.getAllExecutingActions();
        return toDetails(running);
    }

    /**
     * Retrieve summary details of all the actions of the given type that are currently executing.
     */
    public ScriptExecutionDetails[] getExecutingActions(String type)
    {
        List<ExecutionSummary> running = actionTrackingService.getExecutingActions(type);
        return toDetails(running);
    }

    /**
     * Retrieve summary details of all instances of the specified action that are currently executing.
     */
    public ScriptExecutionDetails[] getExecutingActions(Action action)
    {
        List<ExecutionSummary> running = actionTrackingService.getExecutingActions(action);
        return toDetails(running);
    }

    private ScriptExecutionDetails[] toDetails(List<ExecutionSummary> running)
    {
        List<ScriptExecutionDetails> details = new ArrayList<ScriptExecutionDetails>();
        for (ExecutionSummary summary : running)
        {
            ExecutionDetails detail = actionTrackingService.getExecutionDetails(summary);
            if (detail != null)
            {
                details.add(new ScriptExecutionDetails(detail, serviceRegistry));
            }
        }

        return details.toArray(new ScriptExecutionDetails[details.size()]);
    }
}
