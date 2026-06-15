/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import static java.util.List.of;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;

/**
 * Test class tests AFTS Search In Field works Created for Search-840
 */
public class SearchAFTSInFieldTest extends AbstractSearchServicesE2ETest
{
    private FolderModel folder1, folder2;
    private FileModel file1, file2, file3, file4, file5;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        // Create Folders:
        // Folder1: Expected to be found with file.txt
        folder1 = new FolderModel("file txt folder");
        dataContent.usingUser(testUser).usingSite(testSite).createFolder(folder1);

        // Folder2: Not expected to be found with file.txt
        folder2 = new FolderModel("txt files folder");
        dataContent.usingUser(testUser).usingSite(testSite).createFolder(folder2);

        // Create File(s): Expected to be found with file.txt
        file1 = new FileModel("file.txt", "file.txt", "", FileType.TEXT_PLAIN, "file.txt");

        file2 = new FileModel("1-file.txt", "1-file.txt", "", FileType.TEXT_PLAIN, "1-file.txt");

        file3 = new FileModel("file1.txt", "file1.txt", "", FileType.TEXT_PLAIN, "file1.txt");

        file4 = new FileModel("txt file", "txt file", "", FileType.TEXT_PLAIN, "txt file");

        // Not Expected to be found with file.txt
        file5 = new FileModel("txt files", "txt files", "", FileType.TEXT_PLAIN, "txt files");

        of(file1, file2, file3, file4, file5).forEach(
                f -> dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder1).createContent(f));

        waitForContentIndexing(file5.getContent(), true);
    }

    @Test(priority = 1, groups = {TestGroup.ACS_63n})
    public void testSearchInFieldName()
    {
        // Field names in various formats
        Stream<String> fieldNames = Stream.of("{http://www.alfresco.org/model/content/1.0}name",
                "@{http://www.alfresco.org/model/content/1.0}name",
                "cm_name",
                "cm:name",
                "@cm:name",
                "name");

        // For each field name, check that queries return consistent results with / out ''
        fieldNames.forEach(fieldName -> {
            // Query string without quotes
            String query = fieldName + ":file.txt";

            Set<String> expectedNames = new HashSet<>();
            expectedNames.add("file.txt"); // file1
            expectedNames.add("1-file.txt"); // file2
            expectedNames.add("file1.txt"); // file3
            expectedNames.add("txt file"); // file4
            expectedNames.add("file txt folder"); // folder1

            testSearchQueryUnordered(query, expectedNames);

            // Query string in single quotes
            query = fieldName + ":'file.txt'";
            testSearchQueryUnordered(query, expectedNames);
        });
    }

    @Test(priority = 2, groups = {TestGroup.ACS_63n})
    public void testSearchInFieldTitle()
    {
        // Field names in various formats
        Stream<String> fieldNames = Stream.of("{http://www.alfresco.org/model/content/1.0}title",
                "@{http://www.alfresco.org/model/content/1.0}title",
                "cm_title",
                "cm:title",
                "@cm:title");

        // For each field name, check that queries return consistent results with / out ''
        fieldNames.forEach(fieldName -> {
            String query = fieldName + ":" + file2.getName();
            boolean fileFound = isContentInSearchResults(query, file2.getName(), true);
            Assert.assertTrue(fileFound, "File Not found for query: " + query);

            testSearchQuery(query, 1);

            query = fieldName + ":'" + file2.getName() + "\'";
            fileFound = isContentInSearchResults(query, file2.getName(), true);
            Assert.assertTrue(fileFound, "File Not found for query: " + query);

            testSearchQuery(query, 1);
        });
    }

    @Test(priority = 3, groups = {TestGroup.ACS_63n})
    public void testSearchInFieldContent()
    {
        // Field names in various formats
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("TEXT");
        fieldNames.add("{http://www.alfresco.org/model/dictionary/1.0}content");
        fieldNames.add("cm:content");
        fieldNames.add("d:content");

        // For each field name, check that queries return consistent results with / out ''
        fieldNames.forEach(fieldName -> {
            String query = fieldName + ":" + file3.getContent();
            boolean fileFound = isContentInSearchResults(query, file3.getName(), true);
            Assert.assertTrue(fileFound, "File Not found for query: " + query);

            Integer resultCount1 = testSearchQuery(query, null).getPagination().getTotalItems();

            query = fieldName + ":'" + file3.getContent() + "\'";
            fileFound = isContentInSearchResults(query, file3.getName(), true);
            Assert.assertTrue(fileFound, "File Not found for query: " + query);

            testSearchQuery(query, resultCount1).getPagination().getTotalItems();
        });
    }

    @Test(priority = 5)
    public void testSearchInFieldTYPE()
    {
        // Field names in various formats
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("TYPE");
        fieldNames.add("EXACTTYPE");

        // For each field name, check that queries return consistent results with / out ''
        fieldNames.forEach(fieldName -> {

            String query = fieldName + ":cm\\:content" + " and =cm:name:" + file1.getName();
            boolean fileFound = isContentInSearchResults(query, file1.getName(), true);
            Assert.assertTrue(fileFound, "Content Not found for query: " + query);

            Integer resultCount1 = testSearchQuery(query, 1).getPagination().getTotalItems();

            query = fieldName + ":'cm:content'" + " and =cm:name:" + file1.getName();
            fileFound = isContentInSearchResults(query, file1.getName(), true);
            Assert.assertTrue(fileFound, "Content Not found for query: " + query);

            testSearchQuery(query, resultCount1).getPagination().getTotalItems();
        });
    }

    @Test(priority = 6)
    public void testSearchInFieldID() throws Exception
    {
        String query = "ID:'workspace://SpacesStore/" + file1.getNodeRefWithoutVersion() + "'";
        boolean fileFound = isContentInSearchResults(query, file1.getName(), true);
        Assert.assertTrue(fileFound, "Content Not found for query: " + query);

        testSearchQuery(query, 1).getPagination().getTotalItems();
    }

    @Test(priority = 7)
    public void testSearchInFieldPARENT()
    {
        String query = "PARENT:" + folder1.getNodeRefWithoutVersion();

        Set<String> expectedNames = new HashSet<>();
        expectedNames.add(file1.getName());
        expectedNames.add(file2.getName());
        expectedNames.add(file3.getName());
        expectedNames.add(file4.getName());
        expectedNames.add(file5.getName());

        testSearchQueryUnordered(query, expectedNames);

        query = "PARENT:'" + folder1.getNodeRefWithoutVersion() + "\'";

        testSearchQueryUnordered(query, expectedNames);
    }

    @Test(priority = 9, groups = {TestGroup.ACS_63n})
    public void testSearchInFieldNameExactMatch()
    {
        // Check that queries return consistent results with / out ''
        String query = "=name:" + file1.getName();
        boolean fileFound = isContentInSearchResults(query, file1.getName(), true);
        Assert.assertTrue(fileFound, "File Not found for query: " + query);

        Integer resultCount1 = testSearchQuery(query, 1).getPagination().getTotalItems();
        Assert.assertSame(resultCount1, 1, "File count does not match for query: " + query);

        query = "=name:'" + file1.getName() + "\'";
        fileFound = isContentInSearchResults(query, file1.getName(), true);
        Assert.assertTrue(fileFound, "File Not found for query: " + query);

        testSearchQuery(query, resultCount1).getPagination().getTotalItems();
    }

    @Test(priority = 10, groups = {TestGroup.ACS_63n})
    public void testSearchInFieldNameQueryExpansion()
    {
        // Check that queries return consistent results with / out ''
        String query = "~name:" + file1.getName();

        Set<String> expectedNames = new HashSet<>();
        expectedNames.add(file1.getName());
        expectedNames.add(file2.getName());
        expectedNames.add(file3.getName());
        expectedNames.add(file4.getName());
        expectedNames.add(folder1.getName());

        testSearchQueryUnordered(query, expectedNames);

        query = "~name:'" + file1.getName() + "\'";
        testSearchQueryUnordered(query, expectedNames);
    }

    @Test(priority = 11, groups = {TestGroup.ACS_63n})
    public void testWithConjunctionDisjunctionAndNegation()
    {
        // Query string to include Conjunction, Disjunction and Negation

        String query1 = "~name:" + file1.getName(); // Query expected to return 5 results
        String query2 = "=name:" + file2.getName(); // Query expected to return 1 result
        String query3 = "=name:" + file3.getName(); // Query expected to return 1 result

        // Check Query with Conjunction, Negation, Disjunction: returns right results
        // "~name:file.txt and ! (=name:1-file.txt or =name:file1.txt)"
        String query = query1 + " and ! (" + query2 + " or " + query3 + ")";

        // Check that expected files are included in the results
        Set<String> expectedNames = new HashSet<>();
        expectedNames.add("file.txt"); // file1
        expectedNames.add("txt file"); // file4
        expectedNames.add("file txt folder"); // folder1

        SearchResponse response = testSearchQueryUnordered(query, expectedNames);

        // Check result count is 5-(1+1)=3
        int resultCount = response.getPagination().getTotalItems();
        Assert.assertEquals(resultCount, 3, "File count does not match for query: " + query);

        // Check that file2 and file3 are excluded from the results
        boolean fileFound = isContentInSearchResponse(response, file2.getName());
        Assert.assertFalse(fileFound, "File2 found for query: " + query);

        fileFound = isContentInSearchResponse(response, file3.getName());
        Assert.assertFalse(fileFound, "File3 found for query: " + query);
    }

    /** Check that a 200 success is returned when performing exact search against the DB (even on a tokenised field without cross-locale support). */
    @Test(priority = 13)
    public void testExactMatchAgainstDB()
    {
        // Using a simple query we will hit the DB.
        String query = "=cm:title:test";
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(query);
        SearchRequest searchRequest = new SearchRequest(queryModel);
        restClient.authenticateUser(dataUser.getAdminUser()).withSearchAPI().search(searchRequest);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}
