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
public class ReplicationDefinitionsPost extends AbstractReplicationWebscript
{
   @Override
   protected Map<String, Object> buildModel(ReplicationModelBuilder modelBuilder, 
                                            WebScriptRequest req, Status status, Cache cache)
   {
       // Create our definition
       ReplicationDefinition replicationDefinition = null;
       JSONObject json;
       
       try 
       {
           json = new JSONObject(new JSONTokener(req.getContent().getContent()));
           
           // Check for the required parameters
           if(! json.has("name"))
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "name is required but wasn't supplied");
           if(! json.has("description"))
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "description is required but wasn't supplied");
           
           // Ensure one doesn't already exist with that name
           String name = json.getString("name"); 
           if(replicationService.loadReplicationDefinition(name) != null)
           {
              throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A replication definition already exists with that name");
           }
           
           // Create the definition
           replicationDefinition = replicationService.createReplicationDefinition(
                 name, json.getString("description")
           );
           
           // Set the extra parts
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
      
       // Return the details on it
       return modelBuilder.buildDetails(replicationDefinition);
   }
}