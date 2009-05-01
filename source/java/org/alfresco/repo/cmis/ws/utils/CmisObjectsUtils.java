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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisFaultType;
import org.alfresco.repo.cmis.ws.EnumObjectType;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.cmis.ws.utils.DescendantsQueueManager.DescendantElement;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
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
    public static final String NODE_REFERENCE_ID_DELIMETER = "/";
    private static final int NODE_REFERENCE_WITH_SUFFIX_DELIMETERS_COUNT = 5;
    private static final String DOUBLE_NODE_REFERENCE_ID_DELIMETER = NODE_REFERENCE_ID_DELIMETER + NODE_REFERENCE_ID_DELIMETER;

    private static final List<QName> DOCUMENT_AND_FOLDER_TYPES;
    static
    {
        DOCUMENT_AND_FOLDER_TYPES = new LinkedList<QName>();
        DOCUMENT_AND_FOLDER_TYPES.add(ContentModel.TYPE_CONTENT);
        DOCUMENT_AND_FOLDER_TYPES.add(ContentModel.TYPE_FOLDER);
    }

    private static final Map<String, EnumServiceException> CLASS_TO_ENUM_EXCEPTION_MAPPING;
    static
    {
        CLASS_TO_ENUM_EXCEPTION_MAPPING = new HashMap<String, EnumServiceException>();
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(AccessDeniedException.class.getName(), EnumServiceException.PERMISSION_DENIED);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(java.lang.RuntimeException.class.getName(), EnumServiceException.RUNTIME);
        // TODO: insert CLASS_TO_ENUM_EXCEPTION_MAPPING.put(<Concreate_Exception_Type>.class.getName(), EnumServiceException.<Appropriate_Enum_value>);
    }

    private CheckOutCheckInService checkOutCheckInService;
    private CMISDictionaryService cmisDictionaryService;
    private FileFolderService fileFolderService;
    private AuthorityService authorityService;
    private NodeService nodeService;
    private LockService lockService;

    private Throwable lastOperationException;

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
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

    public CmisException createCmisException(String message, EnumServiceException exceptionType)
    {
        return createCmisException(message, exceptionType, null, 0);
    }

    public CmisException createCmisException(String message, Throwable cause)
    {
        EnumServiceException exceptionType = null;

        if (CLASS_TO_ENUM_EXCEPTION_MAPPING.containsKey(cause.getClass().getName()))
        {
            exceptionType = CLASS_TO_ENUM_EXCEPTION_MAPPING.get(cause.getClass().getName());
        }

        exceptionType = (exceptionType == null) ? (EnumServiceException.RUNTIME) : (exceptionType);

        return createCmisException(message, exceptionType, cause, 0);
    }

    public CmisException createCmisException(String message, EnumServiceException exceptionType, Throwable cause)
    {
        return createCmisException(message, exceptionType, cause, 0);
    }

    public CmisException createCmisException(String message, EnumServiceException exceptionType, Throwable cause, int errorCode)
    {
        CmisFaultType fault = new CmisFaultType();
        fault.setMessage(message);
        fault.setType(exceptionType);
        fault.setCode(BigInteger.valueOf(errorCode));

        return new CmisException(message, fault, cause);
    }

    public IdentifierConversionResults getIdentifierInstance(String identifier, AlfrescoObjectType expectedType) throws CmisException
    {
        if (!(identifier instanceof String))
        {
            throw createCmisException("Invalid Object Identifier was specified", EnumServiceException.INVALID_ARGUMENT);
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
            NodeRef nodeReference = safeGetNodeRef(cutNodeVersionIfNecessary(identifier, identifier.split(NODE_REFERENCE_ID_DELIMETER), 1));
            result = createNodeReferenceIdentifierResult(nodeReference);
            actualObjectType = determineActualObjectType(expectedType, this.nodeService.getType(nodeReference));
        }

        if ((expectedType == AlfrescoObjectType.ANY_OBJECT) || (actualObjectType == expectedType))
        {
            return result;
        }

        throw createCmisException(("Unexpected object type of the specified Object Identifier " + identifier), EnumServiceException.INVALID_ARGUMENT);
    }

    public void deleteFolder(NodeRef folderNodeReference, boolean continueOnFailure, boolean totalDeletion, List<String> resultList) throws CmisException
    {
        DescendantsQueueManager queueManager = new DescendantsQueueManager(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, null, null, folderNodeReference));

        do
        {
            DescendantElement currentElement = queueManager.getNextElement();
            if (!nodeService.exists(currentElement.getChildAssoc().getChildRef()))
            {
                continue;
            }

            UnlinkOperationStatus unlinkStatus = unlinkObject(currentElement.getChildAssoc().getChildRef(), currentElement.getChildAssoc().getParentRef(), totalDeletion);
            if (!unlinkStatus.isObjectUnlinked())
            {
                processUnlinkStatus(currentElement, unlinkStatus, queueManager, resultList, continueOnFailure);
            }
        } while (!queueManager.isEmpty() && (continueOnFailure || resultList.isEmpty()));
    }

    private void processUnlinkStatus(DescendantElement currentElement, UnlinkOperationStatus unlinkStatus, DescendantsQueueManager queueManager, List<String> resultList,
            boolean addAllFailedToDelete)
    {
        if (!unlinkStatus.getChildren().isEmpty())
        {
            queueManager.addLast(currentElement);
            queueManager.addFirst(currentElement, unlinkStatus.getChildren());
            return;
        }

        resultList.add(currentElement.getChildAssoc().getChildRef().toString());
        if (addAllFailedToDelete)
        {
            queueManager.removeParents(currentElement, resultList);
        }
    }

    public boolean deleteObject(NodeRef objectNodeReference)
    {
        return canLock(objectNodeReference) && performNodeDeletion(objectNodeReference);
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
            QName name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName((String) nodeService.getProperty(objectNodeRef,
                    ContentModel.PROP_NAME)));
            nodeService.addChild(parentFolderNodeRef, objectNodeRef, ContentModel.ASSOC_CONTAINS, name);
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
        if (folderNodeRef == null)
        {
            return false;
        }
        QName typeQName = nodeService.getType(folderNodeRef);
        CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(typeQName, CMISScope.FOLDER);
        return typeDef != null;
    }

    public boolean isDocument(NodeRef documentNodeRef)
    {
        if (documentNodeRef == null)
        {
            return false;
        }
        QName typeQName = nodeService.getType(documentNodeRef);
        CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(typeQName, CMISScope.DOCUMENT);
        return typeDef != null;
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
        NodeRef searchedObjectNodeReference = fileFolderService.searchSimple(folderNodeReference, (String) nodeService.getProperty(objectNodeReference, ContentModel.PROP_NAME));
        return (searchedObjectNodeReference != null) && searchedObjectNodeReference.equals(objectNodeReference);
    }

    public boolean isPrimaryObjectParent(NodeRef folderNodeReference, NodeRef objectNodeReference)
    {
        NodeRef searchedParentObject = nodeService.getPrimaryParent(objectNodeReference).getParentRef();
        return (searchedParentObject != null) && searchedParentObject.equals(folderNodeReference);
    }

    public boolean isWorkingCopy(NodeRef objectIdentifier)
    {
        return nodeService.hasAspect(objectIdentifier, ContentModel.ASPECT_WORKING_COPY);
    }

    private boolean performNodeDeletion(NodeRef objectNodeReference)
    {
        if (nodeService.hasAspect(objectNodeReference, ContentModel.ASPECT_WORKING_COPY))
        {
            checkOutCheckInService.cancelCheckout(objectNodeReference);
            return true;
        }

        try
        {
            nodeService.deleteNode(objectNodeReference);
        }
        catch (Throwable e)
        {
            return false;
        }
        return true;
    }

    private boolean canLock(NodeRef objectNodeReference)
    {
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        return (this.lockService.getLockStatus(objectNodeReference, currentUserName) != LockStatus.LOCKED) || authorityService.isAdminAuthority(currentUserName);
    }

    private UnlinkOperationStatus unlinkObject(NodeRef objectNodeReference, NodeRef parentFolderNodeReference, boolean totalDeletion)
    {
        if (isFolder(objectNodeReference))
        {
            List<ChildAssociationRef> children = nodeService.getChildAssocs(objectNodeReference);
            boolean objectUnlinked = (children == null || children.isEmpty()) && deleteObject(objectNodeReference);
            return new UnlinkOperationStatus(objectUnlinked, children != null ? children : new LinkedList<ChildAssociationRef>());
        }

        boolean objectUnlinked = false;
        if (totalDeletion)
        {
            objectUnlinked = deleteObject(objectNodeReference);
        }
        else
        {
            objectUnlinked = !isPrimaryObjectParent(parentFolderNodeReference, objectNodeReference) && removeObject(objectNodeReference, parentFolderNodeReference);
        }
        return new UnlinkOperationStatus(objectUnlinked, new LinkedList<ChildAssociationRef>());
    }

    private NodeRef safeGetNodeRef(String nodeIdentifier) throws CmisException
    {
        if (NodeRef.isNodeRef(nodeIdentifier))
        {
            NodeRef result = new NodeRef(nodeIdentifier);
            if (nodeService.exists(result))
            {
                return result;
            }
        }

        throw createCmisException("Invalid Object Identifier was specified: Identifier is incorrect or Object with the specified Identifier does not exist",
                EnumServiceException.OBJECT_NOT_FOUND);
    }

    private String cutNodeVersionIfNecessary(String identifier, String[] splitNodeIdentifier, int startIndex)
    {
        String withoutVersionSuffix = identifier;
        if (splitNodeIdentifier.length == NODE_REFERENCE_WITH_SUFFIX_DELIMETERS_COUNT)
        {
            withoutVersionSuffix = splitNodeIdentifier[startIndex++ - 1] + DOUBLE_NODE_REFERENCE_ID_DELIMETER + splitNodeIdentifier[startIndex++] + NODE_REFERENCE_ID_DELIMETER
                    + splitNodeIdentifier[startIndex];
        }
        return withoutVersionSuffix;
    }

    private AlfrescoObjectType determineActualObjectType(AlfrescoObjectType expectedType, QName objectType)
    {
        CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(objectType);
        if ((expectedType == AlfrescoObjectType.DOCUMENT_OBJECT || expectedType == AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT)
                && typeDef.getTypeId().getScope() == CMISScope.DOCUMENT)
        {
            return expectedType;
        }
        if ((expectedType == AlfrescoObjectType.FOLDER_OBJECT || expectedType == AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT)
                && typeDef.getTypeId().getScope() == CMISScope.FOLDER)
        {
            return expectedType;
        }
        return AlfrescoObjectType.ANY_OBJECT;
    }

    private IdentifierConversionResults createAssociationIdentifierResult(final String identifier)
    {
        return new IdentifierConversionResults()
        {
            @SuppressWarnings("unchecked")
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
            @SuppressWarnings("unchecked")
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

        protected UnlinkOperationStatus()
        {
        }

        public boolean isObjectUnlinked()
        {
            return objectUnlinked;
        }

        public List<ChildAssociationRef> getChildren()
        {
            return this.children;
        }
    }

    public Throwable getLastOperationException()
    {
        return lastOperationException;
    }

}
