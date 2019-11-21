package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/28/2016.
 */
public class MoveTests extends CmisTest
{
    UserModel unauthorizedUser;
    UserModel siteManager;
    UserModel contributorUser;
    UserModel collaboratorUser;
    UserModel consumerUser;
    SiteModel publicSite;
    FileModel sourceFile;
    FolderModel targetFolder, sourceFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        unauthorizedUser = dataUser.createRandomTestUser();
        siteManager = dataUser.createRandomTestUser();
        contributorUser = dataUser.createRandomTestUser();
        collaboratorUser = dataUser.createRandomTestUser();
        consumerUser = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();

        dataUser.addUserToSite(consumerUser, publicSite, UserRole.SiteConsumer);
        dataUser.addUserToSite(collaboratorUser, publicSite, UserRole.SiteCollaborator);
        dataUser.addUserToSite(contributorUser, publicSite, UserRole.SiteContributor);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to move a file to an existent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerMovesFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().moveTo(targetFolder).and().assertThat().existsInRepo()
                    .usingResource(sourceFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to move file to a nonexistent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerMovesFileToNonExistentTarget() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .createFolder(targetFolder).and().delete()
                .then().moveTo(targetFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to move a nonexistent file in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerMovesNonExistentSourceFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().delete()
                .then().moveTo(targetFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to move file that has multiple versions with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldMoveFileWithMultipleVersions() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().update("first content")
                    .assertThat().documentHasVersion(1.1)
                       .update("second content")
                            .assertThat().documentHasVersion(1.2)
                .then().moveTo(targetFolder).and().assertThat().existsInRepo()
                    .and().usingVersion().assertHasVersions(1.0, 1.1, 1.2)
                        .usingResource(sourceFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to move folder structure to an existent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerMovesFolderStructure() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        FolderModel sourceParentFolder = FolderModel.getRandomFolderModel();
        FolderModel subFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFolder(sourceParentFolder).and().assertThat().existsInRepo()
                .then().usingResource(sourceParentFolder)
                    .createFile(sourceFile).and().assertThat().existsInRepo()
                    .createFolder(subFolder).and().assertThat().existsInRepo()
                .when().usingResource(sourceParentFolder)
                    .moveTo(targetFolder).and().assertThat().existsInRepo()
                         .and().assertThat().hasChildren(sourceFile, subFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify inexistent is not able to move file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void inexistentUserCannotMoveFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().authenticateUser(UserModel.getRandomUserModel())
                .moveTo(targetFolder).and().assertThat().existsInRepo()
                    .usingResource(sourceFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to move checked out file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions=CmisUpdateConflictException.class)
    public void siteManagerShouldNotMoveCheckedOutFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .and().checkOut().assertThat().documentIsCheckedOut()
                .then().usingResource(sourceFile)
                    .moveTo(targetFolder).and().assertThat().existsInRepo()
                       .and().assertThat().documentIsCheckedOut();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to move folder with checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteManagerShouldMoveFolderWithCheckedOutFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        sourceFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFolder(sourceFolder).and().assertThat().existsInRepo()
                .then().usingResource(sourceFolder)
                    .createFile(sourceFile, VersioningState.CHECKEDOUT)
                    .and().assertThat().existsInRepo()
                          .assertThat().documentIsCheckedOut()
                .then().usingResource(sourceFolder)
                    .moveTo(targetFolder).and().assertThat().existsInRepo()
                        .and().assertThat().hasFiles(sourceFile);
        FileModel checkedOutDoc = cmisApi.getFiles().get(0);
        cmisApi.usingResource(checkedOutDoc).assertThat().documentIsCheckedOut();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify unauthorized user is no able to move a file")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserCannotMovesFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().authenticateUser(unauthorizedUser)
                    .then().moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Manager verify PWC document object cannot be moved")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisUpdateConflictException.class)
    public void managerCannotMovePWCDocumentObject() throws Exception
    {
        sourceFile = dataContent.usingUser(siteManager).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        FileModel pwcFile = cmisApi.authenticateUser(siteManager).usingResource(sourceFile).checkOut().withCMISUtil().getPWCFileModel();
        cmisApi.usingResource(pwcFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can move Document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorCanMoveDocumentCreatedBySelf() throws Exception
    {
        sourceFile = dataContent.usingUser(contributorUser).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(contributorUser).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFile).moveTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFile)
                .usingSite(publicSite).assertThat().doesNotHaveFile(sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor cannot move Document created by Manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorCannotMoveDocumentCreatedByManager() throws Exception
    {
        sourceFile = dataContent.usingUser(siteManager).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can move Folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorCanMoveFolderCreatedBySelf() throws Exception
    {
        sourceFolder = dataContent.usingUser(contributorUser).usingSite(publicSite).createFolder();
        targetFolder = dataContent.usingUser(contributorUser).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFolder).moveTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFolder)
                .usingSite(publicSite).assertThat().doesNotHaveFolder(sourceFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor cannot move Folder created by Manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void contributorCannotMoveFolderCreatedByManager() throws Exception
    {
        sourceFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFolder).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can move Document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorCanMoveDocumentCreatedBySelf() throws Exception
    {
        sourceFile = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFile).moveTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFile)
                .usingSite(publicSite).assertThat().doesNotHaveFile(sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator cannot move Document created by Manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorCannotMoveDocumentCreatedByManager() throws Exception
    {
        sourceFile = dataContent.usingUser(siteManager).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can move Folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorCanMoveFolderCreatedBySelf() throws Exception
    {
        sourceFolder = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createFolder();
        targetFolder = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFolder).moveTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFolder)
                .usingSite(publicSite).assertThat().doesNotHaveFolder(sourceFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator cannot move Folder created by Manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void collaboratorCannotMoveFolderCreatedByManager() throws Exception
    {
        sourceFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFolder).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot move Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotMoveDocument() throws Exception
    {
        sourceFile = dataContent.usingUser(siteManager).usingSite(publicSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(consumerUser).usingResource(sourceFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot move Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotMoveFolder() throws Exception
    {
        sourceFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        targetFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        cmisApi.authenticateUser(consumerUser).usingResource(sourceFolder).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot move Document from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotMoveDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        sourceFile = dataContent.usingUser(siteManager).usingSite(privateSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(privateSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot move Folder from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotMoveFolderFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        sourceFolder = dataContent.usingUser(siteManager).usingSite(privateSite).createFolder();
        targetFolder = dataContent.usingUser(siteManager).usingSite(privateSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFolder).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot move Document from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotMoveDocumentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(siteManager).createModeratedRandomSite();
        sourceFile = dataContent.usingUser(siteManager).usingSite(moderatedSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(siteManager).usingSite(moderatedSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFile).moveTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot move Folder from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotMoveFolderFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(siteManager).createModeratedRandomSite();
        sourceFolder = dataContent.usingUser(siteManager).usingSite(moderatedSite).createFolder();
        targetFolder = dataContent.usingUser(siteManager).usingSite(moderatedSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFolder).moveTo(targetFolder);
    }
}
