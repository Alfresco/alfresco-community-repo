package org.alfresco.repo.web.scripts.action;

import java.util.Map;

import org.alfresco.service.cmr.action.ExecutionSummary;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionGet extends AbstractActionWebscript
{
   @Override
   protected Map<String, Object> buildModel(
         RunningActionModelBuilder modelBuilder, WebScriptRequest req,
         Status status, Cache cache) {
      // Which action did they ask for?
      String actionTrackingId = 
         req.getServiceMatch().getTemplateVars().get("action_tracking_id");

      ExecutionSummary action = 
         getSummaryFromKey(actionTrackingId);
      
      // Get the details, if we can
      Map<String,Object> model = modelBuilder.buildSimpleModel(action);
      
      if(model == null) {
         throw new WebScriptException(
               Status.STATUS_NOT_FOUND, 
               "No Running Action found with that tracking id"
         );
      }
      
      return model;
   }
}