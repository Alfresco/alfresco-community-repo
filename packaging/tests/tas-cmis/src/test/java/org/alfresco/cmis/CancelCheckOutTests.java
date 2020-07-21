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
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CancelCheckOutTests extends CmisTest
{
    UserModel siteManager;
    SiteModel testSite;
    FileModel testFile;
    private String fileContent = "content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator);
    }

    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify cancel check out on a pwc")
    public void cancelCheckOutOnPWC() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile)
            .and().assertThat().existsInRepo()
            .then().checkOut().and().assertThat().documentIsCheckedOut()
                .and().assertThat().isPrivateWorkingCopy()
            .then().cancelCheckOut()
                .and().assertThat().isNotPrivateWorkingCopy()
                .and().assertThat().existsInRepo()
                .and().assertThat().documentIsNotCheckedOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify cancel check out on a document that isn't checked out")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisRuntimeException.class)
    public void cancelCheckOutOnADocumentThatIsntCheckedOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .and().assertThat().isNotPrivateWorkingCopy()
                .then().cancelCheckOut();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify cancel check out on deleted document")
    public void cancelCheckOutOnDeletedDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .then().delete().and().assertThat().doesNotExistInRepo()
                .then().cancelCheckOut();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisRuntimeException.class)
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify cancel check out on a pwc twice")
    public void cancelCheckOutTwice() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut().assertThat().isPrivateWorkingCopy()
                .and().cancelCheckOut()
                      .cancelCheckOut();
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that cancel check out on document created with Versioning State CHECKED OUT deletes the document")
    public void cancelCheckOutOnDocWithVersioningStateCheckedOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
            .and().assertThat().existsInRepo().assertThat().documentIsCheckedOut()
            .then().usingPWCDocument()
                .cancelCheckOut()
                   .and().assertThat().doesNotExistInRepo()
                    .usingResource(testFile).assertThat().doesNotExistInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that collaborator user can cancel check out on document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCancelCheckInDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().cancelCheckOut()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.0);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that collaborator user can cancel check out on document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCancelCheckInDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().cancelCheckOut()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.0);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that contributor user can cancel check out on document created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanCancelCheckInDocumentCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .and().assertThat().documentIsCheckedOut()
                .and().cancelCheckOut()
                .and().assertThat().documentIsNotCheckedOut()
                .then().assertThat().documentHasVersion(1.0);
    }
}
