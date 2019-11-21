package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFolderParentTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FolderModel testFolder;
    FolderModel parentFolder;
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
        parentFolder = FolderModel.getRandomFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Verify folder parent")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyFolderParent() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder).createFolder(FolderModel.getRandomFolderModel())
                .then().assertThat().folderHasParent(testFolder);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, description = "Verify folder parent that was previously deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void verifyFolderParentThatWasDeleted() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(parentFolder).createFolder(testFolder)
                .then().assertThat().folderHasParent(parentFolder)
                .and().usingResource(parentFolder).deleteFolderTree()
                .then().usingResource(testFolder).assertThat().folderHasParent(parentFolder);
    }


    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorVerifyFolderParentCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder).createFolder(FolderModel.getRandomFolderModel())
                .then().assertThat().folderHasParent(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorVerifyFolderParentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(parentFolder).createFolder(testFolder);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFolder)
                .assertThat().folderHasParent(parentFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorVerifyFolderParentCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(testFolder).createFolder(FolderModel.getRandomFolderModel())
                .then().assertThat().folderHasParent(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorVerifyFolderParentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(parentFolder).createFolder(testFolder);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFolder)
                .assertThat().folderHasParent(parentFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer can get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerVerifyFolderParent() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(parentFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(parentFolder).createFolder(testFolder);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFolder)
                .assertThat().folderHasParent(parentFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member for a private site is not able to get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberGetFolderParentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFolder(parentFolder)
            .and().assertThat().existsInRepo()
            .and().usingResource(parentFolder).createFolder(testFolder);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().folderHasParent(parentFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member for a moderated site is not able to get folder parent")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberGetFolderParentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite).createFolder(parentFolder)
                .and().assertThat().existsInRepo()
                .and().usingResource(parentFolder).createFolder(testFolder);

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().folderHasParent(parentFolder);
    }
}
