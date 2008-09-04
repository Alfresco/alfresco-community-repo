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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @see org.alfresco.repo.cmis.ws.NavigationServicePortDM
 *
 * @author Dmitry Lazurkin
 *
 */
public class DMNavigationServicePortTest extends BaseServicePortContentTest
{
    private NavigationServicePort navigationServicePort;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();

        navigationServicePort = (NavigationServicePort) applicationContext.getBean("dmNavigationService");
    }

    public void testGetChildrenFolders() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.FOLDERS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF };
        checkListExact(listing, 0, 3, expectedNodeRefs);
    }

    public void testGetChildrenDocuments() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.DOCUMENTS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkListExact(listing, 3, 0, expectedNodeRefs);
    }

    public void testGetChildrenSkipCount() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(1));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 5, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItems() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(3));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 3, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsMore() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(10));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 6, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenSkipCountMore() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(10));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 0, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsSkipCount() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(5));
        request.setMaxItems(BigInteger.valueOf(4));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 1, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsZero() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(0));
        request.setFolderId(rootNodeRef.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<FolderTreeType> listing = response.getChildren().getChild();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkListExact(listing, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenForDocument() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(L0_FILE_0_NODEREF.toString());
        request.setType(TypesOfFileableObjectsEnum.ANY);

        try
        {
            navigationServicePort.getChildren(request);
        }
        catch (FolderNotValidException e)
        {
            return;
        }

        fail("Expects exception");
    }

    private void checkListExact(List<FolderTreeType> objects, int expectedFileCount, int expectedFolderCount, NodeRef[] expectedNodeRefs)
    {
        int fileCount = 0;
        int folderCount = 0;
        List<NodeRef> check = new ArrayList<NodeRef>(8);

        for (NodeRef nodeRef : expectedNodeRefs)
        {
            check.add(nodeRef);
        }

        for (FolderTreeType object : objects)
        {
            NodeRef nodeRef = new NodeRef(getPropertyIDValue(object.getProperties(), CMISMapping.PROP_OBJECT_ID));

            if (cmisMapping.isValidCmisFolder(cmisMapping.getCmisType(nodeService.getType(nodeRef))))
            {
                folderCount++;
            }
            else
            {
                fileCount++;
            }

            assertTrue(check.remove(nodeRef));
        }

        assertTrue("Name list was not exact - remaining: " + check, check.size() == 0);
        assertEquals("Incorrect number of files", expectedFileCount, fileCount);
        assertEquals("Incorrect number of folders", expectedFolderCount, folderCount);
    }

    private void checkList(List<FolderTreeType> objects, int expectedCount, int expectedMaxFileCount, int expectedMaxFolderCount, NodeRef[] expectedNodeRefs)
    {
        int fileCount = 0;
        int folderCount = 0;
        List<NodeRef> check = new ArrayList<NodeRef>(8);

        for (NodeRef nodeRef : expectedNodeRefs)
        {
            check.add(nodeRef);
        }

        for (FolderTreeType object : objects)
        {
            NodeRef nodeRef = new NodeRef(getPropertyIDValue(object.getProperties(), CMISMapping.PROP_OBJECT_ID));

            if (cmisMapping.isValidCmisFolder(cmisMapping.getCmisType(nodeService.getType(nodeRef))))
            {
                folderCount++;
            }
            else
            {
                fileCount++;
            }

            assertTrue(check.remove(nodeRef));
        }

        assertTrue((fileCount + folderCount) == expectedCount);
        assertTrue(fileCount <= expectedMaxFileCount);
        assertTrue(folderCount <= expectedMaxFolderCount);
    }

}
