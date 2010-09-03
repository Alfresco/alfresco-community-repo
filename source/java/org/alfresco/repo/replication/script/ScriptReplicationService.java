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
package org.alfresco.repo.replication.script;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script object representing the replication service.
 * 
 * @author Nick Burch
 */
public class ScriptReplicationService extends BaseScopableProcessorExtension
{
    private static Log logger = LogFactory.getLog(ScriptReplicationService.class);
    
    /** The Services registry */
    private ServiceRegistry serviceRegistry;
    private ReplicationService replicationService;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry the service registry.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Set the replication service
     * 
     * @param replicationService the replication service.
     */
    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
    
    /**
     * Creates a new {@link ScriptReplicationDefinition} and sets the replication name and
     * the description to the specified values.
     * 
     * @param replicationName A unique identifier used to specify the created
     *            {@link ScriptReplicationDefinition}.
     * @param description A description of the replication
     * @return the created {@link ScriptReplicationDefinition}.
     * @see org.alfresco.service.cmr.replication.ReplicationService#createReplicationDefinition(String, String)
     */
    public ScriptReplicationDefinition createReplicationDefinition(String replicationName, String description)
    {
    	 if (logger.isDebugEnabled())
    	 {
          StringBuilder msg = new StringBuilder();
    		 msg.append("Creating ScriptReplicationDefinition [")
    		    .append(replicationName).append(", ")
    		    .append(description).append("]");
    		 logger.debug(msg.toString());
    	 }
    	 
    	 ReplicationDefinition replicationDefinition = replicationService.createReplicationDefinition(replicationName, description);
       return new ScriptReplicationDefinition(serviceRegistry, replicationService, this.getScope(), replicationDefinition);
    }
    
    public void saveReplicationDefinition(ScriptReplicationDefinition definition)
    {
       if (logger.isDebugEnabled())
       {
          StringBuilder msg = new StringBuilder();
          msg.append("Saving ScriptReplicationDefinition [")
             .append(definition.getReplicationName()).append(", ")
             .append(definition.getDescription()).append("]");
          logger.debug(msg.toString());
       }
       
       ReplicationDefinition replicationDefinition = definition.getReplicationDefinition();
       replicationService.saveReplicationDefinition(replicationDefinition);
    }
    
    public ScriptReplicationDefinition loadReplicationDefinition(String replicationName)
    {
        ReplicationDefinition replicationDefinition = replicationService.loadReplicationDefinition(replicationName);
        if(replicationDefinition == null)
           return null;
        return new ScriptReplicationDefinition(serviceRegistry, replicationService, this.getScope(), replicationDefinition);
    }
    
    public ScriptReplicationDefinition[] loadReplicationDefinitions()
    {
        List<ReplicationDefinition> definitions = replicationService.loadReplicationDefinitions();
        return toScriptReplicationDefinitions(definitions);
    }

    public ScriptReplicationDefinition[] loadReplicationDefinitions(String targetName)
    {
        List<ReplicationDefinition> definitions = replicationService.loadReplicationDefinitions(targetName);
        return toScriptReplicationDefinitions(definitions);
    }
    
    public void replicate(ScriptReplicationDefinition definition)
    {
        replicationService.replicate(definition.getReplicationDefinition());
    }

    private ScriptReplicationDefinition[] toScriptReplicationDefinitions(List<ReplicationDefinition> definitions)
    {
        ScriptReplicationDefinition[] scriptDefs = new ScriptReplicationDefinition[definitions.size()];
        for(int i=0; i<scriptDefs.length; i++)
        {
           ReplicationDefinition def = definitions.get(i);
           scriptDefs[i] = new ScriptReplicationDefinition(serviceRegistry, replicationService, this.getScope(), def);
        }
        return scriptDefs;
    }
}
