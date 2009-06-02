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

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;

/**
 * @author Alexander Tsvetkov
 * 
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
        helper.versioningServicePort.checkOut(repositoryId, documentIdHolder, contentCopied);
        assertTrue(contentCopied.value);

        String documentName1 = "Test cmis document (" + System.currentTimeMillis() + ")";
        String documentId1 = helper.createDocument(documentName1, folderId);
        Holder<String> documentIdHolder1 = new Holder<String>(documentId1);
        contentCopied = new Holder<Boolean>();
        helper.versioningServicePort.checkOut(repositoryId, documentIdHolder1, contentCopied);
        assertTrue(contentCopied.value);

        GetCheckedoutDocsResponse response;
        response = getCheckedoutDocs(null, 0, 0);

        if (response.getObject().size() < 2)
        {
            // check in
            helper.versioningServicePort.checkIn(repositoryId, documentIdHolder, null, null, null, null);
            fail("Not all checkout docs have been found");
        }
        validateResponse(response.getObject());

        // assertTrue("Checked out document has not been found ", isExistItemWithProperty(response.getObject(), CMISMapping.PROP_OBJECT_ID, documentId));
        // assertTrue("Checked out document has not been found ", isExistItemWithProperty(response.getObject(), CMISMapping.PROP_OBJECT_ID, documentId1));

        response = getCheckedoutDocs(null, 1, 0);
        assertTrue(response.getObject().size() == 1);
        assertTrue(response.hasMoreItems);

        // check in
        helper.versioningServicePort.checkIn(repositoryId, documentIdHolder, null, null, null, null);

        response = getCheckedoutDocs(companyHomeId, 0, 0);
        assertFalse("Wrong results", isExistItemWithProperty(response.getObject(), CMISDictionaryModel.PROP_NAME, documentName));

    }

    public void testGetChildren() throws Exception
    {
        GetChildrenResponse response;
        response = getChildren(companyHomeId, EnumTypesOfFileableObjects.ANY, 0);

        if ((response != null) && (response.getObject() != null))
        {
            validateResponse(response.getObject());
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

        response = getChildren(folderId, EnumTypesOfFileableObjects.ANY, 0);
        assertEquals(2, response.getObject().size());
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName1));
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(1).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(documentName1));

        response = getChildren(folderId, EnumTypesOfFileableObjects.FOLDERS, 0);
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName1));

        response = getChildren(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 0);
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(documentName1));

        // FIXME: bug • If maxItems > 0, Bool hasMoreItems
        // Should return 1 item
        response = getChildren(folderId, EnumTypesOfFileableObjects.ANY, 1);
        assertTrue("Actual size is: " + response.getObject().size(), response.getObject().size() == 1);
        assertTrue(response.hasMoreItems);

        // • If “includeAllowableActions” is TRUE, the repository will return the allowable actions for the current user for each child object as part of the output.
        // • "IncludeRelationships" indicates whether relationships are also returned for each returned object. If it is set to "source" or "target", relationships for which the
        // returned object is a source, or respectively a target, will also be returned. If it is set to "both", relationships for which the returned object is either a source or a
        // target will be returned. If it is set to "none", relationships are not returned.

        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        // filters
        // response = getChildren(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 0, CMISMapping.PROP_NAME);
        // assertNotNull(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISMapping.PROP_NAME));

        response = getChildren(folderId);
        assertTrue(response.getObject().size() == 2);

    }

    public void testGetDescendants() throws Exception
    {

        GetDescendantsResponse response = getDescendants(companyHomeId, EnumTypesOfFileableObjects.DOCUMENTS, 10);

        if ((response != null) && (response.getObject() != null))
        {
            validateResponse(response.getObject());
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

        response = getDescendants(folderId, EnumTypesOfFileableObjects.FOLDERS, 1);
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName));

        response = getDescendants(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 2);
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(documentName));

        response = getDescendants(folderId, EnumTypesOfFileableObjects.ANY, 2);
        assertTrue(response.getObject().size() == 2);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName));
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(1).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(documentName));

        response = getDescendants(folderId, EnumTypesOfFileableObjects.ANY, -1);
        assertTrue(response.getObject().size() == 2);

        // test with out option parameters
        response = getDescendants(folderId);
        assertTrue(response.getObject().size() == 1);

        response = getDescendants(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 2);
        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        // Filter test
        // response = getDescendants(folderId, EnumTypesOfFileableObjects.DOCUMENTS, 2, CMISMapping.PROP_NAME);
        // assertTrue(response.getObject().size() == 1);
        // assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISMapping.PROP_NAME).equals(documentName));

        helper.deleteFolder(folderId1);

    }

    public void testGetFolderParent() throws Exception
    {
        GetFolderParentResponse response;
        response = getFolderParent(folderId, false);

        if ((response != null) && (response.getObject() != null))
        {
            validateResponse(response.getObject());
        }
        else
        {
            fail("response is null");
        }

        String folderId1;

        String folderName1 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId1 = helper.createFolder(folderName1, folderId);

        response = getFolderParent(folderId1, false);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName));

        String folderName2 = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        String folderId2 = helper.createFolder(folderName2, folderId1);

        response = getFolderParent(folderId2, true);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName1));
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(1).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName));
        assertTrue(response.getObject().size() >= 3);

    }

    public void testGetObjectParents() throws Exception
    {
        GetObjectParentsResponse response = helper.getObjectParents(documentId, "*");

        if ((response != null) && (response.getObject() != null))
        {
            validateResponse(response.getObject());
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
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName1));

        // TODO: not implemented
        // assertNotNull(response.getObject().get(0).getAllowableActions());
        // assertNotNull(response.getObject().get(0).getRelationship());

        // filters
        // response = getObjectParents(documentId1, CMISMapping.PROP_NAME);
        // assertTrue(response.getObject().size() >= 2);
        // assertNotNull(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISMapping.PROP_NAME));

        response = helper.getObjectParents(documentId1);
        assertTrue(response.getObject().size() == 1);
        assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_NAME, null).equals(folderName1));
        // assertTrue(propertiesUtil.getCmisPropertyValue(response.getObject().get(1).getProperties(), CMISMapping.PROP_NAME).equals(folderName));

    }

    private GetDescendantsResponse getDescendants(String folderId, EnumTypesOfFileableObjects type, long depth) throws Exception
    {
        GetDescendants request = cmisObjectFactory.createGetDescendants();

        request.setRepositoryId(repositoryId);
        request.setFolderId(folderId);
        request.setType(type);
        request.setDepth(cmisObjectFactory.createGetDescendantsDepth(BigInteger.valueOf(depth)));
        request.setFilter(cmisObjectFactory.createGetPropertiesFilter("*"));
        request.setIncludeAllowableActions(cmisObjectFactory.createGetDescendantsIncludeAllowableActions(true));
        request.setIncludeRelationships(cmisObjectFactory.createGetDescendantsIncludeRelationships(EnumIncludeRelationships.BOTH));

        GetDescendantsResponse response = ((NavigationServicePort) servicePort).getDescendants(request);
        return response;
    }

    private GetDescendantsResponse getDescendants(String folderId) throws Exception
    {
        GetDescendants request = cmisObjectFactory.createGetDescendants();

        request.setRepositoryId(repositoryId);
        request.setFolderId(folderId);

        GetDescendantsResponse response = ((NavigationServicePort) servicePort).getDescendants(request);
        return response;
    }

    private GetChildrenResponse getChildren(String folderId, EnumTypesOfFileableObjects type, long maxItems) throws Exception
    {

        GetChildren request = cmisObjectFactory.createGetChildren();

        request.setRepositoryId(repositoryId);
        request.setFolderId(folderId);

        request.setFilter(cmisObjectFactory.createGetChildrenFilter("*"));
        request.setMaxItems(cmisObjectFactory.createGetChildrenMaxItems(BigInteger.valueOf(maxItems)));
        request.setSkipCount(cmisObjectFactory.createGetChildrenSkipCount(BigInteger.valueOf(0)));
        request.setType(cmisObjectFactory.createGetChildrenType(type));

        GetChildrenResponse response = ((NavigationServicePort) servicePort).getChildren(request);

        return response;
    }

    private GetChildrenResponse getChildren(String folderId) throws Exception
    {

        GetChildren request = cmisObjectFactory.createGetChildren();

        request.setRepositoryId(repositoryId);
        request.setFolderId(folderId);

        GetChildrenResponse response = ((NavigationServicePort) servicePort).getChildren(request);

        return response;
    }

    public GetFolderParentResponse getFolderParent(String folderId, boolean setReturnToRoot) throws Exception
    {
        GetFolderParent request = cmisObjectFactory.createGetFolderParent();

        request.setRepositoryId(repositoryId);

        request.setFolderId(folderId);

        request.setFilter("*");
        request.setReturnToRoot(cmisObjectFactory.createGetFolderParentReturnToRoot(setReturnToRoot));

        request.setIncludeAllowableActions(cmisObjectFactory.createGetFolderParentIncludeAllowableActions(true));
        request.setIncludeRelationships(cmisObjectFactory.createGetFolderParentIncludeRelationships(EnumIncludeRelationships.BOTH));

        GetFolderParentResponse response = ((NavigationServicePort) servicePort).getFolderParent(request);
        return response;
    }

    public GetFolderParentResponse getFolderParent(String folderId, String filter) throws Exception
    {
        GetFolderParent request = cmisObjectFactory.createGetFolderParent();

        request.setRepositoryId(repositoryId);

        request.setFolderId(folderId);

        request.setFilter(filter);

        request.setIncludeAllowableActions(cmisObjectFactory.createGetFolderParentIncludeAllowableActions(true));
        request.setIncludeRelationships(cmisObjectFactory.createGetFolderParentIncludeRelationships(EnumIncludeRelationships.BOTH));

        GetFolderParentResponse response = null;
        try
        {
            response = ((NavigationServicePort) servicePort).getFolderParent(request);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        return response;
    }

    public GetFolderParentResponse getFolderParent(String folderId) throws Exception
    {
        GetFolderParent request = cmisObjectFactory.createGetFolderParent();

        request.setRepositoryId(repositoryId);
        request.setFolderId(folderId);

        GetFolderParentResponse response = ((NavigationServicePort) servicePort).getFolderParent(request);

        return response;
    }

    private GetCheckedoutDocsResponse getCheckedoutDocs(String folderId, long maxItems, long skipCount) throws Exception
    {
        GetCheckedoutDocs request = cmisObjectFactory.createGetCheckedoutDocs();

        request.setRepositoryId(repositoryId);

        request.setFolderId(cmisObjectFactory.createGetCheckedoutDocsFolderId(folderId));
        request.setFilter(cmisObjectFactory.createGetCheckedoutDocsFilter("*"));
        request.setMaxItems(cmisObjectFactory.createGetCheckedoutDocsMaxItems(BigInteger.valueOf(maxItems)));
        request.setSkipCount(cmisObjectFactory.createGetCheckedoutDocsSkipCount(BigInteger.valueOf(skipCount)));

        request.setIncludeAllowableActions(cmisObjectFactory.createGetCheckedoutDocsIncludeAllowableActions(true));
        request.setIncludeRelationships(cmisObjectFactory.createGetCheckedoutDocsIncludeRelationships(EnumIncludeRelationships.BOTH));

        GetCheckedoutDocsResponse response = ((NavigationServicePort) servicePort).getCheckedoutDocs(request);

        return response;
    }
}
