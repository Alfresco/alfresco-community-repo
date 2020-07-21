package org.alfresco.email.imap;

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

import javax.mail.FolderNotFoundException;

/**
 * Tests for Create Folder action using IMAP client
 * 
 * @author Cristina Axinte
 *
 */
public class ImapCreateFolderTests extends EmailTest
{
    private UserModel adminUser;
    private SiteModel imapSite;
    
    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
        imapSite = dataSite.usingUser(testUser).createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify admin can create a folder in Afresco IMAP root folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminCanCreateFolderInAlfrescoImapRoot() throws Exception
    {
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(adminUser).usingAlfrescoImap().createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify user can create a folder in Afresco IMAP root folder, but it is not displayed in Alfresco Repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void userCanCreateFolderInAlfrescoImapRootButNotDisplayedInRepo() throws Exception
    {
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingAlfrescoImap().createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify admin can create a folder in Sites folder and it will be displayed in Alfresco Repository/Sites")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminCanCreateFolderInSitesFolder() throws Exception
    {
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(adminUser)
                .usingSites().createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify manager can create a folder in IMAP Site > documentLibrary")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void managerCanCreateFolderInIMAPSite() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(managerUser, imapSite, UserRole.SiteManager);
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(managerUser)
                .usingSite(imapSite).createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager can create a folder  with spaces in name in IMAP Site > documentLibrary")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void managerCanCreateFolderWithSpacesInIMAPSite() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();
        imapSite = dataSite.usingUser(managerUser).createIMAPSite();
        FolderModel folderToCreate = new FolderModel("Folder with spaces in name");

        imapProtocol.authenticateUser(managerUser)
                .usingSite(imapSite).createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can create a folder in IMAP Site > documentLibrary")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorCanCreateFolderInImapSite() throws Exception
    {
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser, imapSite, UserRole.SiteCollaborator);
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(collaboratorUser)
                .usingSite(imapSite).createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can create a folder in IMAP Site > documentLibrary")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorCanCreateFolderInImapSite() throws Exception
    {
        UserModel contributorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, imapSite, UserRole.SiteCollaborator);
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(contributorUser)
                .usingSite(imapSite).createFolder(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot create a folder in IMAP Site > documentLibrary")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void consumerCannotCreateFolderInImapSite() throws Exception
    {
        UserModel consumerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(consumerUser, imapSite, UserRole.SiteConsumer);
        dataSite.usingUser(consumerUser).usingSite(imapSite).setIMAPFavorite();
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(consumerUser).usingSite(imapSite).createFolder(folderToCreate)
                .and()
                    .assertThat().doesNotExistInRepo()
                .then().usingSite(imapSite)
                    .assertThat().doesNotContain(folderToCreate);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can create folder successfully with name that contains special characters")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanCreateFolderWithSpecialCharacters() throws Exception
    {
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FolderModel folderToCreate = new FolderModel("(a)[b]!#%^");

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(folderToCreate)
                .then()
                    .assertThat().existsInImap()
                    .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can create folder successfully with name that contains symbols")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanCreateFolderWithSymbols() throws Exception
    {
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FolderModel folderToCreate = new FolderModel("a£¥€$♊♎ ♏ ♐ ♑ ♒ ♓");

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(folderToCreate)
                .then()
                    .assertThat().existsInImap()
                    .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot create folder with name omegaΩ_<>./?")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCannotCreateFolderWithNameThatContainsRestrictedCharacters() throws Exception
    {
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FolderModel folderToCreate = new FolderModel("omegaΩ_<>./?");

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(folderToCreate)
                .and()
                    .assertThat().doesNotExistInRepo()
                .then().usingSite(testSite)
                    .assertThat().doesNotContain(folderToCreate);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify create folder using an user and check folder can be view by another user with access to the same location/site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void verifyUserCanViewTheFolderCreatedByAnotherUser() throws Exception
    {
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        FolderModel folderToCreate = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(collaboratorUser).usingSite(testSite).createFolder(folderToCreate).disconnect();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(folderToCreate)
                .assertThat().existsInImap()
                .assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify error is returned for unauthenticated user on creating folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Not connected")
    public void verifyErrorIsReturnedForUnauthenticatedUserTryingToCreateFolder() throws Exception
    {
        imapProtocol.authenticateUser(testUser).usingSite(imapSite).disconnect().then().createFolder(FolderModel.getRandomFolderModel());
    }
}
