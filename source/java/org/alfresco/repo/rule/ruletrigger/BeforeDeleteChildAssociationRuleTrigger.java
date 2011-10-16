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

import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A rule trigger for the creation of <b>secondary child associations</b>.
 * <p>
 * Policy names supported are:
 * <ul>
 *   <li>{@linkplain NodeServicePolicies.BeforeDeleteChildAssociationPolicy}</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
public class BeforeDeleteChildAssociationRuleTrigger
        extends RuleTriggerAbstractBase
        implements NodeServicePolicies.BeforeDeleteChildAssociationPolicy

{
	/**
     * The logger
     */
    private static Log logger = LogFactory.getLog(BeforeDeleteChildAssociationRuleTrigger.class);
    
    private static final String POLICY = "beforeDeleteChildAssociation";
	
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
			this.policyComponent.bindClassBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY), 
					this, 
					new JavaBehaviour(this, POLICY));
		}
		else
		{
			this.policyComponent.bindAssociationBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY), 
					this, 
					new JavaBehaviour(this, POLICY));
		}
	}

    public void beforeDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }

        NodeRef childNodeRef = childAssocRef.getChildRef();

        // Avoid renamed nodes
        Set<NodeRef> renamedNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_RENAMED_NODES);
        if (renamedNodeRefSet.contains(childNodeRef))
        {
            return;
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Single child assoc trigger (policy = " + POLICY + ") fired for parent node " + childAssocRef.getParentRef() + " and child node " + childAssocRef.getChildRef());
        }
        
        triggerRules(childAssocRef.getParentRef(), childNodeRef);
    }
}
