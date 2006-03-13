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

import java.util.List;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class OnContentUpdateRuleTrigger extends RuleTriggerAbstractBase 
                                        implements ContentServicePolicies.OnContentUpdatePolicy
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(OnContentUpdateRuleTrigger.class);
    
    /** True trigger on new content, false otherwise */
    private boolean onNewContent = false;
    
    /** True trigger parent rules, false otherwier */
    private boolean triggerParentRules = true;
    
    /**
     * If set to true the trigger will fire on new content, otherwise it will fire on content update
     * 
     * @param onNewContent  indicates whether to fire on content create or update
     */
    public void setOnNewContent(boolean onNewContent)
    {
        this.onNewContent = onNewContent;
    }
    
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
                ContentServicePolicies.ON_CONTENT_UPDATE, 
                this, 
                new JavaBehaviour(this, "onContentUpdate"));
    }

    /**
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (newContent == this.onNewContent)
        {
            if (triggerParentRules == true)
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("OnContentUpdate rule triggered fired for content; nodeId=" + nodeRef.getId() + "; newContent=" + newContent);
                }
                
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

}
