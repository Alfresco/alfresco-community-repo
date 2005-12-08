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
package org.alfresco.repo.action;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Class containing behaviour for the actions aspect
 * 
 * @author Roy Wetherall
 */
public class ActionsAspect
{
	private Behaviour onAddAspectBehaviour;
	
	private PolicyComponent policyComponent;
	
    private RuleService ruleService;
    
	private NodeService nodeService;
	
	public void setPolicyComponent(PolicyComponent policyComponent)
	{
		this.policyComponent = policyComponent;
	}
	
	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
   
	public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
	public void init()
	{
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
				ActionModel.ASPECT_ACTIONS,
				new JavaBehaviour(this, "onCopyNode"));
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"),
                ActionModel.ASPECT_ACTIONS,
				new JavaBehaviour(this, "onCopyComplete"));
		
		this.onAddAspectBehaviour = new JavaBehaviour(this, "onAddAspect");
		this.policyComponent.bindClassBehaviour(
				QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
                ActionModel.ASPECT_ACTIONS, 
				onAddAspectBehaviour);
	}
    

    
    /**
     * Helper to diable the on add aspect policy behaviour.  Helpful when importing, 
     * copying and other bulk respstorative operations.
     * 
     * TODO will eventually be redundant when policies can be enabled/diabled in the 
     *      policy componenet
     */
    public void disbleOnAddAspect()
    {
        this.onAddAspectBehaviour.disable();
    }
    
    /**
     * Helper to enable the on add aspect policy behaviour.  Helpful when importing, 
     * copying and other bulk respstorative operations.
     * 
     * TODO will eventually be redundant when policies can be enabled/diabled in the 
     *      policy componenet
     */
    public void enableOnAddAspect()
    {
        this.onAddAspectBehaviour.enable();
    }
	
	/**
	 * On add aspect policy behaviour
	 * @param nodeRef
	 * @param aspectTypeQName
	 */
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
        this.ruleService.disableRules(nodeRef);
        try
        {
			this.nodeService.createNode(
                    nodeRef,
			        ActionModel.ASSOC_ACTION_FOLDER,
                    ActionModel.ASSOC_ACTION_FOLDER,
					ContentModel.TYPE_SYSTEM_FOLDER);
        }
        finally
        {
            this.ruleService.enableRules(nodeRef);
        }
	}
	
	public void onCopyNode(
			QName classRef,
			NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
			PolicyScope copyDetails)
	{
		copyDetails.addAspect(ActionModel.ASPECT_ACTIONS);
		
		List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(
                sourceNodeRef,
                RegexQNamePattern.MATCH_ALL,
                ActionModel.ASSOC_ACTION_FOLDER);
		for (ChildAssociationRef assoc : assocs)
		{
			copyDetails.addChildAssociation(classRef, assoc, true);
		}
		
		this.onAddAspectBehaviour.disable();
	}
	
	public void onCopyComplete(
			QName classRef,
			NodeRef sourceNodeRef,
			NodeRef destinationRef,
			Map<NodeRef, NodeRef> copyMap)
	{
		this.onAddAspectBehaviour.enable();
	}
}
