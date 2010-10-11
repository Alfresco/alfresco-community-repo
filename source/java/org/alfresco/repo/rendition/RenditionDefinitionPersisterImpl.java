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

package org.alfresco.repo.rendition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * This class provides the implementation of RenditionDefinition persistence.
 * 
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionDefinitionPersisterImpl implements RenditionDefinitionPersister
{
    /** Reference to the rendering action space node */
    private static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    protected static final NodeRef RENDERING_ACTION_ROOT_NODE_REF = new NodeRef(SPACES_STORE, "rendering_actions_space");

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

    public List<RenditionDefinition> loadRenditionDefinitions()
    {
        checkRenderingActionRootNodeExists();

        // Note that in the call to getChildAssocs below, only the specified
        // types are included.
        // Subtypes of the type action:action will not be returned.
        Set<QName> actionTypes = new HashSet<QName>();
        actionTypes.add(ActionModel.TYPE_ACTION);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(RENDERING_ACTION_ROOT_NODE_REF, actionTypes);

        List<RenditionDefinition> renderingActions = new ArrayList<RenditionDefinition>(childAssocs.size());
        for (ChildAssociationRef actionAssoc : childAssocs)
        {
            Action nextAction = runtimeActionService.createAction(actionAssoc.getChildRef());
            renderingActions.add(new RenditionDefinitionImpl(nextAction));
        }

        return renderingActions;
    }
    
    public List<RenditionDefinition> loadRenditionDefinitions(String renditionEngineName)
    {
        if (renditionEngineName == null)
        {
            throw new NullPointerException("Unexpected null renditionEngineName");
        }

        List<RenditionDefinition> allRenditionDefinitions = this.loadRenditionDefinitions();

        List<RenditionDefinition> filteredRenditionDefinitions = new ArrayList<RenditionDefinition>();
        for (RenditionDefinition renderAction : allRenditionDefinitions)
        {
            if (renditionEngineName.equals(renderAction.getActionDefinitionName()))
            {
                filteredRenditionDefinitions.add(renderAction);
            }
        }

        return filteredRenditionDefinitions;
    }


    public RenditionDefinition loadRenditionDefinition(QName renditionDefinitionName)
    {
        NodeRef actionNode = findActionNode(renditionDefinitionName);
        if (actionNode != null)
        {
            Action action = runtimeActionService.createAction(actionNode);
            if (action instanceof CompositeAction)
            {
                CompositeAction compAction = (CompositeAction) action;
                return new CompositeRenditionDefinitionImpl(compAction);
            }
            else
            {
                return new RenditionDefinitionImpl(action);
            }
        }
        else
            return null;
    }
    
    public void saveRenditionDefinition(RenditionDefinition renderingAction)
    {
        NodeRef actionNodeRef = findOrCreateActionNode(renderingAction);

        // TODO Serialize using JSON content instead.
        // The current serialization mechanism creates a complex content model
        // structure which is verbose and a JSON-based approach using a simplified
        // content model perhaps could offer performance improvements.
        runtimeActionService.saveActionImpl(actionNodeRef, renderingAction);
    }
    
    public void deleteRenditionDefinition(RenditionDefinition renderingAction)
    {
        NodeRef actionNodeRef = findOrCreateActionNode(renderingAction);
        if(actionNodeRef != null) {
           nodeService.deleteNode(actionNodeRef);
        }
    }
    
    private NodeRef findActionNode(QName renditionDefinitionName)
    {
        checkRenderingActionRootNodeExists();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(//
                    RENDERING_ACTION_ROOT_NODE_REF,//
                    ContentModel.ASSOC_CONTAINS,//
                    renditionDefinitionName);
        if (childAssocs.isEmpty())
        {
            return null;
        }
        else
        {
            if (childAssocs.size() > 1)
            {
                throw new RenditionServiceException("Multiple rendition definitions with the name: "
                        + renditionDefinitionName + " exist!");
            }
            return childAssocs.get(0).getChildRef();
        }
    }

    private NodeRef findOrCreateActionNode(RenditionDefinition renderingAction)
    {
        QName actionName = renderingAction.getRenditionName();
        NodeRef actionNode = findActionNode(actionName);
        if (actionNode == null)
        {
            actionNode = runtimeActionService.createActionNodeRef(//
                        renderingAction,//
                        RENDERING_ACTION_ROOT_NODE_REF,//
                        ContentModel.ASSOC_CONTAINS,//
                        actionName);
        }
        return actionNode;
    }

    /**
     * This method checks whether the folder containing Rendering Action nodes
     * exists.
     * 
     * @throws RenditionServiceException if the folder node does not exist.
     */
    private void checkRenderingActionRootNodeExists()
    {
        if (nodeService.exists(RENDERING_ACTION_ROOT_NODE_REF) == false)
        {
            throw new RenditionServiceException("Unable to find rendering action root node.");
        }
    }
}
