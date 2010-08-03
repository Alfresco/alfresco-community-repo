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
package org.alfresco.repo.web.scripts.replication;

import java.util.Map;

import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Burch
 * @since 3.4
 */
public abstract class AbstractReplicationWebscript extends DeclarativeWebScript
{
    protected NodeService nodeService;
    protected ReplicationService replicationService;
    protected ActionTrackingService actionTrackingService;
    
    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionTrackingService(ActionTrackingService actionTrackingService) 
    {
        this.actionTrackingService = actionTrackingService;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
       ReplicationModelBuilder modelBuilder = new ReplicationModelBuilder(
             nodeService, replicationService, actionTrackingService
       );
       return buildModel(modelBuilder, req, status, cache);
    }
    
    /**
     * Updates properties on the definition, based on the JSON.
     * Doesn't save the definition
     * Doesn't change the name
     */
    protected void updateDefinitionProperties(ReplicationDefinition replicationDefinition, JSONObject json) 
    throws JSONException 
    {
       if(json.has("description")) {
          String description = json.getString("description");
          replicationDefinition.setDescription(description);
       }

       if(json.has("targetName")) {
          String targetName = json.getString("targetName");
          replicationDefinition.setTargetName(targetName);
       }
       
       if(json.has("enabled")) {
          boolean enabled = json.getBoolean("enabled");
          replicationDefinition.setEnabled(enabled);
       }
       
       if(json.has("payload")) {
          replicationDefinition.getPayload().clear();
          JSONArray payload = json.getJSONArray("payload");
          for(int i=0; i<payload.length(); i++) {
             String node = payload.getString(i);
             replicationDefinition.getPayload().add(
                   new NodeRef(node)
             );
          }
       }
    }
    
    protected abstract Map<String, Object> buildModel(
          ReplicationModelBuilder modelBuilder,
          WebScriptRequest req,
          Status status, Cache cache
    );
}