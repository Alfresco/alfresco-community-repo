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
package org.alfresco.repo.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rule service implementation.
 * <p>
 * This service automatically binds to the transaction flush hooks.  It will
 * therefore participate in any flushes that occur during the transaction as
 * well.
 * 
 * @author Roy Wetherall   
 */
public class RuleServiceImpl implements RuleService, RuntimeRuleService
{
    /** key against which to store rules pending on the current transaction */
    private static final String KEY_RULES_PENDING = "RuleServiceImpl.PendingRules";
    
    /** key against which to store executed rules on the current transaction */
    private static final String KEY_RULES_EXECUTED = "RuleServiceImpl.ExecutedRules";
    
    /** qname of assoc to rules */
    private String ASSOC_NAME_RULES_PREFIX = "rules";
    private RegexQNamePattern ASSOC_NAME_RULES_REGEX = new RegexQNamePattern(RuleModel.RULE_MODEL_URI, "^" + ASSOC_NAME_RULES_PREFIX + ".*");
    
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(RuleServiceImpl.class); 
    
    /**
     * The permission-safe node service
     */
    private NodeService nodeService;
    
    /**
     * The runtime node service (ignores permissions)
     */
    private NodeService runtimeNodeService;
    
    /**
     * The action service
     */
    private ActionService actionService;
    
    /**
     * The search service
     */
    private SearchService searchService;
    
    /**
     * The dictionary service
     */
    private DictionaryService dictionaryService;
    
    /**
     * The action service implementation which we need for some things.
     */
    RuntimeActionService runtimeActionService;
       
    /**
     * List of disabled node refs.  The rules associated with these nodes will node be added to the pending list, and
     * therefore not fired.  This list is transient.
     */
    private Set<NodeRef> disabledNodeRefs = new HashSet<NodeRef>(5);
    
    /**
     * List of disabled rules.  Any rules that appear in this list will not be added to the pending list and therefore
     * not fired.
     */
    private Set<Rule> disabledRules = new HashSet<Rule>(5);

	/**
	 * All the rule type currently registered
	 */
	private Map<String, RuleType> ruleTypes = new HashMap<String, RuleType>();

	/**
	 * The rule transaction listener
	 */
	private TransactionListener ruleTransactionListener = new RuleTransactionListener(this);      
    
    /**
     * Set the permission-safe node service 
     * 
     * @param nodeService   the permission-safe node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the direct node service 
     * 
     * @param nodeService   the node service
     */
    public void setRuntimeNodeService(NodeService runtimeNodeService)
    {
        this.runtimeNodeService = runtimeNodeService;
    }
    
    /**
     * Set the action service
     * 
     * @param actionService  the action service
     */
    public void setActionService(ActionService actionService)
	{
		this.actionService = actionService;
	}
    
    /**
     * Set the runtime action service
     * 
     * @param actionRegistration  the action service
     */
    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }
    
    /**
     * Set the search service
     * 
     * @param searchService   the search service
     */
    public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
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
	 * Gets the saved rule folder reference
	 * 
	 * @param nodeRef	the node reference
	 * @return			the node reference
	 */
	private NodeRef getSavedRuleFolderRef(NodeRef nodeRef)
	{
        NodeRef result = null;
        
		List<ChildAssociationRef> assocs = this.runtimeNodeService.getChildAssocs(
                nodeRef,
                RegexQNamePattern.MATCH_ALL,
                RuleModel.ASSOC_RULE_FOLDER);
		if (assocs.size() > 1)
		{
			throw new ActionServiceException("There is more than one rule folder, which is invalid.");
		}
        else if (assocs.size() == 1)
        {
            result = assocs.get(0).getChildRef();
        }
		
		return result;
	}
    
    /**
     * @see org.alfresco.repo.rule.RuleService#getRuleTypes()
     */
    public List<RuleType> getRuleTypes()
    {
		return new ArrayList<RuleType>(this.ruleTypes.values());
    }
    
    /**
     * @see org.alfresco.repo.rule.RuleService#getRuleType(java.lang.String)
     */
    public RuleType getRuleType(String name)
    {
        return this.ruleTypes.get(name);
    }    
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleService#rulesEnabled(NodeRef)
     */
    public boolean rulesEnabled(NodeRef nodeRef)
    {
        return (this.disabledNodeRefs.contains(nodeRef) == false);
    }

    /**
     * @see org.alfresco.service.cmr.rule.RuleService#disableRules(NodeRef)
     */
    public void disableRules(NodeRef nodeRef)
    {
        // Add the node to the set of disabled nodes
        this.disabledNodeRefs.add(nodeRef);
    }

    /**
     * @see org.alfresco.service.cmr.rule.RuleService#enableRules(NodeRef)
     */
    public void enableRules(NodeRef nodeRef)
    {
        // Remove the node from the set of disabled nodes
        this.disabledNodeRefs.remove(nodeRef);
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleService#disableRule(org.alfresco.service.cmr.rule.Rule)
     */
    public void disableRule(Rule rule)
    {
        this.disabledRules.add(rule);
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleService#enableRule(org.alfresco.service.cmr.rule.Rule)
     */
    public void enableRule(Rule rule)
    {
        this.disabledRules.remove(rule);
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleService#hasRules(org.alfresco.repo.ref.NodeRef)
     */
    public boolean hasRules(NodeRef nodeRef)
    {
    	return getRules(nodeRef).size() != 0;
    } 

    /**
     * @see org.alfresco.repo.rule.RuleService#getRules(org.alfresco.repo.ref.NodeRef)
     */
    public List<Rule> getRules(NodeRef nodeRef)
    {
    	return getRules(nodeRef, true, null);
    }

    /**  
     * @see org.alfresco.repo.rule.RuleService#getRules(org.alfresco.repo.ref.NodeRef, boolean)
     */
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInherited)
    {
    	return getRules(nodeRef, includeInherited, null);
    }
    
    /**
     * @see org.alfresco.repo.rule.RuleService#getRulesByRuleType(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.rule.RuleType)
     */
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInherited, String ruleTypeName)
    {
        List<Rule> rules = new ArrayList<Rule>();
        
        if (this.runtimeNodeService.exists(nodeRef) == true && checkNodeType(nodeRef) == true)
        {
            if (includeInherited == true)
            {
                // Get any inherited rules
                for (Rule rule : getInheritedRules(nodeRef, ruleTypeName, null))
                {
                    // Ensure rules are not duplicated in the list
                    if (rules.contains(rule) == false)
                    {
                        rules.add(rule);
                    }
                }
            }
            
            if (this.runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
            {
                NodeRef ruleFolder = getSavedRuleFolderRef(nodeRef);
                if (ruleFolder != null)
                {
                    List<Rule> allRules = new ArrayList<Rule>();
                    
                    // Get the rules for this node
                    List<ChildAssociationRef> ruleChildAssocRefs = 
                        this.runtimeNodeService.getChildAssocs(ruleFolder, RegexQNamePattern.MATCH_ALL, ASSOC_NAME_RULES_REGEX);
                    for (ChildAssociationRef ruleChildAssocRef : ruleChildAssocRefs)
                    {
                        // Create the rule and add to the list
                        NodeRef ruleNodeRef = ruleChildAssocRef.getChildRef();
                        Rule rule = createRule(nodeRef, ruleNodeRef);
                        allRules.add(rule);
                    }
                    
                    // Build the list of rules that is returned to the client
                    for (Rule rule : allRules)
                    {					
                        if ((rules.contains(rule) == false) &&
                                (ruleTypeName == null || ruleTypeName.equals(rule.getRuleTypeName()) == true))
                        {
                            rules.add(rule);						
                        }
                    }
                }
            }
        }
        
        return rules;
    }
    
    /**
     * @see org.alfresco.service.cmr.rule.RuleService#countRules(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int countRules(NodeRef nodeRef)
    {
        int ruleCount = 0;
        
        if (this.runtimeNodeService.exists(nodeRef) == true && checkNodeType(nodeRef) == true)
        {
            if (this.runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
            {
                NodeRef ruleFolder = getSavedRuleFolderRef(nodeRef);
                if (ruleFolder != null)
                {
                    // Get the rules for this node
                    List<ChildAssociationRef> ruleChildAssocRefs = 
                        this.runtimeNodeService.getChildAssocs(ruleFolder, RegexQNamePattern.MATCH_ALL, ASSOC_NAME_RULES_REGEX);
                    
                    ruleCount = ruleChildAssocRefs.size();
                }
            }
        }
        
        return ruleCount;
    }

    /**
     * Looks at the type of the node and indicates whether the node can have rules associated with it
     * 
     * @param nodeRef   the node reference
     * @return          true if the node can have rule associated with it (inherited or otherwise)
     */
    private boolean checkNodeType(NodeRef nodeRef)
    {
        boolean result = true;
        
        QName nodeType = this.runtimeNodeService.getType(nodeRef);
        if (this.dictionaryService.isSubClass(nodeType, ContentModel.TYPE_SYSTEM_FOLDER) == true ||
            this.dictionaryService.isSubClass(nodeType, ActionModel.TYPE_ACTION) == true ||
            this.dictionaryService.isSubClass(nodeType, ActionModel.TYPE_ACTION_CONDITION) == true ||
            this.dictionaryService.isSubClass(nodeType, ActionModel.TYPE_ACTION_PARAMETER) == true)
        {
            result = false;
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("A node of type " + nodeType.toString() + " was checked and can not have rules.");
            }
        }
        
        return result;
    }
	
    /**
     * Gets the inherited rules for a given node reference
     * 
     * @param nodeRef			the nodeRef
     * @param ruleTypeName		the rule type (null if all applicable)
     * @return					a list of inherited rules (empty if none)
     */
	private List<Rule> getInheritedRules(NodeRef nodeRef, String ruleTypeName, Set<NodeRef> visitedNodeRefs)
	{
		List<Rule> inheritedRules = new ArrayList<Rule>();
		
		// Create the visited nodes set if it has not already been created
		if (visitedNodeRefs == null)
		{
			visitedNodeRefs = new HashSet<NodeRef>();
		}
		
		// This check prevents stack over flow when we have a cyclic node graph
		if (visitedNodeRefs.contains(nodeRef) == false)
		{
			visitedNodeRefs.add(nodeRef);
			
			List<Rule> allInheritedRules = new ArrayList<Rule>();
			List<ChildAssociationRef> parents = this.runtimeNodeService.getParentAssocs(nodeRef);
			for (ChildAssociationRef parent : parents)
			{
				List<Rule> rules = getRules(parent.getParentRef(), false);
				for (Rule rule : rules)
				{
					// Add is we hanvn't already added and it should be applied to the children
					if (rule.isAppliedToChildren() == true && allInheritedRules.contains(rule) == false)
					{
						allInheritedRules.add(rule);
					}
				}
				
				for (Rule rule : getInheritedRules(parent.getParentRef(), ruleTypeName, visitedNodeRefs))
				{
					// Ensure that we don't get any rule duplication (don't use a set cos we want to preserve order)
					if (allInheritedRules.contains(rule) == false)
					{
						allInheritedRules.add(rule);
					}
				}
			}
			
			if (ruleTypeName == null)
			{
				inheritedRules = allInheritedRules;
			}
			else
			{
				// Filter the rule list by rule type
				for (Rule rule : allInheritedRules)
				{
					if (rule.getRuleTypeName().equals(ruleTypeName) == true)
					{
						inheritedRules.add(rule);
					}
				}
			}
		}
		
		return inheritedRules;
	}

	/**
	 * @see org.alfresco.repo.rule.RuleService#getRule(String) 
	 */
	public Rule getRule(NodeRef nodeRef, String ruleId) 
	{
		Rule rule = null;
		
		if (this.runtimeNodeService.exists(nodeRef) == true)
		{
			NodeRef ruleNodeRef = getRuleNodeRefFromId(nodeRef, ruleId);
			if (ruleNodeRef != null)
			{
				rule = createRule(nodeRef, ruleNodeRef);
			}
		}
		
		return rule;
	}    
	
	/**
	 * Gets the rule node ref from the action id
	 * 
	 * @param nodeRef	the node reference
	 * @param actionId	the rule id
	 * @return			the rule node reference
	 */
	private NodeRef getRuleNodeRefFromId(NodeRef nodeRef, String ruleId)
	{
		NodeRef result = null;
		if (this.runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
		{
            NodeRef ruleFolder = getSavedRuleFolderRef(nodeRef);
            if (ruleFolder != null)
            {
    			DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver();
    			namespacePrefixResolver.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
    			
    			List<NodeRef> nodeRefs = searchService.selectNodes(
                        ruleFolder, 
    					"*[@sys:" + ContentModel.PROP_NODE_UUID.getLocalName() + "='" + ruleId + "']",
    					null,
    					namespacePrefixResolver,
    					false);
    			if (nodeRefs.size() != 0)
    			{
    				result = nodeRefs.get(0);
    			}
            }
		}
		
		return result;
	}

	/**
	 * Create the rule object from the rule node reference
	 * 
	 * @param ruleNodeRef	the rule node reference
	 * @return				the rule
	 */
    private Rule createRule(NodeRef owningNodeRef, NodeRef ruleNodeRef)
	{
    	// Get the rule properties
		Map<QName, Serializable> props = this.runtimeNodeService.getProperties(ruleNodeRef);
		
    	// Create the rule
    	String ruleTypeName = (String)props.get(RuleModel.PROP_RULE_TYPE);    	
		Rule rule = new RuleImpl(ruleNodeRef.getId(), ruleTypeName, owningNodeRef);
		
		// Set the other rule properties
        boolean isAppliedToChildren = false;
        Boolean value = (Boolean)props.get(RuleModel.PROP_APPLY_TO_CHILDREN);
        if (value != null)
        {
            isAppliedToChildren = value.booleanValue();
        }
		rule.applyToChildren(isAppliedToChildren);
		
		// Populate the composite action details
		runtimeActionService.populateCompositeAction(ruleNodeRef, rule);
		
		return rule;
	}

	/**
     * @see org.alfresco.repo.rule.RuleService#createRule(org.alfresco.repo.rule.RuleType)
     */
    public Rule createRule(String ruleTypeName)
    {
        // Create the new rule, giving it a unique rule id
        String id = GUID.generate();
        return new RuleImpl(id, ruleTypeName, null);
    }

    /**
     * @see org.alfresco.repo.rule.RuleService#saveRule(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.rule.Rule)
     */
    public void saveRule(NodeRef nodeRef, Rule rule)
    {
    	if (this.nodeService.exists(nodeRef) == false)
    	{
    		throw new RuleServiceException("The node does not exist.");
    	}

    	NodeRef ruleNodeRef = getRuleNodeRefFromId(nodeRef, rule.getId());
    	if (ruleNodeRef == null)
    	{
    		if (this.nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == false)
    		{
    			// Add the actionable aspect
    			this.nodeService.addAspect(nodeRef, RuleModel.ASPECT_RULES, null);
    		}
    		
    		Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
    		props.put(RuleModel.PROP_RULE_TYPE, rule.getRuleTypeName());
			props.put(ActionModel.PROP_DEFINITION_NAME, rule.getActionDefinitionName());
			props.put(ContentModel.PROP_NODE_UUID, rule.getId());
			
			// Create the action node
			ruleNodeRef = this.nodeService.createNode(
					getSavedRuleFolderRef(nodeRef),
					ContentModel.ASSOC_CONTAINS,
					QName.createQName(RuleModel.RULE_MODEL_URI, ASSOC_NAME_RULES_PREFIX + GUID.generate()),
					RuleModel.TYPE_RULE,
					props).getChildRef();
			
			// Update the created details
			((RuleImpl)rule).setCreator((String)this.nodeService.getProperty(ruleNodeRef, ContentModel.PROP_CREATOR));
			((RuleImpl)rule).setCreatedDate((Date)this.nodeService.getProperty(ruleNodeRef, ContentModel.PROP_CREATED));
    	}
        
        // Update the properties of the rule
        this.nodeService.setProperty(ruleNodeRef, RuleModel.PROP_APPLY_TO_CHILDREN, rule.isAppliedToChildren());
    	
    	// Save the remainder of the rule as a composite action
    	runtimeActionService.saveActionImpl(nodeRef, ruleNodeRef, rule);
    }
    
    /**
     * @see org.alfresco.repo.rule.RuleService#removeRule(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.rule.RuleImpl)
     */
    public void removeRule(NodeRef nodeRef, Rule rule)
    {
    	if (this.nodeService.exists(nodeRef) == true &&
    		this.nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
    	{
            disableRules(nodeRef);
            try
            {
        		NodeRef ruleNodeRef = getRuleNodeRefFromId(nodeRef, rule.getId());
        		if (ruleNodeRef != null)
        		{
        			this.nodeService.removeChild(getSavedRuleFolderRef(nodeRef), ruleNodeRef);
        		}
            }
            finally
            {
                enableRules(nodeRef);
            }
    	}
    }	
    
    /**
     * @see org.alfresco.repo.rule.RuleService#removeAllRules(NodeRef)
     */
    public void removeAllRules(NodeRef nodeRef)
    {
    	if (this.nodeService.exists(nodeRef) == true && 
        	this.nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
    	{
            NodeRef folder = getSavedRuleFolderRef(nodeRef);
            if (folder != null)
            {
        		List<ChildAssociationRef> ruleChildAssocs = this.nodeService.getChildAssocs(
                                                                            folder, 
                                                                            RegexQNamePattern.MATCH_ALL, ASSOC_NAME_RULES_REGEX);
        		for (ChildAssociationRef ruleChildAssoc : ruleChildAssocs)
    			{
    				this.nodeService.removeChild(folder, ruleChildAssoc.getChildRef());
    			}
            }
    	}
    }
	
    @SuppressWarnings("unchecked")
    public void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule) 
    {
        addRulePendingExecution(actionableNodeRef, actionedUponNodeRef, rule, false);
    }

	@SuppressWarnings("unchecked")
    public void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule, boolean executeAtEnd) 
	{
        // First check to see if the node has been disabled
        if (this.disabledNodeRefs.contains(rule.getOwningNodeRef()) == false &&
            this.disabledRules.contains(rule) == false)
        {
    		PendingRuleData pendingRuleData = new PendingRuleData(actionableNodeRef, actionedUponNodeRef, rule, executeAtEnd);

            Set<PendingRuleData> pendingRules =
                (Set<PendingRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_PENDING);
			if (pendingRules == null)
			{
                // bind pending rules to the current transaction
				pendingRules = new HashSet<PendingRuleData>();
                AlfrescoTransactionSupport.bindResource(KEY_RULES_PENDING, pendingRules);
                // bind the rule transaction listener
                AlfrescoTransactionSupport.bindListener(this.ruleTransactionListener);
                
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Rule '" + rule.getTitle() + "' has been added pending execution to action upon node '" + actionedUponNodeRef.getId() + "'");
                }
			}
			
            // Prevent hte same rule being executed more than one in the same transaction                
			pendingRules.add(pendingRuleData);		
        }
        else
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("The rule '" + rule.getTitle() + "' or the node '" + rule.getOwningNodeRef().getId() + "' has been disabled.");
            }
        }
	}
	
	
	
	/**
	 * @see org.alfresco.repo.rule.RuleService#executePendingRules()
	 */
	public void executePendingRules() 
	{
		if (logger.isDebugEnabled() == true)
		{
			logger.debug("Creating the executed rules list");
		}
        AlfrescoTransactionSupport.bindResource(KEY_RULES_EXECUTED, new HashSet<ExecutedRuleData>());
        try
        {
            List<PendingRuleData> executeAtEndRules = new ArrayList<PendingRuleData>();
            executePendingRulesImpl(executeAtEndRules);
            for (PendingRuleData data : executeAtEndRules)
            {
                executePendingRule(data);
            }
        }
        finally
        {
            AlfrescoTransactionSupport.unbindResource(KEY_RULES_EXECUTED);
            if (logger.isDebugEnabled() == true)
			{
				logger.debug("Unbinding resource");
			}
        }
	}     
    
    /**
     * Executes the pending rules, iterating until all pending rules have been executed
     */
    @SuppressWarnings("unchecked")
    private void executePendingRulesImpl(List<PendingRuleData> executeAtEndRules)
    {
        // get the transaction-local rules to execute
        Set<PendingRuleData> pendingRules =
                (Set<PendingRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_PENDING);
        // only execute if there are rules present
        if (pendingRules != null && !pendingRules.isEmpty())
        {
            PendingRuleData[] pendingRulesArr = pendingRules.toArray(new PendingRuleData[0]);
            // remove all pending rules from the transaction
            AlfrescoTransactionSupport.unbindResource(KEY_RULES_PENDING);
            // execute each rule
            for (PendingRuleData pendingRule : pendingRulesArr) 
            {
                if (pendingRule.getExecuteAtEnd() == false)
                {
                    executePendingRule(pendingRule);
                }
                else
                {
                    executeAtEndRules.add(pendingRule);
                }
            }
            
            // Run any rules that have been marked as pending during execution
            executePendingRulesImpl(executeAtEndRules);
        }   
    }
	
	/**
	 * Executes a pending rule
	 * 
	 * @param pendingRule	the pending rule data object
	 */
	@SuppressWarnings("unchecked")
    private void executePendingRule(PendingRuleData pendingRule) 
	{
		Set<ExecutedRuleData> executedRules =
               (Set<ExecutedRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_EXECUTED);
    
		NodeRef actionedUponNodeRef = pendingRule.getActionedUponNodeRef();
		Rule rule = pendingRule.getRule();
		
		if (executedRules == null || canExecuteRule(executedRules, actionedUponNodeRef, rule) == true) 
		{
			// Evaluate the condition
		    if (this.actionService.evaluateAction(rule, actionedUponNodeRef) == true)
		    {
	            // Add the rule to the executed rule list
	            // (do this before this is executed to prevent rules being added to the pending list) 
	            executedRules.add(new ExecutedRuleData(actionedUponNodeRef, rule));
	            if (logger.isDebugEnabled() == true)
				{
					logger.debug(" ... Adding rule (" + rule.getTitle() + ") and nodeRef (" + actionedUponNodeRef.getId() + ") to executed list");
				}
	            
				// Execute the rule
	            this.actionService.executeAction(rule, actionedUponNodeRef);
		    }
		}
	}
	
	/**
	 * Determines whether the rule can be executed
	 * 
	 * @param executedRules
	 * @param actionedUponNodeRef
	 * @param rule
	 * @return
	 */
	private boolean canExecuteRule(Set<ExecutedRuleData> executedRules, NodeRef actionedUponNodeRef, Rule rule)
	{
		boolean result = true;
		
		if (logger.isDebugEnabled() == true)
		{
			logger.debug(" >> Current executed items count = " + executedRules.size());
		}
		
		if (executedRules != null)
		{
			if (executedRules.contains(new ExecutedRuleData(actionedUponNodeRef, rule)) == true)
			{
				if (logger.isDebugEnabled() == true)
				{
					logger.debug(" >> Already executed this rule (" + rule.getTitle()+ ") on this nodeRef (" + actionedUponNodeRef.getId() + ")");
				}
				result = false;
			}
			else
			{
				result = checkForCopy(executedRules, actionedUponNodeRef, rule);				
			}
		}
		else
		{
			if (logger.isDebugEnabled() == true)
			{
				logger.debug(" >> Executed this rule (" + rule.getTitle()+ ") on (" + actionedUponNodeRef.getId() + ") executed rule is null");
			}
		}
		
		return result;
	}

	/**
	 * Checks to see if a copy exists in the executed rules list
	 * 
	 * @param executedRules
	 * @param actionedUponNodeRef
	 * @param rule
	 * @return
	 */
	private boolean checkForCopy(Set<ExecutedRuleData> executedRules, NodeRef actionedUponNodeRef, Rule rule)
	{
		boolean result = true;
		if (this.nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_COPIEDFROM) == true)
		{
			if (logger.isDebugEnabled() == true)
			{
				logger.debug(" >> Has the copied from aspect (" + actionedUponNodeRef.getId() + ")");
			}
			NodeRef copiedFrom = (NodeRef)this.nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_COPY_REFERENCE);
			
			if (logger.isDebugEnabled() == true && copiedFrom != null) {logger.debug(" >> Got the copedFrom nodeRef (" + copiedFrom.getId() + ")");};
			
			if (copiedFrom != null && executedRules.contains(new ExecutedRuleData(copiedFrom, rule)) == true)
			{
				if (logger.isDebugEnabled() == true)
				{
					logger.debug(" >> Already executed this rule (" + rule.getTitle()+ ") on this the copied from nodeRef (" + copiedFrom.getId() + ")");
				}
				return false;
			}
			else
			{
				if (logger.isDebugEnabled() == true)
				{
					logger.debug(" >> Executed this rule (" + rule.getTitle()+ ") on (" + actionedUponNodeRef.getId() + ") copiedFrom is not is list");
					logger.debug("  > Checking copy");
				}
				result = checkForCopy(executedRules, copiedFrom, rule);
			}
		}
		else
		{
			if (logger.isDebugEnabled() == true)
			{
				logger.debug(" >> Executed this rule (" + rule.getTitle()+ ") on (" + actionedUponNodeRef.getId() + ") no copied from aspect");
			}
		}
		return result;
	}
    
	/**
	 * Register the rule type
	 * 
	 * @param ruleTypeAdapter  the rule type adapter
	 */
	public void registerRuleType(RuleType ruleType) 
	{
		this.ruleTypes.put(ruleType.getName(), ruleType);
	}    
    
    /**
     * Helper class to contain the information about a rule that is executed
     * 
     * @author Roy Wetherall
     */
    private class ExecutedRuleData
    {

        protected NodeRef actionableNodeRef;
        protected Rule rule;
        
        public ExecutedRuleData(NodeRef actionableNodeRef, Rule rule) 
        {
            this.actionableNodeRef = actionableNodeRef;
            this.rule = rule;
        }

        public NodeRef getActionableNodeRef()
        {
        	return actionableNodeRef;
        }

        public Rule getRule()
        {
        	return rule;
        }
        
        @Override
        public int hashCode() 
        {
            int i = actionableNodeRef.hashCode();
            i = (i*37) + rule.hashCode();
            return i;
        }
        
        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof ExecutedRuleData)
            {
                ExecutedRuleData that = (ExecutedRuleData) obj;
                return (this.actionableNodeRef.equals(that.actionableNodeRef) &&
                        this.rule.equals(that.rule));
            }
            else
            {
                return false;
            }
        }
    }

	/**
	 * Helper class to contain the information about a rule that is pending execution
	 * 
	 * @author Roy Wetherall
	 */
	private class PendingRuleData extends ExecutedRuleData
	{
		private NodeRef actionedUponNodeRef;
        private boolean executeAtEnd = false;
        	
        public PendingRuleData(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule, boolean executeAtEnd) 
        {
            super(actionableNodeRef, rule);
            this.actionedUponNodeRef = actionedUponNodeRef;
            this.executeAtEnd = executeAtEnd;
        }
		
		public NodeRef getActionedUponNodeRef() 
		{
			return actionedUponNodeRef;
		}
        
        public boolean getExecuteAtEnd()
        {
            return this.executeAtEnd;
        }
		
		@Override
		public int hashCode() 
		{
			int i = super.hashCode();
			i = (i*37) + actionedUponNodeRef.hashCode();
			return i;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj)
	        {
	            return true;
	        }
	        if (obj instanceof PendingRuleData)
	        {
				PendingRuleData that = (PendingRuleData) obj;
	            return (this.actionableNodeRef.equals(that.actionableNodeRef) &&
	                    this.actionedUponNodeRef.equals(that.actionedUponNodeRef) &&
	                    this.rule.equals(that.rule));
	        }
	        else
	        {
	            return false;
	        }
		}
	}
}
