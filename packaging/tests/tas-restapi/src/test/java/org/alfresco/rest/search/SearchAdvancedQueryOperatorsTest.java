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
 * Migration test class for advanced AFTS query operators on Elasticsearch:
 * ISUNSET (property-not-set) and proximity ('wordA *(N) wordB').
 * Created for ACS-11964.
 */
public class SearchAdvancedQueryOperatorsTest extends AbstractSearchServicesE2ETest
{
    private FolderModel folder;
    private FileModel fileWithoutTitle;
    private FileModel proximityFile;

    private static final String UNIQUE_PREFIX = "acsadvops";

    // Unique fake tokens — avoid tokenization surprises and cross-run pollution.
    private static final String TOKEN_A = "acsadvproxaaa";
    private static final String TOKEN_B = "acsadvproxbbb";
    private static final String TOKEN_C = "acsadvproxccc";
    private static final String TOKEN_D = "acsadvproxddd";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        folder = dataContent.usingUser(testUser).usingSite(testSite).createFolderCmisApi(UNIQUE_PREFIX + "-folder");

        // File created without setting cm:title — used for ISUNSET.
        fileWithoutTitle = new FileModel(UNIQUE_PREFIX + "-file-without-title.txt");
        fileWithoutTitle.setContent("Body of the file without a title");
        dataContent.usingUser(testUser).usingResource(folder).createContent(fileWithoutTitle);

        // Content: 4 unique tokens at known positions A=0, B=1, C=2, D=3.
        // Between A and D there are exactly 2 words (B, C), so proximity *(2) must match.
        proximityFile = new FileModel(UNIQUE_PREFIX + "-proximity-file.txt");
        proximityFile.setContent(TOKEN_A + " " + TOKEN_B + " " + TOKEN_C + " " + TOKEN_D);
        dataContent.usingUser(testUser).usingResource(folder).createContent(proximityFile);

        waitForMetadataIndexing(fileWithoutTitle.getName(), true);
        waitForMetadataIndexing(proximityFile.getName(), true);

        // Poll until the file's content is truly indexed on ES.
        Assert.assertTrue(
                isContentInSearchResults(
                        "cm:content:'" + TOKEN_A + "' AND cm:name:'" + proximityFile.getName() + "'",
                        proximityFile.getName(),
                        true),
                "Setup: proximity file's content should be indexed on ES before running tests");
    }

    /**
     * ISUNSET:"cm:title" must return the file whose cm:title was never set.
     */
    @Test(priority = 1)
    public void testIsUnsetOperator()
    {
        String query = "ISUNSET:\"cm:title\" AND cm:name:'" + fileWithoutTitle.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected file without a title to be findable via ISUNSET on cm:title");
    }

    /**
     * AFTS proximity syntax: 'wordA *(N) wordB' matches when at most N words separate the two.
     * TOKEN_A (position 0) and TOKEN_D (position 3) have exactly 2 words between them (TOKEN_B, TOKEN_C),
     * so *(2) must match.
     */
    @Test(priority = 2)
    public void testProximitySearchUsingAftsSyntax()
    {
        String query = "cm:content:(" + TOKEN_A + " *(2) " + TOKEN_D + ") AND cm:name:'" +
                proximityFile.getName() + "'";
        SearchResponse response = queryAsUser(testUser, query);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        Assert.assertTrue(response.getPagination().getCount() >= 1,
                "Expected AFTS proximity '" + TOKEN_A + " *(2) " + TOKEN_D +
                        "' to find the file (2 words between A and D in the content)");
    }
}