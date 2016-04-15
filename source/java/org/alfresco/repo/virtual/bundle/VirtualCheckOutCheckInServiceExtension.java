
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

    private VirtualStore smartStore;

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public NodeRef checkout(NodeRef nodeRef, NodeRef destinationParentNodeRef, QName destinationAssocTypeQName,
                QName destinationAssocQName)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialNodeRef = smartStore.materializeIfPossible(nodeRef);
        NodeRef materialDestination = smartStore.materializeIfPossible(destinationParentNodeRef);
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
        NodeRef materialNodeRef = smartStore.materializeIfPossible(nodeRef);
        NodeRef workingCopy = theTrait.checkout(materialNodeRef);

        return virtualizeOriginalIfNeeded(nodeRef,
                                          workingCopy);
    }

    @Override
    public NodeRef checkin(NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl,
                boolean keepCheckedOut)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = smartStore.materializeIfPossible(workingCopyNodeRef);
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
        NodeRef materialWorkingCopy = smartStore.materializeIfPossible(workingCopyNodeRef);
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
        NodeRef materialWorkingCopy = smartStore.materializeIfPossible(workingCopyNodeRef);
        NodeRef materialOriginalNode = theTrait.checkin(materialWorkingCopy,
                                                        versionProperties);

        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    @Override
    public NodeRef cancelCheckout(NodeRef workingCopyNodeRef)
    {
        NodeRef materialOriginalNode = getTrait().cancelCheckout(smartStore.materializeIfPossible(workingCopyNodeRef));
        
        return virtualizeOriginalIfNeeded(workingCopyNodeRef,
                                          materialOriginalNode);
    }

    @Override
    public NodeRef getWorkingCopy(NodeRef nodeRef)
    {
        CheckOutCheckInServiceTrait theTrait = getTrait();
        NodeRef materialWorkingCopy = theTrait.getWorkingCopy(smartStore.materializeIfPossible(nodeRef));

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
        NodeRef materialChekedOut = theTrait.getCheckedOut(smartStore.materializeIfPossible(nodeRef));
        return virtualizeVersionIfNeeded(nodeRef,
                                         materialChekedOut);
    }

    @Override
    public boolean isWorkingCopy(NodeRef nodeRef)
    {
        return getTrait().isWorkingCopy(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public boolean isCheckedOut(NodeRef nodeRef)
    {
        return getTrait().isCheckedOut(smartStore.materializeIfPossible(nodeRef));
    }

}
