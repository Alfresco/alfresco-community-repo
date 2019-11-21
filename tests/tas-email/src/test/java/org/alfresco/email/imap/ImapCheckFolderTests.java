package org.alfresco.email.imap;

import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.FolderNotFoundException;
import javax.mail.MessagingException;

public class ImapCheckFolderTests extends EmailTest
{
    UserModel managerUser;
    SiteModel managerTestSite;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        managerTestSite = dataSite.usingUser(managerUser).createIMAPSite();
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Returns the current working directory with admin user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldGetCurrentWorkingDirectory() throws Exception
    {
        testUser = dataUser.getAdminUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFolder).assertThat().existsInRepo()
                .then().assertThat().currentDirectoryIs(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify in IMAP client current directory/list of directories for site manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldGetCurrentWorkingDirectory() throws Exception
    {
        testFolder = dataContent.usingUser(managerUser).usingSite(managerTestSite).createFolder();
        imapProtocol.authenticateUser(managerUser).usingSite(managerTestSite).usingResource(testFolder).assertThat().existsInRepo()
                .then().assertThat().currentDirectoryIs(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Returns the directories list of the root for site manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldGetDirectoriesListOfRoot() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        FolderModel alfrescoImap = FolderModel.getRandomFolderModel();
        alfrescoImap.setName("Alfresco IMAP");
        FolderModel inbox = FolderModel.getRandomFolderModel();
        inbox.setName("INBOX");
        imapProtocol.authenticateUser(testUser).usingRoot()
                .then().assertThat().contains(alfrescoImap, inbox);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "User finds all its folders for IMAP Sites in Alfresco IMAP > Sites folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void userShouldFindAllIMAPSitesFoldersInAlfrescoIMAPFolder() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        SiteModel imapSite1 = dataSite.usingUser(testUser).createIMAPSite();
        SiteModel imapSite2 = dataSite.usingUser(testUser).createIMAPSite();

        imapProtocol.authenticateUser(testUser).usingSites().assertThat().existsInImap()
                .then().assertThat().contains(imapSite1, imapSite2);
    }
    
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify in IMAP client current directory/list of directories for COLLABORATOR user - folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorShouldGetCurrentWorkingDirectory() throws Exception
    {               
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, managerTestSite, UserRole.SiteCollaborator);          
                            
        testFolder = dataContent.usingUser(managerUser).usingSite(managerTestSite).createFolder();        
        imapProtocol.authenticateUser(collaborator).usingSite(managerTestSite).usingResource(testFolder).assertThat().existsInRepo()
                .then().assertThat().currentDirectoryIs(testFolder);
        
        dataSite.usingUser(collaborator).usingSite(managerTestSite).setIMAPFavorite();
        
        FolderModel testFolder1 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                                 .usingResource(testFolder).createFolder();
        FolderModel testFolder2 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                                 .usingResource(testFolder).createFolder();      
        
        imapProtocol.authenticateUser(collaborator).usingSite(managerTestSite)
                    .usingResource(testFolder)
                    .then().assertThat().contains(testFolder1,testFolder2);     
      }
               
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify in IMAP client current directory/list of directories for CONTRIBUTOR user - folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorShouldGetCurrentWorkingDirectory() throws Exception
    {               
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, managerTestSite, UserRole.SiteContributor);          
                            
        testFolder = dataContent.usingUser(managerUser).usingSite(managerTestSite).createFolder();        
        imapProtocol.authenticateUser(contributor).usingSite(managerTestSite).usingResource(testFolder).assertThat().existsInRepo()
                .then().assertThat().currentDirectoryIs(testFolder);
        
        dataSite.usingUser(contributor).usingSite(managerTestSite).setIMAPFavorite();
        
        FolderModel testFolder1 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                .usingResource(testFolder).createFolder();
        FolderModel testFolder2 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                .usingResource(testFolder).createFolder();      

        imapProtocol.authenticateUser(contributor).usingSite(managerTestSite)
                    .usingResource(testFolder)
                    .then().assertThat().contains(testFolder1,testFolder2);     
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify in IMAP client current directory/list of directories for CONSUMER user - folder created by other user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void consumerShouldGetCurrentWorkingDirectory() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, managerTestSite, UserRole.SiteConsumer);

        testFolder = dataContent.usingUser(managerUser).usingSite(managerTestSite).createFolder();
        imapProtocol.authenticateUser(consumer).usingSite(managerTestSite).usingResource(testFolder).assertThat().existsInRepo()
                .then().assertThat().currentDirectoryIs(testFolder);

        dataSite.usingUser(consumer).usingSite(managerTestSite).setIMAPFavorite();

        FolderModel testFolder1 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                .usingResource(testFolder).createFolder();
        FolderModel testFolder2 = dataContent.usingUser(managerUser).usingSite(managerTestSite)
                .usingResource(testFolder).createFolder();

        imapProtocol.authenticateUser(consumer).usingSite(managerTestSite)
                .usingResource(testFolder)
                .then().assertThat().contains(testFolder1,testFolder2);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify unauthorized is not able to access document library section ( PUBLIC imap SITE)")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void unAuthorizedUserCannotAccessPublicImapSiteDocumentLibrary() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        SiteModel publicImapSite = dataSite.usingUser(testUser).createPublicRandomSite();
        dataSite.usingUser(unauthorizedUser).usingSite(publicImapSite).setIMAPFavorite();

        imapProtocol.authenticateUser(unauthorizedUser).usingSite(publicImapSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify unauthorized is not able to access document library section ( Moderated imap SITE)")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void unAuthorizedUserCannotAccessModeratedImapSiteDocumentLibrary() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        SiteModel moderatedImapSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        dataSite.usingUser(unauthorizedUser).usingSite(moderatedImapSite).setIMAPFavorite();

        imapProtocol.authenticateUser(unauthorizedUser).usingSite(moderatedImapSite).assertThat().doesNotContain(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify unauthorized is not able to access document library section ( Private imap SITE)")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = MessagingException.class,
            expectedExceptionsMessageRegExp = ".*Access Denied.*You do not have the appropriate permissions to perform this operation.*")
    public void unAuthorizedUserCannotAccessPrivateImapSiteDocumentLibrary() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        SiteModel privateImapSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        dataSite.usingUser(unauthorizedUser).usingSite(privateImapSite).setIMAPFavorite();

        imapProtocol.authenticateUser(unauthorizedUser).usingSite(privateImapSite).assertThat().doesNotContain(testFolder);
    }
}
