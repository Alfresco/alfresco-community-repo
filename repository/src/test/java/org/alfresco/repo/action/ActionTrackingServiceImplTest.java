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
package org.alfresco.repo.action;

import static org.alfresco.repo.action.ActionServiceImplTest.assertBefore;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionServiceImplTest.CancellableSleepAction;
import org.alfresco.repo.action.ActionServiceImplTest.SleepActionExecuter;
import org.alfresco.repo.action.AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceTransientException;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ConfigurableApplicationContext;

import junit.framework.TestCase;

/**
 * Action tracking service tests. These mostly need
 *  careful control over the transactions they use.
 * 
 * @author Nick Burch
 */
@Category(OwnJVMTestsCategory.class)
public class ActionTrackingServiceImplTest extends TestCase
{
    private static ConfigurableApplicationContext ctx = 
       (ConfigurableApplicationContext)ApplicationContextHelper.getApplicationContext();
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    private NodeRef nodeRef;
    @SuppressWarnings("unused")
    private NodeRef folder;
    private NodeService nodeService;
    private ActionService actionService;
    private ScriptService scriptService;
    private TransactionService transactionService;
    private RuntimeActionService runtimeActionService;
    private ActionTrackingService actionTrackingService;
    private SimpleCache<String, ExecutionDetails> executingActionsCache;
    
    private AsyncOccurs asyncOccurs;
    
    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception
    {
        // Detect any dangling transactions as there is a lot of direct UserTransaction manipulation
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
           throw new IllegalStateException(
                   "There should not be any transactions when starting test: " +
                   AlfrescoTransactionSupport.getTransactionId() + " started at " +
                   new Date(AlfrescoTransactionSupport.getTransactionStartTime()));
        }
    
        // Grab our beans
        this.nodeService = (NodeService)ctx.getBean("nodeService");
        this.scriptService = (ScriptService)ctx.getBean("scriptService");
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.runtimeActionService = (RuntimeActionService)ctx.getBean("actionService");
        this.actionTrackingService = (ActionTrackingService)ctx.getBean("actionTrackingService");
        this.transactionService = (TransactionService)ctx.getBean("transactionService");
        this.executingActionsCache = (SimpleCache<String, ExecutionDetails>)ctx.getBean("executingActionsCache");

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
        
        // Cache should start empty each time
        executingActionsCache.clear();
        
        // Reset the execution instance IDs, so we
        //  can predict what they'll be
        ((ActionTrackingServiceImpl)actionTrackingService).resetNextExecutionId();
        
        // Register the test executor, if needed
        SleepActionExecuter.registerIfNeeded(ctx);
        
        // We want to know when async actions occur
        asyncOccurs = new AsyncOccurs();
        ((PolicyComponent)ctx.getBean("policyComponent")).bindClassBehaviour(
              AsynchronousActionExecutionQueuePolicies.OnAsyncActionExecute.QNAME,
              ActionModel.TYPE_ACTION,
              new JavaBehaviour(asyncOccurs, "onAsyncActionExecute", NotificationFrequency.EVERY_EVENT)
        );
    }

    /** Creating cache keys */
    public void testCreateCacheKeys() throws Exception
    {
       ActionImpl action = (ActionImpl)createWorkingSleepAction("1234");
       assertEquals("sleep-action", action.getActionDefinitionName());
       assertEquals("1234", action.getId());
       assertEquals(-1, action.getExecutionInstance());
       
       // Give it a predictable execution instance
       action.setExecutionInstance(1);
       
       // From an action
       String key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals("sleep-action=1234=1", key);
       
       // From an ExecutionSummary
       ExecutionSummary s = new ExecutionSummary("sleep-action", "1234", 1);
       key = ActionTrackingServiceImpl.generateCacheKey(s);
       assertEquals("sleep-action=1234=1", key);
    }
    
    /** Creating ExecutionDetails and ExecutionSummary */
    public void testExecutionDetailsSummary() throws Exception
    {
       // Create an action with a known execution instance
       Action action = createWorkingSleepAction("1234");
       ((ActionImpl)action).setExecutionInstance(1);
       
       // Create the ExecutionSummary from an action
       String key = ActionTrackingServiceImpl.generateCacheKey(action);
       
       ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
       assertEquals("sleep-action", s.getActionType());
       assertEquals("1234", s.getActionId());
       assertEquals(1, s.getExecutionInstance());
       
       // Create the ExecutionSummery from a key
       s = ActionTrackingServiceImpl.buildExecutionSummary(key);
       assertEquals("sleep-action", s.getActionType());
       assertEquals("1234", s.getActionId());
       assertEquals(1, s.getExecutionInstance());
       
       // Now create ExecutionDetails
       ExecutionDetails d = ActionTrackingServiceImpl.buildExecutionDetails(action);
       assertNotNull(d.getExecutionSummary());
       assertEquals("sleep-action", d.getActionType());
       assertEquals("1234", d.getActionId());
       assertEquals(1, d.getExecutionInstance());
       assertEquals(null, d.getPersistedActionRef());
       assertEquals(null, d.getStartedAt());
       
       // Check the machine details
       // Should be "IP : Name"
       InetAddress localhost = InetAddress.getLocalHost();
       String machineName = localhost.getHostAddress() + " : " + 
                localhost.getHostName();
       assertEquals(machineName, d.getRunningOn());
    }
    
    /** Running an action gives it an execution ID */
    public void testExecutionInstanceAssignment() throws Exception
    {
       ActionImpl action = (ActionImpl)createWorkingSleepAction("1234");
       assertEquals(-1, action.getExecutionInstance());
       
       // Have it run, will get the ID of 1
       actionTrackingService.recordActionExecuting(action);
       assertEquals(1, action.getExecutionInstance());
       
       // And again, gets 2
       actionTrackingService.recordActionExecuting(action);
       assertEquals(2, action.getExecutionInstance());
       
       // And again, gets 3
       actionTrackingService.recordActionExecuting(action);
       assertEquals(3, action.getExecutionInstance());
    }
    
    /** 
     * The correct things happen with the cache
     *  when you mark things as working / failed / etc 
     */
    public void testInOutCache() throws Exception
    {
       Action action = createWorkingSleepAction("1234");
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       String key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(null, executingActionsCache.get(key));
       
       
       // Can complete or fail, won't be there
       actionTrackingService.recordActionComplete(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       actionTrackingService.recordActionFailure(action, new Exception("Testing"));
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       assertEquals("Testing", action.getExecutionFailureMessage());
       assertEquals(null, executingActionsCache.get(key));
       
       
       // Pending will add it, but with no start date
       actionTrackingService.recordActionPending(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());
       assertNotNull(null, executingActionsCache.get(key));
       
       ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
       ExecutionDetails d = actionTrackingService.getExecutionDetails(s);
       assertNotNull(d.getExecutionSummary());
       assertEquals("sleep-action", d.getActionType());
       assertEquals("1234", d.getActionId());
       assertEquals(1, d.getExecutionInstance());
       assertEquals(null, d.getPersistedActionRef());
       assertNull(null, d.getStartedAt());
       
       
       // Run it, will be updated in the cache
       actionTrackingService.recordActionExecuting(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Running, action.getExecutionStatus());
       assertNotNull(null, executingActionsCache.get(key));
       
       s = ActionTrackingServiceImpl.buildExecutionSummary(action);
       d = actionTrackingService.getExecutionDetails(s);
       assertNotNull(d.getExecutionSummary());
       assertEquals("sleep-action", d.getActionType());
       assertEquals("1234", d.getActionId());
       assertEquals(1, d.getExecutionInstance());
       assertEquals(null, d.getPersistedActionRef());
       assertNotNull(null, d.getStartedAt());
       
       
       // Completion removes it
       actionTrackingService.recordActionComplete(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       // Failure removes it
       actionTrackingService.recordActionExecuting(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertNotNull(null, executingActionsCache.get(key));
       
       actionTrackingService.recordActionFailure(action, new Exception("Testing"));
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       assertEquals("Testing", action.getExecutionFailureMessage());
       assertEquals(null, executingActionsCache.get(key));
       
       
       // If run from new, i.e. not via pending, goes into the cache
       ((ActionImpl)action).setExecutionStatus(ActionStatus.New);
       ((ActionImpl)action).setExecutionStartDate(null);
       ((ActionImpl)action).setExecutionEndDate(null);
       ((ActionImpl)action).setExecutionFailureMessage(null);
       ((ActionImpl)action).setExecutionInstance(-1);
       
       actionTrackingService.recordActionExecuting(action);
       assertEquals(ActionStatus.Running, action.getExecutionStatus());
       assertTrue( ((ActionImpl)action).getExecutionInstance() != -1 );
       
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertNotNull(null, executingActionsCache.get(key));
       
       actionTrackingService.recordActionComplete(action);
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
    }
    
    /** Working actions go into the cache, then out */
    public void testWorkingActions() throws Exception 
    {
       final SleepActionExecuter sleepActionExec = 
          (SleepActionExecuter)ctx.getBean(SleepActionExecuter.NAME);
       sleepActionExec.resetTimesExecuted();
       sleepActionExec.setSleepMs(10000);

       // Have it run asynchronously
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();
       Action action = createWorkingSleepAction("54321");
       assertNull(action.getExecutionStartDate());
       assertNull(action.getExecutionEndDate());
       assertNull(action.getExecutionFailureMessage());
       assertEquals(ActionStatus.New, action.getExecutionStatus());
       
       String key = ActionTrackingServiceImpl.generateCacheKey(action);
       assertEquals(null, executingActionsCache.get(key));
       
       this.actionService.executeAction(action, this.nodeRef, false, true);
       
       
       // End the transaction. Should allow the async action
       //  to start up, and begin sleeping
       txn.commit();
       Thread.sleep(150);
       
       // The action should now be running 
       // It will have got an execution instance id, so a new key
       key = ActionTrackingServiceImpl.generateCacheKey(action);
       
       
       // Check it's in the cache
       System.out.println("Checking the cache for " + key);
       assertNotNull(executingActionsCache.get(key));
       
       ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
       ExecutionDetails d = actionTrackingService.getExecutionDetails(s);
       assertNotNull(d.getExecutionSummary());
       assertEquals("sleep-action", d.getActionType());
       assertEquals("54321", d.getActionId());
       assertEquals(1, d.getExecutionInstance());
       assertEquals(null, d.getPersistedActionRef());
       assertNotNull(null, d.getStartedAt());

       
       // Tell it to stop sleeping
       // Then wait for it to finish
       asyncOccurs.awaitExecution(null, sleepActionExec.getExecutingThread(), action.getActionDefinitionName()); 
       
       
       // Ensure it went away again
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       d = actionTrackingService.getExecutionDetails(s);
       assertEquals(null, d);
    }
    
    /** Failing actions go into the cache, then out */
    public void testFatallyFailingActions() throws Exception
    {
       Action failedAction = performFailingActionImpl(true, "54321");
       
       assertEquals(ActionStatus.Failed, failedAction.getExecutionStatus());
       assertEquals("Bang!", failedAction.getExecutionFailureMessage());
    }
    
    /** Failing actions go into the cache, then out */
    public void testTransientlyFailingActions() throws Exception
    {
       Action failedAction = performFailingActionImpl(false, "654321");
       
       assertEquals(ActionStatus.Declined, failedAction.getExecutionStatus());
       assertTrue(failedAction.getExecutionFailureMessage().endsWith("Pop!"));
    }

    private Action performFailingActionImpl(boolean fatalFailure, String actionId) throws Exception
    {
        final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx.getBean(SleepActionExecuter.NAME);
        sleepActionExec.setSleepMs(10000);

        // Have it run asynchronously
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        Action action = createFailingSleepAction(actionId, fatalFailure);
        assertNull(action.getExecutionStartDate());
        assertNull(action.getExecutionEndDate());
        assertNull(action.getExecutionFailureMessage());
        assertEquals(ActionStatus.New, action.getExecutionStatus());

        String key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(null, executingActionsCache.get(key));

        this.actionService.executeAction(action, this.nodeRef, false, true);

        // End the transaction. Should allow the async action
        // to be started, and move into its sleeping phase
        txn.commit();
        Thread.sleep(150);

        // Will get an execution instance id, so a new key
        key = ActionTrackingServiceImpl.generateCacheKey(action);

        // Check it's in the cache
        System.out.println("Checking the cache for " + key);
        assertNotNull(executingActionsCache.get(key));

        ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
        ExecutionDetails d = actionTrackingService.getExecutionDetails(s);
        assertNotNull(d.getExecutionSummary());
        assertEquals("sleep-action", d.getActionType());
        assertEquals(actionId, d.getActionId());
        assertEquals(1, d.getExecutionInstance());
        assertEquals(null, d.getPersistedActionRef());

        // let's be more resilient and try a number of times with a delay
        long start = System.currentTimeMillis();
        int sleepTime = 1000; // 1s
        for (int i = 0; i < 10; i++)
        {
            if (d.getStartedAt() == null)
            {
                Thread.sleep(sleepTime);
                sleepTime += 100; // increase by 100ms
                continue;
            }
            else
            {
                break;
            }
        }
        long end = System.currentTimeMillis();
        assertNotNull("Started at time is null, the action has not yet started after " + (end - start) + "ms",
                d.getStartedAt());

        // Tell it to stop sleeping
        // Then wait for it to finish and go bang
        // (Need to do it by hand, as it won't fire the complete policy
        // as the action has failed)
        sleepActionExec.getExecutingThread().interrupt();
        Thread.sleep(150);

        // Ensure it went away again
        assertEquals(null, executingActionsCache.get(key));

        d = actionTrackingService.getExecutionDetails(s);
        assertEquals(null, d);

        return action;
    }
    
    /** Ensure that pending actions behave properly */
    public void testPendingActions() throws Exception
    {
        // New ones won't be in the cache
        Action action = createWorkingSleepAction("1234");
        assertEquals(ActionStatus.New, action.getExecutionStatus());
       
        String key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(null, executingActionsCache.get(key));
        
       
        // Ask for it to be pending, will go in
        actionTrackingService.recordActionPending(action);
        key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(ActionStatus.Pending, action.getExecutionStatus());
        assertNotNull(null, executingActionsCache.get(key));
        
        ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
        ExecutionDetails d = actionTrackingService.getExecutionDetails(s);
        assertNotNull(d.getExecutionSummary());
        assertEquals("sleep-action", d.getActionType());
        assertEquals("1234", d.getActionId());
        assertEquals(1, d.getExecutionInstance());
        assertEquals(null, d.getPersistedActionRef());
        assertNull(null, d.getStartedAt());

        
        // Run it, will stay
        actionTrackingService.recordActionExecuting(action);
        key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(ActionStatus.Running, action.getExecutionStatus());
        assertNotNull(null, executingActionsCache.get(key));
        
        s = ActionTrackingServiceImpl.buildExecutionSummary(action);
        d = actionTrackingService.getExecutionDetails(s);
        assertNotNull(d.getExecutionSummary());
        assertEquals("sleep-action", d.getActionType());
        assertEquals("1234", d.getActionId());
        assertEquals(1, d.getExecutionInstance());
        assertEquals(null, d.getPersistedActionRef());
        assertNotNull(d.getStartedAt());
       
        
        // Finish, goes
        actionTrackingService.recordActionComplete(action);
        key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(ActionStatus.Completed, action.getExecutionStatus());
        assertEquals(null, executingActionsCache.get(key));
       
       
        // Put another pending one in
        action = createWorkingSleepAction("1234");
        actionTrackingService.recordActionPending(action);
        key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(ActionStatus.Pending, action.getExecutionStatus());
        assertNotNull(null, executingActionsCache.get(key));
        
       
        // Remove it by hand
        executingActionsCache.remove(key);
        assertNull(null, executingActionsCache.get(key));
        int instanceId = ((ActionImpl)action).getExecutionInstance();
        
       
        // Run it, will go back in again, ID unchanged
        actionTrackingService.recordActionExecuting(action);
        assertEquals(key, ActionTrackingServiceImpl.generateCacheKey(action));
        assertEquals(instanceId, ((ActionImpl)action).getExecutionInstance());
        
        assertEquals(ActionStatus.Running, action.getExecutionStatus());
        assertNotNull(null, executingActionsCache.get(key));
       
       
        // Finish, will go again
        actionTrackingService.recordActionComplete(action);
        key = ActionTrackingServiceImpl.generateCacheKey(action);
        assertEquals(ActionStatus.Completed, action.getExecutionStatus());
        assertEquals(null, executingActionsCache.get(key));
    }

    /** Ensure that the listing functions work */
    public void testListings() throws Exception
    {
        // All listings start blank
        assertEquals(0, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(0, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(0, actionTrackingService.getExecutingActions(createWorkingSleepAction(null)).size());

        // Create some actions
        Action sleepAction1 = createWorkingSleepAction("12345");
        Action sleepAction2 = createWorkingSleepAction("54321");
        Action moveAction = createFailingMoveAction();

        // Start putting them in
        actionTrackingService.recordActionExecuting(sleepAction1);
        assertEquals(1, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(1, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(1, actionTrackingService.getExecutingActions(sleepAction1).size());
        assertEquals(0, actionTrackingService.getExecutingActions(sleepAction2).size());
        assertEquals(0, actionTrackingService.getExecutingActions(moveAction).size());

        actionTrackingService.recordActionExecuting(moveAction);
        assertEquals(2, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(1, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(1, actionTrackingService.getExecutingActions(sleepAction1).size());
        assertEquals(0, actionTrackingService.getExecutingActions(sleepAction2).size());
        assertEquals(1, actionTrackingService.getExecutingActions(moveAction).size());

        actionTrackingService.recordActionExecuting(sleepAction2);
        assertEquals(3, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(2, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(1, actionTrackingService.getExecutingActions(sleepAction1).size());
        assertEquals(1, actionTrackingService.getExecutingActions(sleepAction2).size());
        assertEquals(1, actionTrackingService.getExecutingActions(moveAction).size());

        // Now have some finish, should leave the cache
        actionTrackingService.recordActionComplete(sleepAction2);
        assertEquals(2, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(1, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(1, actionTrackingService.getExecutingActions(sleepAction1).size());
        assertEquals(0, actionTrackingService.getExecutingActions(sleepAction2).size());
        assertEquals(1, actionTrackingService.getExecutingActions(moveAction).size());

        actionTrackingService.recordActionComplete(sleepAction1);
        assertEquals(1, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(0, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(0, actionTrackingService.getExecutingActions(sleepAction1).size());
        assertEquals(0, actionTrackingService.getExecutingActions(sleepAction2).size());
        assertEquals(1, actionTrackingService.getExecutingActions(moveAction).size());

        // Check for multiple instances of the same action
        runtimeActionService.saveActionImpl(nodeRef, sleepAction1);
        ((ActionTrackingServiceImpl) actionTrackingService).resetNextExecutionId();

        ActionImpl sa11 = (ActionImpl) runtimeActionService.createAction(nodeRef);
        ActionImpl sa12 = (ActionImpl) runtimeActionService.createAction(nodeRef);
        ActionImpl sa13 = (ActionImpl) runtimeActionService.createAction(nodeRef);
        sa11 = new ActionImpl(sa11, SleepActionExecuter.NAME);
        sa12 = new ActionImpl(sa12, SleepActionExecuter.NAME);
        sa13 = new ActionImpl(sa13, SleepActionExecuter.NAME);

        actionTrackingService.recordActionExecuting(sa11);
        actionTrackingService.recordActionExecuting(sa12);
        actionTrackingService.recordActionExecuting(sa13);
        assertEquals(1, sa11.getExecutionInstance());
        assertEquals(2, sa12.getExecutionInstance());
        assertEquals(3, sa13.getExecutionInstance());

        assertEquals(4, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(3, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(1, actionTrackingService.getExecutingActions(moveAction).size());
        assertEquals(3, actionTrackingService.getExecutingActions(sa11).size());
        assertEquals(3, actionTrackingService.getExecutingActions(sa12).size());
        assertEquals(3, actionTrackingService.getExecutingActions(sa13).size());

        // Let the update change the stored node
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        actionTrackingService.recordActionComplete(sa13);
        actionTrackingService.recordActionComplete(moveAction);

        txn.commit();
        Thread.sleep(50);

        // Check
        assertEquals(2, actionTrackingService.getAllExecutingActions().size());
        assertEquals(0, actionTrackingService.getExecutingActions("test").size());
        assertEquals(2, actionTrackingService.getExecutingActions(SleepActionExecuter.NAME).size());
        assertEquals(2, actionTrackingService.getExecutingActions(sa11).size());
        assertEquals(2, actionTrackingService.getExecutingActions(sa12).size());
        assertEquals(2, actionTrackingService.getExecutingActions(sa13).size() // Others still going
        );
    }

    /** Cancel related */
    public void testCancellation() throws Exception
    {
        // Ensure we get the right answers checking
        CancellableSleepAction sleepAction1 = (CancellableSleepAction) createWorkingSleepAction(null);
        CancellableSleepAction sleepAction2 = (CancellableSleepAction) createWorkingSleepAction(null);
        actionTrackingService.recordActionExecuting(sleepAction1);
        actionTrackingService.recordActionExecuting(sleepAction2);
        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction1));
        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction2));

        // Cancel with the action
        actionTrackingService.requestActionCancellation(sleepAction1);
        assertEquals(true, actionTrackingService.isCancellationRequested(sleepAction1));
        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction2));

        // Cancel with the summary
        ExecutionSummary s2 = ActionTrackingServiceImpl.buildExecutionSummary(sleepAction2);
        actionTrackingService.requestActionCancellation(s2);
        assertEquals(true, actionTrackingService.isCancellationRequested(sleepAction1));
        assertEquals(true, actionTrackingService.isCancellationRequested(sleepAction2));

        // If the action had gone missing from the cache,
        // then a check will put it back
        CancellableSleepAction sleepAction3 = (CancellableSleepAction) createWorkingSleepAction(null);
        String key3 = ActionTrackingServiceImpl.generateCacheKey(sleepAction3);

        assertNull(executingActionsCache.get(key3));
        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction3));
        assertNotNull(executingActionsCache.get(key3));

        executingActionsCache.remove(key3);
        assertNull(executingActionsCache.get(key3));
        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction3));
        assertNotNull(executingActionsCache.get(key3));

        actionTrackingService.requestActionCancellation(sleepAction3);
        assertEquals(true, actionTrackingService.isCancellationRequested(sleepAction3));
        assertNotNull(executingActionsCache.get(key3));

        // Now have one execute and cancel it, ensure it does
        final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx.getBean(SleepActionExecuter.NAME);
        sleepActionExec.setSleepMs(10000);

        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        executingActionsCache.remove(key3);
        this.actionService.executeAction(sleepAction3, this.nodeRef, false, true);

        // End the transaction. Should allow the async action
        // to be started
        txn.commit();
        Thread.sleep(150);

        // Get the updated key, and check
        key3 = ActionTrackingServiceImpl.generateCacheKey(sleepAction3);
        ExecutionSummary s3 = ActionTrackingServiceImpl.buildExecutionSummary(key3);

        assertEquals(false, actionTrackingService.isCancellationRequested(sleepAction3));
        assertEquals(false, actionTrackingService.getExecutionDetails(s3).isCancelRequested());
        assertNotNull(executingActionsCache.get(key3));

        actionTrackingService.requestActionCancellation(sleepAction3);

        assertEquals(true, actionTrackingService.isCancellationRequested(sleepAction3));
        assertEquals(true, actionTrackingService.getExecutionDetails(s3).isCancelRequested());
        assertNotNull(executingActionsCache.get(key3));

        // Have it finish sleeping, will have been cancelled
        // (Can't use the policy, as cancel is counted as a failure)
        sleepActionExec.getExecutingThread().interrupt();
        Thread.sleep(150);

        // Ensure the proper cancelled tracking
        assertEquals(ActionStatus.Cancelled, sleepAction3.getExecutionStatus());
        assertEquals(null, sleepAction3.getExecutionFailureMessage());
    }

    // =================================================================== //

    /**
     * Tests that when we run an action, either synchronously or asynchronously, with it working or failing, that the
     * action execution service correctly sets the flags
     */
    public void xtestExecutionTrackingOnExecution() throws Exception
    {
        // FIXME: This test fails intermittently for no apparent reason.
        // Removed until a reason/resolution can be found
        final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx.getBean(SleepActionExecuter.NAME);
        sleepActionExec.setSleepMs(10);
        Action action;
        NodeRef actionNode;

        // We need real transactions
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();

        // ===========================================================
        // Execute a transient Action that works, synchronously
        // ===========================================================
        action = createWorkingSleepAction(null);
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
        // Execute a transient Action that fails, synchronously
        // ===========================================================
        action = createFailingMoveAction();
        assertNull(action.getExecutionStartDate());
        assertNull(action.getExecutionEndDate());
        assertNull(action.getExecutionFailureMessage());
        assertEquals(ActionStatus.New, action.getExecutionStatus());

        try
        {
            this.actionService.executeAction(action, this.nodeRef);
            fail("Action should have failed, and the error been thrown");
        }
        catch (Exception e)
        {
        }

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
        // Execute a stored Action that works, synchronously
        // ===========================================================
        action = createWorkingSleepAction(null);
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

        // Let the update change the stored node
        // (Can't use policy as the action has already finished!)
        txn.commit();
        Thread.sleep(150);
        txn = transactionService.getUserTransaction();
        txn.begin();

        // Now re-load and check the stored one
        action = runtimeActionService.createAction(actionNode);
        assertNotNull(action.getExecutionStartDate());
        assertNotNull(action.getExecutionEndDate());
        assertBefore(action.getExecutionStartDate(), action.getExecutionEndDate());
        assertBefore(action.getExecutionEndDate(), new Date());
        assertNull(action.getExecutionFailureMessage());
        assertEquals(ActionStatus.Completed, action.getExecutionStatus());

        // ===========================================================
        // Execute a stored Action that fails, synchronously
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
        try
        {
            this.actionService.executeAction(action, this.nodeRef);
            fail("Action should have failed, and the error been thrown");
        }
        catch (Exception e)
        {
        }

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
        // Execute a transient Action that works, asynchronously
        // ===========================================================
        action = createWorkingSleepAction(null);
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
        // to be executed
        asyncOccurs.awaitExecution(txn, null, action.getActionDefinitionName());

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
        // Execute a transient Action that fails, asynchronously
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

        // End the transaction, and await the failure
        // (Can't use the policy as fails not suceeds)
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
        // Execute a stored Action that works, asynchronously
        // ===========================================================
        action = createWorkingSleepAction(null);
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
        // to be executed
        asyncOccurs.awaitExecution(txn, null, action.getActionDefinitionName());
        Thread.sleep(250); // Need to allow the post-commit update to the stored node
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
        // Execute a stored Action that fails, asynchronously
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

        // End the transaction, and await the failure
        // (Can't use the policy as fails not suceeds)
        txn.commit();
        // Now also wait for the on-rollback to kick in and update
        // the persisted copy of the action node too
        Thread.sleep(400);
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

        txn.commit();
    }

    public void testJavascriptAPI() throws Exception
    {
        // We need a background action to sleep for long enough for
        // it still to be running when the JS fires off
        final SleepActionExecuter sleepActionExec = (SleepActionExecuter) ctx.getBean(SleepActionExecuter.NAME);
        sleepActionExec.setSleepMs(2000);

        ActionImpl sleepAction;
        ActionImpl action;

        // Create three test actions:
        ((ActionTrackingServiceImpl) actionTrackingService).resetNextExecutionId();

        // Sleep one that will still be running
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        sleepAction = (ActionImpl) createWorkingSleepAction(null);
        sleepAction.setNodeRef(nodeRef); // This isn't true!
        this.actionService.executeAction(sleepAction, null, false, true);
        txn.commit();

        // Move one that will appear to be "running"
        action = (ActionImpl) createFailingMoveAction();
        actionTrackingService.recordActionExecuting(action);

        // Finally one that has "failed"
        // (Shouldn't show up in any lists)
        txn = transactionService.getUserTransaction();
        txn.begin();
        action = (ActionImpl) createWorkingSleepAction(null);
        action.setExecutionStartDate(new Date(1234));
        action.setExecutionEndDate(new Date(54321));
        action.setExecutionStatus(ActionStatus.Failed);
        this.actionService.saveAction(this.nodeRef, action);
        txn.commit();

        // Call the test
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("NodeRef", nodeRef.toString());
        model.put("SleepAction", sleepAction);

        ScriptLocation location = new ClasspathScriptLocation(
                "org/alfresco/repo/action/script/test_actionTrackingService.js");
        this.scriptService.executeScript(location, model);
    }

    // =================================================================== //

    public class AsyncOccurs implements OnAsyncActionExecute
    {
        private Object waitForExecutionLock = new Object();
        private String wantedType = null;
        private static final long waitTime = 3500;

        @Override
        public void onAsyncActionExecute(Action action, NodeRef actionedUponNodeRef)
        {
            if (wantedType == null || action.getActionDefinitionName().equals(wantedType))
            {
                synchronized (waitForExecutionLock)
                {
                    waitForExecutionLock.notify();
                }
            }
            else
            {
                System.out.println("Ignoring unexpected async action:" + action);
            }
        }

        public void awaitExecution(UserTransaction tx, Thread toWake, String type) throws Exception
        {
            this.wantedType = type;
            synchronized (waitForExecutionLock)
            {
                // Have things begin working
                if (tx != null)
                {
                    tx.commit();
                }
                if (toWake != null)
                {
                    toWake.interrupt();
                }

                // Now wait for them to finish
                try
                {
                    long now = System.currentTimeMillis();
                    waitForExecutionLock.wait(waitTime);

                    if (System.currentTimeMillis() - now >= waitTime)
                    {
                        System.err.println("Warning - trigger wasn't received");
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    private Action createFailingMoveAction()
    {
        Action failingAction = this.actionService.createAction(MoveActionExecuter.NAME);
        failingAction.setTrackStatus(Boolean.TRUE);

        // Create a bad node ref
        NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
        failingAction.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);

        return failingAction;
    }

    /**
     * @param isFatal
     *            <tt>true</tt> means the sleep action will fail with a RuntimeException, <tt>false</tt> means it will
     *            fail with a {@link ActionServiceTransientException}.
     */
    private Action createFailingSleepAction(String id, boolean isFatal) throws Exception
    {
        return ActionServiceImplTest.createFailingSleepAction(id, isFatal, actionService);
    }

    private Action createWorkingSleepAction(String id) throws Exception
    {
        return ActionServiceImplTest.createWorkingSleepAction(id, actionService);
    }
}