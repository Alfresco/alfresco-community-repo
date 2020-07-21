package org.alfresco.cmis;

import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetCheckedOutDocumentsTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile;
    FolderModel testFolder;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFolder = FolderModel.getRandomFolderModel();
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(testFolder)
                .usingResource(testFolder).createFile(testFile).assertThat().existsInRepo()
                    .and().checkOut().assertThat().documentIsCheckedOut();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get checked out documents from a valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetCheckedOutDocumentsFromAValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser)
            .usingResource(testFolder)
                .assertThat().folderHasCheckedOutDocument(testFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get checked out documents from session")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetCheckedOutDocumentsFromSession() throws Exception
    {
        cmisApi.authenticateUser(testUser).assertThat().sessionHasCheckedOutDocument(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager CANNOT get checked out documents from inexistent folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void siteManagerCannotGetCheckedOutDocumentsFromInexistentFolder() throws Exception
    {
        FolderModel testFolder = FolderModel.getRandomFolderModel();
        testFolder.setCmisLocation(Utility.buildPath("/", testFolder.getCmisLocation()));
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .and().usingResource(testFolder)
                .assertThat().folderHasCheckedOutDocument(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that user is not able to get checked out documents from session created by admin in root")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void userShouldNotGetCheckedOutDocumentFromSessionCreatedByAdminInRoot() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingRoot()
            .createFile(testFile).assertThat().existsInRepo()
                .and().checkOut()
                    .then().authenticateUser(testUser)
                        .and().assertThat().sessioDoesNotHaveCheckedOutDocument(testFile);
    }
    
    @Bug(id="MNT-17357")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get checked out documents created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldGetCheckedOutDocumentsFromFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFolder)
                .assertThat().folderHasCheckedOutDocument(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get checked out documents created by himslef")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorShouldGetCheckedOutDocumentsCreatedByHimself() throws Exception
    {
        FileModel collaboratorFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFolder)
                .createFile(collaboratorFile).checkOut()
                    .usingResource(testFolder)
                        .assertThat().folderHasCheckedOutDocument(collaboratorFile)
                            .assertThat().sessionHasCheckedOutDocument(collaboratorFile);
    }
    
    @Bug(id="MNT-17357")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get checked out documents created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldGetCheckedOutDocumentsFromFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(testFolder)
                .assertThat().folderHasCheckedOutDocument(testFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get checked out documents created by himslef")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorShouldGetCheckedOutDocumentsCreatedByHimself() throws Exception
    {
        FileModel contributorFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFolder)
                .createFile(contributorFile).checkOut()
                    .usingResource(testFolder)
                        .assertThat().folderHasCheckedOutDocument(contributorFile)
                            .assertThat().sessionHasCheckedOutDocument(contributorFile);
    }
    
    @Bug(id="MNT-17357")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get checked out documents created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerShouldGetCheckedOutDocumentsFromFolderCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(testFolder)
                .assertThat().folderHasCheckedOutDocument(testFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get checked out documents")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserShouldNotGetCheckedOutDocuments() throws Exception
    {
        FileModel doc = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel folder = FolderModel.getRandomFolderModel();
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFolder(folder).usingResource(folder)
                .createFile(doc).checkOut()
                .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                    .usingResource(folder)
                        .assertThat().folderHasCheckedOutDocument(doc);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager user is able to get checked out documents from folder with operation context")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetCheckedOutDocumentsFromFolderWithOperationContext() throws Exception
    {
        OperationContext context = cmisApi.authenticateUser(testUser).getSession().createOperationContext();
        context.setOrderBy(PropertyIds.NAME + " DESC");
        FileModel doc1 = new FileModel("a-file.txt", FileType.TEXT_PLAIN, documentContent);
        FileModel doc2 = new FileModel("b-file.txt", FileType.TEXT_PLAIN, documentContent);
        FileModel doc3 = new FileModel("c-file.txt", FileType.TEXT_PLAIN, documentContent);
        FolderModel folder = FolderModel.getRandomFolderModel();
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFolder(folder).usingResource(folder)
                .createFile(doc1).checkOut()
                .createFile(doc2).checkOut()
                .createFile(doc3)
                    .usingResource(folder)
                        .assertThat().folderHasCheckedOutDocument(context, doc2, doc1);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify user is able to get checked out documents from session with operation context")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetCheckedOutDocumentsFromSessionWithOperationContext() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        OperationContext context = cmisApi.authenticateUser(user).getSession().createOperationContext();
        context.setOrderBy(PropertyIds.NAME + " DESC");
        FileModel doc1 = new FileModel("a-file.txt", FileType.TEXT_PLAIN, documentContent);
        FileModel doc2 = new FileModel("b-file.txt", FileType.TEXT_PLAIN, documentContent);
        FileModel doc3 = new FileModel("c-file.txt", FileType.TEXT_PLAIN, documentContent);
        SiteModel publicSite = dataSite.usingUser(user).createPublicRandomSite();
        cmisApi.authenticateUser(user).usingSite(publicSite)
                .createFile(doc1).checkOut()
                .createFile(doc2).checkOut()
                .createFile(doc3)
                    .then().assertThat().sessionHasCheckedOutDocument(context, doc2, doc1);
    }
}
