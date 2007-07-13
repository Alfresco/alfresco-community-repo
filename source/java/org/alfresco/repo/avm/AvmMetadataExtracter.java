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
