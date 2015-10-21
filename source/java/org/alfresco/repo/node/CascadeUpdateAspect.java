/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * @author Andy
 *
 */
public class CascadeUpdateAspect implements OnCreateChildAssociationPolicy, OnDeleteChildAssociationPolicy, OnMoveNodePolicy
{
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private SOLRTrackingComponent solrTrackingComponent;
    private DictionaryService dictionaryService;

  
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
 
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Initialise method
     */
    public void init()
    {
        // need to listen to:
        // invokeOnCreateChildAssociation(newParentAssocRef, false);
        // invokeOnDeleteChildAssociation(oldParentAssocRef);
        // invokeOnMoveNode(oldParentAssocRef, newParentAssocRef); 
        // categories affect paths via membership (not paths beneath nodes that are categories) 
        // - only changing category structure requires a cascade not changing a node's on a categories
        
        this.policyComponent.bindAssociationBehaviour(OnCreateChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onCreateChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindAssociationBehaviour(OnDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onDeleteChildAssociation", Behaviour.NotificationFrequency.EVERY_EVENT));
        this.policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onMoveNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        markCascadeUpdate(oldChildAssocRef.getChildRef());
        markCascadeUpdate(newChildAssocRef.getChildRef());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteChildAssociationPolicy#onDeleteChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        markCascadeUpdate(childAssocRef.getChildRef());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        if(!isNewNode)
        {
            markCascadeUpdate(childAssocRef.getChildRef());
        }
    }
    
    
    private void markCascadeUpdate(NodeRef nodeRef)
    {
        Status status = nodeService.getNodeStatus(nodeRef);
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_CRC, solrTrackingComponent.getCRC(status.getDbId()));
        nodeService.setProperty(status.getNodeRef(), ContentModel.PROP_CASCADE_TX, status.getDbTxnId());   
    }
//    /* (non-Javadoc)
//     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
//     */
//    @Override
//    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
//    {
//        HashSet<QName> combinedPropertyNames = new HashSet<QName>(before.size() + 10);
//        combinedPropertyNames.addAll(before.keySet());
//        combinedPropertyNames.addAll(after.keySet());
//        for(QName propertyQName : combinedPropertyNames)
//        {
//            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
//            if((propDef != null) && (propDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY)))
//            {
//                Serializable beforeValue = before.get(propDef.getName());
//                Serializable afterValue = after.get(propDef.getName());
//                if(false == EqualsHelper.nullSafeEquals(beforeValue, afterValue))
//                {
//                    markCascadeUpdate(nodeRef);
//                }
//            }
//                    
//        }
//        
//    }
}
