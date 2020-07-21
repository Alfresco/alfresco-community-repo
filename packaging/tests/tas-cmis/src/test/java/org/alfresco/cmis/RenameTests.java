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
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RenameTests extends CmisTest
{
    SiteModel testSite;
    UserModel managerUser, inexistentUser, nonInvitedUser;
    FileModel testFile;
    FolderModel testFolder;
    private DataUser.ListUserWithRoles usersWithRoles;
    private String prefix = "-edit";
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        nonInvitedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        inexistentUser = new UserModel("inexistent", "inexistent");
        usersWithRoles = dataUser.usingUser(managerUser)
                             .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to rename document and folder created by himself")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanRenameContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().usingResource(testFile)
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFile).assertThat().doesNotExistInRepo()
                .then().usingResource(testFolder)
                    .rename(testFolder.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFolder).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user is able to rename document and folder created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminCanRenameContentFromPublicSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().authenticateUser(dataUser.getAdminUser())
                    .usingResource(testFile)
                        .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                            .when().usingResource(testFile).assertThat().doesNotExistInRepo()
                    .usingResource(testFolder)
                        .rename(testFolder.getName() + prefix).assertThat().existsInRepo()
                            .when().usingResource(testFolder).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename inexistent document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotRenameInexistentDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        testFile.setCmisLocation("/fake-folder/test.txt");
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .then().usingResource(testFile)
                .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                    .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename inexistent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotRenameInexistentFolder() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder.setCmisLocation("/" + folder.getName() + "/");
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .then().usingResource(folder)
                .rename(prefix).assertThat().existsInRepo()
                    .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename document with invalid symbols")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotRenameDocumentInvalidSymbols() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().usingResource(testFile)
                    .rename("/.:?|\\`\\.txt").assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename folder with invalid symbols")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotRenameFolderInvalidSymbols() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().usingResource(testFolder)
                    .rename("/.:?|\\`\\.txt").assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename document with empty string")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="New name must not be empty!*")
    public void siteManagerCannotRenameDocumentEmptyName() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().usingResource(testFile)
                    .rename("").assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename folder with empty name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="New name must not be empty!*")
    public void siteManagerCannotRenameFolderEmptyName() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().usingResource(testFolder)
                    .rename("").assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUpdateConflictException.class)
    public void siteManagerCannotRenameCheckedOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                    .then().usingResource(testFile)
                        .rename(testFile.getName() + prefix).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to rename pwc document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRenamePwcDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        FileModel renamedPwc = new FileModel("pwc" + prefix + " (Working Copy)");
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                    .then().usingResource(testFile)
                        .usingPWCDocument().assertThat().existsInRepo()
                        .rename("pwc" + prefix);
        renamedPwc.setCmisLocation(testFile.getCmisLocation().replace(testFile.getName(), renamedPwc.getName()));
        cmisApi.usingResource(renamedPwc).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to rename document with same name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRenameDocumentWithSameName() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().usingResource(testFile)
                    .rename(testFile.getName()).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to rename document with multiple versions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRenameDocumentWithMultipleVersions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().update("first content").assertThat().documentHasVersion(1.1)
                .then().update("second content").assertThat().documentHasVersion(1.2)
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .and().assertThat().documentHasVersion(1.2);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to rename document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteCollaboratorCanRenameDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to rename folder created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteCollaboratorCanRenameFolderCreatedByManager() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .rename(testFolder.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFolder).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to rename folder created by collaborator")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRenameFolderCreatedByCollaborator() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().authenticateUser(managerUser)
                    .rename(testFolder.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFolder).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to rename document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteCollaboratorCanRenameDocumentCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to rename document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void siteContributorCannotRenameDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to rename document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteContributorCanRenameDocumentCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo()
                        .when().usingResource(testFile).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to rename document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void siteConsumerCannotRenameDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to rename document in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void siteConsumerCannotRenameDocumentInPublicSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to rename document in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotRenameDocumentInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(privateSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(nonInvitedUser)
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to rename document in moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotRenameDocumentInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createModeratedRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(moderatedSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(nonInvitedUser)
                    .rename(testFile.getName() + prefix).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to rename document with same name as another file in same location")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisContentAlreadyExistsException.class,
            expectedExceptionsMessageRegExp="An object with this name already exists.*")
    public void siteManagerCannotRenameDocumentWithSameNameFromOtherFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        FileModel secondFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
            .createFile(secondFile)
                .then().usingResource(testFile)
                    .rename(secondFile.getName()).assertThat().existsInRepo();
    }
}
