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

package org.alfresco.rest.search;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.exception.EmptyRestModelCollectionException;
import org.alfresco.rest.model.RestNodeAssociationModelCollection;
import org.alfresco.rest.model.RestNodeChildAssociationModel;
import org.alfresco.utility.data.CustomObjectTypeProperties;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

/**
 * Migration test class for advanced secondary parent/child association scenarios on Elasticsearch.
 */
public class SearchSecondaryAssociationAdvancedTest extends AbstractSearchServicesE2ETest
{
    private FolderModel primaryFolder;
    private FolderModel secondaryFolderA;
    private FolderModel secondaryFolderB;
    private FolderModel nestedParent;
    private FolderModel nestedChild;
    private FolderModel commonSecondaryFolder;
    private FileModel file;
    private FileModel nestedTargetFile;
    private FileModel siblingFileOne;
    private FileModel siblingFileTwo;

    private String pathBase;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        primaryFolder = new FolderModel("primary-parent-folder");
        secondaryFolderA = new FolderModel("secondary-parent-folder-a");
        secondaryFolderB = new FolderModel("secondary-parent-folder-b");
        nestedParent = new FolderModel("nested-parent-folder");
        nestedChild = new FolderModel("nested-child-folder");
        commonSecondaryFolder = new FolderModel("common-secondary-folder");

        file = new FileModel("multi-secondary-file.txt");
        file.setContent("File that will have multiple secondary parents");
        nestedTargetFile = new FileModel("nested-secondary-file.txt");
        nestedTargetFile.setContent("File to be secondary-associated to a nested folder");
        siblingFileOne = new FileModel("sibling-file-one.txt");
        siblingFileTwo = new FileModel("sibling-file-two.txt");

        dataContent.usingUser(testUser).usingSite(testSite)
                .createCustomContent(primaryFolder, "cmis:folder", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingSite(testSite)
                .createCustomContent(secondaryFolderA, "cmis:folder", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingSite(testSite)
                .createCustomContent(secondaryFolderB, "cmis:folder", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingSite(testSite)
                .createCustomContent(nestedParent, "cmis:folder", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingResource(nestedParent)
                .createCustomContent(nestedChild, "cmis:folder", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingSite(testSite)
                .createCustomContent(commonSecondaryFolder, "cmis:folder", new CustomObjectTypeProperties());

        dataContent.usingUser(testUser).usingResource(primaryFolder)
                .createCustomContent(file, "cmis:document", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingResource(primaryFolder)
                .createCustomContent(nestedTargetFile, "cmis:document", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingResource(primaryFolder)
                .createCustomContent(siblingFileOne, "cmis:document", new CustomObjectTypeProperties());
        dataContent.usingUser(testUser).usingResource(primaryFolder)
                .createCustomContent(siblingFileTwo, "cmis:document", new CustomObjectTypeProperties());

        pathBase = "/app:company_home/st:sites/cm:" + testSite.getTitle() + "/cm:documentLibrary";

        waitForMetadataIndexing(file.getName(), true);
        waitForMetadataIndexing(nestedTargetFile.getName(), true);
        waitForMetadataIndexing(siblingFileOne.getName(), true);
        waitForMetadataIndexing(siblingFileTwo.getName(), true);
    }

    @Test(priority = 1)
    public void testMultipleSecondaryAssociationsInSameSite() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assocA = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).addSecondaryChildren(assocA);

        RestNodeChildAssociationModel assocB = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).addSecondaryChildren(assocB);

        String pathViaA = pathBase + "/cm:" + secondaryFolderA.getName() + "/cm:" + file.getName();
        String pathViaB = pathBase + "/cm:" + secondaryFolderB.getName() + "/cm:" + file.getName();
        String pathViaPrimary = pathBase + "/cm:" + primaryFolder.getName() + "/cm:" + file.getName();

        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaA + "\"", file.getName(), true),
                "File not findable via first secondary parent path");
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaB + "\"", file.getName(), true),
                "File not findable via second secondary parent path");
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaPrimary + "\"", file.getName(), true),
                "File not findable via primary parent path");

        RestNodeAssociationModelCollection secChildrenA = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).removeSecondaryChild(secChildrenA.getEntryByIndex(0));

        RestNodeAssociationModelCollection secChildrenB = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).removeSecondaryChild(secChildrenB.getEntryByIndex(0));
    }

    @Test(priority = 2)
    public void testNestedSecondaryPathQuery() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assoc = new RestNodeChildAssociationModel(nestedTargetFile.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).addSecondaryChildren(assoc);

        String pathViaNestedSecondary = pathBase +
                "/cm:" + nestedParent.getName() +
                "/cm:" + nestedChild.getName() +
                "/cm:" + nestedTargetFile.getName();

        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaNestedSecondary + "\"", nestedTargetFile.getName(), true),
                "File not findable via nested secondary parent path");

        RestNodeAssociationModelCollection secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).removeSecondaryChild(secChildren.getEntryByIndex(0));
    }

    @Test(priority = 3)
    public void testSecondaryAssociationRemovalUpdatesIndex() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assoc = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).addSecondaryChildren(assoc);

        String pathViaSecondary = pathBase + "/cm:" + secondaryFolderA.getName() + "/cm:" + file.getName();
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaSecondary + "\"", file.getName(), true),
                "File should be findable after secondary association is added");

        RestNodeAssociationModelCollection secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).removeSecondaryChild(secChildren.getEntryByIndex(0));

        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaSecondary + "\"", file.getName(), false),
                "File should NOT be findable after secondary association is removed");
    }

    @Test(priority = 4)
    public void testFileFoundOnceGloballyDespiteMultipleAssociations() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assocA = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).addSecondaryChildren(assocA);
        RestNodeChildAssociationModel assocB = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).addSecondaryChildren(assocB);

        SearchResponse response = queryAsUser(testUser, "cm:name:'" + file.getName() + "'");
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Node with multiple secondary parents should still be indexed as a single entry");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()),
                "Expected the single returned entry to be " + file.getName());

        RestNodeAssociationModelCollection secChildrenA = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).removeSecondaryChild(secChildrenA.getEntryByIndex(0));
        RestNodeAssociationModelCollection secChildrenB = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderB).removeSecondaryChild(secChildrenB.getEntryByIndex(0));
    }

    @Test(priority = 5)
    public void testMultipleFilesInSameSecondaryFolder() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assoc1 = new RestNodeChildAssociationModel(siblingFileOne.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(commonSecondaryFolder).addSecondaryChildren(assoc1);
        RestNodeChildAssociationModel assoc2 = new RestNodeChildAssociationModel(siblingFileTwo.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(commonSecondaryFolder).addSecondaryChildren(assoc2);

        String pathViaCommon = pathBase + "/cm:" + commonSecondaryFolder.getName() + "/*";

        // Wait for the secondary-association PATH to be indexed for both files
        // (batch-indexing catches up on association changes with a small lag).
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaCommon + "\"", siblingFileOne.getName(), true),
                "First file should be findable via the common secondary folder path");
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaCommon + "\"", siblingFileTwo.getName(), true),
                "Second file should be findable via the common secondary folder path");

        SearchResponse response = queryAsUser(testUser, "PATH:\"" + pathViaCommon + "\"");
        Assert.assertEquals(response.getPagination().getCount(), 2,
                "Both files should be findable via the common secondary folder path");
        Assert.assertTrue(isContentInSearchResponse(response, siblingFileOne.getName()),
                "Expected " + siblingFileOne.getName() + " among the common secondary folder children");
        Assert.assertTrue(isContentInSearchResponse(response, siblingFileTwo.getName()),
                "Expected " + siblingFileTwo.getName() + " among the common secondary folder children");

        RestNodeAssociationModelCollection secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(commonSecondaryFolder).getSecondaryChildren();
        while (!secChildren.getEntries().isEmpty())
        {
            restClient.authenticateUser(testUser).withCoreAPI().usingResource(commonSecondaryFolder).removeSecondaryChild(secChildren.getEntryByIndex(0));
            secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(commonSecondaryFolder).getSecondaryChildren();
        }
    }

    @Test(priority = 6)
    public void testPathViaPrimaryStillWorksAfterSecondaryAdded() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assoc = new RestNodeChildAssociationModel(file.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).addSecondaryChildren(assoc);

        String pathViaPrimary = pathBase + "/cm:" + primaryFolder.getName() + "/cm:" + file.getName();
        Assert.assertTrue(isContentInSearchResults("PATH:\"" + pathViaPrimary + "\"", file.getName(), true),
                "File must still be findable via primary parent path after a secondary association is added");

        RestNodeAssociationModelCollection secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(secondaryFolderA).removeSecondaryChild(secChildren.getEntryByIndex(0));
    }

    @Test(priority = 7)
    public void testSecondaryAssociationOnDeeplyNestedFileFoundViaSecondaryPath() throws EmptyRestModelCollectionException
    {
        RestNodeChildAssociationModel assoc = new RestNodeChildAssociationModel(nestedTargetFile.getNodeRefWithoutVersion(), "cm:contains");
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).addSecondaryChildren(assoc);

        String secondaryDeepPath = pathBase +
                "/cm:" + nestedParent.getName() +
                "/cm:" + nestedChild.getName() +
                "/cm:" + nestedTargetFile.getName();

        Assert.assertTrue(isContentInSearchResults("PATH:\"" + secondaryDeepPath + "\"", nestedTargetFile.getName(), true),
                "File must be findable at its secondary deep path");

        RestNodeAssociationModelCollection secChildren = restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).getSecondaryChildren();
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(nestedChild).removeSecondaryChild(secChildren.getEntryByIndex(0));
    }
}
