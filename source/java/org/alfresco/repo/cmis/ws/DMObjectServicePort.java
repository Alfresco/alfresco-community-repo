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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.dictionary.CMISFolderTypeDefinition;
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
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;

/**
 * Port for object service
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "ObjectServicePort", serviceName = "ObjectService", portName = "ObjectServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.ObjectServicePort")
public class DMObjectServicePort extends DMAbstractServicePort implements ObjectServicePort
{
    private static final String VERSION_DELIMETER = ".";
    private static final String FOLDER_PATH_MATCHING_PATTERN = "^(/){1}([\\p{L}\\p{Digit} _\\-()]*(/)?)*[\\p{L}\\p{Digit} _\\-()]$";

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Creates a document object of the specified type, and optionally adds the document to a folder
     * 
     * @param repositoryId repository Id
     * @param properties CMIS properties
     * @param folderId parent folder for this new document
     * @param contentStream content stream
     * @param versioningState versioning state (checkedout, minor, major)
     * @return Id of the created document object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         STREAM_NOT_SUPPORTED)
     */
    public void createDocument(String repositoryId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream, EnumVersioningState versioningState,
            List<String> policies, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension, Holder<String> objectId)
            throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef parentNodeRef = safeGetFolderNodeRef(folderId);
        if (null == properties)
        {
            throw cmisObjectsUtils.createCmisException("Properties input parameter is Mandatory", EnumServiceException.INVALID_ARGUMENT);
        }
        Map<String, Object> propertiesMap = propertiesUtil.getPropertiesMap(properties);
        String typeId = extractAndAssertTypeId(propertiesMap);
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(typeId);
        String documentName = checkConstraintsAndGetName(typeId, typeDef, parentNodeRef, contentStream, propertiesMap, versioningState);
        NodeRef newDocumentNodeRef = fileFolderService.create(parentNodeRef, documentName, typeDef.getTypeId().getQName()).getNodeRef();

        if (null != contentStream)
        {
            ContentWriter writer = fileFolderService.getWriter(newDocumentNodeRef);
            String mimeType = (String) propertiesMap.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE);
            mimeType = (null == mimeType) ? (contentStream.getMimeType()) : (mimeType);
            if (null != mimeType)
            {
                writer.setMimetype(mimeType);
            }
            else
            {
                throw cmisObjectsUtils.createCmisException("Content Stream Mime Type was not specified", EnumServiceException.STORAGE);
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
        }
        PropertyFilter propertyFilter = createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_NAME, CMISDictionaryModel.PROP_OBJECT_TYPE_ID }));
        appendDataToDocument(newDocumentNodeRef, properties, versioningState, policies, addACEs, removeACEs, objectId, propertyFilter);
    }
    
    /**
     * Creates a document object as a copy of the given source document in the specified location
     * 
     * @param repositoryId repository Id
     * @param properties CMIS properties
     * @param folderId parent folder for this new document
     * @param contentStream content stream
     * @param versioningState versioning state (checkedout, minor, major)
     * @return Id of the created document object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         STREAM_NOT_SUPPORTED)
     */
    public void createDocumentFromSource(String repositoryId, String sourceId, CmisPropertiesType properties, String folderId, EnumVersioningState versioningState,
            List<String> policies, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension, Holder<String> objectId)
            throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef folderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        NodeRef sourceNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        String name = propertiesUtil.getProperty(sourceNodeRef, CMISDictionaryModel.PROP_NAME, null);
        NodeRef newDocumentNodeRef;
        try
        {
            newDocumentNodeRef = fileFolderService.copy(sourceNodeRef, folderNodeRef, name).getNodeRef();
        }
        catch (FileExistsException e)
        {
            throw cmisObjectsUtils.createCmisException("Document already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
        }
        catch (FileNotFoundException e)
        {
            throw cmisObjectsUtils.createCmisException("Source document not found", EnumServiceException.INVALID_ARGUMENT);
        }
        PropertyFilter propertyFilter = createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_OBJECT_TYPE_ID }));
        appendDataToDocument(newDocumentNodeRef, properties, versioningState, policies, addACEs, removeACEs, objectId, propertyFilter);
    }
    
    /**
     * Creates a folder object of the specified type.
     * 
     * @param repositoryId repository Id
     * @param properties CMIS properties
     * @param folderId parent folder for this new folder
     * @return Id of the created folder object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE)
     */

    public void createFolder(String repositoryId, CmisPropertiesType properties, String folderId, List<String> policies, CmisAccessControlListType addACEs,
            CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException
    {
        // TODO: process Policies and ACL

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
        Map<String, Object> propertiesMap = propertiesUtil.getPropertiesMap(properties);
        String typeId = extractAndAssertTypeId(propertiesMap);
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
            propertiesUtil.setProperties(newFolderNodeRef, properties, createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_NAME,
                    CMISDictionaryModel.PROP_OBJECT_TYPE_ID })));
            objectId.value = propertiesUtil.getProperty(newFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, null);
        }
        catch (FileExistsException e)
        {
            throw cmisObjectsUtils.createCmisException("Folder already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
        }
    }

    /**
     * Creates a policy object of the specified type, and optionally adds the policy to a folder.
     * 
     * @param repositoryId repository Id
     * @param properties CMIS properties
     * @param folderId parent folder for this new policy
     * @return Id of the created policy object
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE)
     */
    public void createPolicy(String repositoryId, CmisPropertiesType properties, String folderId, List<String> policies, CmisAccessControlListType addACEs,
            CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException
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
    public void createRelationship(String repositoryId, CmisPropertiesType properties, List<String> policies, CmisAccessControlListType addACEs,
            CmisAccessControlListType removeACEs, Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException
    {
        // TODO: process Policies and ACL

        Map<String, Object> propertiesMap = propertiesUtil.getPropertiesMap(properties);
        String sourceObjectId = (String) propertiesMap.get(CMISDictionaryModel.PROP_SOURCE_ID);
        String targetObjectId = (String) propertiesMap.get(CMISDictionaryModel.PROP_TARGET_ID);
        
        checkRepositoryId(repositoryId);

        NodeRef sourceNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceObjectId, AlfrescoObjectType.ANY_OBJECT);
        NodeRef targetNodeRef = cmisObjectsUtils.getIdentifierInstance(targetObjectId, AlfrescoObjectType.ANY_OBJECT);
        
        String typeId = (String) propertiesMap.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);

        CMISTypeDefinition relationshipType = cmisDictionaryService.findType(typeId);
        if (relationshipType == null || relationshipType.getTypeId() == null || relationshipType.getTypeId().getScope() != CMISScope.RELATIONSHIP)
        {
            throw cmisObjectsUtils.createCmisException(typeId, EnumServiceException.INVALID_ARGUMENT);
        }

        CMISTypeDefinition sourceType = cmisDictionaryService.findType(propertiesUtil.getProperty(sourceNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        CMISTypeDefinition targetType = cmisDictionaryService.findType(propertiesUtil.getProperty(targetNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));

        if ((null != relationshipType.getAllowedSourceTypes()) && !relationshipType.getAllowedSourceTypes().contains(sourceType))
        {
            throw cmisObjectsUtils.createCmisException(("Source object type is not allowed for \"" + typeId + "\" Relationship type"), EnumServiceException.CONSTRAINT);
        }
        if ((null != relationshipType.getAllowedTargetTypes()) && !relationshipType.getAllowedTargetTypes().contains(targetType))
        {
            throw cmisObjectsUtils.createCmisException(("Target object type is not allowed for \"" + typeId + "\" Relationship type"), EnumServiceException.CONSTRAINT);
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

            String createdId = nodeService.createAssociation(sourceNodeRef, targetNodeRef, relationshipTypeQName).toString();
            objectId.value = createdId;
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
    public void deleteContentStream(String repositoryId, Holder<String> documentId, Holder<String> changeToken, Holder<CmisExtensionType> extension) throws CmisException
    {
        // TODO: Process changeToken
        checkRepositoryId(repositoryId);
        NodeRef currentNode = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT);

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
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, Holder<CmisExtensionType> extension) throws CmisException
    {
        // TODO: Process flag allVersions

        checkRepositoryId(repositoryId);

        NodeRef objectNodeReference = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        checkForRootObject(repositoryId, objectId);
        checkObjectTypeAndAppropriateStates(objectNodeReference, nodeService.getType(objectNodeReference));
        if (allVersions != null && allVersions)
        {
            deleteAllVersions(repositoryId, objectId);
        }
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
    public FailedToDelete deleteTree(String repositoryId, String folderId, Boolean allVersions, EnumUnfileObject unfileObject, Boolean continueOnFailure,
            CmisExtensionType extension) throws CmisException
    {
        //TODO: Process allVersions
        checkRepositoryId(repositoryId);
        checkUnfilingIsNotRequested(unfileObject);
        checkForRootObject(repositoryId, folderId);

        NodeRef folderNodeReference = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        FailedToDelete responce = new FailedToDelete();
        allVersions = allVersions == null ? Boolean.FALSE : allVersions;
        responce.getObjectIds().addAll(cmisObjectsUtils.deleteFolder(folderNodeReference, continueOnFailure, unfileObject, allVersions));

        return responce;
    }

    /**
     * Gets the specified object
     * 
     * @param repositoryId repository Id
     * @param folderPath The path to the folder
     * @param filter property filter
     * @return list of properties for the Folder
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND)
     */
    public CmisObjectType getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeACL, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        PropertyFilter propertyFilter = createPropertyFilter(filter);

        Object identifierInstance = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.ANY_OBJECT);
        String identifier = identifierInstance.toString();

        CmisPropertiesType properties = propertiesUtil.getPropertiesType(identifier, propertyFilter);
        CmisObjectType object = new CmisObjectType();
        object.setProperties(properties);
        if (includeAllowableActions)
        {
            object.setAllowableActions(determineObjectAllowableActions(identifierInstance));
        }

        // TODO: process relationships
        // TODO: process ACL
        // TODO: process rendition

        return object;
    }
    
    /**
     * Gets the specified object by path
     * 
     * @param repositoryId repository Id
     * @param folderPath The path to the folder
     * @param filter property filter
     * @return list of properties for the Folder
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND)
     */
    public CmisObjectType getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeACL, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef objectNodeRef = resolvePathInfo(cmisService.getDefaultRootNodeRef(), path);
        if ((null == path) || (null == objectNodeRef))
        {
            throw cmisObjectsUtils.createCmisException("Path to Folder was not specified or Folder Path is invalid", EnumServiceException.INVALID_ARGUMENT);
        }

        Object identifierInstance = cmisObjectsUtils.getIdentifierInstance(objectNodeRef.toString(), AlfrescoObjectType.ANY_OBJECT);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        CmisObjectType object = new CmisObjectType();
        object.setProperties(propertiesUtil.getPropertiesType(identifierInstance.toString(), propertyFilter));

        if (includeAllowableActions)
        {
            object.setAllowableActions(determineObjectAllowableActions(identifierInstance));
        }

        // TODO: process relationships
        // TODO: process ACL
        // TODO: process rendition

        return object;
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
    public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId, CmisExtensionType extension) throws CmisException
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
    public CmisContentStreamType getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset, BigInteger length, CmisExtensionType extension)
    throws CmisException
    {
        // TODO: process streamId

        checkRepositoryId(repositoryId);
        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);

        CMISTypeDefinition typeDefinition = cmisDictionaryService.findType(propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        if (CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDefinition.getContentStreamAllowed())
        {
            throw cmisObjectsUtils.createCmisException("Content stream not allowed", EnumServiceException.STREAM_NOT_SUPPORTED);
        }

        CmisContentStreamType response = new CmisContentStreamType();
        ContentReader reader = safeGetContentReader(nodeRef);
        String filename = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME, null);
        response.setFilename(filename);
        response.setMimeType(reader.getMimetype());
        ContentReaderDataSource dataSource = new ContentReaderDataSource(reader, filename, offset, length, reader.getSize());
        response.setStream(new DataHandler(dataSource));
        response.setLength(BigInteger.valueOf(dataSource.getSizeToRead()));        
        
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
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        // TODO: maybe this check will be need in terms of document version instead of final document (version specific filing is not supported)
        // checkOnLatestVersion(objectNodeRef);

        NodeRef targetFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(targetFolderId, AlfrescoObjectType.FOLDER_OBJECT);
        String objectType = propertiesUtil.getProperty(objectNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        String targetType = propertiesUtil.getProperty(targetFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        CMISFolderTypeDefinition targetTypeDef = (CMISFolderTypeDefinition) ((null != targetType) ? (cmisDictionaryService.findType(targetType)) : (null));
        if (null == targetTypeDef)
        {
            throw cmisObjectsUtils.createCmisException("Type Definition for Target Folder was not found", EnumServiceException.RUNTIME);
        }

        // FIXME: targetTypeDef.getAllowedTargetTypes() should be changed to something like targetTypeDef.getAllowedChildTypes()
        CMISTypeDefinition objectTypeDef = (null != objectType) ? (cmisDictionaryService.findType(objectType)) : (null);
        if (!targetTypeDef.getAllowedTargetTypes().isEmpty() && !targetTypeDef.getAllowedTargetTypes().contains(objectTypeDef))
        {
            throw cmisObjectsUtils.createCmisException(("Object with '" + objectType + "' Type can't be moved to Folder with '" + targetType + "' Type"),
                    EnumServiceException.CONSTRAINT);
        }
        NodeRef sourceFolderNodeRef = null;
        if ((null != sourceFolderId) && !"".equals(sourceFolderId))
        {
            sourceFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(sourceFolderId, AlfrescoObjectType.FOLDER_OBJECT);
        }
        if ((null != sourceFolderNodeRef) && !cmisObjectsUtils.isPrimaryObjectParent(objectNodeRef, sourceFolderNodeRef))
        {
            if (!cmisObjectsUtils.isChildOfThisFolder(objectNodeRef, sourceFolderNodeRef))
            {
                throw cmisObjectsUtils.createCmisException("The Docuemnt is not a Child of Source Forlder that was specified", EnumServiceException.INVALID_ARGUMENT);
            }
            changeObjectParentAssociation(objectNodeRef, targetFolderNodeRef, sourceFolderNodeRef);
        }
        else
        {
            safeMove(objectNodeRef, targetFolderNodeRef);
        }

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

    public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, Holder<String> changeToken, CmisContentStreamType contentStream,
            Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        // TODO:Process changeToken

        NodeRef nodeRef = cmisObjectsUtils.getIdentifierInstance(documentId.value, AlfrescoObjectType.DOCUMENT_OBJECT);

        CMISTypeDefinition typeDef = cmisDictionaryService.findType(propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        if (CMISContentStreamAllowedEnum.NOT_ALLOWED.equals(typeDef.getContentStreamAllowed()))
        {
            throw cmisObjectsUtils.createCmisException("The Object's Object-Type definition 'contentStreamAllowed' attribute is set to 'notAllowed'.",
                    EnumServiceException.CONSTRAINT);
        }

        if ((null == contentStream) || (contentStream.getStream() == null))
        {
            throw cmisObjectsUtils.createCmisException("New Content Stream was not provided", EnumServiceException.STORAGE);
        }

        ContentReader reader = fileFolderService.getReader(nodeRef);
        if ((null != reader) && !overwriteFlag)
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
            throw cmisObjectsUtils.createCmisException(e.getMessage(), EnumServiceException.UPDATE_CONFLICT);
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
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken, CmisPropertiesType properties, Holder<CmisExtensionType> extension)
    throws CmisException
    {
        // TODO: Process changeToken
        checkRepositoryId(repositoryId);

        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId.value, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        propertiesUtil.setProperties(objectNodeRef, properties, null);

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
    public CmisPropertiesType getProperties(String repositoryId, String objectId, String filter, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        PropertyFilter propertyFilter = createPropertyFilter(filter);

        Object identifierInstance = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.ANY_OBJECT);
        String identifier = identifierInstance.toString();

        CmisPropertiesType result = propertiesUtil.getPropertiesType(identifier, propertyFilter);

        return result;
    }

    /**
     * Gets the renditions of an object, and optionally the operations that the user is allowed to perform on the object.
     * 
     * @param parameters
     * @return collection collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FOLDER_NOT_VALID)
     */
    public List<CmisRenditionType> getRenditions(String repositoryId, String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount,
            CmisExtensionType extension) throws CmisException
    {
        // TODO: Process renditions
        throw cmisObjectsUtils.createCmisException("Renditions objects not supported", EnumServiceException.NOT_SUPPORTED);
    }
    
    private void appendDataToDocument(NodeRef targetDocumentNodeRef, CmisPropertiesType properties, EnumVersioningState versioningState,
            List<String> policies, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, Holder<String> objectId, PropertyFilter propertyFilter)
            throws CmisException
    {
        // TODO: process Policies and ACE

        propertiesUtil.setProperties(targetDocumentNodeRef, properties, propertyFilter);

        String versionLabel = null;

        // FIXME: change condition below to "typeDef.isVersionable()" when dictionary problem will be fixed
        if (true)
        {
            versioningState = (null == versioningState) ? (EnumVersioningState.MINOR) : (versioningState);
            switch (versioningState)
            {
            case CHECKEDOUT:
                targetDocumentNodeRef = checkoutNode(targetDocumentNodeRef);
                break;
            case MAJOR:
                this.versionService.createVersion(targetDocumentNodeRef, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MAJOR));
                break;
            case MINOR:
                this.versionService.createVersion(targetDocumentNodeRef, createVersionProperties(INITIAL_VERSION_DESCRIPTION, VersionType.MINOR));
                break;
            }
            versionLabel = propertiesUtil.getProperty(targetDocumentNodeRef, CMISDictionaryModel.PROP_VERSION_LABEL, "");
        }
        String createdObjectId = ((null != versionLabel) && versionLabel.contains(VERSION_DELIMETER)) ? (targetDocumentNodeRef.toString() + CmisObjectsUtils.NODE_REFERENCE_ID_DELIMETER + versionLabel)
                : (targetDocumentNodeRef.toString());
        objectId.value = createdObjectId;
    }
    
    private String createIgnoringFilter(String[] propertyNames)
    {
        StringBuilder filter = new StringBuilder("");

        for (String propertyName : propertyNames)
        {
            if ((null != propertyName) && !propertyName.equals(""))
            {
                filter.append(propertyName);
                filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
            }
        }

        if (filter.length() > 0)
        {
            filter.deleteCharAt(filter.length() - 1);
        }

        return filter.toString();
    }

    private String extractAndAssertTypeId(Map<String, Object> propertiesMap) throws CmisException
    {
        String typeId = (String) propertiesMap.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        if ((null == typeId) || "".equals(typeId))
        {
            throw cmisObjectsUtils.createCmisException("Type Id property required", EnumServiceException.INVALID_ARGUMENT);
        }
        return typeId;
    }

    private String checkConstraintsAndGetName(String documentTypeId, CMISTypeDefinition typeDef, NodeRef parentNodeRef, CmisContentStreamType contentStream,
            Map<String, Object> propertiesMap, EnumVersioningState versioningState) throws CmisException
    {
        if ((null == typeDef) || (null == typeDef.getTypeId()))
        {
            throw cmisObjectsUtils.createCmisException(("Type with " + documentTypeId + "typeId was not found"), EnumServiceException.RUNTIME);
        }

        if ((CMISScope.DOCUMENT != typeDef.getTypeId().getScope()) || !typeDef.isCreatable())
        {
            throw cmisObjectsUtils.createCmisException(("Invalid document type \"" + documentTypeId + "\". This type is not a Creatable Document type"),
                    EnumServiceException.CONSTRAINT);
        }

        if ((null != contentStream) && CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDef.getContentStreamAllowed())
        {
            throw cmisObjectsUtils.createCmisException(("Content stream not allowed for \"" + documentTypeId + "\" document object type"),
                    EnumServiceException.STREAM_NOT_SUPPORTED);
        }
        else
        {
            if ((CMISContentStreamAllowedEnum.REQUIRED == typeDef.getContentStreamAllowed()) && (null == contentStream))
            {
                throw cmisObjectsUtils.createCmisException("Content stream for document object of " + documentTypeId + " type is required", EnumServiceException.CONSTRAINT);
            }
        }

        // FIXME: change condition below to "!typeDef.isVersionable() && (null != versioningState)" when dictionary problem will be fixed
        if (false)
        {
            throw cmisObjectsUtils.createCmisException(("Verioning for \"" + documentTypeId + "\" document type is not allowed"), EnumServiceException.CONSTRAINT);
        }

        String folderTypeId = propertiesUtil.getProperty(parentNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        CMISTypeDefinition folderTypeDefinition = cmisDictionaryService.findType(folderTypeId);
        if ((null != folderTypeDefinition.getAllowedTargetTypes()) && !folderTypeDefinition.getAllowedTargetTypes().isEmpty()
                && !folderTypeDefinition.getAllowedTargetTypes().contains(typeDef))
        {
            throw cmisObjectsUtils.createCmisException(("Children of \"" + documentTypeId + "\" type are not allowed for specified folder"), EnumServiceException.CONSTRAINT);
        }

        String result = (String) propertiesMap.get(CMISDictionaryModel.PROP_NAME);
        if (null == result)
        {
            throw cmisObjectsUtils.createCmisException("Name property not found", EnumServiceException.INVALID_ARGUMENT);
        }
        propertiesUtil.checkProperty(null, typeDef, CMISDictionaryModel.PROP_NAME, result, (EnumVersioningState.CHECKEDOUT == versioningState));

        return result;
    }

    private void changeObjectParentAssociation(NodeRef objectNodeRef, NodeRef targetFolderNodeRef, NodeRef sourceFolderNodeReference) throws CmisException
    {
        if (!cmisObjectsUtils.removeObject(objectNodeRef, sourceFolderNodeReference) || !cmisObjectsUtils.addObjectToFolder(objectNodeRef, targetFolderNodeRef))
        {
            determineException(cmisObjectsUtils.getLastOperationException());
        }
    }

    private NodeRef resolvePathInfo(NodeRef rootNodeRef, String folderPath) throws CmisException
    {
        Pattern pathMatchingPattern = Pattern.compile(FOLDER_PATH_MATCHING_PATTERN);
        Matcher matcher = pathMatchingPattern.matcher(folderPath);
        if (!matcher.matches())
        {
            throw cmisObjectsUtils
                    .createCmisException(
                            "Folder path is invalid. Folder Path should be started with '/' (point of Root Folder) and containing several Path Elements (Folder Names) separated by '/'-symbol and containing no '/'-symbols in the end. Folder Path can't be resolved correctly",
                            EnumServiceException.INVALID_ARGUMENT);
        }
        NodeRef result = null;
        folderPath = folderPath.substring(1);
        if ("".equals(folderPath))
        {
            result = rootNodeRef;
        }
        else
        {
            FileInfo fileInfo = null;
            try
            {
                List<String> splitedPath = Arrays.asList(folderPath.split("/"));
                fileInfo = fileFolderService.resolveNamePath(rootNodeRef, splitedPath);
            }
            catch (FileNotFoundException e)
            {
            }
            result = fileInfo != null ? fileInfo.getNodeRef() : null;
        }
        return result;
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
    
    public void deleteAllVersions(String repositoryId, String versionSeriesId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef documentNodeRef = cmisObjectsUtils.getIdentifierInstance(versionSeriesId, AlfrescoObjectType.DOCUMENT_OBJECT);
        NodeRef workingCopyRef = (cmisObjectsUtils.isWorkingCopy(documentNodeRef)) ? (documentNodeRef) : (checkOutCheckInService.getWorkingCopy(documentNodeRef));
        if ((null != workingCopyRef) && cmisObjectsUtils.isWorkingCopy(workingCopyRef))
        {
            documentNodeRef = checkOutCheckInService.cancelCheckout(workingCopyRef);
        }

        versionService.deleteVersionHistory(documentNodeRef);
    }

    private ContentReader safeGetContentReader(NodeRef objectNodeReference) throws CmisException
    {
        ContentReader reader = fileFolderService.getReader(objectNodeReference);
        if (reader == null)
        {
            throw cmisObjectsUtils.createCmisException("The specified Document has no Content Stream", EnumServiceException.CONSTRAINT);
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
            throw cmisObjectsUtils.createCmisException("Unfiling is not suppoerted. Each Document must have existent parent Folder", EnumServiceException.CONSTRAINT, e);
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

    private void checkUnfilingIsNotRequested(EnumUnfileObject unfileNonfolderObjects) throws CmisException
    {
        if (unfileNonfolderObjects == EnumUnfileObject.UNFILE)
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

    private void determineException(Throwable lastException) throws CmisException
    {
        if (lastException instanceof AccessDeniedException)
        {
            throw cmisObjectsUtils.createCmisException(lastException.toString(), EnumServiceException.PERMISSION_DENIED);
        }

        throw cmisObjectsUtils.createCmisException("Couldn't to relocate multi-filed Object", EnumServiceException.UPDATE_CONFLICT);
    }
}
