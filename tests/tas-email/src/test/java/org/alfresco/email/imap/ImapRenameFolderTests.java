package org.alfresco.email.imap;

import javax.mail.FolderNotFoundException;

import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImapRenameFolderTests extends EmailTest
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
            description = "Verify rename folder by admin user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldRenameFolder() throws Exception
    {
        testFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).usingResource(testFolder).rename(newFolderName)
                .usingSite(adminSite)
                    .assertThat().contains(new FolderModel(newFolderName))
                    .assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify renaming folder by user with MANAGER role")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldRenameFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).rename(newFolderName)
                .usingSite(testSite)
                    .assertThat().contains(new FolderModel(newFolderName))
                    .assertThat().doesNotContain(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with MANAGER role is not able to rename an nonexistent folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void siteManagerShouldNotRenameNonExistentFolder() throws Exception
    {    
        String newFolderName = RandomData.getRandomName("Folder");
        
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(FolderModel.getRandomFolderModel())
                    .rename(newFolderName)
                    .assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with MANAGER role is able to rename a folder with long name")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerShouldRenameFolderWithLongName() throws Exception
    {    
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String longName = RandomStringUtils.randomAlphabetic(200);     
        
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder)
                    .rename(longName)
                    .usingSite(testSite)
                        .assertThat().contains(new FolderModel(longName))
                        .assertThat().doesNotContain(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to rename a folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void unauthorizedUserShouldNotRenameFolder() throws Exception
    {    
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        
        dataSite.usingUser(unauthorizedUser).usingSite(testSite).setIMAPFavorite();
        
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(unauthorizedUser).usingResource(testFolder)
                    .rename(newFolderName)
                    .then().assertThat().doesNotExistInRepo()
                    .assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
     } 
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent user is not able to rename a folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = TestConfigurationException.class, 
    expectedExceptionsMessageRegExp = ".*You missed some configuration settings in your tests: User failed to connect to IMAP server LOGIN failed. Invalid login/password$")
    public void nonexistentUserShouldNotRenameFolder() throws Exception
    {    
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        
        imapProtocol.authenticateUser(UserModel.getRandomUserModel()).usingResource(testFolder)
                    .rename(newFolderName)
                    .then().assertThat().doesNotExistInRepo()
                    .and().assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
     } 
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify disconnected user is not able to rename a folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp= "Not connected")
    public void disconnectedUserShouldNotRenameFolder() throws Exception
    {    
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        
        imapProtocol.authenticateUser(testUser).usingResource(testFolder).assertThat().existsInImap().then()
                    .disconnect().usingResource(testFolder)
                    .rename(newFolderName)
                    .then().assertThat().doesNotExistInRepo()
                    .and().assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
     } 
   
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify renaming folder by user with COLLABORATOR role - folder created by self")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorShouldRenameFolderCreatedBySelf() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        
        dataSite.usingUser(collaboratorUser).usingSite(testSite).setIMAPFavorite();
        
        testFolder = dataContent.usingUser(collaboratorUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(collaboratorUser).usingSite(testSite).usingResource(testFolder)
                .rename(newFolderName)
                .usingSite(testSite)
                    .assertThat().contains(new FolderModel(newFolderName))
                    .assertThat().doesNotContain(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with COLLABORATOR role is not able to rename a folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorShouldNotRenameFolderCreatedByOtherUser() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);

        dataSite.usingUser(collaboratorUser).usingSite(testSite).setIMAPFavorite();
        
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(collaboratorUser).usingResource(testFolder).rename(newFolderName)
                .usingSite(testSite)
                    .assertThat().contains(testFolder)
                    .assertThat().doesNotContain(new FolderModel(newFolderName));
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with CONTRIBUTOR role is able to rename a folder created by self")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorShouldRenameFolderCreatedBySelf() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        
        dataSite.usingUser(contributorUser).usingSite(testSite).setIMAPFavorite();
                
        testFolder = dataContent.usingUser(contributorUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).usingResource(testFolder)
                    .rename(newFolderName)
                    .usingSite(testSite)
                        .assertThat().contains(new FolderModel(newFolderName))
                        .assertThat().doesNotContain(testFolder);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with CONTRIBUTOR role is NOT able to rename a folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorShouldNotRenameFolderCreatedByOtherUser() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        
        dataSite.usingUser(contributorUser).usingSite(testSite).setIMAPFavorite();
        
        FolderModel testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).usingResource(testFolder)
                    .rename(newFolderName).usingSite(testSite).then().assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
    } 
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with CONSUMER role is not able to rename a folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void consumerShouldNotRenameFolderCreatedByOtherUser() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        
        dataSite.usingUser(consumer).usingSite(testSite).setIMAPFavorite();
        
        FolderModel testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        String newFolderName = RandomData.getRandomName("Folder");
        imapProtocol.authenticateUser(consumer).usingSite(testSite).usingResource(testFolder)
                    .rename(newFolderName).usingSite(testSite).then()
                    .assertThat().contains(testFolder)
                    .and().assertThat().doesNotContain(new FolderModel(newFolderName));
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify renaming folder fails when it was deleted by another user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void userCannotRenameFolderDeletedByAnotherUser() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        dataSite.usingUser(contributorUser).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInRepo()
                .assertThat().existsInImap()
                .delete().disconnect();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).rename("deletedFolderRename");
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can rename folder to a name that contains white spaces")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanRenameFolderToAFolderThatContainsWhiteSpaces() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and()
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .rename("folder with name that contains spaces")
                .then().usingSite(testSite)
                    .assertThat().contains(new FolderModel("folder with name that contains spaces"))
                    .assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can rename folder successfully with name that contains special characters")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanRenameFolderWithSpecialCharacters() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and()
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .rename("(a)[b]!#%^")
                .then().usingSite(testSite)
                    .assertThat().contains(new FolderModel("(a)[b]!#%^"))
                    .assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can rename folder successfully with name that contains symbols")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanRenameFolderWithSymbols() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and()
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .rename("a£¥€$♊♎ ♏ ♐ ♑ ♒ ♓")
                .then().usingSite(testSite)
                    .assertThat().contains(new FolderModel("a£¥€$♊♎ ♏ ♐ ♑ ♒ ♓"))
                    .assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot create folder with name omegaΩ_<>./?")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCannotRenameFolderWithNameThatContainsRestrictedCharacters() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and()
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .rename("omegaΩ_<>./?")
                .then().usingSite(testSite)
                    .assertThat().contains(testFolder)
                    .assertThat().doesNotContain(new FolderModel("omegaΩ_<>./?"));
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify renaming folder fails when it was renamed by another user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void userCannotRenameFolderRenamedByAnotherUser() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        dataSite.usingUser(contributorUser).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).createFolder(testFolder)
                .assertThat().existsInRepo()
                .assertThat().existsInImap()
                .rename("folderRenamedByUser").disconnect();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).rename("deletedFolderRename");
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify a site folder cannot be renamed via IMAP client")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void verifyASiteFolderCannotBeRenamedViaImap() throws Exception
    {
        SiteModel siteModel = dataSite.usingUser(testUser).createIMAPSite();

        imapProtocol.authenticateUser(testUser).usingSiteRoot(siteModel).rename("new site name")
                .and()
                    .assertThat().doesNotExistInRepo()
                .then().usingSites()
                    .assertThat().doesNotContain(new FolderModel("new site name"));
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify renaming folder by changing the case")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void verifyFolderCanBeRenamedByChangingTheCase() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and()
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .rename(testFolder.getName().toUpperCase())
                .then().usingSite(testSite)
                    .assertThat().contains(new FolderModel(testFolder.getName().toUpperCase()))
                    .assertThat().doesNotContain(testFolder);
    }
}
