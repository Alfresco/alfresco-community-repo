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

import java.util.ArrayList;
import java.util.HashMap;
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
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
   {
       List<ReplicationDefinition> definitions = replicationService.loadReplicationDefinitions();
       List<Map<String,Object>> models = new ArrayList<Map<String,Object>>();
       
       for(ReplicationDefinition rd : definitions) {
          Map<String, Object> rdm = new HashMap<String,Object>();

          // Set the basic details
          rdm.put(DEFINITION_NAME, rd.getReplicationName());
          rdm.put(DEFINITION_ENABLED, rd.isEnabled());
          rdm.put(DEFINITION_DETAILS_URL, getDefinitionDetailsUrl(rd));
          
          // TODO - Make this more efficient by getting all
          //  the running instances in one go
          setStatus(rd, rdm);
       }
      
       // Finish up
       Map<String, Object> model = new HashMap<String,Object>();
       model.put(MODEL_DATA_LIST, models);
       return model;
   }
}