package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.alfresco.utility.model.FileType;

public class RenameFileTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FileModel testFile;
    private String renamePrefix = "-edit";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that admin user can rename file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void adminShouldRenameFile() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel originalFileModel = new FileModel(testFile);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guest)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().rename(testFile.getName() + renamePrefix).assertThat().existsInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                        .then().usingResource(originalFileModel).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager can rename file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldRenameFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        FileModel originalFileModel = new FileModel(testFile);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().rename(testFile.getName() + renamePrefix).assertThat().existsInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .then().usingSite(testSite).usingResource(originalFileModel).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                    description ="Verify that user with contributor role cannot rename file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteContributorShouldNotRenameFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSPOWERPOINT);
        FileModel originalFileModel = new FileModel(testFile);
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(contributor).usingResource(testFile)
                    .rename(testFile.getName() + renamePrefix).assertThat().doesNotExistInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                        .then().usingSite(testSite).usingResource(originalFileModel).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that user with collaborator role can rename file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteCollaboratorShouldRenameFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        FileModel originalFileModel = new FileModel(testFile);
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(collaborator).usingResource(testFile)
                    .rename(testFile.getName() + renamePrefix).assertThat().existsInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                        .then().usingSite(testSite).usingResource(originalFileModel).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that user with consumer role cannot rename file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteConsumerShouldNotRenameFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        FileModel originalFileModel = new FileModel(testFile);
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(consumer).usingResource(testFile)
                    .rename(testFile.getName() + renamePrefix).assertThat().doesNotExistInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                        .then().usingSite(testSite).usingResource(originalFileModel).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager cannot rename inexistent file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldNotRenameInexistentFile() throws Exception
    {
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .usingResource(FileModel.getRandomFileModel(FileType.TEXT_PLAIN))
                .rename("inexistent-edited.txt").assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager can rename file with 200 characters")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldRenameFileWithLongName() throws Exception
    {
        String longName = RandomStringUtils.randomAlphabetic(200);
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        FileModel originalFileModel = new FileModel(testFile);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .when().rename(longName).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
                    .then().usingSite(testSite).usingResource(originalFileModel).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager cannot rename locked file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldNotRenameLockedFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        FileModel originalFileModel = new FileModel(testFile);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().lock().and().assertThat().isLocked()
                .when().rename(testFile.getName() + renamePrefix)
                .assertThat().doesNotExistInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_LOCKED)
                    .then().usingSite(testSite).usingResource(originalFileModel).assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify that non existent user cannot rename a file")
    public void nonExistentUserShouldNotRenameFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel originalFileModel = new FileModel(testFile);
        webDavProtocol.authenticateUser(managerUser)
                .usingSite(testSite)
                .createFile(testFile)
                .assertThat().existsInRepo();

        webDavProtocol.authenticateUser(UserModel.getRandomUserModel())
                .usingResource(testFile)
                .rename(testFile.getName() + renamePrefix)
                .assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED);

        webDavProtocol.authenticateUser(managerUser)
                .usingSite(testSite)
                .usingResource(originalFileModel)
                .assertThat().existsInRepo();
    }
}
