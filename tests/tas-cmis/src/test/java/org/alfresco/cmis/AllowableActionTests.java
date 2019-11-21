package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AllowableActionTests extends CmisTest
{
    UserModel testUser;
    SiteModel testSite;
    FileModel testFile, managerFile;
    FolderModel testFolder, managerFolder;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, documentContent);
        managerFolder = FolderModel.getRandomFolderModel();
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(managerFile).assertThat().existsInRepo()
            .createFolder(managerFolder).assertThat().existsInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify valid document has allowable action CAN_CHECK_OUT")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyValidDocumentHasAllowableActionCanCheckOut() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .assertThat().hasAllowableActions(Action.CAN_CHECK_OUT)
                .then().assertThat().isAllowableActionInList(Action.CAN_APPLY_ACL, Action.CAN_DELETE_CONTENT_STREAM, Action.CAN_GET_ALL_VERSIONS);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify valid folder has allowable action CAN_GET_CHILDREN, CAN_CREATE_FOLDER")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyValidFolderHasAllowableAction() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
            .assertThat().hasAllowableActions(Action.CAN_GET_CHILDREN, Action.CAN_CREATE_FOLDER);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify pwc document has allowable action CAN_CHECK_IN, CAN_CANCEL_CHECK_OUT and CAN_MOVE_OBJECT")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyPWCDocumentHasAllowable() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
                .assertThat().existsInRepo().and().assertThat().documentIsCheckedOut()
                    .usingPWCDocument()
                        .assertThat().hasAllowableActions(Action.CAN_CHECK_IN, Action.CAN_MOVE_OBJECT, Action.CAN_CANCEL_CHECK_OUT)
                            .and().assertThat().isAllowableActionInList(Action.CAN_CHECK_IN, Action.CAN_GET_CONTENT_STREAM, Action.CAN_CANCEL_CHECK_OUT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify valid folder does not have allowable action CAN_CHECK_IN, CAN_CHECK_OUT, CAN_CANCEL_CHECK_OUT")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyFolderDoesNotHaveCanCheckInAction() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_CHECK_IN, Action.CAN_CHECK_OUT, Action.CAN_CANCEL_CHECK_OUT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify delete document has allowable actions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void verifyDeletedDocumentHasAllowableActions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().delete()
                    .and().assertThat().hasAllowableActions(Action.CAN_CHECK_IN, Action.CAN_MOVE_OBJECT, Action.CAN_CANCEL_CHECK_OUT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify delete document has allowable actions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void verifyDeletedDocumentGetAllowableActions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).assertThat().existsInRepo()
                .then().delete()
                    .and().assertThat().isAllowableActionInList(Action.CAN_CHECK_IN, Action.CAN_MOVE_OBJECT, Action.CAN_CANCEL_CHECK_OUT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify checked out document does not have allowable action CAN_CHECK_IN, CAN_CANCEL_CHECK_OUT")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void verifyCheckedOutDocumentHasAllowable() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
                .assertThat().existsInRepo().and().assertThat().documentIsCheckedOut()
                    .usingResource(testFile)
                        .assertThat().hasAllowableActions(Action.CAN_GET_ALL_VERSIONS, Action.CAN_GET_CONTENT_STREAM, Action.CAN_GET_ACL, Action.CAN_GET_PROPERTIES)
                            .and().assertThat().doesNotHaveAllowableActions(Action.CAN_CHECK_IN, Action.CAN_CANCEL_CHECK_OUT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor can get allowable and not allowable actions for content created by mananger")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorGetAllowableActionForContentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(managerFile)
                .assertThat().hasAllowableActions(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_CONTENT_STREAM)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_CHECK_OUT, Action.CAN_CHECK_IN, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_CONTENT_STREAM)
            .usingResource(managerFolder)
                .assertThat().hasAllowableActions(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_CREATE_FOLDER)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_DELETE_TREE, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_CREATE_FOLDER);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator can get allowable and not allowable actions for content created by mananger")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorGetAllowableActionForContentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(managerFile)
                .assertThat().hasAllowableActions(Action.CAN_UPDATE_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_CHECK_OUT, Action.CAN_ADD_OBJECT_TO_FOLDER)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_CONTENT_STREAM, 
                            Action.CAN_UPDATE_PROPERTIES, Action.CAN_CHECK_OUT, Action.CAN_ADD_OBJECT_TO_FOLDER)
            .usingResource(managerFolder)
                .assertThat().hasAllowableActions(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_CREATE_FOLDER, Action.CAN_UPDATE_PROPERTIES)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_DELETE_TREE, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_CREATE_FOLDER,Action.CAN_UPDATE_PROPERTIES);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer can get allowable and not allowable actions for content created by mananger")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerGetAllowableActionForContentCreatedByManager() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(managerFile)
                .assertThat().hasAllowableActions(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_ALL_VERSIONS, Action.CAN_ADD_OBJECT_TO_FOLDER)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_MOVE_OBJECT, Action.CAN_CHECK_OUT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_ALL_VERSIONS, Action.CAN_ADD_OBJECT_TO_FOLDER)
            .usingResource(managerFolder)
                .assertThat().hasAllowableActions(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_GET_DESCENDANTS)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_DELETE_TREE, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_GET_DESCENDANTS);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer can get allowable and not allowable actions for content created by mananger")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCanGetAllowableActionForContentCreatedByManager() throws Exception
    {
        UserModel nonInvitedUser = dataUser.createRandomTestUser();
        cmisApi.authenticateUser(nonInvitedUser)
            .usingResource(managerFile)
                .assertThat().hasAllowableActions(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_ALL_VERSIONS, Action.CAN_ADD_OBJECT_TO_FOLDER)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_MOVE_OBJECT, Action.CAN_CHECK_OUT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_PROPERTIES, Action.CAN_GET_ACL, Action.CAN_GET_ALL_VERSIONS, Action.CAN_ADD_OBJECT_TO_FOLDER)
            .usingResource(managerFolder)
                .assertThat().hasAllowableActions(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_GET_DESCENDANTS)
                .assertThat().doesNotHaveAllowableActions(Action.CAN_DELETE_OBJECT, Action.CAN_DELETE_TREE, Action.CAN_MOVE_OBJECT)
                .assertThat().isAllowableActionInList(Action.CAN_GET_FOLDER_PARENT, Action.CAN_GET_FOLDER_TREE, Action.CAN_GET_DESCENDANTS);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get allowable actions from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserOnPrivateSiteCannotGetAllowableActions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        cmisApi.authenticateUser(testUser).usingSite(privateSite).createFile(testFile).assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile)
                    .assertThat().hasAllowableActions(Action.CAN_GET_PROPERTIES);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get allowable actions from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserOnModeratedSiteCannotGetAllowableActions() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        SiteModel moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        cmisApi.authenticateUser(testUser).usingSite(moderatedSite).createFile(testFile).assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile)
                    .assertThat().hasAllowableActions(Action.CAN_GET_PROPERTIES);
    }
}
