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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.HasVersionHistoryEvaluator;
import org.alfresco.repo.action.executer.CreateVersionActionExecuter;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class containing behaviour for the versionable aspect
 * 
 * @author Roy Wetherall
 */
public class VersionableAspect
{
    /**
     * The policy component
     */
	private PolicyComponent policyComponent;
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The rule service
     */
    private RuleService ruleService;
    
    /**
     * The action service
     */
    private ActionService actionService;
    
    /**
     * The rule used to create versions
     */
    private Rule rule;

    /**
     * Auto version behaviour
     */
    private Behaviour autoVersionBehaviour;
	
    /**
     * Set the policy component
     * 
     * @param policyComponent   the policy component
     */
	public void setPolicyComponent(PolicyComponent policyComponent)
	{
		this.policyComponent = policyComponent;
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
     * Set the action service
     * 
     * @param actionService     the action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Initialise the versionable aspect policies
     */
	public void init()
	{
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
				ContentModel.ASPECT_VERSIONABLE, 
                new JavaBehaviour(this, "onAddAspect"));
        autoVersionBehaviour = new JavaBehaviour(this, "onContentUpdate");
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
                ContentModel.ASPECT_VERSIONABLE,
                autoVersionBehaviour);
        
        // Register the copy behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_VERSIONABLE,
                new JavaBehaviour(this, "onCopy"));
        
        // Register the onCreateVersion behavior for the version aspect
        //this.policyComponent.bindClassBehaviour(
        //        QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateVersion"),
        //        ContentModel.ASPECT_VERSIONABLE,
        //        new JavaBehaviour(this, "onCreateVersion"));
	}
    
    /**
     * OnCopy behaviour implementation for the version aspect.
     * <p>
     * Ensures that the propety values of the version aspect are not copied onto
     * the destination node.
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#onCopyNode(QName, NodeRef, StoreRef, boolean, PolicyScope)
     */
    public void onCopy(
            QName sourceClassRef, 
            NodeRef sourceNodeRef, 
            StoreRef destinationStoreRef,
            boolean copyToNewNode,            
            PolicyScope copyDetails)
    {
        // Add the version aspect, but do not copy the version label
        copyDetails.addAspect(ContentModel.ASPECT_VERSIONABLE);
        copyDetails.addProperty(
                ContentModel.ASPECT_VERSIONABLE, 
                ContentModel.PROP_AUTO_VERSION, 
                this.nodeService.getProperty(sourceNodeRef, ContentModel.PROP_AUTO_VERSION));
    }
    
    /**
     * OnCreateVersion behaviour for the version aspect
     * <p>
     * Ensures that the version aspect and it proerties are 'frozen' as part of
     * the versioned state.
     * 
     * @param classRef              the class reference
     * @param versionableNode       the versionable node reference
     * @param versionProperties     the version properties
     * @param nodeDetails           the details of the node to be versioned
     */
    public void onCreateVersion(
            QName classRef,
            NodeRef versionableNode, 
            Map<String, Serializable> versionProperties,
            PolicyScope nodeDetails)
    {
        // Do nothing since we do not what to freeze any of the version 
        // properties
    }
 
	
	/**
	 * On add aspect policy behaviour
     * 
	 * @param nodeRef
	 * @param aspectTypeQName
	 */
	@SuppressWarnings("unchecked")
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
	    if (aspectTypeQName.equals(ContentModel.ASPECT_VERSIONABLE) == true)
        {
	        // Queue create version action
            queueCreateVersionAction(nodeRef);
        }
	}
    
    /**
     * On content update policy bahaviour
     * 
     * @param nodeRef   the node reference
     */
    public void onContentUpdate(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
        {
            // Determine whether the node is auto versionable or not
            boolean autoVersion = false;
            Boolean value = (Boolean)this.nodeService.getProperty(nodeRef, ContentModel.PROP_AUTO_VERSION);
            if (value != null)
            {
                // If the value is not null then 
                autoVersion = value.booleanValue();
            }
            // else this means that the default value has not been set and the versionable aspect was applied pre-1.1
            
            if (autoVersion == true)
            {
                // Queue create version action
                queueCreateVersionAction(nodeRef);
            }
        }
    }
    
    /**
     * Enable the auto version behaviour
     *
     */
    public void enableAutoVersion()
    {
        this.autoVersionBehaviour.enable();
    }
    
    /**
     * Disable the auto version behaviour
     *
     */
    public void disableAutoVersion()
    {
        this.autoVersionBehaviour.disable();
    }
    
    /**
     * Queue create version action
     * 
     * @param nodeRef   the node reference
     */
    private void queueCreateVersionAction(NodeRef nodeRef)
    {
        if (this.rule == null)
        {
            this.rule = this.ruleService.createRule("inbound");
            Action action = this.actionService.createAction(CreateVersionActionExecuter.NAME);
            ActionCondition condition = this.actionService.createActionCondition(HasVersionHistoryEvaluator.NAME);
            condition.setInvertCondition(true);
            // conditions are only evaluated on the parent rule - not the contained actions
            rule.addActionCondition(condition);
            this.rule.addAction(action);
        }
        
        ((RuntimeRuleService)this.ruleService).addRulePendingExecution(nodeRef, nodeRef, this.rule, true);
    }    
}
