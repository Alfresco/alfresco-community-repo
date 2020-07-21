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

public class GetFolderTreeTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FolderModel testFolder;
    FolderModel folderModel1;
    FolderModel folderModel11;
    FolderModel folderModel12;
    FolderModel folderModel2;
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
        testFolder = FolderModel.getRandomFolderModel();
        folderModel1 = FolderModel.getRandomFolderModel();
        folderModel11 = FolderModel.getRandomFolderModel();
        folderModel12 = FolderModel.getRandomFolderModel();
        folderModel2 = FolderModel.getRandomFolderModel();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can get folder tree for valid parent folder with at least 2 children" +
                    " folders and depth set to 1")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void getFolderTreeForValidParentFolderWithAtLeast2ChildrenFoldersAndDepthGreaterThan1() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .and().createFolder(folderModel1)
                .and().createFolder(folderModel2)
                .then().usingResource(testFolder).assertThat().hasFolderTree(1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can get folder tree for valid parent folder with at least 2 children" +
                    " folders and depth set to -1")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void getFolderTreeForValidParentFolderWithAtLeast2ChildrenFoldersAndDepthSetToMinus1() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .and().createFolder(folderModel1)
                .and().usingResource(folderModel1).createFolder(folderModel2)
                .then().usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can NOT get folder tree for valid parent folder with at least 2 children" +
                    " folders and depth set to 0")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class})
    public void getFolderTreeForValidParentFolderWithAtLeast2ChildrenFoldersAndDepthSetTo0() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .and().createFolder(folderModel11)
                .and().createFolder(folderModel12)
                .and().usingResource(folderModel11).createFolder(folderModel2)
                .then().usingResource(testFolder)
                    .assertThat().hasFolderTree(0, folderModel11);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can NOT get folder tree for valid parent folder with at least 2 children" +
                    " folders and depth set to -2")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisInvalidArgumentException.class})
    public void getFolderTreeForValidParentFolderWithAtLeast2ChildrenFoldersAndDepthSetToMinus2() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .and().createFolder(folderModel11)
                .and().createFolder(folderModel12)
                .and().usingResource(folderModel11).createFolder(folderModel2)
                .then().usingResource(testFolder)
                    .assertThat().hasFolderTree(-2, folderModel11);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can NOT get folder tree for parent folder with children" +
                    " that was previously deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void getFolderTreeForDeletedParentFolderWithChildren() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .and().createFolder(folderModel11)
                .and().createFolder(folderModel12)
                .and().usingResource(folderModel11).createFolder(folderModel2)
                .and().usingResource(testFolder).deleteFolderTree()
                .then().usingResource(testFolder)
                    .assertThat().hasFolderTree(1, folderModel11);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetFolderTreeForValidParentFolder() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2)
                .usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetFolderTreeForValidParentFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetFolderTreeForValidParentFolder() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2)
                .usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetFolderTreeForValidParentFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer can get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerCanGetFolderTreeForValidParentFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFolder).assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member of a private site cannot get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetFolderTreeForAFolderFromAPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member of a moderated site cannot get folder tree for valid parent folder with at least 2 children folders")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetFolderTreeForAFolderFromAModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite)
                .createFolder(testFolder)
                .usingResource(testFolder).createFolder(folderModel1).and().createFolder(folderModel2);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasFolderTree(-1, folderModel1, folderModel2);
    }
}
