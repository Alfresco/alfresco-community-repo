/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISConstraintException;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISVersioningStateEnum;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
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
    private static final Map<EnumVersioningState, CMISVersioningStateEnum> VERSIONING_STATE_ENUM_MAPPING;

    private DictionaryService dictionaryService;

    private FileTypeIconRetriever iconRetriever;

    static
    {
        VERSIONING_STATE_ENUM_MAPPING = new HashMap<EnumVersioningState, CMISVersioningStateEnum>(7);
        VERSIONING_STATE_ENUM_MAPPING.put(EnumVersioningState.NONE, CMISVersioningStateEnum.NONE);
        VERSIONING_STATE_ENUM_MAPPING.put(EnumVersioningState.CHECKEDOUT, CMISVersioningStateEnum.CHECKED_OUT);
        VERSIONING_STATE_ENUM_MAPPING.put(EnumVersioningState.MAJOR, CMISVersioningStateEnum.MAJOR);
        VERSIONING_STATE_ENUM_MAPPING.put(EnumVersioningState.MINOR, CMISVersioningStateEnum.MINOR);
    }

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
        try
        {
            NodeRef parentNodeRef = cmisService.getFolder(folderId);
            Map<String, Serializable> propertiesMap = propertiesUtil.getPropertiesMap(properties);
            String typeId = extractAndAssertTypeId(propertiesMap);
            CMISTypeDefinition typeDef = cmisService.getTypeDefinition(typeId);
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
                InputStream inputstream = null;
                try
                {
                    inputstream = contentStream.getStream().getInputStream();
                }
                catch (IOException e)
                {
                    throw ExceptionUtil.createCmisException(e.getMessage(), EnumServiceException.RUNTIME, e);
                }
                writer.putContent(inputstream);
            }
            PropertyFilter propertyFilter = createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_NAME, CMISDictionaryModel.PROP_OBJECT_TYPE_ID }));
            appendDataToDocument(newDocumentNodeRef, properties, versioningState, policies, addACEs, removeACEs, objectId, propertyFilter);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        if (null == properties)
        {
            throw ExceptionUtil.createCmisException("Properties input parameter is Mandatory", EnumServiceException.INVALID_ARGUMENT);
        }
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
        try
        {
            NodeRef folderNodeRef = cmisService.getFolder(folderId);
            NodeRef sourceNodeRef = cmisService.getReadableObject(sourceId, NodeRef.class);
            String name = propertiesUtil.getCmisPropertyValue(properties, CMISDictionaryModel.PROP_NAME, null);
            if (name == null)
            {
                name = propertiesUtil.getProperty(sourceNodeRef, CMISDictionaryModel.PROP_NAME, null);
            }
            NodeRef newDocumentNodeRef;
            try
            {
                newDocumentNodeRef = fileFolderService.copy(sourceNodeRef, folderNodeRef, name).getNodeRef();
            }
            catch (FileExistsException e)
            {
                throw ExceptionUtil.createCmisException("Document already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
            }
            catch (FileNotFoundException e)
            {
                throw ExceptionUtil.createCmisException("Source document not found", EnumServiceException.INVALID_ARGUMENT);
            }
            PropertyFilter propertyFilter = createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_OBJECT_TYPE_ID }));
            appendDataToDocument(newDocumentNodeRef, properties, versioningState, policies, addACEs, removeACEs, objectId, propertyFilter);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        checkRepositoryId(repositoryId);
        try
        {
            NodeRef folderNodeRef = cmisService.getFolder(folderId);
            Map<String, Serializable> propertiesMap = propertiesUtil.getPropertiesMap(properties);
            String typeId = extractAndAssertTypeId(propertiesMap);
            CMISTypeDefinition type = cmisService.getTypeDefinition(typeId);
            if (type == null || type.getTypeId() == null || type.getTypeId().getScope() != CMISScope.FOLDER)
            {
                throw ExceptionUtil.createCmisException("The typeID is not an Object-Type whose baseType is 'Folder': " + typeId, EnumServiceException.CONSTRAINT);
            }

            String name = propertiesUtil.getCmisPropertyValue(properties, CMISDictionaryModel.PROP_NAME, null);
            if (null == name)
            {
                throw ExceptionUtil.createCmisException("Name property not found", EnumServiceException.INVALID_ARGUMENT);
            }

            try
            {
                NodeRef newFolderNodeRef = fileFolderService.create(folderNodeRef, name, type.getTypeId().getQName()).getNodeRef();
                propertiesUtil.setProperties(newFolderNodeRef, properties, createPropertyFilter(createIgnoringFilter(new String[] { CMISDictionaryModel.PROP_NAME,
                        CMISDictionaryModel.PROP_OBJECT_TYPE_ID })));
                applyAclCarefully(newFolderNodeRef, addACEs, removeACEs, EnumACLPropagation.PROPAGATE, policies);
                objectId.value = propertiesUtil.getProperty(newFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_ID, null);
            }
            catch (FileExistsException e)
            {
                throw ExceptionUtil.createCmisException("Folder already exists", EnumServiceException.CONTENT_ALREADY_EXISTS);
            }
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
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
        checkRepositoryId(repositoryId);
        try
        {
            objectId.value = cmisService.createPolicy(propertiesUtil.getPropertiesMap(properties), folderId, policies);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        // TODO: process Policies

        Map<String, Serializable> propertiesMap = propertiesUtil.getPropertiesMap(properties);
        String sourceObjectId = (String) propertiesMap.get(CMISDictionaryModel.PROP_SOURCE_ID);
        String targetObjectId = (String) propertiesMap.get(CMISDictionaryModel.PROP_TARGET_ID);

        checkRepositoryId(repositoryId);

        try
        {
            NodeRef sourceNodeRef = cmisService.getObject(sourceObjectId, NodeRef.class, true, false, false);
            NodeRef targetNodeRef = cmisService.getObject(targetObjectId, NodeRef.class, true, false, false);

            String typeId = (String) propertiesMap.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);

            CMISTypeDefinition relationshipType = cmisService.getTypeDefinition(typeId);
            if (relationshipType == null || relationshipType.getTypeId() == null || relationshipType.getTypeId().getScope() != CMISScope.RELATIONSHIP)
            {
                throw ExceptionUtil.createCmisException(typeId, EnumServiceException.INVALID_ARGUMENT);
            }

            CMISTypeDefinition sourceType = cmisService.getTypeDefinition(sourceNodeRef);
            CMISTypeDefinition targetType = cmisService.getTypeDefinition(targetNodeRef);

            QName relationshipTypeQName = relationshipType.getTypeId().getQName();
            AssociationDefinition associationDef = dictionaryService.getAssociation(relationshipTypeQName);
            if (associationDef != null)
            {
                if (!dictionaryService.isSubClass(nodeService.getType(sourceNodeRef), associationDef.getSourceClass().getName()))
                {
                    throw ExceptionUtil.createCmisException("Source object type isn't allowed as source type", EnumServiceException.CONSTRAINT);
                }
                if (!dictionaryService.isSubClass(nodeService.getType(targetNodeRef), associationDef.getTargetClass().getName()))
                {
                    throw ExceptionUtil.createCmisException("Target object type isn't allowed as target type", EnumServiceException.CONSTRAINT);
                }

                // Check ACL arguments
                if (addACEs != null && !addACEs.getPermission().isEmpty() || removeACEs != null && !removeACEs.getPermission().isEmpty())
                {
                    throw ExceptionUtil.createCmisException("ACLs are not supported for type: " + relationshipType.getDisplayName(), EnumServiceException.CONSTRAINT);
                }
                
                AssociationRef assocRef = nodeService.createAssociation(sourceNodeRef, targetNodeRef, relationshipTypeQName);
				String createdId = (String) cmisService.getProperty(assocRef, CMISDictionaryModel.PROP_OBJECT_ID);

                // Try applying policies
                applyPolicies(createdId, policies);

                objectId.value = createdId;
            }
            else
            {
                throw ExceptionUtil.createCmisException((relationshipType.getTypeId().getQName() + " Relationship type not found"), EnumServiceException.INVALID_ARGUMENT);
            }
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Deletes the content-stream of the specified document. This does not delete properties. If there are other versions this does not affect them, their properties or content.
     * This does not change the ID of the document.
     * 
     * @param repositoryId repository Id
     * @param objectId document Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT, STORAGE,
     *         UPDATE_CONFLICT, VERSIONING)
     */
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, Holder<CmisExtensionType> extension) throws CmisException
    {
        // TODO: Process changeToken
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.deleteContentStream(objectId.value);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.deleteObject(objectId, allVersions == null || allVersions);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
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
        // TODO: Process allVersions
        checkRepositoryId(repositoryId);
        checkUnfilingIsNotRequested(unfileObject);

        List<String> failedToDelete;
        try
        {
            failedToDelete = cmisService.deleteTree(folderId, continueOnFailure == null ? false : continueOnFailure, unfileObject != EnumUnfileObject.DELETE, allVersions == null || allVersions);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        FailedToDelete response = new FailedToDelete();
        response.getObjectIds().addAll(failedToDelete);
        return response;
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
    // FIXME: createCmisObject instead of manual set-upping
    public CmisObjectType getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeACL, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        try
        {
            Object object = cmisService.getReadableObject(objectId, Object.class);
            PropertyFilter propertyFilter = createPropertyFilter(filter);
            CmisObjectType cmisObject = createCmisObject(object, propertyFilter, includeRelationships,
                    includeAllowableActions, renditionFilter);

            Object versionSeries = cmisService.getVersionSeries(objectId, Object.class, false);
            boolean includeAcl = (null != includeACL) ? (includeACL.booleanValue()) : (false);
            if (includeAcl && (versionSeries instanceof NodeRef))
            {
                appendWithAce((NodeRef) versionSeries, cmisObject);
            }

            return cmisObject;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        NodeRef objectNodeRef = resolvePathInfo(path);
        if (null == objectNodeRef)
        {
            throw ExceptionUtil.createCmisException("Path to Folder was not specified or Folder Path is invalid", EnumServiceException.INVALID_ARGUMENT);
        }
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        CmisObjectType object = createCmisObject(objectNodeRef, propertyFilter, includeRelationships,
                includeAllowableActions, renditionFilter);
        boolean includeAcl = (null != includeACL) ? (includeACL.booleanValue()) : (false);
        if (includeAcl)
        {
            appendWithAce(objectNodeRef, object);
        }
        // TODO: process relationships

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
        Object object;
        try
        {
            object = cmisService.getReadableObject(objectId, Object.class);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        return determineObjectAllowableActions(object);
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
        checkRepositoryId(repositoryId);
        try
        {
            NodeRef nodeRef = cmisService.getReadableObject(objectId, NodeRef.class);
            CMISTypeDefinition typeDefinition = cmisService.getTypeDefinition(nodeRef);
            if (CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDefinition.getContentStreamAllowed())
            {
                throw ExceptionUtil.createCmisException("Content stream not allowed", EnumServiceException.STREAM_NOT_SUPPORTED);
            }

            String filename = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME, null);

            ContentReaderDataSource dataSource = null;
            if (streamId != null && streamId.length() > 0)
            {
                FileTypeImageSize streamIcon = null;
                if (streamId.equals("alf:icon16"))
                {
                    streamIcon = FileTypeImageSize.Small;
                }
                else if (streamId.equals("alf:icon32"))
                {
                    streamIcon = FileTypeImageSize.Medium;
                }
                if (streamIcon != null)
                {
                    InputStream iconInputStream = iconRetriever.getIconContent(filename, streamIcon);
                    String iconMimetype = iconRetriever.getIconMimetype(filename, streamIcon);
                    if (iconInputStream != null && iconMimetype != null)
                    {
                        dataSource = new ContentReaderDataSource(iconInputStream, iconMimetype, filename, offset, length);
                    }
                }
                else
                {
                    NodeRef renditionNodeRef = new NodeRef(streamId);
                    ContentReader reader = safeGetContentReader(renditionNodeRef);
                    dataSource = new ContentReaderDataSource(reader, filename, offset, length, reader.getSize());
                }
            }
            else
            {
                ContentReader reader = safeGetContentReader(nodeRef);
                dataSource = new ContentReaderDataSource(reader, filename, offset, length, reader.getSize());
            }

            CmisContentStreamType response = new CmisContentStreamType();

            response.setFilename(filename);

            if (dataSource != null)
            {
                response.setMimeType(dataSource.getContentType());
                response.setStream(new DataHandler(dataSource));
                response.setLength(BigInteger.valueOf(dataSource.getSizeToRead()));
            }

            return response;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        try
        {
            cmisService.moveObject(objectId.value, targetFolderId, sourceFolderId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Sets (creates or replaces) the content-stream for the specified document object.
     * 
     * @param repositoryId repository Id
     * @param objectId document Id
     * @param overwriteFlag flag
     * @param contentStream content stream
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         CONTENT_ALREADY_EXISTS, STORAGE, STREAM_NOT_SUPPORTED, UPDATE_CONFLICT, VERSIONING)
     */

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag, Holder<String> changeToken, CmisContentStreamType contentStream,
            Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.setContentStream(objectId.value, null, overwriteFlag == null || overwriteFlag, contentStream.getStream().getInputStream(), contentStream.getMimeType());
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        catch (Exception e)
        {
            throw ExceptionUtil.createCmisException(e.getMessage(), EnumServiceException.RUNTIME, e);
        }
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
        checkRepositoryId(repositoryId);

        NodeRef objectNodeRef;
        try
        {
            objectNodeRef = cmisService.getObject(objectId.value, NodeRef.class, true, false, false);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        propertiesUtil.setProperties(objectNodeRef, properties, null);
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

        Object identifierInstance;
        try
        {
            identifierInstance = cmisService.getReadableObject(objectId, Object.class);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        CmisPropertiesType result = propertiesUtil.getProperties(identifierInstance, propertyFilter);

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
        checkRepositoryId(repositoryId);
        NodeRef objectNodeRef;
        try
        {
            objectNodeRef = cmisService.getReadableObject(objectId, NodeRef.class);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        List<CmisRenditionType> result = new ArrayList<CmisRenditionType>();
        List<CmisRenditionType> renditions = getRenditions(objectNodeRef, renditionFilter);
        if (renditions != null)
        {
            Cursor cursor = createCursor(renditions.size(), skipCount, maxItems);
            for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
            {
                result.add(renditions.get(index));
            }
        }
        return renditions;
    }

    private void appendDataToDocument(NodeRef targetDocumentNodeRef, CmisPropertiesType properties, EnumVersioningState versioningState, List<String> policies,
            CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, Holder<String> objectId, PropertyFilter propertyFilter) throws CMISConstraintException,
            CmisException, CMISInvalidArgumentException
    {
        propertiesUtil.setProperties(targetDocumentNodeRef, properties, propertyFilter);

        // Apply the ACL before potentially creating a PWC
        applyAclCarefully(targetDocumentNodeRef, addACEs, removeACEs, EnumACLPropagation.PROPAGATE, policies);

        if (versioningState == null)
        {
            versioningState = EnumVersioningState.MAJOR;
        }

        targetDocumentNodeRef = cmisService.applyVersioningState(targetDocumentNodeRef, VERSIONING_STATE_ENUM_MAPPING.get(versioningState));
        objectId.value = targetDocumentNodeRef.toString();
    }

    private String createIgnoringFilter(String[] propertyNames)
    {
        StringBuilder filter = new StringBuilder("");

        for (String propertyName : propertyNames)
        {
            if ((null != propertyName) && !propertyName.equals(""))
            {
                filter.append(propertyName);
                filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMITER);
            }
        }

        if (filter.length() > 0)
        {
            filter.deleteCharAt(filter.length() - 1);
        }

        return filter.toString();
    }

    private String extractAndAssertTypeId(Map<String, Serializable> propertiesMap) throws CmisException
    {
        String typeId = (String) propertiesMap.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        if ((null == typeId) || "".equals(typeId))
        {
            throw ExceptionUtil.createCmisException("Type Id property required", EnumServiceException.INVALID_ARGUMENT);
        }
        return typeId;
    }

    private String checkConstraintsAndGetName(String documentTypeId, CMISTypeDefinition typeDef, NodeRef parentNodeRef, CmisContentStreamType contentStream,
            Map<String, Serializable> propertiesMap, EnumVersioningState versioningState) throws CmisException
    {
        if ((null == typeDef) || (null == typeDef.getTypeId()))
        {
            throw ExceptionUtil.createCmisException(("Type with " + documentTypeId + "typeId was not found"), EnumServiceException.RUNTIME);
        }

        if ((CMISScope.DOCUMENT != typeDef.getTypeId().getScope()) || !typeDef.isCreatable())
        {
            throw ExceptionUtil.createCmisException(("Invalid document type \"" + documentTypeId + "\". This type is not a Creatable Document type"),
                    EnumServiceException.CONSTRAINT);
        }

        if ((null != contentStream) && CMISContentStreamAllowedEnum.NOT_ALLOWED == typeDef.getContentStreamAllowed())
        {
            throw ExceptionUtil.createCmisException(("Content stream not allowed for \"" + documentTypeId + "\" document object type"), EnumServiceException.STREAM_NOT_SUPPORTED);
        }
        else
        {
            if ((CMISContentStreamAllowedEnum.REQUIRED == typeDef.getContentStreamAllowed()) && (null == contentStream))
            {
                throw ExceptionUtil.createCmisException("Content stream for document object of " + documentTypeId + " type is required", EnumServiceException.CONSTRAINT);
            }
        }

        if (!typeDef.isVersionable() && (null != versioningState))
        {
            throw ExceptionUtil.createCmisException(("Verioning for \"" + documentTypeId + "\" document type is not allowed"), EnumServiceException.CONSTRAINT);
        }

        CMISTypeDefinition folderTypeDefinition;
        try
        {
            folderTypeDefinition = cmisService.getTypeDefinition(parentNodeRef);
        }
        catch (CMISInvalidArgumentException e)
        {
            throw ExceptionUtil.createCmisException(e.getMessage(), EnumServiceException.INVALID_ARGUMENT, e);
        }
        if ((null != folderTypeDefinition.getAllowedTargetTypes()) && !folderTypeDefinition.getAllowedTargetTypes().isEmpty()
                && !folderTypeDefinition.getAllowedTargetTypes().contains(typeDef))
        {
            throw ExceptionUtil.createCmisException(("Children of \"" + documentTypeId + "\" type are not allowed for specified folder"), EnumServiceException.CONSTRAINT);
        }

        String result = (String) propertiesMap.get(CMISDictionaryModel.PROP_NAME);
        if (null == result)
        {
            throw ExceptionUtil.createCmisException("Name property not found", EnumServiceException.INVALID_ARGUMENT);
        }
        return result;
    }

    private NodeRef resolvePathInfo(String folderPath) throws CmisException
    {
        NodeRef result = null;
        if (null != folderPath)
        {
            folderPath = folderPath.substring(1);
            if ("".equals(folderPath))
            {
                result = cmisService.getDefaultRootNodeRef();
            }
            else
            {
                FileInfo fileInfo = null;
                try
                {
                    List<String> splitedPath = Arrays.asList(folderPath.split("/"));
                    fileInfo = fileFolderService.resolveNamePath(cmisService.getDefaultRootNodeRef(), splitedPath);
                }
                catch (FileNotFoundException e)
                {
                }
                result = (null != fileInfo) ? (fileInfo.getNodeRef()) : (null);
            }
        }
        return result;
    }

    private ContentReader safeGetContentReader(NodeRef objectNodeReference) throws CmisException
    {
        ContentReader reader = fileFolderService.getReader(objectNodeReference);
        if (reader == null)
        {
            throw ExceptionUtil.createCmisException("The specified Document has no Content Stream", EnumServiceException.CONSTRAINT);
        }
        return reader;
    }

    private void checkUnfilingIsNotRequested(EnumUnfileObject unfileNonfolderObjects) throws CmisException
    {
        if (unfileNonfolderObjects == EnumUnfileObject.UNFILE)
        {
            throw ExceptionUtil.createCmisException("Unfiling is not supported", EnumServiceException.NOT_SUPPORTED);
        }
    }

    public void setFileTypeIconRetriever(FileTypeIconRetriever iconRetriever)
    {
        this.iconRetriever = iconRetriever;
    }
}
