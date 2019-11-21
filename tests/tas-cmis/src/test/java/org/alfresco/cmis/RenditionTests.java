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

@Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.RENDITIONS})
public class RenditionTests extends CmisTest
{
    SiteModel testSite;
    UserModel testUser, inexistentUser, nonInvitedUser;
    FileModel testFile;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        testUser = dataUser.createRandomTestUser();
        nonInvitedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        inexistentUser = new UserModel("inexistent", "inexistent");
        usersWithRoles = dataUser.usingUser(testUser)
                            .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin can get renditions for valid Document object")
    public void adminShouldGetRenditionsForDocument() throws Exception
    {
        FileModel txtModel = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingShared().createFile(txtModel)
            .then().usingResource(txtModel).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin can get renditions for invalid Document object")
    public void adminCannotGetRenditionsForInvalidDocument() throws Exception
    {
        FileModel invalidFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        invalidFile.setCmisLocation("/" + invalidFile.getName() + "/");
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingShared()
            .then().usingResource(invalidFile).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager can get renditions for valid Document")
    public void managerGetRenditionsForDocument() throws Exception
    {
        cmisApi.authenticateUser(testUser)
            .usingResource(testFile).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator can get renditions for valid Document")
    public void collaboratorGetRenditionsForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFile).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor can get renditions for valid Document")
    public void contributorGetRenditionsForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(testFile).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer can get renditions for valid Document")
    public void consumerGetRenditionsForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(testFile).assertThat().renditionIsAvailable()
                .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager can get renditions for checked out document")
    public void managerGetRenditionsForCheckedOutDocument() throws Exception
    {
        FileModel checkedDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(testUser)
            .usingSite(testSite).createFile(checkedDoc, VersioningState.CHECKEDOUT).assertThat().documentIsCheckedOut()
                    .usingResource(checkedDoc)
                        .assertThat().renditionIsAvailable()
                            .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user can get renditions in public site")
    public void nonInvitedUserGetRenditionsInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(nonInvitedUser)
            .usingResource(testFile)
                .assertThat().renditionIsAvailable()
                    .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user cannot get renditions in private site")
    public void nonInvitedUserCannotGetRenditionsInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(privateDoc).assertThat().existsInRepo()
                .authenticateUser(nonInvitedUser)
                    .usingResource(privateDoc)
                        .assertThat().renditionIsAvailable()
                            .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user cannot get renditions in moderated site")
    public void nonInvitedUserCannotGetRenditionsInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        FileModel moderatedDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(testUser).usingSite(moderatedSite)
            .createFile(moderatedDoc).assertThat().existsInRepo()
                .authenticateUser(nonInvitedUser)
                    .usingResource(moderatedDoc)
                        .assertThat().renditionIsAvailable()
                            .assertThat().thumbnailRenditionIsAvailable();
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify inexistent user cannot get renditions")
    public void inexistentUserCannotGetRenditions() throws Exception
    {
        cmisApi.authenticateUser(inexistentUser)
            .usingResource(testFile)
                .assertThat().renditionIsAvailable()
                    .assertThat().thumbnailRenditionIsAvailable();
    }
}
