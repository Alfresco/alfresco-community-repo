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

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Nick Burch
 * @since 3.4
 */
public abstract class AbstractExecuteActionWebscript extends AbstractActionWebscript
{
    protected Map<String, Object> buildModel(
          RunningActionModelBuilder modelBuilder,
          WebScriptRequest req,
          Status status, Cache cache)
    {
        try { 
           // Have the action to run be identified
           Action action = identifyAction(req, status, cache);
           if(action == null) {
              throw new WebScriptException(
                    Status.STATUS_NOT_FOUND, 
                    "No Runnable Action found with the supplied details"
              );
           }
           
           // Ask for it to be run in the background
           // It will be available to execute once the webscript finishes
           actionService.executeAction(
                 action, null, 
                 false, true
           );
   
           // Return the details if we can
           ExecutionSummary summary = getSummaryFromAction(action);
           if(summary == null) {
              throw new WebScriptException(
                    Status.STATUS_EXPECTATION_FAILED, 
                    "Action failed to be added to the pending queue"
              );
           }
           
           return modelBuilder.buildSimpleModel(summary);
       } catch(Exception e) {
          // Transaction broke
          throw new RuntimeException(e);
       }
    }
    
    protected abstract Action identifyAction(
          WebScriptRequest req,
          Status status, Cache cache
    );
}