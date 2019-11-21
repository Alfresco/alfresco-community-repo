package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateFolderTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FolderModel testFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that admin user can create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void adminShouldCreateFolder() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guest)
            .createFolder(testFolder).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that site manager can create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that user with contributor role can create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteContributorShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        webDavProtocol.authenticateUser(contributor).usingSite(testSite)
            .createFolder(testFolder).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that user with collaborator role can create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteCollaboraShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        webDavProtocol.authenticateUser(collaborator).usingSite(testSite)
            .createFolder(testFolder).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, description ="Verify that user with consumer role cannot create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteConsumerShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        webDavProtocol.authenticateUser(consumer).usingSite(testSite)
            .createFolder(testFolder).assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .then().assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that user with manager role can create folder with spaces in name")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManageShouldCreateFolderWithSpacesInName() throws Exception
    {
        testFolder = new FolderModel("test folder " + RandomData.getRandomFolder());
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
             .createFolder(testFolder).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that user with manager role cannot create folder with special characters in name")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE }, expectedExceptions=IllegalArgumentException.class)
    public void siteManageShouldNotCreateFolderWithSpecialChars() throws Exception
    {
        testFolder = new FolderModel("<>.|?#()[]{}");
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
             .createFolder(testFolder);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot create folder twice in the same location")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldNotCreateFolderTwice() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                .when().createFolder(testFolder)
                    .assertThat().hasStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, description ="Verify that inexistent user cannot create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void inexistentUserShouldNotCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(UserModel.getRandomUserModel()).usingResource(FolderModel.getSharedFolderModel())
            .createFolder(testFolder).and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().usingAdmin()
                    .assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, description ="Verify that unauthorized user cannot create folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void unauthorizedUserShouldNotCreateFolder() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(unauthorized).usingRoot()
            .createFolder(testFolder).and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .then().assertThat().doesNotExistInRepo()
                           .assertThat().doesNotExistInWebdav();
    }
}
