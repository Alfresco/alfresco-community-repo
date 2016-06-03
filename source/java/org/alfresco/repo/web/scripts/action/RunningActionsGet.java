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