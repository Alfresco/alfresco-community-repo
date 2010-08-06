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

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionsGet extends AbstractActionWebscript
{
   @Override
   protected Map<String, Object> buildModel(
         RunningActionModelBuilder modelBuilder, WebScriptRequest req,
         Status status, Cache cache) {
      List<ExecutionSummary> actions = null;
      
      // Do they want all actions, or only certain ones?
      String type = req.getParameter("type");
      String nodeRef = req.getParameter("nodeRef");
      
      if(type != null) {
         actions = actionTrackingService.getExecutingActions(type);
      } else if(nodeRef != null) {
         NodeRef actionNodeRef = new NodeRef(nodeRef);
         Action action = runtimeActionService.createAction(actionNodeRef);
         actions = actionTrackingService.getExecutingActions(action); 
      } else {
         actions = actionTrackingService.getAllExecutingActions();
      }
      
      // Build the model list
      return modelBuilder.buildSimpleList(actions);
   }
}