/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.search;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

/**
 * Migration test class for deep folder hierarchies (path indexing verification) on Elasticsearch.
 */
public class SearchDeepFolderHierarchyTest extends AbstractSearchServicesE2ETest
{
    private FolderModel levelOne;
    private FolderModel levelTwo;
    private FolderModel levelThree;
    private FolderModel levelFour;
    private FolderModel levelFive;
    private FolderModel levelSix;
    private FileModel deepFile;
    private FileModel additionalDeepFileOne;
    private FileModel additionalDeepFileTwo;

    private String pathBase;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        levelOne = dataContent.usingUser(testUser).usingSite(testSite)
                .createFolderCmisApi("level-one-folder");
        levelTwo = dataContent.usingUser(testUser).usingSite(testSite).usingResource(levelOne)
                .createFolderCmisApi("level-two-folder");
        levelThree = dataContent.usingUser(testUser).usingSite(testSite).usingResource(levelTwo)
                .createFolderCmisApi("level-three-folder");
        levelFour = dataContent.usingUser(testUser).usingSite(testSite).usingResource(levelThree)
                .createFolderCmisApi("level-four-folder");
        levelFive = dataContent.usingUser(testUser).usingSite(testSite).usingResource(levelFour)
                .createFolderCmisApi("level-five-folder");
        levelSix = dataContent.usingUser(testUser).usingSite(testSite).usingResource(levelFive)
                .createFolderCmisApi("level-six-folder");

        deepFile = new FileModel("deep-nested-file.txt");
        deepFile.setContent("content at the deepest level");
        dataContent.usingUser(testUser).usingResource(levelSix).createContent(deepFile);

        additionalDeepFileOne = new FileModel("deep-nested-file-two.txt");
        additionalDeepFileOne.setContent("second file in the same deepest folder");
        dataContent.usingUser(testUser).usingResource(levelSix).createContent(additionalDeepFileOne);

        additionalDeepFileTwo = new FileModel("deep-nested-file-three.txt");
        additionalDeepFileTwo.setContent("third file in the same deepest folder");
        dataContent.usingUser(testUser).usingResource(levelSix).createContent(additionalDeepFileTwo);

        pathBase = "/app:company_home/st:sites/cm:" + testSite.getTitle() + "/cm:documentLibrary";

        waitForMetadataIndexing(deepFile.getName(), true);
        waitForMetadataIndexing(additionalDeepFileOne.getName(), true);
        waitForMetadataIndexing(additionalDeepFileTwo.getName(), true);
    }

    @Test(priority = 1)
    public void testPathQueryAtDeepLevel()
    {
        String deepPath = pathBase +
                "/cm:" + levelOne.getName() +
                "/cm:" + levelTwo.getName() +
                "/cm:" + levelThree.getName() +
                "/cm:" + levelFour.getName() +
                "/cm:" + levelFive.getName() +
                "/cm:" + levelSix.getName() +
                "/cm:" + deepFile.getName();

        SearchResponse response = queryAsUser(testUser, "PATH:\"" + deepPath + "\"");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected the deeply-nested file to be found at its exact path");
    }

    @Test(priority = 2)
    public void testWildcardPathAcrossLevels()
    {
        String wildcardPath = pathBase +
                "/cm:" + levelOne.getName() +
                "//*";

        SearchResponse response = queryAsUser(testUser,
                "PATH:\"" + wildcardPath + "\" AND cm:name:'" + deepFile.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected the deeply-nested file to be found via wildcard path");
    }

    @Test(priority = 3)
    public void testPathQueryAtIntermediateLevel()
    {
        String intermediatePath = pathBase +
                "/cm:" + levelOne.getName() +
                "/cm:" + levelTwo.getName() +
                "/cm:" + levelThree.getName();

        SearchResponse response = queryAsUser(testUser, "PATH:\"" + intermediatePath + "\"");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected the intermediate-level folder to be found at its path");
    }

    @Test(priority = 4)
    public void testParentQueryAtDeepLevel()
    {
        String deepestFolderPath = pathBase +
                "/cm:" + levelOne.getName() +
                "/cm:" + levelTwo.getName() +
                "/cm:" + levelThree.getName() +
                "/cm:" + levelFour.getName() +
                "/cm:" + levelFive.getName() +
                "/cm:" + levelSix.getName();

        SearchResponse folderLookup = queryAsUser(testUser, "PATH:\"" + deepestFolderPath + "\"");
        String folderId = folderLookup.getEntries().getFirst().getModel().getId();

        String parentQuery = "PARENT:'workspace://SpacesStore/" + folderId +
                "' AND cm:name:'" + deepFile.getName() + "'";

        SearchResponse response = queryAsUser(testUser, parentQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected the file to be found as the direct child of the deepest folder");
    }

    @Test(priority = 5)
    public void testPathQueryAtRootOfHierarchy()
    {
        String rootPath = pathBase + "/cm:" + levelOne.getName();
        SearchResponse response = queryAsUser(testUser, "PATH:\"" + rootPath + "\"");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected the level-one folder to be found at the root of the hierarchy");
    }

    @Test(priority = 6)
    public void testMultipleFilesInSameDeepFolder()
    {
        String deepFolderPath = pathBase +
                "/cm:" + levelOne.getName() +
                "/cm:" + levelTwo.getName() +
                "/cm:" + levelThree.getName() +
                "/cm:" + levelFour.getName() +
                "/cm:" + levelFive.getName() +
                "/cm:" + levelSix.getName() +
                "/*";

        SearchResponse response = queryAsUser(testUser, "PATH:\"" + deepFolderPath + "\"");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 3,
                "Expected all three files at the deepest level to be found");
    }

    @Test(priority = 7)
    public void testTypeAndPathCombined()
    {
        String deepFolderPath = pathBase +
                "/cm:" + levelOne.getName() +
                "//*";

        String query = "TYPE:'cm:content' AND PATH:\"" + deepFolderPath + "\"";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 3,
                "Expected at least the three files under the hierarchy to be returned by TYPE+PATH");
    }

    @Test(priority = 8)
    public void testDirectChildrenViaPathWildcard()
    {
        String midLevelChildrenPath = pathBase +
                "/cm:" + levelOne.getName() +
                "/cm:" + levelTwo.getName() +
                "/cm:" + levelThree.getName() +
                "/*";

        SearchResponse response = queryAsUser(testUser, "PATH:\"" + midLevelChildrenPath + "\"");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected exactly the level-four folder as the direct child of level-three");
    }
}
