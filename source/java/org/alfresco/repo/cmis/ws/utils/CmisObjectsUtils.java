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
import java.util.regex.Pattern;

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisFaultType;
import org.alfresco.repo.cmis.ws.EnumBaseObjectTypeIds;
import org.alfresco.repo.cmis.ws.EnumRelationshipDirection;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.cmis.ws.EnumUnfileObject;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.util.Pair;

/**
 * @author Dmitry Velichkevich
 */
public class CmisObjectsUtils
{
    public static final String NODE_REFERENCE_ID_DELIMETER = "/";

    private static final Pattern VERSION_LABEL_MATCHING_COMPILED_PATTERN = Pattern.compile("([\\p{Graph}])+([\\p{Digit}]*)\\.([\\p{Digit}]*)$");

    private static final String INVALID_OBJECT_IDENTIFIER_MESSAGE = "Invalid Object Identifier was specified: Identifier is incorrect or Object with the specified Identifier does not exist";

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
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(UnsupportedOperationException.class.getName(), EnumServiceException.NOT_SUPPORTED);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(InvalidNodeRefException.class.getName(), EnumServiceException.INVALID_ARGUMENT);
        CLASS_TO_ENUM_EXCEPTION_MAPPING.put(ContentIOException.class.getName(), EnumServiceException.NOT_SUPPORTED);
        // TODO: insert CLASS_TO_ENUM_EXCEPTION_MAPPING.put(<Concreate_Exception_Type>.class.getName(), EnumServiceException.<Appropriate_Enum_value>);
    }

    private CheckOutCheckInService checkOutCheckInService;
    private CMISDictionaryService cmisDictionaryService;
    private FileFolderService fileFolderService;
    private AuthorityService authorityService;
    private VersionService versionService;
    private NodeService nodeService;
    private LockService lockService;

    private Throwable lastException;

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

    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
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

    @SuppressWarnings("unchecked")
    public <IdentifierType> IdentifierType getIdentifierInstance(String identifier, AlfrescoObjectType expectedType) throws CmisException
    {
        if (!(identifier instanceof String))
        {
            throw createCmisException("Invalid Object Identifier was specified", EnumServiceException.INVALID_ARGUMENT);
        }

        IdentifierType result;
        AlfrescoObjectType actualObjectType;

        if (isRelationship(identifier))
        {
            result = (IdentifierType) safeGetAssociationRef(identifier);
            actualObjectType = AlfrescoObjectType.RELATIONSHIP_OBJECT;
        }
        else
        {
            NodeRef nodeReference = safeGetNodeRef(identifier);
            result = (IdentifierType) nodeReference;
            actualObjectType = determineActualObjectType(expectedType, this.nodeService.getType(nodeReference));
        }

        if ((AlfrescoObjectType.ANY_OBJECT == expectedType) || (actualObjectType == expectedType))
        {
            return result;
        }

        throw createCmisException(("Unexpected object type of the specified Object with \"" + identifier + "\" identifier"), EnumServiceException.INVALID_ARGUMENT);
    }

    public List<String> deleteFolder(NodeRef folderNodeReference, boolean continueOnFailure, EnumUnfileObject unfillingStrategy, boolean deleteAllVersions) throws CmisException
    {
        CmisObjectIterator iterator = new CmisObjectIterator(folderNodeReference, unfillingStrategy, continueOnFailure, deleteAllVersions, nodeService, fileFolderService,
                versionService, checkOutCheckInService, this);
        if (iterator.hasNext())
        {
            for (; iterator.hasNext(); iterator.next())
            {
                iterator.remove();
            }
        }
        return iterator.getFailToDelete();
    }

    public boolean deleteObject(NodeRef objectNodeReference)
    {
        if (null == objectNodeReference)
        {
            return false;
        }

        if (versionService.getVersionStoreReference().getIdentifier().equals(objectNodeReference.getStoreRef().getIdentifier()))
        {
            String versionLabel = (String) nodeService.getProperty(objectNodeReference, ContentModel.PROP_VERSION_LABEL);

            if ((null != versionLabel) && !versionLabel.equals(""))
            {
                Version currentVersion = versionService.getCurrentVersion(objectNodeReference);

                if ((null != currentVersion) && nodeService.exists(currentVersion.getVersionedNodeRef()))
                {
                    versionService.deleteVersion(currentVersion.getVersionedNodeRef(), currentVersion);
                    return true;
                }
            }

            return false;
        }

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
                lastException = e;
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
            lastException = e;
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

    public EnumBaseObjectTypeIds determineObjectType(String identifier)
    {
        if (isRelationship(identifier))
        {
            return EnumBaseObjectTypeIds.CMIS_RELATIONSHIP;
        }

        NodeRef objectNodeReference = new NodeRef(identifier);
        if (isFolder(objectNodeReference))
        {
            return EnumBaseObjectTypeIds.CMIS_FOLDER;
        }

        if (isDocument(objectNodeReference))
        {
            return EnumBaseObjectTypeIds.CMIS_DOCUMENT;
        }

        return EnumBaseObjectTypeIds.CMIS_POLICY;
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

    public List<AssociationRef> receiveAssociations(NodeRef objectNodeReference, QNamePattern qnameFilter, EnumRelationshipDirection direction)
    {
        List<AssociationRef> result = new LinkedList<AssociationRef>();

        if ((direction == EnumRelationshipDirection.EITHER) || (direction == EnumRelationshipDirection.TARGET))
        {
            result.addAll(nodeService.getSourceAssocs(objectNodeReference, qnameFilter));
        }

        if ((direction == EnumRelationshipDirection.EITHER) || (direction == EnumRelationshipDirection.SOURCE))
        {
            result.addAll(nodeService.getTargetAssocs(objectNodeReference, qnameFilter));
        }

        return result;
    }

    /**
     * Returns latest minor or major version of document
     * 
     * @param documentNodeRef document node reference
     * @param major need latest major version
     * @return latest version node reference
     */
    public NodeRef getLatestNode(NodeRef documentNodeRef, boolean major)
    {
        Version specifiedVersion = versionService.getCurrentVersion(documentNodeRef);
        NodeRef latestVersionNodeRef = documentNodeRef;

        if ((null != specifiedVersion) && (null != specifiedVersion.getVersionedNodeRef()))
        {
            latestVersionNodeRef = specifiedVersion.getVersionedNodeRef();

            if (major)
            {
                Version latestVersion = versionService.getCurrentVersion(latestVersionNodeRef);

                if ((null != latestVersion) && (VersionType.MAJOR != latestVersion.getVersionType()))
                {
                    VersionHistory versionHistory = versionService.getVersionHistory(latestVersion.getFrozenStateNodeRef());
                    if (null != versionHistory)
                    {
                        for (latestVersion = versionHistory.getPredecessor(latestVersion); (null != latestVersion) && (VersionType.MAJOR != latestVersion.getVersionType()); latestVersion = versionHistory
                                .getPredecessor(latestVersion))
                        {
                        }
                    }

                    if ((null != latestVersion) && (null != latestVersion.getFrozenStateNodeRef()))
                    {
                        latestVersionNodeRef = latestVersion.getFrozenStateNodeRef();
                    }
                }
            }
        }

        return latestVersionNodeRef;
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
            List<AssociationRef> associations = receiveAssociations(objectNodeReference, new MatcheAllQNames(), EnumRelationshipDirection.EITHER);
            for (AssociationRef association : associations)
            {
                if ((null != association) && (null != association.getSourceRef()) && (null != association.getTargetRef()) && (null != association.getTypeQName()))
                {
                    nodeService.removeAssociation(association.getSourceRef(), association.getTargetRef(), association.getTypeQName());
                }
            }

            for (ChildAssociationRef parentAssociation : nodeService.getParentAssocs(objectNodeReference))
            {
                if (!parentAssociation.isPrimary())
                {
                    nodeService.removeChildAssociation(parentAssociation);
                }
            }

            nodeService.deleteNode(objectNodeReference);
        }
        catch (Throwable e)
        {
            lastException = e;
            return false;
        }
        return true;
    }

    private boolean canLock(NodeRef objectNodeReference)
    {
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        return (this.lockService.getLockStatus(objectNodeReference, currentUserName) != LockStatus.LOCKED) || authorityService.isAdminAuthority(currentUserName);
    }

    private AssociationRef safeGetAssociationRef(String identifier) throws CmisException
    {
        AssociationRef result = new AssociationRef(identifier);

        if (!nodeService.exists(result.getSourceRef()) || !nodeService.exists(result.getTargetRef()))
        {
            throw createCmisException(INVALID_OBJECT_IDENTIFIER_MESSAGE, EnumServiceException.INVALID_ARGUMENT);
        }

        return result;
    }

    private NodeRef safeGetNodeRef(String nodeIdentifier) throws CmisException
    {
        Pair<String, String> nodeRefAndVersionLabel = (null != nodeIdentifier) ? (splitOnNodeRefAndVersionLabel(nodeIdentifier)) : (null);
        if ((null != nodeRefAndVersionLabel) && (null != nodeRefAndVersionLabel.getFirst()) && NodeRef.isNodeRef(nodeRefAndVersionLabel.getFirst()))
        {
            NodeRef result = new NodeRef(nodeRefAndVersionLabel.getFirst());
            if (nodeService.exists(result))
            {
                result = getNodeRefFromVersion(result, nodeRefAndVersionLabel.getSecond());

                if ((null != result) && nodeService.exists(result))
                {
                    return result;
                }
            }
        }

        throw createCmisException(INVALID_OBJECT_IDENTIFIER_MESSAGE, EnumServiceException.OBJECT_NOT_FOUND);
    }

    private Pair<String, String> splitOnNodeRefAndVersionLabel(String nodeIdentifier)
    {
        String versionLabel = null;
        int versionDelimeterIndex = nodeIdentifier.lastIndexOf(NODE_REFERENCE_ID_DELIMETER);
        if (versionDelimeterIndex > 0)
        {
            versionLabel = nodeIdentifier.substring(versionDelimeterIndex + 1);
            if ((null != versionLabel) && !versionLabel.equals("") && VERSION_LABEL_MATCHING_COMPILED_PATTERN.matcher(versionLabel).matches())
            {
                nodeIdentifier = nodeIdentifier.substring(0, versionDelimeterIndex);
            }
            else
            {
                versionLabel = null;
            }
        }

        return new Pair<String, String>(nodeIdentifier, versionLabel);
    }

    private NodeRef getNodeRefFromVersion(NodeRef nodeRef, String versionLabel) throws CmisException
    {
        NodeRef result = nodeRef;

        NodeRef latestNodeRef = ((null != versionLabel) && (null != nodeRef)) ? (getLatestNode(nodeRef, false)) : (null);
        if ((null != latestNodeRef) && !versionLabel.equals(nodeService.getProperty(latestNodeRef, ContentModel.PROP_VERSION_LABEL)))
        {
            VersionHistory versionHistory = versionService.getVersionHistory(latestNodeRef);
            if (null != versionHistory)
            {
                Version version = versionHistory.getVersion(versionLabel);

                if ((null == version) || (null == version.getFrozenStateNodeRef()))
                {
                    throw createCmisException(("Specified object has no " + versionLabel + " version"), EnumServiceException.INVALID_ARGUMENT);
                }

                result = version.getFrozenStateNodeRef();
            }
        }

        return result;
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

    private class MatcheAllQNames implements QNamePattern
    {
        public boolean isMatch(QName qname)
        {
            return true;
        }
    }

    public Throwable getLastOperationException()
    {
        Throwable result = lastException;
        lastException = null;
        return result;
    }
}
