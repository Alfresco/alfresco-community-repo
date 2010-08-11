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

import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationDefinitionDelete extends AbstractReplicationWebscript
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
       
       // Delete it
       replicationService.deleteReplicationDefinition(replicationDefinition);
       
       // Report that we have deleted it
       status.setCode(Status.STATUS_NO_CONTENT);
       status.setMessage("Replication Definition deleted");
       status.setRedirect(true);
       return null;
   }
}