/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;

/**
 * Port for object service
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "ObjectServicePort", serviceName = "ObjectService", portName = "ObjectServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.ObjectServicePort")
public class DMObjectServicePort extends DMAbstractServicePort implements ObjectServicePort
{
    private static final int SINGLE_PARENT_CONDITION = 1;
    private static final String VERSION_DELIMETER = ".";

    private PermissionService permissionService;
    private DictionaryService dictionaryService;

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Creates a document object of the specified type, and optionally adds the document to a folder
     * 
     * @param repositoryId repository Id
     * @param typeId document type
     * @param properties CMIS properties
     * @param folderId parent folder for this new document
     * @param contentStream content stream
     * @param versioningState versioning state (checkedout, minor, major)
     * @return Id of the created document object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         STREAM_NOT_SUPPORTED)
     */
    public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream,
            EnumVersioningState versioningState) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef parentNodeRef = safeGetFolderNodeRef(folderId);
        Map<String, Object> propertiesMap = propertiesUtil.getPropertiesMap(properties);
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(typeId);

        String documentName = checkConstraintsAndGetName(typeId, typeDef, parentNodeRef, contentStream, propertiesMap, versioningState);

        NodeRef newDocumentNodeRef = fileFolderService.create(parentNodeRef, documentName, typeDef.getTypeId().getQName()).getNodeRef();
        ContentWriter writer = fileFolderService.getWriter(newDocumentNodeRef);
        String mimeType = (String) propertiesMap.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE);
        if (mimeType != null)
        {
            writer.setMimetype(mimeType);
        }
        else if ((contentStream != null) && (contentStream.getMimeType() != null))
        {
            writer.setMimetype(contentStream.getMimeType());
        }
        else
        {
            throw cmisObjectsUtils.createCmisException("ContentStream meime type was not specified", EnumServiceException.CONSTRAINT);
        }
        InputStream inputstream = null;
        try
        {
            inputstream = contentStream.getStream().getInputStream();
        }
        catch (IOException e)
        {
            throw cmisObjectsUtils.createCmisException(e.toString(), EnumServiceException.CONSTRAINT);
        }
        writer.putContent(inputstream);

        if (versioningState == null)
        {
            versioningState = EnumVersioningState.MAJOR;
        }

        propertiesUtil.setProperties(newDocumentNodeRef, properties, createPropertyFilter(extendStandardFilter(CMISDictionaryModel.PROP_NAME)));
        switch (versioningState)
        {
        case CHECKEDOUT:
            newDocumentNodeRef = checkoutNode(newDocumentNodeRef);
            break;
        case MAJOR:
            this.versionService.createVersion(newDocumentNodeRef, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MAJOR));
            break;
        case MINOR:
            this.versionService.createVersion(newDocumentNodeRef, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MINOR));
            break;
        }

        String versionLabel = propertiesUtil.getProperty(newDocumentNodeRef, CMISDictionaryModel.PROP_VERSION_LABEL, "");
        return versionLabel != null && versionLabel.contains(VERSION_DELIMETER) ? newDocumentNodeRef.toString() + CmisObjectsUtils.NODE_REFERENCE_ID_DELIMETER + versionLabel
                : newDocumentNodeRef.toString();
    }

    private String checkConstraintsAndGetName(String documentTypeId, CMISTypeDefinition typeDef, NodeRef parentNodeRef, CmisContentStreamType contentStream,
            Map<String, Object> propertiesMap, EnumVersioningState versioningState) throws CmisException
    {
        if ((null == typeDef) || (null == typeDef.getTypeId()))
        {
            throw cmisObjectsUtils.createCmisException(("Type with " + documentTypeId + "typeId was not found"), EnumServiceException.RUNTIME);
        }

        if ((typeDef.getTypeId().getScope() != CMISScope.DOCUMENT) || !typeDef.isCreatable())
        {
            throw cmisObjectsUtils.createCmisException(("Invalid document type \"" + documentTypeId + "\". Specified type is not Document type or type is not Creatable"),
                    EnumServiceException.CONSTRAINT);
        }

        if (CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDef.getContentStreamAllowed())
        {
            throw cmisObjectsUtils.createCmisException(("Content stream not allowed for \"" + documentTypeId + "\" document object type"),
                    EnumServiceException.STREAM_NOT_SUPPORTED);
        }
        else
        {
            if ((CMISContentStreamAllowedEnum.REQUIRED == typeDef.getContentStreamAllowed()) && (contentStream == null))
            {
                throw cmisObjectsUtils.createCmisException("Content stream for document object of " + documentTypeId + " type is required", EnumServiceException.CONSTRAINT);
            }
        }

        if (typeDef.isVersionable() && (versioningState != null))
        {
            throw cmisObjectsUtils.createCmisException(("Verioning for \"" + documentTypeId + "\" document type is not allowed"), EnumServiceException.CONSTRAINT);
        }

        String folderTypeId = propertiesUtil.getProperty(parentNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        CMISTypeDefinition folderTypeDefinition = cmisDictionaryService.findType(folderTypeId);
        if ((folderTypeDefinition.getAllowedTargetTypes() != null) && !folderTypeDefinition.getAllowedTargetTypes().isEmpty()
                && !folderTypeDefinition.getAllowedTargetTypes().contains(typeDef))
        {
            throw cmisObjectsUtils.createCmisException(("Children of \"" + documentTypeId + "\" type are not allowed for specified folder"), EnumServiceException.CONSTRAINT);
        }

        String result = (String) propertiesMap.get(CMISDictionaryModel.PROP_NAME);
        if (result == null)
        {
            throw cmisObjectsUtils.createCmisException("Name property not found", EnumServiceException.INVALID_ARGUMENT);
        }
        propertiesUtil.checkProperty(null, typeDef, CMISDictionaryModel.PROP_NAME, result, (EnumVersioningState.CHECKEDOUT == versioningState));

        return result;
    }

    /**
     * Creates a folder object of the specified type.
     * 
     * @param repositoryId repository Id
     * @param typeId document type
     * @param properties CMIS properties
     * @param folderId parent folder for this new folder
     * @return Id of the created folder object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE)
     */
    public String createFolder(String repositoryId, String typeId, CmisPropertiesType properties, String folderId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef folderNodeRef = null;
        try
        {
            folderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        }
        catch (CmisException e)
        {
            e.getFaultInfo().setType(EnumServiceException.CONSTRAINT);
            throw e;
        }
        assertExistFolder(folderNodeRef);

        CMISTypeDefinition type = getCmisTypeDefinition(typeId);
        TypeDefinition nativeType = dictionaryService.getType(nodeService.getType(folderNodeRef));
        if (type == null || type.getTypeId() == null || type.getTypeId().getScope() != CMISScope.FOLDER)
        {
            throw cmisObjectsUtils.createCmisException("The typeID is not an Object-Type whose baseType is 'Folder': " + typeId, EnumServiceException.CONSTRAINT);
        }

        String name = propertiesUtil.getCmisPropertyValue(properties, CMISDictionaryModel.PROP_NAME, null);
        propertiesUtil.checkProperty(nativeType, type, CMISDictionaryModel.PROP_NAME, name, false);

        try
        {
            NodeRef newFolderNodeRef = fileFolderService.create(folderNodeRef, name, type.getTypeId().getQName()).getNodeRef();
            propertiesUtil.setProperties(newFolderNodeRef, properties, createPropertyFilter(extendStandardFilter(CMISDictionaryModel.PROP_NAME)));
            return propertiesUtil.getProperty(newFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, null);
        }
        catch (FileExistsException e)
        {
            throw cmisObjectsUtils.createCmisException("Folder already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
        }
    }

    private String extendStandardFilter(String propertyName)
    {
        return extendStandardFilter(new String[] { propertyName });
    }

    private String extendStandardFilter(String[] propertyNames)
    {
        StringBuilder filter = new StringBuilder(propertiesUtil.createStandardNotUpdatablePropertiesFilter());

        for (String propertyName : propertyNames)
        {
            if ((null != propertyName) && !propertyName.equals(""))
            {
                filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
                filter.append(propertyName);
            }
        }

        return filter.toString();
    }

    /**
     * Creates a policy object of the specified type, and optionally adds the policy to a folder.
     * 
     * @param repositoryId repository Id
     * @param typeId policy type
     * @param properties CMIS properties
     * @param folderId parent folder for this new policy
     * @return Id of the created policy object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE)
     */
    public String createPolicy(String repositoryId, String typeId, CmisPropertiesType properties, String folderId) throws CmisException
    {
        throw cmisObjectsUtils.createCmisException("Policy objects not supported", EnumServiceException.NOT_SUPPORTED);
    }

    /**
     * Creates a relationship object of the specified type.
     * 
     * @param repositoryId repository Id
     * @param typeId relationship type
     * @param properties CMIS properties
     * @param sourceObjectId source object Id
     * @param targetObjectId target object Id
     * @return Id of the created relationship object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE)
     */
    public String createRelationship(String repositoryId, String typeId, CmisPropertiesType properties, String sourceObjectId, String targetObjectId) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef sourceNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceObjectId, AlfrescoObjectType.ANY_OBJECT);
        NodeRef targetNodeRef = cmisObjectsUtils.getIdentifierInstance(targetObjectId, AlfrescoObjectType.ANY_OBJECT);

        CMISTypeDefinition relationshipType = cmisDictionaryService.findType(typeId);
        if (relationshipType == null || relationshipType.getTypeId() == null || relationshipType.getTypeId().getScope() != CMISScope.RELATIONSHIP)
        {
            throw cmisObjectsUtils.createCmisException(typeId, EnumServiceException.INVALID_ARGUMENT);
        }

        CMISTypeDefinition sourceType = cmisDictionaryService.findType(propertiesUtil.getProperty(sourceNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        CMISTypeDefinition targetType = cmisDictionaryService.findType(propertiesUtil.getProperty(targetNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));

        if ((null != relationshipType.getAllowedSourceTypes()) && !relationshipType.getAllowedSourceTypes().contains(sourceType))
        {
            cmisObjectsUtils.createCmisException(("Source object type is not allowed for \"" + typeId + "\" Relationship type"), EnumServiceException.CONSTRAINT);
        }
        if ((null != relationshipType.getAllowedTargetTypes()) && !relationshipType.getAllowedTargetTypes().contains(targetType))
        {
            cmisObjectsUtils.createCmisException(("Target object type is not allowed for \"" + typeId + "\" Relationship type"), EnumServiceException.CONSTRAINT);
        }

        QName relationshipTypeQName = relationshipType.getTypeId().getQName();
        AssociationDefinition associationDef = dictionaryService.getAssociation(relationshipTypeQName);
        if (associationDef != null)
        {
            if (!dictionaryService.isSubClass(nodeService.getType(sourceNodeRef), associationDef.getSourceClass().getName()))
            {
                throw cmisObjectsUtils.createCmisException("Source object type isn't allowed as source type", EnumServiceException.CONSTRAINT);
            }

            if (!dictionaryService.isSubClass(nodeService.getType(targetNodeRef), associationDef.getTargetClass().getName()))
            {
                throw cmisObjectsUtils.createCmisException("Target object type isn't allowed as target type", EnumServiceException.CONSTRAINT);
            }

            return nodeService.createAssociation(sourceNodeRef, targetNodeRef, relationshipTypeQName).toString();
        }
        else
        {
            throw cmisObjectsUtils.createCmisException((relationshipType.getTypeId().getQName() + " Relationship type not found"), EnumServiceException.INVALID_ARGUMENT);
        }
    }

    /**
     * Deletes the content-stream of the specified document. This does not delete properties. If there are other versions this does not affect them, their properties or content.
     * This does not change the ID of the document.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void deleteContentStream(String repositoryId, String documentId) throws CmisException
    {
        // TODO: Where is changeToken?
        checkRepositoryId(repositoryId);
        NodeRef currentNode = cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT);

        CMISTypeDefinition typeDef = cmisDictionaryService.findType(propertiesUtil.getProperty(currentNode, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        if (CMISContentStreamAllowedEnum.REQUIRED.equals(typeDef.getContentStreamAllowed()))
        {
            throw cmisObjectsUtils.createCmisException("The 'contentStreamAllowed' attribute of the specified Object-Type definition is set to 'required'.",
                    EnumServiceException.CONSTRAINT);
        }

        safeDeleteContentStream(currentNode);
    }

    /**
     * Deletes specified object.
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         UPDATE_CONFLICT)
     */
    public void deleteObject(String repositoryId, String objectId) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeReference = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        checkForRootObject(repositoryId, objectId);
        checkObjectTypeAndAppropriateStates(objectNodeReference, nodeService.getType(objectNodeReference));
        if (!cmisObjectsUtils.deleteObject(objectNodeReference))
        {
            throw cmisObjectsUtils.createCmisException("Currently authenticated User has no appropriate Permissions to delete specified Object",
                    EnumServiceException.PERMISSION_DENIED);
        }
    }

    /**
     * Deletes the tree rooted at specified folder (including that folder)
     * 
     * @param repositoryId repository Id
     * @param folderId folder Id
     * @param unfileNonfolderObjects unfile : unfile all non-folder objects from folders in this tree. They may remain filed in other folders, or may become unfiled,
     *        deletesinglefiled : delete non-folder objects filed only in this tree, and unfile the others so they remain filed in other folders, delete : delete all non-folder
     *        objects in this tree (Default)
     * @param continueOnFailure flag
     * @return collection of object IDs that failed to delete (if continueOnFailure is FALSE, then single object ID)
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, UPDATE_CONFLICT)
     */
    public FailedToDelete deleteTree(String repositoryId, String folderId, EnumUnfileNonfolderObjects unfileNonfolderObjects, Boolean continueOnFailure) throws CmisException
    {
        checkRepositoryId(repositoryId);
        checkUnfilingIsNotRequested(unfileNonfolderObjects);
        checkForRootObject(repositoryId, folderId);

        NodeRef folderNodeReference = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        FailedToDelete responce = new FailedToDelete();
        cmisObjectsUtils.deleteFolder(folderNodeReference, continueOnFailure, (unfileNonfolderObjects == EnumUnfileNonfolderObjects.DELETE), responce.getObjectId());

        return responce;
    }

    /**
     * Gets the list of allowable actions (CMIS service calls) for an object based on the current user's context, subject to any access constraints that are currently imposed by
     * the repository.
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @return list of allowable actions
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        return determineObjectAllowableActions(cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.ANY_OBJECT));
    }

    /**
     * Gets the content-stream for a document.
     * 
     * @param repositoryId repository Id
     * @param documentId document to return the content-stream
     * @return content stream
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, STREAM_NOT_SUPPORTED)
     */
    public CmisContentStreamType getContentStream(String repositoryId, String documentId) throws CmisException
    {
        // TODO:
        // Specification says:
        // Each CMIS protocol binding SHALL provide a way for fetching a sub-range within
        // a content stream, in a manner appropriate to that protocol.
        // 
        // Implementation of sub-range fetching is suspended.
        // See http://tools.oasis-open.org/issues/browse/CMIS-134

        checkRepositoryId(repositoryId);
        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT);

        CMISTypeDefinition typeDefinition = cmisDictionaryService.findType(propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        if (CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDefinition.getContentStreamAllowed())
        {
            cmisObjectsUtils.createCmisException("Content stream not allowed", EnumServiceException.STREAM_NOT_SUPPORTED);
        }

        CmisContentStreamType response = new CmisContentStreamType();
        ContentReader reader = safeGetContentReader(nodeRef);

        response.setLength(BigInteger.valueOf(reader.getSize()));
        response.setMimeType(reader.getMimetype());
        String filename = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME, null);
        response.setFilename(filename);
        response.setStream(new DataHandler(new ContentReaderDataSource(reader, filename)));

        return response;
    }

    /**
     * Moves the specified filed object from one folder to another
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @param targetFolderId the target folder to be moved into
     * @param sourceFolderId the source folder to be moved out of
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void moveObject(String repositoryId, String objectId, String targetFolderId, String sourceFolderId) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeRef = null;
        try
        {
            objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        }
        catch (CmisException e)
        {
            e.getFaultInfo().setType(EnumServiceException.CONSTRAINT);
            throw e;
        }

        // TODO: maybe this check will be need in terms of document version instead of final document (version specific filing is not supported)
        // checkOnLatestVersion(objectNodeRef);

        NodeRef targetFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(targetFolderId, AlfrescoObjectType.FOLDER_OBJECT);
        List<ChildAssociationRef> parentsAssociations = nodeService.getParentAssocs(objectNodeRef);
        if ((parentsAssociations != null) && (SINGLE_PARENT_CONDITION != nodeService.getParentAssocs(objectNodeRef).size()))
        {
            try
            {
                NodeRef sourceFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceFolderId, AlfrescoObjectType.FOLDER_OBJECT);

                if (!cmisObjectsUtils.isPrimaryObjectParent(sourceFolderNodeRef, objectNodeRef))
                {
                    changeObjectParentAssociation(objectNodeRef, targetFolderNodeRef, sourceFolderNodeRef);
                    return;
                }
            }
            catch (CmisException e)
            {
                e.getFaultInfo().setMessage(
                        "Invalid source forlder for multifiled document was specified. Multifiled document must be moved from concrete folder. Exception message: "
                                + e.getFaultInfo().getMessage());
                throw e;
            }
        }

        safeMove(objectNodeRef, targetFolderNodeRef);
        // TODO: Allowed_Child_Object_Types
    }

    /**
     * Sets (creates or replaces) the content-stream for the specified document object.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @param overwriteFlag flag
     * @param contentStream content stream
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         CONTENT_ALREADY_EXISTS, STORAGE, STREAM_NOT_SUPPORTED, UPDATE_CONFLICT, VERSIONING)
     */
    public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT);

        CMISTypeDefinition typeDef = cmisDictionaryService.findType(propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        if (CMISContentStreamAllowedEnum.NOT_ALLOWED.equals(typeDef.getContentStreamAllowed()))
        {
            throw cmisObjectsUtils.createCmisException("The Object's Object-Type definition 'contentStreamAllowed' attribute is set to 'notAllowed'.",
                    EnumServiceException.CONSTRAINT);
        }

        if (contentStream.getStream() == null)
        {
            throw cmisObjectsUtils.createCmisException("New Content Stream was not provided", EnumServiceException.CONSTRAINT);
        }

        if ((nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) != null) && !overwriteFlag)
        {
            throw cmisObjectsUtils.createCmisException("Content already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
        }

        ContentWriter writer = fileFolderService.getWriter(nodeRef);
        InputStream inputstream = null;
        try
        {
            inputstream = contentStream.getStream().getInputStream();
        }
        catch (IOException e)
        {
            throw cmisObjectsUtils.createCmisException(e.getMessage(), EnumServiceException.CONSTRAINT);
        }

        writer.setMimetype(contentStream.getMimeType());
        writer.putContent(inputstream);

        documentId.value = propertiesUtil.getProperty(cmisObjectsUtils.getLatestNode(nodeRef, false), CMISDictionaryModel.PROP_OBJECT_ID, documentId.value);
    }

    /**
     * Updates properties of the specified object. As per the data model, content-streams are not properties.
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @param changeToken change token
     * @param properties list of properties to update
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties) throws CmisException
    {
        // TODO: change token
        checkRepositoryId(repositoryId);

        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        propertiesUtil.setProperties(objectNodeRef, properties, createPropertyFilter(propertiesUtil.createStandardNotUpdatablePropertiesFilter()));

        // no new version
        objectId.value = propertiesUtil.getProperty(objectNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, objectId.value);
    }

    /**
     * Gets the properties of an object, and optionally the operations that the user is allowed to perform on the object.
     * 
     * @param parameters
     * @return collection collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FOLDER_NOT_VALID)
     */
    public GetPropertiesResponse getProperties(GetProperties parameters) throws CmisException
    {
        checkRepositoryId(parameters.getRepositoryId());

        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        Object identifierInstance = cmisObjectsUtils.getIdentifierInstance(parameters.getObjectId(), AlfrescoObjectType.ANY_OBJECT);
        String identifier = identifierInstance.toString();
        EnumReturnVersion returnVersion = (parameters.getReturnVersion() != null && parameters.getReturnVersion().getValue() != null) ? parameters.getReturnVersion().getValue()
                : null;

        if ((returnVersion != null) && (cmisObjectsUtils.determineObjectType(identifier) == EnumObjectType.DOCUMENT))
        {
            identifier = cmisObjectsUtils.getLatestNode(new NodeRef(identifier), (EnumReturnVersion.LATEST != returnVersion)).toString();
        }

        GetPropertiesResponse response = new GetPropertiesResponse();
        response.setObject(new CmisObjectType());
        CmisObjectType object = response.getObject();
        object.setProperties(propertiesUtil.getPropertiesType(identifier, propertyFilter));

        if (parameters.getIncludeAllowableActions() != null && parameters.getIncludeAllowableActions().getValue())
        {
            // TODO: allowable actions
        }

        if (parameters.getIncludeRelationships() != null && parameters.getIncludeAllowableActions().getValue())
        {
            // TODO: relationships
        }

        return response;
    }

    private void changeObjectParentAssociation(NodeRef objectNodeRef, NodeRef targetFolderNodeRef, NodeRef sourceFolderNodeReference) throws CmisException
    {
        if (!cmisObjectsUtils.removeObject(objectNodeRef, sourceFolderNodeReference) || !cmisObjectsUtils.addObjectToFolder(objectNodeRef, targetFolderNodeRef))
        {
            determineException(cmisObjectsUtils.getLastOperationException());
        }
    }

    private void safeMove(NodeRef objectNodeRef, NodeRef targetFolderNodeRef) throws CmisException
    {
        try
        {
            fileFolderService.move(objectNodeRef, targetFolderNodeRef, null);
        }
        catch (Exception e)
        {
            determineException(e);
        }
    }

    private void safeDeleteContentStream(NodeRef documentNodeReference) throws CmisException
    {
        try
        {
            nodeService.setProperty(documentNodeReference, ContentModel.PROP_CONTENT, null);
        }
        catch (NodeLockedException e)
        {
            throw cmisObjectsUtils.createCmisException("Content Stream Deletion is not allowed for specified Object", EnumServiceException.UPDATE_CONFLICT);
        }
    }

    private ContentReader safeGetContentReader(NodeRef objectNodeReference) throws CmisException
    {
        ContentReader reader = fileFolderService.getReader(objectNodeReference);
        if (reader == null)
        {
            throw cmisObjectsUtils.createCmisException("The specified Document has no Content Stream", EnumServiceException.INVALID_ARGUMENT);
        }
        return reader;
    }

    private NodeRef safeGetFolderNodeRef(String folderId) throws CmisException
    {
        try
        {
            return this.cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        }
        catch (CmisException e)
        {
            throw cmisObjectsUtils.createCmisException("Unfiling is not suppoerted. Each Document must have existent parent Folder", EnumServiceException.OBJECT_NOT_FOUND, e);
        }
    }

    private void checkObjectTypeAndAppropriateStates(NodeRef objectNodeReference, QName objectType) throws CmisException
    {
        if (objectType == null)
        {
            throw cmisObjectsUtils.createCmisException("Specified Object has invalid Object Type", EnumServiceException.INVALID_ARGUMENT);
        }

        if (objectType.equals(ContentModel.TYPE_FOLDER) && (nodeService.getChildAssocs(objectNodeReference).size() > 0))
        {
            throw cmisObjectsUtils.createCmisException("Could not delete folder with at least one Child", EnumServiceException.CONSTRAINT);
        }
    }

    private void checkUnfilingIsNotRequested(EnumUnfileNonfolderObjects unfileNonfolderObjects) throws CmisException
    {
        if (unfileNonfolderObjects == EnumUnfileNonfolderObjects.UNFILE)
        {
            throw cmisObjectsUtils.createCmisException("Unfiling is not supported", EnumServiceException.NOT_SUPPORTED);
        }
    }

    private void checkForRootObject(String repositoryId, String objectId) throws CmisException
    {
        if (this.cmisService.getDefaultRootNodeRef().toString().equals(objectId) || repositoryId.equals(objectId))
        {
            throw cmisObjectsUtils.createCmisException("Could not delete Repository object or Root Folder object - operation is not allowed or not supported",
                    EnumServiceException.NOT_SUPPORTED);
        }
    }

    // TODO: see moveObject
    // private void checkOnLatestVersion(NodeRef currentNode) throws CmisException
    // {
    // if (!cmisObjectsUtils.isFolder(currentNode) && !propertiesUtil.getProperty(currentNode, CMISDictionaryModel.PROP_IS_LATEST_VERSION, false))
    // {
    // throw cmisObjectsUtils.createCmisException("Document is a non-current Document Version", EnumServiceException.VERSIONING);
    // }
    // }

    private CmisAllowableActionsType determineObjectAllowableActions(Object objectIdentifier) throws CmisException
    {
        if (objectIdentifier instanceof AssociationRef)
    {
            return determineRelationshipAllowableActions((AssociationRef) objectIdentifier);
    }

        switch (cmisObjectsUtils.determineObjectType(objectIdentifier.toString()))
        {
        case DOCUMENT:
        {
            return determineDocumentAllowableActions((NodeRef) objectIdentifier);
        }
        case FOLDER:
        {
            return determineFolderAllowableActions((NodeRef) objectIdentifier);
        }
        }

        // TODO: determinePolicyAllowableActions() when Policy functionality is ready
        throw cmisObjectsUtils.createCmisException("It is impossible to get Allowable actions for the specified Object", EnumServiceException.NOT_SUPPORTED);
    }

    private CmisAllowableActionsType determineBaseAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = new CmisAllowableActionsType();
        result.setCanGetProperties(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_PROPERTIES) == AccessStatus.ALLOWED);
        result.setCanUpdateProperties(this.permissionService.hasPermission(objectNodeReference, PermissionService.WRITE_PROPERTIES) == AccessStatus.ALLOWED);
        result.setCanDelete(this.permissionService.hasPermission(objectNodeReference, PermissionService.DELETE) == AccessStatus.ALLOWED);

        // TODO: response.setCanAddPolicy(value);
        // TODO: response.setCanRemovePolicy(value);
        // TODO: response.setCanGetAppliedPolicies(value);

        return result;
    }

    private CmisAllowableActionsType determineDocumentAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = determineBaseAllowableActions(objectNodeReference);
        determineCommonFolderDocumentAllowableActions(objectNodeReference, result);
        result.setCanGetParents(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        result.setCanViewContent(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_CONTENT) == AccessStatus.ALLOWED);
        result.setCanSetContent(this.permissionService.hasPermission(objectNodeReference, PermissionService.WRITE_CONTENT) == AccessStatus.ALLOWED);
        result.setCanCheckout(this.permissionService.hasPermission(objectNodeReference, PermissionService.CHECK_OUT) == AccessStatus.ALLOWED);
        result.setCanCheckin(this.permissionService.hasPermission(objectNodeReference, PermissionService.CHECK_IN) == AccessStatus.ALLOWED);
        result.setCanCancelCheckout(this.permissionService.hasPermission(objectNodeReference, PermissionService.CANCEL_CHECK_OUT) == AccessStatus.ALLOWED);
        result.setCanDeleteContent(result.isCanUpdateProperties() && result.isCanSetContent());
        return result;
    }

    private CmisAllowableActionsType determineFolderAllowableActions(NodeRef objectNodeReference)
    {
        CmisAllowableActionsType result = determineBaseAllowableActions(objectNodeReference);
        determineCommonFolderDocumentAllowableActions(objectNodeReference, result);

        result.setCanGetChildren(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_CHILDREN) == AccessStatus.ALLOWED);
        result.setCanCreateDocument(this.permissionService.hasPermission(objectNodeReference, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED);
        result.setCanGetDescendants(result.isCanGetChildren() && (this.permissionService.hasPermission(objectNodeReference, PermissionService.READ) == AccessStatus.ALLOWED));
        result.setCanDeleteTree(result.isCanDelete() && (this.permissionService.hasPermission(objectNodeReference, PermissionService.DELETE_CHILDREN) == AccessStatus.ALLOWED));
        result.setCanGetFolderParent(result.isCanGetRelationships());
        result.setCanCreateFolder(result.isCanCreateDocument());
        // TODO: response.setCanCreatePolicy(value);
        return result;
    }

    private void determineCommonFolderDocumentAllowableActions(NodeRef objectNodeReference, CmisAllowableActionsType allowableActions)
    {
        allowableActions.setCanAddToFolder(this.permissionService.hasPermission(objectNodeReference, PermissionService.CREATE_ASSOCIATIONS) == AccessStatus.ALLOWED);
        allowableActions.setCanGetRelationships(this.permissionService.hasPermission(objectNodeReference, PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        allowableActions.setCanMove(allowableActions.isCanUpdateProperties() && allowableActions.isCanAddToFolder());
        allowableActions.setCanRemoveFromFolder(allowableActions.isCanUpdateProperties());
        allowableActions.setCanCreateRelationship(allowableActions.isCanAddToFolder());
    }

    private CmisAllowableActionsType determineRelationshipAllowableActions(AssociationRef association)
    {
        CmisAllowableActionsType result = new CmisAllowableActionsType();
        result.setCanDelete(this.permissionService.hasPermission(association.getSourceRef(), PermissionService.DELETE_ASSOCIATIONS) == AccessStatus.ALLOWED);
        result.setCanGetRelationships(this.permissionService.hasPermission(association.getSourceRef(), PermissionService.READ_ASSOCIATIONS) == AccessStatus.ALLOWED);
        return result;
    }

    private void determineException(Throwable lastException) throws CmisException
    {
        if (lastException instanceof AccessDeniedException)
        {
            throw cmisObjectsUtils.createCmisException(lastException.toString(), EnumServiceException.PERMISSION_DENIED);
        }

        throw cmisObjectsUtils.createCmisException("Couldn't to relocate multi-filed Object", EnumServiceException.UPDATE_CONFLICT);
    }
}
