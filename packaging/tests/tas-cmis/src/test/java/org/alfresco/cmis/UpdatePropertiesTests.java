package org.alfresco.cmis;

import java.util.HashMap;
import java.util.Map;

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
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/30/2016.
 */
public class UpdatePropertiesTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FolderModel testFolder;
    String propertyNameValue;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        dataContent.deployContentModel("shared-resources/model/tas-model.xml");
        usersWithRoles = dataUser.usingUser(testUser)
                            .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
                description = "Verify site manager is able to update properties to a valid document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerUpdatesFileProperties() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue)
                    .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
                description = "Verify site manager is able to update properties to a valid folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerUpdatesFolderProperties() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFolder(testFolder).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue)
                    .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify if exception is thrown when user tries to update properties to a non existent document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerUpdatesNoneExistentFileProperties() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile).assertThat().existsInRepo()
                .then().delete().and().assertThat().doesNotExistInRepo()
                .then().updateProperty("cmis:name", propertyNameValue);
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update properties with invalid property with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class)
    public void siteManagerCannotUpdateFileWithInvalidProperty() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:fakeProp", propertyNameValue);
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify that deleted user is not able to update properties with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void deletedUserCannotUpdateFileProperties() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        UserModel toBeDeleted = dataUser.createRandomTestUser();
        FolderModel shared = FolderModel.getSharedFolderModel();
        cmisApi.authenticateUser(toBeDeleted)
            .usingResource(shared)
                .createFile(testFile).and().assertThat().existsInRepo();
        dataUser.deleteUser(toBeDeleted);
        // Token will still be valid right after this call
        // Just wait for it to expire
        Thread.sleep(120 * 1000);
        
        cmisApi.updateProperty("cmis:name", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update name property while document is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCannotUpdatePWCDocumentName() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
            .then().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .usingPWCDocument().updateProperty("cmis:name", testFile.getName() + "-edit")
                .cancelCheckOut()
                .then().usingResource(testFile)
                .assertThat().contentPropertyHasValue("cmis:name", testFile.getName());
    }
   
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is able to update the title and description while document is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanUpdateCheckOutDocTitleAndDescription() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
            .then().checkOut().and().assertThat().documentIsCheckedOut()
                .then().usingPWCDocument()
                    .updateProperty("cmis:description", propertyNameValue)
                    .updateProperty("cm:title", propertyNameValue)
                .and().assertThat().contentPropertyHasValue("cmis:description", propertyNameValue)
                      .assertThat().contentPropertyHasValue("cm:title", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is able to update the title and description for document with version is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanUpdateDocumentWithVersion() throws Exception
    {
        propertyNameValue = RandomData.getRandomAlphanumeric();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile).assertThat().existsInRepo()
                .then().update("first content").update("second content")
                    .updateProperty("cmis:description", propertyNameValue)
                    .updateProperty("cm:title", propertyNameValue)
                .and().assertThat().contentPropertyHasValue("cmis:description", propertyNameValue)
                      .assertThat().contentPropertyHasValue("cm:title", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update document name with invalid symbols")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotUpdateDocNameInvalidSymbols() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = "/.:?|\\`\\";
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update folder name with invalid symbols")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotUpdateFolderNameInvalidSymbols() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        propertyNameValue = "/.:?|\\`\\";
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update document name with empty value")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisRuntimeException.class,
            expectedExceptionsMessageRegExp="Local name cannot be null or empty.")
    public void siteManagerCannotUpdateDocNameEmptyValue() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", "");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is not able to update folder name with empty value")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisRuntimeException.class,
            expectedExceptionsMessageRegExp="Local name cannot be null or empty.")
    public void siteManagerCannotUpdateFolderNameEmptyValue() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(testFolder).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", "");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is able to update Integer and Long types with max values")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanUpdateIntAndLongTypesWithMaxValue() throws Exception
    {
        FileModel customFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:tas:document");
        properties.put(PropertyIds.NAME, customFile.getName());
        
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(customFile, properties, VersioningState.MAJOR).assertThat().existsInRepo()
                .then().updateProperty("tas:IntPropertyC", Integer.MAX_VALUE)
                       .and().assertThat().contentPropertyHasValue("tas:IntPropertyC", String.valueOf(Integer.MAX_VALUE))
                .then().updateProperty("tas:LongPropertyC", Long.MAX_VALUE)
                       .and().assertThat().contentPropertyHasValue("tas:LongPropertyC", String.valueOf(Long.MAX_VALUE));
        cmisApi.usingResource(customFile).delete().assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is able to update Integer and Long types with min values")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanUpdateIntAndLongTypesWithMinValue() throws Exception
    {
        FileModel customFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "custom content");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "D:tas:document");
        properties.put(PropertyIds.NAME, customFile.getName());
        
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(customFile, properties, VersioningState.MAJOR).assertThat().existsInRepo()
                .then().updateProperty("tas:IntPropertyC", Integer.MIN_VALUE)
                       .and().assertThat().contentPropertyHasValue("tas:IntPropertyC", String.valueOf(Integer.MIN_VALUE))
                .then().updateProperty("tas:LongPropertyC", Long.MIN_VALUE)
                       .and().assertThat().contentPropertyHasValue("tas:LongPropertyC", String.valueOf(Long.MIN_VALUE));
        cmisApi.usingResource(customFile).delete().assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site collaborator is able to update properties to a valid document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanUpdateHisOwnFileProperties() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue)
                    .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site collaborator is able to update properties to a valid document created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanUpdateFilePropertiesCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .and().updateProperty("cmis:name", propertyNameValue)
                        .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site contributor is able to update properties to a valid document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanUpdateHisOwnFileProperties() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().updateProperty("cmis:name", propertyNameValue)
                    .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site collaborator is able to update properties to a valid document created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanUpdateFilePropertiesCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                    .and().updateProperty("cmis:name", propertyNameValue)
                        .and().assertThat().contentPropertyHasValue("cmis:name", propertyNameValue)
                .then().updateProperty("cmis:description", "some description")
                    .and().assertThat().contentPropertyHasValue("cmis:description", "some description");
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify site consumer is not able to update properties to a valid document created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotUpdateFilePropertiesCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                    .and().updateProperty("cmis:name", propertyNameValue);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify disabled user is not able to update properties")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void disabledUserCannotUpdateFileProperties() throws Exception
    {
        UserModel disabledUser = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(disabledUser).usingShared()
            .createFile(testFile).assertThat().existsInRepo();
        dataUser.usingAdmin().disableUser(disabledUser);
        cmisApi.usingResource(testFile).updateProperty("cmis:name", propertyNameValue);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
                description = "Verify non invited user is not able to update properties to a document created by site manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotUpdateFilePropertiesCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        UserModel nonInvitedUser = dataUser.createRandomTestUser();
        propertyNameValue = RandomData.getRandomAlphanumeric();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(nonInvitedUser)
                    .and().updateProperty("cmis:name", propertyNameValue);
    }
}
