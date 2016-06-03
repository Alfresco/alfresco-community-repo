package org.alfresco.repo.web.scripts.replication;

import java.util.Map;

import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationDefinitionGet extends AbstractReplicationWebscript
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
      
       // Have it turned into simple models
       return modelBuilder.buildDetails(replicationDefinition);
   }
}