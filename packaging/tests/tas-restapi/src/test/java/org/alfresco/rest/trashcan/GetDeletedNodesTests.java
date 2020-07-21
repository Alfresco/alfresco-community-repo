/*
 * Copyright (C) 2005-2017 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.rest.trashcan;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for /api-explorer/#!/trashcan/listDeletedNodes
 *           /api-explorer/#!/trashcan/deleteDeletedNode
 *           /api-explorer/#!/trashcan/getDeletedNode
 *           /api-explorer/#!/trashcan/restoreDeletedNode
 * 
 * @author jcule
 *
 */
public class GetDeletedNodesTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel deleteNodesSiteModel;
    private FolderModel deleteNodesFolder1, deleteNodesFolder2, deleteNodesFolder3, deleteNodesFolder4, deleteNodesFolder5, getDeleteNodesFolder6;
    private FileModel file, file1, file2, file3, file4;

    private RestNodeModelsCollection deletedNodes, deletedNodesMaxItem;
    private RestNodeModel node;

    private RestRenditionInfoModelCollection nodeRenditionInfoCollection;
    private RestRenditionInfoModel nodeRenditionInfo;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);
        deleteNodesSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception
    {
        dataSite.usingAdmin().deleteSite(deleteNodesSiteModel);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.REGRESSION })
    public void testDeletedNodesBiggerThanMaxCount() throws Exception
    {
        // get the number of item in the trashcan
        deletedNodes = restClient.withCoreAPI().usingTrashcan().findDeletedNodes();
        int count = deletedNodes.getPagination().getCount();
        int totalItems = deletedNodes.getPagination().getTotalItems();

        // create folders
        deleteNodesFolder1 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createFolder();
        deleteNodesFolder2 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createFolder();
        deleteNodesFolder3 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createFolder();

        // delete folders
        dataContent.usingUser(adminUserModel).usingResource(deleteNodesFolder1).deleteContent();
        dataContent.usingUser(adminUserModel).usingResource(deleteNodesFolder2).deleteContent();
        dataContent.usingUser(adminUserModel).usingResource(deleteNodesFolder3).deleteContent();

        int maxItems = count + 1;
        deletedNodesMaxItem = restClient.withCoreAPI().usingTrashcan().usingParams(String.format("maxItems=%s", maxItems)).findDeletedNodes();

        String countMaxItem = Integer.toString(maxItems);
        String totalItemsMaxItem = Integer.toString(totalItems + 3);
        String hasMoreItemsMaxItem = Boolean.toString(deletedNodesMaxItem.getPagination().isHasMoreItems());
        String skipCount = Integer.toString(deletedNodesMaxItem.getPagination().getSkipCount());

        restClient.assertStatusCodeIs(HttpStatus.OK);
        deletedNodesMaxItem.getPagination().assertThat().field("totalItems").is(totalItemsMaxItem).and().field("count").is(countMaxItem);
        deletedNodesMaxItem.getPagination().assertThat().field("hasMoreItems").is(hasMoreItemsMaxItem).and().field("skipCount").is(skipCount);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TRASHCAN }, executionType = ExecutionType.SANITY, description = "Sanity tests for GET /deleted-nodes and GET /deleted-nodes/{nodeId}")
    @Test(groups = { TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.SANITY })
    public void testGetDeletedNodesFromTrashcan() throws Exception
    {
        // Create a folder and a file
        deleteNodesFolder4 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createFolder();
        file = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createContent(DocumentType.TEXT_PLAIN);

        // Delete file and folder
        dataContent.usingUser(adminUserModel).usingResource(deleteNodesFolder4).deleteContent();
        dataContent.usingUser(adminUserModel).usingResource(file).deleteContent();

        // GET /deleted-nodes: deleted file and folder are in the trashcan list
        deletedNodes = restClient.withCoreAPI().usingTrashcan().findDeletedNodes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        deletedNodes.getEntryByIndex(0).assertThat().field("name").is(file.getName()).and()
                                                    .field("id").is(file.getNodeRefWithoutVersion());
        deletedNodes.getEntryByIndex(1).assertThat().field("name").is(deleteNodesFolder4.getName()).and()
                                                    .field("id").is(deleteNodesFolder4.getNodeRefWithoutVersion());

        // GET /deleted-nodes/{nodeId}: check a single deleted object
        node = restClient.withCoreAPI().usingTrashcan().findDeletedNode(deleteNodesFolder4);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        node.assertThat().field("name").is(deleteNodesFolder4.getName()).and()
                         .field("id").is(deleteNodesFolder4.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TRASHCAN }, executionType = ExecutionType.SANITY, description = "Sanity tests for DELETE /deleted-nodes/{nodeId} and POST /deleted-nodes/{nodeId}/restore")
    @Test(groups = { TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.SANITY })
    public void testDeleteAndRestoreNodeFromTrashcan() throws Exception
    {
        // Create a folder and a file
        deleteNodesFolder5 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createFolder();
        String docLibNodeRef = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).getNodeRef();
        file1 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createContent(DocumentType.TEXT_PLAIN);

        // Delete file and folder
        dataContent.usingUser(adminUserModel).usingResource(deleteNodesFolder5).deleteContent();
        dataContent.usingUser(adminUserModel).usingResource(file1).deleteContent();

        // DELETE /deleted-nodes/{nodeId}: delete an object from trashcan
        restClient.withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(file1);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingTrashcan().findDeletedNode(file1);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);

        // POST /deleted-nodes/{nodeId}/restore: restore to repository a deleted object
        node = restClient.withCoreAPI().usingTrashcan().restoreNodeFromTrashcan(deleteNodesFolder5);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        node.assertThat().field("id").is(deleteNodesFolder5.getNodeRefWithoutVersion()).and()
                         .field("parentId").is(docLibNodeRef);

        // Checks that the node was removed from trashcan 
        restClient.withCoreAPI().usingTrashcan().findDeletedNode(deleteNodesFolder5);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TRASHCAN }, executionType = ExecutionType.SANITY, description = "Sanity tests for GET /deleted-nodes/{nodeId}/content")
    @Test(groups = { TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.SANITY })
    public void testGetDeletedNodesContent() throws Exception
    {
        // Create file2 based on existing resource
        FileModel newFile = FileModel.getFileModelBasedOnTestDataFile("sampleContent.txt");
        newFile.setName("sampleContent.txt");
        file2 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createContent(newFile);

        // Delete file2 to be moved in trashcan
        dataContent.usingUser(adminUserModel).usingResource(file2).deleteContent();

        // Make GET /deleted-nodes/{nodeId}/content and check file content
        restClient.withCoreAPI().usingTrashcan().getDeletedNodeContent(file2);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Disposition", file2.getName());
        restClient.onResponse().getResponse().body().asString().contains("Sample text.");
    }

    @Bug(id = "REPO-4484")
    @TestRail(section = { TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.REQUIRE_SOLR }, executionType = ExecutionType.SANITY,
              description = "Sanity tests for GET /deleted-nodes/{nodeId}/renditions, GET /deleted-nodes/{nodeId}/renditions/{renditionId}, GET /deleted-nodes/{nodeId}/renditions/{renditionId}/content")
    @Test(groups = { TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.SANITY, TestGroup.RENDITIONS })
    public void testGetDeletedNodesRenditions() throws Exception
    {
        // Create file3 based on existing resource
        file3 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createContent(DocumentType.TEXT_PLAIN);

        // Create rendition and delete file3 to be moved in trashcan
        restClient.withCoreAPI().usingNode(file3).createNodeRendition("pdf");
        restClient.withCoreAPI().usingNode(file3).createNodeRendition("doclib");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        dataContent.usingUser(adminUserModel).usingResource(file3).deleteContent();

        // GET /deleted-nodes/{nodeId}/renditions
        Utility.sleep(500, 60000, () ->
        {
            nodeRenditionInfoCollection = restClient.authenticateUser(adminUserModel).withCoreAPI().usingTrashcan().getDeletedNodeRenditions(file3);
            restClient.assertStatusCodeIs(HttpStatus.OK);

            // Check if renditions are retrieved, created or not. Entries are ordered
            nodeRenditionInfoCollection.assertThat().entriesListContains("id", "doclib");
            nodeRenditionInfoCollection.getEntryByIndex(5).assertThat().field("id").is("pdf").and()
                                                                       .field("status").is("CREATED");
        });

        // GET /deleted-nodes/{nodeId}/renditions/{id}
        nodeRenditionInfo = restClient.authenticateUser(adminUserModel).withCoreAPI().usingTrashcan().getDeletedNodeRendition(file3, "pdf");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodeRenditionInfo.assertThat().field("id").is("pdf").and()
                                      .field("status").is("CREATED");

        // GET /deleted-nodes/{nodeId}/renditions/{id}/content
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingTrashcan().getDeletedNodeRenditionContent(file3, "pdf");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Type","application/pdf;charset=UTF-8");
        Assert.assertTrue(restClient.onResponse().getResponse().body().asInputStream().available() > 0);
    }

    @Bug(id = "REPO-4778")
    @TestRail(section = {TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.REQUIRE_SOLR}, executionType = ExecutionType.SANITY,
            description = "Sanity test to verify Range request header on GET /deleted-nodes/{nodeId}/renditions/{renditionId}/content endpoint")
    @Test(groups = {TestGroup.REST_API, TestGroup.TRASHCAN, TestGroup.SANITY, TestGroup.RENDITIONS})
    public void testGetDeletedNodesRenditionsandVerifyRangeRequestheader() throws Exception
    {
        // Create file4 based on existing resource
        file4 = dataContent.usingUser(adminUserModel).usingSite(deleteNodesSiteModel).createContent(DocumentType.TEXT_PLAIN);

        // Create rendition and delete file4 to be moved in trashcan
        restClient.withCoreAPI().usingNode(file4).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        dataContent.usingUser(adminUserModel).usingResource(file4).deleteContent();

        // GET /deleted-nodes/{nodeId}/renditions/{id}/content and verify range request header
        Utility.sleep(500, 60000, () ->
        {
            restClient.configureRequestSpec().addHeader("content-range", "bytes=1-10");
            restClient.authenticateUser(adminUserModel).withCoreAPI().usingTrashcan().getDeletedNodeRenditionContent(file4, "pdf");
            restClient.assertStatusCodeIs(HttpStatus.PARTIAL_CONTENT);
            restClient.assertHeaderValueContains("Content-Type", "application/pdf;charset=UTF-8");
            restClient.assertHeaderValueContains("content-range", "bytes 1-10");
            restClient.assertHeaderValueContains("content-length", String.valueOf(10));
        });
    }
}
