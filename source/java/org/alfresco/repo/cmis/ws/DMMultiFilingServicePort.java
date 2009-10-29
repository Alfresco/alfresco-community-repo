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

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for Multi-Filing service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "MultiFilingServicePort", serviceName = "MultiFilingService", portName = "MultiFilingServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.MultiFilingServicePort")
public class DMMultiFilingServicePort extends DMAbstractServicePort implements MultiFilingServicePort
{
    /**
     * Adds an existing non-folder, fileable object to a folder.
     * 
     * @param repositoryId Repository Id
     * @param objectId object Id to be added to a folder
     * @param folderId folder Id to which the object is added
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT)
     */
    public void addObjectToFolder(String repositoryId, String objectId, String folderId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        NodeRef parentFolderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);

        CMISTypeDefinition objectType = cmisDictionaryService.findType(propertiesUtil.getProperty(objectNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));
        CMISTypeDefinition folderType = cmisDictionaryService.findType(propertiesUtil.getProperty(parentFolderNodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, (String) null));

        if (folderType == null)
        {
            throw cmisObjectsUtils.createCmisException("Type of the specified parent folder can't be resovled", EnumServiceException.RUNTIME);
        }

        if (!folderType.getAllowedTargetTypes().isEmpty() && !folderType.getAllowedTargetTypes().contains(objectType))
        {
            throw cmisObjectsUtils.createCmisException("The typeId of Object is not in the list of AllowedChildObjectTypeIds of the parent-folder specified by folderId",
                    EnumServiceException.CONSTRAINT);
        }
        cmisObjectsUtils.addObjectToFolder(objectNodeRef, parentFolderNodeRef);
    }

    /**
     * Removes a non-folder child object from a folder or from all folders. This does not delete the object and does not change the ID of the object.
     * 
     * @param repositoryId repository Id
     * @param objectId The object to be removed from a folder
     * @param folderId The folder to be removed from.
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId) throws CmisException
    {
        checkRepositoryId(repositoryId);

        NodeRef objectNodeReference = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        NodeRef folderNodeReference = checkAndReceiveFolderIdentifier(folderId);

        assertExistFolder(folderNodeReference);
        checkObjectChildParentRelationships(objectNodeReference, folderNodeReference);

        if (!cmisObjectsUtils.removeObject(objectNodeReference, folderNodeReference))
        {
            throw cmisObjectsUtils.createCmisException("The specified Object is not child of the specified Folder Object", EnumServiceException.INVALID_ARGUMENT);
        }
    }

    private NodeRef checkAndReceiveFolderIdentifier(String folderIdentifier) throws CmisException
    {
        try
        {
            return cmisObjectsUtils.getIdentifierInstance(folderIdentifier, AlfrescoObjectType.FOLDER_OBJECT);
        }
        catch (Throwable e)
        {
            throw cmisObjectsUtils.createCmisException("Unfiling is not supported. An Object can't be deleted from all Folders", EnumServiceException.NOT_SUPPORTED, e);
        }
    }

    private void checkObjectChildParentRelationships(NodeRef objectNodeReference, NodeRef folderNodeReference) throws CmisException
    {
        if (cmisObjectsUtils.isPrimaryObjectParent(folderNodeReference, objectNodeReference))
        {
            throw cmisObjectsUtils.createCmisException("Unfiling is not supported. Use deleteObjectService instead", EnumServiceException.NOT_SUPPORTED);
        }
    }
}
