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

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Port for object service
 *
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "ObjectServicePort", serviceName = "ObjectService", portName = "ObjectServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.ObjectServicePort")
public class DMObjectServicePort extends DMAbstractServicePort implements ObjectServicePort
{

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

    public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream,
            EnumVersioningState versioningState) throws PermissionDeniedException, UpdateConflictException, StorageException, StreamNotSupportedException, FolderNotValidException,
            OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {

        if (descriptorService.getServerDescriptor().getId().equals(repositoryId) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        Map<String, Serializable> propertiesMap = getPropertiesMap(properties);
        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();
        CMISTypeId cmisTypeId = cmisMapping.getCmisTypeId(typeId);

        if (cmisMapping.getCmisTypeId(typeId).equals(CMISMapping.DOCUMENT_TYPE_ID) == false)
        {
            throw new ConstraintViolationException("Invalid document type");
        }

        NodeRef parentNodeRef = getNodeRefFromOID(folderId);

        if (!nodeService.exists(parentNodeRef))
        {
            throw new FolderNotValidException("Invalid parent OID");
        }

        String documentName = (String) propertiesMap.get(CMISMapping.PROP_NAME);
        if (documentName == null)
        {
            throw new InvalidArgumentException("Name property not found");
        }

        NodeRef newDocumentNodeRef = fileFolderService.create(parentNodeRef, documentName, cmisTypeId.getQName()).getNodeRef();
        ContentWriter writer = fileFolderService.getWriter(newDocumentNodeRef);
        String mimeType = (String) propertiesMap.get(CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE);
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
            e.printStackTrace();
            throw new ConstraintViolationException("", e.getCause());
        }
        writer.putContent(inputstream);

        if (versioningState == null)
        {
            versioningState = EnumVersioningState.MAJOR;
        }

        cmisPropertyService.setProperties(newDocumentNodeRef, propertiesMap);

        switch (versioningState)
        {
        case CHECKEDOUT:
            //TODO: maybe this must be done by VersioningService
            cmisPropertyService.setProperty(newDocumentNodeRef, CMISMapping.PROP_IS_VERSION_SERIES_CHECKED_OUT, Boolean.TRUE);
            break;
        case MAJOR:
            cmisPropertyService.setProperty(newDocumentNodeRef, CMISMapping.PROP_IS_MAJOR_VERSION, Boolean.FALSE);
            break;
        case MINOR:
            cmisPropertyService.setProperty(newDocumentNodeRef, CMISMapping.PROP_IS_MAJOR_VERSION, Boolean.TRUE);
            break;

        }

        return newDocumentNodeRef.toString();
    }

      public String createFolder(String repositoryId, String typeId, CmisPropertiesType properties, String folderId) throws PermissionDeniedException, UpdateConflictException,
            FolderNotValidException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        if (descriptorService.getServerDescriptor().getId().equals(repositoryId) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        NodeRef folderNodeRef = getNodeRefFromOID(folderId);
        assertExistFolder(folderNodeRef);

        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();

        CMISTypeId cmisTypeId = cmisMapping.getCmisTypeId(typeId);

        Map<String, Serializable> propertiesMap = getPropertiesMap(properties);

        String name = (String) propertiesMap.get(CMISMapping.PROP_NAME);
        if (name == null)
        {
            throw new InvalidArgumentException("Name property not found");
        }

        try
        {
            NodeRef newFolderNodeRef = fileFolderService.create(folderNodeRef, name, cmisTypeId.getQName()).getNodeRef();
            cmisPropertyService.setProperties(newFolderNodeRef, propertiesMap);
            return newFolderNodeRef.toString();
        }
        catch (FileExistsException e)
        {
            throw new UpdateConflictException("Folder already exists");
        }
    }

    public String createPolicy(String repositoryId, String typeId, CmisPropertiesType properties, String folderId) throws PermissionDeniedException, UpdateConflictException,
            FolderNotValidException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        return null;
    }

    public String createRelationship(String repositoryId, String typeId, CmisPropertiesType properties, String sourceObjectId, String targetObjectId)
            throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException,
            RuntimeException, ConstraintViolationException
    {
        return null;
    }

    public void deleteContentStream(String repositoryId, String documentId) throws PermissionDeniedException, UpdateConflictException, StorageException,
            StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, VersioningException, InvalidArgumentException, RuntimeException,
            ConstraintViolationException
    {
    }

    public void deleteObject(String repositoryId, String objectId) throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException,
            OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
    }

    public FailedToDelete deleteTree(String repositoryId, String folderId, EnumUnfileNonfolderObjects unfileNonfolderObjects, Boolean continueOnFailure)
            throws PermissionDeniedException, UpdateConflictException, FolderNotValidException, OperationNotSupportedException, InvalidArgumentException, RuntimeException,
            ConstraintViolationException
    {
        return null;
    }

    public CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId) throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException,
            OperationNotSupportedException, InvalidArgumentException, RuntimeException
    {
        return null;
    }

    public CmisContentStreamType getContentStream(String repositoryId, String documentId) throws PermissionDeniedException, UpdateConflictException, StorageException,
            StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, OffsetException
    {
        NodeRef nodeRef = getNodeRefFromOID(documentId);

        if (!nodeService.exists(nodeRef))
        {
            throw new ObjectNotFoundException("Invalid document OID");
        }

        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();

        if (cmisMapping.isValidCmisDocument(cmisMapping.getCmisType(nodeService.getType(nodeRef))) == false)
        {
            throw new StreamNotSupportedException("Stream not supported for this type of node");
        }

        ContentReader reader = fileFolderService.getReader(nodeRef);

        CmisContentStreamType response = new CmisContentStreamType();
        response.setLength(BigInteger.valueOf(reader.getSize()));
        response.setMimeType(reader.getMimetype());
        String filename = (String) cmisPropertyService.getProperty(nodeRef, CMISMapping.PROP_NAME);
        response.setFilename(filename);
        response.setStream(new DataHandler(new ContentReaderDataSource(reader, filename)));

        return response;
    }

    public void moveObject(String repositoryId, String objectId, String targetFolderId, String sourceFolderId) throws PermissionDeniedException, UpdateConflictException,
            ObjectNotFoundException, FolderNotValidException, OperationNotSupportedException, NotInFolderException, InvalidArgumentException, RuntimeException,
            ConstraintViolationException
    {
    }

    public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream) throws PermissionDeniedException,
            UpdateConflictException, StorageException, StreamNotSupportedException, ObjectNotFoundException, OperationNotSupportedException, ContentAlreadyExistsException,
            InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties) throws PermissionDeniedException,
            UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
    }

    public GetPropertiesResponse getProperties(GetProperties parameters) throws PermissionDeniedException, UpdateConflictException, FilterNotValidException,
            ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException
    {
        if (descriptorService.getServerDescriptor().getId().equals(parameters.getRepositoryId()) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        NodeRef nodeRef = getNodeRefFromOID(parameters.getObjectId());

        if (nodeService.exists(nodeRef) == false)
        {
            throw new ObjectNotFoundException("Object not found");
        }

        QName typeQName = nodeService.getType(nodeRef);

        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();

        if (cmisMapping.isValidCmisDocument((cmisMapping.getCmisType(typeQName))))
        {
            if (parameters.getReturnVersion() != null)
            {
                EnumReturnVersion neededVersion = parameters.getReturnVersion().getValue();

                if (neededVersion.equals(EnumReturnVersion.LATEST))
                {
                    nodeRef = getLatestVersionNodeRef(nodeRef, false);
                }
                else if (neededVersion.equals(EnumReturnVersion.LATESTMAJOR))
                {
                    nodeRef = getLatestVersionNodeRef(nodeRef, true);
                }
            }
        }

        GetPropertiesResponse response = new GetPropertiesResponse();
        response.setObject(new CmisObjectType());
        CmisObjectType object = response.getObject();
        object.setProperties(getPropertiesType(nodeRef, propertyFilter));

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

}
