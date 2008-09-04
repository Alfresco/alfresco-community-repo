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
import java.math.BigInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "ObjectServicePort", serviceName = "ObjectService", portName = "ObjectServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.ObjectServicePort")
public class DMObjectServicePort extends DMAbstractServicePort implements ObjectServicePort
{
    private static final Log log = LogFactory.getLog("org.alfresco.repo.cmis.ws");

    public org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete deleteTree(java.lang.String folderId, org.alfresco.repo.cmis.ws.DeleteWithMultiFilingEnum unfileMultiFiledDocuments,
            java.lang.Boolean continueOnFailure) throws RuntimeException, ConcurrencyException, InvalidArgumentException, OperationNotSupportedException, FolderNotValidException,
            PermissionDeniedException
    {
        System.out.println(folderId);
        System.out.println(unfileMultiFiledDocuments);
        System.out.println(continueOnFailure);
        try
        {
            org.alfresco.repo.cmis.ws.DeleteTreeResponse.FailedToDelete _return = null;
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetAllowableActionsResponse.AllowableActionCollection getAllowableActions(java.lang.String objectId, java.lang.String asUser)
            throws RuntimeException, ConcurrencyException, InvalidArgumentException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(objectId);
        System.out.println(asUser);
        try
        {
            org.alfresco.repo.cmis.ws.GetAllowableActionsResponse.AllowableActionCollection _return = null;
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void moveObject(java.lang.String objectId, java.lang.String folderId, java.lang.String sourceFolderId) throws RuntimeException, ConcurrencyException,
            InvalidArgumentException, ObjectNotFoundException, NotInFolderException, ConstraintViolationException, OperationNotSupportedException, FolderNotValidException,
            PermissionDeniedException
    {
        System.out.println(objectId);
        System.out.println(folderId);
        System.out.println(sourceFolderId);
        try
        {
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new NotInFolderException("NotInFolderException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void deleteContentStream(java.lang.String documentId) throws RuntimeException, ConcurrencyException, InvalidArgumentException, ObjectNotFoundException,
            StorageException, ConstraintViolationException, OperationNotSupportedException, StreamNotSupportedException, PermissionDeniedException
    {
        System.out.println(documentId);
        try
        {
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new StorageException("StorageException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new StreamNotSupportedException("StreamNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public java.lang.String createFolder(java.lang.String typeId, org.alfresco.repo.cmis.ws.FolderObjectType propertyCollection, java.lang.String folderId) throws RuntimeException,
            ConcurrencyException, InvalidArgumentException, TypeNotFoundException, ConstraintViolationException, OperationNotSupportedException, FolderNotValidException,
            PermissionDeniedException
    {
        System.out.println(typeId);
        System.out.println(propertyCollection);
        System.out.println(folderId);
        try
        {
            java.lang.String _return = "";
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new TypeNotFoundException("TypeNotFoundException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void updateProperties(java.lang.String objectId, java.lang.String changeToken, org.alfresco.repo.cmis.ws.DocumentFolderOrRelationshipObjectType object)
            throws RuntimeException, ConcurrencyException, InvalidArgumentException, ObjectNotFoundException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(objectId);
        System.out.println(changeToken);
        System.out.println(object);
        try
        {
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.CreateDocumentResponse createDocument(CreateDocument parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            TypeNotFoundException, StorageException, ConstraintViolationException, OperationNotSupportedException, StreamNotSupportedException, FolderNotValidException,
            PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.CreateDocumentResponse _return = null;
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new TypeNotFoundException("TypeNotFoundException...");
        // throw new StorageException("StorageException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new StreamNotSupportedException("StreamNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public byte[] getContentStream(java.lang.String documentId, java.math.BigInteger offset, java.math.BigInteger length) throws RuntimeException, ConcurrencyException,
            InvalidArgumentException, ObjectNotFoundException, StorageException, OperationNotSupportedException, StreamNotSupportedException, OffsetException,
            PermissionDeniedException
    {
        if (offset == null)
        {
            offset = new BigInteger("0");
        }
        if (length == null)
        {
            length = new BigInteger("-1");
        }


        NodeRef nodeRef = OIDUtils.OIDtoNodeRef(documentId);

        if (nodeRef == null)
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Invalid object ID");
            throw new InvalidArgumentException("Invalid object ID", basicFault);
        }

        if (!nodeService.exists(nodeRef))
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Invalid document OID");
            throw new ObjectNotFoundException("Invalid document OID", basicFault);
        }

        if (isFolderType(nodeRef))
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Stream not supported for this type of node");
            throw new StreamNotSupportedException("Stream not supported for this type of node", basicFault);
        }

        ContentReader contentReader = null;

        if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_LINK))
        {
            NodeRef destRef = (NodeRef) nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
            if (nodeService.exists(destRef))
            {
                contentReader = fileFolderService.getReader(destRef);
            }
            else
            {
                throw new ObjectNotFoundException("Missing link destination");
            }
        }
        else
        {
            contentReader = fileFolderService.getReader(nodeRef);
        }

        if (length.intValue() == -1)
        {
            length = new BigInteger(String.valueOf(contentReader.getSize()));
        }

        byte[] buffer = new byte[length.intValue()];

        try
        {
            InputStream inputStream = contentReader.getContentInputStream();
            inputStream.skip(offset.intValue());
            int resultLength = inputStream.read(buffer, 0, length.intValue());

            if (resultLength == -1)
            {
                return new byte[0];
            }

            byte[] result = new byte[resultLength];
            for (int i = 0; i < resultLength; i++)
            {
                result[i] = buffer[i];
            }
            return result;
        }
        catch (IndexOutOfBoundsException e)
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, e.getMessage());
            throw new OffsetException(e.getMessage(), basicFault, e);
        }
        catch (IOException e)
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, e.getMessage());
            throw new RuntimeException(e.getMessage(), basicFault, e);
        }
    }

    public java.lang.String createRelationship(java.lang.String typeId, org.alfresco.repo.cmis.ws.RelationshipObjectType propertyCollection, java.lang.String sourceObjectId,
            java.lang.String targetObjectId) throws RuntimeException, ConcurrencyException, InvalidArgumentException, TypeNotFoundException, ObjectNotFoundException,
            ConstraintViolationException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(typeId);
        System.out.println(propertyCollection);
        System.out.println(sourceObjectId);
        System.out.println(targetObjectId);
        try
        {
            java.lang.String _return = "";
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new TypeNotFoundException("TypeNotFoundException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.SetContentStreamResponse setContentStream(SetContentStream parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            AlreadyExistsException, ObjectNotFoundException, StorageException, ConstraintViolationException, OperationNotSupportedException, StreamNotSupportedException,
            PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.SetContentStreamResponse _return = null;
            return _return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new AlreadyExistsException("AlreadyExistsException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new StorageException("StorageException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new StreamNotSupportedException("StreamNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void deleteObject(java.lang.String objectId) throws RuntimeException, ConcurrencyException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(objectId);
        try
        {
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw new java.lang.RuntimeException(ex);
        }
        // throw new RuntimeException("RuntimeException...");
        // throw new ConcurrencyException("ConcurrencyException...");
        // throw new InvalidArgumentException("InvalidArgumentException...");
        // throw new ObjectNotFoundException("ObjectNotFoundException...");
        // throw new ConstraintViolationException("ConstraintViolationException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetPropertiesResponse getProperties(GetProperties parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            ObjectNotFoundException, FilterNotValidException, OperationNotSupportedException, PermissionDeniedException
    {
        GetPropertiesResponse response = new GetPropertiesResponse();
        response.setObject(new DocumentFolderOrRelationshipObjectType());

        PropertyFilter propertyFilter = new PropertyFilter(parameters.getFilter());

        NodeRef nodeRef = OIDUtils.OIDtoNodeRef(parameters.getObjectId());

        if (nodeRef != null)
        {
            if (nodeService.exists(nodeRef) == false)
            {
                // TODO: error code
                BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Object not found");
                throw new ObjectNotFoundException("Object not found", basicFault);
            }

            if (isFolderType(nodeRef))
            {
                setFolderObjectTypeProperties(nodeRef, response.getObject(), propertyFilter);
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

                setDocumentObjectTypeProperties(nodeRef, response.getObject(), propertyFilter);
            }
        }
        else
        {
            AssociationRef associationRef = OIDUtils.OIDtoAssocRef(parameters.getObjectId());

            if (associationRef != null)
            {
                if (exists(associationRef) == false)
                {
                    // TODO: error code
                    BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Object not found");
                    throw new ObjectNotFoundException("Object not found", basicFault);
                }

                setRelationshipObjectTypeProperties(associationRef, response.getObject(), propertyFilter);
            }
            else
            {
                // TODO: error code
                BasicFault basicFault = ExceptionUtils.createBasicFault(null, "Invalid object ID");
                throw new InvalidArgumentException("Invalid object ID", basicFault);
            }
        }

        return response;
    }

}
