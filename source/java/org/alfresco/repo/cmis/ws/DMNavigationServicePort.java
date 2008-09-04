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
import java.util.List;

import org.alfresco.cmis.CMISService.TypesFilter;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for navigation service
 *
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "NavigationServicePort", serviceName = "NavigationService", portName = "NavigationServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.NavigationServicePort")
public class DMNavigationServicePort extends DMAbstractServicePort implements NavigationServicePort
{

    public GetCheckedoutDocsResponse getCheckedoutDocs(GetCheckedoutDocs parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    /**
     * Asserts "Folder with folderNodeRef exists"
     *
     * @param folderNodeRef node reference
     * @throws FolderNotValidException folderNodeRef doesn't exist or folderNodeRef isn't for folder object
     */
    private void assertExistFolder(NodeRef folderNodeRef) throws FolderNotValidException
    {
        CMISMapping cmisMapping = cmisDictionaryService.getCMISMapping();
        if (folderNodeRef == null || nodeService.exists(folderNodeRef) == false || cmisMapping.isValidCmisFolder(cmisMapping.getCmisType(nodeService.getType(folderNodeRef))) == false)
        {
            // TODO: error code
            throw new FolderNotValidException("OID for non-existent object or not folder object", ExceptionUtils.createBasicFault(null, "OID for non-existent object or not folder object"));
        }
    }

    public GetChildrenResponse getChildren(GetChildren parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException,
            FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        PropertyFilter propertyFilter = new PropertyFilter(parameters.getFilter());

        NodeRef folderNodeRef = getNodeRefFromOID(parameters.getFolderId());
        assertExistFolder(folderNodeRef);

        NodeRef[] listing = null;
        switch (parameters.getType())
        {
        case DOCUMENTS:
            listing = cmisService.getChildren(folderNodeRef, TypesFilter.Documents);
            break;
        case FOLDERS:
            listing = cmisService.getChildren(folderNodeRef, TypesFilter.Folders);
            break;
        case POLICIES:
            throw new OperationNotSupportedException("Policies listing isn't supported", ExceptionUtils.createBasicFault(null, "Policies listing isn't supported"));
        case ANY:
            listing = cmisService.getChildren(folderNodeRef, TypesFilter.Any);
            break;
        }

        int maxItems = listing.length;
        int skipCount = 0;

        GetChildrenResponse response = new GetChildrenResponse();

        // TODO: getChildren, support for BigIntegers, support for ResultSet
        if (parameters.getSkipCount() != null)
        {
            BigInteger skipCountParam = parameters.getSkipCount();
            skipCount = skipCountParam.max(BigInteger.valueOf(skipCount)).intValue();
        }

        if (parameters.getMaxItems() != null)
        {
            BigInteger maxItemsParam = parameters.getMaxItems();
            maxItems = maxItemsParam.min(BigInteger.valueOf(maxItems)).intValue();

            if (maxItems == 0)
            {
                maxItems = listing.length;
            }

            response.setHasMoreItems(maxItems < listing.length);
        }

        response.setChildren(new ChildrenType());
        List<FolderTreeType> resultListing = response.getChildren().getChild();

        for (int index = skipCount; index < listing.length && maxItems > 0; ++index, --maxItems)
        {
            NodeRef currentFileInfo = listing[index];
            FolderTreeType folderTreeType = new FolderTreeType();
            folderTreeType.setProperties(getPropertiesType(currentFileInfo, propertyFilter));
            resultListing.add(folderTreeType);
        }

        return response;
    }

    public GetDescendantsResponse getDescendants(GetDescendants parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public GetFolderParentResponse getFolderParent(GetFolderParent parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

    public GetObjectParentsResponse getObjectParents(GetObjectParents parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        return null;
    }

}
