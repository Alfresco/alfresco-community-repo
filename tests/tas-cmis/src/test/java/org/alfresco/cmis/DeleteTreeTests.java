package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/27/2016.
 */
public class DeleteTreeTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FolderModel parentTestFolder, childTestFolder;
    private String content = "content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete parent folder with multiple children in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerDeletesFolderTree() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).refreshResource()
            .deleteFolderTree()
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify exception is thrown when deleting inexistent folder tree in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerDeletesInexistentFolderTree() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).deleteFolderTree().assertThat().doesNotExistInRepo()
            .deleteFolderTree();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify inexistent user is NOT able to delete parent folder with multiple children in DocumentLibrary")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void inexistentUserCannotDeleteFolderTree() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        UserModel inexistentUser = UserModel.getRandomUserModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(inexistentUser)
                .usingResource(parentTestFolder).deleteFolderTree();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete parent folder with allVersions parameter set to true")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerDeletesFolderTreeWithAllVersionsParamTrue() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).deleteFolderTree(true, UnfileObject.DELETE, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Only allVersions=true is supported for delete parent folder tree, allVersions=true is not supported")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class})
    public void deleteFolderTreeWithAllVersionsParamFalseNotSupported() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).deleteFolderTree(false, UnfileObject.DELETE, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
//    @Bug(id="REPO-1108")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete parent folder with unfile parameter set to DELETESINGLEFILED, using checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteManagerDeletesFolderTreeWithDeleteSingleFieldWithCheckoutDoc() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder)
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder)
                .createFile(testFile).and().assertThat().existsInRepo()
                .and().checkOut()
            .when().usingResource(parentTestFolder).deleteFolderTree(true, UnfileObject.DELETESINGLEFILED, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to delete parent folder with unfile parameter set to DELETESINGLEFILED, using add object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerDeletesFolderTreeWithDeleteSingleFieldWithAddDocToFolder1() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder)
            .createFolder(childTestFolder)
            .usingResource(parentTestFolder) 
                .createFile(testFile).and().assertThat().existsInRepo()
                .usingResource(testFile).addDocumentToFolder(childTestFolder, true)
            .then().usingResource(childTestFolder).deleteFolderTree(true, UnfileObject.DELETESINGLEFILED, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(parentTestFolder).assertThat().existsInRepo()
                   .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete parent folder with unfile parameter set to DELETESINGLEFILED, using add object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerDeletesFolderTreeWithDeleteSingleFieldWithAddDocToFolder2() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder)
            .createFolder(childTestFolder)
            .usingResource(parentTestFolder) 
                .createFile(testFile).and().assertThat().existsInRepo()
                .usingResource(testFile).addDocumentToFolder(childTestFolder, true)
            .then().usingResource(childTestFolder).deleteFolderTree(true, UnfileObject.DELETE, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(parentTestFolder).assertThat().existsInRepo()
                   .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Unfile-ing is not supported for delete parent folder tree")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class},
            expectedExceptionsMessageRegExp="Unfiling not supported!*")
    public void deleteFolderTreeWithUnfileNotSupported() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).deleteFolderTree(true, UnfileObject.UNFILE, true)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
//    @Bug(id="REPO-1108")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete parent folder with continueOnFailure parameter set to false")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerDeletesFolderTreeWithContinueOnFailureParamFalse() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder)
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder)
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).deleteFolderTree(true, UnfileObject.DELETE, false)
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to delete parent folder with multiple children created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorDeletesOwnFolderTree() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingSite(testSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .when().usingResource(parentTestFolder).refreshResource()
                        .deleteFolderTree()
                            .and().assertThat().doesNotExistInRepo()
                            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                               .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to delete parent folder with multiple children created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCannotDeleteFolderTreeCreatedByManager() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .when().usingResource(parentTestFolder).refreshResource()
                            .deleteFolderTree().and().assertThat().hasFailedDeletedObject(parentTestFolder.getNodeRef())
                                .and().assertThat().existsInRepo()
                                .then().usingResource(childTestFolder).assertThat().existsInRepo()
                                    .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is not able to delete parent folder with multiple children created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCannotDeleteFolderTreeCreatedByManager() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .when().usingResource(parentTestFolder).refreshResource()
                            .deleteFolderTree().and().assertThat().hasFailedDeletedObject(parentTestFolder.getNodeRef())
                                .and().assertThat().existsInRepo()
                                .then().usingResource(childTestFolder).assertThat().existsInRepo()
                                    .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to delete parent folder with multiple children created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorDeletesOwnFolderTree() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingSite(testSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .when().usingResource(parentTestFolder).refreshResource()
                        .deleteFolderTree()
                            .and().assertThat().doesNotExistInRepo()
                            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                                .usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to delete parent folder with multiple children created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCannotDeleteFolderTreeCreatedByManager() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .when().usingResource(parentTestFolder).refreshResource()
                            .deleteFolderTree().and().assertThat().hasFailedDeletedObject(parentTestFolder.getNodeRef())
                                .and().assertThat().existsInRepo()
                                .then().usingResource(childTestFolder).assertThat().existsInRepo()
                                    .usingResource(testFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to delete parent folder with multiple children in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotDeleteFolderTreeInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser)
            .usingSite(privateSite)
                .createFolder(parentTestFolder).and().assertThat().existsInRepo()
                    .usingResource(parentTestFolder)
                        .createFolder(childTestFolder).and().assertThat().existsInRepo()
                        .createFile(testFile).and().assertThat().existsInRepo()
                    .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .when().usingResource(parentTestFolder)
                            .deleteFolderTree();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that only the parent folder is displayed in trash can after deleting it")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void checkTrashCanAfterDeletingParentFolder() throws Exception
    {
        parentTestFolder = FolderModel.getRandomFolderModel();
        childTestFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(parentTestFolder).and().assertThat().existsInRepo()
            .usingResource(parentTestFolder)
                .createFolder(childTestFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo()
            .when().usingResource(parentTestFolder).refreshResource()
            .deleteFolderTree()
                .and().assertThat().doesNotExistInRepo()
            .then().usingResource(childTestFolder).assertThat().doesNotExistInRepo()
                   .usingResource(testFile).assertThat().doesNotExistInRepo();
        dataUser.assertTrashCanHasContent(parentTestFolder);
        dataUser.assertTrashCanDoesNotHaveContent(childTestFolder, testFile);
    }
}
