package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.FolderNotFoundException;
import javax.mail.MessagingException;

public class ImapCopyFolderTests extends EmailTest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
        adminSite = dataSite.usingAdmin().createIMAPSite();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify copy non-empty folder to a different location by admin user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldCopyNonEmptyFolder() throws Exception
    {
        testFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        FolderModel copyFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        FolderModel copiedFolder = testFolder;
        copiedFolder.setProtocolLocation(Utility.buildPath(copyFolder.getProtocolLocation(), testFolder.getName()));
        dataContent.usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).usingResource(testFolder)
                .copyTo(copyFolder).then().usingResource(copiedFolder).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify copy folder with Manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldCopyFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel copyFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel copiedFolder = testFolder;
        copiedFolder.setCmisLocation(Utility.buildPath(copyFolder.getCmisLocation(), testFolder.getName()));
        copiedFolder.setProtocolLocation(Utility.buildPath(copyFolder.getProtocolLocation(), testFolder.getName()));
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder)
                .copyTo(copyFolder).then().usingResource(copiedFolder).assertThat().existsInRepo().assertThat().existsInImap();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify copy folder that has been deleted with Manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void siteManagerShouldNotCopyFolderThatHasBeenDeleted() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel folderModel = dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFolder).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(folderModel).delete()
                .and().assertThat().doesNotExistInRepo().assertThat().doesNotContain(folderModel)
                .then().copyTo(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify copy folder with Contributor user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorShouldCopyFolder() throws Exception
    {
        UserModel contributorUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel copyFolder = dataContent.usingUser(contributorUser).usingSite(testSite).createFolder();
        FolderModel copiedFolder = testFolder;
        copiedFolder.setProtocolLocation(Utility.buildPath(copyFolder.getProtocolLocation(), testFolder.getName()));
        dataContent.usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).usingResource(testFolder)
                .copyTo(copyFolder).then().usingResource(copiedFolder).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify copy folder with Collaborator user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorShouldCopyFolder() throws Exception
    {
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel copyFolder = dataContent.usingUser(collaboratorUser).usingSite(testSite).createFolder();
        FolderModel copiedFolder = testFolder;
        copiedFolder.setProtocolLocation(Utility.buildPath(copyFolder.getProtocolLocation(), testFolder.getName()));
        dataContent.usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(collaboratorUser).usingSite(testSite).usingResource(testFolder)
                .copyTo(copyFolder).then().usingResource(copiedFolder).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that copy folder with Consumer user is not possible")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void consumerShouldNotCopyFolder() throws Exception
    {
        UserModel consumerUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(consumerUser, testSite, UserRole.SiteConsumer);
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel copyFolder = new FolderModel("copyFolder");
        imapProtocol.authenticateUser(consumerUser).usingSite(testSite).createFolder(copyFolder).usingResource(testFolder)
                .copyTo(copyFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify copy folder to a location for which the user does not have permission")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = MessagingException.class)
    public void userCopyFolderToALocationThatHeDoesNotHaveAccess() throws Exception
    {
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        testFolder = dataContent.usingAdmin().usingSite(testSite).createFolder();
        FolderModel copyFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        dataContent.usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingResource(testFolder)
                .copyTo(copyFolder);
    }
}
