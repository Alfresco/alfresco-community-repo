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
package org.alfresco.repo.avm;

import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A component that listens for changes to the content 
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class AvmMetadataExtracter
        implements ContentServicePolicies.OnContentUpdatePolicy
{
    /** the component to register the behaviour with */
    private PolicyComponent policyComponent;
    /** the action that will do the work */
    private ContentMetadataExtracter extracterAction;
    
    /**
     * @param policyComponent used for registrations
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param extracterAction       the action that will perform the actual extraction
     */
    public void setExtracterAction(ContentMetadataExtracter extracterAction)
    {
        this.extracterAction = extracterAction;
    }

    /**
     * Registers the policy behaviour methods
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onContentUpdate"),
                this,
                new JavaBehaviour(this, "onContentUpdate"));   
    }

    /**
     * When the content changes, the metadata is extracted.
     */
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        /*
         * The use of the action here is a supreme hack.  The code within the action is
         * what we are after and it would be a shame to duplicate it.  Also, it is the
         * intention that rules will eventually work against AVM, making this class
         * obsolete.
         */
        
        // Ignore everything non-AVM
        if (!nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
        {
            return;
        }
        
        // We extract the content whether it is new or not
        extracterAction.executeImpl(null, nodeRef);
    }
}
