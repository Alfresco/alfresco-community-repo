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

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.replication.ReplicationDefinition;
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
public class ReplicationDefinitionPut extends AbstractReplicationWebscript
{
   @Override
   protected Map<String, Object> buildModel(ReplicationModelBuilder modelBuilder, 
                                            WebScriptRequest req, Status status, Cache cache)
   {
       // Which definition did they ask for?
       String replicationDefinitionName = 
          req.getServiceMatch().getTemplateVars().get("replication_definition_name");
       ReplicationDefinition replicationDefinition =
          replicationService.loadReplicationDefinition(replicationDefinitionName);
      
       // Does it exist?
       if(replicationDefinition == null) {
          throw new WebScriptException(
                Status.STATUS_NOT_FOUND, 
                "No Replication Definition found with that name"
          );
       }
       
       // Grab the JSON, and prepare to update 
       try 
       {
           JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));
           
           // Are they trying to rename?
           if(json.has("name")) {
              String jsonName = json.getString("name");
              if(! jsonName.equals(replicationDefinitionName)) {
                 // Name has changed, ensure the new name is spare
                 if(replicationService.loadReplicationDefinition(jsonName) != null) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The specified new name is already in use");
                 }

                 // Rename it
                 replicationService.renameReplicationDefinition(
                       replicationDefinitionName,
                       jsonName
                 );
                 
                 // And grab the updated version post-rename
                 replicationDefinition = replicationService.loadReplicationDefinition(jsonName);
              }
           }
           
           // Update everything else
           updateDefinitionProperties(replicationDefinition, json);
           
           // Save the changes
           replicationService.saveReplicationDefinition(replicationDefinition);
       }
       catch (IOException iox)
       {
           throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from request.", iox);
       }
       catch (JSONException je)
       {
           throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request.", je);
       }
      
       // Return the new details on it
       return modelBuilder.buildDetails(replicationDefinition);
   }
}