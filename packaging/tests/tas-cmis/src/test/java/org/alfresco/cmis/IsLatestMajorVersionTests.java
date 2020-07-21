package org.alfresco.cmis;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.report.Bug.Status;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IsLatestMajorVersionTests extends CmisTest
{
    UserModel managerUser, nonInviteUser;
    SiteModel testSite;
    FileModel minorVersionDoc, majorVersionDoc;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        minorVersionDoc = FileModel.getRandomFileModel(FileType.XML, documentContent);
        majorVersionDoc = FileModel.getRandomFileModel(FileType.XML, documentContent);
        managerUser = dataUser.createRandomTestUser();
        nonInviteUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(managerUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(minorVersionDoc, VersioningState.MINOR)
            .createFile(majorVersionDoc, VersioningState.MAJOR);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify is latest major version for document created with VersioningState set to major")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyIsLatestMajorVersionForMajorState() throws Exception
    {
        cmisApi.authenticateUser(managerUser).usingResource(majorVersionDoc)
                .and().assertThat().isLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify latest major version for folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions=InvalidCmisObjectException.class)
    public void verifyIsLatestMajorVersionOnFolders() throws Exception
    {
        FolderModel folderModel = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
                .createFolder(folderModel)
                .and().assertThat().isLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify is latest major version for document created with VersioningState set to minor")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyIsLatestMajorVersionForMinorState() throws Exception
    {
        cmisApi.authenticateUser(managerUser).usingResource(minorVersionDoc)
                .and().assertThat().isNotLatestMajorVersion();
    }
    
    @Bug(id = "MNT-17961", status = Status.FIXED)
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that major version doesn't change for document created with VersioningState set to none")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void verifyMajorVersionNotChangedForNoneState() throws Exception
    {
        FileModel testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
                .createFile(testFile, VersioningState.NONE).and().assertThat().existsInRepo()
                .and().assertThat().isLatestMajorVersion()
                .and().checkOut().and().assertThat().documentIsCheckedOut()
                .and().prepareDocumentForCheckIn()
                      .withMinorVersion()
                      .checkIn().refreshResource()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.0);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify is latest major version for document created with VersioningState set to checkedout")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyIsLatestMajorVersionForCheckedOutState() throws Exception
    {
        FileModel testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
                .createFile(testFile,  VersioningState.CHECKEDOUT)
                .usingPWCDocument()
                .assertThat().existsInRepo()
                    .and().assertThat().isNotLatestMajorVersion()
                    .and().assertThat().documentIsCheckedOut();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify latest major version fails for document that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void verifyIsLatestMajorVersionFailsOnDeletedDocument() throws Exception
    {
        FileModel testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().isLatestMajorVersion()
                .and().delete()
                    .then().assertThat().isLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator can verify is latest major version for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanVerifyIsLatestMajorVersion() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(majorVersionDoc)
            .usingResource(majorVersionDoc)
                .and().assertThat().isLatestMajorVersion()
                    .then().usingResource(minorVersionDoc).assertThat().isNotLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor can verify is latest major version for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanVerifyIsLatestMajorVersion() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(majorVersionDoc)
            .usingResource(majorVersionDoc)
                .and().assertThat().isLatestMajorVersion()
                    .then().usingResource(minorVersionDoc).assertThat().isNotLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer can verify is latest major version for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanVerifyIsLatestMajorVersion() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(majorVersionDoc)
            .usingResource(majorVersionDoc)
                .and().assertThat().isLatestMajorVersion()
                    .then().usingResource(minorVersionDoc).assertThat().isNotLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user can verify is latest major version for document in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCanVerifyIsLatestMajorVersionInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(nonInviteUser).usingResource(majorVersionDoc)
            .usingResource(majorVersionDoc)
                .and().assertThat().isLatestMajorVersion()
                    .then().usingResource(minorVersionDoc).assertThat().isNotLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user can verify is latest major version for document in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotVerifyIsLatestMajorVersionInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        cmisApi.authenticateUser(managerUser)
            .usingSite(privateSite).createFile(privateDoc)
                .then().authenticateUser(nonInviteUser).usingResource(privateDoc)
                    .and().assertThat().isLatestMajorVersion();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user can verify is latest major version for document in moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotVerifyIsLatestMajorVersionInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        FileModel moderatedDoc = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        cmisApi.authenticateUser(managerUser)
            .usingSite(moderatedSite).createFile(moderatedDoc)
                .then().authenticateUser(nonInviteUser).usingResource(moderatedDoc)
                    .and().assertThat().isLatestMajorVersion();
    }
}
