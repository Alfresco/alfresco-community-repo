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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.SchedulerAccessorBean;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author Tiago Salvado
 */
@ContextConfiguration({"classpath:alfresco/application-context.xml", "classpath:alfresco/schedule/test-schedule-context.xml"})
public class AbstractScheduledLockedJobTest extends BaseSpringTest
{
    private static final int TOTAL_NODES = 10;
    private static final int NUM_THREADS = 2;
    private static final String ARCHIVE_STORE_URL = "archive://SpacesStore";

    private NodeService nodeService;
    private TransactionService transactionService;
    private Repository repository;

    private SchedulerAccessorBean testCleanerAccessor;
    private JobDetail testCleanerJobDetail;

    private static final Log LOGGER = LogFactory.getLog(AbstractScheduledLockedJobTest.class);

    /**
     * Sets services and job beans
     */
    @Before
    public void setUp()
    {
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        repository = (Repository) applicationContext.getBean("repositoryHelper");
    }

    @Test
    public void test() throws SchedulerException, InterruptedException
    {
        createAndDeleteNodes(TOTAL_NODES);

        assertTrue("Expected nodes haven't been created", getNumberOfNodesInTrashcan() >= TOTAL_NODES);

        CleanerThread[] threads = new CleanerThread[NUM_THREADS];

        for (int i = 0; i < NUM_THREADS; i++)
        {
            CleanerThread t = new CleanerThread(i);
            threads[i] = t;
            t.start();
            Thread.sleep(30000);
        }

        for (Thread t : threads)
        {
            t.join();
        }

        while (getNumberOfNodesInTrashcan() > 0)
        {
            Thread.sleep(2000);
        }

        for (int i = 0; i < threads.length; i++)
        {
            CleanerThread t = threads[i];
            if (t.hasErrors())
            {
                fail("An error has occurred when executing multiple cleaner jobs at the same time");
            }
        }
    }

    /**
     * Creates and deletes the specified number of nodes.
     *
     * @param archivedNodes
     *            Number of nodes to be created and added to trashcan
     */
    private void createAndDeleteNodes(int archivedNodes)
    {
        AuthenticationUtil.runAsSystem(() -> {
            RetryingTransactionHelper.RetryingTransactionCallback<Void> txnWork = () -> {
                for (int i = 0; i < archivedNodes; i++)
                {
                    addNodeToTrashcan();
                }
                return null;
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(txnWork);
        });
    }

    /**
     * Creates and deletes nodes
     */
    private void addNodeToTrashcan()
    {
        NodeRef companyHome = repository.getCompanyHome();
        String name = "Sample (" + System.currentTimeMillis() + ")";

        ChildAssociationRef association = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, name), ContentModel.TYPE_CONTENT,
                ImmutableMap.of(ContentModel.PROP_NAME, name));

        NodeRef parent = association.getChildRef();

        nodeService.deleteNode(parent);
    }

    /**
     * It returns the number of nodes present on trashcan.
     *
     * @return
     */
    private long getNumberOfNodesInTrashcan()
    {
        StoreRef storeRef = new StoreRef(ARCHIVE_STORE_URL);
        NodeRef archiveRoot = nodeService.getRootNode(storeRef);
        return nodeService.getChildAssocs(archiveRoot, ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL).size();
    }

    /**
     * 
     */
    private class CleanerThread extends Thread
    {
        private int threadNum;
        private boolean started;
        private Cleaner testCleaner;

        CleanerThread(int threadNum)
        {
            super(CleanerThread.class.getSimpleName() + "-" + threadNum);
            this.threadNum = threadNum;
        }

        @Override
        public void run()
        {
            try
            {
                testCleanerAccessor = (SchedulerAccessorBean) applicationContext.getBean("testSchedulerAccessor");
                testCleanerJobDetail = (JobDetail) applicationContext.getBean("testCleanerJobDetail");
                testCleaner = (Cleaner) testCleanerJobDetail.getJobDataMap().get("testCleaner");
                testCleanerAccessor.getScheduler().triggerJob(testCleanerJobDetail.getKey());
                LOGGER.info("Thread " + this.threadNum + " has started");
                this.started = true;
            }
            catch (SchedulerException e)
            {
                this.started = false;
            }
        }

        public boolean hasErrors()
        {
            return !started || testCleaner != null && testCleaner.hasErrors();
        }
    }
}
