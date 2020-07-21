package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LockFileTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FileModel testFile;
    private String content = "webdav file content";
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteCollaborator, UserRole.SiteConsumer,UserRole.SiteContributor);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that admin user can lock a file created in Guest Home folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void adminCanLockFile() throws Exception
    {
        FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guestHomeFolder)
            .createFile(testFile).then().assertThat().existsInRepo()
                .then().lock().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site manager user can lock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteManagerCanLockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .then().lock().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site collaborator user can lock a file created by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorCanLockFileCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .and().lock().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site collaborator user can lock a file created by himself")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorCanUnlockFileCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().lock().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site contributor user cannot lock a file created by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorCannotLockFileCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .and().lock().assertThat().hasStatus(HttpStatus.FORBIDDEN.value())
                    .assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site contributor user cannot unlock a file created by himself")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorCanLockFileCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site consumer user cannot unlock a file created by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteConsumerCannotLockFileCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .and().lock().assertThat().hasStatus(HttpStatus.FORBIDDEN.value())
                    .assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager can create inexistent file as locked")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerCanCreateLockedInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
            .usingResource(testFile)
                .lock().assertThat().hasStatus(HttpStatus.CREATED.value())
                .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
                    .and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site collaborator can create inexistent file as locked")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteCollaboratorCanCreateLockedInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingSite(testSite)
            .usingResource(testFile)
                .lock().assertThat().hasStatus(HttpStatus.CREATED.value())
                .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
                    .and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site contributor can create inexistent file as locked")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteContributorCanCreateLockedInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingSite(testSite)
            .usingResource(testFile)
                .lock().assertThat().hasStatus(HttpStatus.CREATED.value())
                    .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
                        .and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site consumer cannot can create inexistent file as locked")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteConsumerCannotCreateLockedInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingSite(testSite)
            .usingResource(testFile)
                .lock().assertThat().hasStatus(HttpStatus.FORBIDDEN.value())
                    .and().assertThat().doesNotExistInRepo().and().assertThat().doesNotExistInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that inexistent user cannot lock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void inexistentUserCannotLockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().authenticateUser(UserModel.getRandomUserModel())
                .and().lock().and().assertThat().hasStatus(HttpStatus.UNAUTHORIZED.value());
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that disconected user cannot lock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void disconectedUserCannotLockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().disconnect()
                .and().lock().assertThat().hasStatus(HttpStatus.UNAUTHORIZED.value());
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthorized user cannot lock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void unauthorizedUserCannotLockFile() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().authenticateUser(unauthorized)
                .and().lock().assertThat().hasStatus(HttpStatus.FORBIDDEN.value());
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot lock a folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerCanLockFolder() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).then().assertThat().existsInRepo()
                .lock().and().assertThat().hasStatus(HttpStatus.OK.value())
                    .and().assertThat().isLocked();
    }
}
