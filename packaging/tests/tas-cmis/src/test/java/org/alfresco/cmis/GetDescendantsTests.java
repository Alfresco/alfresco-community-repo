package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetDescendantsTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FileModel fileModel;
    FolderModel testFolder;
    FolderModel folderModel;
    DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteCollaborator, UserRole.SiteContributor, UserRole.SiteConsumer);
    }

    @BeforeMethod(alwaysRun = true)
    public void generateRandomContent()
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        fileModel = FileModel.getRandomFileModel(FileType.MSPOWERPOINT);
        testFolder = FolderModel.getRandomFolderModel();
        folderModel = FolderModel.getRandomFolderModel();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can get descendants for valid parent folder with at least 3 children and depth set to >=1")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void getDescendantsForValidParentFolderWithAtLeast3ChildrenAndDepthGreaterThan1() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder).createFile(testFile)
                .and().createFile(fileModel)
                .and().createFolder(folderModel)
                .then().usingResource(testFolder).assertThat().hasDescendants(1, folderModel, fileModel, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can get descendants for valid parent folder with at least 3 children and depth set to -1")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void getDescendantsForValidParentFolderWithAtLeast3ChildrenAndDepthSetToMinus1() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder).createFile(testFile)
                .then().createFile(fileModel)
                .and().createFolder(folderModel)
                .then().usingResource(testFolder).assertThat().hasDescendants(-1, folderModel, fileModel, testFile);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager CANNOT get descendants for valid parent folder with at least 2 children and depth set to 0")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class})
    public void getDescendantsForValidParentFolderWithAtLeast2ChildrenAndDepthSetTo0() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().usingResource(testFolder).createFile(testFile)
                .and().createFolder(folderModel)
                .then().usingResource(testFolder).assertThat().hasDescendants(0, folderModel, testFile);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager CANNOT get descendants for valid parent folder with at least 2 children and depth set to -2")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class})
    public void getDescendantsForValidParentFolderWithAtLeast2ChildrenAndDepthSetToMinus2() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().usingResource(testFolder).createFile(testFile)
                .and().createFolder(folderModel)
                .then().usingResource(testFolder).assertThat().hasDescendants(-2, folderModel, testFile);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager CANNOT get descendants for parent folder with at least 2 children that was previously deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void getDescendantsForDeletedParentFolderWithAtLeast2Children() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().usingResource(testFolder).createFile(testFile)
                .and().createFolder(folderModel)
                .and().usingResource(testFolder).deleteFolderTree()
                .then().usingResource(testFolder)
                    .assertThat().hasDescendants(1, folderModel, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get descendants for parent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetDescendantsForValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile)
                .usingResource(testFolder).assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get descendants for parent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetDescendantsForValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFolder)
                .assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get descendants for parent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetDescendantsForValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile)
                .usingResource(testFolder).assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get descendants for parent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetDescendantsForValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFolder)
                .assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer can get descendants for parent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerCanGetDescendantsForValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFolder)
                .assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get descendants for a folder from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetDescendantsAFolderFromAPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasDescendants(-1, testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get descendants for a folder from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetDescendantsAFolderFromAModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasDescendants(-1, testFile);
    }
}
