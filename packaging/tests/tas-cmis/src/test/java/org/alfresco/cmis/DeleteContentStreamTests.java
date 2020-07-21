package org.alfresco.cmis;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/27/2016.
 */
public class DeleteContentStreamTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite, privateSite;
    FileModel testFile;
    String content = "file content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to delete content of a not empty document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerDeletesDocumentContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile)
                .deleteContent().and().assertThat().contentIs("");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to delete content of a nonexistent document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void userCantDeleteContentFromNonexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        testFile.setCmisLocation("/fake-folder/test.txt");
        cmisApi.authenticateUser(siteManager)
            .usingResource(testFile).deleteContent();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete content of a empty document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteContentOfEmptyDocument() throws Exception
    {
        FileModel emptyDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager)
            .usingSite(publicSite).createFile(emptyDoc).and().assertThat().existsInRepo()
                .and().assertThat().contentIs("")
            .then().deleteContent().and().assertThat().contentIs("");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify nonexistent user is not able to delete content of a document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void nonexistentUserCannotDeleteDocumentContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile)
            .then().authenticateUser(UserModel.getRandomUserModel())
                .usingResource(testFile).deleteContent();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete content of a not empty document with refresh set to TRUE with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteDocContentWithRefreshTrue() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML, content);
        cmisApi.authenticateUser(siteManager)
            .usingSite(publicSite).createFile(testFile)
                .deleteContent(true).and().assertThat().contentIs("");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete content of a not empty document with refresh set to FALSE with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanDeleteDocContentWithRefreshFalse() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.HTML, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile)
                .deleteContent(true).and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin is able to delete content of a not empty document created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminDeletesContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(dataUser.getAdminUser())
                .deleteContent().and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete content of a not empty document created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerDeletesContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .deleteContent().and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to delete content of a not empty document created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorDeletesContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .deleteContent().and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to delete content of a not empty document created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorDeletesContentOfFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().deleteContent()
                .and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to delete content of a not empty document created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotDeleteContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .deleteContent();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to delete content of a not empty document created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorDeletesContentOfFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().deleteContent()
                .and().assertThat().contentIs("");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to delete content of a not empty document in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotDeleteContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .deleteContent();
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUpdateConflictException.class, expectedExceptionsMessageRegExp = "^.*Cannot perform operation since the node.*is locked.$")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to delete content of a checked out file with CMIS")
    public void managerCannotDeleteContentOfCheckedOutFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                .when().deleteContent();
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to delete content of a PWC file with CMIS")
    public void managerDeletesContentOfPWCFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                .usingPWCDocument().deleteContent()
                .assertThat().contentIs("");
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to delete content of a file created in a private site with CMIS")
    public void unauthorizedUserCannotDeleteContentOfFileFromPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(privateSite).createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).deleteContent();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to delete content of invalid file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = InvalidCmisObjectException.class, expectedExceptionsMessageRegExp = "^Content at.*is not a file$")
    public void userShouldNotDeleteContentOfInvalidFile() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .deleteContent();
    }

}
