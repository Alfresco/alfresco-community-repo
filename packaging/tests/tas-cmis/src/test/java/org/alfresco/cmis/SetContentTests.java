package org.alfresco.cmis;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/28/2016.
 */
public class SetContentTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite;
    FileModel testFile;
    FolderModel testFolder;
    private String someContent = "some content";
    private String secondContent = "second content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY, 
                description = "Verify site manager is able to set content to a valid document in DocumentLibrary with CMIS")
    public void siteManagerSetFileContentForFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
            .then().update("content")
                .then().setContent(someContent).and().refreshResource()
                    .then().assertThat().contentIs(someContent)
                        .and().assertThat().contentLengthIs(12);
    }

    @Test(expectedExceptions = InvalidCmisObjectException.class, groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
                description = "Verify if exception is thrown when user tries to set content to an invalid document in DocumentLibrary with CMIS")
    public void siteManagerSetFolderContent() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager)
            .usingSite(publicSite)
                .createFolder(testFolder).setContent(someContent);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
                description = "Verify that inexistent user is not able to set content to a document with CMIS")
    public void inexistentUserCannotSetFileContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentLengthIs(0)
            .then().authenticateUser(UserModel.getRandomUserModel())
                .setContent(someContent);
    }
    
    @Bug(id="ACE-5614")
    @Test(groups = { TestGroup.REGRESSION , TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
                description = "Verify site manager is able to set content to a document with no content with overwrite parameter set to false with CMIS")
    public void siteManagerCanSetContentWithFalseOverwriteToDocWithNoContent() throws Exception
    {
        FileModel newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(newFile).assertThat().existsInRepo()
            .and().assertThat().contentLengthIs(0)
            .then().setContent(someContent, false).and()
                .and().assertThat().contentIs(someContent);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisContentAlreadyExistsException.class)
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
                description = "Verify site manager is not able to set content to a document with content with overwrite parameter set to false with CMIS")
    public void siteManagerCanSetContentWithFalseOverwriteToDocWithContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, someContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(someContent)
            .then().setContent(secondContent, false);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
                description = "Verify unauthorized user is not able to set content to a document")
    public void unauthorizedUserCannotSetContent() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, someContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
            .then().authenticateUser(unauthorizedUser)
                .setContent(secondContent);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is able to set file content with CMIS")
    public void adminCanSetFileContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(dataUser.getAdminUser())
                .and().setContent(someContent).and().refreshResource()
                .then().assertThat().contentIs(someContent)
                .and().assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to set file content  for a file created by other user with CMIS")
    public void managerCanSetFileContentForFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .and().setContent(someContent).and().refreshResource()
                .then().assertThat().contentIs(someContent)
                .and().assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to set content to a document created by other user with CMIS")
    public void contributorCannotSetFileContentForFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .then().setContent(someContent);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to set content to a document created by self with CMIS")
    public void contributorCanSetFileContentForFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().setContent(someContent).and().refreshResource()
                .then().assertThat().contentIs(someContent)
                .and().assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to set content to a document created by other user with CMIS")
    public void collaboratorCanSetFileContentForFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .then().usingResource(testFile)
                    .setContent(someContent).and().refreshResource()
                        .and().assertThat().contentIs(someContent)
                            .assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to set content to a document created by self with CMIS")
    public void collaboratorCanSetFileContentForFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().setContent(someContent).and().refreshResource()
                .then().assertThat().contentIs(someContent)
                .and().assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to set file content with CMIS")
    public void consumerCannotSetFileContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .then().setContent(someContent);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUpdateConflictException.class, expectedExceptionsMessageRegExp = "^.*Cannot perform operation since the node.*is locked.$")
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to set content to a checked out document with CMIS")
    public void siteManagerCannotSetContentToCheckedOutFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().and().assertThat().documentIsCheckedOut()
                .setContent(someContent);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to set content to PWC document version with CMIS")
    public void siteManagerSetContentToPWCFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "first content");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().and().assertThat().documentIsCheckedOut()
                .usingPWCDocument().setContent(someContent).and().refreshResource()
                .then().assertThat().contentIs(someContent)
                .and().assertThat().contentLengthIs(12);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to set content to a document from a private site")
    public void unauthorizedUserCannotSetContentToPrivateSiteDoc() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, someContent);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(testFile).setContent(secondContent);
    }
}
