package org.alfresco.repo.rule.ruletrigger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SingleAssocRefPolicyRuleTrigger extends RuleTriggerAbstractBase
{
    private static Log logger = LogFactory.getLog(OnPropertyUpdateRuleTrigger.class);
    
    private String policyNamespace = NamespaceService.ALFRESCO_URI;
    private String policyName;
    private Set<QName> excludedAssocTypes = Collections.emptySet();
    
    public void setPolicyNamespace(String policyNamespace)
    {
        this.policyNamespace = policyNamespace;
    }
    
    public void setPolicyName(String policyName)
    {
        this.policyName = policyName;
    }
    
    public void setExcludedAssociationTypes(Set<QName> assocTypes) {
        this.excludedAssocTypes = assocTypes;
    }
    
    /**
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        PropertyCheck.mandatory(this, "policyNamespace", policyNamespace);
        PropertyCheck.mandatory(this, "policyName", policyName);
        
        this.policyComponent.bindAssociationBehaviour(
                QName.createQName(this.policyNamespace, this.policyName),
                this,
                new JavaBehaviour(this, "policyBehaviour"));
    }
    
    public void policyBehaviour(AssociationRef assocRef)
    {
        final QName assocTypeQName = assocRef.getTypeQName();
        if ( !excludedAssocTypes.contains(assocTypeQName))
        {
            NodeRef nodeRef = assocRef.getSourceRef();
            
            if (nodeService.exists(nodeRef))
            {
                List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef parentAssocRef : parentsAssocRefs)
                {
                    triggerRules(parentAssocRef.getParentRef(), nodeRef);
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug(
                                "OnUpdateAssoc rule triggered (parent); " +
                                        "nodeRef=" + parentAssocRef.getParentRef());
                    }
                }
            }
        }
    } 
}
