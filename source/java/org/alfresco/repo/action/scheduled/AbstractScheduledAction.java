/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action.scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Abstract action support.
 * 
 * Each action applies to a set of nodes.
 * 
 * These actions may be executed in one overall transaction or one individual transaction. If actions are in individual transactions an error may halt subsequent execution or
 * processing can try and invoke the action for each node.
 * 
 * @author Andy Hind
 */
public abstract class AbstractScheduledAction implements ScheduledActionDefinition
{
    /**
     * Logging
     */
    private static Log s_logger = LogFactory.getLog(AbstractScheduledAction.class);

    /**
     * Enum to define the transaction mode.
     * 
     * @author Andy Hind
     */
    public enum TransactionMode
    {
        ISOLATED_TRANSACTIONS, UNTIL_FIRST_FAILURE, ONE_TRANSACTION;

        public static TransactionMode getTransactionMode(String transactionModeString)
        {
            TransactionMode transactionMode;
            if (transactionModeString.equalsIgnoreCase("ISOLATED_TRANSACTIONS"))
            {
                transactionMode = TransactionMode.ISOLATED_TRANSACTIONS;
            }
            else if (transactionModeString.equalsIgnoreCase("UNTIL_FIRST_FAILURE"))
            {
                transactionMode = TransactionMode.UNTIL_FIRST_FAILURE;
            }
            else if (transactionModeString.equalsIgnoreCase("ONE_TRANSACTION"))
            {
                transactionMode = TransactionMode.ONE_TRANSACTION;
            }
            else
            {
                // The default ....
                transactionMode = TransactionMode.ISOLATED_TRANSACTIONS;
            }
            return transactionMode;
        }
    }

    /**
     * Enum to define if compensating actions are run.
     * 
     * @author Andy Hind
     */
    public enum CompensatingActionMode
    {
        RUN_COMPENSATING_ACTIONS_ON_FAILURE, IGNORE;

        public static CompensatingActionMode getCompensatingActionMode(String compensatingActionModeString)
        {
            CompensatingActionMode compensatingActionMode;
            if (compensatingActionModeString.equalsIgnoreCase("RUN_COMPENSATING_ACTIONS_ON_FAILURE"))
            {
                compensatingActionMode = CompensatingActionMode.RUN_COMPENSATING_ACTIONS_ON_FAILURE;
            }
            else if (compensatingActionModeString.equalsIgnoreCase("IGNORE"))
            {
                compensatingActionMode = CompensatingActionMode.IGNORE;
            }
            else
            {
                // The default ....
                compensatingActionMode = CompensatingActionMode.IGNORE;
            }
            return compensatingActionMode;
        }
    }

    /*
     * Key used to pass the action in the quartz job definition
     */
    private static final String ACTION_JOB_DATA_MAP_KEY = "Action";

    /*
     * The Action service.
     */
    private ActionService actionService;

    /*
     * The user in whose name the action will run.
     */
    private String runAsUser;

    /*
     * The template definition of the action.
     */
    private TemplateActionDefinition templateActionDefinition;

    /*
     * The transaction mode in which all the nodes found by this sceduled action will be treated.
     */
    private TransactionMode transactionMode = TransactionMode.ISOLATED_TRANSACTIONS;

    /*
     * Control if compensating actions will be used. The default is not to apply compensating actions.
     */
    private CompensatingActionMode compensatingActionMode = CompensatingActionMode.IGNORE;

    /*
     * The transaction service
     */
    private TransactionService transactionService;

    /**
     * Simple constructor
     */
    public AbstractScheduledAction()
    {
        super();
    }

    /**
     * Get the user in whose name to run the action.
     * 
     * @return
     */
    public String getRunAsUser()
    {
        return runAsUser;
    }

    /**
     * Set the user in whose name to run the action.
     * 
     * @param runAsUser
     */
    public void setRunAsUser(String runAsUser)
    {
        this.runAsUser = runAsUser;
    }

    /**
     * Get the template definition.
     */
    public TemplateActionDefinition getTemplateActionDefinition()
    {
        return templateActionDefinition;
    }

    /**
     * Set the action service - IOC.
     * 
     * @param actionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Get the actions service.
     * 
     * @return
     */
    public ActionService getActionService()
    {
        return actionService;
    }

    /**
     * Set the behaviour for compensating actiions.
     * 
     * @param compensatingActionModeString
     */
    public void setCompensatingActionMode(String compensatingActionModeString)
    {
        this.compensatingActionMode = CompensatingActionMode.getCompensatingActionMode(compensatingActionModeString);
    }

    /**
     * Set transactional behaviour.
     * 
     * @param transactionModeString
     */
    public void setTransactionMode(String transactionModeString)
    {
        this.transactionMode = TransactionMode.getTransactionMode(transactionModeString);
    }

    /**
     * Get the transaction service.
     * 
     * @return
     */
    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    /**
     * Set the transactions service - IOC.
     * 
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the template action that is used to generate the real action for each node.
     */
    public void setTemplateActionDefinition(TemplateActionDefinition templateActionDefinition)
    {
        this.templateActionDefinition = templateActionDefinition;
    }

    /**
     * Get the behaviour for compensating actions.
     * 
     * @return
     */
    public CompensatingActionMode getCompensatingActionModeEnum()
    {
        return compensatingActionMode;
    }

    /**
     * Get the transaction mode.
     * 
     * @return
     */
    public TransactionMode getTransactionModeEnum()
    {
        return transactionMode;
    }

    public void register(Scheduler scheduler) throws SchedulerException
    {
        JobDetail jobDetail = getJobDetail();
        Trigger trigger = getTrigger();

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug(("Registering job: " + jobDetail));
            s_logger.debug(("With trigger: " + trigger));
        }
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Get the trigger definition for this job. Used to register with the injected scheduler.
     * 
     * @return
     */
    public abstract Trigger getTrigger();

    /**
     * Get the list of nodes against which this action should run.
     * 
     * @return
     */
    public abstract List<NodeRef> getNodes();

    /**
     * Generate the actual action for the given node from the action template.
     * 
     * @param nodeRef
     * @return
     */
    public abstract Action getAction(NodeRef nodeRef);

    /**
     * Get the job detail.
     * 
     * @return
     */
    private JobDetail getJobDetail()
    {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ACTION_JOB_DATA_MAP_KEY, this);

        JobDetail jobDetail = new JobDetail();
        jobDetail.setName(getJobName());
        jobDetail.setGroup(getJobGroup());
        jobDetail.setJobDataMap(jobDataMap);
        jobDetail.setJobClass(JobDefinition.class);
        return jobDetail;
    }

    /**
     * Job definition to run scheduled action
     * 
     * @author Andy Hind
     */
    public static class JobDefinition implements Job
    {

        public void execute(JobExecutionContext ctx) throws JobExecutionException
        {
            final AbstractScheduledAction abstractScheduledAction = (AbstractScheduledAction) ctx.getJobDetail()
                    .getJobDataMap().get(ACTION_JOB_DATA_MAP_KEY);

            // Run as the required user
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    // Get the list of nodes
                    List<NodeRef> nodes = abstractScheduledAction.getNodes();
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Found " + nodes.size());
                    }

                    // Individual transactions
                    if (abstractScheduledAction.getTransactionModeEnum() == TransactionMode.ONE_TRANSACTION)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Executing in one transaction");
                        }
                        runTransactionalActions(nodes);
                        return null;
                    }
                    // Single global transaction
                    else
                    {

                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("Executing in individual transaction");
                        }
                        for (NodeRef nodeRef : nodes)
                        {

                            try
                            {
                                runTransactionalAction(nodeRef);
                            }
                            catch (Throwable t)
                            {
                                if (abstractScheduledAction.getTransactionModeEnum() == TransactionMode.ISOLATED_TRANSACTIONS)
                                {
                                    s_logger
                                            .error(
                                                    "Error in scheduled action executed in isolated transactions (other actions will continue",
                                                    t);
                                }
                                else
                                {
                                    throwRuntimeException(t);
                                }
                            }
                        }
                        return null;
                    }
                }

                /**
                 * Apply the action to all nodes in one overall transaction
                 * 
                 * @param nodes
                 */
                public void runTransactionalActions(final List<NodeRef> nodes)
                {
                    boolean runCompensatingActions = abstractScheduledAction.getCompensatingActionModeEnum() == CompensatingActionMode.RUN_COMPENSATING_ACTIONS_ON_FAILURE;

                    try
                    {
                        TransactionUtil.executeInUserTransaction(abstractScheduledAction.getTransactionService(),
                                new TransactionUtil.TransactionWork<Object>()
                                {
                                    public Object doWork() throws Exception
                                    {
                                        // Build the full list of compensating actions
                                        // If anything goes wrong we need to do all these instead
                                        List<Pair<Action, NodeRef>> compensatingActions = new ArrayList<Pair<Action, NodeRef>>(
                                                nodes.size());

                                        for (NodeRef nodeRef : nodes)
                                        {
                                            Action action = abstractScheduledAction.getAction(nodeRef);
                                            Action compensatingAction = action.getCompensatingAction();
                                            if (compensatingAction != null)
                                            {
                                                compensatingActions.add(new Pair<Action, NodeRef>(compensatingAction,
                                                        nodeRef));
                                            }
                                        }

                                        // Run all the actions
                                        try
                                        {

                                            for (NodeRef nodeRef : nodes)
                                            {
                                                Action action = abstractScheduledAction.getAction(nodeRef);
                                                abstractScheduledAction.getActionService().executeAction(action,
                                                        nodeRef);

                                            }
                                            return null;
                                        }
                                        catch (Throwable t)
                                        {
                                            // Throw exception to trigger compensating actions
                                            throw new CompensatingActionException("Requires compensating action", t,
                                                    compensatingActions);
                                        }

                                    }
                                });
                    }
                    catch (Throwable t)
                    {
                        // Do compensation if required
                        doCompensation(runCompensatingActions, true, t);
                    }
                }

                /**
                 * Run compensating actions.
                 * 
                 * These are always in their own transaction. We try to run all compensating actions.
                 * 
                 * @param runCompensatingActions
                 * @param rethrow
                 * @param t
                 */
                private void doCompensation(boolean runCompensatingActions, boolean rethrow, Throwable t)
                {
                    // If the error triggers compensation, and they should be processed.
                    if (runCompensatingActions && (t instanceof CompensatingActionException))
                    {
                        CompensatingActionException cae = (CompensatingActionException) t.getCause();
                        for (Pair<Action, NodeRef> pair : cae.getCompensatingActions())
                            if ((pair != null) && (pair.getFirst() != null) && (pair.getSecond() != null))
                            {
                                try

                                {
                                    // try the compensating action in its own tx
                                    runTransactionalCompensatingAction(pair);
                                }
                                catch (Throwable cat)
                                {
                                    s_logger.error("Error executing compensating action", t);
                                }
                            }
                    }

                    if (rethrow)
                    {
                        throwRuntimeException(t);
                    }
                }

                /**
                 * Run a single transaction in its own tx
                 * 
                 * @param nodeRef
                 */
                public void runTransactionalAction(final NodeRef nodeRef)
                {
                    boolean runCompensatingActions = abstractScheduledAction.getCompensatingActionModeEnum() == CompensatingActionMode.RUN_COMPENSATING_ACTIONS_ON_FAILURE;
                    boolean rethrow = abstractScheduledAction.getTransactionModeEnum() != TransactionMode.ISOLATED_TRANSACTIONS;

                    try
                    {
                        TransactionUtil.executeInUserTransaction(abstractScheduledAction.getTransactionService(),
                                new TransactionUtil.TransactionWork<Object>()
                                {
                                    public Object doWork() throws Exception
                                    {
                                        // try action - failure triggers compensation
                                        Action action = abstractScheduledAction.getAction(nodeRef);
                                        Action compensatingAction = action.getCompensatingAction();
                                        try
                                        {
                                            abstractScheduledAction.getActionService().executeAction(action, nodeRef);
                                            return null;
                                        }
                                        catch (Throwable t)
                                        {
                                            if (compensatingAction != null)
                                            {
                                                throw new CompensatingActionException(
                                                        "Requires compensating action",
                                                        t,
                                                        Collections
                                                                .<Pair<Action, NodeRef>> singletonList(new Pair<Action, NodeRef>(
                                                                        action.getCompensatingAction(), nodeRef)));
                                            }
                                            else
                                            {
                                                return throwRuntimeException(t);
                                            }
                                        }
                                    }
                                });
                    }
                    catch (Throwable t)
                    {
                        // Run compensating action if required
                        doCompensation(runCompensatingActions, rethrow, t);
                    }
                }

                /**
                 * Manage running a compensating action and chaining all its compensating actions until done
                 * 
                 * @param pair
                 */
                public void runTransactionalCompensatingAction(final Pair<Action, NodeRef> pair)
                {
                    boolean runCompensatingActions = abstractScheduledAction.getCompensatingActionModeEnum() == CompensatingActionMode.RUN_COMPENSATING_ACTIONS_ON_FAILURE;

                    try
                    {
                        TransactionUtil.executeInUserTransaction(abstractScheduledAction.getTransactionService(),
                                new TransactionUtil.TransactionWork<Object>()
                                {
                                    public Object doWork() throws Exception
                                    {
                                        try
                                        {
                                            abstractScheduledAction.getActionService().executeAction(pair.getFirst(),
                                                    pair.getSecond());
                                            return null;
                                        }
                                        catch (Throwable t)
                                        {
                                            List<Pair<Action, NodeRef>> compensatingActions = new ArrayList<Pair<Action, NodeRef>>(
                                                    1);
                                            if (pair.getFirst().getCompensatingAction() != null)
                                            {
                                                compensatingActions.add(new Pair<Action, NodeRef>(pair.getFirst()
                                                        .getCompensatingAction(), pair.getSecond()));
                                            }
                                            throw new CompensatingActionException("Requires compensating action", t,
                                                    compensatingActions);

                                        }

                                    }
                                });
                    }
                    catch (Throwable t)
                    {
                        // Run compensation
                        doCompensation(runCompensatingActions, true, t);
                    }
                }

            }, abstractScheduledAction.getRunAsUser());
        }
    }

    /**
     * Simple class to hold to related objects
     * 
     * @author Andy Hind
     */
    public static class Pair<FIRST, SECOND>
    {
        FIRST first;

        SECOND second;

        Pair(FIRST first, SECOND second)
        {
            this.first = first;
            this.second = second;
        }

        FIRST getFirst()
        {
            return first;
        }

        SECOND getSecond()
        {
            return second;
        }
    }

    /**
     * Support method to translate exceptions to runtime exceptions.
     * 
     * @param t
     * @return
     */
    private static Object throwRuntimeException(Throwable t)
    {
        if (t instanceof RuntimeException)
        {
            throw (RuntimeException) t;
        }
        else
        {
            throw new RuntimeException("Error during execution of transaction.", t);
        }
    }
}
