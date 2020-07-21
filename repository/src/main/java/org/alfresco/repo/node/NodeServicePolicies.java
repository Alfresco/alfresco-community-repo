/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.policy.AssociationPolicy;
import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Node service policies
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface NodeServicePolicies 
{
    @AlfrescoPublicApi
    public interface BeforeCreateStorePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateStore");
        /**
         * Called before a new node store is created.
         * 
         * @param nodeTypeQName the type of the node that will be used for the root
         * @param storeRef the reference to the store about to be created
         */
        public void beforeCreateStore(QName nodeTypeQName, StoreRef storeRef);
    }
    
    @AlfrescoPublicApi
    public interface OnCreateStorePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateStore");
        /**
         * Called when a new node store has been created.
         * 
         * @param rootNodeRef the reference to the newly created root node
         */
        public void onCreateStore(NodeRef rootNodeRef);
    }
    
    @AlfrescoPublicApi
    public interface BeforeCreateNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateNode");
        /**
         * Called before a new node is created.
         * 
         * @param parentRef         the parent node reference
         * @param assocTypeQName    the association type qualified name
         * @param assocQName        the association qualified name
         * @param nodeTypeQName     the node type qualified name
         */
        public void beforeCreateNode(
                        NodeRef parentRef,
                        QName assocTypeQName,
                        QName assocQName,
                        QName nodeTypeQName);
    }
    
    @AlfrescoPublicApi
    public interface OnCreateNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode");
        /**
         * Called when a new node has been created.
         * 
         * @param childAssocRef  the created child association reference
         */
        public void onCreateNode(ChildAssociationRef childAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface BeforeMoveNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeMoveNode");
        /**
         * Called before a node is moved.
         *
         * @param oldChildAssocRef the child association reference prior to the move
         * @param newParentRef the new parent node reference
         * 
         * @since 4.1
         */
        public void beforeMoveNode(ChildAssociationRef oldChildAssocRef, NodeRef newParentRef);
    }
    
    @AlfrescoPublicApi
    public interface OnMoveNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode");
        /**
         * Called when a node has been moved.
         *
         * @param oldChildAssocRef the child association reference prior to the move
         * @param newChildAssocRef the child association reference after the move
         */
        public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface BeforeUpdateNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeUpdateNode");
        /**
         * Called before a node is updated.  This includes the modification of properties, child and target 
         * associations and the addition of aspects.
         * 
         * @param nodeRef  reference to the node being updated
         */
        public void beforeUpdateNode(NodeRef nodeRef);
    }
    
    @AlfrescoPublicApi 
    public interface OnUpdateNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateNode");
        /**
         * Called after a new node has been created.  This includes the modification of properties and
         * the addition of aspects.
         * 
         * @param nodeRef  reference to the updated node
         */
        public void onUpdateNode(NodeRef nodeRef);
    }
    
    @AlfrescoPublicApi
    public interface OnUpdatePropertiesPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties");
        /**
         * Called after a node's properties have been changed.
         * 
         * @param nodeRef reference to the updated node
         * @param before the node's properties before the change
         * @param after the node's properties after the change 
         */
        public void onUpdateProperties(
                NodeRef nodeRef,
                Map<QName, Serializable> before,
                Map<QName, Serializable> after);
        
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_1 = Arg.START_VALUE;
        static Arg ARG_2 = Arg.END_VALUE;
    }
    
    @AlfrescoPublicApi 
    public interface BeforeDeleteNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode");
        /**
         * Called before a node is deleted.
         * 
         * @param nodeRef   the node reference
         */
        public void beforeDeleteNode(NodeRef nodeRef);
    }
    
    @AlfrescoPublicApi    
    public interface BeforeArchiveNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeArchiveNode");
        /**
         * Called before a node is archived.
         * 
         * @param nodeRef   the node reference
         */
        public void beforeArchiveNode(NodeRef nodeRef);
    }
    
    @AlfrescoPublicApi
    public interface OnDeleteNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode");
        /**
         * Called after a node is deleted.  The reference given is for an association
         * which has been deleted and cannot be used to retrieve node or associaton
         * information from any of the services.
         * 
         * @param childAssocRef     the primary parent-child association of the deleted node
         * @param isNodeArchived    indicates whether the node has been archived rather than purged
         */
        public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived);
    }
    
    @AlfrescoPublicApi     
    public interface BeforeAddAspectPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeAddAspect");
        /**
         * Called before an <b>aspect</b> is added to a node
         * 
         * @param nodeRef the node to which the aspect will be added
         * @param aspectTypeQName the type of the aspect
         */
        public void beforeAddAspect(NodeRef nodeRef, QName aspectTypeQName);
    }
    
    @AlfrescoPublicApi
    public interface OnAddAspectPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect");
        /**
         * Called after an <b>aspect</b> has been added to a node
         * 
         * @param nodeRef the node to which the aspect was added
         * @param aspectTypeQName the type of the aspect
         */
        public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName);
    }
    
    @AlfrescoPublicApi
    public interface BeforeRemoveAspectPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveAspect");
        /**
         * Called before an <b>aspect</b> is removed from a node
         * 
         * @param nodeRef the node from which the aspect will be removed
         * @param aspectTypeQName the type of the aspect
         */
        public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName);
    }
    
    @AlfrescoPublicApi
    public interface OnRemoveAspectPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect");
        /**
         * Called after an <b>aspect</b> has been removed from a node
         * 
         * @param nodeRef the node from which the aspect will be removed
         * @param aspectTypeQName the type of the aspect
         */
        public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName);
    }
    
    @AlfrescoPublicApi
    public interface OnRestoreNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onRestoreNode");
        /**
         * Called after an archived node is restored.
         * 
         * @param childAssocRef  the newly created child association reference
         */
        public void onRestoreNode(ChildAssociationRef childAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface OnCreateChildAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation");
        /**
         * Called after a node child association has been created.
         * 
         * @param childAssocRef         the child association that has been created
         * @param isNewNode             <tt>true</tt> if the node is new or <tt>false</tt> if the node is being linked in
         */
        public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode);
    }
    
    @AlfrescoPublicApi
    public interface BeforeDeleteChildAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteChildAssociation");
        /**
         * Called before a node child association is deleted.
         * 
         * @param childAssocRef the child association to be deleted
         */
        public void beforeDeleteChildAssociation(ChildAssociationRef childAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface OnDeleteChildAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteChildAssociation");
        /**
         * Called after a node child association has been deleted.
         * 
         * @param childAssocRef the child association that has been deleted
         */
        public void onDeleteChildAssociation(ChildAssociationRef childAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface OnCreateAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateAssociation");
        /**
         * Called after a regular node association is created.
         * 
         * @param nodeAssocRef the regular node association that was created
         */
        public void onCreateAssociation(AssociationRef nodeAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface BeforeDeleteAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteAssociation");
        /**
         * Called before a regular node association is deleted.
         * 
         * @param nodeAssocRef the regular node association that will be removed
         */
        public void beforeDeleteAssociation(AssociationRef nodeAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface OnDeleteAssociationPolicy extends AssociationPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteAssociation");
        /**
         * Called after a regular node association is deleted.
         * 
         * @param nodeAssocRef the regular node association that was removed
         */
        public void onDeleteAssociation(AssociationRef nodeAssocRef);
    }
    
    @AlfrescoPublicApi
    public interface BeforeSetNodeTypePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeSetNodeType");
        /**
         * Called before the type of a node is set explicitly.
         * 
         * @param nodeRef the node having its type set.
         * @param oldType the current type of the node.
         * @param newType the type the node will be given.
         */
        public void beforeSetNodeType(NodeRef nodeRef, QName oldType, QName newType);
    }
    
    @AlfrescoPublicApi
    public interface OnSetNodeTypePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onSetNodeType");
        /**
         * Called after the type of a node is set explicitly.
         * 
         * @param nodeRef the node that has had its type set.
         * @param oldType the previous type of the node.
         * @param newType the type the node has been given.
         */
        public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType);
    }
}
