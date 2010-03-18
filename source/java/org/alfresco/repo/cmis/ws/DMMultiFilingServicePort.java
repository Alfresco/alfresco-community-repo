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

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for Multi-Filing service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "MultiFilingServicePort", serviceName = "MultiFilingService", portName = "MultiFilingServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.MultiFilingServicePort")
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
    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        checkConstraints(objectId, folderId, false);
        try
        {
            cmisService.addObjectToFolder(objectId, folderId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Removes a non-folder child object from a folder or from all folders. This does not delete the object and does not change the ID of the object.
     * 
     * @param repositoryId repository Id
     * @param objectId The object to be removed from a folder
     * @param folderId The folder to be removed from.
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        checkConstraints(objectId, folderId, true);

        try
        {
            cmisService.removeObjectFromFolder(objectId, folderId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }

    }
    
    private void checkConstraints(String objectId, String folderId, boolean checkIsObjectInFolder) throws CmisException
    {
        NodeRef objectNodeRef = null;
        NodeRef folderNodeRef = null;
        CMISTypeDefinition objectTypeDef = null;

        try
        {
            objectNodeRef = cmisService.getObject(objectId, NodeRef.class, true, false, false);
            folderNodeRef = cmisService.getFolder(folderId);
            objectTypeDef = cmisService.getTypeDefinition(objectNodeRef);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e.getMessage(), EnumServiceException.INVALID_ARGUMENT);
        }

        if (!objectTypeDef.getTypeId().getBaseTypeId().equals(CMISDictionaryModel.DOCUMENT_TYPE_ID))
        {
            throw ExceptionUtil.createCmisException("Object " + objectId + " is not a document", EnumServiceException.INVALID_ARGUMENT);
        }
        
        if (checkIsObjectInFolder && !isObjectInFolder(objectNodeRef, folderNodeRef))
        {
            throw ExceptionUtil.createCmisException("Folder doesn't contain specified object", EnumServiceException.OBJECT_NOT_FOUND);
        }
    }
   
}
