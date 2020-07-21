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
 * Created by Claudia Agache on 9/26/2016.
 */
public class AppendContentStreamTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite, privateSite;
    FileModel testFile;
    String initialContent = "initial content ";
    String textToAppend = "text to append";
    private DataUser.ListUserWithRoles usersWithRoles;
  
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }

    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to append content to a not empty file in DocumentLibrary with CMIS")
    public void siteManagerShouldAppendContentToNotEmptyFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
            .and().assertThat().contentIs(initialContent)
                .then().update(textToAppend)
                    .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to append content to a non existent Document in DocumentLibrary with CMIS")
    public void userShouldNotAppendContentToNonexistentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile)
            .assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
            .then().delete()
                .and().assertThat().doesNotExistInRepo()
            .then().update(textToAppend);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to append content to an empty file with version with CMIS")
    public void siteManagerCanAppendContentToEmptyFileWithVersion() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
            .and().assertThat().contentIs(initialContent)
                .then().setContent("")
                    .assertThat().contentIs("")
                    .refreshResource()
                .then().update(textToAppend)
                    .and().assertThat().contentIs(textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to append content to an empty file with no version in DocumentLibrary with CMIS")
    public void siteManagerCanAppendContentToEmptyFileWithNoVersion() throws Exception
    {
        FileModel emptyFile = FileModel.getRandomFileModel(FileType.HTML);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(emptyFile).assertThat().existsInRepo()
            .and().assertThat().contentIs("")
                .then().update(textToAppend)
                    .and().assertThat().contentIs(textToAppend);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to append content with last chunk parameter set to FALSE with CMIS")
    public void siteManagerCanAppendContentWithLastChunkSetToFalse() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
            .and().assertThat().contentIs(initialContent)
                .then().update(textToAppend, false)
                    .and().assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin is able to append content to a file created by other user in DocumentLibrary with CMIS")
    public void adminShouldAppendContentToFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(dataUser.getAdminUser()).update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to append content to a file created by other user in DocumentLibrary with CMIS")
    public void siteManagerShouldAppendContentToFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to append content to a file created by self in DocumentLibrary with CMIS")
    public void contributorShouldAppendContentToFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is not able to append content to a file created by other user in DocumentLibrary with CMIS")
    public void contributorCannotAppendContentToFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).update(textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to append content to a file created by other user in DocumentLibrary with CMIS")
    public void collaboratorShouldAppendContentToFileCreatedByOtherUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to append content to a file created by self in DocumentLibrary with CMIS")
    public void collaboratorShouldAppendContentToFileCreatedBySelf() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to append content to a file with CMIS")
    public void consumerCannotAppendContentToFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).update(textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUpdateConflictException.class, expectedExceptionsMessageRegExp = "^.*Cannot perform operation since the node.*is locked.$")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to append content to a checked out file with CMIS")
    public void managerCannotAppendContentToCheckedOutFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                .when().update(textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to append content to a PWC file with CMIS")
    public void managerCanAppendContentToPWCFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager)
                .usingSite(publicSite).createFile(testFile).assertThat().existsInRepo()
                .then().checkOut().assertThat().documentIsCheckedOut()
                .usingPWCDocument().update(textToAppend)
                .assertThat().contentIs(initialContent + textToAppend);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user is not able to append content to a file created in a private site with CMIS")
    public void unauthorizedUserCannotAppendContentToFileFromPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, initialContent);
        cmisApi.authenticateUser(siteManager)
                .usingSite(privateSite).createFile(testFile).assertThat().existsInRepo()
                .and().assertThat().contentIs(initialContent)
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .usingResource(testFile).update(textToAppend);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is not able to append content to an invalid file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = InvalidCmisObjectException.class, expectedExceptionsMessageRegExp = "^Content at.*is not a file$")
    public void userCannotAppendContentOfInvalidFile() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .update(textToAppend);
    }

}
