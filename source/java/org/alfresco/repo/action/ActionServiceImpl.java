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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluator;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionList;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ActionStatus;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.CompositeActionCondition;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Action service implementation
 * 
 * @author Roy Wetherall
 */
public class ActionServiceImpl implements ActionService, RuntimeActionService, ApplicationContextAware,
            CopyServicePolicies.OnCopyNodePolicy, CopyServicePolicies.OnCopyCompletePolicy
{
    /**
     * Transaction resource name
     */
    private static final String POST_TRANSACTION_PENDING_ACTIONS = "postTransactionPendingActions";

    /**
     * Error message
     */
    private static final String ERR_FAIL = "The action failed to execute due to an error.";

    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(ActionServiceImpl.class);

    /**
     * Thread local containing the current action chain
     */
    ThreadLocal<Set<String>> currentActionChain = new ThreadLocal<Set<String>>();

    /**
     * The application context used to retrieve defined actions
     */
    private ApplicationContext applicationContext;
    private NodeService nodeService;
    private SearchService searchService;
    private DictionaryService dictionaryService;
    private AuthenticationContext authenticationContext;
    private ActionTrackingService actionTrackingService;
    private PolicyComponent policyComponent;

    /**
     * The asynchronous action execution queues map of name, queue
     */
    private Map<String, AsynchronousActionExecutionQueue> asynchronousActionExecutionQueues;

    /**
     * Action transaction listener
     */
    private ActionTransactionListener transactionListener = new ActionTransactionListener(this);

    /**
     * All the condition definitions currently registered
     */
    private Map<String, ActionConditionDefinition> conditionDefinitions = new HashMap<String, ActionConditionDefinition>();

    /**
     * All the action definitions currently registered
     */
    private Map<String, ActionDefinition> actionDefinitions = new HashMap<String, ActionDefinition>();

    /**
     * All the parameter constraints
     */
    private Map<String, ParameterConstraint> parameterConstraints = new HashMap<String, ParameterConstraint>();

    /**
     * Set the application context
     * 
     * @param applicationContext the application context
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Set the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the search service
     * 
     * @param searchService the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set the authentication component
     * 
     * @param authenticationContext the authentication component
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Set the action tracking service
     * 
     * @param actionTrackingService the action tracking service
     */
    public void setActionTrackingService(ActionTrackingService actionTrackingService)
    {
        this.actionTrackingService = actionTrackingService;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param policyComponent used to set up the action-based policy behaviour
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the asynchronous action execution queues
     * 
     * @param asynchronousActionExecutionQueue the asynchronous action execution
     *            queues
     */
    public void setAsynchronousActionExecutionQueues(
                Map<String, AsynchronousActionExecutionQueue> asynchronousActionExecutionQueues)
    {
        this.asynchronousActionExecutionQueues = asynchronousActionExecutionQueues;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);

        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                    ActionModel.TYPE_ACTION_PARAMETER, new JavaBehaviour(this, "getCopyCallback"));
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                    ActionModel.TYPE_ACTION_PARAMETER, new JavaBehaviour(this, "onCopyComplete"));
    }

    /**
     * Gets the saved action folder reference
     * 
     * @param nodeRef the node reference
     * @return the node reference
     */
    private NodeRef getSavedActionFolderRef(NodeRef nodeRef)
    {
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(nodeRef, RegexQNamePattern.MATCH_ALL,
                    ActionModel.ASSOC_ACTION_FOLDER);
        if (assocs.size() != 1)
        {
            throw new ActionServiceException("Unable to retrieve the saved action folder reference.");
        }

        return assocs.get(0).getChildRef();
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinition(java.lang.String)
     */
    public ActionDefinition getActionDefinition(String name)
    {
        // get direct access to action definition (i.e. ignoring public flag of
        // executer)
        ActionDefinition definition = null;
        Object bean = this.applicationContext.getBean(name);
        if (bean != null && bean instanceof ActionExecuter)
        {
            ActionExecuter executer = (ActionExecuter) bean;
            definition = executer.getActionDefinition();
        }
        return definition;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinitions()
     */
    public List<ActionDefinition> getActionDefinitions()
    {
        return new ArrayList<ActionDefinition>(this.actionDefinitions.values());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActionDefinitions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ActionDefinition> getActionDefinitions(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            return getActionDefinitions();
        }
        else
        {
            // TODO for now we will only filter by type, we will introduce
            // filtering by aspect later
            QName nodeType = this.nodeService.getType(nodeRef);
            List<ActionDefinition> result = new ArrayList<ActionDefinition>();
            for (ActionDefinition actionDefinition : getActionDefinitions())
            {
                List<QName> appliciableTypes = actionDefinition.getApplicableTypes();
                if (appliciableTypes != null && appliciableTypes.isEmpty() == false)
                {
                    for (QName applicableType : actionDefinition.getApplicableTypes())
                    {
                        if (this.dictionaryService.isSubClass(nodeType, applicableType))
                        {
                            result.add(actionDefinition);
                            break;
                        }
                    }
                }
                else
                {
                    result.add(actionDefinition);
                }
            }

            return result;
        }
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActionConditionDefinition(java.lang.String)
     */
    public ActionConditionDefinition getActionConditionDefinition(String name)
    {
        return this.conditionDefinitions.get(name);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActionConditionDefinitions()
     */
    public List<ActionConditionDefinition> getActionConditionDefinitions()
    {
        return new ArrayList<ActionConditionDefinition>(this.conditionDefinitions.values());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getParameterConstraint(java.lang.String)
     */
    public ParameterConstraint getParameterConstraint(String name)
    {
        return this.parameterConstraints.get(name);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getParameterConstraints()
     */
    public List<ParameterConstraint> getParameterConstraints()
    {
        return new ArrayList<ParameterConstraint>(this.parameterConstraints.values());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createActionCondition(java.lang.String)
     */
    public ActionCondition createActionCondition(String name)
    {
        if (logger.isDebugEnabled())
            logger.debug("Creating Action Condition - [" + name + "]");

        if (CompositeActionCondition.COMPOSITE_CONDITION.equals(name))
        {
            return new CompositeActionConditionImpl(GUID.generate());
        }

        return new ActionConditionImpl(GUID.generate(), name);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createActionCondition(java.lang.String,
     *      java.util.Map)
     */
    public ActionCondition createActionCondition(String name, Map<String, Serializable> params)
    {
        ActionCondition condition = createActionCondition(name);
        condition.setParameterValues(params);
        return condition;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createAction()
     */
    public Action createAction(String name)
    {
        return new ActionImpl(null, GUID.generate(), name, null);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createAction(java.lang.String,
     *      java.util.Map)
     */
    public Action createAction(String name, Map<String, Serializable> params)
    {
        Action action = createAction(name);
        action.setParameterValues(params);
        return action;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createCompositeAction()
     */
    public CompositeAction createCompositeAction()
    {
        return new CompositeActionImpl(null, GUID.generate());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#createCompositeActionCondition()
     */
    public CompositeActionCondition createCompositeActionCondition()
    {
        return new CompositeActionConditionImpl(GUID.generate());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#evaluateAction(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateAction(Action action, NodeRef actionedUponNodeRef)
    {
        boolean result = true;

        if (action.hasActionConditions() == true)
        {
            List<ActionCondition> actionConditions = action.getActionConditions();
            for (ActionCondition condition : actionConditions)
            {
                boolean tempresult = evaluateActionCondition(condition, actionedUponNodeRef);

                if (logger.isDebugEnabled())
                    logger.debug("\tCondition " + condition.getActionConditionDefinitionName() + " Result - "
                                + tempresult);

                result = result && tempresult;
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("\tAll Condition Evaluation Result - " + result);

        return result;
    }

    /**
     * Evaluates the actions by finding corresponding actionEvaluators in
     * applicationContext (registered through Spring). Composite conditions are
     * evaluated here as well. It is also possible to have composite actions
     * inside composite actions.
     * 
     * @see org.alfresco.service.cmr.action.ActionService#evaluateActionCondition(org.alfresco.service.cmr.action.ActionCondition,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateActionCondition(ActionCondition condition, NodeRef actionedUponNodeRef)
    {
        if (condition instanceof CompositeActionCondition)
        {
            CompositeActionCondition compositeCondition = (CompositeActionCondition) condition;
            if (logger.isDebugEnabled())
            {
                logger.debug("Evaluating Composite Condition - BOOLEAN CONDITION IS "
                            + (compositeCondition.isORCondition() ? "OR" : "AND"));
            }

            if (!compositeCondition.hasActionConditions())
            {
                throw new IllegalStateException("CompositeActionCondition has no subconditions.");
            }

            boolean result;
            if (compositeCondition.isORCondition())
            {
                result = false;
            }
            else
            {
                result = true;
            }

            for (ActionCondition simplecondition : compositeCondition.getActionConditions())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Evaluating composite condition " + simplecondition.getActionConditionDefinitionName());
                }

                if (compositeCondition.isORCondition())
                {
                    result = result || evaluateSimpleCondition(simplecondition, actionedUponNodeRef);

                    // Short circuit for the OR condition
                    if (result)
                        break;
                }
                else
                {
                    result = result && evaluateSimpleCondition(simplecondition, actionedUponNodeRef);
                    // Short circuit for the AND condition
                    if (!result)
                        break;
                }
            }

            if (compositeCondition.getInvertCondition())
            {
                return !result;
            }
            else
            {
                return result;
            }
        }
        else
        {
            return evaluateSimpleCondition(condition, actionedUponNodeRef);
        }
    }

    private boolean evaluateSimpleCondition(ActionCondition condition, NodeRef actionedUponNodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Evaluating simple condition " + condition.getActionConditionDefinitionName());
        }
        // Evaluate the condition
        ActionConditionEvaluator evaluator = (ActionConditionEvaluator) this.applicationContext.getBean(condition
                    .getActionConditionDefinitionName());
        return evaluator.evaluate(condition, actionedUponNodeRef);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions)
    {
        executeAction(action, actionedUponNodeRef, checkConditions, action.getExecuteAsychronously());
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions,
                boolean executeAsychronously)
    {
        Set<String> actionChain = this.currentActionChain.get();

        if (executeAsychronously == false)
        {
            executeActionImpl(action, actionedUponNodeRef, checkConditions, false, actionChain);
        }
        else
        {
            // Add to the post transaction pending action list
            addPostTransactionPendingAction(action, actionedUponNodeRef, checkConditions, actionChain);
        }
    }

    /**
     * called by transaction service.
     */
    public void postCommit()
    {
        for (PendingAction pendingAction : getPostTransactionPendingActions())
        {
            queueAction(pendingAction);
        }
    }

    /**
     * 
     */
    private void queueAction(PendingAction action)
    {
        // Get the right queue
        AsynchronousActionExecutionQueue queue = getQueue(action.action);

        // Queue the action for execution
        queue.executeAction(this, action.getAction(), action.getActionedUponNodeRef(), action.getCheckConditions(),
                    action.getActionChain());
    }

    /**
     * Queue a compensating action for execution against a specific node
     */
    private void queueAction(Action compensatingAction, NodeRef actionedUponNodeRef)
    {
        // Get the right queue
        AsynchronousActionExecutionQueue queue = getQueue(compensatingAction);

        // Queue the action for execution
        queue.executeAction(this, compensatingAction, actionedUponNodeRef, false, null);
    }

    /**
     * Get the queue to use for asynchronous execution of the given action.
     */
    private AsynchronousActionExecutionQueue getQueue(Action action)
    {
        ActionExecuter executer = (ActionExecuter) this.applicationContext.getBean(action.getActionDefinitionName());
        AsynchronousActionExecutionQueue queue = null;

        String queueName = executer.getQueueName();
        if (queueName == null)
        {
            queue = asynchronousActionExecutionQueues.get("");
        }
        else
        {
            queue = asynchronousActionExecutionQueues.get(queueName);
        }
        if (queue == null)
        {
            // can't get queue
            throw new ActionServiceException("Unable to get AsynchronousActionExecutionQueue name: " + queueName);
        }

        return queue;
    }
    
    /**
     * Get whether the action should be tracked by the {@link ActionTrackingService} or not.
     * 
     * @param action        the action, which may or may not have any say about the status tracking
     * @return              <tt>true</tt> if the status must be tracked, otherwise <tt>false</tt>
     */
    private boolean getTrackStatus(Action action)
    {
        Boolean trackStatusManual = action.getTrackStatus();
        if (trackStatusManual != null)
        {
            return trackStatusManual.booleanValue();
        }
        // The flag has not be changed for the specific action, so use the value from the
        // action definition.
        ActionDefinition actionDef = getActionDefinition(action.getActionDefinitionName());
        if (actionDef == null)
        {
            return false;       // default to 'false' if the definition has disappeared
        }
        else
        {
            return actionDef.getTrackStatus();
        }
    }

    @Override
    public void executeActionImpl(Action action, NodeRef actionedUponNodeRef, boolean checkConditions,
                boolean executedAsynchronously, Set<String> actionChain)
    {
        if (logger.isDebugEnabled() == true)
        {
            StringBuilder builder = new StringBuilder("Execute action impl action chain = ");
            if (actionChain == null)
            {
                builder.append("null");
            }
            else
            {
                for (String value : actionChain)
                {
                    builder.append(value).append(" ");
                }
            }
            logger.debug(builder.toString());
            logger.debug("Current action = " + action.getId());
        }
        
        // get the current user early in case the process fails and we are
        // unable to do it later
        String currentUserName = this.authenticationContext.getCurrentUserName();

        if (actionChain == null || actionChain.contains(action.getId()) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Doing executeActionImpl");
            }

            try
            {
                Set<String> origActionChain = null;

                if (actionChain == null)
                {
                    actionChain = new HashSet<String>();
                }
                else
                {
                    origActionChain = new HashSet<String>(actionChain);
                }
                actionChain.add(action.getId());
                this.currentActionChain.set(actionChain);

                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Adding " + action.getId() + " to action chain.");
                }

                try
                {
                    // Check and execute now
                    if (checkConditions == false || evaluateAction(action, actionedUponNodeRef) == true)
                    {
                        if (getTrackStatus(action))
                        {
                            // Mark the action as starting
                            actionTrackingService.recordActionExecuting(action);
                        }

                        // Execute the action
                        directActionExecution(action, actionedUponNodeRef);
                        
                        if (getTrackStatus(action))
                        {
                            // Mark it as having worked
                            actionTrackingService.recordActionComplete(action);
                        }
                    }
                }
                finally
                {
                    if (origActionChain == null)
                    {
                        this.currentActionChain.remove();
                    }
                    else
                    {
                        this.currentActionChain.set(origActionChain);
                    }

                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Resetting the action chain.");
                    }
                }
            }
            catch (Throwable exception)
            {
                // DH: No logging of the exception. Leave the logging decision
                // to the client code,
                // which can handle the rethrown exception.
                if (executedAsynchronously == true)
                {
                    // If one is specified, queue the compensating action ready
                    // for execution
                    Action compensatingAction = action.getCompensatingAction();
                    if (compensatingAction != null)
                    {
                        // Set the current user
                        ((ActionImpl) compensatingAction).setRunAsUser(currentUserName);
                        queueAction(compensatingAction, actionedUponNodeRef);
                    }
                }
                
                if (getTrackStatus(action))
                {
                    // Have the failure logged on the action
                    actionTrackingService.recordActionFailure(action, exception);
                }

                // Rethrow the exception
                if (exception instanceof RuntimeException)
                {
                    throw (RuntimeException) exception;
                }
                else
                {
                    throw new ActionServiceException(ERR_FAIL, exception);
                }

            }
        }
    }
    
    /**
     * @see org.alfresco.repo.action.RuntimeActionService#directActionExecution(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    public void directActionExecution(Action action, NodeRef actionedUponNodeRef)
    {
        // Debug output
        if (logger.isDebugEnabled() == true)
        {
            logger
                        .debug("The action is being executed as the user: "
                                    + this.authenticationContext.getCurrentUserName());
        }

        // Get the action executer and execute
        ActionExecuter executer = (ActionExecuter) this.applicationContext.getBean(action.getActionDefinitionName());
        executer.execute(action, actionedUponNodeRef);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#executeAction(org.alfresco.service.cmr.action.Action,
     *      NodeRef)
     */
    public void executeAction(Action action, NodeRef actionedUponNodeRef)
    {
        executeAction(action, actionedUponNodeRef, true);
    }

    /**
     * @see org.alfresco.repo.action.RuntimeActionService#registerActionConditionEvaluator(org.alfresco.repo.action.evaluator.ActionConditionEvaluator)
     */
    public void registerActionConditionEvaluator(ActionConditionEvaluator actionConditionEvaluator)
    {
        ActionConditionDefinition cond = actionConditionEvaluator.getActionConditionDefintion();
        this.conditionDefinitions.put(cond.getName(), cond);
    }

    /**
     * @see org.alfresco.repo.action.RuntimeActionService#registerActionExecuter(org.alfresco.repo.action.executer.ActionExecuter)
     */
    public void registerActionExecuter(ActionExecuter actionExecuter)
    {
        ActionDefinition action = actionExecuter.getActionDefinition();
        this.actionDefinitions.put(action.getName(), action);
    }

    /**
     * @see org.alfresco.repo.action.RuntimeActionService#registerParameterConstraint(org.alfresco.service.cmr.action.ParameterConstraint)
     */
    public void registerParameterConstraint(ParameterConstraint parameterConstraint)
    {
        this.parameterConstraints.put(parameterConstraint.getName(), parameterConstraint);
    }

    /**
     * Gets the action node ref from the action id
     * 
     * @param nodeRef the node reference
     * @param actionId the action id
     * @return the action node reference
     */
    private NodeRef getActionNodeRefFromId(NodeRef nodeRef, String actionId)
    {
        NodeRef result = null;

        if (this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == true)
        {
            DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver();
            namespacePrefixResolver.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX,
                        NamespaceService.SYSTEM_MODEL_1_0_URI);

            List<NodeRef> nodeRefs = searchService.selectNodes(getSavedActionFolderRef(nodeRef), "*[@sys:"
                        + ContentModel.PROP_NODE_UUID.getLocalName() + "='" + actionId + "']", null,
                        namespacePrefixResolver, false);
            if (nodeRefs.size() != 0)
            {
                result = nodeRefs.get(0);
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#saveAction(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void saveAction(NodeRef nodeRef, Action action)
    {
        NodeRef actionNodeRef = getActionNodeRefFromId(nodeRef, action.getId());
        if (actionNodeRef == null)
        {
            if (this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == false)
            {
                // Apply the actionable aspect
                this.nodeService.addAspect(nodeRef, ActionModel.ASPECT_ACTIONS, null);
            }

            // Create the action and reference
            actionNodeRef = createActionNodeRef(action, getSavedActionFolderRef(nodeRef), ContentModel.ASSOC_CONTAINS,
                        ActionModel.ASSOC_NAME_ACTIONS);
        }
        saveActionImpl(actionNodeRef, action);
    }

    public NodeRef createActionNodeRef(Action action, NodeRef parentNodeRef, QName assocTypeName, QName assocName)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ActionModel.PROP_DEFINITION_NAME, action.getActionDefinitionName());
        props.put(ContentModel.PROP_NODE_UUID, action.getId());

        QName actionType = ActionModel.TYPE_ACTION;
        if (action instanceof ActionList<?>)
        {
            actionType = ActionModel.TYPE_COMPOSITE_ACTION;
        }

        // Create the action node
        NodeRef actionNodeRef = this.nodeService.createNode(parentNodeRef, assocTypeName, assocName, actionType, props)
                    .getChildRef();

        // Update the created details and the node reference
        ((ActionImpl) action).setCreator((String) this.nodeService
                    .getProperty(actionNodeRef, ContentModel.PROP_CREATOR));
        ((ActionImpl) action).setCreatedDate((Date) this.nodeService.getProperty(actionNodeRef,
                    ContentModel.PROP_CREATED));
        ((ActionImpl) action).setNodeRef(actionNodeRef);

        return actionNodeRef;
    }

    /**
     * @see org.alfresco.repo.action.RuntimeActionService#saveActionImpl(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void saveActionImpl(NodeRef actionNodeRef, Action action)
    {
        // Save action properties
        saveActionProperties(actionNodeRef, action);

        // Update the parameters of the action
        saveParameters(actionNodeRef, action);

        // Update the conditions of the action
        saveConditions(actionNodeRef, action);

        if (action instanceof ActionList<?>)
        {
            // Update composite action
            saveCompositeActions(actionNodeRef, (ActionList<?>) action);
        }

        // Update the modified details
        ((ActionImpl) action).setModifier((String) this.nodeService.getProperty(actionNodeRef,
                    ContentModel.PROP_MODIFIER));
        ((ActionImpl) action).setModifiedDate((Date) this.nodeService.getProperty(actionNodeRef,
                    ContentModel.PROP_MODIFIED));
    }

    /**
     * Save the action property values
     * 
     * @param actionNodeRef the action node reference
     * @param action the action
     */
    private void saveActionProperties(NodeRef actionNodeRef, Action action)
    {
        // Update the action property values
        Map<QName, Serializable> props = this.nodeService.getProperties(actionNodeRef);
        props.put(ActionModel.PROP_ACTION_TITLE, action.getTitle());
        props.put(ActionModel.PROP_ACTION_DESCRIPTION, action.getDescription());
        if (action.getTrackStatus() != null)
        {
            props.put(ActionModel.PROP_TRACK_STATUS, action.getTrackStatus());
        }
        props.put(ActionModel.PROP_EXECUTE_ASYNCHRONOUSLY, action.getExecuteAsychronously());
        
        props.put(ActionModel.PROP_EXECUTION_START_DATE, action.getExecutionStartDate());
        props.put(ActionModel.PROP_EXECUTION_END_DATE, action.getExecutionEndDate());
        props.put(ActionModel.PROP_EXECUTION_ACTION_STATUS, action.getExecutionStatus());
        props.put(ActionModel.PROP_EXECUTION_FAILURE_MESSAGE, action.getExecutionFailureMessage());
        
        this.nodeService.setProperties(actionNodeRef, props);

        // Update the compensating action (model should enforce the singularity
        // of this association)
        Action compensatingAction = action.getCompensatingAction();
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(actionNodeRef, RegexQNamePattern.MATCH_ALL,
                    ActionModel.ASSOC_COMPENSATING_ACTION);
        if (assocs.size() == 0)
        {
            if (compensatingAction != null)
            {
                // Map<QName, Serializable> props2 = new HashMap<QName,
                // Serializable>(2);
                // props2.put(ActionModel.PROP_DEFINITION_NAME,
                // compensatingAction.getActionDefinitionName());
                // props2.put(ContentModel.PROP_NODE_UUID,
                // compensatingAction.getId());

                // NodeRef compensatingActionNodeRef =
                // this.nodeService.createNode(
                // / actionNodeRef,
                // ActionModel.ASSOC_COMPENSATING_ACTION,
                // ActionModel.ASSOC_COMPENSATING_ACTION,
                // ActionModel.TYPE_ACTION,
                // props2).getChildRef();

                // Create the compensating node reference
                NodeRef compensatingActionNodeRef = createActionNodeRef(compensatingAction, actionNodeRef,
                            ActionModel.ASSOC_COMPENSATING_ACTION, ActionModel.ASSOC_COMPENSATING_ACTION);
                saveActionImpl(compensatingActionNodeRef, compensatingAction);
            }
        }
        else
        {
            ChildAssociationRef assoc = assocs.get(0);
            if (compensatingAction == null)
            {
                this.nodeService.removeChild(actionNodeRef, assoc.getChildRef());
            }
            else
            {
                saveActionImpl(assoc.getChildRef(), compensatingAction);
            }
        }
    }

    /**
     * Save the actions of a composite action
     * 
     * @param compositeActionNodeRef the node reference of the composite action
     * @param action2 the composite action
     */
    private void saveCompositeActions(NodeRef compositeActionNodeRef, ActionList<?> action2)
    {
        // TODO Need a way of sorting the order of the actions

        Map<String, Action> idToAction = new HashMap<String, Action>();
        List<String> orderedIds = new ArrayList<String>();
        for (Action action : action2.getActions())
        {
            idToAction.put(action.getId(), action);
            orderedIds.add(action.getId());
        }

        List<ChildAssociationRef> actionRefs = this.nodeService.getChildAssocs(compositeActionNodeRef,
                    RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_ACTIONS);
        for (ChildAssociationRef actionRef : actionRefs)
        {
            NodeRef actionNodeRef = actionRef.getChildRef();
            if (idToAction.containsKey(actionNodeRef.getId()) == false)
            {
                // Delete the action
                this.nodeService.removeChild(compositeActionNodeRef, actionNodeRef);
            }
            else
            {
                // Update the action
                Action action = idToAction.get(actionNodeRef.getId());
                saveActionImpl(actionNodeRef, action);
                orderedIds.remove(actionNodeRef.getId());
            }

        }

        // Create the actions remaining
        for (String actionId : orderedIds)
        {
            Action action = idToAction.get(actionId);

            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(ActionModel.PROP_DEFINITION_NAME, action.getActionDefinitionName());
            props.put(ContentModel.PROP_NODE_UUID, action.getId());

            NodeRef actionNodeRef = this.nodeService.createNode(compositeActionNodeRef, ActionModel.ASSOC_ACTIONS,
                        ActionModel.ASSOC_ACTIONS, ActionModel.TYPE_ACTION, props).getChildRef();

            // Update the created details and the node reference
            ((ActionImpl) action).setCreator((String) this.nodeService.getProperty(actionNodeRef,
                        ContentModel.PROP_CREATOR));
            ((ActionImpl) action).setCreatedDate((Date) this.nodeService.getProperty(actionNodeRef,
                        ContentModel.PROP_CREATED));
            ((ActionImpl) action).setNodeRef(actionNodeRef);

            saveActionImpl(actionNodeRef, action);
        }
    }

    /**
     * Saves the conditions associated with an action.
     * 
     * @param actionNodeRef the action node reference
     * @param action the action
     */
    private void saveConditions(NodeRef actionNodeRef, Action action)
    {
        // TODO Need a way of sorting out the order of the conditions
        List<ActionCondition> actionConditionsList = action.getActionConditions();
        saveActionConditionList(actionNodeRef, actionConditionsList, false);
    }

    private void saveActionConditionList(NodeRef actionNodeRef, List<ActionCondition> actionConditionsList,
                boolean isComposite)
    {
        if (logger.isDebugEnabled())
            logger.debug("SaveActionCondition list, " + actionConditionsList.size() + (isComposite ? " Composite" : "")
                        + " conditions to be saved");

        Map<String, ActionCondition> idToCondition = new HashMap<String, ActionCondition>();
        List<String> orderedIds = new ArrayList<String>();

        for (ActionCondition actionCondition : actionConditionsList)
        {
            idToCondition.put(actionCondition.getId(), actionCondition);
            orderedIds.add(actionCondition.getId());
        }

        List<ChildAssociationRef> conditionRefs = this.nodeService.getChildAssocs(actionNodeRef,
                    RegexQNamePattern.MATCH_ALL, !isComposite ? ActionModel.ASSOC_CONDITIONS
                                : ActionModel.ASSOC_COMPOSITE_ACTION_CONDITION);

        for (ChildAssociationRef conditionRef : conditionRefs)
        {
            NodeRef conditionNodeRef = conditionRef.getChildRef();
            if (idToCondition.containsKey(conditionNodeRef.getId()) == false)
            {
                // Delete the condition
                this.nodeService.removeChild(actionNodeRef, conditionNodeRef);
            }
            else
            {
                saveConditionProperties(conditionNodeRef, idToCondition.get(conditionNodeRef.getId()));
                // Update the conditions parameters
                saveParameters(conditionNodeRef, idToCondition.get(conditionNodeRef.getId()));
                orderedIds.remove(conditionNodeRef.getId());
            }
        }

        // Create the conditions remaining
        for (String nextId : orderedIds)
        {
            ActionCondition actionCondition = idToCondition.get(nextId);

            if (!isComposite && actionCondition instanceof CompositeActionCondition)
            {
                if (logger.isDebugEnabled())
                    logger.debug("Saving Composite Condition");

                NodeRef conditionNodeRef = saveActionCondition(actionNodeRef, actionCondition,
                            ActionModel.ASSOC_CONDITIONS, ActionModel.TYPE_COMPOSITE_ACTION_CONDITION);
                saveActionConditionList(conditionNodeRef, ((CompositeActionCondition) actionCondition)
                            .getActionConditions(), true);
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Saving Condition " + actionCondition.getActionConditionDefinitionName());

                @SuppressWarnings("unused")
                NodeRef conditionNodeRef = saveActionCondition(actionNodeRef, actionCondition,
                            !isComposite ? ActionModel.ASSOC_CONDITIONS : ActionModel.ASSOC_COMPOSITE_ACTION_CONDITION,
                            ActionModel.TYPE_ACTION_CONDITION);
            }
        }
    }

    /*
     * private void saveCompositeActionConditionList(NodeRef
     * compositeConditionRef, List<ActionCondition> actionConditionsList) { if
     * (logger.isDebugEnabled())
     * logger.debug("SaveActionCondition list Composite, "+
     * actionConditionsList.size() + " conditions to be saved"); Map<String,
     * ActionCondition> idToCondition = new HashMap<String, ActionCondition>();
     * List<String> orderedIds = new ArrayList<String>(); for (ActionCondition
     * actionCondition : actionConditionsList) {
     * idToCondition.put(actionCondition.getId(), actionCondition);
     * orderedIds.add(actionCondition.getId()); } List<ChildAssociationRef>
     * conditionRefs = this.nodeService.getChildAssocs(compositeConditionRef,
     * RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_CONDITIONS); for
     * (ChildAssociationRef conditionRef : conditionRefs) { NodeRef
     * conditionNodeRef = conditionRef.getChildRef(); if
     * (idToCondition.containsKey(conditionNodeRef.getId()) == false) { //
     * Delete the condition this.nodeService.removeChild(compositeConditionRef,
     * conditionNodeRef); } else { saveConditionProperties(conditionNodeRef,
     * idToCondition.get(conditionNodeRef.getId())); // Update the conditions
     * parameters saveParameters(conditionNodeRef,
     * idToCondition.get(conditionNodeRef.getId()));
     * orderedIds.remove(conditionNodeRef.getId()); } } // Create the conditions
     * remaining for (String nextId : orderedIds) { ActionCondition
     * actionCondition = idToCondition.get(nextId); NodeRef conditionNodeRef =
     * saveActionCondition(compositeConditionRef, actionCondition,
     * ActionModel.ASSOC_CONDITIONS, ActionModel.TYPE_ACTION_CONDITION); } }
     */

    private NodeRef saveActionCondition(NodeRef actionNodeRef, ActionCondition actionCondition, QName AssociationQName,
                QName typeName)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ActionModel.PROP_DEFINITION_NAME, actionCondition.getActionConditionDefinitionName());
        props.put(ContentModel.PROP_NODE_UUID, actionCondition.getId());

        NodeRef conditionNodeRef = this.nodeService.createNode(actionNodeRef, AssociationQName, AssociationQName,
                    typeName, props).getChildRef();

        saveConditionProperties(conditionNodeRef, actionCondition);
        saveParameters(conditionNodeRef, actionCondition);
        return conditionNodeRef;
    }

    /**
     * Save the condition properties
     * 
     * @param conditionNodeRef
     * @param condition
     */
    private void saveConditionProperties(NodeRef conditionNodeRef, ActionCondition condition)
    {
        this.nodeService.setProperty(conditionNodeRef, ActionModel.PROP_CONDITION_INVERT, condition
                    .getInvertCondition());

        if (condition instanceof CompositeActionCondition)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("SAVING OR = " + ((CompositeActionCondition) condition).isORCondition());
            }
            this.nodeService.setProperty(conditionNodeRef, ActionModel.PROP_CONDITION_ANDOR, new Boolean(
                        ((CompositeActionCondition) condition).isORCondition()));
        }
    }

    /**
     * Saves the parameters associated with an action or condition
     * 
     * @param parameterizedNodeRef the parameterized item node reference
     * @param item the parameterized item
     */
    private void saveParameters(NodeRef parameterizedNodeRef, ParameterizedItem item)
    {
        Map<String, Serializable> parameterMap = new HashMap<String, Serializable>();
        parameterMap.putAll(item.getParameterValues());

        List<ChildAssociationRef> parameters = this.nodeService.getChildAssocs(parameterizedNodeRef,
                    ActionModel.ASSOC_PARAMETERS, ActionModel.ASSOC_PARAMETERS);
        for (ChildAssociationRef ref : parameters)
        {
            NodeRef paramNodeRef = ref.getChildRef();
            Map<QName, Serializable> nodeRefParameterMap = this.nodeService.getProperties(paramNodeRef);
            String paramName = (String) nodeRefParameterMap.get(ActionModel.PROP_PARAMETER_NAME);
            if (parameterMap.containsKey(paramName) == false)
            {
                // Delete parameter from node ref
                this.nodeService.removeChild(parameterizedNodeRef, paramNodeRef);
            }
            else
            {
                // Update the parameter value
                nodeRefParameterMap.put(ActionModel.PROP_PARAMETER_VALUE, parameterMap.get(paramName));
                this.nodeService.setProperties(paramNodeRef, nodeRefParameterMap);
                parameterMap.remove(paramName);
            }
        }

        // Add any remaining parameters
        for (Map.Entry<String, Serializable> entry : parameterMap.entrySet())
        {
            Map<QName, Serializable> nodeRefProperties = new HashMap<QName, Serializable>(2);
            nodeRefProperties.put(ActionModel.PROP_PARAMETER_NAME, entry.getKey());
            nodeRefProperties.put(ActionModel.PROP_PARAMETER_VALUE, entry.getValue());

            this.nodeService.createNode(parameterizedNodeRef, ActionModel.ASSOC_PARAMETERS,
                        ActionModel.ASSOC_PARAMETERS, ActionModel.TYPE_ACTION_PARAMETER, nodeRefProperties);
        }
    }

    // TODO: Add copy behaviour

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getActions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<Action> getActions(NodeRef nodeRef)
    {
        List<Action> result = new ArrayList<Action>();

        if (this.nodeService.exists(nodeRef) == true
                    && this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == true)
        {
            List<ChildAssociationRef> actions = this.nodeService.getChildAssocs(getSavedActionFolderRef(nodeRef),
                        RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_NAME_ACTIONS);
            for (ChildAssociationRef action : actions)
            {
                NodeRef actionNodeRef = action.getChildRef();
                result.add(createAction(actionNodeRef));
            }
        }

        return result;
    }

    /**
     * Create an action from the action node reference
     * 
     * @param actionNodeRef the action node reference
     * @return the action
     */
    public Action createAction(NodeRef actionNodeRef)
    {
        Action result = null;

        Map<QName, Serializable> properties = this.nodeService.getProperties(actionNodeRef);

        QName actionType = this.nodeService.getType(actionNodeRef);
        if (ActionModel.TYPE_COMPOSITE_ACTION.equals(actionType) == true)
        {
            // Create a composite action
            result = new CompositeActionImpl(actionNodeRef, actionNodeRef.getId());
            populateCompositeAction(actionNodeRef, (CompositeAction) result);
        }
        else
        {
            // Create an action
            result = new ActionImpl(actionNodeRef, actionNodeRef.getId(), (String) properties
                        .get(ActionModel.PROP_DEFINITION_NAME));
            populateAction(actionNodeRef, result);
        }

        return result;
    }

    /**
     * Populate the details of the action from the node reference
     * 
     * @param actionNodeRef the action node reference
     * @param action the action
     */
    private void populateAction(NodeRef actionNodeRef, Action action)
    {
        // Populate the action properties
        populateActionProperties(actionNodeRef, action);

        // Set the parameters
        populateParameters(actionNodeRef, action);

        // Set the conditions
        List<ChildAssociationRef> conditions = this.nodeService.getChildAssocs(actionNodeRef,
                    RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_CONDITIONS);

        if (logger.isDebugEnabled())
            logger.debug("Retrieving " + (conditions == null ? " null" : conditions.size()) + " conditions");

        if (conditions != null)
        {
            for (ChildAssociationRef condition : conditions)
            {
                NodeRef conditionNodeRef = condition.getChildRef();
                action.addActionCondition(createActionCondition(conditionNodeRef));
            }
        }
    }

    /**
     * Populates the action properties from the node reference
     * 
     * @param actionNodeRef the action node reference
     * @param action the action
     */
    private void populateActionProperties(NodeRef actionNodeRef, Action action)
    {
        Map<QName, Serializable> props = this.nodeService.getProperties(actionNodeRef);

        action.setTitle((String) props.get(ActionModel.PROP_ACTION_TITLE));
        action.setDescription((String) props.get(ActionModel.PROP_ACTION_DESCRIPTION));

        Boolean trackStatusObj = (Boolean) props.get(ActionModel.PROP_TRACK_STATUS);
        action.setTrackStatus(trackStatusObj);              // Allowed to be null

        Boolean executeAsynchObj = (Boolean) props.get(ActionModel.PROP_EXECUTE_ASYNCHRONOUSLY);
        boolean executeAsynch = executeAsynchObj == null ? false : executeAsynchObj.booleanValue();
        action.setExecuteAsynchronously(executeAsynch);

        ((ActionImpl) action).setCreator((String) props.get(ContentModel.PROP_CREATOR));
        ((ActionImpl) action).setCreatedDate((Date) props.get(ContentModel.PROP_CREATED));
        ((ActionImpl) action).setModifier((String) props.get(ContentModel.PROP_MODIFIER));
        ((ActionImpl) action).setModifiedDate((Date) props.get(ContentModel.PROP_MODIFIED));
        
        ((ActionImpl) action).setExecutionStartDate((Date) props.get(ActionModel.PROP_EXECUTION_START_DATE));
        ((ActionImpl) action).setExecutionEndDate((Date) props.get(ActionModel.PROP_EXECUTION_END_DATE));
        ((ActionImpl) action).setExecutionStatus(ActionStatus.valueOf(props.get(ActionModel.PROP_EXECUTION_ACTION_STATUS)));
        ((ActionImpl) action).setExecutionFailureMessage((String) props.get(ActionModel.PROP_EXECUTION_FAILURE_MESSAGE));

        // Get the compensating action
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(actionNodeRef, RegexQNamePattern.MATCH_ALL,
                    ActionModel.ASSOC_COMPENSATING_ACTION);
        if (assocs.size() != 0)
        {
            Action compensatingAction = createAction(assocs.get(0).getChildRef());
            action.setCompensatingAction(compensatingAction);
        }
    }

    /**
     * Populate the parameters of a parameterized item from the parameterized
     * item node reference
     * 
     * @param parameterizedItemNodeRef the parameterized item node reference
     * @param parameterizedItem the parameterized item
     */
    private void populateParameters(NodeRef parameterizedItemNodeRef, ParameterizedItem parameterizedItem)
    {
        List<ChildAssociationRef> parameters = this.nodeService.getChildAssocs(parameterizedItemNodeRef,
                    RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_PARAMETERS);
        for (ChildAssociationRef parameter : parameters)
        {
            NodeRef parameterNodeRef = parameter.getChildRef();
            Map<QName, Serializable> properties = this.nodeService.getProperties(parameterNodeRef);
            parameterizedItem.setParameterValue((String) properties.get(ActionModel.PROP_PARAMETER_NAME), properties
                        .get(ActionModel.PROP_PARAMETER_VALUE));
        }
    }

    /**
     * Creates an action condition from an action condition node reference
     * 
     * @param conditionNodeRef the condition node reference
     * @return the action condition
     */
    private ActionCondition createActionCondition(NodeRef conditionNodeRef)
    {
        if (logger.isDebugEnabled())
            logger.debug("\tCreateActionCondition: Retrieving Conditions from repository");

        Map<QName, Serializable> properties = this.nodeService.getProperties(conditionNodeRef);
        QName conditionType = this.nodeService.getType(conditionNodeRef);

        ActionCondition condition = null;
        if (ActionModel.TYPE_COMPOSITE_ACTION_CONDITION.equals(conditionType) == false)
        {
            condition = new ActionConditionImpl(conditionNodeRef.getId(), (String) properties
                        .get(ActionModel.PROP_DEFINITION_NAME));
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("\tRetrieving Composite Condition from repository");
            }

            // Create a composite condition
            CompositeActionCondition compositeCondition = new CompositeActionConditionImpl(GUID.generate());
            populateCompositeActionCondition(conditionNodeRef, compositeCondition);

            condition = compositeCondition;
        }

        Boolean invert = (Boolean) this.nodeService.getProperty(conditionNodeRef, ActionModel.PROP_CONDITION_INVERT);
        condition.setInvertCondition(invert == null ? false : invert.booleanValue());

        populateParameters(conditionNodeRef, condition);
        return condition;
    }

    private void populateCompositeActionCondition(NodeRef compositeNodeRef,
                CompositeActionCondition compositeActionCondition)
    {
        List<ChildAssociationRef> conditions = this.nodeService.getChildAssocs(compositeNodeRef,
                    RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_COMPOSITE_ACTION_CONDITION);

        Boolean OR = (Boolean) this.nodeService.getProperty(compositeNodeRef, ActionModel.PROP_CONDITION_ANDOR);

        if (logger.isDebugEnabled())
        {
            logger.debug("\tPopulating Composite Condition with subconditions, Condition OR = " + OR);
        }

        compositeActionCondition.setORCondition(OR == null ? false : OR.booleanValue());

        for (ChildAssociationRef conditionNodeRef : conditions)
        {
            NodeRef actionNodeRef = conditionNodeRef.getChildRef();
            ActionCondition currentCondition = createActionCondition(actionNodeRef);

            if (logger.isDebugEnabled())
                logger.debug("\t\tAdding subcondition " + currentCondition.getActionConditionDefinitionName());

            compositeActionCondition.addActionCondition(currentCondition);
        }
    }

    /**
     * Populates a composite action from a composite action node reference
     * 
     * @param compositeNodeRef the composite action node reference
     * @param compositeAction the composite action
     */
    public void populateCompositeAction(NodeRef compositeNodeRef, CompositeAction compositeAction)
    {
        populateAction(compositeNodeRef, compositeAction);

        List<ChildAssociationRef> actions = this.nodeService.getChildAssocs(compositeNodeRef,
                    RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_ACTIONS);
        for (ChildAssociationRef action : actions)
        {
            NodeRef actionNodeRef = action.getChildRef();
            compositeAction.addAction(createAction(actionNodeRef));
        }
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#getAction(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    public Action getAction(NodeRef nodeRef, String actionId)
    {
        Action result = null;

        if (this.nodeService.exists(nodeRef) == true
                    && this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == true)
        {
            NodeRef actionNodeRef = getActionNodeRefFromId(nodeRef, actionId);
            if (actionNodeRef != null)
            {
                result = createAction(actionNodeRef);
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#removeAction(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(NodeRef nodeRef, Action action)
    {
        if (this.nodeService.exists(nodeRef) == true
                    && this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == true)
        {
            NodeRef actionNodeRef = getActionNodeRefFromId(nodeRef, action.getId());
            if (actionNodeRef != null)
            {
                this.nodeService.removeChild(getSavedActionFolderRef(nodeRef), actionNodeRef);
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionService#removeAllActions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeAllActions(NodeRef nodeRef)
    {
        if (this.nodeService.exists(nodeRef) == true
                    && this.nodeService.hasAspect(nodeRef, ActionModel.ASPECT_ACTIONS) == true)
        {
            List<ChildAssociationRef> actions = new ArrayList<ChildAssociationRef>(this.nodeService.getChildAssocs(
                        getSavedActionFolderRef(nodeRef), RegexQNamePattern.MATCH_ALL, ActionModel.ASSOC_NAME_ACTIONS));
            for (ChildAssociationRef action : actions)
            {
                this.nodeService.removeChild(getSavedActionFolderRef(nodeRef), action.getChildRef());
            }
        }
    }

    /**
     * Add a pending action to the list to be queued for execution once the
     * transaction is completed.
     * 
     * @param action the action
     * @param actionedUponNodeRef the actioned upon node reference
     * @param checkConditions indicates whether to check the conditions before
     *            execution
     */
    @SuppressWarnings("unchecked")
    private void addPostTransactionPendingAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions,
                Set<String> actionChain)
    {
        if (logger.isDebugEnabled() == true)
        {
            StringBuilder builder = new StringBuilder("addPostTransactionPendingAction action chain = ");
            if (actionChain == null)
            {
                builder.append("null");
            }
            else
            {
                for (String value : actionChain)
                {
                    builder.append(value).append(" ");
                }
            }
            logger.debug(builder.toString());
            logger.debug("Current action = " + action.getId());
        }

        // Don't continue if the action is already in the action chain
        if (actionChain == null || actionChain.contains(action.getId()) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Doing addPostTransactionPendingAction");
            }

            // Set the run as user to the current user
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("The current user is: " + this.authenticationContext.getCurrentUserName());
            }
            ((ActionImpl) action).setRunAsUser(this.authenticationContext.getCurrentUserName());

            // Ensure that the transaction listener is bound to the transaction
            AlfrescoTransactionSupport.bindListener(this.transactionListener);

            // Add the pending action to the transaction resource
            List<PendingAction> pendingActions = (List<PendingAction>) AlfrescoTransactionSupport
                        .getResource(POST_TRANSACTION_PENDING_ACTIONS);
            if (pendingActions == null)
            {
                pendingActions = new ArrayList<PendingAction>();
                AlfrescoTransactionSupport.bindResource(POST_TRANSACTION_PENDING_ACTIONS, pendingActions);
            }

            // Check that action has only been added to the list once
            PendingAction pendingAction = new PendingAction(action, actionedUponNodeRef, checkConditions, actionChain);
            if (pendingActions.contains(pendingAction) == false)
            {
                pendingActions.add(pendingAction);
                actionTrackingService.recordActionPending(action);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.RuntimeActionService#getPostTransactionPendingActions()
     */
    @SuppressWarnings("unchecked")
    private List<PendingAction> getPostTransactionPendingActions()
    {
        return (List<PendingAction>) AlfrescoTransactionSupport.getResource(POST_TRANSACTION_PENDING_ACTIONS);
    }

    /**
     * Pending action details class
     */
    private class PendingAction
    {
        /**
         * The action
         */
        private Action action;

        /**
         * The actioned upon node reference
         */
        private NodeRef actionedUponNodeRef;

        /**
         * Indicates whether the conditions should be checked before the action
         * is executed
         */
        private boolean checkConditions;

        private Set<String> actionChain;

        /**
         * Constructor
         * 
         * @param action the action
         * @param actionedUponNodeRef the actioned upon node reference
         * @param checkConditions indicated whether the conditions need to be
         *            checked
         */
        public PendingAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions,
                    Set<String> actionChain)
        {
            this.action = action;
            this.actionedUponNodeRef = actionedUponNodeRef;
            this.checkConditions = checkConditions;
            this.actionChain = actionChain;
        }

        /**
         * Get the action
         * 
         * @return the action
         */
        public Action getAction()
        {
            return action;
        }

        /**
         * Get the actioned upon node reference
         * 
         * @return the actioned upon node reference
         */
        public NodeRef getActionedUponNodeRef()
        {
            return actionedUponNodeRef;
        }

        /**
         * Get the check conditions value
         * 
         * @return indicates whether the condition should be checked
         */
        public boolean getCheckConditions()
        {
            return this.checkConditions;
        }

        public Set<String> getActionChain()
        {
            return this.actionChain;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            int hashCode = 37 * this.actionedUponNodeRef.hashCode();
            hashCode += 37 * this.action.hashCode();
            return hashCode;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof PendingAction)
            {
                PendingAction that = (PendingAction) obj;
                return (this.action.equals(that.action) && this.actionedUponNodeRef.equals(that.actionedUponNodeRef));
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * @return Returns {@link AdctionParameterTypeCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return AdctionParameterTypeCopyBehaviourCallback.INSTANCE;
    }

    /**
     * Records any potential <b>d:noderef</d> properties for once the copy is
     * done.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class AdctionParameterTypeCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final AdctionParameterTypeCopyBehaviourCallback INSTANCE = new AdctionParameterTypeCopyBehaviourCallback();

        @Override
        public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails,
                    Map<QName, Serializable> properties)
        {
            NodeRef sourceNodeRef = copyDetails.getSourceNodeRef();
            recordNodeRefsForRepointing(sourceNodeRef, properties, ActionModel.PROP_PARAMETER_VALUE);
            // Don't modify the properties
            return properties;
        }
    }

    /**
     * Ensures that <b>d:noderef</b> properties are repointed if the target was
     * also copied as part of the hierarchy.
     */
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode,
                Map<NodeRef, NodeRef> copyMap)
    {
        AdctionParameterTypeCopyBehaviourCallback.INSTANCE.repointNodeRefs(sourceNodeRef, targetNodeRef,
                    ActionModel.PROP_PARAMETER_VALUE, copyMap, nodeService);
    }
}
