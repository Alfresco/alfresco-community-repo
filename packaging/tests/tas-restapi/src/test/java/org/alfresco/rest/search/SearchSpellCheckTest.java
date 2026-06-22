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

import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.model.RestRequestSpellcheckModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;

/**
 * Search end point Public API test with spell checking enabled.
 */
public class SearchSpellCheckTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
    }

    /**
     * Perform the below query { "spellcheck": {}, "query": { "userQuery": "alfrezco", "query": "cm:title:alfrezco" } } to yield a result set whose context contains a spellCheck object of type {@code searchInsteadFor} with suggestion {@code alfresco}.
     */
    @Test(priority = 1)
    public void testSearchMissSpelled()
    {
        waitForContentIndexing(file4.getContent(), true);

        // Name
        SearchRequest searchReq = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("cm:name:alfrezco");
        queryReq.setUserQuery("alfrezco");
        searchReq.setQuery(queryReq);
        searchReq.setSpellcheck(new RestRequestSpellcheckModel());
        assertResponse(query(searchReq));

        // Title
        queryReq.setQuery("cm:title:alfrezco");
        queryReq.setUserQuery("alfrezco");
        searchReq.setQuery(queryReq);
        searchReq.setSpellcheck(new RestRequestSpellcheckModel());
        assertResponse(query(searchReq));

        // Description
        queryReq.setQuery("cm:description:alfrezco");
        queryReq.setUserQuery("alfrezco");
        searchReq.setQuery(queryReq);
        searchReq.setSpellcheck(new RestRequestSpellcheckModel());
        assertResponse(query(searchReq));

        // Content
        queryReq.setQuery("cm:content:alfrezco");
        queryReq.setUserQuery("alfrezco");
        searchReq.setQuery(queryReq);
        searchReq.setSpellcheck(new RestRequestSpellcheckModel());
        assertResponse(query(searchReq));
    }

    private void assertResponse(SearchResponse nodes)
    {
        nodes.assertThat().entriesListIsNotEmpty();
        nodes.getContext().assertThat().field("spellCheck").isNotEmpty();
        nodes.getContext().getSpellCheck().assertThat().field("suggestions").contains("alfresco");
        nodes.getContext().getSpellCheck().assertThat().field("type").is("searchInsteadFor");
    }

    /**
     * Perform alternative way by setting the value in the spellcheck object.
     * 
     * <pre>
     * {
     *   "query": { "query": "cm:title:alfrezco", "language": "afts" },
     *   "spellcheck": { "query": "alfrezco" }
     * }
     * </pre>
     */
    @Test(priority = 2)
    public void testSearchMissSpelledVersion2()
    {
        SearchRequest searchReq = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("cm:title:alfrezco");
        searchReq.setQuery(queryReq);

        RestRequestSpellcheckModel spellCheck = new RestRequestSpellcheckModel();
        spellCheck.setQuery("alfrezco");
        searchReq.setSpellcheck(spellCheck);
        assertResponse(query(searchReq));
    }

    @Test(priority = 3)
    public void testSearchWithSpellcheckerAndCorrectSpelling()
    {
        SearchRequest searchReq = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("cm:title:alfresco");
        queryReq.setUserQuery("alfresco");
        searchReq.setQuery(queryReq);
        searchReq.setSpellcheck(new RestRequestSpellcheckModel());
        SearchResponse res = query(searchReq);
        Assert.assertNull(res.getContext().getSpellCheck());
        res.assertThat().entriesListIsNotEmpty();
    }

    /**
     * Test to check the different spellcheck types {@code searchInsteadFor} and {@code didYouMean}. Suggestion ranking is based on maxEdits, count of entries, alphabetical order.
     */
    @Test(priority = 4)
    public void testSpellCheckType()
    {
        // Create a file with the word in cm:name only
        FileModel file = new FileModel("learning", "", "", FileType.TEXT_PLAIN, "");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        // Wait for the file to be indexed
        Assert.assertTrue(waitForMetadataIndexing(file.getName(), true));

        // Correct spelling with cm:name field
        SearchResponse response = SearchSpellcheckQuery(testUser, "cm:name:learning", "learning");

        response.assertThat().entriesListIsNotEmpty();
        response.getContext().assertThat().field("spellCheck").isNull();

        // Matching result, no spellcheck object returned
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, null, null);

        // Correct spelling with no specific field
        response = SearchSpellcheckQuery(testUser, "learning", "learning");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, null, null);

        // Correct spelling with a different field. Used cm:content field
        response = SearchSpellcheckQuery(testUser, "cm:content:learning", "learning");

        // 0 results, no spellcheck object returned
        response.assertThat().entriesListIsEmpty();
        testSearchSpellcheckResponse(response, null, null);

        // Incorrect spelling with cm:name field
        response = SearchSpellcheckQuery(testUser, "cm:name:lerning", "lerning");

        // 1 match with right spelling
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        // TODO: Investigate: Share shows searchInsteadFor = lerning, API shows learning
        // testSearchSpellcheckResponse(response, "searchInsteadFor", "lerning");

        // Incorrect spelling with no field
        response = SearchSpellcheckQuery(testUser, "lerning", "lerning");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        // TODO: Investigate: Share shows searchInsteadFor = lerning, API shows learning
        // testSearchSpellcheckResponse(response, "searchInsteadFor", "lerning");

        // Incorrect spelling with cm:content field
        response = SearchSpellcheckQuery(testUser, "cm:content:lerning", "lerning");

        // 0 results, no spellcheck object returned
        response.assertThat().entriesListIsEmpty();
        response.getContext().assertThat().field("spellCheck").isNull();

        // Create a file with word with 1 max edit in cm:name only
        FileModel file2 = new FileModel("leaning", "", "", FileType.TEXT_PLAIN, "");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file2);

        // Create a file with word in cm:name and cm:content only
        FileModel file3 = new FileModel("leaning 2", "", "", FileType.TEXT_PLAIN, "leaning 2");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file3);

        Assert.assertTrue(waitForContentIndexing(file3.getContent(), true));

        // Incorrect spelling with cm:name field
        response = SearchSpellcheckQuery(testUser, "cm:name:lerning", "lerning");

        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file3.getName()), "Expected file not returned in the search results: " + file3.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "leaning");

        // Incorrect spelling with no field
        response = SearchSpellcheckQuery(testUser, "lerning", "lerning");

        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file3.getName()), "Expected file not returned in the search results: " + file3.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "leaning");

        // Incorrect spelling with cm:content field
        response = SearchSpellcheckQuery(testUser, "cm:content:lerning", "lerning");

        Assert.assertTrue(isContentInSearchResponse(response, file3.getName()), "Expected file not returned in the search results: " + file3.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "leaning");

        // Correct spelling with cm:name field
        response = SearchSpellcheckQuery(testUser, "cm:name:learning", "learning");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "didYouMean", "leaning");

        // Correct spelling with no field
        response = SearchSpellcheckQuery(testUser, "learning", "learning");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "didYouMean", "leaning");

        // Correct spelling with cm:content field
        response = SearchSpellcheckQuery(testUser, "cm:content:learning", "learning");

        Assert.assertTrue(isContentInSearchResponse(response, file3.getName()), "Expected file not returned in the search results: " + file3.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "leaning");
    }

    /** Test for the spellcheck parameters minEdit and maxPrefix. */
    @Test(priority = 5)
    public void testSpellCheckParameters()
    {
        // Create a file with word in cm:name and cm:content
        FileModel file = new FileModel("eklipse", "", "", FileType.TEXT_PLAIN, "eklipse");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        // Create a file with word in cm:name, cm:title and cm:content
        FileModel file2 = new FileModel("eklipses", "eklipses", "", FileType.TEXT_PLAIN, "eklipses");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file2);

        Assert.assertTrue(waitForContentIndexing(file2.getName(), true));

        // Search with field not filled in either file
        SearchResponse response = SearchSpellcheckQuery(testUser, "cm:description:'eclipse'", "eclipse");
        testSearchSpellcheckResponse(response, null, null);

        // Incorrect spelling with the field on a file as well
        response = SearchSpellcheckQuery(testUser, "cm:name:'eclipse'", "eclipse");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "eklipse");

        // Incorrect spelling with no field for file1
        response = SearchSpellcheckQuery(testUser, "eclipse", "eclipse");

        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "eklipse");

        // Add Solr query, to check the suggestions on each shard
        restClient.authenticateUser(testUser).withParams("spellcheck.q=eclipsess&spellcheck=on").withSolrAPI().getSelectQuery();

        // Incorrect spelling with no field for file2
        response = SearchSpellcheckQuery(testUser, "eclipsess", "eclipsess");

        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "eklipses");

        // Search for the field only filled on file2 and not file1
        response = SearchSpellcheckQuery(testUser, "cm:title:'eclipses'", "eclipses");

        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "eklipses");

        // Query using 3 edits (more than spellcheck works for [maxEdits<=2])
        response = SearchSpellcheckQuery(testUser, "elapssed", "elapssed");
        testSearchSpellcheckResponse(response, null, null);

        // Query with edit on first letter (does not work with spellcheck [minPrefix=1])
        response = SearchSpellcheckQuery(testUser, "iklipse ", "iklipse ");
        testSearchSpellcheckResponse(response, null, null);
    }

    /**
     * Test to check the fields defined for spellcheck in shared.properties work (cm:name, cm:title, cm:description and cm:content).
     */
    @Test(priority = 6)
    public void testSpellCheckFields()
    {
        // Create a file with same word in all fields
        FileModel file = new FileModel("book", "book", "book", FileType.TEXT_PLAIN, "book");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        Assert.assertTrue(waitForContentIndexing(file.getContent(), true));

        // Incorrect spelling with no field
        SearchResponse response = SearchSpellcheckQuery(testUser, "bo0k", "bo0k");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "book");

        // Incorrect spelling with the cm:name field
        response = SearchSpellcheckQuery(testUser, "cm:name:'bo0k'", "bo0k");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "book");

        // Incorrect spelling with the cm:title field
        response = SearchSpellcheckQuery(testUser, "cm:title:'bo0k'", "bo0k");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "book");

        // Incorrect spelling with the cm:description field
        response = SearchSpellcheckQuery(testUser, "cm:description:'bo0k'", "bo0k");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "book");

        // Incorrect spelling with the cm:content field
        response = SearchSpellcheckQuery(testUser, "cm:content:'bo0k'", "bo0k");
        Assert.assertTrue(isContentInSearchResponse(response, file.getName()), "Expected file not returned in the search results: " + file.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "book");

        // Incorrect spelling with the cm:author field (not a suggestable field for spellcheck)
        response = SearchSpellcheckQuery(testUser, "cm:author:'bo0k'", "bo0k");
        testSearchSpellcheckResponse(response, null, null);
    }

    /** Test to check the ACL tracker works with spellcheck enabled. */
    @Test(priority = 7)
    public void testSpellCheckACL()
    {
        // Create User 2
        testUser2 = dataUser.createRandomTestUser("User2");

        // Create Private Site 2
        testSite2 = new SiteModel(RandomData.getRandomName("Site2"));
        testSite2.setVisibility(Visibility.PRIVATE);
        testSite2 = dataSite.usingUser(testUser).createSite(testSite2);

        // Make User 2 Site Collaborator
        getDataUser().addUserToSite(testUser2, testSite2, UserRole.SiteCollaborator);

        // Add file <spellcheckspacebar> to testSite
        FileModel file1 = new FileModel("spellcheckspacebar", "", "", FileType.TEXT_PLAIN, "spellcheckspacebar");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file1);

        // Add file <spellcheckspacecar> to testSite, testSite2
        FileModel file2 = new FileModel("spellcheckspacecar", "", "", FileType.TEXT_PLAIN, "spellcheckspacecar");
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file2);
        dataContent.usingUser(testUser).usingSite(testSite2).createContent(file2);

        Assert.assertTrue(waitForContentIndexing(file2.getContent(), true));

        // Checks for User 1
        // Incorrect spelling with no field
        SearchResponse response = SearchSpellcheckQuery(testUser, "spellcheckspaceber", "spellcheckspaceber");
        Assert.assertTrue(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "spellcheckspacebar");

        // Correct spelling with no field
        response = SearchSpellcheckQuery(testUser, "spellcheckspacebar", "spellcheckspacebar");
        Assert.assertTrue(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file1.getName());
        testSearchSpellcheckResponse(response, "didYouMean", "spellcheckspacecar");

        // Incorrect spelling with no field
        response = SearchSpellcheckQuery(testUser, "spellcheckspacebra", "spellcheckspacebra");
        Assert.assertTrue(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file1.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "spellcheckspacebar");

        // Correct spelling with no field
        response = SearchSpellcheckQuery(testUser, "spellcheckspacecar", "spellcheckspacecar");
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, null, null);

        // Add Solr query, to check the suggestions on the shard
        restClient.authenticateUser(testUser).withParams("spellcheck.q=spellcheckspacebur&spellcheck=on").withSolrAPI().getSelectQuery();

        // Checks for User 2
        // Incorrect spelling for files created with no field
        response = SearchSpellcheckQuery(testUser2, "spellcheckspacebur", "spellcheckspacebur");
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "spellcheckspacecar");

        // Correct spelling, no field
        response = SearchSpellcheckQuery(testUser2, "spellcheckspacebar", "spellcheckspacebar");
        Assert.assertFalse(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file1.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "spellcheckspacecar");

        // Incorrect spelling, no field
        response = SearchSpellcheckQuery(testUser2, "spellcheckspacecra", "spellcheckspacecra");
        Assert.assertFalse(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file1.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, "searchInsteadFor", "spellcheckspacecar");

        // Correct spelling, no field
        response = SearchSpellcheckQuery(testUser2, "spellcheckspacecar", "spellcheckspacecar");
        Assert.assertFalse(isContentInSearchResponse(response, file1.getName()), "Expected file not returned in the search results: " + file1.getName());
        Assert.assertTrue(isContentInSearchResponse(response, file2.getName()), "Expected file not returned in the search results: " + file2.getName());
        testSearchSpellcheckResponse(response, null, null);
    }
}
