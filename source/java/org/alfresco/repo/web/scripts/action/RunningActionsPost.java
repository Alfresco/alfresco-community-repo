package org.alfresco.repo.web.scripts.action;

import java.io.IOException;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
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
public class RunningActionsPost extends AbstractExecuteActionWebscript
{
   @Override
   protected Action identifyAction(WebScriptRequest req, Status status,
         Cache cache) {
      // Which action did they ask for?
      String nodeRef = req.getParameter("nodeRef");
      if(nodeRef == null) {
         try {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            if(! json.has("nodeRef")) {
               throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'nodeRef' parameter");
            }
            nodeRef = json.getString("nodeRef");
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
      
      // Does it exist in the repo?
      NodeRef actionNodeRef = new NodeRef(nodeRef);
      if(! nodeService.exists(actionNodeRef)) {
         return null;
      }
      
      // Load the specified action
      Action action = runtimeActionService.createAction(actionNodeRef);
      return action;
   }
}