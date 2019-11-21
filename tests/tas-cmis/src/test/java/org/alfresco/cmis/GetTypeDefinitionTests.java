package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetTypeDefinitionTests extends CmisTest
{
    UserModel testUser;
    SiteModel publicSite, privateSite, moderatedSite;
    FileModel testFile;
    FolderModel testFolder;

    DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(testUser).createPublicRandomSite();
        privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        cmisApi.authenticateUser(testUser);

        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteContributor,
                UserRole.SiteConsumer);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, 
              description = "Verify site manager can get Type Definition for a valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS })
    public void siteManagerShouldGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFolder(testFolder)
               .and().assertThat().existsInRepo()
               .then().assertThat()
               .typeDefinitionIs(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY, 
              description = "Verify site manager can get Type Definition for a valid document")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS })
    public void siteManagerShouldGetTypeDefinitionForValidDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFile(testFile)
               .and().assertThat().existsInRepo()
               .then().assertThat()
               .typeDefinitionIs(testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site manager cannot get Type Definition for a deleted document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisObjectNotFoundException.class })
    public void siteManagerShouldGetTypeDefinitionForDeletedDocument() throws Exception
    {
        FileModel deletedFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFile(deletedFile)
               .and().usingResource(deletedFile).delete()
               .then().assertThat()
               .typeDefinitionIs(deletedFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify user that was deleted cannot get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisUnauthorizedException.class })
    public void deletedUserCannotGetTypeDefinitionForValidFolder() throws Exception
    {
        UserModel deletedUser = dataUser.createRandomTestUser();

        dataUser.usingUser(testUser).addUserToSite(deletedUser, publicSite, UserRole.SiteManager);
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFolder(testFolder);
        cmisApi.authenticateUser(deletedUser).usingSite(publicSite).usingResource(testFolder)
               .then().assertThat()
               .typeDefinitionIs(testFolder);

        dataUser.deleteUser(deletedUser);
        cmisApi.disconnect().assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify user that was deleted can NOT get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteManagerGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(publicSite).createFolder(testFolder)
                .usingResource(testFolder).and().assertThat().existsInRepo()
                .then().assertThat().typeDefinitionIs(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site Contributor is able to get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFolder(testFolder).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
               .usingResource(testFolder).assertThat().typeDefinitionIs(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site Collaborator is able to get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite).createFolder(testFolder)
               .usingResource(testFolder).and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site Consumer is NOT able to get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void siteConsumerCantGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingSite(publicSite).createFolder(testFolder)
               .usingResource(testFolder).and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
              description = "Verify site Manager is able to get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteManagerGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);

        cmisApi.authenticateUser(testUser).usingSite(publicSite).createFile(testFile).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).assertThat().typeDefinitionIs(testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site Contributor is able to get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite).createFile(testFile)
               .usingResource(testFile).and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify site Collaborator is able to get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.XML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite).createFile(testFile)
               .usingResource(testFile).and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFile);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
             description = "Verify site Consumer is NOT able to get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void siteConsumerCantGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.UNDEFINED);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingSite(publicSite).createFile(testFile)
               .usingResource(testFile)
               .and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify user outside private site cannot get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class })
    public void outsideUserPrivateSiteCantGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.XML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(privateSite).createFile(testFile)
                .usingResource(testFile).and().assertThat().existsInRepo()
                .then().assertThat().typeDefinitionIs(testFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
              description = "Verify user outside moderated site cannot get Type Definition for a valid file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class})
    public void outsideUserModeratedSiteGetTypeDefinitionForValidFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.XML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(moderatedSite).createFile(testFile)
               .usingResource(testFile)
               .and().assertThat().existsInRepo()
               .then().assertThat().typeDefinitionIs(testFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
                description = "Verify user outside private site cannot get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class })
    public void outsideUserPrivateSiteCantGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.XML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(privateSite).createFolder(testFolder)
            .usingResource(testFolder).createFile(testFile)
            .usingResource(testFolder).and().assertThat().existsInRepo()
            .then().assertThat().typeDefinitionIs(testFolder);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION, 
                description = "Verify user outside moderated site cannot get Type Definition for a valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class})
    public void outsideUserModeratedSiteGetTypeDefinitionForValidFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.XML);

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(moderatedSite).createFolder(testFolder)
            .usingResource(testFolder).createFile(testFile)
            .usingResource(testFolder)
            .and().assertThat().existsInRepo()
            .then().assertThat().typeDefinitionIs(testFolder);
    }
}
