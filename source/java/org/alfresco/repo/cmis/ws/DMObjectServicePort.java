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
package org.alfresco.repo.cmis.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils;
import org.alfresco.repo.cmis.ws.utils.CmisObjectsUtils.IdentifierConversionResults;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
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
@javax.jws.WebService(name = "ObjectServicePort", serviceName = "ObjectService", portName = "ObjectServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.ObjectServicePort")
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws StorageException
     * @throws StreamNotSupportedException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream, EnumVersioningState versioningState)
        throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, FolderNotValidException,
               OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        Map<String, Serializable> propertiesMap = getPropertiesMap(properties);
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(typeId);
        if (typeDef.getTypeId().getScope() != CMISScope.DOCUMENT)
        {
            throw new ConstraintViolationException("Invalid document type " + typeId);
        }

        NodeRef parentNodeRef = safeGetFolderNodeRef(folderId);

        String documentName = (String) propertiesMap.get(CMISDictionaryModel.PROP_NAME);
        if (documentName == null)
        {
            throw new InvalidArgumentException("Name property not found");
        }

        NodeRef newDocumentNodeRef = fileFolderService.create(parentNodeRef, documentName, typeDef.getTypeId().getQName()).getNodeRef();
        ContentWriter writer = fileFolderService.getWriter(newDocumentNodeRef);
        String mimeType = (String) propertiesMap.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE);
        if (mimeType != null)
        {
            writer.setMimetype(mimeType);
        }
        InputStream inputstream = null;
        try
        {
            inputstream = contentStream.getStream().getInputStream();
        }
        catch (IOException e)
        {
            throw new ConstraintViolationException("", e.getCause());
        }
        writer.putContent(inputstream);

        if (versioningState == null)
        {
            versioningState = EnumVersioningState.MAJOR;
        }

        // TODO:
        // cmisPropertyService.setProperties(newDocumentNodeRef, propertiesMap);

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

        String versionLabel = (String) cmisService.getProperty(newDocumentNodeRef, CMISDictionaryModel.PROP_VERSION_LABEL);
        return versionLabel != null && versionLabel.contains(VERSION_DELIMETER) ? 
                newDocumentNodeRef.toString() + CmisObjectsUtils.NODE_REFERENCE_ID_DELIMETER + versionLabel :
                newDocumentNodeRef.toString();
    }

    /**
     * Creates a folder object of the specified type.
     * 
     * @param repositoryId repository Id
     * @param typeId document type
     * @param properties CMIS properties
     * @param folderId parent folder for this new folder
     * @return Id of the created folder object
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public String createFolder(String repositoryId, String typeId, CmisPropertiesType properties, String folderId)
        throws PermissionDeniedException, UpdateConflictException, FolderNotValidException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        NodeRef folderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();

        CMISTypeDefinition type = getCmisTypeDefinition(typeId);
        if (type == null || type.getTypeId().getScope() != CMISScope.FOLDER)
        {
            throw new TypeNotFoundException(typeId);
        }

        Map<String, Serializable> propertiesMap = getPropertiesMap(properties);
        String name = (String) propertiesMap.get(CMISDictionaryModel.PROP_NAME);
        if (name == null)
        {
            throw new InvalidArgumentException("Name property not found");
        }

        assertExistFolder(folderNodeRef);

        try
        {
            NodeRef newFolderNodeRef = fileFolderService.create(folderNodeRef, name, type.getTypeId().getQName()).getNodeRef();
            // TODO:
            // cmisPropertyService.setProperties(newFolderNodeRef, propertiesMap);
            return (String) cmisService.getProperty(newFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_ID);
        }
        catch (FileExistsException e)
        {
            throw new UpdateConflictException("Folder already exists");
        }
    }

    /**
     * Creates a policy object of the specified type, and optionally adds the policy to a folder.
     * 
     * @param repositoryId repository Id
     * @param typeId policy type
     * @param properties CMIS properties
     * @param folderId parent folder for this new policy
     * @return Id of the created policy object
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public String createPolicy(String repositoryId, String typeId, CmisPropertiesType properties, String folderId)
        throws PermissionDeniedException, UpdateConflictException, FolderNotValidException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        // TODO:
        return null;
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public String createRelationship(String repositoryId, String typeId, CmisPropertiesType properties, String sourceObjectId, String targetObjectId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef sourceNodeRef;
        NodeRef targetNodeRef;

        try
        {
            sourceNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceObjectId, AlfrescoObjectType.ANY_OBJECT).getConvertedIdentifier();
            targetNodeRef = cmisObjectsUtils.getIdentifierInstance(targetObjectId, AlfrescoObjectType.ANY_OBJECT).getConvertedIdentifier();
        }
        catch (InvalidArgumentException e)
        {
            if (e.getCause() instanceof ObjectNotFoundException)
            {
                throw new ObjectNotFoundException(e.getMessage());
            }
            throw e;
        }

        CMISTypeDefinition relationshipType = cmisDictionaryService.findType(typeId);
        if (relationshipType == null || relationshipType.getTypeId().getScope() != CMISScope.RELATIONSHIP)
        {
            throw new TypeNotFoundException(typeId);
        }

        QName relationshipTypeQName = relationshipType.getTypeId().getQName();
        AssociationDefinition associationDef = dictionaryService.getAssociation(relationshipTypeQName);
        if (associationDef != null)
        {
            if (!dictionaryService.isSubClass(nodeService.getType(sourceNodeRef), associationDef.getSourceClass().getName()))
            {
                throw new ConstraintViolationException("Source object type isn't allowed as source type");
            }

            if (!dictionaryService.isSubClass(nodeService.getType(targetNodeRef), associationDef.getTargetClass().getName()))
            {
                throw new ConstraintViolationException("Target object type isn't allowed as target type");
            }

            return nodeService.createAssociation(sourceNodeRef, targetNodeRef, relationshipTypeQName).toString();
        }
        else
        {
            throw new TypeNotFoundException(relationshipType.getTypeId().getQName() + " Relationship type not found");
        }
    }

    /**
     * Deletes the content-stream of the specified document. This does not delete properties. If there are other versions this does not affect them, their properties or content.
     * This does not change the ID of the document.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws StorageException
     * @throws StreamNotSupportedException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws VersioningException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void deleteContentStream(String repositoryId, String documentId)
        throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, VersioningException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        safeDeleteContentStream((NodeRef) cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier());
    }

    /**
     * Deletes specified object.
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void deleteObject(String repositoryId, String objectId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeReference = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier();
        checkForRootObject(repositoryId, objectId);
        checkObjectTypeAndAppropriateStates(objectNodeReference, nodeService.getType(objectNodeReference));
        if (!cmisObjectsUtils.deleteObject(objectNodeReference))
        {
            throw new PermissionDeniedException("Currently authenticated User has no appropriate Permissions to delete specified Object");
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public FailedToDelete deleteTree(String repositoryId, String folderId, EnumUnfileNonfolderObjects unfileNonfolderObjects, Boolean continueOnFailure)
        throws PermissionDeniedException, UpdateConflictException, FolderNotValidException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        checkUnfilingIsNotRequested(unfileNonfolderObjects);
        checkForRootObject(repositoryId, folderId);

        NodeRef folderNodeReference = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     */
    public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws StorageException
     * @throws StreamNotSupportedException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws OffsetException
     */
    public CmisContentStreamType getContentStream(String repositoryId, String documentId)
        throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, OffsetException
    {
        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(documentId, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();

        CmisContentStreamType response = new CmisContentStreamType();
        ContentReader reader = safeGetContentReader(nodeRef);

        response.setLength(BigInteger.valueOf(reader.getSize()));
        response.setMimeType(reader.getMimetype());
        String filename = (String) cmisService.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME);
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
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws NotInFolderException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void moveObject(String repositoryId, String objectId, String targetFolderId, String sourceFolderId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, FolderNotValidException, OperationNotSupportedException, NotInFolderException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier();
        NodeRef targetFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(targetFolderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();
        NodeRef sourceFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceFolderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();
        
        // TODO: Allowed_Child_Object_Types

        if (nodeService.getParentAssocs(objectNodeRef).size() == SINGLE_PARENT_CONDITION || !changeObjectParentAssociation(objectNodeRef, targetFolderNodeRef, sourceFolderNodeRef))
        {
            safeMove(objectNodeRef, targetFolderNodeRef);
        }
    }

    /**
     * Sets (creates or replaces) the content-stream for the specified document object.
     * 
     * @param repositoryId repository Id
     * @param documentId document Id
     * @param overwriteFlag flag
     * @param contentStream content stream
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws StorageException
     * @throws StreamNotSupportedException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws ContentAlreadyExistsException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream)
        throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, ContentAlreadyExistsException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier();

        if (contentStream.getStream() == null)
        {
            throw new InvalidArgumentException("New Content Stream was not provided");
        }

        if ((nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) != null) && !overwriteFlag)
        {
            throw new ContentAlreadyExistsException();
        }

        ContentWriter writer = fileFolderService.getWriter(nodeRef);
        InputStream inputstream = null;
        try
        {
            inputstream = contentStream.getStream().getInputStream();
        }
        catch (IOException e)
        {
            throw new ConstraintViolationException("", e.getCause());
        }

        writer.setMimetype(contentStream.getMimeType());
        writer.putContent(inputstream);
    }

    /**
     * Updates properties of the specified object. As per the data model, content-streams are not properties.
     * 
     * @param repositoryId repository Id
     * @param objectId object Id
     * @param changeToken change token
     * @param properties list of properties to update
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        checkForReadOnlyProperties(properties);

        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier();
        setProperties(objectNodeRef, properties);

        // TODO: change token

        // no new version
        objectId.value = (String) cmisService.getProperty(objectNodeRef, CMISDictionaryModel.PROP_OBJECT_ID);
    }

    /**
     * Gets the properties of an object, and optionally the operations that the user is allowed to perform on the object.
     * 
     * @param parameters
     * @return collection collection of CmisObjectType
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FilterNotValidException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     */
    public GetPropertiesResponse getProperties(GetProperties parameters)
        throws PermissionDeniedException, UpdateConflictException, FilterNotValidException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException
    {
        checkRepositoryId(parameters.getRepositoryId());

        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        String identifier = ((NodeRef) cmisObjectsUtils.getIdentifierInstance(parameters.getObjectId(), AlfrescoObjectType.ANY_OBJECT).getConvertedIdentifier()).toString();
        EnumReturnVersion returnVersion = (parameters.getReturnVersion() != null && parameters.getReturnVersion().getValue() != null) ? parameters.getReturnVersion().getValue() : null;
        
        if ((cmisObjectsUtils.determineObjectType(identifier) == EnumObjectType.DOCUMENT) && returnVersion != null)
        {
            identifier = getLatestNode(new NodeRef(identifier), returnVersion != EnumReturnVersion.LATEST).toString();
        }

        GetPropertiesResponse response = new GetPropertiesResponse();
        response.setObject(new CmisObjectType());
        CmisObjectType object = response.getObject();
        object.setProperties(getPropertiesType(identifier, propertyFilter));

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

    
    private Map<String, Serializable> getPropertiesMap(CmisPropertiesType cmisProperties) throws InvalidArgumentException
    {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            String name = PropertyUtil.getRepositoryPropertyName(cmisProperty.getName());
            if (name == null)
            {
                throw new InvalidArgumentException("Unknown property with name " + name);
            }

            properties.put(name, PropertyUtil.getValue(cmisProperty));
        }

        return properties;
    }

    private boolean changeObjectParentAssociation(NodeRef objectNodeRef, NodeRef targetFolderNodeRef, NodeRef sourceFolderNodeReference)
        throws UpdateConflictException, PermissionDeniedException
    {
        if (cmisObjectsUtils.isPrimaryObjectParent(sourceFolderNodeReference, objectNodeRef))
        {
            return false;
        }
    
        if (!cmisObjectsUtils.removeObject(objectNodeRef, sourceFolderNodeReference) && cmisObjectsUtils.addObjectToFolder(objectNodeRef, targetFolderNodeRef))
        {
            determineException(cmisObjectsUtils.getLastOperationException());
        }
    
        return true;
    }

    private void safeMove(NodeRef objectNodeRef, NodeRef targetFolderNodeRef)
        throws PermissionDeniedException, UpdateConflictException
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

    private void safeDeleteContentStream(NodeRef documentNodeReference) throws ConstraintViolationException
    {
        try
        {
            nodeService.setProperty(documentNodeReference, ContentModel.PROP_CONTENT, null);
        }
        catch (NodeLockedException e)
        {
            throw new ConstraintViolationException("Content Stream Deletion is not allowed for specified Document", e);
        }
    }

    private ContentReader safeGetContentReader(NodeRef objectNodeReference) throws StorageException
    {
        ContentReader reader = fileFolderService.getReader(objectNodeReference);
        if (reader == null)
        {
            throw new StorageException("The specified Document has no Content Stream");
        }
        return reader;
    }

    private NodeRef safeGetFolderNodeRef(String folderId) throws FolderNotValidException
    {
        try
        {
            return this.cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();
        }
        catch (InvalidArgumentException e)
        {
            throw new FolderNotValidException("Unfiling is not suppoerted. Each Document must have existent parent Folder");
        }
    }
    
    private void checkObjectTypeAndAppropriateStates(NodeRef objectNodeReference, QName objectType) throws InvalidArgumentException, ConstraintViolationException
    {
        if (objectType == null)
        {
            throw new InvalidArgumentException("Specified Object has invalid Object Type");
        }

        if (objectType.equals(ContentModel.TYPE_FOLDER) && (nodeService.getChildAssocs(objectNodeReference).size() > 0))
        {
            throw new ConstraintViolationException("Could not delete folder with at least one Child");
        }
    }

    private void checkUnfilingIsNotRequested(EnumUnfileNonfolderObjects unfileNonfolderObjects) throws OperationNotSupportedException
    {
        if (unfileNonfolderObjects == EnumUnfileNonfolderObjects.UNFILE)
        {
            throw new OperationNotSupportedException("Unfiling is not supported");
        }
    }

    private void checkForRootObject(String repositoryId, String objectId) throws OperationNotSupportedException
    {
        if (this.cmisService.getDefaultRootNodeRef().toString().equals(objectId) || repositoryId.equals(objectId))
        {
            throw new OperationNotSupportedException("Could not delete Repository object or Root Folder object - operation is not allowed or not supported");
        }
    }

    private void checkForReadOnlyProperties(CmisPropertiesType properties) throws ConstraintViolationException
    {
        for (CmisProperty property : properties.getProperty())
        {
            if (PropertyUtil.isReadOnlyRepositoryProperty(property.getName()))
            {
                throw new ConstraintViolationException("The property " + property.getName() + " is Read Only and couldn't be updated");
            }
        }
    }

    private CmisAllowableActionsType determineObjectAllowableActions(IdentifierConversionResults objectIdentifierContainer) throws OperationNotSupportedException
    {
        Object objectNodeReference = objectIdentifierContainer.getConvertedIdentifier();

        if (objectNodeReference instanceof AssociationRef)
        {
            return determineRelationshipAllowableActions((AssociationRef) objectIdentifierContainer.getConvertedIdentifier());
        }

        switch (cmisObjectsUtils.determineObjectType(objectNodeReference.toString()))
        {
            case DOCUMENT:
            {
                return determineDocumentAllowableActions((NodeRef) objectNodeReference);
            }
            case FOLDER:
            {
                return determineFolderAllowableActions((NodeRef) objectNodeReference);
            }
        }

        // TODO: determinePolicyAllowableActions() when Policy functionality is ready
        throw new OperationNotSupportedException("It is impossible to get Allowable actions for the specified Object");
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

    private void determineException(Throwable lastException) throws PermissionDeniedException, UpdateConflictException
    {
        if (lastException instanceof AccessDeniedException)
        {
            throw new PermissionDeniedException(lastException.getMessage());
        }

        throw new UpdateConflictException("Couldn't to relocate multi-filed Object");
    }
    
}
