package org.alfresco.repo.rule.ruletrigger;

import java.util.List;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class SingleNodeRefPolicyRuleTrigger extends RuleTriggerAbstractBase
{
    private static final String ERR_POLICY_NAME_NOT_SET = "Unable to register rule trigger since policy name has not been set.";
    
    private String policyNamespace = NamespaceService.ALFRESCO_URI;
    
    private String policyName;
    
    private boolean triggerParentRules = true;
    
    public void setPolicyNamespace(String policyNamespace)
    {
        this.policyNamespace = policyNamespace;
    }
    
    public void setPolicyName(String policyName)
    {
        this.policyName = policyName;
    }
    
    public void setTriggerParentRules(boolean triggerParentRules)
    {
        this.triggerParentRules = triggerParentRules;
    }
    
    public void registerRuleTrigger()
    {
        if (policyName == null)
        {
            throw new RuleServiceException(ERR_POLICY_NAME_NOT_SET);
        }
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(this.policyNamespace, this.policyName), 
                this, 
                new JavaBehaviour(this, "policyBehaviour"));        
    }

    public void policyBehaviour(NodeRef nodeRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        if (triggerParentRules == true)
        {
            List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
            for (ChildAssociationRef parentAssocRef : parentsAssocRefs)
            {
                triggerRules(parentAssocRef.getParentRef(), nodeRef);
            }
        }
        else
        {
            triggerRules(nodeRef, nodeRef);
        }
    }
}
