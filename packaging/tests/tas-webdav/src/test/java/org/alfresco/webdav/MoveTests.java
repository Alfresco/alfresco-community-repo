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

public class MoveTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FolderModel sourceFolder, destinationFolder;
    FileModel testFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        destinationFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(destinationFolder);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that admin user can move folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void adminShouldMoveFolder() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        sourceFolder = new FolderModel("move " + RandomData.getRandomFolder());
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingRoot()
            .createFolder(sourceFolder).and().assertThat().existsInRepo()
                .then().moveTo(guest).and().assertThat().existsInRepo().and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .when().usingResource(sourceFolder).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager can move folder with content in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteManagerShouldMoveFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(sourceFolder)
                .then().usingResource(sourceFolder).createFile(testFile).and().assertThat().existsInRepo()
                    .when().usingResource(sourceFolder).moveTo(destinationFolder).and().assertThat().existsInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .then().usingResource(sourceFolder).assertThat().doesNotExistInRepo()
                    .and().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that user with contributor role cannot move folder with content in site folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorShouldNotMoveFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel contributor = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                    .then().authenticateUser(contributor).usingResource(sourceFolder)
                        .moveTo(destinationFolder).and().assertThat().doesNotExistInRepo().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .when().usingResource(sourceFolder).assertThat().existsInRepo()
                        .and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that user with collaborator role cannot move folder with content in site folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorShouldNotMoveFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel collaborator = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                    .then().authenticateUser(collaborator).usingResource(sourceFolder)
                        .moveTo(destinationFolder).and().assertThat().doesNotExistInRepo().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .when().usingResource(sourceFolder).assertThat().existsInRepo()
                        .and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that user with consumer role cannot move folder with content in site folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteConsumerShouldNotMoveFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        UserModel consumer = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFolder(sourceFolder)
                .and().usingResource(sourceFolder).createFile(testFile)
                    .then().authenticateUser(consumer).usingResource(sourceFolder)
                        .moveTo(destinationFolder).and().assertThat().doesNotExistInRepo().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .when().usingResource(sourceFolder).assertThat().existsInRepo()
                        .and().usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager can move file from site in repository")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteManagerShouldMoveFileInRepository() throws Exception
    {
        FolderModel guest = FolderModel.getSharedFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .moveTo(guest).assertThat().existsInRepo().and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .then().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that disconected user cannot move folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void disconnectedUserShouldNotMoveFolder() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(sourceFolder)
                .then().disconnect().and()
                .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().authenticateUser(managerUser)
                    .usingResource(sourceFolder).assertThat().existsInRepo().assertThat().existsInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that disconected user cannot move file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void disconnectedUserShouldNotMoveFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .then().disconnect().and()
                .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().authenticateUser(managerUser)
                    .usingResource(testFile).assertThat().existsInRepo().assertThat().existsInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that unauthorized user cannot move file or folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void unauthorizedUserShouldNotMoveContent() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
            .createFolder(sourceFolder)
                .then().authenticateUser(unauthorized)
                .usingResource(testFile)
                    .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .usingResource(sourceFolder)
                    .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .usingResource(testFile).assertThat().existsInRepo().assertThat().existsInWebdav()
                .usingResource(sourceFolder).assertThat().existsInRepo().assertThat().existsInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that inexistent user cannot move file or folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void inexistentUserShouldNotMoveContent() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
            .createFolder(sourceFolder)
                .then().authenticateUser(UserModel.getRandomUserModel())
                .usingResource(testFile)
                    .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .usingResource(sourceFolder)
                    .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .authenticateUser(managerUser)
                    .usingResource(testFile).assertThat().existsInRepo().assertThat().existsInWebdav()
                    .usingResource(sourceFolder).assertThat().existsInRepo().assertThat().existsInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager cannot move locked file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerShouldNotMoveLockedFolder() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                    .then().lock().assertThat().isLocked()
                    .when().usingResource(testFile)
                        .moveTo(destinationFolder).and().assertThat().doesNotExistInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_LOCKED)
                    .and().usingResource(testFile).assertThat().existsInRepo()
                        .assertThat().existsInWebdav().and().assertThat().isLocked();
    }

    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION,
            description ="Verify the response code for a conflict when a file is moved in a folder that already has a file with the same name")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void conflictWhileMovingFile() throws Exception
    {
        sourceFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel testFileSource = new FileModel(testFile);
        FileModel testFileDestination = new FileModel(testFile);
        webDavProtocol.authenticateUser(dataUser.getAdminUser())
                .usingSite(testSite).createFolder(sourceFolder).assertThat().existsInRepo()
                .then().usingResource(sourceFolder).createFile(testFileSource).assertThat().existsInRepo()
                .then().usingResource(destinationFolder).createFile(testFileDestination).assertThat().existsInRepo()
                .when().usingResource(testFileSource).doNotOverwriteIfExists()
                .moveTo(destinationFolder).and().assertThat().hasStatus(HttpStatus.SC_PRECONDITION_FAILED);
    }
}
