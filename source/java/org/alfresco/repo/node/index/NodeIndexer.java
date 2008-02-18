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
package org.alfresco.repo.node.index;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Handles the node policy callbacks to ensure that the node hierarchy is properly
 * indexed.
 * 
 * @author Derek Hulley
 */
public class NodeIndexer
        implements NodeServicePolicies.OnCreateNodePolicy,
                   NodeServicePolicies.OnUpdateNodePolicy,
                   NodeServicePolicies.OnDeleteNodePolicy,
                   NodeServicePolicies.OnCreateChildAssociationPolicy,
                   NodeServicePolicies.OnDeleteChildAssociationPolicy
{
    /** the component to register the behaviour with */
    private PolicyComponent policyComponent;
    /** the component to index the node hierarchy */
    private Indexer indexer;
    private TenantService tenantService;
    
    /**
     * @param policyComponent used for registrations
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param indexer the indexer that will be index
     */
    public void setIndexer(Indexer indexer)
    {
        this.indexer = indexer;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Registers the policy behaviour methods
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                this,
                new JavaBehaviour(this, "onCreateNode"));   
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode"),
                this,
                new JavaBehaviour(this, "onUpdateNode"));   
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                this,
                new JavaBehaviour(this, "onDeleteNode"));   
        policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                this,
                new JavaBehaviour(this, "onCreateChildAssociation"));   
        policyComponent.bindAssociationBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteChildAssociation"),
                this,
                new JavaBehaviour(this, "onDeleteChildAssociation"));   
    }

    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        indexer.createNode(tenantService.getName(childAssocRef));
    }

    public void onUpdateNode(NodeRef nodeRef)
    {
        indexer.updateNode(tenantService.getName(nodeRef));
    }

    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isArchivedNode)
    {
        indexer.deleteNode(tenantService.getName(childAssocRef));
    }

    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNew)
    {
        if (!isNew)
        {
            indexer.createChildRelationship(tenantService.getName(childAssocRef));
        }
    }

    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        indexer.deleteChildRelationship(tenantService.getName(childAssocRef));
    }
}
