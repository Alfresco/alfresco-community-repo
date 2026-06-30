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
 * #L%
 */

package org.alfresco.rest.search;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;

public class SearchCasesTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        Assert.assertTrue(waitForContentIndexing(file4.getContent(), true));
    }

    @Test(priority = 1)
    public void testSearchNameField()
    {
        SearchResponse response = queryAsUser(testUser, "cm:name:pangram");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 2)
    public void testSearchTitleField()
    {
        SearchResponse response2 = queryAsUser(testUser, "cm:title:cars");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response2.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 3)
    public void testSearchDescriptionField()
    {
        SearchResponse response3 = queryAsUser(testUser, "cm:description:alfresco");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response3.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 4)
    public void testSearchTextFile()
    {
        SearchResponse response6 = queryAsUser(testUser, "cm:name:pangram.txt");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 5)
    public void testSearchPDFFile()
    {
        SearchResponse response6 = queryAsUser(testUser, "cm:name:cars.PDF");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 6)
    public void testSearchODTFile()
    {
        SearchResponse response6 = queryAsUser(testUser, "cm:name:" + file4.getName() + "'");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 7)
    public void testSearchPhraseQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "The quick brown fox jumps over the lazy dog");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 8)
    public void testSearchExactTermQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "=alfresco");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 9)
    public void testSearchConjunctionQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "unique AND search");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 10)
    public void testSearchDisjunctionQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "file OR discovery");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 11)
    public void testSearchNegationQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "pangram NOT pan");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 12)
    public void testSearchWildcardQueries()
    {
        SearchResponse response6 = queryAsUser(testUser, "al?res*");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        response6.assertThat().entriesListIsNotEmpty();
    }

    @Test(priority = 13)
    public void testSearchUpdateContent() throws InterruptedException
    {
        String originalText = String.valueOf(System.currentTimeMillis());
        String newText = String.valueOf(System.currentTimeMillis() + 300000);

        // Create test file to be accessed only by this test method to avoid inconsistent results when querying updates
        FileModel updateableFile = createFileWithProvidedText(originalText + ".txt", originalText);

        // Verify that 1 occurrence of the original text is found
        SearchResponse response1 = queryAsUser(testUser, "cm:content:" + originalText);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response1.getEntries().size(), 1, "Expected 1 original text before update");

        // Verify that 0 occurrences of the replacement text are found
        SearchResponse response2 = queryAsUser(testUser, "cm:content:" + newText);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response2.getEntries().size(), 0, "Expected 0 new text before update");

        // Update the content
        String newContent = "Description: Contains provided string: " + newText;
        dataContent.usingUser(adminUserModel).usingSite(testSite).usingResource(updateableFile)
                .updateContent(newContent);
        Assert.assertTrue(waitForContentIndexing(newText, true));

        // Verify that 0 occurrences of the original text are found
        SearchResponse response3 = queryAsUser(testUser, "cm:content:" + originalText);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response3.getEntries().size(), 0, "Expected 0 original text after update");

        // Verify that 1 occurrence of the replacement text is found
        SearchResponse response4 = queryAsUser(testUser, "cm:content:" + newText);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response4.getEntries().size(), 1, "Expected 1 new text before update");
    }

    /**
     * { "query": { "query": "*" }, "facetFields": { "facets": [{"field": "cm:mimetype"},{"field": "modifier"}] } }
     */
    @Test(priority = 14)
    public void searchWithFacedFields() throws InterruptedException
    {
        String uniqueText = String.valueOf(System.currentTimeMillis());

        // Create test file to be accessed only by this test method to avoid inconsistent results
        createFileWithProvidedText(uniqueText + ".ODT", uniqueText);

        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("cm:content:" + uniqueText);
        query.setQuery(queryReq);

        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> facets = new ArrayList<>();
        facets.add(new RestRequestFacetFieldModel("cm:content.mimetype"));
        facets.add(new RestRequestFacetFieldModel("modifier"));
        facetFields.setFacets(facets);
        query.setFacetFields(facetFields);

        SearchResponse response = query(query);

        Assert.assertNotNull(response.getContext().getFacetsFields());
        Assert.assertFalse(response.getContext().getFacetsFields().isEmpty());
        Assert.assertNull(response.getContext().getFacetQueries());
        Assert.assertNull(response.getContext().getFacets());

        // Look up the "modifier" facet by label instead of relying on response ordering
        RestResultBucketsModel modifierFacet = response.getContext().getFacetsFields().stream()
                .filter(f -> "modifier".equals(f.getLabel()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("'modifier' facet not found in response"));

        modifierFacet.assertThat().field("label").is("modifier");
        FacetFieldBucket bucket1 = modifierFacet.getBuckets().getFirst();
        bucket1.assertThat().field("label").is(testUser.getUsername());
        bucket1.assertThat().field("display").is("FN-" + testUser.getUsername() + " LN-" + testUser.getUsername());
        bucket1.assertThat().field("filterQuery").is("modifier:\"" + testUser.getUsername() + "\"");
        bucket1.assertThat().field("count").is(1);
    }

    @Test(priority = 15)
    public void searchSpecialCharacters()
    {
        String specialCharfileName = "è¥äæ§ç§-åæ.pdf";
        FileModel file = new FileModel(specialCharfileName, "è¥äæ§ç§-åæ¬¯¸" + "è¥äæ§ç§-åæ¬¯¸", "è¥äæ§ç§-åæ¬¯¸", FileType.TEXT_PLAIN,
                "Text file with Special Characters: " + specialCharfileName);
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        waitForIndexing(file.getName(), true);

        SearchRequest searchReq = createQuery("name:'" + specialCharfileName + "'");
        SearchResponse nodes = query(searchReq);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        int searchCount = 0;
        while (nodes.isEmpty() && searchCount < SEARCH_MAX_ATTEMPTS)
        {
            // Wait for the solr indexing (eventual consistency).
            Utility.waitToLoopTime(properties.getSolrWaitTimeInSeconds(), "Wait For Results After Indexing. Retry Attempt: " + (searchCount + 1));
            nodes = query(searchReq);
            restClient.assertStatusCodeIs(HttpStatus.OK);
            searchCount++;
        }

        nodes.assertThat().entriesListIsNotEmpty();
        restClient.onResponse().assertThat().body("list.entries.entry[0].name", Matchers.equalToIgnoringCase(specialCharfileName));
    }
}
