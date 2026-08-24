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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import io.restassured.path.json.JsonPath;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.UserModel;

/**
 * End-to-end test for the Share Document Library "tag" filter (slingshot {@code doclist} webscript, driven by {@code filters.lib.js}) running against a real search server.
 * <p>
 * clicking a tag that contains a space used to return either no documents or every document. This test tags documents through the public v1 REST API, waits for the live index to catch up, then calls the same {@code /slingshot/doclib2/doclist} endpoint the Share UI uses and asserts that the tag filter returns exactly the tagged document - for both a single-word tag and a tag containing a space.
 * <p>
 * The test lives in {@code org.alfresco.rest.search} so it is picked up automatically by the Elasticsearch E2E suite ({@code elasticsearch-e2e-suite.xml}), proving the fix works against an Elasticsearch server.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.LongVariable"})
public class DocumentLibraryTagFilterTest extends RestTest
{
    /** Webscript service prefix for the slingshot doclist endpoint (equivalent to {@code /alfresco/s}). */
    private static final String DOCLIST_BASE_PATH = "alfresco/service/slingshot/doclib2/doclist";

    /** Default Share Document Library container name. */
    private static final String DOCUMENT_LIBRARY = "documentLibrary";

    /** Maximum number of polling attempts while waiting for a tag to become searchable. */
    private static final int SEARCH_MAX_ATTEMPTS = 30;

    private UserModel testUser;

    private String singleWordTag;
    private String spaceTag;

    private FileModel singleWordTaggedFile;
    private FileModel spaceTaggedFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        // Own the site with a dedicated user so the same user can add both content and tags.
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();

        // Unique suffix keeps the tags private to this test run (the tag filter is repo-wide, not site-scoped).
        String unique = RandomData.getRandomName("Tag").toLowerCase();
        singleWordTag = "single" + unique;
        spaceTag = "long " + unique; // contains a space - the scenario that used to fail

        singleWordTaggedFile = createTaggedFile(singleWordTag);
        spaceTaggedFile = createTaggedFile(spaceTag);

        // Wait until both tags resolve through the doclist endpoint (category node + cm:taggable both indexed).
        assertTrue(waitForTagFilter(singleWordTag, singleWordTaggedFile.getName()),
                "Single-word tag was not indexed/searchable in time: " + singleWordTag);
        assertTrue(waitForTagFilter(spaceTag, spaceTaggedFile.getName()),
                "Space-containing tag was not indexed/searchable in time: " + spaceTag);
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
        JsonPath json = tagFilter(tag);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        List<String> fileNames = json.getList("items.location.file");
        assertNotNull(fileNames, "Doclist response did not contain an items list for tag: " + tag);
        assertTrue(fileNames.contains(expectedFileName),
                "Tag filter '" + tag + "' did not return the tagged document '" + expectedFileName + "'. Got: " + fileNames);
        assertFalse(fileNames.contains(excludedFileName),
                "Tag filter '" + tag + "' incorrectly returned an unrelated document '" + excludedFileName + "'. Got: " + fileNames);
        assertEquals(json.getInt("totalRecords"), 1,
                "Tag filter '" + tag + "' returned an unexpected number of documents. Got: " + fileNames);
    }

    /** Polls the doclist tag filter until {@code expectedFileName} appears or the retry budget is exhausted. */
    private boolean waitForTagFilter(String tag, String expectedFileName)
    {
        for (int attempt = 0; attempt < SEARCH_MAX_ATTEMPTS; attempt++)
        {
            JsonPath json = tagFilter(tag);
            if (String.valueOf(HttpStatus.OK.value()).equals(restClient.getStatusCode()))
            {
                List<String> fileNames = json.getList("items.location.file");
                if (fileNames != null && fileNames.contains(expectedFileName))
                {
                    return true;
                }
            }
            Utility.waitToLoopTime(properties.getSolrWaitTimeInSeconds(),
                    "Waiting for tag to be indexed. Attempt: " + (attempt + 1));
        }
        return false;
    }

    /**
     * Calls the slingshot doclist webscript with the tag filter, as the Share UI does: {@code GET /alfresco/s/slingshot/doclib2/doclist/all/site/{site}/documentLibrary?filter=tag&filterData=<tag>}.
     */
    private JsonPath tagFilter(String tag)
    {
        restClient.authenticateUser(testUser);
        restClient.configureRequestSpec().setBasePath(DOCLIST_BASE_PATH);

        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET,
                "all/site/{site}/{container}?filter=tag&filterData={filterData}",
                testSite.getId(), DOCUMENT_LIBRARY, tag);
        return restClient.process(request).getResponse().jsonPath();
    }
}
