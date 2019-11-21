package org.alfresco.cmis;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetAllVersionsTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite;
    FileModel testFile, managerFile;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(siteManager)
                .addUsersWithRolesToSite(publicSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(managerFile).assertThat().existsInRepo()
                .and().checkOut().assertThat().documentIsCheckedOut()
                    .then().prepareDocumentForCheckIn().checkIn()
                        .and().checkOut().assertThat().documentIsCheckedOut()
                        .then().prepareDocumentForCheckIn()
                            .withMajorVersion().checkIn();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can get all versions for a valid document")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetAllVersionsForAValidDocument() throws Exception
    {
        cmisApi.authenticateUser(siteManager).usingResource(managerFile)
            .usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can not get all versions for a document that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerShouldNotGetAllVersionsForADeletedDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
                .and().update("content 1").update("content 2")
                .assertThat().documentHasVersion(1.2).usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 1.2)
                .and().delete()
                    .then().usingVersion().getAllDocumentVersions();
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can get all versions for a document using OperationContext: OrderBy")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetAllVersionsWithOperationContextForADocument() throws Exception
    {
        OperationContext context = cmisApi.authenticateUser(siteManager).getSession().createOperationContext();
        context.setOrderBy(PropertyIds.OBJECT_ID + " DESC");
        
        cmisApi.authenticateUser(siteManager).usingResource(managerFile)
            .usingVersion().getAllDocumentVersionsBy(context).assertHasVersionsInOrder(2.0, 1.1, 1.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager cannot get all versions for a folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=InvalidCmisObjectException.class)
    public void siteManagerCannotGetAllVersionsForFolder() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFolder(folder).assertThat().existsInRepo()
                .usingVersion().getAllDocumentVersions();
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can get all versions for a valid checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetAllVersionsForCheckedOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
                .and().checkOut().assertThat().documentIsCheckedOut()
                    .then().prepareDocumentForCheckIn().checkIn()
                .and().checkOut().assertThat().documentIsCheckedOut()
                    .then().usingResource(testFile)
                        .usingVersion().getAllDocumentVersions().assertHasVersions("pwc", 1.1, 1.0);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can get all versions for a valid pwc document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldGetAllVersionsForPWCDocument() throws Exception
    {
        OperationContext context = cmisApi.getSession().createOperationContext();
        context.setOrderBy(PropertyIds.OBJECT_ID + " DESC");
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(testFile).assertThat().existsInRepo()
                .and().checkOut().assertThat().documentIsCheckedOut()
                    .then().prepareDocumentForCheckIn().checkIn()
                .and().checkOut().assertThat().documentIsCheckedOut()
                    .then().usingResource(testFile).usingPWCDocument()
                        .usingVersion().getAllDocumentVersions().assertHasVersions(1.1, 1.0, "pwc")
                        .usingVersion().getAllDocumentVersionsBy(context).assertHasVersionsInOrder("pwc", 1.1, 1.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get all versions for a document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanGetAllVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(managerFile)
                .usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator can get all versions for a document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanGetAllVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(managerFile)
                .usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer can get all versions for a document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanGetAllVersionsForDocumentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(managerFile)
                .usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user can get all versions for a document in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCannotGetAllVersionsForDocumentFromPublicSite() throws Exception
    {
        UserModel nonInvitedUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(nonInvitedUser)
                .usingResource(managerFile)
                    .usingVersion().getAllDocumentVersions().assertHasVersions(1.0, 1.1, 2.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user cannot get all versions for a document in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetAllVersionsForDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite).createFile(testFile)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile)
                    .usingVersion().getAllDocumentVersions().assertHasVersions(1.0);
    }
    
    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify non invited user cannot get all versions for a document in moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetAllVersionsForDocumentFromModeratedSite() throws Exception
    {
        SiteModel moderated = dataSite.usingUser(siteManager).createPrivateRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        cmisApi.authenticateUser(siteManager).usingSite(moderated).createFile(testFile)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile)
                    .usingVersion().getAllDocumentVersions().assertHasVersions(1.0);
    }
}
