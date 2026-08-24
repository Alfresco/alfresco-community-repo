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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.RetryOperation;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.UserModel;

/**
 * End-to-end regression test for tag filtering (MNT-25799: a tag containing a space used to return no documents or every document).
 * <p>
 * Tags two documents through the public v1 REST API, waits for the live index to catch up, then queries the public Search API with {@code TAG:'<tag>'} and asserts the filter returns exactly the tagged document - for both a single-word tag and a tag containing a space.
 * <p>
 * The test lives in {@code org.alfresco.rest.search} so it is picked up by the part2 suite ({@code part2-suite.xml} includes the whole package). The public Search API is available on both the Solr-backed community stack and Elasticsearch, unlike the Share slingshot doclist webscript ({@code share-services}), which is not present in the community image.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.LongVariable"})
public class DocumentLibraryTagFilterTest extends RestTest
{
    private UserModel testUser;

    private String singleWordTag;
    private String spaceTag;

    private FileModel singleWordTaggedFile;
    private FileModel spaceTaggedFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        // Own the site with a dedicated user so the same user can add both content and tags.
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();

        // Unique suffix keeps the tags private to this test run (tags are repo-wide, not site-scoped).
        String unique = RandomData.getRandomName("Tag").toLowerCase();
        singleWordTag = "single" + unique;
        spaceTag = "long " + unique; // contains a space - the scenario that used to fail

        singleWordTaggedFile = createTaggedFile(singleWordTag);
        spaceTaggedFile = createTaggedFile(spaceTag);

        // Wait until both tags resolve through the Search API (cm:taggable indexed).
        waitForTagIndexed(singleWordTag, singleWordTaggedFile.getName());
        waitForTagIndexed(spaceTag, spaceTaggedFile.getName());
    }

    /** A tag containing a space must return exactly the document it was applied to. */
    @Test
    public void tagFilterWithSpaceInTagNameReturnsOnlyTheTaggedDocument()
    {
        assertTagFilterReturnsExactly(spaceTag, spaceTaggedFile.getName(), singleWordTaggedFile.getName());
    }

    /** Regression guard: single-word tags keep working exactly as before. */
    @Test
    public void tagFilterWithSingleWordTagReturnsOnlyTheTaggedDocument()
    {
        assertTagFilterReturnsExactly(singleWordTag, singleWordTaggedFile.getName(), spaceTaggedFile.getName());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Creates a text document in the test site's document library and tags it via the public v1 REST API. */
    private FileModel createTaggedFile(String tag)
    {
        FileModel file = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "MNT-25799 tag filter test content");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        restClient.authenticateUser(testUser).withCoreAPI().usingResource(file).addTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        return file;
    }

    /** Runs the tag filter and asserts it returns exactly the expected file and never the other (unrelated) file. */
    private void assertTagFilterReturnsExactly(String tag, String expectedFileName, String excludedFileName)
    {
        List<String> fileNames = tagFilter(tag);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        assertTrue(fileNames.contains(expectedFileName),
                "Tag filter '" + tag + "' did not return the tagged document '" + expectedFileName + "'. Got: " + fileNames);
        assertFalse(fileNames.contains(excludedFileName),
                "Tag filter '" + tag + "' incorrectly returned an unrelated document '" + excludedFileName + "'. Got: " + fileNames);
        assertEquals(fileNames.size(), 1,
                "Tag filter '" + tag + "' returned an unexpected number of documents. Got: " + fileNames);
    }

    /** Polls the Search API until {@code expectedFileName} resolves for the tag (index catch-up). */
    private void waitForTagIndexed(String tag, String expectedFileName) throws Exception
    {
        RetryOperation op = () -> assertTrue(tagFilter(tag).contains(expectedFileName),
                "Tag not searchable yet: " + tag);
        Utility.sleep(300, 100000, op);
    }

    /** Runs an AFTS {@code TAG:'<tag>'} query and returns the names of the matching documents. */
    private List<String> tagFilter(String tag)
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setLanguage("afts");
        queryModel.setQuery("TAG:'" + tag + "'");
        query.setQuery(queryModel);

        return restClient.authenticateUser(testUser)
                .withSearchAPI()
                .search(query)
                .getEntries()
                .stream()
                .map(SearchNodeModel::getModel)
                .map(model -> model.getName())
                .collect(Collectors.toList());
    }
}
