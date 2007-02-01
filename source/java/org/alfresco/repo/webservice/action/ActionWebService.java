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
package org.alfresco.repo.webservice.action;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action web service implementation
 * 
 * @author Roy Wetherall
 */
public class ActionWebService extends AbstractWebService implements ActionServiceSoapPort
{
    /** Log */
    private static Log logger = LogFactory.getLog(ActionWebService.class);
    
    /** The action service */
    private ActionService actionService;
    
    /** The rule service */
    private RuleService ruleService;
    
    /** The dictionary service */
    private DictionaryService dictionaryService;
    
    /** The transaction service */
    private TransactionComponent transactionService;
    
    /**
     * Set the action service
     * 
     * @param actionService     the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the rule service
     * 
     * @param ruleService   the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set the dictionary service
     * 
     * @param dictionaryService     the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Sets the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionComponent transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getConditionDefinitions()
     */
    public ActionItemDefinition[] getConditionDefinitions() throws RemoteException,
            ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<ActionItemDefinition[]>()
            {
                public ActionItemDefinition[] doWork() throws Exception
                {
                    return getConditionDefintionsImpl();
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * 
     * @return
     * @throws RemoteException
     */
    private ActionItemDefinition[] getConditionDefintionsImpl() throws RemoteException
    {
        // Get the action condition defintions from the action service
        List<ActionConditionDefinition> definitions = this.actionService.getActionConditionDefinitions();
        
        // Marshal the results into an array of action item types
        ActionItemDefinition[] result = new ActionItemDefinition[definitions.size()];
        int index = 0;
        for (ActionConditionDefinition definition : definitions)
        {
            result[index] = convertToActionItemDefintion(definition, ActionItemDefinitionType.condition);
            index++;
        }
        
        return result;
    }

    /**
     *  @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getActionDefinitions()
     */
    public ActionItemDefinition[] getActionDefinitions() throws RemoteException,
            ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<ActionItemDefinition[]>()
            {
                public ActionItemDefinition[] doWork() throws Exception
                {
                    return getActionDefinitionsImpl();
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * 
     * @return
     * @throws RemoteException
     */
    private ActionItemDefinition[] getActionDefinitionsImpl() throws RemoteException
    {
        // Get the action defintions from the action service
        List<ActionDefinition> definitions = this.actionService.getActionDefinitions();
        
        // Marshal the results into an array of action item types
        ActionItemDefinition[] result = new ActionItemDefinition[definitions.size()];
        int index = 0;
        for (ActionDefinition definition : definitions)
        {
            result[index] = convertToActionItemDefintion(definition, ActionItemDefinitionType.action);
            index++;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getActionItemDefinition(java.lang.String, org.alfresco.repo.webservice.action.ActionItemDefinitionType)
     */
    public ActionItemDefinition getActionItemDefinition(final String name, final ActionItemDefinitionType definitionType) throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<ActionItemDefinition>()
            {
                public ActionItemDefinition doWork() throws Exception
                {
                    return getActionItemDefinitionImpl(name, definitionType);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * 
     * @param name
     * @param definitionType
     * @return
     * @throws RemoteException
     * @throws ActionFault
     */
    public ActionItemDefinition getActionItemDefinitionImpl(String name, ActionItemDefinitionType definitionType) throws RemoteException, ActionFault
    {
        ActionItemDefinition result = null;
        
        if (definitionType.equals(ActionItemDefinitionType.action) == true)
        {
            ActionDefinition actionDefinition = this.actionService.getActionDefinition(name);
            if (actionDefinition != null)
            {
                result = convertToActionItemDefintion(actionDefinition, definitionType);
            }
        }
        else
        {
            ActionConditionDefinition conditionDefinition = this.actionService.getActionConditionDefinition(name);
            if (conditionDefinition != null)
            {
                result = convertToActionItemDefintion(conditionDefinition, definitionType);
            }
        }
        
        return result;
    }

    /**
     * Marshal the parameterized item defintion into a action item defition object.
     * 
     * @param definition
     * @param type
     * @return
     */
    private ActionItemDefinition convertToActionItemDefintion(ParameterizedItemDefinition definition, ActionItemDefinitionType type)
    {
        // Create action item defintion
        ActionItemDefinition actionItemType = new ActionItemDefinition();
        actionItemType.setName(definition.getName());
        actionItemType.setType(type);
        actionItemType.setTitle(definition.getTitle());
        actionItemType.setDescription(definition.getDescription());
        actionItemType.setAdHocPropertiesAllowed(definition.getAdhocPropertiesAllowed());
        
        // Marshal the paremeter definitions
        List<ParameterDefinition> params = definition.getParameterDefinitions();
        org.alfresco.repo.webservice.action.ParameterDefinition[] parameterDefinitions = new org.alfresco.repo.webservice.action.ParameterDefinition[params.size()];
        int index = 0;
        for (ParameterDefinition paramDef : params)
        {
            org.alfresco.repo.webservice.action.ParameterDefinition parameterDefinition = new org.alfresco.repo.webservice.action.ParameterDefinition(
                    paramDef.getName(),
                    paramDef.getType().toString(),
                    paramDef.isMandatory(),
                    paramDef.getDisplayLabel());
            parameterDefinitions[index] = parameterDefinition;
            index ++;
        }
        actionItemType.setParameterDefinition(parameterDefinitions);
        
        return actionItemType;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getRuleTypes()
     */
    public org.alfresco.repo.webservice.action.RuleType[] getRuleTypes() throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.RuleType[]>()
            {
                public org.alfresco.repo.webservice.action.RuleType[] doWork() throws Exception
                {
                    return getRuleTypesImpl();
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
    }
    }
    
    public org.alfresco.repo.webservice.action.RuleType[] getRuleTypesImpl() throws RemoteException, ActionFault
    {
        // Get the list of rule types
        List<RuleType> ruleTypes = this.ruleService.getRuleTypes();
        
        // Marshal the rule types into an array
        org.alfresco.repo.webservice.action.RuleType[] results = new org.alfresco.repo.webservice.action.RuleType[ruleTypes.size()];
        int index = 0;
        for (RuleType ruleType : ruleTypes)
        {
            org.alfresco.repo.webservice.action.RuleType webServiceRuleType = new org.alfresco.repo.webservice.action.RuleType(
                    ruleType.getName(),
                    ruleType.getDisplayLabel());
            results[index] = webServiceRuleType;
            index ++;
        }
        
        return results;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getRuleType(java.lang.String)
     */
    public org.alfresco.repo.webservice.action.RuleType getRuleType(final String name) throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.RuleType>()
            {
                public org.alfresco.repo.webservice.action.RuleType doWork() throws Exception
                {
                    return getRuleTypeImpl(name);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    public org.alfresco.repo.webservice.action.RuleType getRuleTypeImpl(String name) throws RemoteException, ActionFault
    {
        org.alfresco.repo.webservice.action.RuleType result = null;
        
        RuleType ruleType = this.ruleService.getRuleType(name);
        if (ruleType != null)
        {
            result = new org.alfresco.repo.webservice.action.RuleType(ruleType.getName(), ruleType.getDisplayLabel());
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getActions(org.alfresco.repo.webservice.types.Reference, java.lang.String[])
     */
    public org.alfresco.repo.webservice.action.Action[] getActions(final Reference reference, final ActionFilter filter) throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.Action[]>()
            {
                public org.alfresco.repo.webservice.action.Action[] doWork() throws Exception
                {
                    return getActionsImpl(reference, filter);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    private org.alfresco.repo.webservice.action.Action[] getActionsImpl(Reference reference, ActionFilter filter) throws RemoteException, ActionFault
    {
        // Get the actions
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);        
        List<Action> actions = this.actionService.getActions(nodeRef);
        
        org.alfresco.repo.webservice.action.Action[] webServiceActions = new org.alfresco.repo.webservice.action.Action[actions.size()];
        
        // Filter the results
        if (filter != null)
        {
            // TODO implement the filters
        }
        
        // Marshal the results
        int index = 0;
        for (Action action : actions)
        {
            webServiceActions[index] = convertToWebServiceAction(action);
            index++;
        }
        
        return webServiceActions;
    }
    
    private org.alfresco.repo.webservice.action.Action convertToWebServiceAction(Action action)
    {
        // Get the parameters into a named value array
        NamedValue[] namedValues = convertParametersToNamedValues(action);
        
        // Get the conditions
        List<ActionCondition> conditions = action.getActionConditions();
        Condition[] webServiceConditions = new Condition[conditions.size()];
        int index2 = 0;
        for (ActionCondition condition : conditions)
        {
            webServiceConditions[index2] = convertToWebServiceCondition(condition);
            index2++;
        }
        
        // Get the compenstaing action
        Action compensatingAction = action.getCompensatingAction();
        org.alfresco.repo.webservice.action.Action webServiceCompensatingAction = null;
        if (compensatingAction != null)
        {
            webServiceCompensatingAction = convertToWebServiceAction(compensatingAction);
        }
        
        // Sort out any sub-actions
        org.alfresco.repo.webservice.action.Action[] childWebServiceActions = null;
        if (action instanceof CompositeAction)
        {
            List<Action> childActions = ((CompositeAction)action).getActions();
            childWebServiceActions = new org.alfresco.repo.webservice.action.Action[childActions.size()];
            int index3 = 0;
            for (Action childAction : childActions)
            {
                childWebServiceActions[index3] = convertToWebServiceAction(childAction);
                index3 ++;
            }
        }
        
        // Create the web service action object
        org.alfresco.repo.webservice.action.Action webServiceAction = new org.alfresco.repo.webservice.action.Action(
                Utils.convertToReference(this.nodeService, this.namespaceService, action.getNodeRef()),
                action.getId(),
                action.getActionDefinitionName(),
                action.getTitle(),
                action.getDescription(),
                namedValues,
                webServiceConditions,
                webServiceCompensatingAction,
                childWebServiceActions);
        
        return webServiceAction;
    }

    /**
     * 
     * @param item
     * @return
     */
    private NamedValue[] convertParametersToNamedValues(ParameterizedItem item)
    {
        NamedValue[] namedValues = null;
        if (item != null)
        {
            Map<String, Serializable> params = item.getParameterValues();
            namedValues = new NamedValue[params.size()];
            int index = 0;
            for (Map.Entry<String, Serializable> entry : params.entrySet())
            {
                String value = null;
                try
                {
                    value = DefaultTypeConverter.INSTANCE.convert(String.class, entry.getValue());
                } 
                catch (Throwable exception)
                {
                    value = entry.getValue().toString();
                } 
                NamedValue namedValue = new NamedValue();
                namedValue.setName(entry.getKey());
                namedValue.setIsMultiValue(false);
                namedValue.setValue(value);               
                namedValues[index] = namedValue;
                index++;
            }
        }
        return namedValues;
    }
    
    /**
     * 
     * @param condition
     * @return
     */
    private Condition convertToWebServiceCondition(ActionCondition condition)
    {
        // Get the parameter values as an array of names values
        NamedValue[] namedValues = convertParametersToNamedValues(condition);
        
        Condition webServiceCondition = new Condition(
                condition.getId(),
                condition.getActionConditionDefinitionName(),
                condition.getInvertCondition(),
                namedValues);
        
        return webServiceCondition;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#saveActions(org.alfresco.repo.webservice.types.Reference, org.alfresco.repo.webservice.action.Action[])
     */
    public org.alfresco.repo.webservice.action.Action[] saveActions(
            final Reference reference, 
            final org.alfresco.repo.webservice.action.Action[] webServiceActions) throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.Action[]>()
            {
                public org.alfresco.repo.webservice.action.Action[] doWork() throws Exception
                {
                    return saveActionsImpl(reference, webServiceActions);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * 
     * @param reference
     * @param webServiceActions
     * @return
     * @throws RemoteException
     * @throws ActionFault
     */
    private org.alfresco.repo.webservice.action.Action[] saveActionsImpl(
            Reference reference, 
            org.alfresco.repo.webservice.action.Action[] webServiceActions) throws RemoteException, ActionFault
    {
        // Get the node reference
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        
        // Create the result array
        org.alfresco.repo.webservice.action.Action[] results = new org.alfresco.repo.webservice.action.Action[webServiceActions.length];
        
        int index = 0;
        for (org.alfresco.repo.webservice.action.Action webServiceAction : webServiceActions)
        {
            // Convert to a server action object
            Action action = convertToAction(webServiceAction);
            
            // Save the action
            this.actionService.saveAction(nodeRef, action);
            
            // Add the updated action to the results
            results[index] = convertToWebServiceAction(action);
            index++;
        }
        
        return results;
    }
    
    /**
     * Convert a web service action object into a repository action object.
     * 
     * @param webServiceAction  the web service action object
     * @return                  the repository action object
     */
    private Action convertToAction(org.alfresco.repo.webservice.action.Action webServiceAction)
    {
        // If the id is null then generate one
        String id = webServiceAction.getId();
        if (id == null || id.length() == 0)
        {
            id = GUID.generate();
        }
        
        // Try and get the action node reference
        NodeRef actionNodeRef = null;
        Reference actionReference = webServiceAction.getActionReference();
        if (actionReference != null)
        {
            actionNodeRef = Utils.convertToNodeRef(actionReference, this.nodeService, this.searchService, this.namespaceService);
        }
        
        // Create the action (or composite action)
        ActionImpl action = null;
        String actionDefinitionName = webServiceAction.getActionName();        
        if (CompositeActionExecuter.NAME.equals(actionDefinitionName) == true)
        {
            action = new CompositeActionImpl(actionNodeRef, id);
        }
        else
        {
            action = new ActionImpl(actionNodeRef, id, actionDefinitionName);
        }
        
        // Set some of the action's details
        action.setTitle(webServiceAction.getTitle());
        action.setDescription(webServiceAction.getDescription());
        
        // Set the parameters
        NamedValue[] namedValues = webServiceAction.getParameters();
        for (NamedValue namedValue : namedValues)
        {
            // Get the type of the property
            DataTypeDefinition propertyType = null;
            ActionDefinition actionDefintion = this.actionService.getActionDefinition(action.getActionDefinitionName());
            ParameterDefinition propertyDefintion = actionDefintion.getParameterDefintion(namedValue.getName());
            if (propertyDefintion != null)
            {
                propertyType = this.dictionaryService.getDataType(propertyDefintion.getType());
            }
            
            // Convert the value into the correct type
            Serializable value = null;
            if (propertyType != null)
            {
                value = (Serializable)DefaultTypeConverter.INSTANCE.convert(propertyType, namedValue.getValue());
            }
            else
            {
                value = namedValue.getValue();
            }
            
            // Set the parameter
            action.setParameterValue(namedValue.getName(), value);
        }
        
        // Set the conditions
        Condition[] webServiceConditions = webServiceAction.getConditions();
        if (webServiceConditions != null)
        {
            for (Condition webServiceCondition : webServiceConditions)
            {
                action.addActionCondition(convertToActionCondition(webServiceCondition));
            }
        }
        
        // Set the compensating action
        org.alfresco.repo.webservice.action.Action webServiceCompensatingAction = webServiceAction.getCompensatingAction();
        if (webServiceCompensatingAction != null)
        {
            Action compensatingAction = convertToAction(webServiceCompensatingAction);
            action.setCompensatingAction(compensatingAction);
        }
        
        // Set the child actions (if we are dealing with a composite action)
        if (CompositeActionExecuter.NAME.equals(actionDefinitionName) == true)
        {
            org.alfresco.repo.webservice.action.Action[] webServiceChildActions = webServiceAction.getActions();
            if (webServiceChildActions != null)
            {
                for (org.alfresco.repo.webservice.action.Action webServiceChildAction : webServiceChildActions)
                {
                    Action childAction = convertToAction(webServiceChildAction);
                    ((CompositeAction)action).addAction(childAction);
                }
            }
        }
        
        return action;
    }
    
    /**
     * 
     * @param webServiceCondition
     * @return
     */
    private ActionCondition convertToActionCondition(Condition webServiceCondition)
    {
        // If the id is null then generate one
        String id = webServiceCondition.getId();
        if (id == null || id.length() == 0)
        {
            id = GUID.generate();
        }
        
        // Create the action condition
        ActionCondition actionCondition = new ActionConditionImpl(id, webServiceCondition.getConditionName());
        
        // Set the details of the condition
        actionCondition.setInvertCondition(webServiceCondition.isInvertCondition());
        
        // Set the condition parameters
        NamedValue[] namedValues = webServiceCondition.getParameters();
        for (NamedValue namedValue : namedValues)
        {
            // Get the type of the property
            DataTypeDefinition propertyType = null;
            ActionConditionDefinition actionConditionDefintion = this.actionService.getActionConditionDefinition(actionCondition.getActionConditionDefinitionName());
            ParameterDefinition propertyDefintion = actionConditionDefintion.getParameterDefintion(namedValue.getName());
            if (propertyDefintion != null)
            {
                propertyType = this.dictionaryService.getDataType(propertyDefintion.getType());
            }
            
            // Convert the value into the correct type
            Serializable value = null;
            if (propertyType != null)
            {
                value = (Serializable)DefaultTypeConverter.INSTANCE.convert(propertyType, namedValue.getValue());
            }
            else
            {
                value = namedValue.getValue();
            }
            
            // Set the parameter
            actionCondition.setParameterValue(namedValue.getName(), value);
        }
        
        return actionCondition;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#removeActions(org.alfresco.repo.webservice.types.Reference, org.alfresco.repo.webservice.action.Action[])
     */
    public void removeActions(final Reference reference, final org.alfresco.repo.webservice.action.Action[] webServiceActions)
            throws RemoteException, ActionFault
    {
        try
        {
            TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    removeActionsImpl(reference, webServiceActions);
                    return null;
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    private void removeActionsImpl(Reference reference, org.alfresco.repo.webservice.action.Action[] webServiceActions)
        throws RemoteException, ActionFault
    {
        // Get the node reference
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        
        if (webServiceActions == null)
        {
            // Remove all the actions
            this.actionService.removeAllActions(nodeRef);
        }
        else
        {
            for (org.alfresco.repo.webservice.action.Action webServiceAction : webServiceActions)
            {
                Action action = convertToAction(webServiceAction);
                this.actionService.removeAction(nodeRef, action);
            }
        }
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#executeActions(org.alfresco.repo.webservice.types.Predicate, org.alfresco.repo.webservice.action.Action[])
     */
    public ActionExecutionResult[] executeActions(final Predicate predicate, final org.alfresco.repo.webservice.action.Action[] webServiceActions) throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<ActionExecutionResult[]>()
            {
                public ActionExecutionResult[] doWork() throws Exception
                {
                    return executeActionsImpl(predicate, webServiceActions);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * Execute actions implementation
     * 
     * @param predicate
     * @param webServiceActions
     * @return
     * @throws RemoteException
     * @throws ActionFault
     */
    public ActionExecutionResult[] executeActionsImpl(Predicate predicate, org.alfresco.repo.webservice.action.Action[] webServiceActions) throws RemoteException, ActionFault
    {
        List<ActionExecutionResult> results = new ArrayList<ActionExecutionResult>(10);
        
        // Resolve the predicate to a list of nodes
        List<NodeRef> nodeRefs = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        for (NodeRef nodeRef : nodeRefs)
        {
            // Create the execution result object and set the action reference
            ActionExecutionResult executionResult = new ActionExecutionResult();
            executionResult.setReference(Utils.convertToReference(this.nodeService, this.namespaceService, nodeRef));
            
            // Tyr and execute the actions
            List<org.alfresco.repo.webservice.action.Action> executedActions = new ArrayList<org.alfresco.repo.webservice.action.Action>(10);
            for (org.alfresco.repo.webservice.action.Action webServiceAction : webServiceActions)
            {
                // Get the repository action object
                Action action = convertToAction(webServiceAction);
                
                // TODO what about condition inversion
                if (this.actionService.evaluateAction(action, nodeRef) == true)
                {
                    // Execute the action (now that we know the conditions have been met)
                    this.actionService.executeAction(action, nodeRef, false);                    
                    executedActions.add(webServiceAction);
                }
            }
            
            // Set the executed actions on the execution result object
            org.alfresco.repo.webservice.action.Action[] executedWebServiceActions = (org.alfresco.repo.webservice.action.Action[])executedActions.toArray(new org.alfresco.repo.webservice.action.Action[executedActions.size()]);
            executionResult.setActions(executedWebServiceActions);
            
            // Add the execution object to the result list
            results.add(executionResult);
        }
        return (ActionExecutionResult[])results.toArray(new ActionExecutionResult[results.size()]);
    }  

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#getRules(org.alfresco.repo.webservice.types.Reference, org.alfresco.repo.webservice.action.RuleFilter)
     */
    public org.alfresco.repo.webservice.action.Rule[] getRules(final Reference reference, final RuleFilter ruleFilter)
            throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.Rule[]>()
            {
                public org.alfresco.repo.webservice.action.Rule[] doWork() throws Exception
                {
                    return getRulesImpl(reference, ruleFilter);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    private org.alfresco.repo.webservice.action.Rule[] getRulesImpl(Reference reference, RuleFilter ruleFilter)
        throws RemoteException, ActionFault
    {
        // Get the node reference
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        
        // Get the rules associtated with the node reference
        List<Rule> rules = this.ruleService.getRules(nodeRef);
        
        // Filter the results based on the rule filter passed
        // TODO
        
        // Marshal the results
        org.alfresco.repo.webservice.action.Rule[] webServiceRules = new org.alfresco.repo.webservice.action.Rule[rules.size()];
        int index = 0;
        for (Rule rule : rules)
        {
            webServiceRules[index] = convertToWebServiceRule(rule);
            index ++;
        }
        
        
        return webServiceRules;
    }
    
    private org.alfresco.repo.webservice.action.Rule convertToWebServiceRule(Rule rule)
    {        
        Reference owningReference = null;
        NodeRef owningNodeRef = this.ruleService.getOwningNodeRef(rule);
        if (owningNodeRef != null)
        {
            owningReference = Utils.convertToReference(this.nodeService, this.namespaceService, owningNodeRef);
        }
        
        // Create the web service rule object
        org.alfresco.repo.webservice.action.Rule webServiceRule = new org.alfresco.repo.webservice.action.Rule(
                Utils.convertToReference(this.nodeService, this.namespaceService, rule.getNodeRef()),
                owningReference,
                rule.getRuleTypes().toArray(new String[rule.getRuleTypes().size()]),
                rule.getTitle(),
                rule.getDescription(),
                rule.getExecuteAsynchronously(),
                convertToWebServiceAction(rule.getAction()));
        
        return webServiceRule;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#saveRules(org.alfresco.repo.webservice.types.Reference, org.alfresco.repo.webservice.action.Rule[])
     */
    public org.alfresco.repo.webservice.action.Rule[] saveRules(final Reference reference, final org.alfresco.repo.webservice.action.Rule[] webServiceRules)
            throws RemoteException, ActionFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<org.alfresco.repo.webservice.action.Rule[]>()
            {
                public org.alfresco.repo.webservice.action.Rule[] doWork() throws Exception
                {
                    return saveRulesImpl(reference, webServiceRules);
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    private org.alfresco.repo.webservice.action.Rule[] saveRulesImpl(Reference reference, org.alfresco.repo.webservice.action.Rule[] webServiceRules)
        throws RemoteException, ActionFault
    {
        // Get the node reference
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        
        // Create the result array
        org.alfresco.repo.webservice.action.Rule[] results = new org.alfresco.repo.webservice.action.Rule[webServiceRules.length];
        
        int index = 0;
        for (org.alfresco.repo.webservice.action.Rule webServiceRule : webServiceRules)
        {
            // Convert to a server rule object
            Rule rule = convertToRule(webServiceRule);
            
            // Save the rule
            this.ruleService.saveRule(nodeRef, rule);
            
            // Add the updated rule to the results
            results[index] = convertToWebServiceRule(rule);
            index++;
        }
        
        return results;
    }

    /**
     * @see org.alfresco.repo.webservice.action.ActionServiceSoapPort#removeRules(org.alfresco.repo.webservice.types.Reference, org.alfresco.repo.webservice.action.Rule[])
     */
    public void removeRules(final Reference reference, final org.alfresco.repo.webservice.action.Rule[] webServiceRules)
            throws RemoteException, ActionFault
    {
        try
        {
            TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    removeRulesImpl(reference, webServiceRules);
                    return null;
                }
            });
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * 
     * @param reference
     * @param webServiceRules
     * @throws RemoteException
     * @throws ActionFault
     */
    public void removeRulesImpl(Reference reference, org.alfresco.repo.webservice.action.Rule[] webServiceRules)
        throws RemoteException, ActionFault
    {
        // Get the node reference
        NodeRef nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        
        if (webServiceRules == null)
        {
            // Remove all the actions
            this.ruleService.removeAllRules(nodeRef);
        }
        else
        {
            for (org.alfresco.repo.webservice.action.Rule webServiceRule : webServiceRules)
            {
                Rule rule = convertToRule(webServiceRule);
                this.ruleService.removeRule(nodeRef, rule);
            }
        }

    }
    
    /**
     * 
     * @param webServiceRule
     * @return
     */
    private Rule convertToRule(org.alfresco.repo.webservice.action.Rule webServiceRule)
    {        
        NodeRef ruleNodeRef = null;
        if (webServiceRule.getRuleReference() != null)
        {
            ruleNodeRef = Utils.convertToNodeRef(
                    webServiceRule.getRuleReference(), 
                    this.nodeService, 
                    this.searchService, 
                    this.namespaceService);
        }
        
        // Get the rule type name
        String[] ruleTypes = webServiceRule.getRuleTypes();
        
        // Create the rule
        Rule rule = new Rule();
        List<String> ruleTypesList = new ArrayList<String>(ruleTypes.length);
        for (String ruleType : ruleTypes)
        {
            ruleTypesList.add(ruleType);
        }
        rule.setRuleTypes(ruleTypesList);
        rule.setNodeRef(ruleNodeRef);    
        
        // Set some of the rules details
        rule.setTitle(webServiceRule.getTitle());
        rule.setDescription(webServiceRule.getDescription());
        rule.setExecuteAsynchronously(webServiceRule.isExecuteAsynchronously());
        
        // Set the action
        Action action = convertToAction(webServiceRule.getAction());
        rule.setAction(action);
        
        return rule;
    }
}
