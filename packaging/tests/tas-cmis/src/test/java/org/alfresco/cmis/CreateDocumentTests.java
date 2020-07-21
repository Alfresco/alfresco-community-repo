package org.alfresco.cmis;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/26/2016.
 */
public class CreateDocumentTests extends CmisTest
{
    SiteModel testSite;
    UserModel testUser;
    UserModel inexistentUser;
    FileModel testFile, adminFile;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        inexistentUser = new UserModel("inexistent", "inexistent");
        usersWithRoles = dataUser.usingUser(testUser)
                            .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
   
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to create files in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldCreateDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify inexistent user isn't able to create files in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void inexistentUserShouldNotCreateDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(inexistentUser).usingSite(testSite).createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify unauthorized user isn't able to create files in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserShouldNotCreateDocument() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(unauthorizedUser)
                .usingSite(testSite).createFile(testFile);
    }

    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify admin user is able to create files in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminShouldCreateDocument() throws Exception
    {
        adminFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(testSite)
                .createFile(adminFile).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document twice in the same location with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisContentAlreadyExistsException.class)
    public void siteManagerCannotCreateDocumentTwice() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                    .then().createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document ending with '.'")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotCreateDocumentEndingWithPoint() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, documentContent);
        testFile.setName(testFile.getName() + ".");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create document in invalid location with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void adminCannotCreateDocumentAtInvalidPath() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        FolderModel invalidLocation = new FolderModel("/Shared/invalidFolder");
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingResource(invalidLocation).createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create document with invalid characters in name with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void adminCannotCreateDocumentWithInvalidCharacters() throws Exception
    {
        FileModel invalidCharDoc = new FileModel("/.:?|\\`\\.txt", FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(dataUser.getAdminUser())
            .usingResource(FolderModel.getSharedFolderModel())
                .createFile(invalidCharDoc);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create document with invalid base type id with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotCreateDocWithInvalidObjectTypeId() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, "cmis:fakeType", VersioningState.MAJOR);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create document with cmis:folder base type id with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotCreateDocWithFolderTypeId() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, BaseTypeId.CMIS_FOLDER.value(), VersioningState.MAJOR);
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that manager user is able to create document with valid symbols in name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanCreateDocumentWithValidSymbols() throws Exception
    {
        FileModel validSymbolsDoc = new FileModel("!@#$%^&", FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(validSymbolsDoc).and().assertThat().existsInRepo();
        Assert.assertNotNull(cmisApi.withCMISUtil().getCmisObject(validSymbolsDoc.getCmisLocation()));
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that manager is not able to create document with empty name")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class)
    public void siteManagerCannotCreateDocumentWithEmptyName() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(new FileModel("", FileType.TEXT_PLAIN));
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file with Major versioning state")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldCreateDocumentWithMajorVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, VersioningState.MAJOR).and().assertThat().existsInRepo()
                    .and().assertThat().documentHasVersion(1.0)
                        .assertThat().isLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file with Minor versioning state")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldCreateDocumentWithMinorVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, VersioningState.MINOR).and().assertThat().existsInRepo()
                    .and().assertThat().documentHasVersion(0.1)
                        .assertThat().isNotLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file with CHECKEDOUT versioning state")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldCreateDocumentWithCheckedOutVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, VersioningState.CHECKEDOUT).and().assertThat().existsInRepo()
                    .and().assertThat().documentIsCheckedOut()
                        .assertThat().isPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to create file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldCreateDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to create file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldCreateDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to create file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerShouldNotCreateDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingSite(testSite)
                .createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to create file")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserShouldNotCreateDocumentInSite() throws Exception
    {
        UserModel outsider = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(outsider)
            .usingSite(testSite)
                .createFile(testFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify disabled user is not able to create file in Shared folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void disabledUserShouldNotCreateDocument() throws Exception
    {
        UserModel disabled = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(disabled);
        dataUser.usingAdmin().disableUser(disabled);
        cmisApi.usingShared()
                .createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to create file in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserShouldNotCreateDocumentInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingSite(privateSite)
                .createFile(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create document with no properties")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="Properties must not be empty!*")
    public void siteManagerCannotCreateDocumentWithNoProperties() throws Exception
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, properties, VersioningState.MAJOR);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file with a name containing multi byte characters.")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void managerIsAbleToCreateFileWithMultiByteName() throws Exception
    {
        testFile = new FileModel(RandomData.getRandomAlphanumeric() + "\ufeff\u6768\u6728\u91d1");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType=ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document at document location")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=InvalidCmisObjectException.class)
    public void siteManagerCannotCreateDocumentInDocumentLocation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                    .then().usingResource(testFile)
                        .createFile(testFile);
    }
    
}
