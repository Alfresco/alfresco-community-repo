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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/27/2016.
 */
public class GetContentStreamTests extends CmisTest
{
    SiteModel publicSite, privateSite;
    UserModel siteManager;
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
            description = "Verify site manager is able to get a document content in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetDocumentContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile)
                .then().assertThat().existsInRepo()
                    .and().assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get empty document content in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetEmptyDocumentContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile)
                .then().assertThat().existsInRepo().and().assertThat().contentIs("");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to get content from checked out document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetContentFromCheckedOutDoc() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                    .then().checkOut()
                    .and().assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin is able to get document content with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminShouldGetContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(dataUser.getAdminUser())
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to get content of file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get content of file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldGetContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get content of file created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldGetContentOfFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get content of file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldGetContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get content of file created by self in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldGetContentOfFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is able to get content of file created by other user in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerShouldGetContentOfFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to get content from checked out document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetContentOfPWCDoc() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile)
                .and().assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                .usingPWCDocument().assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify unauthorized is not able to get content of file created in a private site with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class, CmisUnauthorizedException.class })
    public void unauthorizedUserShouldNotGetFileContent() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to get content of non existent file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisObjectNotFoundException.class)
    public void userShouldNotGetContentOfNonexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        testFile.setCmisLocation("/" + testFile.getName() + "/");
        cmisApi.authenticateUser(siteManager)
                .usingResource(testFile).assertThat().contentIs(content);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to get content of invalid file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = InvalidCmisObjectException.class, expectedExceptionsMessageRegExp = "^Content at.*is not a file$")
    public void userShouldNotGetContentOfInvalidFile() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .and().assertThat().contentIs(content);
    }

}
