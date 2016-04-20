package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Special rule trigger to be invoked when the node has been restored from the trashcan (recycle bin).
 * 
 * @author arsenyko
 */
public class RestoreNodeRuleTrigger extends RuleTriggerAbstractBase implements NodeServicePolicies.OnRestoreNodePolicy
{
    private static Log logger = LogFactory.getLog(RestoreNodeRuleTrigger.class);

    private static final String POLICY = "onRestoreNode";

    public void onRestoreNode(ChildAssociationRef childAssocRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Restore node rule trigger fired for parent node " + 
                    this.nodeService.getType(childAssocRef.getParentRef()).toString() + " " + childAssocRef.getParentRef() + 
                    " and child node " +
                    this.nodeService.getType(childAssocRef.getChildRef()).toString() + " " + childAssocRef.getChildRef());
        }
        triggerRules(childAssocRef.getParentRef(), childAssocRef.getChildRef());
    }

    /**
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, POLICY), this, new JavaBehaviour(this, POLICY));
    }

}
