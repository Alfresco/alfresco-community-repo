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

/**
 * Created by Claudia Agache on 10/5/2016.
 */
public class GetParentsTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FolderModel testFolder, parentFolder;
    FileModel testFile;
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
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get file parents with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerGetsFileParents() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFile(testFile).and().assertThat().existsInRepo()
                .addDocumentToFolder(testFolder, true).and().assertThat().existsInRepo()
                .then().assertThat().hasParents(testFolder.getName(), parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerGetsFolderParents() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .then().assertThat().hasParents(parentFolder.getName());
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to get parents for an inexistent folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void siteManagerCannotGetInexistentFolderParents() throws Exception
    {
        FolderModel inexistentFolder = FolderModel.getRandomFolderModel();
        inexistentFolder.setCmisLocation("/" + inexistentFolder.getName() + "/");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .and().usingResource(inexistentFolder).assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetsFolderParentsCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .then().assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetsFileParentsCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFile(testFile).and().assertThat().existsInRepo()
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetsFolderParentsCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFolder)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorGetsFileParentsCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .usingResource(testFolder).createFile(testFile).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFile)
                .assertThat().hasParents(testFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetsFolderParentsCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .then().assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetsFileParentsCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFile(testFile).and().assertThat().existsInRepo()
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetsFolderParentsCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFolder)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorGetsFileParentsCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .usingResource(testFolder).createFile(testFile).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFile)
                .assertThat().hasParents(testFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerGetsFolderParents() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFolder)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerGetsFileParents() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo()
                .usingResource(testFolder).createFile(testFile).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFile)
                .assertThat().hasParents(testFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non site member for a private site is not able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberGetsFolderParentsFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non site member for a private site is not able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberGetsFileParentsFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFile(testFile).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFile)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non site member for a moderated site is not able to get folder parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberGetsFolderParentsFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFolder(testFolder).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFolder)
                .assertThat().hasParents(parentFolder.getName());
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non site member for a moderated site is not able to get file parents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberGetsFileParentsFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite)
                .createFolder(parentFolder).and().assertThat().existsInRepo()
                .usingResource(parentFolder).createFile(testFile).and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFile)
                .assertThat().hasParents(parentFolder.getName());
    }
}
