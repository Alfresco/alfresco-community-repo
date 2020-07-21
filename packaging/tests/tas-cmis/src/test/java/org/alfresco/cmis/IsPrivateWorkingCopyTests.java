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

public class IsPrivateWorkingCopyTests extends CmisTest
{
    SiteModel testSite;
    UserModel managerUser, nonInvitedUser;
    FileModel checkedOutDoc, simpleDoc;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        checkedOutDoc = FileModel.getRandomFileModel(FileType.XML, documentContent);
        simpleDoc = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        managerUser = dataUser.createRandomTestUser();
        nonInvitedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(managerUser)
                            .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(checkedOutDoc).and().checkOut()
            .createFile(simpleDoc);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to verify if checked out document is private working copy")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanVerifyIsPrivateWorkingCopy() throws Exception
    {
        cmisApi.authenticateUser(managerUser).usingResource(checkedOutDoc)
            .assertThat().isPrivateWorkingCopy()
                .then().usingResource(simpleDoc).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to verify if pwc document is private working copy")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanVerifyIsPrivateWorkingCopyOnPwc() throws Exception
    {
        cmisApi.authenticateUser(managerUser).usingResource(checkedOutDoc)
            .usingPWCDocument()
                .assertThat().isPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to verify if deleted document is private working copy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotVerifyIsPrivateWorkingCopyOnDeletedDoc() throws Exception
    {
        FileModel deletedDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(deletedDoc)
                .then().delete().and().assertThat().doesNotExistInRepo()
                    .assertThat().isPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to verify if checked out document is private working copy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanVerifyIsPrivateWorkingCopy() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(checkedOutDoc)
                .assertThat().isPrivateWorkingCopy()
                    .then().usingResource(simpleDoc).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to verify if checked out document is private working copy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanVerifyIsPrivateWorkingCopy() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(checkedOutDoc)
                .assertThat().isPrivateWorkingCopy()
                    .then().usingResource(simpleDoc).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is able to verify if checked out document is private working copy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanVerifyIsPrivateWorkingCopy() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(checkedOutDoc)
                .assertThat().isPrivateWorkingCopy()
                    .then().usingResource(simpleDoc).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is able to verify if checked out document is private working copy in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCanVerifyIsPrivateWorkingCopyInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(checkedOutDoc)
                .assertThat().isPrivateWorkingCopy()
                    .then().usingResource(simpleDoc).assertThat().isNotPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to verify if checked out document is private working copy in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotVerifyIsPrivateWorkingCopyInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        FileModel privateFile = FileModel.getRandomFileModel(FileType.HTML);
        cmisApi.authenticateUser(managerUser).usingSite(privateSite)
            .createFile(privateFile)
                 .then().authenticateUser(nonInvitedUser)
                     .assertThat().isPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to verify if checked out document is private working copy in moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotVerifyIsPrivateWorkingCopyInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(managerUser).createPrivateRandomSite();
        FileModel moderatedFile = FileModel.getRandomFileModel(FileType.HTML);
        cmisApi.authenticateUser(managerUser).usingSite(moderatedSite)
            .createFile(moderatedFile)
                 .then().authenticateUser(nonInvitedUser)
                     .assertThat().isPrivateWorkingCopy();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to verify if document created with CHECKEDOUT versioning state is private working copy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyIsPrivateWorkingCopyForDocumentWithCheckedOutVersioningState() throws Exception
    {
        FileModel checkedDoc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(managerUser).usingSite(testSite)
            .createFile(checkedDoc, VersioningState.CHECKEDOUT).refreshResource()
                    .assertThat().isPrivateWorkingCopy();
    }
}
