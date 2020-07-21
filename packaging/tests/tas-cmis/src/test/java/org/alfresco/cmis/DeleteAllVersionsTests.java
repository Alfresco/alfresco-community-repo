package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
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
public class DeleteAllVersionsTests extends CmisTest
{
    UserModel testUser;
    UserModel consumerUser;
    UserModel collaboratorUser;
    UserModel contributorUser;
    UserModel unauthorizedUser;
    SiteModel testSite;
    FileModel testFile;
    FolderModel testFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        consumerUser = dataUser.createRandomTestUser();
        collaboratorUser = dataUser.createRandomTestUser();
        contributorUser = dataUser.createRandomTestUser();
        unauthorizedUser = dataUser.createRandomTestUser();

        testSite = dataSite.usingUser(testUser).createPublicRandomSite();

        dataUser.addUserToSite(consumerUser, testSite, UserRole.SiteConsumer);
        dataUser.addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        dataUser.addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete all document versions in DocumentLibrary with CMIS")
    @Test(groups = {TestGroup.SANITY, TestGroup.CMIS })
    public void siteManagerDeletesAllDocumentVersions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().update(testFile.getName())
                .then().deleteAllVersions(true).and().assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete only latest document version in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerDeletesLatestDocumentVersion() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().update(testFile.getName())
                .then().deleteAllVersions(false).and().assertThat().documentHasVersion(1.0);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete all versions of inexistent file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerCannotDeleteAllVersionsOfInexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        testFile.setCmisLocation("/fake-folder/inexistentFile.txt");
        cmisApi.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).deleteAllVersions(true);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete PWC file version of a file with multiple versions with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeletePWCFileWithDeleteAllVersionsTrue() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .usingResource(testFile).update("content 1")
                    .assertThat().documentHasVersion(1.1)
                .usingResource(testFile).update("content 2")
                    .assertThat().documentHasVersion(1.2)
                .usingResource(testFile).checkOut()
                    .assertThat().documentIsCheckedOut()
                .usingPWCDocument().deleteAllVersions(true)
                    .assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo().and().assertThat().documentHasVersion(1.2);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete PWC file version of a file with multiple versions set to false with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeletePWCFileWithDeleteAllVersionsFalse() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .then().update("content 1")
                    .assertThat().documentHasVersion(1.1)
                .then().update("content 2")
                    .assertThat().documentHasVersion(1.2)
                .then().checkOut().refreshResource()
                    .assertThat().documentIsCheckedOut()
                .usingPWCDocument().deleteAllVersions(false)
                .assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo().and().assertThat().documentHasVersion(1.2);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete file original multiple version which is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisConstraintException.class, expectedExceptionsMessageRegExp = "^Could not delete/cancel checkout on the original checked out document$")
    public void siteManagerCannotDeleteOriginalFileMultipleVersionWhenCheckedout() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .then().update("content 1")
                    .assertThat().documentHasVersion(1.1)
                .then().update("content 2")
                    .assertThat().documentHasVersion(1.2)
                .refreshResource().then().checkOut()
                    .assertThat().documentIsCheckedOut();
        cmisApi.usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is NOT able to delete a checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeletePWCDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).then().update("content 1")
                .and().checkOut().assertThat().documentIsCheckedOut()
                .when().authenticateUser(unauthorizedUser)
                .usingResource(testFile).usingPWCDocument().deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager can delete object Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile).usingResource(testFile).update("content 1")
                .usingResource(testFile).deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager can delete object Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteFolder() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder).usingResource(testFolder)
                .deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor can delete object Document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanDeleteDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(contributorUser).usingSite(testSite).createFile(testFile).usingResource(testFile).update("content 1")
                .deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor cannot delete object Document created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorCannotDeleteDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).then().update("content 1")
                .when().authenticateUser(contributorUser).usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor can delete object Folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanDeleteFolderCreatedBySelf() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(contributorUser).usingSite(testSite).createFolder(testFolder).usingResource(testFolder)
                .deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor cannot delete object Folder created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorCannotDeleteFolderCreatedByManager() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .when().authenticateUser(contributorUser)
                .usingResource(testFolder).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator can delete object Document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanDeleteDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(collaboratorUser).usingSite(testSite).createFile(testFile).usingResource(testFile).update("content 1")
                .deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator cannot delete object Document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorCannotDeleteDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).then().update("content 1")
                .when().authenticateUser(collaboratorUser)
                .usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator can delete object Folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanDeleteFolderCreatedBySelf() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(collaboratorUser).usingSite(testSite).createFolder(testFolder).usingResource(testFolder)
                .deleteAllVersions(true)
                .assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator cannot delete object Folder created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorCannotDeleteFolderCreatedByManager() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .then().authenticateUser(collaboratorUser)
                .usingResource(testFolder).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer cannot delete object Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotDeleteDocumentCreated() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).update("content 1")
                .when().authenticateUser(consumerUser)
                .usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer cannot delete object Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotDeleteFolderCreated() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .then().authenticateUser(consumerUser)
                .usingResource(testFolder).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot delete Document from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeleteDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
                .createFile(testFile).then().update("content 1")
                .when().authenticateUser(unauthorizedUser)
                .usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot delete Folder from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeleteFolderFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(privateSite).createFolder(testFolder);
        cmisApi.authenticateUser(unauthorizedUser).usingResource(testFolder).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot delete Document from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeleteDocumentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(moderatedSite).createFile(testFile).usingResource(testFile).update("content 1");
        cmisApi.authenticateUser(unauthorizedUser).usingResource(testFile).deleteAllVersions(true);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot delete Folder from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotDeleteFolderFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFolder = new FolderModel(RandomData.getRandomName("Folder"));
        cmisApi.authenticateUser(testUser).usingSite(moderatedSite).createFolder(testFolder);
        cmisApi.authenticateUser(unauthorizedUser).usingResource(testFolder).deleteAllVersions(true);
    }
}
