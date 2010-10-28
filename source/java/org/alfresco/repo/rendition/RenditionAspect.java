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
package org.alfresco.repo.rendition;

import java.util.List;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rendition aspect behaviour bean.
 * <p/>
 * When any rendition node is deleted, its parent association back to the source
 * node must be predeleted also. Otherwise a child-association remains from the
 * source node to the rendition node in the archive store.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class RenditionAspect implements NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** logger */
    private static final Log log = LogFactory.getLog(RenditionAspect.class);

    /** Services */
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    
    /**
     * Set the policy component
     * 
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    /**
     * Initialise method
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), 
                RenditionModel.ASPECT_RENDITION, 
                new JavaBehaviour(this, "beforeDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (!nodeService.exists(nodeRef))
        {
            return;
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Predeleting rendition assoc to " + nodeRef);
        }
        
        List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
        
        // There should in fact only be one parent of type rn:rendition to a rendition node.
        final int parentCount = parents.size();
        if (parents.size() > 1 && log.isDebugEnabled())
        {
            log.debug("Unexpectedly found " + parentCount + " source nodes. Removing all parent assocs.");
        }
        for (ChildAssociationRef chAssRef : parents)
        {
            // Initially only for non-primary child-associations
            if (chAssRef.isPrimary() == false)
            {
                nodeService.removeChildAssociation(chAssRef);
            }
        }
    }
}
