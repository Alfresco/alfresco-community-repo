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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionServiceImplTransactionalTest.SleepActionExecuter;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.search.impl.parsers.CMISParser.nullPredicate_return;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ExecutionDetails;
import org.alfresco.service.cmr.action.ExecutionSummary;
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
 * Action tracking service tests. These mostly need
 *  careful control over the transactions they use.
 * 
 * @author Nick Burch
 */
public class ActionTrackingServiceImplTest extends TestCase
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
    private ActionTrackingService actionTrackingService;
    private RetryingTransactionHelper transactionHelper;
    private EhCacheAdapter<String, ExecutionDetails> executingActionsCache;
    
    @Override
    protected void setUp() throws Exception {
        this.transactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        this.nodeService = (NodeService)ctx.getBean("nodeService");
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.runtimeActionService = (RuntimeActionService)ctx.getBean("actionService");
        this.actionTrackingService = (ActionTrackingService)ctx.getBean("actionTrackingService");
        this.transactionService = (TransactionService)ctx.getBean("transactionService");
        this.executingActionsCache = (EhCacheAdapter<String, ExecutionDetails>)ctx.getBean("executingActionsSharedCache");

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

    /** Creating cache keys */
    public void testCreateCacheKeys() throws Exception
    {
       Action action = createWorkingSleepAction("1234");
       assertEquals("sleep-action", action.getActionDefinitionName());
       assertEquals("1234", action.getId());
       // assertNull(action.getExecutionInstance()); // TODO
       
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
       // Create the ExecutionSummary from an action
       Action action = createWorkingSleepAction("1234");
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
    }
    
    // Running an action gives it an execution ID
    // TODO
    
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
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       actionTrackingService.recordActionFailure(action, new Exception("Testing"));
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       assertEquals("Testing", action.getExecutionFailureMessage());
       assertEquals(null, executingActionsCache.get(key));
       
       
       // Pending won't add it in either
       actionTrackingService.recordActionPending(action);
       assertEquals(ActionStatus.Pending, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       
       // Run it, will go in
       actionTrackingService.recordActionExecuting(action);
       assertEquals(ActionStatus.Running, action.getExecutionStatus());
       assertNotNull(null, executingActionsCache.get(key));
       
       ExecutionSummary s = ActionTrackingServiceImpl.buildExecutionSummary(action);
       ExecutionDetails d = actionTrackingService.getExecutionDetails(s);
       assertNotNull(d.getExecutionSummary());
       assertEquals("sleep-action", d.getActionType());
       assertEquals("1234", d.getActionId());
       assertEquals(1, d.getExecutionInstance());
       assertEquals(null, d.getPersistedActionRef());
       assertNotNull(null, d.getStartedAt());
       
       
       // Completion removes it
       actionTrackingService.recordActionComplete(action);
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       // Failure removes it
       actionTrackingService.recordActionExecuting(action);
       assertNotNull(null, executingActionsCache.get(key));
       
       actionTrackingService.recordActionFailure(action, new Exception("Testing"));
       assertEquals(ActionStatus.Failed, action.getExecutionStatus());
       assertEquals("Testing", action.getExecutionFailureMessage());
       assertEquals(null, executingActionsCache.get(key));
    }
    
    /** Working actions go into the cache, then out */
    public void testWorkingActions() throws Exception 
    {
       final SleepActionExecuter sleepActionExec = 
          (SleepActionExecuter)ctx.getBean(SleepActionExecuter.NAME);
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
       //  to be started
       txn.commit();
       Thread.sleep(150);
       
       
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
       sleepActionExec.executingThread.interrupt();
       Thread.sleep(100);
       
       
       // Ensure it went away again
       assertEquals(ActionStatus.Completed, action.getExecutionStatus());
       assertEquals(null, executingActionsCache.get(key));
       
       d = actionTrackingService.getExecutionDetails(s);
       assertEquals(null, d);
    }
    
    /** Failing actions go into the cache, then out */
    public void testFailingActions() throws Exception
    {
       
    }
    
    // Ensure that the listing functions work
    // TODO
    
    
    // =================================================================== //
    
    
    private Action createFailingMoveAction() {
       Action failingAction = this.actionService.createAction(MoveActionExecuter.NAME);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
       failingAction.setParameterValue(MoveActionExecuter.PARAM_ASSOC_QNAME, ContentModel.ASSOC_CHILDREN);
       // Create a bad node ref
       NodeRef badNodeRef = new NodeRef(this.storeRef, "123123");
       failingAction.setParameterValue(MoveActionExecuter.PARAM_DESTINATION_FOLDER, badNodeRef);
       
       return failingAction;
    }
    private Action createWorkingSleepAction(String id) throws Exception {
       Action workingAction = actionService.createAction(SleepActionExecuter.NAME);
       Field idF = ParameterizedItemImpl.class.getDeclaredField("id");
       idF.setAccessible(true);
       idF.set(workingAction, id);
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
    	private Thread executingThread;
    	
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
    	   executingThread = Thread.currentThread();
    	   //System.err.println("Sleeping for " + sleepMs + " for " + action);
    	   
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