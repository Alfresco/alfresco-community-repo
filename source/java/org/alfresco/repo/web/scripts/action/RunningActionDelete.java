package org.alfresco.repo.web.scripts.action;

import java.util.Map;

import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionDelete extends AbstractActionWebscript
{
   @Override
   protected Map<String, Object> buildModel(
         RunningActionModelBuilder modelBuilder, WebScriptRequest req,
         Status status, Cache cache) {
      // Which action did they ask for?
      String actionTrackingId = 
         req.getServiceMatch().getTemplateVars().get("action_tracking_id");

      // Check it exists
      ExecutionSummary action = 
         getSummaryFromKey(actionTrackingId);
      if(action == null) {
         throw new WebScriptException(
               Status.STATUS_NOT_FOUND, 
               "No Running Action found with that tracking id"
         );
      }
      
      ExecutionDetails details =
         actionTrackingService.getExecutionDetails(action);
      if(details == null) {
         throw new WebScriptException(
               Status.STATUS_NOT_FOUND, 
               "No Running Action found with that tracking id"
         );
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