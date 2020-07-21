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

package org.alfresco.repo.quickshare;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryActionPersister;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Default implementation of the {@link QuickShareLinkExpiryActionPersister}.
 * It is responsible for persisting and retrieving the quick share link expiry actions.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class QuickShareLinkExpiryActionPersisterImpl implements QuickShareLinkExpiryActionPersister
{
    protected static final NodeRef QUICK_SHARE_LINK_EXPIRY_ACTIONS_ROOT = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                "shared_link_expiry_actions_space");

    /* Injected services */
    private NodeService nodeService;
    private RuntimeActionService runtimeActionService;
    private BehaviourFilter behaviourFilter;
    private ImporterBootstrap importerBootstrap;
    private Properties bootstrapView;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    public void setBootstrapView(Properties bootstrapView)
    {
        this.bootstrapView = bootstrapView;
    }

    @Override
    public void saveQuickShareLinkExpiryAction(QuickShareLinkExpiryAction linkExpiryAction)
    {
        ParameterCheck.mandatory("linkExpiryAction", linkExpiryAction);

        NodeRef actionNodeRef = findOrCreateActionNode(linkExpiryAction);
        try
        {
            behaviourFilter.disableBehaviour(actionNodeRef);
            runtimeActionService.saveActionImpl(actionNodeRef, linkExpiryAction);
        }
        finally
        {
            behaviourFilter.enableBehaviour(actionNodeRef);
        }
    }

    @Override
    public NodeRef getQuickShareLinkExpiryActionNode(QName linkExpiryActionName)
    {
        ParameterCheck.mandatory("linkExpiryActionName", linkExpiryActionName);

        NodeRef rootNodeRef = getOrCreateActionsRootNodeRef();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(rootNodeRef, ContentModel.ASSOC_CONTAINS, linkExpiryActionName);
        if (!childAssocs.isEmpty())
        {
            if (childAssocs.size() > 1)
            {
                throw new QuickShareLinkExpiryActionException(
                            "Multiple quick share link expiry actions with the name: " + linkExpiryActionName + " exist!");
            }
            return childAssocs.get(0).getChildRef();
        }
        return null;
    }

    @Override
    public QuickShareLinkExpiryAction loadQuickShareLinkExpiryAction(QName linkExpiryActionName)
    {
        NodeRef actionNode = getQuickShareLinkExpiryActionNode(linkExpiryActionName);
        return loadQuickShareLinkExpiryAction(actionNode);
    }

    @Override
    public QuickShareLinkExpiryAction loadQuickShareLinkExpiryAction(NodeRef linkExpiryActionNodeRef)
    {
        if (linkExpiryActionNodeRef != null)
        {
            Action action = runtimeActionService.createAction(linkExpiryActionNodeRef);
            return new QuickShareLinkExpiryActionImpl(action);
        }

        return null;
    }

    @Override
    public void deleteQuickShareLinkExpiryAction(QuickShareLinkExpiryAction linkExpiryAction)
    {
        ParameterCheck.mandatory("linkExpiryAction", linkExpiryAction);

        NodeRef actionNodeRef = findOrCreateActionNode(linkExpiryAction);
        if (actionNodeRef != null)
        {
            nodeService.deleteNode(actionNodeRef);
        }
    }

    private NodeRef findOrCreateActionNode(QuickShareLinkExpiryAction linkExpiryAction)
    {
        QName actionQName = linkExpiryAction.getActionQName();
        NodeRef actionNode = getQuickShareLinkExpiryActionNode(actionQName);
        if (actionNode == null)
        {
            NodeRef rootNodeRef = getOrCreateActionsRootNodeRef();
            actionNode = runtimeActionService.createActionNodeRef(linkExpiryAction, rootNodeRef, ContentModel.ASSOC_CONTAINS, actionQName);
        }
        return actionNode;
    }

    /**
     * Gets the folder containing quick share link expiry action nodes.
     * If it doesn't exist then it tries to create it.
     *
     * @throws QuickShareLinkExpiryActionException if the folder node can't be created.
     */
    private NodeRef getOrCreateActionsRootNodeRef()
    {
        if (!nodeService.exists(QUICK_SHARE_LINK_EXPIRY_ACTIONS_ROOT))
        {
            //import
            // This lazy create approach, avoids the need to create a patch for existing repo.
            List<Properties> singletonList = new ArrayList<>();
            singletonList.add(bootstrapView);
            importerBootstrap.setBootstrapViews(singletonList);
            importerBootstrap.setUseExistingStore(true);
            importerBootstrap.bootstrap();

            // if still doesn't exist, throw an exception.
            if (!nodeService.exists(QUICK_SHARE_LINK_EXPIRY_ACTIONS_ROOT))
            {
                throw new QuickShareLinkExpiryActionException("Couldn't import the quick share link expiry actions root node.");
            }
        }
        return QUICK_SHARE_LINK_EXPIRY_ACTIONS_ROOT;
    }
}
