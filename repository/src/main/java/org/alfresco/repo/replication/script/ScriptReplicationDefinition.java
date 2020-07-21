/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.replication.script;

import java.util.List;

import org.alfresco.repo.jscript.ScriptAction;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mozilla.javascript.Scriptable;

/**
 * ReplicationDefinition JavaScript Object. This class is a JavaScript-friendly wrapper for
 * the {@link ReplicationDefinition replicationDefinition} class.
 * 
 * @author Nick Burch
 * @see org.alfresco.service.cmr.replication.ReplicationDefinition
 */
public final class ScriptReplicationDefinition extends ScriptAction
{
   private static final long serialVersionUID = -6729144733846247372L;
   
   private ReplicationService replicationService;

   public ScriptReplicationDefinition(ServiceRegistry serviceRegistry, ReplicationService replicationService, 
            Scriptable scope, ReplicationDefinition replicationDefinition)
    {
    	 super(serviceRegistry, (Action)replicationDefinition, null);
    	 this.replicationService = replicationService;
    }
    
    /**
     * Returns the name of this replication definition
     * 
     * @return the name which uniquely identifies this replication definition.
     */
    public String getReplicationName()
    {
        return getReplicationDefinition().getReplicationName();
    }
    
    public String getDescription()
    {
        return getReplicationDefinition().getDescription();
    }
    
    public String getTargetName()
    {
        return getReplicationDefinition().getTargetName();
    }
    public void setTargetName(String target)
    {
        getReplicationDefinition().setTargetName(target);
    }
    
    public ScriptNode[] getPayload()
    {
        List<NodeRef> payload = getReplicationDefinition().getPayload();
        ScriptNode[] nodes = new ScriptNode[payload.size()];
        
        for(int i=0; i<nodes.length; i++)
        {
           nodes[i] = new ScriptNode(payload.get(i), services);
        }
        
        return nodes;
    }
    public void setPayload(ScriptNode[] payloadNodes)
    {
        List<NodeRef> payload = getReplicationDefinition().getPayload();
        payload.clear();
        
        for(ScriptNode payloadNode : payloadNodes)
        {
           payload.add(payloadNode.getNodeRef());
        }
    }

    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
            .append("[").append(getReplicationName()).append("]");

        return msg.toString();
    }
    
    ReplicationDefinition getReplicationDefinition()
    {
        return (ReplicationDefinition)action;
    }
    
    /**
     * Triggers the execution of the replication.
     */
    public void replicate()
    {
        executeImpl(null);
    }
    
    @Override
    protected void executeImpl(ScriptNode node)
    {
       ReplicationDefinition replicationDefinition = getReplicationDefinition();
       replicationService.replicate(replicationDefinition);
    }
}
