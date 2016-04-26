package org.alfresco.repo.web.scripts.action;

import java.io.IOException;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class RunningReplicationActionsPost extends AbstractExecuteActionWebscript
{
   private ReplicationService replicationService;
   
   @Override
   protected Action identifyAction(WebScriptRequest req, Status status,
         Cache cache) {
      // Which action did they ask for?
      String name = req.getParameter("name");
      if(name == null) {
         try {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            if(! json.has("name")) {
               throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'name' parameter");
            }
            name = json.getString("name");
         }
         catch (IOException iox)
         {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from request.", iox);
         }
         catch (JSONException je)
         {
             throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request.", je);
         }
      }
      
      // Load the specified replication definition
      ReplicationDefinition replicationDefinition = 
         replicationService.loadReplicationDefinition(name);
      return replicationDefinition;
   }

   public void setReplicationService(ReplicationService replicationService) 
   {
      this.replicationService = replicationService;
   }
}