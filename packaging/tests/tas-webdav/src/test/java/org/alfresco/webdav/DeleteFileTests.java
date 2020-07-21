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

public class DeleteFileTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FileModel testFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                description ="Verify that admin user can delete content from repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void adminShouldDeleteContent() throws Exception
    {
        FolderModel guest = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(dataUser.getAdminUser())
            .usingResource(guest)
            .createFile(testFile)
                .then().delete().and().assertThat().hasStatus(HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                      .assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                description ="Verify that site manager can delete content from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldDeleteContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .then().delete().assertThat().hasStatus(HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav().and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site collaborator cannot delete content from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteCollaboratorShouldNotDeleteContent() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .then().authenticateUser(collaborator).usingResource(testFile)
                    .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .and().assertThat().existsInRepo()
                        .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site contributor cannot delete content from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteContributorShouldNotDeleteContent() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .then().authenticateUser(contributor).usingResource(testFile)
                    .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .and().assertThat().existsInRepo()
                        .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site consumer cannot delete content from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteConsumerShouldNotDeleteContent() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile)
                .then().authenticateUser(consumer).usingResource(testFile)
                .delete().and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager cannot delete a file twice from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldNotDeleteAFileTwice() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.XML);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite).createFile(testFile).delete().and().assertThat().doesNotExistInRepo()
                .when().delete().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.SANITY, 
                    description ="Verify that site manager cannot delete inexistent file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldNotDeleteInexistentContent() throws Exception
    {
        webDavProtocol.authenticateUser(managerUser)
            .usingResource(FileModel.getRandomFileModel(FileType.MSEXCEL))
                .delete().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a disconected user cannot delete file from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void disconectedUserShouldNotDeleteContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().disconnect()
                .then().delete().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                    .then().authenticateUser(managerUser).usingResource(testFile)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a unauthorized user cannot delete file from site")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void unauthorizedUserShouldNotDeleteContent() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(unauthorized)
                .and().delete().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that a inexistent user cannot delete file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void inexistentUserShouldNotDeleteContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(UserModel.getRandomUserModel())
                .and().delete().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
                .then().authenticateUser(managerUser)
                    .assertThat().existsInRepo().and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={ TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType= ExecutionType.REGRESSION, 
                description ="Verify that site manager cannot delete locked file")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    public void siteManagerCannotNotDeleteLockedFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().lock().assertThat().isLocked()
                    .then().delete().assertThat().hasStatus(HttpStatus.SC_LOCKED)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav();
    }
}
