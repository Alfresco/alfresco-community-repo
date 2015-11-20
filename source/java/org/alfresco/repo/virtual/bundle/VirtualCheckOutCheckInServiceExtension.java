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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.coci.traitextender.CheckOutCheckInServiceExtension;
import org.alfresco.repo.coci.traitextender.CheckOutCheckInServiceTrait;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualCheckOutCheckInServiceExtension extends
            SpringBeanExtension<CheckOutCheckInServiceExtension, CheckOutCheckInServiceTrait> implements
            CheckOutCheckInServiceExtension
{

    public VirtualCheckOutCheckInServiceExtension()
    {
        super(CheckOutCheckInServiceTrait.class);
    }

    private VirtualStore virtualStore;

    public void setVirtualStore(VirtualStore virtualStore)
    {
        this.virtualStore = virtualStore;
    }

    @Override
    public NodeRef checkout(NodeRef nodeRef, NodeRef destinationParentNodeRef, QName destinationAssocTypeQName,
                QName destinationAssocQName)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialNodeRef = virtualStore.materializeIfPossible(nodeRef);
        NodeRef materialDestination = virtualStore.materializeIfPossible(destinationParentNodeRef);
        NodeRef workingCopy = theTrait.checkout(materialNodeRef,
                                                materialDestination,
                                                destinationAssocTypeQName,
                                                destinationAssocQName);

        if (Reference.isReference(destinationParentNodeRef))
        {
            Reference parentReference = Reference.fromNodeRef(destinationParentNodeRef);
            Reference workingCopyReference = NodeProtocol.newReference(workingCopy,
                                                                       parentReference);
            return workingCopyReference.toNodeRef(workingCopy.getStoreRef());
        }
        else
        {
            return workingCopy;
        }

    }

    @Override
    public NodeRef checkout(NodeRef nodeRef)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialNodeRef = virtualStore.materializeIfPossible(nodeRef);
        NodeRef workingCopy = theTrait.checkout(materialNodeRef);

        return virtualizeOriginalIfNeeded(nodeRef,
                                          workingCopy);
    }

    @Override
    public NodeRef checkin(NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl,
                boolean keepCheckedOut)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = virtualStore.materializeIfPossible(workingCopyNodeRef);
        NodeRef materialOriginalNode = theTrait.checkin(materialWorkingCopy,
                                                        versionProperties,
                                                        contentUrl,
                                                        keepCheckedOut);
        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    @Override
    public NodeRef checkin(NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = virtualStore.materializeIfPossible(workingCopyNodeRef);
        NodeRef materialOriginalNode = theTrait.checkin(materialWorkingCopy,
                                                        versionProperties,
                                                        contentUrl);

        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    private NodeRef virtualizeOriginalIfNeeded(NodeRef workingCopyNodeRef, NodeRef materialOriginalNode)
    {
        if (materialOriginalNode != null && Reference.isReference(workingCopyNodeRef))
        {
            Reference workingCopyReference = Reference.fromNodeRef(workingCopyNodeRef);
            Reference parentReference = workingCopyReference.execute(new GetParentReferenceMethod());
            Reference originalReference = NodeProtocol.newReference(materialOriginalNode,
                                                                    parentReference);
            return originalReference.toNodeRef(materialOriginalNode.getStoreRef());
        }
        else
        {
            return materialOriginalNode;
        }
    }

    @Override
    public NodeRef checkin(NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = virtualStore.materializeIfPossible(workingCopyNodeRef);
        NodeRef materialOriginalNode = theTrait.checkin(materialWorkingCopy,
                                                        versionProperties);

        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    @Override
    public NodeRef cancelCheckout(NodeRef workingCopyNodeRef)
    {
        NodeRef materialOriginalNode = getTrait().cancelCheckout(virtualStore.materializeIfPossible(workingCopyNodeRef));
        
        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    @Override
    public NodeRef getWorkingCopy(NodeRef nodeRef)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = theTrait.getWorkingCopy(virtualStore.materializeIfPossible(nodeRef));

        return virtualizeVersionIfNeeded(nodeRef,
                                         materialWorkingCopy);
    }

    private NodeRef virtualizeVersionIfNeeded(NodeRef originalNodeRef, NodeRef materialVersion)
    {
        if (materialVersion != null && Reference.isReference(originalNodeRef)
                    && !Reference.isReference(materialVersion))
        {
            Reference reference = Reference.fromNodeRef(originalNodeRef);
            Reference parentReference = reference.execute(new GetParentReferenceMethod());
            Reference workingCopyReference = NodeProtocol.newReference(materialVersion,
                                                                       parentReference);
            return workingCopyReference.toNodeRef(materialVersion.getStoreRef());
        }
        else
        {
            return materialVersion;
        }
    }

    @Override
    public NodeRef getCheckedOut(NodeRef nodeRef)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialChekedOut = theTrait.getCheckedOut(virtualStore.materializeIfPossible(nodeRef));
        return virtualizeVersionIfNeeded(nodeRef,
                                         materialChekedOut);
    }

    @Override
    public boolean isWorkingCopy(NodeRef nodeRef)
    {
        return getTrait().isWorkingCopy(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public boolean isCheckedOut(NodeRef nodeRef)
    {
        return getTrait().isCheckedOut(virtualStore.materializeIfPossible(nodeRef));
    }

}
