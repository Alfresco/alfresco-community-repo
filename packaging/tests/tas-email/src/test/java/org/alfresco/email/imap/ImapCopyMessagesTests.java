package org.alfresco.email.imap;

import javax.mail.MessagingException;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImapCopyMessagesTests extends EmailTest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can copy non-empty file via IMAP client to a different location")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldCopyNonEmptyFile() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().copyMessageTo(testFolder).assertThat().containsMessages(fileModel);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can copy non-empty files via IMAP client to a different location")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldCopyNonEmptyFiles() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel1 = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel fileModel2 = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileModel1).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().usingSite(testSite).copyMessagesTo(testFolder).assertThat().containsMessages(fileModel1, fileModel2);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can copy empty file via IMAP client to a different location")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerShouldCopyEmptyFile() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel = new FileModel("EmptyFile.txt", FileType.TEXT_PLAIN, "");
        fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(fileModel);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().copyMessageTo(testFolder).assertThat().containsMessages(fileModel);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can copy file via IMAP client to a location where the message already exists")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void userShouldCopyFileWhereAlreadyExists() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel = new FileModel("CopyFile.txt", FileType.TEXT_PLAIN, "content of copied file");
        dataContent.usingSite(testSite).usingResource(testFolder).createContent(fileModel);
        FileModel fileToBeCopied = dataContent.usingSite(testSite).createContent(fileModel);
        
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileToBeCopied).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().copyMessageTo(testFolder).assertThat().containsMessages(fileToBeCopied);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot copy a file via IMAP client if it was already deleted from repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = MessagingException.class,
                                                                            expectedExceptionsMessageRegExp ="There are no messages to be copied")
    public void userCannotCopyDeletedFile() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileModel).assertThat().existsInImap();
        dataContent.usingResource(fileModel).deleteContent();
        imapProtocol.copyMessageTo(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot copy file via IMAP client to a location where you don't have permissions")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = MessagingException.class,
                                                                            expectedExceptionsMessageRegExp = ".*NO APPEND failed. Can't append message - Permission denied.*")
    public void userCannotCopyFileWhereNoPermissions() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createIMAPSite();
        FileModel file = dataContent.usingUser(user).usingSite(site).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataSite.usingUser(user).usingSite(testSite).setIMAPFavorite();
        
        imapProtocol.authenticateUser(user).usingSite(site).usingResource(file).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().copyMessageTo(testFolder);
    }
}
