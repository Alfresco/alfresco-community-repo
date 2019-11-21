package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CopyTests extends WebDavTest
{
    UserModel managerUser, adminUser;
    SiteModel testSite;
    FolderModel sourceFolder, destinationFolder;
    FileModel testFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        destinationFolder = FolderModel.getRandomFolderModel();
        adminUser = dataUser.getAdminUser();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(destinationFolder);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
            description = "Verify that admin user can copy an empty folder in repository")
    public void adminShouldCopyEmptyFolderToNewLocation() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        sourceFolder = new FolderModel("copy " + RandomData.getRandomFolder());
        
        webDavProtocol.authenticateUser(adminUser).usingRoot().createFolder(sourceFolder).then()
                .copyTo(guest).and().assertThat().existsInRepo()
                .and().assertThat().hasStatus(HttpStatus.SC_CREATED).when()
                .usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that an user with site manager role can copy an empty folder with content in site")
   public void siteManagerUserShouldCopyEmptyFolder() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(managerUser, testSite, UserRole.SiteManager);
        sourceFolder = FolderModel.getRandomFolderModel();
        
        webDavProtocol.authenticateUser(adminUser).usingSite(testSite).createFolder(sourceFolder).and().assertThat().existsInRepo()
                .then().copyTo(destinationFolder).and().assertThat().existsInRepo().and().assertThat()
                .hasStatus(HttpStatus.SC_CREATED).when().usingResource(sourceFolder)
                .assertThat().existsInRepo();
    }   
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site manager can copy an empty folder with content in site")
   public void siteManagerShouldCopyEmptyFolder() throws Exception
    {
       sourceFolder = FolderModel.getRandomFolderModel();
        
       webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder).assertThat().existsInRepo()
           .then().copyTo(destinationFolder)
           .and().assertThat().existsInRepo().and().assertThat().hasStatus(HttpStatus.SC_CREATED)
           .when().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.SANITY, 
            description = "Verify that an user with site manager role can copy folder with content in site")
   public void siteManagerUserShouldCopyFolderWithContent() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        UserModel managerUser = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(managerUser, testSite, UserRole.SiteManager);
        
        webDavProtocol.authenticateUser(adminUser).usingSite(testSite).createFolder(sourceFolder)
                .then().usingResource(sourceFolder).createFile(testFile)
                .and().assertThat().existsInRepo().when().usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().existsInRepo().and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().hasFiles(testFile).then().usingResource(sourceFolder).assertThat()
                .existsInRepo().and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.SANITY, 
            description = "Verify that site manager can copy folder with content in site")
   public void siteManagerShouldCopyFolderWithContent() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);      
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .then().usingResource(sourceFolder).createFile(testFile).and().assertThat().existsInRepo()
                .when().usingResource(sourceFolder)
                .copyTo(destinationFolder).and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED).and().assertThat().hasFiles(testFile).then()
                .usingResource(sourceFolder).assertThat()
                .existsInRepo().and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with collaborator role can copy an empty folder in site folder")
    public void siteCollaboratorShouldCopyEmptyFolderAddedByOtherUser() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        sourceFolder = FolderModel.getRandomFolderModel();
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().assertThat().existsInRepo().then()
                .authenticateUser(collaborator).usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED)
                .when().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
            description = "Verify that user with collaborator role can copy folder with content in site folder")
    public void siteCollaboratorShouldCopyFolderWithContentAddedByOtherUser() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel collaborator = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.XML);
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                .then().authenticateUser(collaborator).usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED).and().assertThat().hasFiles(testFile)
                .then().usingResource(sourceFolder).assertThat()
                .existsInRepo().and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with collaborator role can copy a file in site")
   public void siteCollaboratorShouldCopyFileAddedByOtherUser() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
               
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo().then()
                .authenticateUser(collaborator).usingResource(testFile).copyTo(destinationFolder)
                .and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED)
                .when().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with contribuitor role can copy an empty folder in site")
   public void siteContribuitorShouldCopyEmptyFolderAddedByOtherUser() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();      
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);        
        sourceFolder = FolderModel.getRandomFolderModel();
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().assertThat().existsInRepo().then().authenticateUser(contributor)
                .usingResource(sourceFolder).copyTo(destinationFolder).and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED)
                .when().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.SANITY, 
            description = "Verify that user with contributor role can copy folder with content in site folder")
     public void siteContributorShouldCopyFolderWithContentAddedByOtherUser() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel contributor = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                .then().authenticateUser(contributor).usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED).and().assertThat().hasFiles(testFile)
                .when().usingResource(sourceFolder).assertThat()
                .existsInRepo().and().usingResource(testFile).assertThat().existsInRepo();
    }   
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with contributor role can copy a file in site")
   public void siteContributorShouldCopyFileAddedByOtherUser() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteCollaborator);
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
               
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo().then()
                .authenticateUser(contributor).usingResource(testFile)
                .copyTo(destinationFolder).and().assertThat().existsInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_CREATED)
                .when().usingResource(testFile).assertThat().existsInRepo();
    }    
   
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with consumer role cannot copy an empty folder in site")
   public void siteConsumerShouldNotCopyEmptyFolderAddedByOtherUser() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel consumer = dataUser.createRandomTestUser();      
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);       
               
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().assertThat().existsInRepo().then()
                .authenticateUser(consumer).usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().doesNotExistInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .when().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
            description = "Verify that user with consumer role cannot copy folder with content in site folder")
    public void siteConsumerShouldNotCopyFolderWithContentAddedByOther() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSPOWERPOINT);
        UserModel consumer = dataUser.createRandomTestUser();   
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                .then().authenticateUser(consumer).usingResource(sourceFolder).copyTo(destinationFolder)
                .and().assertThat().doesNotExistInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .when().usingResource(sourceFolder).assertThat().existsInRepo()
                .and().usingResource(testFile).assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType = ExecutionType.REGRESSION, 
            description = "Verify that user with consumer role cannot copy a file in site")
   public void siteConsumerShouldNotCopyFileAddedByOtherUser() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
               
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo().then().authenticateUser(consumer)
                .usingResource(testFile).copyTo(destinationFolder).and().assertThat().doesNotExistInRepo().and()
                .assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .when().usingResource(testFile).assertThat().existsInRepo();
    }       
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
            description = "Verify that site manager can copy file from site in repository")
    public void siteManagerShouldCopyFileInRepository() throws Exception
    {
        FolderModel guest = FolderModel.getSharedFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                .copyTo(guest).assertThat().existsInRepo().and().assertThat()
                .hasStatus(HttpStatus.SC_CREATED)
                .then().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site manager can copy an empty folder twice in same location")
    public void siteManagerShouldCopyEmptyFolderTwiceInSameLocation() throws Exception
    {
       sourceFolder = FolderModel.getRandomFolderModel();
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                 .copyTo(destinationFolder).assertThat().existsInRepo()
                 .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                 .then().copyTo(destinationFolder).assertThat().existsInRepo()
                 .and().assertThat().hasStatus(HttpStatus.SC_CREATED);            
    }     
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that site manager can copy a folder with content twice in same location")
    public void siteManagerShouldCopyFolderWithContentTwiceInSameLocation() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                 .and().usingResource(sourceFolder).createFile(testFile)
                 .copyTo(destinationFolder).assertThat().existsInRepo()
                 .and().assertThat().hasStatus(HttpStatus.SC_CREATED).then().copyTo(destinationFolder)
                 .assertThat().existsInRepo().and().assertThat().hasStatus(HttpStatus.SC_CREATED);            
    }     
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot copy nonexistent folder with content from site")  
    public void siteManagerShouldNotCopyNonexistentFolderWithContent() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(managerUser, testSite, UserRole.SiteManager);
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                 .and().usingResource(sourceFolder).createFile(testFile)
                 .delete().and().assertThat().doesNotExistInRepo()
                 .when().copyTo(destinationFolder).assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot copy nonexistent empty folder from site")
    public void siteManagerShouldNotCopyNonexistentEmptyFolder() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(managerUser, testSite, UserRole.SiteManager);
        sourceFolder = FolderModel.getRandomFolderModel();
       
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                 .delete().and().assertThat().doesNotExistInRepo()
                 .when().copyTo(destinationFolder).assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot copy nonexistent file from site")    
    public void siteManagerShouldNotCopyNonexistentFile() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();  
        dataUser.usingUser(managerUser).addUserToSite(managerUser, testSite, UserRole.SiteManager);
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                 .delete().and().assertThat().doesNotExistInRepo().when().copyTo(destinationFolder)
                 .assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }        
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthenticated user cannot copy a folder with content from site")
    public void unauthenticatedUserShouldNotCopyFolderWithContent() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                .and().assertThat().existsInRepo().when().disconnect().usingResource(sourceFolder)
                .copyTo(destinationFolder).and().assertThat().doesNotExistInRepo()
                .and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthenticated user cannot copy an empty folder from site")  
    public void unauthenticatedUserShouldNotCopyEmptyFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();        
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                 .and().assertThat().existsInRepo().when().disconnect().usingResource(sourceFolder)
                 .copyTo(destinationFolder).assertThat().doesNotExistInRepo()
                 .and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                 .then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthenticated user cannot copy a file from site")  
    public void unauthenticatedUserShouldNotCopyFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile)
                 .and().assertThat().existsInRepo().when().disconnect().usingResource(testFile)
                 .copyTo(destinationFolder).assertThat().doesNotExistInRepo()
                 .and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                 .then().usingResource(testFile).assertThat().existsInRepo();
    }   
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that nonnexistent user cannot copy a folder with content from site")
    public void nonexistentUserShouldNotCopyFolderWithContent() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                .and().assertThat().existsInRepo().then().authenticateUser(UserModel.getRandomUserModel()).usingResource(sourceFolder)
                .copyTo(destinationFolder).then().usingAdmin().and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .assertThat().doesNotExistInRepo().then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that nonexistent user cannot copy an empty folder from site")
    public void nonexistentUserShouldNotCopyEmptyFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().assertThat().existsInRepo()
                .then().authenticateUser(UserModel.getRandomUserModel()).usingResource(sourceFolder)
                .copyTo(destinationFolder).then().usingAdmin().and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .assertThat().doesNotExistInRepo().then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthorized user cannot copy an empty folder from site")
    public void unauthorizedUserShouldNotCopyEmptyFolder() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        sourceFolder = FolderModel.getRandomFolderModel();
        
        webDavProtocol.authenticateUser(adminUser).usingRoot().createFolder(sourceFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(unauthorizedUser).usingResource(sourceFolder)
                .copyTo(destinationFolder).assertThat().hasStatus(HttpStatus.SC_FORBIDDEN).and()
                .assertThat().doesNotExistInRepo().then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthorized user cannot copy a folder with content from site")
    public void unauthorizedUserShouldNotCopyFolderWithContent() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        
        webDavProtocol.authenticateUser(adminUser).usingRoot().createFolder(sourceFolder).and().usingResource(sourceFolder)
                .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(unauthorizedUser).usingResource(sourceFolder)
                .copyTo(destinationFolder).assertThat().hasStatus(HttpStatus.SC_FORBIDDEN).and()
                .assertThat().doesNotExistInRepo().then().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager can copy a locked file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldCopyLockedFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .lock()
                    .copyTo(destinationFolder).and().assertThat().existsInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                        .and().assertThat().hasFiles(testFile)
                    .and().usingResource(testFile).assertThat().existsInRepo();
    }
}
