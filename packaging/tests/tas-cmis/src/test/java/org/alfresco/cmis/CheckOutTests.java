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
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckOutTests extends CmisTest
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
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteConsumer);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify check out valid document")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyCheckOutValidDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                .and().checkOut()
                .then().assertThat().documentIsCheckedOut()
                    .and().assertThat().isPrivateWorkingCopy();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify check out inexistent document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void verifyCheckOutInexistentDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
                .and().delete().assertThat().doesNotExistInRepo()
                .then().checkOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify if PWC is created for document that is not checked out")
    @Test(groups = {TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyPWCForDocThatIsNotCheckedOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify error when a document is checked out twice")
    @Test(groups = {TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisVersioningException.class, expectedExceptionsMessageRegExp = "^Check out failed.*This node is already checked out.$")
    public void checkOutDocumentTwice() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                    .then().checkOut();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify private working copy does NOT exists for a document that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyPWCDoesNotExistsForDeletedDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(testFile)
                .and().checkOut()
                .then().assertThat().documentIsCheckedOut()
                .usingPWCDocument()
                .delete()
                .and().assertThat().doesNotExistInRepo()
                .then().usingResource(testFile)
                .assertThat().isNotPrivateWorkingCopy().and().assertThat().documentIsNotCheckedOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that contributor user can NOT check out document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = { CmisUnauthorizedException.class, CmisPermissionDeniedException.class })
    public void contributorCannotCheckOutDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .and().checkOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that consumer user can NOT check out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = { CmisUnauthorizedException.class, CmisPermissionDeniedException.class })
    public void consumerCannotCheckOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .and().checkOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that unauthorized user can NOT check out document from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = { CmisUnauthorizedException.class, CmisPermissionDeniedException.class })
    public void unauthorizedUserCannotCheckOutDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(unauthorizedUser)
                .and().checkOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that unauthorized user can NOT check out document from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = { CmisUnauthorizedException.class, CmisPermissionDeniedException.class })
    public void unauthorizedUserCannotCheckOutDocumentModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(siteManager).createModeratedRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);
        cmisApi.authenticateUser(siteManager).usingSite(moderatedSite)
                .createFile(testFile)
                .and().assertThat().existsInRepo()
                .authenticateUser(unauthorizedUser)
                .and().checkOut();
    }
}
