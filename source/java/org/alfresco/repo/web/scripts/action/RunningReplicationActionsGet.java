package org.alfresco.repo.web.scripts.action;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.replication.ReplicationDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningReplicationActionsGet extends AbstractActionWebscript
{
   private ReplicationService replicationService;
   
   @Override
   protected Map<String, Object> buildModel(
         RunningActionModelBuilder modelBuilder, WebScriptRequest req,
         Status status, Cache cache) {
      List<ExecutionSummary> actions = null;
      
      // Do they want all replication actions, or only certain ones?
      String name = req.getParameter("name");
      
      if(name != null) {
         // Try to find a replication definition with this name
         ReplicationDefinition rd = replicationService.loadReplicationDefinition(name);
         
         // Look up what's running
         if(rd != null) {
            actions = actionTrackingService.getExecutingActions(rd);
         }
      } else {
         // All replication actions
         actions = actionTrackingService.getExecutingActions(
               ReplicationDefinitionImpl.EXECUTOR_NAME
         );
      }
      
      // Build the model list
      return modelBuilder.buildSimpleList(actions);
   }

   public void setReplicationService(ReplicationService replicationService) 
   {
      this.replicationService = replicationService;
   }
}