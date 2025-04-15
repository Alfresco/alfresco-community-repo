/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.patch.v35;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel.RM_CUSTOM_URI;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASSOC_FROZEN_CONTENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patch to create new hold child association to link the record to the hold
 * <p>
 * See: https://alfresco.atlassian.net/browse/APPS-659
 *
 * @since 3.5
 */
public class RMv35HoldNewChildAssocPatch extends AbstractModulePatch
{
    /** logger */
    protected static final Logger LOGGER = LoggerFactory.getLogger(RMv35HoldNewChildAssocPatch.class);

    /** A name for the associations created by this patch. */
    protected static final QName PATCH_ASSOC_NAME = QName.createQName(RM_CUSTOM_URI,
            RMv35HoldNewChildAssocPatch.class.getSimpleName());

    /** The batch size for processing frozen nodes. */
    private int batchSize = 1000;

    /**
     * File plan service interface
     */
    private FilePlanService filePlanService;

    /**
     * Hold service interface.
     */
    private HoldService holdService;

    /**
     * Interface for public and internal node and store operations.
     */
    private NodeService nodeService;

    private BehaviourFilter behaviourFilter;

    /**
     * Setter for fileplanservice
     *
     * @param filePlanService
     *            File plan service interface
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Setter for hold service
     *
     * @param holdService
     *            Hold service interface.
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * Setter for node service
     *
     * @param nodeService
     *            Interface for public and internal node and store operations.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public BehaviourFilter getBehaviourFilter()
    {
        return behaviourFilter;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Setter for maximum batch size
     *
     * @param maxBatchSize
     *            The max amount of associations to be created between the frozen nodes and the hold in a transaction
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    @Override
    public void applyInternal()
    {
        behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

        try
        {
            int patchedNodesCounter = 0;

            for (NodeRef filePlan : filePlanService.getFilePlans())
            {
                for (NodeRef hold : holdService.getHolds(filePlan))
                {
                    LOGGER.debug("Analyzing hold {}", hold.getId());

                    BatchWorker batchWorker = new BatchWorker(hold);

                    LOGGER.debug("Hold has {} items to be analyzed", batchWorker.getWorkSize());

                    while (batchWorker.hasMoreResults())
                    {
                        processBatch(hold, batchWorker);
                    }

                    LOGGER.debug("Patched {} items in hold", batchWorker.getTotalPatchedNodes());

                    patchedNodesCounter += batchWorker.getTotalPatchedNodes();
                }
            }

            LOGGER.debug("Patch applied to {} children across all holds", patchedNodesCounter);
        }
        finally
        {
            behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }
    }

    private void processBatch(NodeRef hold, BatchWorker batch)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            Collection<ChildAssociationRef> childRefs = batch.getNextWork();

            LOGGER.debug("Processing batch of {} children in hold", childRefs.size());

            for (ChildAssociationRef child : childRefs)
            {
                NodeRef childNodeRef = child.getChildRef();

                if (!isChildContainedByHold(hold, childNodeRef))
                {
                    nodeService.addChild(hold, childNodeRef, ASSOC_CONTAINS, PATCH_ASSOC_NAME);
                    batch.countPatchedNode();
                }
            }

            return null;
        }, false, true);
    }

    private boolean isChildContainedByHold(NodeRef hold, NodeRef child)
    {
        // In testing we found that this was returning more than just "contains" associations.
        // Possibly this is due to the code in Node2ServiceImpl.getParentAssocs not using the second
        // parameter.
        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(child, ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        return parentAssocs.stream()
                .anyMatch(entry -> entry.getParentRef().equals(hold) && entry.getTypeQName().equals(ASSOC_CONTAINS));
    }

    private class BatchWorker
    {
        NodeRef hold;
        int totalPatchedNodes = 0;
        int workSize;
        Iterator<ChildAssociationRef> iterator;

        public BatchWorker(NodeRef hold)
        {
            this.hold = hold;
            setupHold();
        }

        public boolean hasMoreResults()
        {
            return iterator == null ? true : iterator.hasNext();
        }

        public void countPatchedNode()
        {
            this.totalPatchedNodes += 1;
        }

        public int getTotalPatchedNodes()
        {
            return totalPatchedNodes;
        }

        public int getWorkSize()
        {
            return workSize;
        }

        public void setupHold()
        {
            // Get child assocs without preloading
            List<ChildAssociationRef> holdChildren = nodeService.getChildAssocs(hold, ASSOC_FROZEN_CONTENT,
                    RegexQNamePattern.MATCH_ALL, Integer.MAX_VALUE, false);
            this.iterator = holdChildren.listIterator();
            this.workSize = holdChildren.size();
        }

        public Collection<ChildAssociationRef> getNextWork()
        {
            List<ChildAssociationRef> frozenNodes = new ArrayList<ChildAssociationRef>(batchSize);
            while (iterator.hasNext() && frozenNodes.size() < batchSize)
            {
                frozenNodes.add(iterator.next());
            }
            return frozenNodes;
        }

    }

}
