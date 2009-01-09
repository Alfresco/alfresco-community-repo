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

import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for Multi-Filing service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "MultiFilingServicePort", serviceName = "MultiFilingService", portName = "MultiFilingServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.MultiFilingServicePort")
public class DMMultiFilingServicePort extends DMAbstractServicePort implements MultiFilingServicePort
{
    /**
     * Adds an existing non-folder, fileable object to a folder.
     * 
     * @param repositoryId Repository Id
     * @param objectId object Id to be added to a folder
     * @param folderId folder Id to which the object is added
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws FolderNotValidException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void addObjectToFolder(String repositoryId, String objectId, String folderId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, FolderNotValidException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);
        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier();
        NodeRef parentFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();

        // TODO: check for allowed child object types

        cmisObjectsUtils.addObjectToFolder(objectNodeRef, parentFolderNodeRef);
    }

    /**
     * Removes a non-folder child object from a folder or from all folders. This does not delete the object and does not change the ID of the object.
     * 
     * @param repositoryId repository Id
     * @param objectId The object to be removed from a folder
     * @param folderId The folder to be removed from.
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
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, FolderNotValidException, OperationNotSupportedException, NotInFolderException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeReference = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier();
        NodeRef folderNodeReference = checkAndReceiveFolderIdentifier(folderId);

        assertExistFolder(folderNodeReference);
        checkObjectChildParentRelationships(objectNodeReference, folderNodeReference);

        if (!cmisObjectsUtils.removeObject(objectNodeReference, folderNodeReference))
        {
            throw new NotInFolderException("The specified Object is not child of the specified Folder Object");
        }
    }

    private NodeRef checkAndReceiveFolderIdentifier(String folderIdentifier) throws OperationNotSupportedException
    {
        try
        {
            return cmisObjectsUtils.getIdentifierInstance(folderIdentifier, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();
        }
        catch (Throwable e)
        {
            throw new OperationNotSupportedException("Unfiling is not supported. An Object can't be deleted from all Folders");
        }
    }

    private void checkObjectChildParentRelationships(NodeRef objectNodeReference, NodeRef folderNodeReference) throws OperationNotSupportedException
    {
        if (cmisObjectsUtils.isPrimaryObjectParent(folderNodeReference, objectNodeReference))
        {
            throw new OperationNotSupportedException("Unfiling is not supported. Use deleteObjectService instead");
        }
    }
}
