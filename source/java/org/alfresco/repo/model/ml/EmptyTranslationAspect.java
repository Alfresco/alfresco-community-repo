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

package org.alfresco.repo.model.ml;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
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
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION,
                new JavaBehaviour(this, "getCopyCallback"));
        
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
     */
    public void onCopyNode(QName classRef, NodeRef sourceNodeRef, StoreRef destinationStoreRef, boolean copyToNewNode, PolicyScope copyDetails) 
    {
        throw new IllegalStateException("It's impossible to copy an empty translation");
    }

    /** 
      * If a content is added to a <b>cm:mlEmptyTranslation<b>, remove this aspect. 
      */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) 
    {
        if (newContent)
        {
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION);
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
        }
    }
    
    /**
     * Extends the NO-OP copy behaviour to generate an exception if copied.  In other words,
     * the presence of {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION} should prevent
     * a node from being copied; if this is not done by the copy client, it is enforced here. 
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    private static class EmptyTranslationAspectCopyBehaviourCallback extends DoNothingCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new EmptyTranslationAspectCopyBehaviourCallback();
        
        /**
         * @throws          IllegalStateException       always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            throw new IllegalStateException(
                    "Nodes with " + ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION + " may not be copied");
        }
    }
    
    /**
     * @return              Returns {@link EmptyTranslationAspectCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return EmptyTranslationAspectCopyBehaviourCallback.INSTANCE;
    }
}
