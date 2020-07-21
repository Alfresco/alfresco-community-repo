package org.alfresco.cmis;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckInTests extends CmisTest
{
    UserModel unauthorizedUser;
    UserModel siteManager;
    SiteModel testSite;
    FileModel testFile;
    private String fileContent = "content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        unauthorizedUser = dataUser.createRandomTestUser();
        siteManager = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify check in document with minor version and no content")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void checkInDocumentWithMinorVersionAndNoContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
            .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .checkIn().refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(1.1);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify check in document with minor version and with content")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void checkInDocumentWithMinorVersionAndWithContent() throws Exception
    {
        String newContent = "new minor content";
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
                  .assertThat().isPrivateWorkingCopy()
            .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent(newContent)
                .checkIn()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(1.1)
                .and().assertThat().contentIs(newContent);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify check in document with major version and no content")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void checkInDocumentWithMajorVersionAndNoContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
            .and().prepareDocumentForCheckIn()
                .withMajorVersion()
                .checkIn().refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(2.0);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify check in document with major version and with content")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void checkInDocumentWithMajorVersionAndWithContent() throws Exception
    {
        String newContent = "new major content";
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
            .and().prepareDocumentForCheckIn()
                .withMajorVersion()
                .withContent(newContent)
                .checkIn().refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(2.0);
        Utility.sleep(100, 5000, () ->
                cmisApi.assertThat().contentIs(newContent));
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in document that wasn't checked out")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisVersioningException.class)
    public void checkInDocumentThatWasntCheckedOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().assertThat().documentIsNotCheckedOut()
            .then().prepareDocumentForCheckIn()
                .checkIn();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in comment for document with major version")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void getCheckInCommentForDocMajorVersion() throws Exception
    {
        String newContent = "new major content";
        String comment = "major version comment";
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
            .and().prepareDocumentForCheckIn()
                .withMajorVersion()
                .withContent(newContent)
                .withComment(comment)
                .checkIn().refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(2.0)
                .and().assertThat().contentIs(newContent)
                .assertThat().hasCheckInCommentLastVersion(comment);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in comments for multiple versions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void getCheckInCommentForDocWithMultipleVersions() throws Exception
    {
        String minorComment = "minor version comment";
        String majorComment = "major version comment";
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .then().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent("minor content")
                .withComment(minorComment)
                .checkIn().refreshResource()
                    .and().assertThat().documentIsNotCheckedOut()
                    .and().assertThat().hasCheckInCommentForVersion(1.1, minorComment)
            .then().checkOut().prepareDocumentForCheckIn()
                .withMajorVersion()
                .withContent("major content")
                .withComment(majorComment)
                .checkIn().refreshResource()
                    .and().assertThat().documentIsNotCheckedOut()
                    .and().assertThat().hasCheckInCommentForVersion(2.0, majorComment);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in document with minor version and no content")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisVersioningException.class)
    public void checkInDocumentTwice() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().assertThat().documentIsCheckedOut()
            .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .checkIn()
                    .prepareDocumentForCheckIn()
                .checkIn();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in document created with VersioningState checkedout")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void checkInDocumentCreatedWithVersioningStateCheckedOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
            .and().assertThat().existsInRepo().assertThat().documentIsCheckedOut()
            .then().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent("minor content")
                .checkIn().refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(1.1);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in for deleted document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void checkInDeletedDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile)
            .and().assertThat().existsInRepo()
            .then().delete().assertThat().doesNotExistInRepo()
            .then().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent("minor content")
                .checkIn();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in document created with VersioningState checkedout")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void checkInDocumentWithProperties() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, testFile.getName() + "-edit");
        properties.put("cm:title", testFile.getName() + "-title");
        properties.put("cm:description", "description after checkout");
        
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
            .and().assertThat().existsInRepo().assertThat().documentIsCheckedOut()
            .then().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent("minor content")
                .checkIn(properties).refreshResource()
            .and().assertThat().documentIsNotCheckedOut()
            .then().assertThat().documentHasVersion(1.1)
                .and().assertThat().contentPropertyHasValue("cmis:description", "description after checkout")
                      .assertThat().contentPropertyHasValue("cmis:name", testFile.getName() + "-edit")
                      .assertThat().contentPropertyHasValue("cm:title", testFile.getName() + "-title");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check in document created with VersioningState checkedout")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class)
    public void checkInDocumentWithInvalidProperties() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("cmis:fakeProp","fake-value");
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
            .and().assertThat().existsInRepo().assertThat().documentIsCheckedOut()
            .then().prepareDocumentForCheckIn()
                .withMinorVersion()
                .withContent("minor content")
                .checkIn(properties).refreshResource();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that contributor user can check in document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanCheckInDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .checkIn().refreshResource()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.1);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that collaborator user can check in document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCheckInDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .checkIn().refreshResource()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.1);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that collaborator user can check in document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCheckInDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().prepareDocumentForCheckIn()
                .withMinorVersion()
                .checkIn().refreshResource()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.1);
    }
}
