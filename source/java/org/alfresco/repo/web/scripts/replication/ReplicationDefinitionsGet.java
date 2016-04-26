package org.alfresco.repo.web.scripts.replication;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationDefinitionsGet extends AbstractReplicationWebscript
{
   @Override
   protected Map<String, Object> buildModel(ReplicationModelBuilder modelBuilder, 
                                            WebScriptRequest req, Status status, Cache cache)
   {
       // Get all the defined replication definitions
       List<ReplicationDefinition> definitions = replicationService.loadReplicationDefinitions();
       
       // How do we need to sort them?
       Comparator<Map<String,Object>> sorter = new ReplicationModelBuilder.SimpleSorterByName();
       String sort = req.getParameter("sort");
       if(sort == null) {
          // Default was set above
       } else if(sort.equalsIgnoreCase("status")) {
          sorter = new ReplicationModelBuilder.SimpleSorterByStatus();
       } else if(sort.equalsIgnoreCase("lastRun") ||
             sort.equalsIgnoreCase("lastRunTime")) {
          sorter = new ReplicationModelBuilder.SimpleSorterByLastRun();
       }
       
       // Have them turned into simple models
       return modelBuilder.buildSimpleList(definitions, sorter);
   }
}