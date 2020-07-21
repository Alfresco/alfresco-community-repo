package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
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

public class RenameFolderTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FolderModel testFolder;
    private String renamePrefix = "-edit";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, description = "Verify that admin user can rename folder")
    public void adminShouldRenameFolder() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        webDavProtocol.authenticateUser(dataUser.getAdminUser())
            .usingResource(guest).createFolder(testFolder).and().assertThat().existsInRepo()
            .when().rename(testFolder.getName() + renamePrefix).assertThat().existsInRepo()
                .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, description = "Verify that site manager can rename folder")
    public void siteManagerShouldRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().rename(testFolder.getName() + renamePrefix).assertThat().existsInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, description = "Verify that user with contributor role cannot rename folder")
    public void siteContributorShouldNotRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(contributor).usingResource(testFolder)
                    .rename(testFolder.getName() + renamePrefix).assertThat().doesNotExistInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, description = "Verify that user with collaborator role can rename folder")
    public void siteCollaboratorShouldRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(collaborator)
                    .usingResource(testFolder)
                        .rename(testFolder.getName() + renamePrefix).assertThat().existsInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                        .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
        description = "Verify that user with consumer role cannot rename folder")
    public void siteConsumerShouldNotRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);

        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(consumer)
                    .usingResource(testFolder)
                        .rename(testFolder.getName() + renamePrefix).assertThat().doesNotExistInRepo()
                        .and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                        .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that disconected user cannot rename a folder")
    public void disconectedUserShouldNotRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
            .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().disconnect()
                    .usingResource(testFolder).rename(testFolder.getName() + renamePrefix).assertThat().doesNotExistInRepo()
                    .and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED);

        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).usingResource(originalFolderModel).assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that unauthorized user cannot rename a folder")
    public void unauthorizedUserShouldNotRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);

        webDavProtocol.authenticateUser(dataUser.getAdminUser())
                .usingRoot()
                .createFolder(testFolder)
                .assertThat().existsInRepo();

        webDavProtocol.authenticateUser(dataUser.createRandomTestUser())
                .usingResource(testFolder)
                .rename(testFolder.getName() + renamePrefix)
                .assertThat().doesNotExistInRepo()
                .assertThat().hasStatus(HttpStatus.SC_FORBIDDEN);

        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingRoot()
                .usingResource(originalFolderModel).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that inexistent user cannot rename a folder")
    public void inexistentUserShouldNotRenameFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
            .createFolder(testFolder).and().assertThat().existsInRepo();

        webDavProtocol.authenticateUser(UserModel.getRandomUserModel())
                    .usingResource(testFolder).rename(testFolder.getName() + renamePrefix)
                    .assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED);

        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).usingResource(originalFolderModel).assertThat().existsInRepo();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
                    description ="Verify that site manager can rename folder with 200 characters")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldRenameFolderWithLongName() throws Exception
    {
        String longName = RandomStringUtils.randomAlphabetic(200);
        testFolder = FolderModel.getRandomFolderModel();
        FolderModel originalFolderModel = new FolderModel(testFolder);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().rename(longName)
                .and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
                    .then().usingSite(testSite).usingResource(originalFolderModel).assertThat().doesNotExistInRepo();
    }
}
