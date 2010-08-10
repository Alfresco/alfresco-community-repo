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