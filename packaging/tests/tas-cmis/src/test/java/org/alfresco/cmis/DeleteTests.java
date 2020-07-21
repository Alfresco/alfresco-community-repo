package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/26/2016.
 */
public class DeleteTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite, privateSite;
    FolderModel testFolder;
    FileModel testFile;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete files in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldDeleteDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).and().assertThat().existsInRepo()
                .then().delete()
                    .and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete empty folders in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldDeleteEmptyFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFolder(testFolder).and().assertThat().existsInRepo()
                .then().delete()
                    .and().assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete folders with chidren with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisConstraintException.class, expectedExceptionsMessageRegExp = "^Could not delete folder with at least one child!$")
    public void siteManagerCannotDeleteFolderWithChildren() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFolder(testFolder)
            .usingResource(testFolder).createFile(testFile)
                .usingResource(testFolder).delete();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete file with multiple versions with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteFileWithVersions() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFolder(testFolder)
            .usingResource(testFolder).createFile(testFile)
                .then().update("content 1").assertThat().documentHasVersion(1.1)
                .then().update("content 2").assertThat().documentHasVersion(1.2)
            .then().usingResource(testFile).delete()
            .assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete file which is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisConstraintException.class, expectedExceptionsMessageRegExp = "^Could not delete/cancel checkout on the original checked out document$")
    public void siteManagerCannotDeleteCheckedOutFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).usingResource(testFile).checkOut()
            .assertThat().documentIsCheckedOut()
                .then().delete();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete PWC file version with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeletePWCFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).then().checkOut()
            .assertThat().documentIsCheckedOut()
            .usingPWCDocument().delete()
            .assertThat().doesNotExistInRepo()
            .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete nonexistent file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerCannotDeleteNonexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        testFile.setCmisLocation("/" + testFile.getName() + "/");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .usingResource(testFile).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete nonexistent folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerCannotDeleteNonexistentFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        testFolder.setCmisLocation("/" + testFolder.getName() + "/");
        cmisApi.authenticateUser(siteManager)
            .usingResource(testFolder).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete files created by another users in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldDeleteDocumentCreatedByAnotherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).delete()
                .and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to delete content created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldDeleteContentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .then().usingResource(testFile).delete()
                .and().assertThat().doesNotExistInRepo()
                .then().usingResource(testFolder).delete()
                .and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to delete file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorShouldNotDeleteFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                    .usingResource(testFile).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to delete folder created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorShouldNotDeleteFolderCreatedByOtherUser() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                    .usingResource(testFolder).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to delete content created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldDeleteContentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .then().usingResource(testFile).delete()
                .and().assertThat().doesNotExistInRepo()
                .then().usingResource(testFolder).delete()
                .and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is not able to delete file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorShouldNotDeleteFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .usingResource(testFile).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is not able to delete folder created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorShouldNotDeleteFolderCreatedByOtherUser() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .usingResource(testFolder).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to delete file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerShouldNotDeleteFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .usingResource(testFile).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to delete folder created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerShouldNotDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .usingResource(testFolder).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin is able to delete files created by another users in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminShouldDeleteDocumentCreatedByAnotherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(dataUser.getAdminUser())
                .usingResource(testFile).delete()
                .and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to delete file created inside a private site with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserShouldNotDeleteFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager)
                .usingSite(privateSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                    .usingResource(testFile).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to delete folder created inside a private site with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserShouldNotDeleteFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(siteManager)
                .usingSite(privateSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                    .usingResource(testFolder).delete();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to delete content created inside a private site with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeleteCheckedOutFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
                .createFile(testFile).then().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .when().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).delete();
    }
}
