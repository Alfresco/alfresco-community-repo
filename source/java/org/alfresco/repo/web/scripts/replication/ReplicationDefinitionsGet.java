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