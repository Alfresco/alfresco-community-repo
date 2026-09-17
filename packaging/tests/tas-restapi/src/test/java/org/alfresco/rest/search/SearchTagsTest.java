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

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

/**
 * Migration test class for tag-based search scenarios on Elasticsearch.
 */
public class SearchTagsTest extends AbstractSearchServicesE2ETest
{
    private FileModel fileWithSingleTag;
    private FileModel fileWithMultipleTags;
    private FileModel anotherFileWithTagOne;
    private FileModel fileWithAllThreeTags;

    private static final String TAG_PREFIX = "acsmigrationtag";
    private static final String TAG_ONE = TAG_PREFIX + "one";
    private static final String TAG_TWO = TAG_PREFIX + "two";
    private static final String TAG_THREE = TAG_PREFIX + "three";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        FolderModel folder = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi("tags-folder");

        fileWithSingleTag = new FileModel("file-with-single-tag.txt");
        fileWithSingleTag.setContent("File with a single tag");
        dataContent.usingUser(testUser).usingResource(folder).createContent(fileWithSingleTag);

        fileWithMultipleTags = new FileModel("file-with-multiple-tags.txt");
        fileWithMultipleTags.setContent("File with multiple tags");
        dataContent.usingUser(testUser).usingResource(folder).createContent(fileWithMultipleTags);

        anotherFileWithTagOne = new FileModel("another-file-with-tag-one.txt");
        anotherFileWithTagOne.setContent("Second file also tagged with TAG_ONE");
        dataContent.usingUser(testUser).usingResource(folder).createContent(anotherFileWithTagOne);

        fileWithAllThreeTags = new FileModel("file-with-all-three-tags.txt");
        fileWithAllThreeTags.setContent("File tagged with all three migration tags");
        dataContent.usingUser(testUser).usingResource(folder).createContent(fileWithAllThreeTags);

        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithSingleTag).addTag(TAG_ONE);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithMultipleTags).addTag(TAG_TWO);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithMultipleTags).addTag(TAG_THREE);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(anotherFileWithTagOne).addTag(TAG_ONE);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithAllThreeTags).addTag(TAG_ONE);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithAllThreeTags).addTag(TAG_TWO);
        restClient.authenticateUser(testUser).withCoreAPI().usingResource(fileWithAllThreeTags).addTag(TAG_THREE);

        waitForMetadataIndexing(fileWithSingleTag.getName(), true);
        waitForMetadataIndexing(fileWithMultipleTags.getName(), true);
        waitForMetadataIndexing(anotherFileWithTagOne.getName(), true);
        waitForMetadataIndexing(fileWithAllThreeTags.getName(), true);

        // Wait until the TAG index catches up — batch-indexing needs time after tag-aspect
        // and cm:categories property changes before TAG queries start returning results.
        Assert.assertTrue(isContentInSearchResults("TAG:'" + TAG_ONE + "'", fileWithSingleTag.getName(), true),
                "Setup: TAG_ONE should be searchable after batch-indexing catches up");
        Assert.assertTrue(isContentInSearchResults("TAG:'" + TAG_TWO + "'", fileWithMultipleTags.getName(), true),
                "Setup: TAG_TWO should be searchable after batch-indexing catches up");
        Assert.assertTrue(isContentInSearchResults("TAG:'" + TAG_THREE + "'", fileWithMultipleTags.getName(), true),
                "Setup: TAG_THREE should be searchable after batch-indexing catches up");
    }

    @Test(priority = 1)
    public void testSearchByTag()
    {
        SearchResponse response = queryAsUser(testUser, "TAG:'" + TAG_ONE + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected at least one file tagged with " + TAG_ONE);
        Assert.assertTrue(isContentInSearchResponse(response, fileWithSingleTag.getName()),
                "Expected " + fileWithSingleTag.getName() + " to be returned for TAG_ONE");
    }

    @Test(priority = 2)
    public void testSearchByMultipleTagsOnSameFile()
    {
        SearchResponse responseTwo = queryAsUser(testUser, "TAG:'" + TAG_TWO + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(responseTwo.getPagination().getCount() >= 1,
                "Expected the multi-tagged file to be findable via TAG_TWO");
        Assert.assertTrue(isContentInSearchResponse(responseTwo, fileWithMultipleTags.getName()),
                "Expected " + fileWithMultipleTags.getName() + " to be returned for TAG_TWO");

        SearchResponse responseThree = queryAsUser(testUser, "TAG:'" + TAG_THREE + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(responseThree.getPagination().getCount() >= 1,
                "Expected the multi-tagged file to be findable via TAG_THREE");
        Assert.assertTrue(isContentInSearchResponse(responseThree, fileWithMultipleTags.getName()),
                "Expected " + fileWithMultipleTags.getName() + " to be returned for TAG_THREE");
    }

    @Test(priority = 3)
    public void testTagAndNameCombined()
    {
        String query = "TAG:'" + TAG_ONE + "' AND cm:name:'" + fileWithSingleTag.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected exactly the one tagged file with matching name");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithSingleTag.getName()),
                "Expected the returned entry to be " + fileWithSingleTag.getName());
    }

    @Test(priority = 4)
    public void testTagWithWildcard()
    {
        SearchResponse response = queryAsUser(testUser, "TAG:'" + TAG_PREFIX + "*'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 3,
                "Expected wildcard TAG query to find all files sharing the migration tag prefix");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithSingleTag.getName()),
                "Expected " + fileWithSingleTag.getName() + " in the wildcard TAG results");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithMultipleTags.getName()),
                "Expected " + fileWithMultipleTags.getName() + " in the wildcard TAG results");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithAllThreeTags.getName()),
                "Expected " + fileWithAllThreeTags.getName() + " in the wildcard TAG results");
    }

    @Test(priority = 5)
    public void testTagsWithConjunction()
    {
        String query = "TAG:'" + TAG_TWO + "' AND TAG:'" + TAG_THREE + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected at least one file tagged with both TAG_TWO and TAG_THREE");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithMultipleTags.getName()),
                "Expected " + fileWithMultipleTags.getName() + " to satisfy the TAG_TWO AND TAG_THREE conjunction");
    }

    @Test(priority = 6)
    public void testTagsWithDisjunction()
    {
        String query = "TAG:'" + TAG_ONE + "' OR TAG:'" + TAG_TWO + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 3,
                "Expected disjunction TAG query to return files matching either tag");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithSingleTag.getName()),
                "Expected " + fileWithSingleTag.getName() + " (TAG_ONE) in the disjunction results");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithMultipleTags.getName()),
                "Expected " + fileWithMultipleTags.getName() + " (TAG_TWO) in the disjunction results");
    }

    @Test(priority = 7)
    public void testMultipleFilesShareTag()
    {
        SearchResponse response = queryAsUser(testUser, "TAG:'" + TAG_ONE + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 3,
                "Expected TAG_ONE to be shared by at least three files");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithSingleTag.getName()),
                "Expected " + fileWithSingleTag.getName() + " to share TAG_ONE");
        Assert.assertTrue(isContentInSearchResponse(response, anotherFileWithTagOne.getName()),
                "Expected " + anotherFileWithTagOne.getName() + " to share TAG_ONE");
        Assert.assertTrue(isContentInSearchResponse(response, fileWithAllThreeTags.getName()),
                "Expected " + fileWithAllThreeTags.getName() + " to share TAG_ONE");
    }
}
