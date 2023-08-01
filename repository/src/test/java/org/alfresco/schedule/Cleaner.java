/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.schedule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test implementation similar to the trash can cleaner job implementation that will be used in
 * {@link AbstractScheduledLockedJobTest}
 * 
 * @author Tiago Salvado
 */
public class Cleaner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Cleaner.class);

    private final NodeService nodeService;
    private final TransactionService transactionService;

    private final String archiveStoreUrl = "archive://SpacesStore";
    private final int deleteBatchCount;
    private List<NodeRef> nodesToClean;

    private int numErrors = 0;

    private static final int REMOVAL_WAIT_TIME_MS = 5000;

    /**
     *
     * @param nodeService
     * @param transactionService
     * @param deleteBatchCount
     */
    public Cleaner(NodeService nodeService, TransactionService transactionService, int deleteBatchCount)
    {
        this.nodeService = nodeService;
        this.transactionService = transactionService;
        this.deleteBatchCount = deleteBatchCount;
    }

    /**
     *
     * It deletes the {@link java.util.List List} of {@link org.alfresco.service.cmr.repository.NodeRef NodeRef}
     * received as argument.
     *
     * @param nodes
     *
     *            return The number of deleted nodes
     */
    private int deleteNodes(List<NodeRef> nodes)
    {
        AtomicInteger deletedNodes = new AtomicInteger();
        for (NodeRef nodeRef : nodes)
        {
            // create a new transaction for each deletion so the transactions are smaller and the progress of the
            // cleaner is not lost in case of any problems encountered during the job execution
            AuthenticationUtil.runAsSystem(() -> {
                RetryingTransactionCallback<Void> txnWork = () -> {
                    try
                    {
                        nodeService.deleteNode(nodeRef);
                    }
                    catch (InvalidNodeRefException inre)
                    {
                        numErrors++;
                    }
                    deletedNodes.getAndIncrement();
                    // Waiting REMOVAL_WAIT_TIME_MS seconds for next deletion so we don't need to have many nodes on the trash can
                    Thread.sleep(REMOVAL_WAIT_TIME_MS);
                    return null;
                };
                return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
            });
        }
        return deletedNodes.get();
    }

    /**
     *
     * It returns the {@link java.util.List List} of {@link org.alfresco.service.cmr.repository.NodeRef NodeRef} of the
     * archive store set to be deleted according to configuration for <b>deleteBatchCount</b>.
     *
     * @return
     */
    private List<NodeRef> getBatchToDelete()
    {
        return getChildAssocs().stream().map(ChildAssociationRef::getChildRef).collect(Collectors.toList());
    }

    /**
     *
     * It will return the first {@link #deleteBatchCount}
     * {@link org.alfresco.service.cmr.repository.ChildAssociationRef}s of type {@link ContentModel}.ASSOC_CHILDREN from
     * the archive store set.
     *
     * @return
     */
    private List<ChildAssociationRef> getChildAssocs()
    {
        StoreRef archiveStore = new StoreRef(archiveStoreUrl);
        NodeRef archiveRoot = nodeService.getRootNode(archiveStore);
        return nodeService.getChildAssocs(archiveRoot, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL, deleteBatchCount,
                false);
    }

    /**
     *
     * The method that will clean the specified <b>archiveStoreUrl</b> to the limits defined by the values set for
     * <b>deleteBatchCount</b>.
     */
    public void clean()
    {
        LOGGER.info("Running TestCleaner");

        // Retrieve in a new read-only transaction the list of nodes to be deleted by the TestCleaner
        AuthenticationUtil.runAsSystem(() -> {
            RetryingTransactionCallback<Void> txnWork = () -> {
                nodesToClean = getBatchToDelete();

                LOGGER.info(String.format("Number of nodes to delete: %s", nodesToClean.size()));

                return null;
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, true, true);
        });

        int deletedNodes = deleteNodes(nodesToClean);

        LOGGER.info(String.format("TestCleaner finished. Number of deleted nodes: %s", deletedNodes));
    }

    public boolean hasErrors()
    {
        return numErrors > 0;
    }
}
