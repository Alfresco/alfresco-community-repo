/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.patch.v74;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.behaviour.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patch to update disposition properties in all those folders which are moved from one category to another category
 * and missing disposition properties
 */
public class RMv74UpdateDispositionPropertiesPatch extends AbstractModulePatch
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RMv74UpdateDispositionPropertiesPatch.class);
    private NodeDAO nodeDAO;

    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;

    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    private RecordsManagementSearchBehaviour recordsManagementSearchBehaviour;

    /** How many operations in a transaction */
    private int batchSize = 1000;
    /** How many nodes do we query each time */
    private int querySize = 5000;
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }
    public void setQuerySize(int querySize)
    {
        this.querySize = querySize;
    }
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    public void setRecordsManagementQueryDAO(RecordsManagementQueryDAO recordsManagementQueryDAO)
    {
        this.recordsManagementQueryDAO = recordsManagementQueryDAO;
    }
    public void setRecordsManagementSearchBehaviour(RecordsManagementSearchBehaviour recordsManagementSearchBehaviour)
    {
        this.recordsManagementSearchBehaviour = recordsManagementSearchBehaviour;
    }

    @Override
    public void applyInternal()
    {
        LOGGER.info("Starting execution of patch RMv74UpdateDispositionPropertiesPatch.");

        behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        try
        {
            BatchProcessor batchProcessor = new BatchProcessor();

            while (batchProcessor.hasNext())
            {
                batchProcessor.process();
            }

            LOGGER.info("Finished execution of patch RMv74UpdateDispositionPropertiesPatch ", batchProcessor.getTotalNodesProcessed());
        }
        finally
        {
            behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
        }
    }
    private class BatchProcessor
    {
        long minNodeId;
        long maxNodeId;
        long nextNodeId;
        long lastNodeProcessed;
        int counter;
        int totalCounter;

        public BatchProcessor()
        {
            this.minNodeId = nodeDAO.getMinNodeId();
            this.maxNodeId = nodeDAO.getMaxNodeId();
            this.nextNodeId = minNodeId;
            this.counter = 0;
            this.lastNodeProcessed = 0;
        }

        public int getTotalNodesProcessed()
        {
            return totalCounter;
        }

        public boolean hasNext()
        {
            return nextNodeId <= maxNodeId;
        }

        public void process()
        {
            resetCounter();

            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

                Long currentNodeId = nextNodeId;
                // While we haven't reached our batchSize and still have nodes to verify, keep processing
                while (counter < batchSize && nextNodeId <= maxNodeId)
                {
                    // Set upper value for query
                    Long upperNodeId = nextNodeId + querySize;

                    // Get nodes with aspects from node id nextNodeId to upperNodeId, ordered by node id and add/remove the aspect
                    updateDispositionPropertiesInFolders(currentNodeId, upperNodeId);
                    setNextNodeId();
                }

                LOGGER.debug("Processed batch [{},{}]. Changed nodes: {}", currentNodeId, lastNodeProcessed, counter);
                return true;
            }, false, true);
        }

        private void updateDispositionPropertiesInFolders(Long currentNode, Long upperNodeId)
        {
            List<NodeRef> folders = recordsManagementQueryDAO.getRecordFoldersWithSchedules(currentNode, upperNodeId);
            for (NodeRef folder : folders)
            {
                recordsManagementSearchBehaviour.onAddDispositionLifecycleAspect(folder, null);
                lastNodeProcessed = nodeDAO.getNodePair(folder).getFirst();
                incrementCounter();
            }
        }

        private void setNextNodeId()
        {
             /*If the last query did not return results, the lastNodeProcessed will be lower than the previous
             nextNodeId as it would be unchanged.*/
            if (lastNodeProcessed < nextNodeId)
            {
                this.nextNodeId += querySize;
                return;
            }

            // Next node id should be the last node id processed +1
            this.nextNodeId = lastNodeProcessed + 1;
        }

        private void resetCounter()
        {
            this.counter = 0;
        }

        private void incrementCounter()
        {
            this.counter++;
            this.totalCounter++;
        }
    }
}
