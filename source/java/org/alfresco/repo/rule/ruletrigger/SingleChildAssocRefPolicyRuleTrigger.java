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
package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SingleChildAssocRefPolicyRuleTrigger extends RuleTriggerAbstractBase
{
	private static final String ERR_POLICY_NAME_NOT_SET = "Unable to register rule trigger since policy name has not been set.";
	
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(SingleChildAssocRefPolicyRuleTrigger.class);
    
	private String policyNamespace = NamespaceService.ALFRESCO_URI;
	
	private String policyName;
	
	private boolean isClassBehaviour = false;
	
	public void setPolicyNamespace(String policyNamespace)
	{
		this.policyNamespace = policyNamespace;
	}
	
	public void setPolicyName(String policyName)
	{
		this.policyName = policyName;
	}
	
	public void setIsClassBehaviour(boolean isClassBehaviour)
	{
		this.isClassBehaviour = isClassBehaviour;
	}
	
	/**
	 * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
	 */
	public void registerRuleTrigger()
	{
		if (policyName == null)
		{
			throw new RuleServiceException(ERR_POLICY_NAME_NOT_SET);
		}
		
		if (isClassBehaviour == true)
		{
			this.policyComponent.bindClassBehaviour(
					QName.createQName(this.policyNamespace, this.policyName), 
					this, 
					new JavaBehaviour(this, "policyBehaviour"));
		}
		else
		{
			this.policyComponent.bindAssociationBehaviour(
					QName.createQName(this.policyNamespace, this.policyName), 
					this, 
					new JavaBehaviour(this, "policyBehaviour"));
		}
	}

	public void policyBehaviour(ChildAssociationRef childAssocRef)
	{
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Single child assoc trigger (policy = " + this.policyName + ") fired for parent node " + childAssocRef.getParentRef() + " and child node " + childAssocRef.getChildRef());
        }
        
		triggerRules(childAssocRef.getParentRef(), childAssocRef.getChildRef());
	}
}
