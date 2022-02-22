/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionResult;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class provides the implementation for the rmaction webscript.
 *
 * @author Neil McErlean
 */
public class RmActionPost extends DeclarativeWebScript
{
   private static Log logger = LogFactory.getLog(RmActionPost.class);

   private static final String PARAM_NAME = "name";
   private static final String PARAM_NODE_REF = "nodeRef";
   private static final String PARAM_NODE_REFS = "nodeRefs";
   private static final String PARAM_PARAMS = "params";

   private NodeService nodeService;
   private RecordsManagementActionService rmActionService;

   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
   {
      this.rmActionService = rmActionService;
   }

   @SuppressWarnings("unchecked")
   @Override
   public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
   {
      String reqContentAsString;
      try
      {
         reqContentAsString = req.getContent().getContent();
      }
      catch (IOException iox)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST,
               "Could not read content from req.", iox);
      }

      String actionName = null;
      List<NodeRef> targetNodeRefs = new ArrayList<>(1);
      Map<String, Serializable> actionParams = new HashMap<>(3);

      try
      {
         JSONObject jsonObj = new JSONObject(new JSONTokener(reqContentAsString));

         // Get the action name
         if (jsonObj.has(PARAM_NAME))
         {
            actionName = jsonObj.getString(PARAM_NAME);
         }

         // Get the target references
         if (jsonObj.has(PARAM_NODE_REF))
         {
            NodeRef nodeRef = new NodeRef(jsonObj.getString(PARAM_NODE_REF));
            targetNodeRefs.add(nodeRef);
         }
         if (jsonObj.has(PARAM_NODE_REFS))
         {
            JSONArray jsonArray = jsonObj.getJSONArray(PARAM_NODE_REFS);
            if (jsonArray.length() != 0)
            {
               targetNodeRefs = new ArrayList<>(jsonArray.length());
               for (int i = 0; i < jsonArray.length(); i++)
               {
                  NodeRef nodeRef = new NodeRef(jsonArray.getString(i));
                  targetNodeRefs.add(nodeRef);
               }
            }
         }

         // params are optional.
         if (jsonObj.has(PARAM_PARAMS))
         {
            JSONObject paramsObj = jsonObj.getJSONObject(PARAM_PARAMS);
            for (Iterator<String> iter = paramsObj.keys(); iter.hasNext(); )
            {
               String nextKeyString = iter.next();
               Object nextValue = paramsObj.get(nextKeyString);

               // Check for date values
               if ((nextValue instanceof JSONObject) && ((JSONObject)nextValue).has("iso8601"))
               {
                   String dateStringValue = ((JSONObject)nextValue).getString("iso8601");
                   nextValue = ISO8601DateFormat.parse(dateStringValue);
               }

               actionParams.put(nextKeyString, (Serializable)nextValue);
            }
         }
      }
      catch (JSONException exception)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unable to parse request JSON.", exception);
      }

      // validate input: check for mandatory params.
      // Some RM actions can be posted without a nodeRef.
      if (actionName == null)
      {
         throw new WebScriptException(Status.STATUS_BAD_REQUEST,
               "A mandatory parameter has not been provided in URL");
      }

      // Check that all the nodes provided exist and build report string
      StringBuilder targetNodeRefsString = new StringBuilder(30);
      boolean firstTime = true;
      for (NodeRef targetNodeRef : targetNodeRefs)
      {
         if (!nodeService.exists(targetNodeRef))
         {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                  "The targetNode does not exist (" + targetNodeRef.toString() + ")");
         }

         // Build the string
         if (firstTime)
         {
            firstTime = false;
         }
         else
         {
            targetNodeRefsString.append(", ");
         }
         targetNodeRefsString.append(targetNodeRef.toString());
      }

      // Proceed to execute the specified action on the specified node.
      if (logger.isDebugEnabled())
      {
         StringBuilder msg = new StringBuilder();
         msg.append("Executing Record Action ")
            .append(actionName)
            .append(", (")
            .append(targetNodeRefsString.toString())
            .append("), ")
            .append(actionParams);
         logger.debug(msg.toString());
      }

      Map<String, Object> model = new HashMap<>();
      if (targetNodeRefs.isEmpty())
      {
         RecordsManagementActionResult result = this.rmActionService.executeRecordsManagementAction(actionName, actionParams);
         if (result.getValue() != null)
         {
            model.put("result", result.getValue().toString());
         }
      }
      else
      {
         Map<NodeRef, RecordsManagementActionResult> resultMap = this.rmActionService.executeRecordsManagementAction(targetNodeRefs, actionName, actionParams);
         Map<String, String> results = new HashMap<>(resultMap.size());
         for (Map.Entry<NodeRef, RecordsManagementActionResult> entry : resultMap.entrySet())
         {
             Object value = entry.getValue().getValue();
             if (value != null)
             {
                 results.put(entry.getKey().toString(), value.toString());
             }
         }
         model.put("results", results);
      }

      model.put("message", "Successfully queued action [" + actionName + "] on " + targetNodeRefsString.toString());

      return model;
   }
}
