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

import static java.util.List.of;

import org.springframework.http.HttpStatus;
import org.testng.Assert;

import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;

/**
 * Abstract Search test class that contains useful methods such as:
 * <ul>
 * <li>Preparing the data to index.</li>
 * <li>Polling the search API until results are present.</li>
 * </ul>
 * <p>
 * Mirrors the InsightEngine {@code AbstractSearchServicesE2ETest}: a thin specialisation of {@link AbstractE2EFunctionalTest} that adds the standard "search services" sample data set. All Spring context, autowires, common test user/site setup and generic search/query/wait/ spellcheck helpers are inherited from {@link AbstractE2EFunctionalTest}.
 */
public abstract class AbstractSearchServicesE2ETest extends AbstractE2EFunctionalTest
{
    private static final String SEARCH_DATA_SAMPLE_FOLDER = "FolderSearch";

    private static final int MAX_ATTEMPTS_TO_RETRY_QUERY = 10;
    private static final int MAX_WAIT_IN_SECONDS_BEFORE_RETRY_QUERY = 5;
    private static final int MAX_ATTEMPTS_TO_READ_RESPONSE = 10;
    private static final int MAX_WAIT_IN_SECONDS_BEFORE_REREAD_RESPONSE = 2;

    protected FileModel file, file2, file3, file4;
    protected FolderModel folder;

    /**
     * Creates the standard test data structure used across search tests:
     * 
     * <pre>
     * |- FolderSearch
     * |-- pangram.txt
     * |-- cars.PDF
     * |-- alfresco.docx
     * |-- &lt;uniqueFileName&gt;.ODT
     * </pre>
     */
    public void searchServicesDataPreparation()
    {
        folder = new FolderModel(SEARCH_DATA_SAMPLE_FOLDER);
        dataContent.usingUser(testUser).usingSite(testSite).createFolder(folder);

        String title = "Title: " + unique_searchString;
        String description = "Description: File is created for search tests by Author: " + unique_searchString + " . ";

        file = new FileModel("pangram.txt", "pangram" + title, description, FileType.TEXT_PLAIN,
                description + " The quick brown fox jumps over the lazy dog");

        file2 = new FileModel("cars.PDF", "cars", description, FileType.TEXT_PLAIN,
                "The landrover discovery is not a sports car");

        file3 = new FileModel("alfresco.docx", "alfresco", "alfresco", FileType.TEXT_PLAIN,
                "Alfresco text file for search ");

        file4 = new FileModel(unique_searchString + ".ODT", "uniquee" + title, description, FileType.TEXT_PLAIN,
                "Unique text file for search ");

        of(file, file2, file3, file4).forEach(
                f -> dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(f));

        waitForMetadataIndexing(file4.getName(), true);
    }

    /**
     * Creates a file with the provided text in cm:name, cm:title, cm:description and cm:content (under {@link #folder} of {@link #testSite}) and waits for the content to be indexed.
     */
    protected FileModel createFileWithProvidedText(String filename, String providedText) throws InterruptedException
    {
        String title = "Title: File containing " + providedText;
        String description = "Description: Contains provided string: " + providedText;
        FileModel uniqueFile = new FileModel(filename, title, description, FileType.TEXT_PLAIN,
                "The content " + providedText + " is a provided string");
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(uniqueFile);
        Assert.assertTrue(waitForContentIndexing(providedText, true));
        return uniqueFile;
    }

    /**
     * Repeats the query until the response status is OK and the entries list is non-empty, up to {@link #MAX_ATTEMPTS_TO_READ_RESPONSE} re-reads after a successful status.
     */
    protected SearchResponse queryUntilResponseEntriesListNotEmpty(UserModel user, String queryString)
    {
        SearchResponse response = queryUntilStatusIsOk(user, queryString);
        if (restClient.getStatusCode().matches(String.valueOf(HttpStatus.OK.value())))
        {
            for (int readAttempts = 0; readAttempts < MAX_ATTEMPTS_TO_READ_RESPONSE; readAttempts++)
            {
                if (!response.isEmpty())
                {
                    return response;
                }
                Utility.waitToLoopTime(MAX_WAIT_IN_SECONDS_BEFORE_REREAD_RESPONSE,
                        "Re-reading empty response. Retry Attempt: " + (readAttempts + 1));
            }
        }
        return response;
    }

    private SearchResponse queryUntilStatusIsOk(UserModel user, String queryString)
    {
        // Repeat query until status is OK or query retry limit is hit
        for (int queryAttempts = 0; queryAttempts < MAX_ATTEMPTS_TO_RETRY_QUERY - 1; queryAttempts++)
        {
            try
            {
                SearchResponse response = queryAsUser(user, queryString);
                if (restClient.getStatusCode().matches(String.valueOf(HttpStatus.OK.value())))
                {
                    return response;
                }
                Utility.waitToLoopTime(MAX_WAIT_IN_SECONDS_BEFORE_RETRY_QUERY,
                        "Re-trying query for valid status code. Retry Attempt: " + (queryAttempts + 1));
            }
            catch (EmptyJsonResponseException ignore)
            {
                // try again
            }
        }
        // Final attempt
        return queryAsUser(user, queryString);
    }
}
