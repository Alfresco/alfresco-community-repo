package org.alfresco.email.imap;

import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.Test;

/**
 * Tests for Search for Messages in IMAP client
 * 
 * @author Cristina Axinte
 *
 */
public class ImapSearchMessagesTests extends EmailTest
{
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify that no results are returned when searching for a message name term with no match")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void noResultsReturnedWhenSearchingForNotExistingMessage() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String fileName1="File-new1";
        FileModel fileModel1 = dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder)
                .createContent(new FileModel(fileName1, fileName1, fileName1 + "description", FileType.TEXT_PLAIN, fileName1 + "content"));
        String fileName2="File2";
        FileModel fileModel2 = dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder)
                .createContent(new FileModel(fileName2, fileName2, fileName2 + "description", FileType.TEXT_PLAIN, fileName2 + "content"));

        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder)
            .searchSubjectFor("new").assertThat().resultsContainMessage(fileModel1)
            .assertThat().resultsDoNotContainMessage(fileModel2);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify that searched message is returned when searching for full message name")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void searchedMessageIsReturnedWhenSearchingForFullMessageName() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String fileName1="File-new1";
        FileModel fileModel1 = dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder)
                .createContent(new FileModel(fileName1, fileName1, fileName1 + "description", FileType.TEXT_PLAIN, fileName1 + "content"));
        String fileName2="File2";
        FileModel fileModel2 = dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder)
                .createContent(new FileModel(fileName2, fileName2, fileName2 + "description", FileType.TEXT_PLAIN, fileName2 + "content"));

        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder)
            .searchSubjectFor("File2").assertThat().resultsContainMessage(fileModel2)
            .assertThat().resultsDoNotContainMessage(fileModel1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that search results are returned for search term containing white spaces")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerVerifySearchResultsForWhiteSpaces() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        String fileName1="File new";
        FileModel fileModel1 = dataContent.usingUser(testUser).usingSite(testSite)
                .createContent(new FileModel(fileName1, fileName1, fileName1 + "description", FileType.TEXT_PLAIN, fileName1 + "content"));
        String fileName2="File new1";
        FileModel fileModel2 = dataContent.usingSite(testSite)
                .createContent(new FileModel(fileName2, fileName2, fileName2 + "description", FileType.TEXT_PLAIN, fileName2 + "content"));

        imapProtocol.authenticateUser(testUser).usingSite(testSite).searchSubjectFor("File new")
                .assertThat().resultsContainMessage(fileModel1, fileModel2);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that search results are returned for search term containing wildcards")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerVerifySearchResultsForWildcards() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        String fileName1="File new 1";
        FileModel fileModel1 = dataContent.usingUser(testUser).usingSite(testSite)
                .createContent(new FileModel(fileName1, fileName1, fileName1 + "description", FileType.TEXT_PLAIN, fileName1 + "content"));
        String fileName2="File presentation 2";
        FileModel fileModel2 = dataContent.usingSite(testSite)
                .createContent(new FileModel(fileName2, fileName2, fileName2 + "description", FileType.TEXT_PLAIN, fileName2 + "content"));

        imapProtocol.authenticateUser(testUser).usingSite(testSite).searchSubjectWithWildcardsFor("File.*1.*")
                .assertThat().resultsContainMessage(fileModel1)
                .assertThat().resultsDoNotContainMessage(fileModel2);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that search results are returned for search term containing special characters")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerVerifySearchResultsForSpecialCharacters() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        String fileName1="File@#$%^&()-_=+-[]{};'";
        FileModel fileModel1 = dataContent.usingUser(testUser).usingSite(testSite)
                .createContent(new FileModel(fileName1, fileName1, fileName1 + "description", FileType.TEXT_PLAIN, fileName1 + "content"));
        String fileName2="File";
        FileModel fileModel2 = dataContent.usingSite(testSite)
                .createContent(new FileModel(fileName2, fileName2, fileName2 + "description", FileType.TEXT_PLAIN, fileName2 + "content"));

        imapProtocol.authenticateUser(testUser).usingSite(testSite)
                .searchSubjectFor("=").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2)
                .searchSubjectFor("@").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2)
                .searchSubjectFor("#").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2)
                .searchSubjectFor("$").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2)
                .searchSubjectFor("@#").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2)
                .searchSubjectFor("[]").assertThat().resultsContainMessage(fileModel1).assertThat().resultsDoNotContainMessage(fileModel2);
    }
}
