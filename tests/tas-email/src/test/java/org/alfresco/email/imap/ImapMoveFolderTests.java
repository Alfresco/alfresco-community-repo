package org.alfresco.email.imap;

import javax.mail.FolderNotFoundException;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImapMoveFolderTests extends EmailTest
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
            description = "Verify move non-empty folder to a different location by admin user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldMoveNonEmptyFolder() throws Exception
    {
        testFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        FolderModel moveToFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        dataContent.usingAdmin().usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).usingResource(testFolder).moveTo(moveToFolder)
                .and().usingResource(testFolder).assertThat().doesNotExistInRepo()
                .then().usingResource(moveToFolder).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify move folder with Manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldMoveFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .and().usingResource(testFolder).assertThat().doesNotExistInRepo()
                .then().usingResource(moveToFolder).assertThat().contains(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder at location where the folder already exists is successful")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void userShouldMoveFolderWhereAlreadyExists() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        dataContent.usingUser(testUser).usingSite(testSite)
            .usingResource(moveToFolder)
            .createFolder(testFolder);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .and().usingResource(testFolder).assertThat().doesNotExistInRepo()
                .then().usingResource(moveToFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder with Contributor will create the new folder, but is NOT able to delete the current one")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteContributorCannotMoveFolder() throws Exception
    {
        UserModel siteContributor = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(siteContributor, testSite, UserRole.SiteContributor);
        dataSite.usingUser(siteContributor).usingSite(testSite).setIMAPFavorite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        imapProtocol.authenticateUser(siteContributor).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .usingResource(moveToFolder).assertThat().contains(testFolder)
                .and().usingResource(testFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder with Collaborator will create the new folder, but is NOT able to delete the current one")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteCollaboratorCannotMoveFolder() throws Exception
    {
        UserModel siteCollaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(siteCollaborator, testSite, UserRole.SiteContributor);
        dataSite.usingUser(siteCollaborator).usingSite(testSite).setIMAPFavorite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        imapProtocol.authenticateUser(siteCollaborator).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .usingResource(moveToFolder).assertThat().contains(testFolder)
                .and().usingResource(testFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder with Consumer will create the new folder, but is NOT able to delete the current one")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteConsumerCannotMoveFolder() throws Exception
    {
        UserModel siteConsumer = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(siteConsumer, testSite, UserRole.SiteContributor);
        dataSite.usingUser(siteConsumer).usingSite(testSite).setIMAPFavorite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        imapProtocol.authenticateUser(siteConsumer).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .usingResource(moveToFolder).assertThat().contains(testFolder)
                .and().usingResource(testFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder fails using user that doesn't have permission to the IMAP site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void unauthorizedUserCannotMoveFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        dataSite.usingUser(unauthorizedUser).usingSite(testSite).setIMAPFavorite();
        
        imapProtocol.authenticateUser(unauthorizedUser).usingSite(testSite).usingResource(testFolder).moveTo(moveToFolder)
                .usingResource(moveToFolder).assertThat().doesNotContain(testFolder)
                .and().usingResource(testFolder).assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify move folder with Manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void siteManagerShouldNotMoveDeletedFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FolderModel moveToFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder)
                .assertThat().existsInImap()
                .assertThat().existsInRepo()
                .delete()
                .moveTo(moveToFolder);
    }
}
