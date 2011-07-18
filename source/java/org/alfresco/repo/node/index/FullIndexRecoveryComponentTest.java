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
package org.alfresco.repo.node.index;

import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.Transaction;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.node.index.AbstractReindexComponent.InIndex;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Checks that full index recovery is possible
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private FullIndexRecoveryComponent indexRecoverer;
    private AVMFullIndexRecoveryComponent avmIndexRecoveryComponent;
    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    private UserTransaction testTX;
    private NodeDAO nodeDAO;
    
    public void setUp() throws Exception
    {
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("lucene");
        indexRecoverer = (FullIndexRecoveryComponent) luceneSubSystem.getApplicationContext().getBean("search.indexRecoveryComponent");
        avmIndexRecoveryComponent = (AVMFullIndexRecoveryComponent) luceneSubSystem.getApplicationContext().getBean("search.avmIndexRecoveryComponent");
        nodeService = (NodeService) ctx.getBean("nodeService");
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");     
        nodeDAO = (NodeDAO) ctx.getBean("nodeDAO"); 

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
    }
    
    public void testSetup() throws Exception
    {
        
    }
    
    public void XtestDeletionReporting() throws Exception
    {
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        NodeRef folder = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}folder"), ContentModel.TYPE_FOLDER).getChildRef();

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        NodeRef[] refs = new NodeRef[20];
        for(int i = 0; i < refs.length; i++)
        {
            refs[i] = nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}file"+i), ContentModel.TYPE_CONTENT).getChildRef();
        }
        
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();
        for(int i = 0; i < refs.length; i++)
        {
            nodeService.deleteNode(refs[i]);
            testTX.commit();
            testTX = transactionService.getUserTransaction();
            testTX.begin();
        }
        
        // The following test are important but take too long ....
        
//        List<Transaction> startTxns = nodeDaoService.getTxnsByCommitTimeAscending(
//                Long.MIN_VALUE, Long.MAX_VALUE, 1, null, false);
//        InIndex startAllPresent = indexRecoverer.areTxnsInStartSample(startTxns);
//        assertEquals(InIndex.YES, startAllPresent);
//        Long maxId = nodeDaoService.getMaxTxnIdByCommitTime(Long.MAX_VALUE);
//        startTxns = nodeDaoService.getTxnsByCommitTimeAscending(
//                Long.MIN_VALUE, Long.MAX_VALUE, maxId.intValue(), null, false);
//        startAllPresent = indexRecoverer.areTxnsInStartSample(startTxns);
//        assertEquals(InIndex.INDETERMINATE, startAllPresent);
       
//        for(int i = 0; i <= maxId.intValue(); i++)
//        {
//            System.out.println("TX "+i+" is "+indexRecoverer.isTxnPresentInIndex(nodeDaoService.getTxnById(i)));
//        }
        
//        startTxns = nodeDaoService.getTxnsByCommitTimeAscending(
//                Long.MIN_VALUE, Long.MAX_VALUE, startTxns.size() - 20, null, false);
//        startAllPresent = indexRecoverer.areTxnsInStartSample(startTxns);
//        assertEquals(InIndex.YES, startAllPresent);
//        
        
        
        List<Transaction> endTxns = nodeDAO.getTxnsByCommitTimeDescending(
                Long.MIN_VALUE, Long.MAX_VALUE, 20, null, false);
        InIndex endAllPresent = indexRecoverer.areAllTxnsInEndSample(endTxns);
        assertEquals(InIndex.INDETERMINATE, endAllPresent);
        
        endTxns = nodeDAO.getTxnsByCommitTimeDescending(
                Long.MIN_VALUE, Long.MAX_VALUE, 21, null, false);
        endAllPresent = indexRecoverer.areAllTxnsInEndSample(endTxns);
        assertEquals(InIndex.INDETERMINATE, endAllPresent);
        
        endTxns = nodeDAO.getTxnsByCommitTimeDescending(
                Long.MIN_VALUE, Long.MAX_VALUE, 22, null, false);
        endAllPresent = indexRecoverer.areAllTxnsInEndSample(endTxns);
        assertEquals(InIndex.YES, endAllPresent);
    }
    
    public synchronized void testReindexing() throws Exception
    {
        indexRecoverer.setRecoveryMode(FullIndexRecoveryComponent.RecoveryMode.FULL.name());
        avmIndexRecoveryComponent.setRecoveryMode(FullIndexRecoveryComponent.RecoveryMode.FULL.name());
        // reindex
        Thread reindexThread = new Thread()
        {
            public void run()
            {
                indexRecoverer.reindex();
            }
        };
        Thread avmReindexThread = new Thread()
        {
            public void run()
            {
                avmIndexRecoveryComponent.reindex();
            }
        };
        //reindexThread.setDaemon(true);
        //avmReindexThread.setDaemon(true);
        reindexThread.start();
        avmReindexThread.start();
        
        // must allow the rebuild to complete or the test after this one will fail to validate their indexes 
        // - as they now will be deleted.
        reindexThread.join();
        avmReindexThread.join();
        
        // wait a bit and then terminate
        wait(20000);
        indexRecoverer.setShutdown(true);
        avmIndexRecoveryComponent.setShutdown(true);
        wait(20000);
    }
}
