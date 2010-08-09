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
package org.alfresco.repo.web.scripts.action;

import java.util.Map;
import java.util.NoSuchElementException;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.ActionTrackingServiceImpl;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Nick Burch
 * @since 3.4
 */
public abstract class AbstractActionWebscript extends DeclarativeWebScript
{
    protected NodeService nodeService;
    protected ActionService actionService;
    protected RuntimeActionService runtimeActionService;
    protected ActionTrackingService actionTrackingService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService) 
    {
        this.actionService = actionService;
    }

    public void setRuntimeActionService(RuntimeActionService runtimeActionService) 
    {
        this.runtimeActionService = runtimeActionService;
    }

    public void setActionTrackingService(ActionTrackingService actionTrackingService) 
    {
        this.actionTrackingService = actionTrackingService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
       RunningActionModelBuilder modelBuilder = new RunningActionModelBuilder(
             nodeService, actionService, actionTrackingService
       );
       return buildModel(modelBuilder, req, status, cache);
    }
    
    protected abstract Map<String, Object> buildModel(
          RunningActionModelBuilder modelBuilder,
          WebScriptRequest req,
          Status status, Cache cache
    );
    
    
    /**
     * Takes a running action ID, and returns an 
     *  ExecutionSummary object for it. Note - doesn't
     *  check to see if the object exists in the
     *  cache though!
     */
    public static ExecutionSummary getSummaryFromKey(String key)
    {
       return WrappedActionTrackingService.getSummaryFromKey(key);
    }
    
    /**
     * Returns the ExecutionSummary for the given action if it
     *  is currently executing, or null if it isn't
     */
    public static ExecutionSummary getSummaryFromAction(Action action)
    {
       // Is it running?
       if(action.getExecutionStatus() == ActionStatus.Running) {
          return WrappedActionTrackingService.buildExecutionSummary(action);
       }
       // Has it been given a execution id?
       // (eg has already finished, but this one was run)
       if( ((ActionImpl)action).getExecutionInstance() != -1 ) {
          return WrappedActionTrackingService.buildExecutionSummary(action);
       }
       // Not running, and hasn't run, we can't help
       return null;
    }
    
    /**
     * Returns the running action ID for the given
     *  ExecutionSummary
     */
    public static String getRunningId(ExecutionSummary summary)
    {
       return WrappedActionTrackingService.getRunningId(summary);
    }
    
    /**
     * So we can get at protected methods, which we need as
     *  we use the same naming scheme as the cache in the
     *  interests of simplicity.
     */
    private static class WrappedActionTrackingService extends ActionTrackingServiceImpl
    {
       private static String getRunningId(ExecutionSummary summary)
       {
          return ActionTrackingServiceImpl.generateCacheKey(summary);
       }
       
       protected static ExecutionSummary buildExecutionSummary(Action action)
       {
          return ActionTrackingServiceImpl.buildExecutionSummary(action);
       }
       
       private static ExecutionSummary getSummaryFromKey(String key)
       {
          try {
             // Try to have the key turned into a summary for us
             return ActionTrackingServiceImpl.buildExecutionSummary(key);
          } catch(NoSuchElementException e) {
             // Wrong format
             return null;
          }
       }
    }
}