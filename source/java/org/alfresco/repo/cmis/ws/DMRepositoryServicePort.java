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

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Michael Shavnev
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "RepositoryServicePort", serviceName = "RepositoryService", portName = "RepositoryServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.RepositoryServicePort")
public class DMRepositoryServicePort extends DMAbstractServicePort implements RepositoryServicePort
{
    private static final Log log = LogFactory.getLog("org.alfresco.repo.cmis.ws");

    private String rootPath;
    private NodeRef rootNodeRef;

    public GetRootFolderResponse getRootFolder(GetRootFolder parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException, FilterNotValidException,
    OperationNotSupportedException, PermissionDeniedException
    {
        PropertyFilter propertyFilter = new PropertyFilter(parameters.getFilter());
        FolderObjectType folderObject = new FolderObjectType();

        propertyFilter.disableProperty(CmisProperty.NAME);
        propertyFilter.disableProperty(CmisProperty.PARENT);
        setFolderObjectTypeProperties(getRootNodeRef(), folderObject, propertyFilter);
        propertyFilter.enableProperty(CmisProperty.NAME);
        propertyFilter.enableProperty(CmisProperty.PARENT);

        if (propertyFilter.allow(CmisProperty.NAME))
        {
            folderObject.setName("CMIS_Root_Folder");
        }

        if (propertyFilter.allow(CmisProperty.PARENT))
        {
            folderObject.setParent(OIDUtils.toOID(rootNodeRef));
        }

        GetRootFolderResponse response = new GetRootFolderResponse();
        response.setRootFolder(folderObject);
        return response;
    }

    private NodeRef getRootNodeRef()
    {
        if (rootNodeRef == null)
        {
            int indexOfStoreDelim = rootPath.indexOf(StoreRef.URI_FILLER);

            if (indexOfStoreDelim == -1)
            {
                throw new java.lang.RuntimeException("Bad path format, " + StoreRef.URI_FILLER + " not found");
            }

            indexOfStoreDelim += StoreRef.URI_FILLER.length();

            int indexOfPathDelim = rootPath.indexOf("/", indexOfStoreDelim);

            if (indexOfPathDelim == -1)
            {
                throw new java.lang.RuntimeException("Bad path format, / not found");
            }

            String storePath = rootPath.substring(0, indexOfPathDelim);
            String rootPathInStore = rootPath.substring(indexOfPathDelim);

            StoreRef storeRef = new StoreRef(storePath);
            if (nodeService.exists(storeRef) == false)
            {
                throw new java.lang.RuntimeException("No store for path: " + storeRef);
            }

            NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

            if (rootPath.equals("/"))
            {
                rootNodeRef = storeRootNodeRef;
            }
            else
            {
                List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, rootPathInStore, null, namespaceService, false);

                if (nodeRefs.size() > 1)
                {
                    throw new java.lang.RuntimeException("Multiple possible roots for : \n" + "   root path: " + rootPath + "\n" + "   results: " + nodeRefs);
                }
                else if (nodeRefs.size() == 0)
                {
                    throw new java.lang.RuntimeException("No root found for : \n" + "   root path: " + rootPath);
                }
                else
                {
                    rootNodeRef = nodeRefs.get(0);
                }
            }
        }

        return rootNodeRef;
    }

    public GetTypesResponse getTypes(GetTypes parameters) throws RuntimeException, ConcurrencyException, InvalidArgumentException, OperationNotSupportedException,
    PermissionDeniedException
    {
        System.out.println(parameters);
        try
        {
            org.alfresco.repo.cmis.ws.GetTypesResponse _return = null;
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
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void getTypeDefinition(java.lang.String typeId, java.lang.Boolean includeInheritedProperties, javax.xml.ws.Holder<ObjectTypeDefinitionType> type,
            javax.xml.ws.Holder<java.lang.Boolean> canCreateInstances) throws RuntimeException, ConcurrencyException, InvalidArgumentException, TypeNotFoundException,
            OperationNotSupportedException, PermissionDeniedException
            {
        System.out.println(typeId);
        System.out.println(includeInheritedProperties);
        try
        {
            org.alfresco.repo.cmis.ws.ObjectTypeDefinitionType typeValue = null;
            type.value = typeValue;
            java.lang.Boolean canCreateInstancesValue = null;
            canCreateInstances.value = canCreateInstancesValue;
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
        // throw new OperationNotSupportedException("OperationNotSupportedException...");
        // throw new PermissionDeniedException("PermissionDeniedException...");
            }

    public RepositoryInfoType getRepositoryInfo() throws RuntimeException, ConcurrencyException, InvalidArgumentException, OperationNotSupportedException,
    PermissionDeniedException
    {
        try
        {
            org.alfresco.repo.cmis.ws.RepositoryInfoType _return = null;
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
        // throw new PermissionDeniedException("PermissionDeniedException...");
    }

    public void setRootPath(String rootPath)
    {
        this.rootPath = rootPath;
    }

}
