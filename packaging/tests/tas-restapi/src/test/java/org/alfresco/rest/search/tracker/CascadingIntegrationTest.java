/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.search.tracker;

import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.search.AbstractE2EFunctionalTest;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;

/**
 * Test class tests cascading updates for a child node when parent node is updated
 */
public class CascadingIntegrationTest extends AbstractE2EFunctionalTest
{
    @Autowired
    protected DataContent dataContent;
    private FolderModel parentFolderSharded;
    private FileModel firstChildFileSharded;
    private FileModel secondChildFileSharded;

    @BeforeClass(alwaysRun = true)
    public void setupEnvironment()
    {
        assertTrue(deployCustomModel("sharding-content-model.xml"),
                "failing while deploying sharding model");
    }

    @AfterClass
    public void cleanUpEnvironment()
    {
        if (firstChildFileSharded != null)
        {
            dataContent.usingSite(testSite).usingUser(testUser).usingResource(firstChildFileSharded).deleteContent();
            restClient.withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(firstChildFileSharded);
        }

        if (secondChildFileSharded != null)
        {
            dataContent.usingSite(testSite).usingUser(testUser).usingResource(secondChildFileSharded).deleteContent();
            restClient.withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(secondChildFileSharded);
        }

        if (parentFolderSharded != null)
        {
            dataContent.usingSite(testSite).usingUser(testUser).usingResource(parentFolderSharded).deleteContent();
            restClient.withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(parentFolderSharded);
        }

        dataContent.deleteSite(testSite);
        assertTrue(deactivateCustomModel("sharding-content-model.xml"),
                "failing while deactivating sharding model");
        assertTrue(deleteCustomModel("sharding-content-model.xml"),
                "failing while removing sharding model");
    }

    @Test(priority = 1)
    public void testChildPathWhenParentRenamed()
    {
        // Create Parent folder
        FolderModel parentFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();

        // Create a file in the parent folder
        FileModel childFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");

        cmisApi.authenticateUser(testUser).usingSite(testSite).usingResource(parentFolder)
                .createFile(childFile,
                        Map.of(PropertyIds.NAME, childFile.getName(),
                                PropertyIds.OBJECT_TYPE_ID, "cmis:document"),
                        VersioningState.MAJOR)
                .assertThat().existsInRepo();

        // Query to find nodes where Path with original folder name matches
        String parentQuery = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + parentFolder.getName() + "/*\"";

        // Rename parent folder
        String parentNewName = "parentRenamed";
        parentFolder.setName(parentNewName);

        ContentModel parentNewNameModel = new ContentModel(parentNewName);
        dataContent.usingUser(testUser).usingResource(parentFolder).renameContent(parentNewNameModel);

        waitForMetadataIndexing(parentNewName, true);

        // Find nodes where Path with new folder name matches
        String parentQueryAfterRename = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + parentNewName + "/*\"";
        boolean indexingInProgress = !isContentInSearchResults(parentQueryAfterRename, childFile.getName(), true);

        // Query using new parent name: Expect child file
        int descendantCountOfNewName = query(parentQueryAfterRename).getPagination().getCount();
        Assert.assertEquals(descendantCountOfNewName, 1, String.format("Indexing in progress: %s New renamed path has not the same descendants as before renaming: %s", indexingInProgress, parentQueryAfterRename));

        // Query using old parent name: Expect no descendant after rename
        int descendantCountOfOriginalName = query(parentQuery).getPagination().getCount();
        Assert.assertEquals(descendantCountOfOriginalName, 0, "Old path still has descendants: " + parentQuery);
    }

    @Test(priority = 2)
    public void testGrandChildPathWhenGrandParentRenamed()
    {
        // Create grand parent folder
        FolderModel grandParentFolder = dataContent.usingSite(testSite).usingUser(testUser).createFolder();

        // Create child folder
        FolderModel childFolder = dataContent.usingUser(testUser).usingResource(grandParentFolder).createFolder();

        // Create grand child file
        FileModel grandChildFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");

        cmisApi.authenticateUser(testUser).usingSite(testSite).usingResource(childFolder)
                .createFile(grandChildFile,
                        Map.of(PropertyIds.NAME, grandChildFile.getName(),
                                PropertyIds.OBJECT_TYPE_ID, "cmis:document"),
                        VersioningState.MAJOR)
                .assertThat().existsInRepo();

        // Wait for file to be indexed
        waitForMetadataIndexing(grandChildFile.getName(), true);

        // Query to find nodes where Path with original folder name matches

        String parentQuery = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + grandParentFolder.getName() + "/*\"";

        // Rename grand parent folder
        String grandParentNewName = "grandParentRenamed";
        grandParentFolder.setName(grandParentNewName);

        ContentModel grandParentFolderRenamed = new ContentModel(grandParentNewName);
        dataContent.usingUser(testUser).usingResource(grandParentFolder).renameContent(grandParentFolderRenamed);

        // Find nodes where Path with new folder name matches
        String childrenQueryAfterRename = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + grandParentNewName + "/*\"";

        String grandChildrenQueryAfterRename = "PATH:\"app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + grandParentNewName + "/cm:" + childFolder.getName() + "/*\"";

        boolean indexingInProgress = !isContentInSearchResults(grandChildrenQueryAfterRename, grandChildFile.getName(), true);

        // Query using new parent name: Expect grandchild file
        int grandChildrenCountOfNewName = query(grandChildrenQueryAfterRename).getPagination().getCount();
        Assert.assertEquals(grandChildrenCountOfNewName, 1,
                String.format("Indexing in progress: %s New renamed path has not the same descendants as before renaming: %s",
                        indexingInProgress,
                        grandChildrenQueryAfterRename));

        // Query using new parent name: Expect child folder
        int childrenCountOfNewName = query(childrenQueryAfterRename).getPagination().getCount();
        Assert.assertEquals(childrenCountOfNewName, 1,
                String.format("Indexing in progress: %s New renamed path has not the same descendants as before renaming: %s",
                        indexingInProgress,
                        childrenQueryAfterRename));

        // Query using old parent name: Expect no descendant after rename
        int descendantCountOfOriginalName = query(parentQuery).getPagination().getCount();
        Assert.assertEquals(descendantCountOfOriginalName, 0, "Old path still has descendants: " + parentQuery);
    }

    /**
     * Index three nodes (parent folder and two files) in two different shards. Check that, after parent renaming, both the children are searchable in the new path (computed accordingly with the new parent folder name)
     */
    @Test(priority = 3)
    public void testChildrenPathOnParentRenamedWithChildrenInDifferentShards()
    {

        // Create Parent folder. It will be indexed in shard 0
        parentFolderSharded = FolderModel.getRandomFolderModel();

        List<String> secondaryTypes = List.of("P:shard:sharding");
        Map<String, Object> parentProperties = Map.of(PropertyIds.NAME, parentFolderSharded.getName(),
                PropertyIds.OBJECT_TYPE_ID, "cmis:folder",
                "cmis:secondaryObjectTypeIds", secondaryTypes,
                "shard:shardId", "0");

        // Create a first child in parent folder. It will be indexed in the parent shard (shard 0)
        firstChildFileSharded = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");
        Map<String, Object> propertiesFirstChild = Map.of(PropertyIds.NAME, firstChildFileSharded.getName(),
                PropertyIds.OBJECT_TYPE_ID, "cmis:document",
                "cmis:secondaryObjectTypeIds", secondaryTypes,
                "shard:shardId", "0");

        // Create a second child in parent folder. It will be indexed in shard 1.
        secondChildFileSharded = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");
        Map<String, Object> propertiesSecondChild = Map.of(PropertyIds.NAME, secondChildFileSharded.getName(),
                PropertyIds.OBJECT_TYPE_ID, "cmis:document",
                "cmis:secondaryObjectTypeIds", secondaryTypes,
                "shard:shardId", "1");

        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolderSharded, parentProperties).then()
                .usingResource(parentFolderSharded)
                .createFile(firstChildFileSharded, propertiesFirstChild, VersioningState.MAJOR)
                .createFile(secondChildFileSharded, propertiesSecondChild, VersioningState.MAJOR);

        // Check everything is indexed
        assertTrue(waitForIndexing(firstChildFileSharded.getName(), true), "file: " + firstChildFileSharded.getName() + " has not been indexed.");
        assertTrue(waitForIndexing(secondChildFileSharded.getName(), true), "file: " + secondChildFileSharded.getName() + " has not been indexed.");
        assertTrue(waitForIndexing(parentFolderSharded.getName(), true), "file: " + parentFolderSharded.getName() + " has not been indexed.");

        // Query to find nodes where Path with original folder name matches
        String parentQuery = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + parentFolderSharded.getName() + "/*\"";

        // Rename parent folder
        String parentNewName = "parentRenamedSharding";
        parentFolderSharded.setName(parentNewName);
        ContentModel parentNewNameModel = new ContentModel(parentNewName);
        dataContent.usingUser(testUser).usingResource(parentFolderSharded).renameContent(parentNewNameModel);

        String parentQueryAfterRename = "PATH:\"/app:company_home/st:sites/cm:" + testSite.getTitle() +
                "/cm:documentLibrary/cm:" + parentNewName + "/*\"";

        // Query using new parent name: Expect the two children
        int descendantCountOfNewNameBeforeUpdate = query(parentQueryAfterRename).getPagination().getCount();
        Assert.assertEquals(descendantCountOfNewNameBeforeUpdate, 0, "There should be 0 results performing the new query before updating parent name");

        assertTrue(waitForMetadataIndexing(parentNewName, true), "failing while renaming " + parentFolderSharded.getName() + " to " + parentNewName);

        boolean indexingInProgress = !isContentInSearchResults(parentQueryAfterRename, firstChildFileSharded.getName(), true);

        // Query using new parent name: Expect the two children
        int descendantCountOfNewName = query(parentQueryAfterRename).getPagination().getCount();
        Assert.assertEquals(descendantCountOfNewName, 2, String.format("Indexing in progress: %s New renamed path has not the same descendants as before renaming: %s", indexingInProgress, parentQuery));

        // Query using old parent name: Expect no descendant after rename
        int descendantCountOfOriginalName = query(parentQuery).getPagination().getCount();
        Assert.assertEquals(descendantCountOfOriginalName, 0, "Old path still has descendants: " + parentQuery);

    }
}
