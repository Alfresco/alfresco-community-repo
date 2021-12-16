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

public class UnlockFileTests extends WebDavTest
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
            description ="Verify that admin user can unlock a file created in Guest Home folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void adminCanUnlockFile() throws Exception
    {
        FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guestHomeFolder)
            .createFile(testFile).then().assertThat().existsInRepo()
                .then().lock().assertThat().isLocked()
                    .and().unlock().and().assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site manager user can unlock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteManagerCanUnlockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .then().lock().assertThat().isLocked()
                    .and().unlock().assertThat().hasStatus(HttpStatus.NO_CONTENT.value())
                    .and().assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site collaborator user cannot unlock a file locked by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorCannotUnlockFileLockedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .and().unlock().and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site collaborator user can unlock a file locked by himself")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorCanUnlockFileLockedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .and().lock().assertThat().isLocked()
                    .and().unlock().and().assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site contributor user cannot unlock a file locked by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorCannotUnlockFileLockedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                    .unlock().and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site contributor user cannot unlock a file created by himself")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorCanUnlockFileCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                    .unlock().and().assertThat().hasStatus(HttpStatus.NO_CONTENT.value()).and().assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site consumer user cannot unlock a file locked by manager")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteConsumerCannotUnlockFileLockedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .unlock().and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager user cannot unlock inexistent file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerCannotUnlockInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser)
            .usingResource(FileModel.getRandomFileModel(FileType.MSEXCEL2007))
                .unlock().assertThat().hasStatus(HttpStatus.BAD_REQUEST.value());
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that inexistent user cannot unlock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void inexistentUserCannotUnlockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                    .then().authenticateUser(UserModel.getRandomUserModel())
                        .unlock().and().assertThat().hasStatus(HttpStatus.UNAUTHORIZED.value())
                    .and().authenticateUser(managerUser).assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that disconected user cannot unlock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void disconectedUserCannotUnlockFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                    .then().disconnect()
                        .unlock().and().assertThat().hasStatus(HttpStatus.UNAUTHORIZED.value())
                    .and().authenticateUser(managerUser).assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthorized user cannot unlock a file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void unauthorizedUserCannotUnlockFile() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .and().lock().assertThat().isLocked()
                    .then().authenticateUser(unauthorized)
                        .unlock().and().assertThat().hasStatus(HttpStatus.PRECONDITION_FAILED.value())
                    .and().assertThat().isLocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot unlock a file that is not locked")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void unlockFileThatIsNotLocked() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).then().assertThat().existsInRepo()
                .unlock().and().assertThat().hasStatus(HttpStatus.BAD_REQUEST.value())
                    .and().assertThat().isUnlocked();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot unlock a folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerCanUnlockFolder() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).then().assertThat().existsInRepo()
                .lock().assertThat().isLocked()
                .then().unlock()
                    .and().assertThat().hasStatus(HttpStatus.NO_CONTENT.value()).and().assertThat().isUnlocked();
    }

    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY,
            description ="Checks no existent file is not locked (and status 404)")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void checkLockStatusForNonExistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).
                usingResource(testFile).
                assertThat().isUnlocked().assertThat().hasStatus(HttpStatus.NOT_FOUND.value());
    }
}
