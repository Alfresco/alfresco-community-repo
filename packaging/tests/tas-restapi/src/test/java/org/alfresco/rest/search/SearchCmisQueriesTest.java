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
 * Migration test class for CMIS-language queries on Elasticsearch.
 * Created for ACS-11964.
 */
public class SearchCmisQueriesTest extends AbstractSearchServicesE2ETest
{
    private FolderModel folder;
    private FileModel invoice;
    private FileModel report;
    private FileModel memo;

    private static final String UNIQUE_PREFIX = "acscmismig";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        folder = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi(UNIQUE_PREFIX + "-folder");

        invoice = new FileModel(UNIQUE_PREFIX + "-invoice-january.txt");
        invoice.setContent("Invoice content covering multiple line items");
        dataContent.usingUser(testUser).usingResource(folder).createContent(invoice);

        report = new FileModel(UNIQUE_PREFIX + "-report-quarterly.txt");
        report.setContent("Quarterly report of activities");
        dataContent.usingUser(testUser).usingResource(folder).createContent(report);

        memo = new FileModel(UNIQUE_PREFIX + "-memo-internal.txt");
        memo.setContent("Internal memo for staff");
        dataContent.usingUser(testUser).usingResource(folder).createContent(memo);

        waitForContentIndexing(invoice.getContent(), true);
        waitForContentIndexing(report.getContent(), true);
        waitForContentIndexing(memo.getContent(), true);
    }

    private SearchResponse runCmisQuery(String cmisQuery)
    {
        SearchRequest searchRequest = new SearchRequest();
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(cmisQuery);
        queryModel.setLanguage(SearchLanguage.CMIS.toString());
        searchRequest.setQuery(queryModel);
        return restClient.authenticateUser(testUser).withSearchAPI().search(searchRequest);
    }

    @Test(priority = 1)
    public void testCmisSelectWithLike()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name LIKE '" + UNIQUE_PREFIX + "-invoice%'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected exactly one invoice file with the CMIS LIKE query");
    }

    @Test(priority = 2)
    public void testCmisWithMultipleWhereClauses()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name LIKE '" + UNIQUE_PREFIX +
                "%' AND cmis:name LIKE '%report%'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected exactly the report file matching both LIKE conditions");
    }

    @Test(priority = 3)
    public void testCmisFullTextWithMetadata()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE CONTAINS('quarterly') AND cmis:name LIKE '" +
                UNIQUE_PREFIX + "%'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected at least one file matching CONTAINS + cmis:name filter");
    }

    @Test(priority = 4)
    public void testCmisExactEqualityMatch()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name = '" + memo.getName() + "'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected exact-equality CMIS query to return the specific file");
    }

    @Test(priority = 5)
    public void testCmisWithOrClause()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name = '" + invoice.getName() +
                "' OR cmis:name = '" + report.getName() + "'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 2,
                "Expected CMIS OR to return exactly two matching files");
    }

    @Test(priority = 6)
    public void testCmisSelectFromCmisFolder()
    {
        String cmisQuery = "SELECT * FROM cmis:folder WHERE cmis:name = '" + folder.getName() + "'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 1,
                "Expected CMIS query on cmis:folder type to return the folder");
    }

    @Test(priority = 7)
    public void testCmisWithOrderBy()
    {
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name LIKE '" + UNIQUE_PREFIX +
                "%' ORDER BY cmis:name ASC";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 3,
                "Expected ORDER BY CMIS query to return the prefixed files");
        String firstName = response.getEntries().getFirst().getModel().getName();
        Assert.assertEquals(firstName, invoice.getName(),
                "Expected the alphabetically first file (invoice) to be returned first");
    }

    @Test(priority = 8)
    public void testCmisWithNotClause()
    {
        // Select all our test files but exclude anything matching '%invoice%'.
        // Should return the report and memo files, but NOT the invoice.
        String cmisQuery = "SELECT * FROM cmis:document WHERE cmis:name LIKE '" + UNIQUE_PREFIX +
                "%' AND cmis:name NOT LIKE '%invoice%'";
        SearchResponse response = runCmisQuery(cmisQuery);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertEquals(response.getPagination().getCount(), 2,
                "Expected NOT LIKE to exclude the invoice file and return exactly the report and memo files");

        // Verify the invoice is truly excluded (guards against ES translator bug where NOT is ignored).
        boolean invoiceInResults = response.getEntries().stream()
                .anyMatch(e -> e.getModel().getName().equals(invoice.getName()));
        Assert.assertFalse(invoiceInResults,
                "Expected the invoice file to be excluded by NOT LIKE '%invoice%'");
    }
}