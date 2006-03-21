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
package org.alfresco.repo.node.index;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.Indexer;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Handles the node policy callbacks to ensure that the node hierarchy is properly
 * indexed.
 * 
 * @author Derek Hulley
 */
public class NodeIndexer
        implements NodeServicePolicies.BeforeCreateStorePolicy,
                   NodeServicePolicies.OnCreateNodePolicy,
                   NodeServicePolicies.OnUpdateNodePolicy,
                   NodeServicePolicies.OnDeleteNodePolicy,
                   NodeServicePolicies.OnCreateChildAssociationPolicy,
                   NodeServicePolicies.OnDeleteChildAssociationPolicy
{
    /** the component to register the behaviour with */
    private PolicyComponent policyComponent;
    /** the component to index the node hierarchy */
    private Indexer indexer;
    
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

    /**
     * Registers the policy behaviour methods
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateStore"),
                ContentModel.TYPE_STOREROOT,
                new JavaBehaviour(this, "beforeCreateStore"));   
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

    public void beforeCreateStore(QName nodeTypeQName, StoreRef storeRef)
    {
        // indexer can perform some cleanup here, if required
    }

    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        indexer.createNode(childAssocRef);
    }

    public void onUpdateNode(NodeRef nodeRef)
    {
        indexer.updateNode(nodeRef);
    }

    public void onDeleteNode(ChildAssociationRef childAssocRef)
    {
        indexer.deleteNode(childAssocRef);
    }

    public void onCreateChildAssociation(ChildAssociationRef childAssocRef)
    {
        indexer.createChildRelationship(childAssocRef);
    }

    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        indexer.deleteChildRelationship(childAssocRef);
    }
}
