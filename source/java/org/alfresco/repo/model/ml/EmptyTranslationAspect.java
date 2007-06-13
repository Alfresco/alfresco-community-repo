/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.model.ml;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class containing behaviour for the multilingual empty translation aspect.
 * An empty translation is a document's properties translation. This kind of node
 * doesn't have a content.  
 *  
 * {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION ml empty document aspect}
 *
 * @author Yannick Pignot
 */
public class EmptyTranslationAspect implements 
        CopyServicePolicies.OnCopyNodePolicy,
        ContentServicePolicies.OnContentUpdatePolicy
{
    
    //     Dependencies
    private PolicyComponent policyComponent;
    
    private NodeService nodeService;
    
    
    /**
     * Initialise the Multilingual Empty Translation Aspect
     * <p>
     * Ensures that the {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION ml empty document aspect}
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"),
                ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION, 
                new JavaBehaviour(this, "onCopyNode"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"), 
                ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION, 
                new JavaBehaviour(this, "onContentUpdate"));
    }
    
    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    

    /**
     * @param nodeService the Node Service to set
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }   

    /**
     * Copy a <b>cm:mlEmptyTranslation<b> is not permit.
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#onCopyNode(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.StoreRef, boolean, org.alfresco.repo.policy.PolicyScope)
     */
    public void onCopyNode(QName classRef, NodeRef sourceNodeRef, StoreRef destinationStoreRef, boolean copyToNewNode, PolicyScope copyDetails) 
    {
        throw new IllegalStateException("It's impossible to copy an empty translation");
    }

    /** 
      * If a content is added to a <b>cm:mlEmptyTranslation<b>, remove this aspect. 
      * 
      * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
      */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) 
    {
        if (newContent)
        {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
        }
    }
}
