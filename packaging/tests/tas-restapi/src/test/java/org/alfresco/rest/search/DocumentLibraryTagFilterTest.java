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

import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;

/**
 * End-to-end test for tag-based document filtering, verified through the public Search REST API ({@code /alfresco/api/-default-/public/search/versions/1/search}) running against a real search server.
 * <p>
 * Clicking a tag that contains a space used to return either no documents or every document. This test tags documents through the public v1 REST API (capturing each tag's category nodeRef from the response), waits for the live index to catch up, then runs an exact {@code +=cm\:taggable:"<tagNodeRef>"} membership query and asserts that the tag filter returns exactly the tagged document - for both a single-word tag and a tag containing a space. The exact-term match on the tag nodeRef is honoured by both Solr and Elasticsearch, mirroring the Share {@code filters.lib.js} tag filter.
 * <p>
 * The test lives in {@code org.alfresco.rest.search} so it is picked up automatically by the Elasticsearch E2E suite ({@code elasticsearch-e2e-suite.xml}), proving the behaviour works against an Elasticsearch server. Using the Search API (rather than the Share {@code slingshot/doclib2/doclist} webscript) keeps the test runnable on the community-repo stack, which does not deploy the share-services module.
 */
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.LongVariable"})
public class DocumentLibraryTagFilterTest extends AbstractE2EFunctionalTest
{
    private String singleWordTag;
    private String spaceTag;

    private FileModel singleWordTaggedFile;
    private FileModel spaceTaggedFile;

    private String singleWordTagNodeRef;
    private String spaceTagNodeRef;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        // Unique suffix keeps the tags private to this test run (the tag filter is repo-wide, not site-scoped).
        String unique = RandomData.getRandomName("Tag").toLowerCase();
        singleWordTag = "single" + unique;
        spaceTag = "long " + unique; // contains a space - the scenario that used to fail

        singleWordTaggedFile = createFile();
        singleWordTagNodeRef = tagFileAndGetTagNodeRef(singleWordTaggedFile, singleWordTag);

        spaceTaggedFile = createFile();
        spaceTagNodeRef = tagFileAndGetTagNodeRef(spaceTaggedFile, spaceTag);

        // Wait until both tags resolve through the Search API (cm:taggable indexed for each document).
        assertTrue(waitForTagFilter(singleWordTagNodeRef, singleWordTaggedFile.getName()),
                "Single-word tag was not indexed/searchable in time: " + singleWordTag);
        assertTrue(waitForTagFilter(spaceTagNodeRef, spaceTaggedFile.getName()),
                "Space-containing tag was not indexed/searchable in time: " + spaceTag);
    }

    /** A tag containing a space must return exactly the document it was applied to. */
    @Test
    public void tagFilterWithSpaceInTagNameReturnsOnlyTheTaggedDocument()
    {
        assertTagFilterReturnsExactly(spaceTagNodeRef, spaceTaggedFile.getName(), singleWordTaggedFile.getName());
    }

    /** Regression guard: single-word tags keep working exactly as before. */
    @Test
    public void tagFilterWithSingleWordTagReturnsOnlyTheTaggedDocument()
    {
        assertTagFilterReturnsExactly(singleWordTagNodeRef, singleWordTaggedFile.getName(), spaceTaggedFile.getName());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Creates a text document in the test site's document library. */
    private FileModel createFile()
    {
        FileModel file = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "MNT-25799 tag filter test content");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);
        return file;
    }

    /** Tags the file via the public v1 REST API and returns the created tag category nodeRef. */
    private String tagFileAndGetTagNodeRef(FileModel file, String tag)
    {
        RestTagModel tagModel = restClient.authenticateUser(testUser).withCoreAPI().usingResource(file).addTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        return "workspace://SpacesStore/" + tagModel.getId();
    }

    /** Runs the tag filter and asserts it returns exactly the expected file and never the other (unrelated) file. */
    private void assertTagFilterReturnsExactly(String tagNodeRef, String expectedFileName, String excludedFileName)
    {
        SearchResponse response = tagFilter(tagNodeRef);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        List<String> fileNames = resultFileNames(response);
        assertTrue(fileNames.contains(expectedFileName),
                "Tag filter '" + tagNodeRef + "' did not return the tagged document '" + expectedFileName + "'. Got: " + fileNames);
        assertFalse(fileNames.contains(excludedFileName),
                "Tag filter '" + tagNodeRef + "' incorrectly returned an unrelated document '" + excludedFileName + "'. Got: " + fileNames);
        assertEquals(fileNames.size(), 1,
                "Tag filter '" + tagNodeRef + "' returned an unexpected number of documents. Got: " + fileNames);
    }

    /** Polls the Search API tag filter until {@code expectedFileName} appears or the retry budget is exhausted. */
    private boolean waitForTagFilter(String tagNodeRef, String expectedFileName)
    {
        return isContentInSearchResults(tagQuery(tagNodeRef), expectedFileName, true);
    }

    /** Runs the exact {@code +=cm\:taggable} membership search for the given tag nodeRef as {@link #testUser}. */
    private SearchResponse tagFilter(String tagNodeRef)
    {
        return query(createQuery(tagQuery(tagNodeRef)));
    }

    /**
     * Builds the AFTS query that matches documents carrying the given tag.
     * <p>
     * MNT-25799: uses an exact ({@code =}) membership match on the tag category nodeRef, mirroring the Share {@code filters.lib.js} tag filter. This form is honoured by both Solr and Elasticsearch and returns exactly the tagged document(s) - including for tags containing spaces, since the match is on the nodeRef rather than a tokenised tag phrase.
     */
    private String tagQuery(String tagNodeRef)
    {
        return "+=cm\\:taggable:\"" + tagNodeRef + "\"";
    }

    /** Extracts the {@code cm:name} of every document returned by a search response. */
    private List<String> resultFileNames(SearchResponse response)
    {
        return response.getEntries().stream()
                .map(entry -> entry.getModel().getName())
                .collect(Collectors.toList());
    }
}
