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
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetChildrenTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FileModel secondFile;
    FolderModel testFolder;
    FolderModel subFolder;
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
        secondFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        testFolder = FolderModel.getRandomFolderModel();
        subFolder = FolderModel.getRandomFolderModel();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, description = "Get children from valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void getChildrenFromValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
            .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder)
            .then().usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                .assertThat().hasFiles(testFile, secondFile)
                .assertThat().hasFolders(subFolder);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
            description = "Fails to get children from folder that was previously deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS} , expectedExceptions = CmisObjectNotFoundException.class)
    public void getChildrenFromDeletedFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(testFolder)
            .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder)
            .then().usingResource(testFolder)
                .assertThat().hasChildren(testFile, secondFile, subFolder)
            .and().usingResource(testFolder).refreshResource().and().deleteFolderTree()
            .then().usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get children for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetChildrenFromValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder)
                .then().usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                .assertThat().hasFiles(testFile, secondFile)
                .assertThat().hasFolders(subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get children for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorCanGetChildrenFromValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder);


        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                .assertThat().hasFiles(testFile, secondFile)
                .assertThat().hasFolders(subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get children for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetChildrenFromValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder)
                .then().usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                .assertThat().hasFiles(testFile, secondFile)
                .assertThat().hasFolders(subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor can get children for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorCanGetChildrenFromValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder);


        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                .assertThat().hasFiles(testFile, secondFile)
                .assertThat().hasFolders(subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer can get children for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerCanGetChildrenFromValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder);


        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder)
                    .assertThat().hasFiles(testFile, secondFile)
                    .assertThat().hasFolders(subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member cannot get children for a folder from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetChildrenFromValidFolderFromAPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(testUser).usingSite(privateSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder);

        cmisApi.authenticateUser(testUser)
                .usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non site member cannot get children for a folder from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={ CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void nonSiteMemberCannotGetChildrenFromValidFolderFromAModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(testUser).usingSite(moderatedSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().usingResource(testFolder)
                .createFile(testFile)
                .createFile(secondFile)
                .createFolder(subFolder);

        cmisApi.authenticateUser(testUser)
                .usingResource(testFolder).assertThat().hasChildren(testFile, secondFile, subFolder);
    }
}
