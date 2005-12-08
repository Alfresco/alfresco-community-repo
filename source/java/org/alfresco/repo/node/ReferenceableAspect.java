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
package org.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Registers and contains the behaviour specific to the
 * {@link org.alfresco.model.ContentModel#ASPECT_REFERENCEABLE referencable aspect}.
 * 
 * @author Derek Hulley
 */
public class ReferenceableAspect implements CopyServicePolicies.OnCopyNodePolicy
{
    // Logger
    private static final Log logger = LogFactory.getLog(ReferenceableAspect.class);

    // Dependencies
    private PolicyComponent policyComponent;

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Initialise the Referencable Aspect
     * <p>
     * Ensures that the {@link ContentModel#ASPECT_REFERENCEABLE referencable aspect}
     * copy behaviour is disabled.
     */
    public void init()
    {
        // disable copy for referencable aspect
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_REFERENCEABLE,
                new JavaBehaviour(this, "onCopyNode"));
    }

    /**
     * Does nothing 
     */
    public void onCopyNode(
            QName classRef,
            NodeRef sourceNodeRef,
            StoreRef destinationStoreRef,
            boolean copyToNewNode,
            PolicyScope copyDetails)
    {
        // don't copy
    }
}
