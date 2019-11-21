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
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/28/2016.
 */
public class AddObjectToFolderTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FolderModel destinationFolder;
    FileModel sourceFile;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        destinationFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(destinationFolder)
            .assertThat().existsInRepo();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to add document object to folder with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldAddFileToFolder() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
                .then().addDocumentToFolder(destinationFolder, true)
                    .and().assertThat().existsInRepo()
                    .and().assertThat().objectIdIs(sourceFile.getNodeRef());
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to add folder object to folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class,
            expectedExceptionsMessageRegExp="Object is not a document!*")
    public void siteManagerShouldNotAddFolderToFolder() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel folderToAdd = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(folderToAdd).assertThat().existsInRepo()
                .then().addDocumentToFolder(folderToAdd, true);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to add file a document object in more than one folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerShouldNotAddInvalidFileToFolder() throws Exception
    {
        FileModel randomFile = FileModel.getRandomFileModel(FileType.HTML);
        randomFile.setCmisLocation("/" + randomFile.getName() + "/");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .usingResource(randomFile)
                .addDocumentToFolder(destinationFolder, true);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to add folder object to folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerShouldNotAddInvalidFolderToFolder() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel randomFolder = FolderModel.getRandomFolderModel();
        randomFolder.setCmisLocation("/" + randomFolder.getName() + "/");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
                .then().addDocumentToFolder(randomFolder, true);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to add PWC document to folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldAddPWCFileToFolder() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
                .then().checkOut().and().assertThat().documentIsCheckedOut()
                .usingPWCDocument().addDocumentToFolder(destinationFolder, true)
                    .and().assertThat().existsInRepo()
                    .and().assertThat().documentIsCheckedOut();
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to add document object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddDocumentWithVersionsToFolderWithTrueAllVersions() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
                .then().update("first content").update("second content")
                    .and().assertThat().documentHasVersion(1.2)
                .then().addDocumentToFolder(destinationFolder, true)
                    .and().assertThat().existsInRepo()
                        .and().assertThat().documentHasVersion(1.2);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to add document object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class,
            expectedExceptionsMessageRegExp="Only allVersions=true supported!*")
    public void siteManagerCannotAddDocumentToFolderWithFalseAllVersions() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
                .then().update("update content")
                    .and().assertThat().documentHasVersion(1.1)
                .then().addDocumentToFolder(destinationFolder, false);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to add document object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanAddFileToFolderCreatedByHimself() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingSite(testSite)
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().addDocumentToFolder(destinationFolder, true)
                        .and().assertThat().existsInRepo()
                            .and().assertThat().objectIdIs(sourceFile.getNodeRef());
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to add document object created by manager to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanAddFileToFolderCreatedByManager() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .then().addDocumentToFolder(destinationFolder, true)
                            .and().assertThat().existsInRepo();
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to add document object to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanAddFileToFolderCreatedByHimself() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingSite(testSite)
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().addDocumentToFolder(destinationFolder, true)
                        .and().assertThat().existsInRepo()
                            .and().assertThat().objectIdIs(sourceFile.getNodeRef());
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to add document object created by manager to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanAddFileToFolderCreatedByManager() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .then().addDocumentToFolder(destinationFolder, true)
                            .and().assertThat().existsInRepo();
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is able to add document object created by manager to folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCanAddFileToFolderCreatedByManager() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .then().addDocumentToFolder(destinationFolder, true);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to delete parent folder with multiple children in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotDeleteFolderTreeInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        destinationFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser)
            .usingSite(privateSite)
                .createFolder(destinationFolder).assertThat().existsInRepo()
                .createFile(sourceFile).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .then().addDocumentToFolder(destinationFolder, true);
    }
}

