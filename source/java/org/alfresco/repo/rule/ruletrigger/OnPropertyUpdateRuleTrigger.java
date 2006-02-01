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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * On propety update trigger
 * 
 * @author Roy Wetherall
 */
public class OnPropertyUpdateRuleTrigger extends RuleTriggerAbstractBase 
                                        implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** True trigger parent rules, false otherwier */
    private boolean triggerParentRules = true;
    
    /**
     * Indicates whether the parent rules should be triggered or the rules on the node itself
     * 
     * @param triggerParentRules    true trigger parent rules, false otherwise
     */
    public void setTriggerParentRules(boolean triggerParentRules)
    {
        this.triggerParentRules = triggerParentRules;
    }
    
    /*
     * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
     */
    public void registerRuleTrigger()
    {
        // Bind behaviour
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                this, 
                new JavaBehaviour(this, "onUpdateProperties"));
    }

    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
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
