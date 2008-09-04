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

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "NavigationServicePort", serviceName = "NavigationService", portName = "NavigationServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.NavigationServicePort")
public class DMNavigationServicePort extends DMAbstractServicePort implements NavigationServicePort
{
    private static final Log log = LogFactory.getLog("org.alfresco.repo.cmis.ws");

    public org.alfresco.repo.cmis.ws.GetDescendantsResponse getDescendants(GetDescendants parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            FilterNotValidException, OperationNotSupportedException, FolderNotValidException, PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetDescendantsResponse _return = null;
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
        // throw new FilterNotValidException("FilterNotValidException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetCheckedoutDocsResponse getCheckedoutDocs(GetCheckedoutDocs parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            FilterNotValidException, OperationNotSupportedException, FolderNotValidException, PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetCheckedoutDocsResponse _return = null;
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
        // throw new FilterNotValidException("FilterNotValidException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetDocumentParentsResponse getDocumentParents(GetDocumentParents parameters) throws RuntimeException, ConcurrencyException,
            InvalidArgumentException, ObjectNotFoundException, FilterNotValidException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetDocumentParentsResponse _return = null;
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
        // throw new FilterNotValidException("FilterNotValidException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetChildrenResponse getChildren(GetChildren parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            FilterNotValidException, OperationNotSupportedException, FolderNotValidException, PermissionDeniedException
    {
        PropertyFilter propertyFilter = new PropertyFilter(parameters.getFilter());

        NodeRef folderNodeRef = OIDUtils.OIDtoNodeRef(parameters.getFolderId());

        assertExistFolder(folderNodeRef);

        List<FileInfo> listing;
        if (parameters.getType().equals(TypesOfObjectsEnum.DOCUMENTS))
        {
            listing = fileFolderService.listFiles(folderNodeRef);
        }
        else if (parameters.getType().equals(TypesOfObjectsEnum.FOLDERS))
        {
            listing = fileFolderService.listFolders(folderNodeRef);
        }
        else
        {
            listing = fileFolderService.list(folderNodeRef);
        }

        int maxItems = listing.size();
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
                maxItems = listing.size();
            }

            response.setHasMoreItems(maxItems < listing.size());
        }

        response.setDocumentAndFolderCollection(new DocumentAndFolderCollection());
        List<DocumentOrFolderObjectType> resultListing = response.getDocumentAndFolderCollection().getObject();

        for (int index = skipCount; index < listing.size() && maxItems > 0; ++index, --maxItems)
        {
            FileInfo currentFileInfo = listing.get(index);
            DocumentOrFolderObjectType documentOrFolderObjectType = new DocumentOrFolderObjectType();

            if (currentFileInfo.isFolder())
            {
                setFolderObjectTypeProperties(currentFileInfo.getNodeRef(), documentOrFolderObjectType, propertyFilter);
            }
            else
            {
                setDocumentObjectTypeProperties(currentFileInfo.getNodeRef(), documentOrFolderObjectType, propertyFilter);
            }

            resultListing.add(documentOrFolderObjectType);
        }

        return response;
    }

    /**
     * Asserts "Folder with folderNodeRef exists"
     *
     * @param folderNodeRef node reference
     * @throws FolderNotValidException folderNodeRef doesn't exist or folderNodeRef isn't for folder object
     */
    private void assertExistFolder(NodeRef folderNodeRef) throws FolderNotValidException
    {
        if (folderNodeRef == null || nodeService.exists(folderNodeRef) == false || isFolderType(folderNodeRef) == false)
        {
            // TODO: error code
            BasicFault basicFault = ExceptionUtils.createBasicFault(null, "OID for non-existent object or not folder object");
            throw new FolderNotValidException("OID for non-existent object or not folder object", basicFault);
        }
    }

    public org.alfresco.repo.cmis.ws.GetFolderParentResponse getFolderParent(GetFolderParent parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            FilterNotValidException, OperationNotSupportedException, FolderNotValidException, PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetFolderParentResponse _return = null;
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
        // throw new FilterNotValidException("FilterNotValidException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new FolderNotValidException("FolderNotValidException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public org.alfresco.repo.cmis.ws.GetUnfiledDocsResponse getUnfiledDocs(GetUnfiledDocs parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException,
            FilterNotValidException, OperationNotSupportedException, PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetUnfiledDocsResponse _return = null;
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
        // throw new FilterNotValidException("FilterNotValidException...");
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

}
