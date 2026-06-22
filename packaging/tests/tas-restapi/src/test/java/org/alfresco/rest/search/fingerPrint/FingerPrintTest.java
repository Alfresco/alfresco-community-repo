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

package org.alfresco.rest.search.fingerPrint;

import static com.google.common.collect.Sets.newHashSet;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.search.AbstractE2EFunctionalTest;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.report.Bug;

/**
 * Search end point Public API test with fingerprint.
 */
public class FingerPrintTest extends AbstractE2EFunctionalTest
{
    private FolderModel folder;
    private FileModel fileBanana, fileTaco, fileCat, fileDog, fileOriginal;
    private FileModel fileToBeUpdated;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        /* Create the following file structure in the same Site : In addition to the preconditions created in dataPreparation |- folder |-- pangram-banana.txt |-- pangram-taco.txt |-- pangram-cat.txt |-- pangram-dog.txt |-- original.txt |-- toBeUpdated.txt */
        folder = new FolderModel("The quick brown fox jumps over");
        dataContent.usingUser(testUser).usingSite(testSite).createFolder(folder);

        fileBanana = new FileModel("pangram-banana.txt", FileType.TEXT_PLAIN, "The quick brown fox jumps over the lazy banana");
        fileTaco = new FileModel("pangram-taco.txt", FileType.TEXT_PLAIN, "The quick brown fox jumps over the lazy dog that ate the taco");

        fileCat = new FileModel("pangram-cat.txt", FileType.TEXT_PLAIN, "The quick brown fox jumps over the lazy cat");
        fileDog = new FileModel("pangram-dog.txt", FileType.TEXT_PLAIN, "The quick brown fox ate the lazy dog");

        // fileOriginal with different content
        fileOriginal = new FileModel("original.txt", FileType.TEXT_PLAIN, "This is an original file with some content");

        // Index a new fileToBeUpdated with content similar to fileOriginal
        fileToBeUpdated = new FileModel("toBeUpdated.txt", FileType.TEXT_PLAIN, "This is an original file with some content");

        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileBanana);
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileTaco);
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileCat);
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileDog);

        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileOriginal);
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(fileToBeUpdated);

        // Additional wait implemented to remove inconsistent failures. Ref: Search-1438 for details
        waitForIndexing("FINGERPRINT:" + fileDog.getNodeRefWithoutVersion(), true);
        waitForIndexing("FINGERPRINT:" + fileToBeUpdated.getNodeRefWithoutVersion(), true);
    }

    @Test(priority = 1)
    @Bug(id = "MNT-20449")
    public void makeSureFingerprintQueryWorksAfterMetadataUpdate()
    {
        // Index a new file with content
        FileModel file = new FileModel("Project_Contract.txt", FileType.TEXT_PLAIN, "A content which is completely different from other indexed files.");
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(file);

        // make sure the content has been indexed (i.e. the ContentTracker fingerprint has been correctly computed
        assertTrue(waitForIndexing("FINGERPRINT:" + file.getNodeRefWithoutVersion(), true));

        // Update some metadata attribute of the file
        String newFileName = "Contrattazione.pdf";
        file.setName(newFileName);

        // ...and reindex it
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(file).renameContent(file);

        // Make sure the new version of the file has been indexed
        assertTrue(waitForMetadataIndexing("Contrattazione", true));

        // ...and finally
        assertTrue(waitForIndexing("FINGERPRINT:" + file.getNodeRefWithoutVersion(), true));
    }

    /**
     * Search similar document based on document finger print. The data prep should have loaded 2 files which one is similar to the files loaded as part of this test. Note that for fingerprint to work it need a 5 word sequence.
     */
    @Test(priority = 2)
    public void search()
    {
        String uuid = fileBanana.getNodeRefWithoutVersion();
        Assert.assertNotNull(uuid);

        String fingerprintQuery = String.format("FINGERPRINT:%s", uuid);
        SearchResponse response = queryAsUser(testUser, fingerprintQuery);

        int count = response.getEntries().size();
        assertTrue(count > 1);

        Set<String> expectedNames = newHashSet();
        expectedNames.add(fileBanana.getName());
        expectedNames.add(fileTaco.getName());
        expectedNames.add(fileCat.getName());

        testSearchQueryUnordered(fingerprintQuery, expectedNames, SearchLanguage.AFTS);
    }

    @Test(priority = 3)
    public void searchSimilar()
    {
        String uuid = fileTaco.getNodeRefWithoutVersion();
        Assert.assertNotNull(uuid);

        // In the response entity there is a score of each doc, change below threshold to bring more like or less.
        String fingerprintQuery = String.format("FINGERPRINT:%s_68", uuid);
        SearchResponse response = query(fingerprintQuery);

        int count = response.getEntries().size();
        assertTrue(count >= 1);

        Set<String> expectedNames = newHashSet();
        expectedNames.add(fileTaco.getName());

        testSearchQueryUnordered(fingerprintQuery, expectedNames, SearchLanguage.AFTS);
    }

    @Test(priority = 4)
    public void searchSimilar67Percent()
    {
        String uuid = fileTaco.getNodeRefWithoutVersion();
        Assert.assertNotNull(uuid);

        // Check that: When asked for less overlap of 67%: More documents are matched
        String fingerprintQuery = String.format("FINGERPRINT:%s_67", uuid);
        SearchResponse response = query(fingerprintQuery);

        int count = response.getEntries().size();
        assertTrue(count > 1);

        Set<String> expectedNames = newHashSet();
        expectedNames.add(fileTaco.getName());
        expectedNames.add(fileBanana.getName());
        expectedNames.add(fileCat.getName());

        testSearchQueryUnordered(fingerprintQuery, expectedNames, SearchLanguage.AFTS);
    }

    @Test(priority = 5)
    @Bug(id = "SEARCH-2065")
    public void searchAfterVersionUpdate()
    {
        // Check that fileToBeUpdated is found with a fingerprint query with fileOriginal: as they have similar content
        boolean found = isContentInSearchResults("FINGERPRINT:" + fileOriginal.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), true);
        Assert.assertTrue(found, "Matching File Not found in results for Fingerprint Query with original file");

        // Update content of the fileToBeUpdated to match file1 in the dataprep
        String newFileContent = "The quick brown fox jumps over the updated file";
        fileToBeUpdated.setContent(newFileContent);
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(fileToBeUpdated).updateContent(newFileContent);

        // Wair for the new version of the file to be indexed
        assertTrue(waitForContentIndexing(fileToBeUpdated.getContent(), true));

        // Check that fileToBeUpdated is NOT found with a fingerprint query with fileOriginal
        boolean notFound = isContentInSearchResults("FINGERPRINT:" + fileOriginal.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), false);
        Assert.assertTrue(notFound, "Updated File unexpectedly found in results for Fingerprint Query with original file");

        // Check that fileToBeUpdated is found with a fingerprint query with file1
        found = isContentInSearchResults("FINGERPRINT:" + fileBanana.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), true);
        Assert.assertTrue(found, "Update File Not found in results for Fingerprint Query with updated content");
    }

    @Test(priority = 6)
    @Bug(id = "SEARCH-2065")
    public void searchAfterVersionRevert()
    {
        // Revert fileToBeUpdated to previous version
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(fileToBeUpdated).revertVersion("1.0", "{}");
        String revertedContent = restClient.authenticateUser(testUser).withCoreAPI().usingNode(fileToBeUpdated).getVersionContent("1.2").getResponse().asString();
        Assert.assertEquals(revertedContent, fileOriginal.getContent(), "Reverted content does not match Original");

        // Wair for the new version of the file to be indexed
        assertTrue(waitForContentIndexing(fileToBeUpdated.getContent(), true));

        // Check that fileToBeUpdated is found with a fingerprint query with fileOriginal
        boolean found = isContentInSearchResults("FINGERPRINT:" + fileOriginal.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), true);
        Assert.assertTrue(found, "File not found in results for Fingerprint Query with original file after reverting version changes");

        // Check that fileToBeUpdated is NOT found with a fingerprint query with file1
        boolean notFound = isContentInSearchResults("FINGERPRINT:" + fileBanana.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), false);
        Assert.assertTrue(notFound, "File appears in the results for Fingerprint Query even after reverting content changes");
    }

    @Test(priority = 7)
    @Bug(id = "SEARCH-2065")
    public void searchAfterVersionDelete()
    {
        // Revert fileToBeUpdated to previous version
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(fileToBeUpdated).deleteNodeVersion("1.2");

        // Check that fileToBeUpdated is NOT found with a fingerprint query with fileOriginal
        boolean notFound = isContentInSearchResults("FINGERPRINT:" + fileOriginal.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), false);
        Assert.assertTrue(notFound, "Updated File unexpectedly found in results for Fingerprint Query with original file");

        // Check that fileToBeUpdated is found with a fingerprint query with file1
        boolean found = isContentInSearchResults("FINGERPRINT:" + fileBanana.getNodeRefWithoutVersion(), fileToBeUpdated.getName(), true);
        Assert.assertTrue(found, "Update File Not found in results for Fingerprint Query with updated content");
    }
}
