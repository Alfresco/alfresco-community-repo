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

public class GetPropertiesTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FolderModel testFolder;
    DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteManager, UserRole.SiteCollaborator,
                UserRole.SiteContributor, UserRole.SiteConsumer);
    }

    @BeforeMethod(alwaysRun = true)
    public void generateRandomContent()
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFolder = FolderModel.getRandomFolderModel();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify secondaryObjectTypeIds property for valid document")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifySecondaryObjectTypeIdsPropertyForValidDocument() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                    "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
            description = "Verify secondaryObjectTypeIds property for valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifySecondaryObjectTypeIdsPropertyForValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                    "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property for inexistent folder (that was previously deleted)")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void verifySecondaryObjectTypeIdsPropertyForInexistentFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                    "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized")
                .and().usingResource(testFolder).deleteFolderTree()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                    "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void managerVerifiesSecondaryObjectTypeIdsPropertyForValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(testSite).usingResource(testFolder)
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                    "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site collaborator for valid document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorVerifiesSecondaryObjectTypeIdsPropertyForValidDocumentCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site collaborator for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorVerifiesSecondaryObjectTypeIdsPropertyForValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site collaborator for valid document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorVerifiesSecondaryObjectTypeIdsPropertyForValidDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFile)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site collaborator for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorVerifiesSecondaryObjectTypeIdsPropertyForValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFolder)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site contributor for valid document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorVerifiesSecondaryObjectTypeIdsPropertyForValidDocumentCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site contributor for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorVerifiesSecondaryObjectTypeIdsPropertyForValidFolderCreatedBySelf() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo()
                .then().assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site contributor for valid document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorVerifiesSecondaryObjectTypeIdsPropertyForValidDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFile)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site contributor for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorVerifiesSecondaryObjectTypeIdsPropertyForValidFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFolder)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site consumer for valid document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerVerifiesSecondaryObjectTypeIdsPropertyForValidDocument() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFile)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify secondaryObjectTypeIds property as site consumer for valid folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerVerifiesSecondaryObjectTypeIdsPropertyForValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFolder)
                .assertThat().objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get secondaryObjectTypeIds property for a valid document from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotGetSecondaryObjectTypeIdsForAValidDocumentFromAPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFile(testFile)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFile).assertThat()
                .objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get secondaryObjectTypeIds property for a valid folder from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotGetSecondaryObjectTypeIdsForAValidFolderFromAPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFolder(testFolder)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFolder).assertThat()
                .objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get secondaryObjectTypeIds property for a valid document from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotGetSecondaryObjectTypeIdsForAValidDocumentFromAModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite).createFile(testFile)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFile).assertThat()
                .objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify that non site member cannot get secondaryObjectTypeIds property for a valid folder from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotGetSecondaryObjectTypeIdsForAValidFolderFromAModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite).createFolder(testFolder)
                .and().assertThat().existsInRepo();

        cmisApi.authenticateUser(testUser).usingResource(testFolder).assertThat()
                .objectHasProperty("cmis:secondaryObjectTypeIds", "Secondary Object Type Ids",
                        "secondaryObjectTypeIds", "cmis:secondaryObjectTypeIds", "P:cm:titled", "P:sys:localized");
    }
}
