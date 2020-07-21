package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.FolderNotFoundException;

public class ImapDeleteFolderTests extends EmailTest
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
            description = "Verify admin can delete folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).createFolder(testFolder)
                .assertThat().existsInRepo()
                .assertThat().existsInImap()
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(adminSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify site Manager can delete a folder with a message(file)")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldDeleteNonEmptyFolder() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(user).createIMAPSite();
        FolderModel folder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(user).usingSite(site).createFolder(folder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
        dataContent.usingUser(user).usingSite(site).usingResource(folder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.usingSite(site).usingResource(folder).assertThat().countMessagesIs(1)
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(site).assertThat().doesNotContain(folder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can delete a empty folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerShouldDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInRepo()
                .assertThat().existsInImap()
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(testSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can delete a folder that contains collaborator user messages")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, enabled = false)//disable since it's failing intermitent
    public void siteManagerShouldDeleteFolderWithCollaboratorMessages() throws Exception
    {
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
        testFile = dataContent.usingUser(collaboratorUser).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.usingSite(testSite).usingResource(testFolder)
                .assertThat().countMessagesIs(1)
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(testSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can delete a folder that contains contributor user messages")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, enabled = false)//disable since it's failing intermitent
    public void siteManagerShouldDeleteFolderWithContributorMessages() throws Exception
    {
        UserModel contributorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
        dataContent.usingUser(contributorUser).usingSite(testSite).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.usingSite(testSite).usingResource(testFolder).assertThat().countMessagesIs(1)
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(testSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that an unauthorized user can not delete folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void unauthorizedUserShouldNotDeleteFolder() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(unauthorizedUser).usingResource(testFolder).delete();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can delete a folder that contains consumer user messages")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void siteManagerShouldDeleteFolderWithConsumerMessages() throws Exception
    {
        UserModel consumerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(consumerUser, testSite, UserRole.SiteContributor);
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
        dataContent.usingUser(consumerUser).usingSite(testSite).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataUser.usingUser(testUser).removeUserFromSite(consumerUser, testSite);
        dataUser.usingUser(testUser).addUserToSite(consumerUser, testSite, UserRole.SiteConsumer);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).assertThat().countMessagesIs(1)
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(testSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot delete folder that no longer exists")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void userCannotDeleteFolderThatNoLongerExists() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .delete()
                .assertThat().doesNotExistInRepo()
                .usingSite(testSite).assertThat().doesNotContain(testFolder)
                .usingResource(testFolder)
                .delete();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting folder that contains messages by COLLABORATOR user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void collaboratorTriesToDeleteFolderContainingMessagesByCollaborator() throws Exception
    {
        UserModel collaboratorUser1 = dataUser.createRandomTestUser();
        UserModel collaboratorUser2 = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser1, testSite, UserRole.SiteCollaborator);
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser2, testSite, UserRole.SiteCollaborator);
        testFolder = FolderModel.getRandomFolderModel();

        dataSite.usingUser(collaboratorUser1).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(collaboratorUser1).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .disconnect();
        testFile = dataContent.usingUser(collaboratorUser1).usingSite(testSite).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataSite.usingUser(collaboratorUser2).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(collaboratorUser2).usingSite(testSite).usingResource(testFolder)
                .delete()
                .assertThat().existsInRepo()
                .usingSite(testSite).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting folder that contains messages by CONTRIBUTOR user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void contributorTriesToDeleteFolderContainingMessagesByContributor() throws Exception
    {
        UserModel contributorUser1 = dataUser.createRandomTestUser();
        UserModel contributorUser2 = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser1, testSite, UserRole.SiteContributor);
        dataUser.usingUser(testUser).addUserToSite(contributorUser2, testSite, UserRole.SiteContributor);
        testFolder = FolderModel.getRandomFolderModel();

        dataSite.usingUser(contributorUser1).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(contributorUser1).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .disconnect();
        testFile = dataContent.usingUser(contributorUser1).usingSite(testSite).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataSite.usingUser(contributorUser2).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(contributorUser2).usingSite(testSite).usingResource(testFolder)
                .delete()
                .assertThat().existsInRepo()
                .usingSite(testSite).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting folder that contains messages by CONSUMER user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void consumerTriesToDeleteFolderContainingMessagesByConsumer() throws Exception
    {
        UserModel consumerUser1 = dataUser.createRandomTestUser();
        UserModel consumerUser2 = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(consumerUser1, testSite, UserRole.SiteCollaborator);
        dataUser.usingUser(testUser).addUserToSite(consumerUser2, testSite, UserRole.SiteConsumer);
        testFolder = FolderModel.getRandomFolderModel();

        dataSite.usingUser(consumerUser1).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(consumerUser1).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .disconnect();
        testFile = dataContent.usingUser(consumerUser1).usingSite(testSite).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataUser.usingUser(testUser).removeUserFromSite(consumerUser1, testSite);
        dataUser.usingUser(testUser).addUserToSite(consumerUser1, testSite, UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser2).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(consumerUser2).usingSite(testSite).usingResource(testFolder)
                .delete()
                .assertThat().existsInRepo()
                .usingSite(testSite).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify delete folder from a location where you don't have permission")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void userTriesToDeleteFolderFromALocationToWhichHeDoesNotHavePermissionToDelete() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingAdmin().createIMAPSite();
        testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFolder(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .disconnect();

        dataSite.usingUser(userModel).usingSite(privateSite).setIMAPFavorite();
        imapProtocol.authenticateUser(userModel).usingSite(testSite).usingResource(testFolder).delete();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin cannot delete a open folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "This operation is not allowed on an open folder")
    public void adminTriesToDeleteOpenFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).createFolder(testFolder)
                .assertThat().existsInRepo()
                .assertThat().existsInImap()
                .attemptToDeleteOpenFolder();
    }
}
