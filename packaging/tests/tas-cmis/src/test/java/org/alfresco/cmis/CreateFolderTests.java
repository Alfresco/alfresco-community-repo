package org.alfresco.cmis;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/26/2016
 */
public class CreateFolderTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FolderModel testFolder;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin user is able to create folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to create folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify inexistent user can't create folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void inexistentUserShouldNotCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(new UserModel("random user", "random password")).usingSite(testSite).createFolder(testFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify a folder with same name can't be created in the same location")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisContentAlreadyExistsException.class,
            expectedExceptionsMessageRegExp="An object with this name already exists.*")
    public void shouldNotCreateFolderWithSameName() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify a folder can't be created at invalid path")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class,
            expectedExceptionsMessageRegExp="Object not found.*")
    public void shouldNotCreateFolderAtInvalidPath() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(new SiteModel("inexitentSite")).createFolder(testFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create folder with invalid base type id with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotCreateFolderWithInvalidObjectTypeId() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder, "cmis:fakeType");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create folder with cmis:document base type id with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotCreateFolderWithDocumentTypeId() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder, BaseTypeId.CMIS_DOCUMENT.value());
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create folder with invalid characters in name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void adminCannotCreateFolderWithInvalidCharacters() throws Exception
    {
        testFolder = new FolderModel("/.:?|\\`\\.txt");
        cmisApi.authenticateUser(dataUser.getAdminUser())
            .usingResource(FolderModel.getSharedFolderModel())
                .createFolder(testFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create folder with empty name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class,
            expectedExceptionsMessageRegExp="Property cmis:name must be set!*")
    public void adminCannotCreateFolderWithEmptyName() throws Exception
    {
        cmisApi.authenticateUser(dataUser.getAdminUser())
            .usingResource(FolderModel.getSharedFolderModel())
                .createFolder(new FolderModel(""));
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create folder with empty properties")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="Properties must not be empty!*")
    public void siteManagerCannotCreateFolderWithNoParamenters() throws Exception
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFolder(testFolder, properties);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create folder with properties specific for documents")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="Property 'cmis:isLatestMajorVersion' is not valid for this type or one of the secondary types!*")
    public void siteManagerCannotCreateFolderWithDocumentParamenters() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
        properties.put(PropertyIds.NAME, testFolder.getName());
        properties.put(PropertyIds.IS_LATEST_MAJOR_VERSION, "true");
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite)
                .createFolder(testFolder, properties);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor is able to create folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator is able to create folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer is not able to create folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotCreateFolder() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingSite(testSite)
                .createFolder(testFolder);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify disabled user is not able to create folder in Shared location")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void disabledUserShouldNotCreateFolder() throws Exception
    {
        UserModel disabled = dataUser.createRandomTestUser();
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(disabled);
        dataUser.usingAdmin().disableUser(disabled);
        cmisApi.usingShared()
                .createFolder(testFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to create folder in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserShouldNotCreateDocumentInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingSite(privateSite)
                .createFolder(testFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create folder with a name containing multi byte characters.")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void managerIsAbleToCreateFolderWithMultiByteName() throws Exception
    {
        testFolder = new FolderModel(RandomData.getRandomAlphanumeric() + "\ufeff\u6768\u6728\u91d1");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
    }
}
