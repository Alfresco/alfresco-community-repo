package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
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

public class DeleteFolderTests extends WebDavTest
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
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                description ="Verify that admin user can delete folder from repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void adminShouldDeleteFolder() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(dataUser.getAdminUser())
            .usingResource(guest).createFolder(testFolder)
                .then().delete().and().assertThat().hasStatus(HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                      .assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                description ="Verify that site manager can delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder)
                .then().delete().assertThat().hasStatus(HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site collaborator cannot delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteCollaboratorShouldNotDeleteFolder() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder)
                .then().authenticateUser(collaborator).usingResource(testFolder)
                    .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .and().assertThat().existsInRepo()
                        .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site contributor cannot delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteContributorShouldNotDeleteFolder() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder)
                .then().authenticateUser(contributor).usingResource(testFolder)
                    .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .and().assertThat().existsInRepo()
                        .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site consumer cannot delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteConsumerShouldNotDeleteFolder() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder)
                .then().authenticateUser(consumer).usingResource(testFolder)
                .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager cannot delete a folder twice from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldNotDeleteAFolderTwice() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite).createFolder(testFolder).delete().and().assertThat().doesNotExistInRepo()
                .when().delete().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager cannot delete inexistent folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldNotDeleteInexistentFolder() throws Exception
    {
        webDavProtocol.authenticateUser(managerUser)
            .usingResource(FolderModel.getRandomFolderModel())
                .delete().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a disconected user cannot delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void disconectedUserShouldNotDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().disconnect()
                .then().delete().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                    .then().authenticateUser(managerUser).usingResource(testFolder)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a unauthorized user cannot delete folder from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void unauthorizedUserShouldNotDeleteFolder() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().assertThat().existsInRepo()
            .then().authenticateUser(unauthorized)
                .and().delete().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a inexistent user cannot delete folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void inexistentUserShouldNotDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).and().assertThat().existsInRepo()
            .then().authenticateUser(UserModel.getRandomUserModel())
                .and().delete().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().authenticateUser(managerUser)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that site manager can delete folder with children")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerShouldDeleteFolderWithChildren() throws Exception
    {
        FileModel subFile = FileModel.getRandomFileModel(FileType.MSWORD2007);
        FolderModel subFolder = FolderModel.getRandomFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder)
                .usingResource(testFolder)
                    .createFile(subFile)
                    .createFolder(subFolder)
                .usingResource(testFolder).delete().assertThat().hasStatus(HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND)
                .usingResource(subFile).assertThat().doesNotExistInRepo()
                .usingResource(subFolder).assertThat().doesNotExistInRepo();
    }
}
