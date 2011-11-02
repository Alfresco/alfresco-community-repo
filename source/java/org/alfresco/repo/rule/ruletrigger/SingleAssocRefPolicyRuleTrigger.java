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
package org.alfresco.repo.rule.ruletrigger;

import java.util.List;

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
	
	public void setPolicyNamespace(String policyNamespace)
	{
		this.policyNamespace = policyNamespace;
	}
	
	public void setPolicyName(String policyName)
	{
		this.policyName = policyName;
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
        NodeRef nodeRef = assocRef.getSourceRef();
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
