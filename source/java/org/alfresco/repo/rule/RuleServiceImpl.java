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
package org.alfresco.repo.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.cache.NullCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Rule service implementation.
 * <p>
 * This service automatically binds to the transaction flush hooks.  It will
 * therefore participate in any flushes that occur during the transaction as
 * well.
 * 
 * @author Roy Wetherall   
 */
public class RuleServiceImpl
        implements RuleService, RuntimeRuleService,
                NodeServicePolicies.OnCreateChildAssociationPolicy,
                NodeServicePolicies.OnCreateNodePolicy,
                NodeServicePolicies.OnUpdateNodePolicy,
                NodeServicePolicies.OnAddAspectPolicy
{
    /** key against which to store rules pending on the current transaction */
    private static final String KEY_RULES_PENDING = "RuleServiceImpl.PendingRules";
    
    /** key against which to store executed rules on the current transaction */
    private static final String KEY_RULES_EXECUTED = "RuleServiceImpl.ExecutedRules";
    
    /** qname of assoc to rules */
    private String ASSOC_NAME_RULES_PREFIX = "rules";
    private RegexQNamePattern ASSOC_NAME_RULES_REGEX = new RegexQNamePattern(RuleModel.RULE_MODEL_URI, "^" + ASSOC_NAME_RULES_PREFIX + ".*");
    
    private static final Set<QName> IGNORE_PARENT_ASSOC_TYPES = new HashSet<QName>(7);
    static
    {
        IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_MEMBER);
        IGNORE_PARENT_ASSOC_TYPES.add(ContentModel.ASSOC_IN_ZONE);
    }
    
    private static Log logger = LogFactory.getLog(RuleServiceImpl.class); 
    
    private NodeService nodeService;
    private NodeService runtimeNodeService;
    private CopyService copyService;
    private ActionService actionService;
    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private PermissionService permissionService;
    
    /**
     * The action service implementation which we need for some things.
     */
    private RuntimeActionService runtimeActionService;
    
    /**
     * Cache of raw rules (not inherited or interpreted) for a given node
     */
    private SimpleCache<NodeRef, List<Rule>> nodeRulesCache;
       
    /**
     * List of disabled node refs.  The rules associated with these nodes will node be added to the pending list, and
     * therefore not fired.  This list is transient.
     * 
     * TODO: (DH) Make this txn-local
     */
    private Set<NodeRef> disabledNodeRefs = new HashSet<NodeRef>(5);
    
    /**
     * List of disabled rules.  Any rules that appear in this list will not be added to the pending list and therefore
     * not fired.
     */
    private Set<Rule> disabledRules = new HashSet<Rule>(5);
    
    /** List of disables rule types */
    private Set<String> disabledRuleTypes = new HashSet<String>(3);

    /**
     * All the rule type currently registered
     */
    private Map<String, RuleType> ruleTypes = new HashMap<String, RuleType>();

    /**
     * The rule transaction listener
     */
    private TransactionListener ruleTransactionListener = new RuleTransactionListener(this);   
    
    /**
     * Indicates whether the rules are disabled for the current thread
     */
    private ThreadLocal<Boolean> rulesDisabled = new ThreadLocal<Boolean>();
    
    /**
     * Global flag that indicates whether the 
     */
    private boolean globalRulesDisabled = false;
    
    /**
     * Set the permission-safe node service 
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the direct node service 
     */
    public void setRuntimeNodeService(NodeService runtimeNodeService)
    {
        this.runtimeNodeService = runtimeNodeService;
    }

    /**
     * Set the service for locating copied nodes' originals
     */
    public void setCopyService(CopyService copyService)
    {
        this.copyService = copyService;
    }

    /**
     * Set the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the runtime action service
     */
    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }
    
    /**
     * Set the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set the policy component to listen for various events
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set the cache to hold node's individual rules.  This cache <b>must not be shared</b>
     * across transactions.
     * 
     * @param nodeRulesCache        a cache of raw rules contained on a node
     * 
     * @see NullCache
     */
    public void setNodeRulesCache(SimpleCache<NodeRef, List<Rule>> nodeRulesCache)
    {
        this.nodeRulesCache = nodeRulesCache;
    }

    /**
     * Set the global rules disabled flag
     * 
     * @param rulesDisabled     true to disable allr ules, false otherwise
     */
    public void setRulesDisabled(boolean rulesDisabled)
    {
        this.globalRulesDisabled = rulesDisabled;
    }
    
    /**
     * Registers to listen for events of interest.  For instance, the creation or deletion of a rule folder
     * will affect the caching of rules.
     */
    public void init()
    {
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                RuleModel.ASPECT_RULES,
                RuleModel.ASSOC_RULE_FOLDER,
                new JavaBehaviour(this, "onCreateChildAssociation"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                RuleModel.ASPECT_RULES,
                new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                RuleModel.ASPECT_RULES,
                new JavaBehaviour(this, "onUpdateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                RuleModel.TYPE_RULE,
                new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                RuleModel.TYPE_RULE,
                new JavaBehaviour(this, "onUpdateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ActionModel.TYPE_ACTION_BASE,
                new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                ActionModel.TYPE_ACTION_BASE,
                new JavaBehaviour(this, "onUpdateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,
                ActionModel.TYPE_ACTION_PARAMETER,
                new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdateNodePolicy.QNAME,
                ActionModel.TYPE_ACTION_PARAMETER,
                new JavaBehaviour(this, "onUpdateNode"));
    }

    /**
     * Cache invalidation
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        nodeRulesCache.clear();
    }

    /**
     * Cache invalidation
     */
    public void onUpdateNode(NodeRef nodeRef)
    {
        nodeRulesCache.clear();
    }

    /**
     * Cache invalidation
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        nodeRulesCache.clear();
    }

    /**
     * Cache invalidation
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        nodeRulesCache.clear();
    }
    
    protected NodeRef getSavedRuleFolderRef(NodeRef nodeRef)
    {
        NodeRef result = null;
        ChildAssociationRef assoc = getSavedRuleFolderAssoc(nodeRef);
        if (assoc != null)
        {
            result = assoc.getChildRef();
        }
        return result;
    }

    /**
     * Gets the saved rule folder reference
     * 
     * @param nodeRef    the node reference
     * @return            the node reference
     */
    public ChildAssociationRef getSavedRuleFolderAssoc(NodeRef nodeRef)
    {
        ChildAssociationRef result = null;
        
        List<ChildAssociationRef> assocs = this.runtimeNodeService.getChildAssocs(
                nodeRef,
                RuleModel.ASSOC_RULE_FOLDER,
                RuleModel.ASSOC_RULE_FOLDER);
        if (assocs.size() > 1)
        {
            throw new ActionServiceException("There is more than one rule folder, which is invalid.");
        }
        else if (assocs.size() == 1)
        {
            result = assocs.get(0);
        }
        
        return result;
    }
    
    @Override
    public List<RuleType> getRuleTypes()
    {
        return new ArrayList<RuleType>(this.ruleTypes.values());
    }
    
    @Override
    public RuleType getRuleType(String name)
    {
        return this.ruleTypes.get(name);
    }    
    
    @Override
    public void enableRules()
    {
        this.rulesDisabled.remove();        
    }

    @Override
    public void disableRules()
    {
        this.rulesDisabled.set(Boolean.TRUE);
    }
    
    @Override
    public boolean isEnabled()
    {
        return (this.globalRulesDisabled == false && this.rulesDisabled.get() == null);
    }
    
    @Override
    public boolean rulesEnabled(NodeRef nodeRef)
    {
        return (this.disabledNodeRefs.contains(nodeRef) == false);
    }

    @Override
    public void disableRules(NodeRef nodeRef)
    {
        // Add the node to the set of disabled nodes
        this.disabledNodeRefs.add(nodeRef);
    }

    @Override
    public void enableRules(NodeRef nodeRef)
    {
        // Remove the node from the set of disabled nodes
        this.disabledNodeRefs.remove(nodeRef);
    }
    
    @Override
    public void disableRule(Rule rule)
    {
        this.disabledRules.add(rule);
    }
    
    @Override
    public void enableRule(Rule rule)
    {
        this.disabledRules.remove(rule);
    }
    
    @Override
    public void disableRuleType(String ruleType)
    {
    	disabledRuleTypes.add(ruleType);
    }

    @Override
    public void enableRuleType(String ruleType)
    {
    	disabledRuleTypes.remove(ruleType);
    }
    
    @Override
    public boolean isRuleTypeEnabled(String ruleType)
    {
    	boolean result = true;
    	if (disabledRuleTypes.contains(ruleType) == true)
    	{
    		result = false;    		
    	}
    	return result;
    }
    
    @Override
    public boolean hasRules(NodeRef nodeRef)
    {
        return getRules(nodeRef).size() != 0;
    } 

    @Override
    public List<Rule> getRules(NodeRef nodeRef)
    {
        return getRules(nodeRef, true, null);
    }

    @Override
    public List<Rule> getRules(NodeRef nodeRef, boolean includeInherited)
    {
        return getRules(nodeRef, includeInherited, null);
    }
    
    @Override
    public List<Rule> getRules(final NodeRef nodeRef, final boolean includeInherited, final String ruleTypeName)
    {
        //Run from system user: https://issues.alfresco.com/jira/browse/ALF-607
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<Rule>>()
        {

            public List<Rule> doWork() throws Exception
            {
                List<Rule> rules = new ArrayList<Rule>();

                if (!runtimeNodeService.exists(nodeRef) || !checkNodeType(nodeRef))
                {
                    // Node has gone or is not the correct type
                    return rules;
                }
                if (includeInherited == true && runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_IGNORE_INHERITED_RULES) == false)
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
            
                // Get the node's own rules and add them to the list
                List<Rule> nodeRules = getRulesForNode(nodeRef);
                for (Rule rule : nodeRules)
                {                   
                    if ((rules.contains(rule) == false) && (ruleTypeName == null || rule.getRuleTypes().contains(ruleTypeName) == true))
                    {
                        rules.add(rule);                        
                    }
                }
        
                return rules;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    private List<Rule> getRulesForNode(NodeRef nodeRef)
    {
            // Extra check of CONSUMER permission was added to rule selection,
            // to prevent Access Denied Exception due to the bug:
            // https://issues.alfresco.com/browse/ETWOTWO-438
            
        if (!runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) ||
            permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED)
        {
            // Doesn't have the aspect or the user doesn't have access
            return Collections.emptyList();
        }
        List<Rule> nodeRules = nodeRulesCache.get(nodeRef);
        if (nodeRules != null)
        {
            // We have already processed this node
            return nodeRules;
        }
        // Not in the cache, so go and get the rules
        nodeRules = new ArrayList<Rule>();
        NodeRef ruleFolder = getSavedRuleFolderRef(nodeRef);
        if (ruleFolder != null)
        {
            // Get the rules for this node
            List<ChildAssociationRef> ruleChildAssocRefs = 
                this.runtimeNodeService.getChildAssocs(ruleFolder, RegexQNamePattern.MATCH_ALL, ASSOC_NAME_RULES_REGEX);
            for (ChildAssociationRef ruleChildAssocRef : ruleChildAssocRefs)
            {
                // Create the rule and add to the list
                NodeRef ruleNodeRef = ruleChildAssocRef.getChildRef();
                Rule rule = getRule(ruleNodeRef);
                nodeRules.add(rule);
            }
        }
        // Store this in the cache for later re-use
        nodeRulesCache.put(nodeRef, nodeRules);
        // Done
        return nodeRules;
    }
        
    @Override
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
     * @param nodeRef            the nodeRef
     * @param ruleTypeName        the rule type (null if all applicable)
     * @return                    a list of inherited rules (empty if none)
     */
    private List<Rule> getInheritedRules(NodeRef nodeRef, String ruleTypeName, Set<NodeRef> visitedNodeRefs)
    {
        List<Rule> inheritedRules = new ArrayList<Rule>();
        
        if (this.runtimeNodeService.hasAspect(nodeRef, RuleModel.ASPECT_IGNORE_INHERITED_RULES) == false)
        {        
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
                    // We are not interested in following potentially massive person group membership trees!
                    if (IGNORE_PARENT_ASSOC_TYPES.contains(parent.getTypeQName()))
                    {
                        continue;
                    }

                    // Add the inherited rule first
                    for (Rule rule : getInheritedRules(parent.getParentRef(), ruleTypeName, visitedNodeRefs))
                    {
                        // Ensure that we don't get any rule duplication (don't use a set cos we want to preserve order)
                        if (allInheritedRules.contains(rule) == false)
                        {
                            allInheritedRules.add(rule);
                        }
                    }
                    
                    List<Rule> rules = getRules(parent.getParentRef(), false);
                    for (Rule rule : rules)
                    {
                        // Add is we hanvn't already added and it should be applied to the children
                        if (rule.isAppliedToChildren() == true && allInheritedRules.contains(rule) == false)
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
                        if (rule.getRuleTypes().contains(ruleTypeName) == true)
                        {
                            inheritedRules.add(rule);
                        }
                    }
                }
            }
        }
        
        return inheritedRules;
    }

    /**
     * Create the rule object from the rule node reference
     * 
     * @param ruleNodeRef    the rule node reference
     * @return                the rule
     */
    @SuppressWarnings("unchecked")
    public Rule getRule(NodeRef ruleNodeRef)
    {
        // Get the rule properties
        Map<QName, Serializable> props = this.runtimeNodeService.getProperties(ruleNodeRef);
        
        // Create the rule
        Rule rule = new Rule(ruleNodeRef);
        
        // Set the title and description
        String title = DefaultTypeConverter.INSTANCE.convert(String.class, props.get(ContentModel.PROP_TITLE));
        String description = DefaultTypeConverter.INSTANCE.convert(String.class, props.get(ContentModel.PROP_DESCRIPTION));
        rule.setTitle(title);
        rule.setDescription(description);
        
        // Set the rule types
        rule.setRuleTypes((List<String>)props.get(RuleModel.PROP_RULE_TYPE));
        
        // Set the applied to children value
        boolean isAppliedToChildren = false;
        Boolean value = (Boolean)props.get(RuleModel.PROP_APPLY_TO_CHILDREN);
        if (value != null)
        {
            isAppliedToChildren = value.booleanValue();
        }
        rule.applyToChildren(isAppliedToChildren);
        
        // Set the execute asynchronously value
        boolean executeAsync = false;
        Boolean value2 = (Boolean)props.get(RuleModel.PROP_EXECUTE_ASYNC);
        if (value2 != null)
        {
            executeAsync = value2.booleanValue();
        }
        rule.setExecuteAsynchronously(executeAsync);
        
        // Set the disabled value
        boolean ruleDisabled = false;
        Boolean value3 = (Boolean)props.get(RuleModel.PROP_DISABLED);
        if (value3 != null)
        {
            ruleDisabled = value3.booleanValue();
        }
        rule.setRuleDisabled(ruleDisabled);
        
        // Get the action node reference
        List<ChildAssociationRef> actions = this.nodeService.getChildAssocs(ruleNodeRef, RuleModel.ASSOC_ACTION, RuleModel.ASSOC_ACTION);
        if (actions.size() == 0)
        {
            throw new RuleServiceException("Rule exists without a specified action");
        }
        else if (actions.size() > 1)
        {
            throw new RuleServiceException("Rule exists with more than one specified action");
        }
        NodeRef actionNodeRef = actions.get(0).getChildRef();
        
        // Here we need to create the action from the action node reference
        Action action = runtimeActionService.createAction(actionNodeRef);
        rule.setAction(action);
        
        return rule;
    }

    @Override
    public void saveRule(NodeRef nodeRef, Rule rule)
    {
        checkForLinkedRules(nodeRef);
        
        if (this.permissionService.hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED)
        {        
            disableRules();
            try
            {
                if (this.nodeService.exists(nodeRef) == false)
                {
                    throw new RuleServiceException("The node does not exist.");
                }
        
                NodeRef ruleNodeRef = rule.getNodeRef();
                if (ruleNodeRef == null)
                {
                    if (this.nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == false)
                    {
                        // Add the actionable aspect
                        this.nodeService.addAspect(nodeRef, RuleModel.ASPECT_RULES, null);
                    }
        
                    // Create the action node
                    ruleNodeRef = this.nodeService.createNode(
                            getSavedRuleFolderRef(nodeRef),
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(RuleModel.RULE_MODEL_URI, ASSOC_NAME_RULES_PREFIX + GUID.generate()),
                            RuleModel.TYPE_RULE).getChildRef();
                    
                    // Set the rule node reference and the owning node reference
                    rule.setNodeRef(ruleNodeRef);
                }
                
                // Update the properties of the rule
                this.nodeService.setProperty(ruleNodeRef, ContentModel.PROP_TITLE, rule.getTitle());
                this.nodeService.setProperty(ruleNodeRef, ContentModel.PROP_DESCRIPTION, rule.getDescription());
                this.nodeService.setProperty(ruleNodeRef, RuleModel.PROP_RULE_TYPE, (Serializable)rule.getRuleTypes());
                this.nodeService.setProperty(ruleNodeRef, RuleModel.PROP_APPLY_TO_CHILDREN, rule.isAppliedToChildren());
                this.nodeService.setProperty(ruleNodeRef, RuleModel.PROP_EXECUTE_ASYNC, rule.getExecuteAsynchronously());
                this.nodeService.setProperty(ruleNodeRef, RuleModel.PROP_DISABLED, rule.getRuleDisabled());  
                
                // Save the rule's action
                saveAction(ruleNodeRef, rule);
            }
            finally
            {
                enableRules();
                // Drop the rules from the cache
                nodeRulesCache.remove(nodeRef);
            }
        }
        else
        {
            throw new RuleServiceException("Insufficient permissions to save a rule.");
        }
    }
    
    @Override
    public void saveRule(NodeRef nodeRef, Rule rule, int index)
    {
        saveRule(nodeRef, rule);
        setRulePosition(nodeRef, rule.getNodeRef(), index);
    }
    
    @Override
    public void setRulePosition(NodeRef nodeRef, NodeRef ruleNodeRef, int index)
    {
        NodeRef ruleFolder = getSavedRuleFolderRef(nodeRef);
        if (ruleFolder != null)
        {
            List<ChildAssociationRef> assocs = this.runtimeNodeService.getChildAssocs(ruleFolder, RegexQNamePattern.MATCH_ALL, ASSOC_NAME_RULES_REGEX);
            List<ChildAssociationRef> orderedAssocs = new ArrayList<ChildAssociationRef>(assocs.size());
            ChildAssociationRef movedAssoc = null;
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef childNodeRef = assoc.getChildRef();
                if (childNodeRef.equals(ruleNodeRef) == true)
                {
                    movedAssoc = assoc;
                }
                else
                {
                    orderedAssocs.add(assoc);
                }
            }          
            if (movedAssoc != null)
            {
                orderedAssocs.add(index, movedAssoc);
            }
            
            index = 0;
            for (ChildAssociationRef orderedAssoc : orderedAssocs)
            {
                nodeService.setChildAssociationIndex(orderedAssoc, index);
                index++;
            }
        }
    }
    
    @Override
    public void setRulePosition(NodeRef nodeRef, Rule rule, int index)
    {
        setRulePosition(nodeRef, rule.getNodeRef(), index);
    }
    
    /**
     * Save the action related to the rule.
     * 
     * @param ruleNodeRef   the node reference representing the rule
     * @param rule          the rule
     */
    private void saveAction(NodeRef ruleNodeRef, Rule rule)
    {
        // Get the action definition from the rule
        Action action = rule.getAction();
        if (action == null)
        {
            throw new RuleServiceException("An action must be specified when defining a rule.");
        }
        
        // Get the current action node reference
        NodeRef actionNodeRef = null;
        List<ChildAssociationRef> actions = this.nodeService.getChildAssocs(ruleNodeRef, RuleModel.ASSOC_ACTION, RuleModel.ASSOC_ACTION);
        if (actions.size() == 1)
        {
            // We need to check that the action is the same
            actionNodeRef = actions.get(0).getChildRef();
            if (actionNodeRef.getId().equals(action.getId()) == false)
            {
                // Delete the old action
                this.nodeService.deleteNode(actionNodeRef);
                actionNodeRef = null;
            }
        }
        else if (actions.size() > 1)
        {
            throw new RuleServiceException("The rule has become corrupt.  More than one action is associated with the rule.");
        }
        
        // Create the new action node reference
        if (actionNodeRef == null)
        {
            actionNodeRef = this.runtimeActionService.createActionNodeRef(action, ruleNodeRef, RuleModel.ASSOC_ACTION, RuleModel.ASSOC_ACTION);
        }
        
        // Update the action node
        this.runtimeActionService.saveActionImpl(actionNodeRef, action);
            
    }
    
    @Override
    public void removeRule(NodeRef nodeRef, Rule rule)
    {
        checkForLinkedRules(nodeRef);
        
        if (this.permissionService.hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED)
        {
            if (this.nodeService.exists(nodeRef) == true &&
                this.nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
            {
                disableRules(nodeRef);
                try
                {
                    NodeRef ruleNodeRef = rule.getNodeRef();
                    if (ruleNodeRef != null)
                    {
                        this.nodeService.removeChild(getSavedRuleFolderRef(nodeRef), ruleNodeRef);
                    }
                }
                finally
                {
                    enableRules(nodeRef);
                }
                
                // If this was the last rule on the node, remove the aspect
                if(countRules(nodeRef) == 0)
                {
                    this.nodeService.removeAspect(nodeRef, RuleModel.ASPECT_RULES);
                }
            }
            // Drop the rules from the cache
            nodeRulesCache.remove(nodeRef);
        }
        else
        {
            throw new RuleServiceException("Insufficient permissions to remove a rule.");
        }
    }    
    
    /**
     * Checks if rules are linked and throws an exception if they are.
     * 
     * @param nodeRef   node reference of rule node
     */
    private void checkForLinkedRules(NodeRef nodeRef)
    {
        if (isLinkedToRuleNode(nodeRef)== true)
        {
            throw new RuleServiceException("Can not edit rules as they are linked to another rule set.");
        }
    }

    @Override
    public void removeAllRules(NodeRef nodeRef)
    {
        checkForLinkedRules(nodeRef);
        
        if (this.permissionService.hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED)
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
                
                // As this was the last rule on the node, remove the aspect
                this.nodeService.removeAspect(nodeRef, RuleModel.ASPECT_RULES);
            }
            // Drop the rules from the cache
            nodeRulesCache.remove(nodeRef);
        }
        else
        {
            throw new RuleServiceException("Insufficient permissions to remove a rule.");
        }
    }
    
    @Override
    public void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule) 
    {
        addRulePendingExecution(actionableNodeRef, actionedUponNodeRef, rule, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeRulePendingExecution(NodeRef actionedUponNodeRef)
    {
        ParameterCheck.mandatory("actionedUponNodeRef", actionedUponNodeRef);
        
        List<PendingRuleData> pendingRules = (List<PendingRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_PENDING);
        if (pendingRules != null)
        {
            boolean listUpdated = false;
            List<PendingRuleData> temp = new ArrayList<PendingRuleData>(pendingRules);
            for (PendingRuleData pendingRuleData : temp)
            {
                if (pendingRuleData.getActionedUponNodeRef().equals(actionedUponNodeRef) == true)
                {
                    // Remove from the pending list
                    pendingRules.remove(pendingRuleData);
                    listUpdated = true;
                }
            }
            
            if (listUpdated == true)
            {
                AlfrescoTransactionSupport.bindResource(KEY_RULES_PENDING, pendingRules);
                AlfrescoTransactionSupport.bindListener(this.ruleTransactionListener);
            }
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void addRulePendingExecution(NodeRef actionableNodeRef, NodeRef actionedUponNodeRef, Rule rule, boolean executeAtEnd) 
    {
        ParameterCheck.mandatory("actionableNodeRef", actionableNodeRef);
        ParameterCheck.mandatory("actionedUponNodeRef", actionedUponNodeRef);
        
        // First check to see if the node has been disabled
        if (this.isEnabled() == true &&
            this.disabledNodeRefs.contains(this.getOwningNodeRef(rule)) == false &&
            this.disabledRules.contains(rule) == false)
        {
            PendingRuleData pendingRuleData = new PendingRuleData(actionableNodeRef, actionedUponNodeRef, rule, executeAtEnd);

            List<PendingRuleData> pendingRules =
                (List<PendingRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_PENDING);
            if (pendingRules == null)
            {
                // bind pending rules to the current transaction
                pendingRules = new ArrayList<PendingRuleData>();
                AlfrescoTransactionSupport.bindResource(KEY_RULES_PENDING, pendingRules);
                // bind the rule transaction listener
                AlfrescoTransactionSupport.bindListener(this.ruleTransactionListener);
                
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Rule '" + rule.getTitle() + "' has been added pending execution to action upon node '" + actionedUponNodeRef.getId() + "'");
                }
            }
            
            // Prevent the same rule being executed more than once in the same transaction    
            if (pendingRules.contains(pendingRuleData) == false)
            {
                pendingRules.add(pendingRuleData);
            }
        }
        else
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("The rule '" + rule.getTitle() + "' or the node '" + this.getOwningNodeRef(rule).getId() + "' has been disabled.");
            }
        }
    }
    
    @Override
    public void executePendingRules() 
    {           
        if (AlfrescoTransactionSupport.getResource(KEY_RULES_EXECUTED) == null)
        {
        	 if (logger.isDebugEnabled() == true)
             {
                 logger.debug("Creating the executed rules list");
             }
            AlfrescoTransactionSupport.bindResource(KEY_RULES_EXECUTED, new HashSet<ExecutedRuleData>());
        }
        else
        {
        	if (logger.isDebugEnabled() == true)
        	{
        		logger.debug("Executed rules list already exists");
        	}
        }
    	
    	List<PendingRuleData> executeAtEndRules = new ArrayList<PendingRuleData>();
        executePendingRulesImpl(executeAtEndRules);
        for (PendingRuleData data : executeAtEndRules)
        {
            executePendingRule(data);
        }
    }     
    
    /**
     * Executes the pending rules, iterating until all pending rules have been executed
     */
    @SuppressWarnings("unchecked")
    private void executePendingRulesImpl(List<PendingRuleData> executeAtEndRules)
    {
        // get the transaction-local rules to execute
        List<PendingRuleData> pendingRules =
                (List<PendingRuleData>) AlfrescoTransactionSupport.getResource(KEY_RULES_PENDING);
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
		
        NodeRef ruleNodeRef = rule.getNodeRef();
        if (!ruleNodeRef.getStoreRef().equals(actionedUponNodeRef.getStoreRef()) && !nodeService.exists(ruleNodeRef))
        {
            NodeRef newRuleNodeRef = new NodeRef(actionedUponNodeRef.getStoreRef(), ruleNodeRef.getId());
            if (nodeService.exists(newRuleNodeRef))
            {
                ruleNodeRef = newRuleNodeRef;
            }
            
        }
        final NodeRef finalRuleNodeRef = ruleNodeRef;
        // update all associations and actions
        rule = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Rule>()
        {
            public Rule doWork() throws Exception
            {
                return getRule(finalRuleNodeRef);
            }
        }, AuthenticationUtil.getSystemUserName());

        if (executedRules == null || canExecuteRule(executedRules, actionedUponNodeRef, rule) == true)
        {
            executeRule(rule, actionedUponNodeRef, executedRules);
        }
    }
    
    @Override
    public void executeRule(Rule rule, NodeRef actionedUponNodeRef, Set<ExecutedRuleData> executedRules)
    {
        // Get the action associated with the rule
        Action action = rule.getAction();
        if (action == null)
        {
            throw new RuleServiceException("Attempting to execute a rule that does not have a rule specified.");
        }
        
        // Evaluate the condition
        if (this.actionService.evaluateAction(action, actionedUponNodeRef) == true)
        {
            if (executedRules != null)
            {
                // Add the rule to the executed rule list
                // (do this before this is executed to prevent rules being added to the pending list) 
                executedRules.add(new ExecutedRuleData(actionedUponNodeRef, rule));
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug(" ... Adding rule (" + rule.getTitle() + ") and nodeRef (" + actionedUponNodeRef.getId() + ") to executed list");
                }
            }
            
            // Execute the rule
            boolean executeAsync = rule.getExecuteAsynchronously();
            // ALF-718: Treats email actions as a special case where they may be performed after the
            // current transaction is committed. This only deals with the bug fix and a more thorough approach
            // (but one with potentially wide ranging consequences) is to replace the boolean executeAsynchronously
            // property on Rules and Actions with an ExecutionTime property - which would
            // be an enumerated type with members SYNCHRONOUSLY, SYNCRHONOUSLY_AFTER_COMMIT and ASYNCHRONOUSLY.
            //
            // NOTE: this code is not at the Action level (i.e. ActionService) since the logic of sending after
            // successful commit works in the context of a Rule but not for the InvitationService.
            if (action.getActionDefinitionName().equals(CompositeActionExecuter.NAME))
            {
                for (Action subAction : ((CompositeAction)action).getActions())
                {
                    if (subAction.getActionDefinitionName().equals(MailActionExecuter.NAME))
                    {
                        subAction.setParameterValue(MailActionExecuter.PARAM_SEND_AFTER_COMMIT, true);
        }
    }
            }
            else if (action.getActionDefinitionName().equals(MailActionExecuter.NAME))
            {
                action.setParameterValue(MailActionExecuter.PARAM_SEND_AFTER_COMMIT, true);
            }
    
            executeAction(action, actionedUponNodeRef, executeAsync);
        }
    }

    private void executeAction(Action action, NodeRef actionedUponNodeRef, boolean executeAsynchronously)
    {
        this.actionService.executeAction(action, actionedUponNodeRef, true, executeAsynchronously);
    }
    
    /**
     * Determines whether the rule can be executed
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
     */
    private boolean checkForCopy(Set<ExecutedRuleData> executedRules, NodeRef actionedUponNodeRef, Rule rule)
    {
        boolean result = true;
        if (this.nodeService.exists(actionedUponNodeRef)
                && this.permissionService.hasPermission(actionedUponNodeRef, PermissionService.READ).equals(AccessStatus.ALLOWED))
        {
            NodeRef copiedFrom = copyService.getOriginal(actionedUponNodeRef);
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" >> Got the copiedFrom nodeRef (" + copiedFrom + ")");
            }
            
            if (copiedFrom != null)
            {
                if (executedRules.contains(new ExecutedRuleData(copiedFrom, rule)) == true)
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
    public class ExecutedRuleData
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

    @Override
    public NodeRef getOwningNodeRef(final Rule rule)
    {
        // Run from system user: https://issues.alfresco.com/jira/browse/ALF-607
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef result = null;
        
                NodeRef ruleNodeRef = rule.getNodeRef();
                if (ruleNodeRef != null)
                {
                    result = getOwningNodeRefRuleImpl(ruleNodeRef);
                }
        
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef getOwningNodeRefRuleImpl(NodeRef ruleNodeRef)
    {
        // Get the system folder parent
        NodeRef systemFolder = this.nodeService.getPrimaryParent(ruleNodeRef).getParentRef();
        
        // Get the owning node ref
        return this.nodeService.getPrimaryParent(systemFolder).getParentRef();
    }

    @Override
    public NodeRef getOwningNodeRef(final Action action)
    {
        // Run from system user: https://issues.alfresco.com/jira/browse/ALF-607
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {

            public NodeRef doWork() throws Exception
            {

                NodeRef result = null;
                NodeRef actionNodeRef = action.getNodeRef();
                if (actionNodeRef != null)
                {
                    result = getOwningNodeRefActionImpl(actionNodeRef);
                }
        
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef getOwningNodeRefActionImpl(NodeRef actionNodeRef)
    {
        NodeRef result = null;
        NodeRef parentNodeRef = this.nodeService.getPrimaryParent(actionNodeRef).getParentRef();
        if (parentNodeRef != null)
        {
            QName parentType = this.nodeService.getType(parentNodeRef);
            if (RuleModel.TYPE_RULE.equals(parentType) == true)
            {
                result = getOwningNodeRefRuleImpl(parentNodeRef);
            }
            else if (ActionModel.TYPE_COMPOSITE_ACTION.equals(parentType) == true) 
            {
                result = getOwningNodeRefActionImpl(parentNodeRef);
            }
        }
        return result;
    }
    
    @Override
    public boolean isLinkedToRuleNode(NodeRef nodeRef)
    {
        return (getLinkedToRuleNode(nodeRef) != null);
    }
    
    @Override
    public NodeRef getLinkedToRuleNode(NodeRef nodeRef)
    {
        NodeRef result = null;
        
        // Check whether the node reference has the rule aspect
        if (nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
        {
            ChildAssociationRef assoc = getSavedRuleFolderAssoc(nodeRef);
            if (assoc.isPrimary() == false)
            {
                result = nodeService.getPrimaryParent(assoc.getChildRef()).getParentRef();
            }
        }
        
        return result;
    }
    
    @Override
    public List<NodeRef> getLinkedFromRuleNodes(NodeRef nodeRef)
    {
        List<NodeRef> result = new ArrayList<NodeRef>();
        
        if (nodeService.hasAspect(nodeRef, RuleModel.ASPECT_RULES) == true)
        {
            ChildAssociationRef assoc = getSavedRuleFolderAssoc(nodeRef);
            if (assoc.isPrimary() == true)
            {
                List<ChildAssociationRef> linkedAssocs = nodeService.getParentAssocs(assoc.getChildRef());
                for (ChildAssociationRef linkAssoc : linkedAssocs)
                {
                    if (linkAssoc.isPrimary() == false)
                    {
                        result.add(linkAssoc.getParentRef());
                    }
                }
            }
        }
        return result;
    }
}
