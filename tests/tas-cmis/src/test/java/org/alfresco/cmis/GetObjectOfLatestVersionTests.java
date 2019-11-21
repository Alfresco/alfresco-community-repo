package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetObjectOfLatestVersionTests extends CmisTest
{
    UserModel testUser, nonInvitedUser;
    SiteModel testSite;
    FileModel testFile, managerFile;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerFile = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        testUser = dataUser.createRandomTestUser();
        nonInvitedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(managerFile).assertThat().existsInRepo()
                .then().checkOut()
                    .and().prepareDocumentForCheckIn().withMajorVersion().checkIn()
                .then().checkOut()
                    .and().prepareDocumentForCheckIn().withMinorVersion().checkIn();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to get last major version for document checked in with minor version")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetLastMajorVersionForDocumentCheckedInWithMajorVersion() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .usingVersion().assertLatestMajorVersionIs(2.0);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to get last minor version for document checked in with major version")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetLastMinorVersionForDocumentCheckedInWithMajorVersion() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .usingVersion().assertLatestMinorVersionIs(2.1);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to get last major version for document that was already deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void siteManagerCannotGetLastMajorVersionForInexistentDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
                .and().prepareDocumentForCheckIn().withMajorVersion().checkIn()
                    .then().usingVersion().assertLatestMajorVersionIs(2.0)
                    .and().delete()
                .then().usingVersion().assertLatestMajorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to get last major version for document that was checked out")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetLastMajorVersionForCheckedOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
            .and().assertThat().existsInRepo()
            .and().checkOut()
            .and().prepareDocumentForCheckIn().withMajorVersion().checkIn()
                .usingVersion().assertLatestMajorVersionIs(2.0)
                .checkOut()
                    .then().usingVersion().assertLatestMajorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to get last minor version for document created with minor VersioningState")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetLastMinorVersionForDocumentWithMinorVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.MINOR).and().assertThat().existsInRepo()
                .then().usingVersion().assertLatestMinorVersionIs(0.1);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to get last major version for document created with minor VersioningState")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class,
        expectedExceptionsMessageRegExp="There is no major version!*")
    public void siteManagerCannotGetLastMajorVersionForDocumentWithMinorVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.MINOR).and().assertThat().existsInRepo()
                .then().usingVersion().assertLatestMajorVersionIs(0.1);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to get last minor version for document created with checked out VersioningState")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetLastVersionForDocumentWithCheckedOutVersioningState() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT).and().assertThat().existsInRepo()
                .then().usingVersion().assertLatestMinorVersionIs(1.0)
                       .usingVersion().assertLatestMajorVersionIs(1.0)
                    .then().usingPWCDocument()
                        .usingVersion().assertLatestMajorVersionIs(1.0)
                        .usingVersion().assertLatestMinorVersionIs(1.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get last versions for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanGetLastVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(managerFile)
                .usingVersion().assertLatestMinorVersionIs(2.1)
                    .usingVersion().assertLatestMajorVersionIs(2.0);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get last versions for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanGetLastVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(managerFile)
                .usingVersion().assertLatestMinorVersionIs(2.1)
                    .usingVersion().assertLatestMajorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is able to get last versions for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanGetLastVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(managerFile)
                .usingVersion().assertLatestMinorVersionIs(2.1)
                    .usingVersion().assertLatestMajorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is able to get last versions for document created in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCanGetLastVersionsForDocumentInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(nonInvitedUser)
            .usingResource(managerFile)
                .usingVersion().assertLatestMinorVersionIs(2.1)
                    .usingVersion().assertLatestMajorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get last versions for document from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetLastVersionsForDocumentCreatedByManagerInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.createPrivateRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.XML, documentContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingSite(privateSite)
                .createFile(privateDoc)
                    .then().checkOut()
                        .and().prepareDocumentForCheckIn().withMajorVersion().checkIn()
                 .authenticateUser(nonInvitedUser).usingVersion().assertLatestMinorVersionIs(2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get last versions for document from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetLastVersionsForDocumentCreatedByManagerInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.createModeratedRandomSite();
        FileModel moderatedDoc = FileModel.getRandomFileModel(FileType.XML, documentContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingSite(moderatedSite)
                .createFile(moderatedDoc)
                    .then().checkOut()
                        .and().prepareDocumentForCheckIn().withMajorVersion().checkIn()
                 .authenticateUser(nonInvitedUser).usingVersion().assertLatestMinorVersionIs(2.0);
    }
}
