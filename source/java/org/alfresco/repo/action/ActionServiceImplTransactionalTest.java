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
package org.alfresco.repo.action;

import static org.alfresco.repo.action.ActionServiceImplTest.assertBefore;

import java.util.Date;
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionTrackingServiceImplTest.SleepActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Action service tests which need careful control
 *  over the transactions they use.
 * 
 * @author Nick Burch
 */
public class ActionServiceImplTransactionalTest extends TestCase
{
    private static ConfigurableApplicationContext ctx = 
       (ConfigurableApplicationContext)ApplicationContextHelper.getApplicationContext();
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    private NodeRef nodeRef;
    private NodeRef folder;
    private NodeService nodeService;
    private ActionService actionService;
    private TransactionService transactionService;
    private RuntimeActionService runtimeActionService;
    private RetryingTransactionHelper transactionHelper;
    
    @Override
    protected void setUp() throws Exception {
        this.transactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        this.nodeService = (NodeService)ctx.getBean("nodeService");
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.runtimeActionService = (RuntimeActionService)ctx.getBean("actionService");
        this.transactionService = (TransactionService)ctx.getBean("transactionService");

        AuthenticationUtil.setRunAsUserSystem();
        
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        
        // Where to put things
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        this.nodeService.setProperty(
                this.nodeRef,
                ContentModel.PROP_CONTENT,
                new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        this.folder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        
        txn.commit();
        
        // Register the test executor, if needed
        if(!ctx.containsBean(SleepActionExecuter.NAME))
        {
           ctx.getBeanFactory().registerSingleton(
                 SleepActionExecuter.NAME,
                 new SleepActionExecuter()
           );
        }
    }
    
    /**
     * Tests that when we run an action, either
     *  synchronously or asynchronously, with it
     *  working or failing, that the action execution
     *  service correctly sets the flags
     */
    public void testExecutionTrackingOnExecution() throws Exception {
       final SleepActionExecuter sleepActionExec = 
          (SleepActionExecuter)ctx.getBean(SleepActionExecuter.NAME);
       sleepActionExec.setSleepMs(10);
       Action action;
       NodeRef actionNode;

       // We need real transactions
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       
       
       // ===========================================================
       //    Execute a transient Action that works, synchronously
       // ===========================================================
       action = createWorkingSleepAction();
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef);
       
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       
       
       // ===========================================================
       //    Execute a transient Action that fails, synchronously
       // ===========================================================
       action = createFailingMoveAction();
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       try {
          this.actionService.executeAction(action, this.nodeRef);
          fail("Action should have failed, and the error been thrown");
       } catch(Exception e) {}
       
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());

       // Tidy up from the action failure
       txn.rollback();
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       
       // ===========================================================
       //    Execute a stored Action that works, synchronously
       // ===========================================================
       action = createWorkingSleepAction();
       this.actionService.saveAction(this.nodeRef, action);
       actionNode = action.getNodeRef();
       assertNotNull(actionNode);
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef);
       
       // Check our copy
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       
       // Now re-load and check the stored one
       action = runtimeActionService.createAction(actionNode);
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());

       
       // ===========================================================
       //    Execute a stored Action that fails, synchronously
       // ===========================================================
       action = createFailingMoveAction();
       this.actionService.saveAction(this.nodeRef, action);
       actionNode = action.getNodeRef();
       String actionId = action.getId();
       assertNotNull(actionNode);
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       // Save this
       txn.commit();
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Run the action - will fail and trigger a rollback
       try {
          this.actionService.executeAction(action, this.nodeRef);
          fail("Action should have failed, and the error been thrown");
       } catch(Exception e) {}

       // Check our copy
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       
       // Wait for the post-rollback update to complete
       // (The stored one gets updated asynchronously)
       txn.rollback();
       Thread.sleep(150);
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Now re-load and check the stored one
       action = runtimeActionService.createAction(actionNode);
       assertEquals(actionId, action.getId());
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());

       // Tidy up from the action failure
       txn.commit();
       txn = transactionService.getUserTransaction();
       txn.begin();

       
       // ===========================================================
       //    Execute a transient Action that works, asynchronously
       // ===========================================================
       action = createWorkingSleepAction();
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef, false, true);
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());

       // End the transaction. Should allow the async action
       //  to be executed
       txn.commit();
       Thread.sleep(150);
       
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       
       // Put things back ready for the next check
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       
       // ===========================================================
       //    Execute a transient Action that fails, asynchronously
       // ===========================================================
       action = createFailingMoveAction();
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef, false, true);
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());
       
       // End the transaction. Should allow the async action
       //  to be executed
       txn.commit();
       Thread.sleep(150);
       
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       
       // Put things back ready for the next check
       txn = transactionService.getUserTransaction();
       txn.begin();

       
       // ===========================================================
       //    Execute a stored Action that works, asynchronously
       // ===========================================================
       action = createWorkingSleepAction();
       this.actionService.saveAction(this.nodeRef, action);
       actionNode = action.getNodeRef();
       assertNotNull(actionNode);
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef, false, true);
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());
       
       // End the transaction. Should allow the async action
       //  to be executed
       txn.commit();
       Thread.sleep(150);
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Check our copy
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       
       // Now re-load and check the stored one
       action = runtimeActionService.createAction(actionNode);
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());

       
       // ===========================================================
       //    Execute a stored Action that fails, asynchronously
       // ===========================================================
       action = createFailingMoveAction();
       this.actionService.saveAction(this.nodeRef, action);
       actionNode = action.getNodeRef();
       assertNotNull(actionNode);
       
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       this.actionService.executeAction(action, this.nodeRef, false, true);
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());
       
       // End the transaction. Should allow the async action
       //  to be executed
       // Need to wait longer, as we have two async actions
       //  that need to occur - action + record
       txn.commit();
       Thread.sleep(250);
       txn = transactionService.getUserTransaction();
       txn.begin();
       
       // Check our copy
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       
       // Now re-load and check the stored one
       action = runtimeActionService.createAction(actionNode);
       assertNotNull(action.getExecutionStartDate());
       assertNotNull(action.getExecutionEndDate());
       assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
       assertBefore(action.getExecutionEndDate(), new Date());
       assertNotNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
    }
    
    private Action createFailingMoveAction() {
       Action failingAction = this.actionService.createAction(MoveActionExecuter.NAME);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CHILDREN);
       // Create a bad node ref
       NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
       failingAction.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);
       
       return failingAction;
    }
    private Action createWorkingSleepAction() {
       Action workingAction = actionService.createAction(SleepActionExecuter.NAME);
       return workingAction;
    }
    
    /**
     * This class is only used during JUnit testing.
     * 
     * @author Neil Mc Erlean
     */
    public static class SleepActionFilter extends AbstractAsynchronousActionFilter
    {
    	public int compare(OngoingAsyncAction sae1, OngoingAsyncAction sae2)
    	{
    		// Sleep actions are always equivalent.
    		return 0;
    	}
    }
    
    /**
     * This class is only intended for use in JUnit tests.
     * 
     * @author Neil McErlean.
     */
    public static class SleepActionExecuter extends ActionExecuterAbstractBase
    {
    	public static final String NAME = "sleep-action";
    	private int sleepMs;
    	
    	private int timesExecuted = 0;
    	private void incrementTimesExecutedCount() {timesExecuted++;}
    	public int getTimesExecuted() {return timesExecuted;}
    	
    	public int getSleepMs()
    	{
    		return sleepMs;
    	}
    	
    	public void setSleepMs(int sleepMs)
    	{
    		this.sleepMs = sleepMs;
    	}
    	
    	/**
    	 * Add parameter definitions
    	 */
    	@Override
    	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    	{
    		// Intentionally empty
    	}

    	@Override
    	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    		try
    		{
    			Thread.sleep(sleepMs);
    		}
    		catch (InterruptedException ignored)
    		{
    			// Intentionally empty
    		}
    		finally
    		{
    			incrementTimesExecutedCount();
    		}
    	}
    }
}