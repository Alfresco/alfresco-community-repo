package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A rule trigger for when nodes are moved.
 * 
 * @since 3.4.6
 */
public class OnMoveNodeRuleTrigger extends RuleTriggerAbstractBase implements NodeServicePolicies.OnMoveNodePolicy
{
    private static final String POLICY_NAME = NodeServicePolicies.OnMoveNodePolicy.QNAME.getLocalName();

    private boolean isClassBehaviour = false;

    public void setIsClassBehaviour(boolean isClassBehaviour)
    {
        this.isClassBehaviour = isClassBehaviour;
    }

    /**
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        if (isClassBehaviour == true)
        {
            this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), this, new JavaBehaviour(this, POLICY_NAME));
        }
        else
        {
            this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), this, new JavaBehaviour(this, POLICY_NAME));
        }
    }

    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        // Check that it is not rename operation.
        if (!oldChildAssocRef.getParentRef().equals(newChildAssocRef.getParentRef()))
        {
            triggerChildrenRules(newChildAssocRef, newChildAssocRef);
        }
    }

    private void triggerChildrenRules(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }
        triggerRules(newChildAssocRef.getParentRef(), newChildAssocRef.getChildRef());
        for (ChildAssociationRef ref : nodeService.getChildAssocs(newChildAssocRef.getChildRef()))
        {
            triggerChildrenRules(ref, ref);
        }
    }
}
