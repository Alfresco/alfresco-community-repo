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
import java.util.Collections;
import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;

/**
 * @author Alexander Tsvetkov
 */

public class DMNavigationServiceTest extends AbstractServiceTest
{
    public DMNavigationServiceTest()
    {
        super();
    }

    public DMNavigationServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        createInitialContent();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        deleteInitialContent();
    }

    protected Object getServicePort()
    {
        return helper.navigationServicePort;
    }

    public void testGetCheckedoutDocs() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        helper.versioningServicePort.checkOut(repositoryId, documentIdHolder, new Holder<CmisExtensionType>(), contentCopied);
        assertTrue(contentCopied.value);

        String documentName1 = "Test cmis document (" + System.currentTimeMillis() + ")";
        String documentId1 = helper.createDocument(documentName1, folderId);
        Holder<String> documentIdHolder1 = new Holder<String>(documentId1);
        contentCopied = new Holder<Boolean>();
        helper.versioningServicePort.checkOut(repositoryId, documentIdHolder1, new Holder<CmisExtensionType>(), contentCopied);
        assertTrue(contentCopied.value);

        List<CmisObjectType> result = getCheckedoutDocs(null, 0, 0);

        if (result == null || result.size() < 2)
        {
            // check in
            helper.versioningServicePort.checkIn(repositoryId, documentIdHolder, false, null, null, null, null, null, null, new Holder<CmisExtensionType>());
            fail("Not all checkout docs have been found");
        }
        validateResponse(result);

        result = getCheckedoutDocs(null, 1, 0);
        assertNotNull(result);
        assertTrue(result.size() == 1);

        // check in
        helper.versioningServicePort.checkIn(repositoryId, documentIdHolder, false, null, null, null, null, null, null, new Holder<CmisExtensionType>());

        result = getCheckedoutDocs(companyHomeId, 0, 0);
        assertFalse("Wrong results", isExistItemWithProperty(result, CMISDictionaryModel.PROP_NAME, documentName));

    }

    public void testGetChildren() throws Exception
    {
        List<CmisObjectInFolderType> response = getChildren(companyHomeId, 100, true);

        if (null != response)
        {
            for (CmisObjectInFolderType object : response)
            {
                assertNotNull(object);
                assertNotNull(object.getObject());
                assertNotNull(object.getObject().getProperties());
                assertNotNull(object.getObject().getProperties().getProperty());
                String name = getStringProperty(object.getObject().getProperties(), CMISDictionaryModel.PROP_NAME);
                assertNotNull("Name property is undefined", name);
                assertNotNull(object.getPathSegment());
                assertTrue(object.getPathSegment().endsWith(name));
            }
        }
        else
        {
            fail("response is null");
        }

        String folderName1 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        @SuppressWarnings("unused")
        String folderId1 = helper.createFolder(folderName1, folderId);
        String documentName1 = "Test cmis document (" + System.currentTimeMillis() + ")";
        @SuppressWarnings("unused")
        String documentId1 = helper.createDocument(documentName1, folderId, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);

        response = getChildren(folderId, 2, false);
        assertEquals(2, response.size());
        assertTrue(getStringProperty(response.get(0).getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName1));
        assertTrue(getStringProperty(response.get(1).getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(documentName1));

        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        // filters
        // response = getChildren(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 0, CMISMapping.PROP_NAME);
        // assertNotNull(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISMapping.PROP_NAME));

    }

    public void testGetDescendants() throws Exception
    {
        List<CmisObjectInFolderContainerType> response = getDescendants(companyHomeId, BigInteger.valueOf(5));

        if ((response != null))
        {
            validateResponse(response, true);
        }
        else
        {
            fail("response is null");
        }

        folderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        String folderId1 = helper.createFolder(folderName, folderId);
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        @SuppressWarnings("unused")
        String documentId1 = helper.createDocument(documentName, folderId1, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);

        response = getDescendants(folderId1, null);
        assertTrue(response.size() == 1);
        assertTrue(getStringProperty(response.get(0).getObjectInFolder().getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(documentName));

        // test with out option parameters
        response = getDescendants(folderId1, BigInteger.TEN);
        assertTrue(response.size() == 1);
        assertTrue(getStringProperty(response.get(0).getObjectInFolder().getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(documentName));

        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        helper.deleteFolder(folderId1);
    }

    public void testGetFolderTree() throws Exception
    {
        List<CmisObjectInFolderContainerType> response = getFolderTree(companyHomeId, BigInteger.valueOf(5));

        if (null != response)
        {
            validateResponse(response, false);
        }
        else
        {
            fail("response is null");
        }

        String internalFolderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        String folderId1 = helper.createFolder(internalFolderName, folderId);
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        @SuppressWarnings("unused")
        String documentId1 = helper.createDocument(documentName, folderId1, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);

        response = getFolderTree(folderId, null);
        assertTrue(response.size() == 1);
        String name = getStringProperty(response.get(0).getObjectInFolder().getObject().getProperties(), CMISDictionaryModel.PROP_NAME);
        assertEquals(internalFolderName, name);

        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        helper.deleteFolder(folderId1);
    }

    public void testGetFolderParent() throws Exception
    {
        CmisObjectType response = getFolderParent(folderId);

        if ((response != null))
        {
            validateResponse(Collections.singletonList(response));
        }
        else
        {
            fail("response is null");
        }

        String folderId1;

        String folderName1 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId1 = helper.createFolder(folderName1, folderId);

        response = getFolderParent(folderId1);
        assertTrue(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName));

        String folderName2 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        String folderId2 = helper.createFolder(folderName2, folderId1);

        response = getFolderParent(folderId2, CMISDictionaryModel.PROP_NAME);
        assertTrue(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName1));
    }

    public void testGetObjectParents() throws Exception
    {
        List<CmisObjectParentsType> response = helper.getObjectParents(documentId, "*");

        if ((response != null))
        {
            for (CmisObjectParentsType object : response)
            {
                assertNotNull(object);
                validateCmisObjectType(object.getObject());
            }
        }
        else
        {
            fail("response is null");
        }

        String folderId1;
        String documentId1;

        String folderName1 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId1 = helper.createFolder(folderName1, folderId);
        String documentName1 = "Test cmis document (" + System.currentTimeMillis() + ")";
        documentId1 = helper.createDocument(documentName1, folderId1, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);

        response = helper.getObjectParents(documentId1, "*");
        assertTrue(response.size() == 1);
        assertTrue(getStringProperty(response.get(0).getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName1));

        response = helper.getObjectParents(documentId1);
        assertTrue(response.size() == 1);
        assertTrue(getStringProperty(response.get(0).getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName1));

    }

    private List<CmisObjectInFolderContainerType> getDescendants(String folderId, BigInteger depth) throws Exception
    {
        List<CmisObjectInFolderContainerType> result = ((NavigationServicePort) servicePort).getDescendants(repositoryId, folderId, depth, "*", false, null, "", false, null);
        return result;
    }

    private List<CmisObjectInFolderContainerType> getFolderTree(String folderId, BigInteger depth) throws Exception
    {
        List<CmisObjectInFolderContainerType> result = ((NavigationServicePort) servicePort).getFolderTree(repositoryId, folderId, depth, "*", false, null, "", false, null);
        return result;
    }

    private List<CmisObjectInFolderType> getChildren(String folderId, int maxItems, boolean includePathSegments) throws Exception
    {
        CmisObjectInFolderListType result = ((NavigationServicePort) servicePort).getChildren(repositoryId, folderId, "*", "", false, null, "", includePathSegments, BigInteger
                .valueOf(maxItems), BigInteger.valueOf(0), null);
        assertNotNull("Get Children response is undefined", result);
        return result.getObjects();
    }

    public CmisObjectType getFolderParent(String folderId, String filter) throws Exception
    {
        CmisObjectType response = null;
        try
        {
            response = ((NavigationServicePort) servicePort).getFolderParent(repositoryId, folderId, "*", null);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        return response;
    }

    public CmisObjectType getFolderParent(String folderId) throws Exception
    {
        CmisObjectType response = ((NavigationServicePort) servicePort).getFolderParent(repositoryId, folderId, "*", null);
        return response;
    }

    private List<CmisObjectType> getCheckedoutDocs(String folderId, long maxItems, long skipCount) throws Exception
    {
        CmisObjectListType result = ((NavigationServicePort) servicePort).getCheckedOutDocs(repositoryId, folderId, "*", null, false, null, "", BigInteger.valueOf(maxItems),
                BigInteger.valueOf(skipCount), null);
        assertNotNull(result);
        return result.getObjects();
    }
}
