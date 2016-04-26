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