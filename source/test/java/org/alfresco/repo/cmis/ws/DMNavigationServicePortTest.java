package org.alfresco.repo.cmis.ws;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cmis.ws.OIDUtils;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @see org.alfresco.cmis.ws.NavigationServicePortDM
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
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF };
        checkListExact(listing, 0, 3, expectedNodeRefs);
    }

    public void testGetChildrenDocuments() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.DOCUMENTS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkListExact(listing, 3, 0, expectedNodeRefs);
    }

    public void testGetChildrenSkipCount() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(1));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 5, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItems() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(3));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 3, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsMore() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(10));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 6, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenSkipCountMore() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(10));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 0, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsSkipCount() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setSkipCount(BigInteger.valueOf(5));
        request.setMaxItems(BigInteger.valueOf(4));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkList(listing, 1, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenMaxItemsZero() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setMaxItems(BigInteger.valueOf(0));
        request.setFolderId(OIDUtils.toOID(rootNodeRef));
        request.setType(TypesOfObjectsEnum.FOLDERS_AND_DOCUMETS);

        GetChildrenResponse response = navigationServicePort.getChildren(request);
        List<DocumentOrFolderObjectType> listing = response.getDocumentAndFolderCollection().getObject();
        NodeRef[] expectedNodeRefs = new NodeRef[] { L0_FOLDER_0_NODEREF, L0_FOLDER_1_NODEREF, L0_FOLDER_2_NODEREF, L0_FILE_0_NODEREF, L0_FILE_1_NODEREF, L0_FILE_2_NODEREF };
        checkListExact(listing, 3, 3, expectedNodeRefs);
    }

    public void testGetChildrenForDocument() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(OIDUtils.toOID(L0_FILE_0_NODEREF));
        request.setType(TypesOfObjectsEnum.FOLDERS);

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

    public void testGetChildrenForRelationship() throws Exception
    {
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        GetChildren request = new GetChildren();
        request.setFolderId(OIDUtils.toOID(L0_FILE_1_TO_L0_FILE_0_ASSOCREF));
        request.setType(TypesOfObjectsEnum.FOLDERS);

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

    private void checkListExact(List<DocumentOrFolderObjectType> objects, int expectedFileCount, int expectedFolderCount, NodeRef[] expectedNodeRefs)
    {
        int fileCount = 0;
        int folderCount = 0;
        List<NodeRef> check = new ArrayList<NodeRef>(8);

        for (NodeRef nodeRef : expectedNodeRefs)
        {
            check.add(nodeRef);
        }

        for (DocumentOrFolderObjectType object : objects)
        {
            NodeRef nodeRef = OIDUtils.OIDtoNodeRef(object.getObjectID());
            if (dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER))
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

    private void checkList(List<DocumentOrFolderObjectType> objects, int expectedCount, int expectedMaxFileCount, int expectedMaxFolderCount, NodeRef[] expectedNodeRefs)
    {
        int fileCount = 0;
        int folderCount = 0;
        List<NodeRef> check = new ArrayList<NodeRef>(8);

        for (NodeRef nodeRef : expectedNodeRefs)
        {
            check.add(nodeRef);
        }

        for (DocumentOrFolderObjectType object : objects)
        {
            NodeRef nodeRef = OIDUtils.OIDtoNodeRef(object.getObjectID());
            if (dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER))
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
