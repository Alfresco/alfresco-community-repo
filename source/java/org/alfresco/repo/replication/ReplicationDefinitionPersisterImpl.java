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

package org.alfresco.repo.replication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * This class provides the implementation of ReplicationDefinition persistence.
 * 
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationDefinitionPersisterImpl implements ReplicationDefinitionPersister
{
    /** Reference to the replication action space node */
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final NodeRef REPLICATION_ACTION_ROOT_NODE_REF = new NodeRef(SPACES_STORE, "replication_actions_space");
    protected static final Set<QName> ACTION_TYPES = new HashSet<QName>(
          Arrays.asList(new QName[] { ActionModel.TYPE_ACTION }));
    

    /* Injected services */
    private NodeService nodeService;
    private RuntimeActionService runtimeActionService;

    /**
     * Injects the NodeService bean.
     * 
     * @param nodeService the NodeService.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Injects the RuntimeActionService bean.
     * 
     * @param runtimeActionService the RuntimeActionService.
     */
    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }

    public List<ReplicationDefinition> loadReplicationDefinitions()
    {
        checkReplicationActionRootNodeExists();

        // Note that in the call to getChildAssocs below, only the specified
        // types are included.
        // Subtypes of the type action:action will not be returned.

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(REPLICATION_ACTION_ROOT_NODE_REF, ACTION_TYPES);

        List<ReplicationDefinition> replicationActions = new ArrayList<ReplicationDefinition>(childAssocs.size());
        for (ChildAssociationRef actionAssoc : childAssocs)
        {
            Action nextAction = runtimeActionService.createAction(actionAssoc.getChildRef());
            replicationActions.add(new ReplicationDefinitionImpl(nextAction));
        }

        return replicationActions;
    }
    
    public List<ReplicationDefinition> loadReplicationDefinitions(String targetName)
    {
        if (targetName == null)
        {
            throw new NullPointerException("Unexpected null target");
        }

        List<ReplicationDefinition> allReplicationDefinitions = this.loadReplicationDefinitions();

        List<ReplicationDefinition> filteredReplicationDefinitions = new ArrayList<ReplicationDefinition>();
        for (ReplicationDefinition replicationAction : allReplicationDefinitions)
        {
            if (targetName.equals(replicationAction.getTargetName()))
            {
               filteredReplicationDefinitions.add(replicationAction);
            }
        }

        return filteredReplicationDefinitions;
    }


    public ReplicationDefinition loadReplicationDefinition(String replicationDefinitionName)
    {
       return loadReplicationDefinition( buildReplicationQName(replicationDefinitionName) );
    }
    public ReplicationDefinition loadReplicationDefinition(QName replicationDefinitionName)
    {
        NodeRef actionNode = findActionNode(replicationDefinitionName);
        if (actionNode != null)
        {
            Action action = runtimeActionService.createAction(actionNode);
            return new ReplicationDefinitionImpl(action);
        }
        else
            return null;
    }
    
    public void renameReplicationDefinition(String oldReplicationName, String newReplicationName)
    {
        renameReplicationDefinition(
              buildReplicationQName(oldReplicationName),
              buildReplicationQName(newReplicationName)
        );
    }
    public void renameReplicationDefinition(QName oldReplicationName, QName newReplicationName)
    {
        NodeRef actionNode = findActionNode(oldReplicationName);
        if(actionNode == null)
        {
           // No current definition with this name
           // So, nothing to do
           return;
        }
        
        // Ensure the destination name is free
        if(findActionNode(newReplicationName) != null)
        {
           throw new ReplicationServiceException("Can't rename to '" + newReplicationName + 
                 "' as a definition with that name already exists");
        }
        
        // Rename the node
        nodeService.moveNode(
              actionNode, REPLICATION_ACTION_ROOT_NODE_REF,
              ContentModel.ASSOC_CONTAINS, newReplicationName
        );
        
        // Update the definition properties
        ReplicationDefinition rd = loadReplicationDefinition(newReplicationName);
        rd.setParameterValue(
              ReplicationDefinitionImpl.REPLICATION_DEFINITION_NAME,
              newReplicationName
        );
        saveReplicationDefinition(rd);

        // All done
    }

    public void saveReplicationDefinition(ReplicationDefinition replicationAction)
    {
        NodeRef actionNodeRef = findOrCreateActionNode(replicationAction);

        // TODO Serialize using JSON content instead.
        // The current serialization mechanism creates a complex content model
        // structure which is verbose and a JSON-based approach using a simplified
        // content model perhaps could offer performance improvements.
        runtimeActionService.saveActionImpl(actionNodeRef, replicationAction);
    }
    
    public void deleteReplicationDefinition(ReplicationDefinition replicationAction)
    {
       QName actionName = replicationAction.getReplicationQName();
       NodeRef actionNode = findActionNode(actionName);
       if(actionNode != null) {
          nodeService.deleteNode(actionNode);
       }
    }
    
    private NodeRef findActionNode(QName replicationDefinitionName)
    {
        checkReplicationActionRootNodeExists();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(//
                    REPLICATION_ACTION_ROOT_NODE_REF,//
                    ContentModel.ASSOC_CONTAINS,//
                    replicationDefinitionName);
        if (childAssocs.isEmpty())
        {
            return null;
        }
        else
        {
            if (childAssocs.size() > 1)
            {
                throw new ReplicationServiceException("Multiple replication definitions with the name: "
                        + replicationDefinitionName + " exist!");
            }
            return childAssocs.get(0).getChildRef();
        }
    }

    private NodeRef findOrCreateActionNode(ReplicationDefinition replicationAction)
    {
        QName actionName = replicationAction.getReplicationQName();
        NodeRef actionNode = findActionNode(actionName);
        if (actionNode == null)
        {
            actionNode = runtimeActionService.createActionNodeRef(//
                        replicationAction,//
                        REPLICATION_ACTION_ROOT_NODE_REF,//
                        ContentModel.ASSOC_CONTAINS,//
                        actionName);
        }
        return actionNode;
    }

    /**
     * This method checks whether the folder containing Replication Action nodes
     * exists.
     * 
     * @throws ReplicationServiceException if the folder node does not exist.
     */
    private void checkReplicationActionRootNodeExists()
    {
        if (nodeService.exists(REPLICATION_ACTION_ROOT_NODE_REF) == false)
        {
            throw new ReplicationServiceException("Unable to find replication action root node.");
        }
    }
    
    private static QName buildReplicationQName(String name)
    {
       return QName.createQName(null, name);
    }
}
