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

import java.math.BigInteger;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete;
import org.alfresco.service.cmr.repository.ContentReader;
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

    public CreateDocumentResponse createDocument(CreateDocument parameters) throws RuntimeException, InvalidArgumentException, TypeNotFoundException, StorageException,
            ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, StreamNotSupportedException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public String createFolder(String repositoryId, String typeId, PropertiesType properties, String folderId) throws RuntimeException, InvalidArgumentException,
            TypeNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public String createPolicy(String repositoryId, String typeId, PropertiesType properties, String folderId) throws RuntimeException, InvalidArgumentException,
            TypeNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public String createRelationship(String repositoryId, String typeId, PropertiesType properties, String sourceObjectId, String targetObjectId) throws RuntimeException,
            InvalidArgumentException, TypeNotFoundException, ObjectNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException,
            PermissionDeniedException
    {
        return null;
    }

    public void deleteContentStream(String repositoryId, String documentId) throws RuntimeException, InvalidArgumentException, VersioningException, ObjectNotFoundException,
            StorageException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, StreamNotSupportedException, PermissionDeniedException
    {
    }

    public void deleteObject(String repositoryId, String objectId) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException,
            OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
    }

    public FailedToDelete deleteTree(String repositoryId, String folderId, UnfileNonfolderObjectsEnum unfileNonfolderObjects, Boolean continueOnFailure) throws RuntimeException,
            InvalidArgumentException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public AllowableActionsType getAllowableActions(String repositoryId, String objectId, String asUser) throws RuntimeException, InvalidArgumentException,
            ObjectNotFoundException, OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        return null;
    }

    public GetContentStreamResponse getContentStream(GetContentStream parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, StorageException,
            OperationNotSupportedException, UpdateConflictException, StreamNotSupportedException, OffsetException, PermissionDeniedException
    {
        NodeRef nodeRef = getNodeRefFromOID(parameters.getDocumentId());

        if (!nodeService.exists(nodeRef))
        {
            // TODO: error code
            throw new ObjectNotFoundException("Invalid document OID", ExceptionUtils.createBasicFault(null, "Invalid document OID"));
        }

        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();

        if (cmisMapping.isValidCmisDocument(cmisMapping.getCmisType(nodeService.getType(nodeRef))) == false)
        {
            // TODO: error code
            throw new StreamNotSupportedException("Stream not supported for this type of node", ExceptionUtils.createBasicFault(null, "Stream not supported for this type of node"));
        }

        ContentReader reader = fileFolderService.getReader(nodeRef);

        GetContentStreamResponse response = new GetContentStreamResponse();
        response.setContentStream(new ContentStreamType());
        ContentStreamType contentStream = response.getContentStream();
        contentStream.setLength(BigInteger.valueOf(reader.getSize()));
        contentStream.setMimeType(reader.getMimetype());
        String filename = (String) cmisPropertyService.getProperty(nodeRef, CMISMapping.PROP_NAME);
        contentStream.setFilename(filename);
        contentStream.setStream(new DataHandler(new ContentReaderDataSource(reader, filename)));

        return response;
    }

    public GetPropertiesResponse getProperties(GetProperties parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, FilterNotValidException,
            OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        PropertyFilter propertyFilter = new PropertyFilter(parameters.getFilter());

        NodeRef nodeRef = getNodeRefFromOID(parameters.getObjectId());

        if (nodeService.exists(nodeRef) == false)
        {
            // TODO: error code
            throw new ObjectNotFoundException("Object not found", ExceptionUtils.createBasicFault(null, "Object not found"));
        }

        QName typeQName = nodeService.getType(nodeRef);

        GetPropertiesResponse response = new GetPropertiesResponse();
        response.setObject(new ObjectType());

        if (cmisDictionaryService.getCMISMapping().isValidCmisFolder(typeQName))
        {
            ObjectType folderObject = response.getObject();
            folderObject.setProperties(getPropertiesType(nodeRef, propertyFilter));
// TODO:           folderObject.setAllowableActions(getAllowableActionsType(nodeRef));
        }
        else
        {
            VersionEnum neededVersion = parameters.getReturnVersion();

            if (neededVersion != null)
            {
                if (neededVersion.equals(VersionEnum.LATEST))
                {
                    nodeRef = getLatestVersionNodeRef(nodeRef, false);
                }
                else if (neededVersion.equals(VersionEnum.LATEST_MAJOR))
                {
                    nodeRef = getLatestVersionNodeRef(nodeRef, true);
                }
            }

            ObjectType documentObject = response.getObject();
            documentObject.setProperties(getPropertiesType(nodeRef, propertyFilter));
// TODO:           documentObject.setAllowableActions(getAllowableActionsType(nodeRef));
        }

        return response;
    }

    public void moveObject(String repositoryId, String objectId, String targetFolderId, String sourceFolderId) throws RuntimeException, InvalidArgumentException,
            ObjectNotFoundException, NotInFolderException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException,
            PermissionDeniedException
    {
    }

    public SetContentStreamResponse setContentStream(SetContentStream parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, StorageException,
            ContentAlreadyExistsException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, StreamNotSupportedException,
            PermissionDeniedException
    {
        return null;
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, PropertiesType properties) throws RuntimeException, InvalidArgumentException,
            ObjectNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
    }

}
