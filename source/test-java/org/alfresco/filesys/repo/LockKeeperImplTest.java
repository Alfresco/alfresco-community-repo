/*
 * Copyright (C) 2013-2013 Alfresco Software Limited.
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
 * 
 * @Since 4.2
 */
package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * Junit tests for the LockKeeper
 * 
 * @see LockKeeper
 * 
 * @author mrogers
 */
public class LockKeeperImplTest extends TestCase
{
	private static Log logger = LogFactory.getLog(LockKeeperImplTest.class);
	
	private ApplicationContext applicationContext;
	private LockKeeper lockKeeper;
	private NodeService nodeService;
	private Repository repositoryHelper;
	private TransactionService transactionService;

    @Override
    protected void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        nodeService = (NodeService)applicationContext.getBean("nodeService");
        repositoryHelper = (Repository)this.applicationContext.getBean("repositoryHelper");
        transactionService = (TransactionService)applicationContext.getBean("transactionService");
        
        ChildApplicationContextFactory fileServersSubSystem = (ChildApplicationContextFactory) applicationContext.getBean("fileServers");
        assertNotNull("fileServers subsystem is null", fileServersSubSystem);
        lockKeeper =  (LockKeeper)fileServersSubSystem.getApplicationContext().getBean("lockKeeper");
        
    	assertNotNull("nodeService is null", nodeService);
    	assertNotNull("lockKeeper is null", lockKeeper);
    	assertNotNull("transactionService is null", transactionService);

        
        AuthenticationUtil.setRunAsUserSystem();
        AuthenticationUtil.setFullyAuthenticatedUser("bugsBunny");
    }
	
    /*
     * Tests a basic sequence of lock, refresh, remove, refresh in separate transactions
     */
    public void testBasicLockUnlock() throws Exception
	{
    	logger.debug("testBasicLockUnlock");
    	
    	final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
    	final String FILE_NAME = "LockKeeperImplTestNode";
    	
        RetryingTransactionCallback<Boolean> lockCB = new RetryingTransactionCallback<Boolean>() {
            @Override
            public Boolean execute() throws Throwable
            {            	
                NodeRef companyHome = repositoryHelper.getCompanyHome();
            	NodeRef nodeRef = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
            	
            	if(nodeRef == null)
            	{
                    ChildAssociationRef ref = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FILE_NAME), ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(ref.getChildRef(), ContentModel.PROP_NAME, FILE_NAME);
                    nodeRef = ref.getChildRef();
            	}
            	
            	logger.debug("first lock");
            	lockKeeper.addLock(nodeRef);
            	boolean locked = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
               return locked;
            }
        };
        boolean locked = tran.doInTransaction(lockCB);
        assertTrue("node not locked", locked);
        
        RetryingTransactionCallback<Void> refreshCB = new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("first refresh");
                lockKeeper.refreshAllLocks();
               return null;
            }
        };
        tran.doInTransaction(refreshCB);
        
        RetryingTransactionCallback<Boolean> removeCB = new RetryingTransactionCallback<Boolean>() {
            @Override
            public Boolean execute() throws Throwable
            {
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                NodeRef nodeRef = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
                lockKeeper.removeLock(nodeRef);
                boolean locked = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
                return locked;
            }
        };
        locked = tran.doInTransaction(removeCB);
        assertFalse("node not unlocked", locked);
        
        RetryingTransactionCallback<Void> refreshAgainCB = new RetryingTransactionCallback<Void>() {
            @Override
            public Void execute() throws Throwable
            {
                logger.debug("second refresh");
                lockKeeper.refreshAllLocks();
                return null;
            }
        };
        tran.doInTransaction(refreshAgainCB);
	}
    
    /*
     * Tests a basic sequence of lock, refresh, remove, refresh in separate transactions
     */
    public void testBasicLockUnlockSeparateTrans() throws Exception
	{
    	logger.debug("testBasicLockUnlock");
    	
    	final RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
    	
        class TestContext
        {     
            NodeRef testNodeRef;    
        };
    	
        final TestContext testContext = new TestContext();
    	
    	
        RetryingTransactionCallback<Void> lockCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
             	String FILE_NAME = "LockKeeperImplTestNode";
            	
            	NodeRef companyHome = repositoryHelper.getCompanyHome();
            	
            	NodeRef nodeRef = nodeService.getChildByName(companyHome, ContentModel.ASSOC_CONTAINS, FILE_NAME);
            	
            	if(nodeRef == null)
            	{
                    ChildAssociationRef ref = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, FILE_NAME), ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(ref.getChildRef(), ContentModel.PROP_NAME, FILE_NAME);
                    nodeRef = ref.getChildRef();
            	}
            	
            	logger.debug("first lock");
            	lockKeeper.addLock(nodeRef);
            	assertTrue("node not locked", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE));
            	
            	testContext.testNodeRef = nodeRef;
            	
                return null;
            }
        };
        tran.doInTransaction(lockCB);
    	
        RetryingTransactionCallback<Void> unlockCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
                logger.debug("remove lock");
              	lockKeeper.removeLock(testContext.testNodeRef);
            	assertFalse("node not unlocked", nodeService.hasAspect(testContext.testNodeRef, ContentModel.ASPECT_LOCKABLE));
       
               return null;
            }
        };
        
        RetryingTransactionCallback<Void> refreshCB = new RetryingTransactionCallback<Void>() {

            @Override
            public Void execute() throws Throwable
            {
            	
            	logger.debug("refresh locks");
            	lockKeeper.refreshAllLocks();
              
               return null;
            }
        };
     
        // This is the test
        tran.doInTransaction(lockCB);
        tran.doInTransaction(refreshCB);
        tran.doInTransaction(unlockCB);
        tran.doInTransaction(refreshCB);
	}
}
