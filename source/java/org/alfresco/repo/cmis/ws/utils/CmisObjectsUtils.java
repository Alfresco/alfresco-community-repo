/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws.utils;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.cmis.dictionary.CMISDictionaryService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.EnumObjectType;
import org.alfresco.repo.cmis.ws.InvalidArgumentException;
import org.alfresco.repo.cmis.ws.ObjectNotFoundException;
import org.alfresco.repo.cmis.ws.OperationNotSupportedException;
import org.alfresco.repo.cmis.ws.utils.DescendantsQueueManager.DescendantElement;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Dmitry Velichkevich
 */
public class CmisObjectsUtils
{
    private static final int NODE_REFERENCE_WITH_SUFFIX_DELIMETERS_COUNT = 5;

    public static final String NODE_REFERENCE_ID_DELIMETER = "/";

    private static final String DOUBLE_NODE_REFERENCE_ID_DELIMETER = NODE_REFERENCE_ID_DELIMETER + NODE_REFERENCE_ID_DELIMETER;

    private static final List<QName> DOCUMENT_AND_FOLDER_TYPES;
    static
    {
        DOCUMENT_AND_FOLDER_TYPES = new LinkedList<QName>();
        DOCUMENT_AND_FOLDER_TYPES.add(ContentModel.TYPE_CONTENT);
        DOCUMENT_AND_FOLDER_TYPES.add(ContentModel.TYPE_FOLDER);
    }

    private CheckOutCheckInService checkOutCheckInService;
    private CMISDictionaryService cmisDictionaryService;
    private FileFolderService fileFolderService;
    private AuthorityService authorityService;
    private NodeService nodeService;
    private LockService lockService;
    private CMISMapping cmisMapping;

    private Throwable lastOperationException;

    public IdentifierConversionResults getIdentifierInstance(String identifier, AlfrescoObjectType expectedType) throws InvalidArgumentException
    {

        if (!(identifier instanceof String))
        {
            throw new InvalidArgumentException("Invalid Object Identifier was specified");
        }

        IdentifierConversionResults result;
        AlfrescoObjectType actualObjectType;

        if (isRelationship(identifier))
        {
            result = createAssociationIdentifierResult(identifier);

            actualObjectType = AlfrescoObjectType.RELATIONSHIP_OBJECT;
        }
        else
        {
            NodeRef nodeReference = receiveNodeReferenceOfExistenceObject(cutNodeVersionIfNecessary(identifier, identifier.split(NODE_REFERENCE_ID_DELIMETER), 1));

            result = createNodeReferenceIdentifierResult(nodeReference);

            actualObjectType = determineActualObjectType(expectedType, this.nodeService.getType(nodeReference));
        }

        if ((expectedType == AlfrescoObjectType.ANY_OBJECT) || (actualObjectType == expectedType))
        {
            return result;
        }

        throw new InvalidArgumentException("Unexpected object type of the specified Object Identifier");
    }

    public void deleteFolder(NodeRef folderNodeReference, boolean continueOnFailure, boolean totalDeletion, List<String> resultList) throws OperationNotSupportedException
    {

        DescendantsQueueManager queueManager = new DescendantsQueueManager(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, null, folderNodeReference));

        do
        {
            DescendantElement currentElement = queueManager.receiveNextElement();

            if (!this.nodeService.exists(currentElement.getNodesAssociation().getChildRef()))
            {
                continue;
            }

            UnlinkOperationStatus unlinkingStatus = unlinkObject(currentElement.getNodesAssociation().getChildRef(), currentElement.getNodesAssociation().getParentRef(),
                    totalDeletion);

            if (!unlinkingStatus.isObjectUnlinked())
            {
                processNotUnlinkedObjectResults(currentElement, unlinkingStatus, queueManager, resultList, continueOnFailure);
            }
        } while (!queueManager.isDepleted() && (continueOnFailure || resultList.isEmpty()));
    }

    public boolean deleteObject(NodeRef objectNodeReference)
    {

        return isObjectLockIsNotATrouble(objectNodeReference) && performNodeDeletion(objectNodeReference);
    }

    public boolean removeObject(NodeRef objectNodeReference, NodeRef folderNodeReference)
    {

        if (isChildOfThisFolder(objectNodeReference, folderNodeReference))
        {
            try
            {
                this.nodeService.removeChild(folderNodeReference, objectNodeReference);
            }
            catch (Throwable e)
            {
                this.lastOperationException = e;

                return false;
            }

            return true;
        }

        return false;
    }

    public boolean addObjectToFolder(NodeRef objectNodeRef, NodeRef parentFolderNodeRef)
    {

        try
        {
            this.nodeService.addChild(parentFolderNodeRef, objectNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName
                    .createValidLocalName((String) this.nodeService.getProperty(objectNodeRef, ContentModel.PROP_NAME))));

            return true;
        }
        catch (Throwable e)
        {
            this.lastOperationException = e;

            return false;
        }
    }

    public boolean isFolder(NodeRef folderNodeRef)
    {

        return (folderNodeRef != null) && this.cmisMapping.isValidCmisFolder(this.cmisMapping.getCmisType(this.nodeService.getType(folderNodeRef)));
    }

    public boolean isDocument(NodeRef documentNodeRef)
    {

        return (documentNodeRef != null) && this.cmisMapping.isValidCmisDocument(this.cmisMapping.getCmisType(this.nodeService.getType(documentNodeRef)));
    }

    public boolean isRelationship(String identifier)
    {

        try
        {
            new AssociationRef(identifier);

            return true;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public boolean isPolicy(NodeRef policyNodeRef)
    {

        // TODO: Policy

        return false;
    }

    public EnumObjectType determineObjectType(String identifier)
    {

        if (isRelationship(identifier))
        {
            return EnumObjectType.RELATIONSHIP;
        }

        NodeRef objectNodeReference = new NodeRef(identifier);

        if (isFolder(objectNodeReference))
        {
            return EnumObjectType.FOLDER;
        }

        if (isDocument(objectNodeReference))
        {
            return EnumObjectType.DOCUMENT;
        }

        return EnumObjectType.POLICY;
    }

    public boolean isChildOfThisFolder(NodeRef objectNodeReference, NodeRef folderNodeReference)
    {

        NodeRef searchedObjectNodeReference = this.fileFolderService.searchSimple(folderNodeReference, (String) this.nodeService.getProperty(objectNodeReference,
                ContentModel.PROP_NAME));

        return (searchedObjectNodeReference != null) && searchedObjectNodeReference.equals(objectNodeReference);
    }

    public boolean isPrimaryObjectParent(NodeRef folderNodeReference, NodeRef objectNodeReference)
    {

        NodeRef searchedParentObject = this.nodeService.getPrimaryParent(objectNodeReference).getParentRef();

        return (searchedParentObject != null) && searchedParentObject.equals(folderNodeReference);
    }

    public boolean isWorkingCopy(NodeRef objectIdentifier)
    {

        return nodeService.hasAspect(objectIdentifier, ContentModel.ASPECT_WORKING_COPY);
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {

        this.cmisDictionaryService = cmisDictionaryService;

        this.cmisMapping = this.cmisDictionaryService.getCMISMapping();
    }

    public void setNodeService(NodeService nodeService)
    {

        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {

        this.fileFolderService = fileFolderService;
    }

    public void setLockService(LockService lockService)
    {

        this.lockService = lockService;
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {

        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {

        this.authorityService = authorityService;
    }

    public Throwable getLastOperationException()
    {

        return lastOperationException;
    }

    private boolean performNodeDeletion(NodeRef objectNodeReference)
    {

        if (this.nodeService.hasAspect(objectNodeReference, ContentModel.ASPECT_WORKING_COPY))
        {
            this.checkOutCheckInService.cancelCheckout(objectNodeReference);

            return true;
        }

        try
        {
            this.nodeService.deleteNode(objectNodeReference);
        }
        catch (Throwable e)
        {
            return false;
        }

        return true;
    }

    private boolean isObjectLockIsNotATrouble(NodeRef objectNodeReference)
    {

        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();

        return (this.lockService.getLockStatus(objectNodeReference, currentUserName) != LockStatus.LOCKED) || this.authorityService.isAdminAuthority(currentUserName);
    }

    private UnlinkOperationStatus unlinkObject(NodeRef objectNodeReference, NodeRef parentFolderNodeReference, boolean totalDeletion)
    {

        if (isFolder(objectNodeReference))
        {
            List<ChildAssociationRef> children = this.nodeService.getChildAssocs(objectNodeReference);

            return new UnlinkOperationStatus(((children == null) || children.isEmpty()) && deleteObject(objectNodeReference), (children != null) ? (children)
                    : (new LinkedList<ChildAssociationRef>()));
        }

        return new UnlinkOperationStatus((totalDeletion) ? (deleteObject(objectNodeReference))
                : (!isPrimaryObjectParent(parentFolderNodeReference, objectNodeReference) && removeObject(objectNodeReference, parentFolderNodeReference)),
                new LinkedList<ChildAssociationRef>());
    }

    private void processNotUnlinkedObjectResults(DescendantElement currentElement, UnlinkOperationStatus unlinkingStatus, DescendantsQueueManager queueManager,
            List<String> resultList, boolean addAllFailedToDelete)
    {

        if (!unlinkingStatus.getChildren().isEmpty())
        {
            queueManager.addElementToQueueEnd(currentElement);

            queueManager.addChildren(unlinkingStatus.getChildren(), currentElement);

            return;
        }

        resultList.add(currentElement.getNodesAssociation().getChildRef().toString());

        if (addAllFailedToDelete)
        {
            queueManager.removeParents(currentElement, resultList);
        }
    }

    private NodeRef receiveNodeReferenceOfExistenceObject(String clearNodeIdentifier) throws InvalidArgumentException
    {

        if (NodeRef.isNodeRef(clearNodeIdentifier))
        {
            NodeRef result = new NodeRef(clearNodeIdentifier);

            if (this.nodeService.exists(result))
            {
                return result;
            }
        }

        throw new InvalidArgumentException("Invalid Object Identifier was specified: Identifier is incorrect or Object with the specified Identifier is not exists",
                new ObjectNotFoundException());
    }

    private String cutNodeVersionIfNecessary(String identifier, String[] splitedNodeIdentifier, int startIndex)
    {

        String withoutVersionSuffix = identifier;

        if (splitedNodeIdentifier.length == NODE_REFERENCE_WITH_SUFFIX_DELIMETERS_COUNT)
        {
            withoutVersionSuffix = splitedNodeIdentifier[startIndex++ - 1] + DOUBLE_NODE_REFERENCE_ID_DELIMETER + splitedNodeIdentifier[startIndex++] + NODE_REFERENCE_ID_DELIMETER
                    + splitedNodeIdentifier[startIndex];
        }

        return withoutVersionSuffix;
    }

    private AlfrescoObjectType determineActualObjectType(AlfrescoObjectType expectedType, QName objectType)
    {

        return (expectedType != AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT) ? (AlfrescoObjectType.fromValue(objectType.toString())) : ((DOCUMENT_AND_FOLDER_TYPES
                .contains(objectType)) ? (AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT) : (AlfrescoObjectType.ANY_OBJECT));
    }

    private IdentifierConversionResults createAssociationIdentifierResult(final String identifier)
    {

        return new IdentifierConversionResults()
        {
            public AssociationRef getConvertedIdentifier()
            {

                return new AssociationRef(identifier);
            }
        };
    }

    private IdentifierConversionResults createNodeReferenceIdentifierResult(final NodeRef identifier)
    {

        return new IdentifierConversionResults()
        {
            public NodeRef getConvertedIdentifier()
            {

                return identifier;
            }
        };
    }

    public interface IdentifierConversionResults
    {
        public <I> I getConvertedIdentifier();
    }

    private class UnlinkOperationStatus
    {
        private boolean objectUnlinked;
        private List<ChildAssociationRef> children;

        public UnlinkOperationStatus(boolean objectUnlinked, List<ChildAssociationRef> children)
        {

            this.objectUnlinked = objectUnlinked;
            this.children = children;
        }

        public boolean isObjectUnlinked()
        {

            return this.objectUnlinked;
        }

        public List<ChildAssociationRef> getChildren()
        {

            return this.children;
        }

        protected UnlinkOperationStatus()
        {
        }
    }
}
