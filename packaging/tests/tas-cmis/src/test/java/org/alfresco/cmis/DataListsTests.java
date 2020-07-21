package org.alfresco.cmis;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.DataListItemModel;
import org.alfresco.utility.model.DataListModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DataListsTests extends CmisTest
{
    UserModel manager;
    SiteModel testSite;
    DataListModel dataListModel;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        manager = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(manager).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(manager)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void adminCanCreateContactDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingShared()
            .createDataList(dataListModel).and().assertThat().existsInRepo()
                .assertThat().objectTypeIdIs("F:dl:dataList");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to create data list type contact")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanCreateContactDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .createDataList(dataListModel).and().assertThat().existsInRepo()
                .assertThat().objectTypeIdIs("F:dl:dataList")
                    .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void collaboratorCanCreatedIssueDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createDataList(dataListModel).and().assertThat().existsInRepo()
                .assertThat().objectTypeIdIs("F:dl:dataList")
                    .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void contributorCanCreateEventAgendaDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:eventAgenda");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
            .createDataList(dataListModel).and().assertThat().existsInRepo()
                .assertThat().objectTypeIdIs("F:dl:dataList")
                    .assertThat().objectHasProperty("dl:dataListItemType", "dl:eventAgenda");
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotCreateEventAgendaDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:eventAgenda");
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingSite(testSite)
            .createDataList(dataListModel).and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanCreateContactDataListItem() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        itemProperties.put("dl:contactLastName", "Snow");
        itemProperties.put("dl:contactEmail", "john.snow@stark.com");
        itemProperties.put("dl:contactCompany", "GOT");
        itemProperties.put("dl:contactJobTitle", "king");
        itemProperties.put("dl:contactPhoneOffice", "1234");
        itemProperties.put("dl:contactPhoneMobile", "5678");
        itemProperties.put("dl:contactNotes", "you know nothing john snow");
        
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact")
                    .then().usingResource(dataListModel)
                        .createDataListItem(contactItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanCreateIssueListItem() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        FileModel attachDoc1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel attachDoc2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueStatus", "Not Started");
        itemProperties.put("dl:issuePriority", "Low");
        itemProperties.put("dl:issueDueDate", new Date());
        itemProperties.put("dl:issueComments", "comment");
        
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        cmisApi.authenticateUser(manager).usingSite(testSite).createFile(attachDoc1).createFile(attachDoc2)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().usingResource(dataListModel)
                        .createDataListItem(issueItem).assertThat().existsInRepo()
                            .then().attachDocument(attachDoc1)
                                   .attachDocument(attachDoc2)
                            .assertThat().objectHasRelationshipWith(attachDoc1)
                            .assertThat().objectHasRelationshipWith(attachDoc2)
                            .then().assignToUser(assignUser, "R:dl:issueAssignedTo")
                                .assertThat().userIsAssigned(assignUser);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to create data list type contact")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisObjectNotFoundException.class,
            expectedExceptionsMessageRegExp="Type 'D:dl:invalidItem' is unknown!*")
    public void managerCannotCreateInvalidDataListItem() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:invalidItem");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact")
                    .then().usingResource(dataListModel)
                        .createDataListItem(contactItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify collaborator user is able to add issue item type for data list created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void collaboratorCanAddIssueItem() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        FileModel attachDoc1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel attachDoc2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueDueDate", new Date());
        itemProperties.put("dl:issueComments", "comment");
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite).createFile(attachDoc1).createFile(attachDoc2)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .then().usingResource(dataListModel)
                            .createDataListItem(issueItem).assertThat().existsInRepo()
                                .then().attachDocument(attachDoc1)
                                       .attachDocument(attachDoc2)
                                .assertThat().objectHasRelationshipWith(attachDoc1)
                                .assertThat().objectHasRelationshipWith(attachDoc2)
                                .then().assignToUser(assignUser, "R:dl:issueAssignedTo")
                                    .assertThat().userIsAssigned(assignUser);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify contributor user is able to add issue item type for data list created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void contributorCanAddIssueItem() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        FileModel attachDoc1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel attachDoc2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueStatus", "Not Started");
        itemProperties.put("dl:issuePriority", "Low");
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite).createFile(attachDoc1).createFile(attachDoc2)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .then().usingResource(dataListModel)
                            .createDataListItem(issueItem).assertThat().existsInRepo()
                                .then().attachDocument(attachDoc1)
                                       .attachDocument(attachDoc2)
                                .assertThat().objectHasRelationshipWith(attachDoc1)
                                .assertThat().objectHasRelationshipWith(attachDoc2)
                                .then().assignToUser(assignUser, "R:dl:issueAssignedTo")
                                    .assertThat().userIsAssigned(assignUser);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify consumer user is able to add issue item type for data list created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotAddIssueItem() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .then().usingResource(dataListModel)
                            .createDataListItem(issueItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify non invited user is able to add issue item type for data list created by manager in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotAddIssueItemInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(manager).createPrivateRandomSite();
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(privateSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                         .usingResource(dataListModel)
                             .createDataListItem(issueItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is not able to create data list item with invalid status")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisConstraintException.class)
    public void managerCannotCreateTaskItemWithInvalidStatusValue() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:simpletask");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:simpletaskStatus", "invalid-status");
        
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:simpletask");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(issueItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is not able to create data list item with invalid status")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="Property 'dl:fakePriority' is not valid for this type or one of the secondary types!*")
    public void managerCannotCreateTaskItemWithInvalidParameter() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:simpletask");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:fakePriority", "High");
        
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:simpletask");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(issueItem).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is not able to assign issue item to deleted user")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisInvalidArgumentException.class,
            expectedExceptionsMessageRegExp="Property cmis:targetId must be set!*")
    public void managerCannotAssignIssueItemToDeletedUser() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueDueDate", new Date());
        itemProperties.put("dl:issueComments", "comment");
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().usingResource(dataListModel)
                        .createDataListItem(issueItem).assertThat().existsInRepo();
        dataUser.usingAdmin().deleteUser(assignUser);
        cmisApi.assignToUser(assignUser, "R:dl:issueAssignedTo").assertThat().userIsAssigned(assignUser);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is not able to assign issue item twice to same user")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisRuntimeException.class)
    public void managerCannotAssignIssueItemTwice() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueDueDate", new Date());
        itemProperties.put("dl:issueComments", "comment");
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().usingResource(dataListModel)
                        .createDataListItem(issueItem).assertThat().existsInRepo();
        cmisApi.assignToUser(assignUser, "R:dl:issueAssignedTo").assertThat().userIsAssigned(assignUser)
            .then().assignToUser(assignUser, "R:dl:issueAssignedTo");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to assign issue item for 2 users")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanAssignIssueItemForTwoUsers() throws Exception
    {
        UserModel assignUser = dataUser.createRandomTestUser();
        UserModel anotherUser = dataUser.createRandomTestUser();
        
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:issue");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueID", RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:issueDueDate", new Date());
        itemProperties.put("dl:issueComments", "comment");
        DataListItemModel issueItem = new DataListItemModel(itemProperties);
        dataListModel = DataListModel.getRandomDataListModel("dl:issue");
        
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:issue")
                    .then().usingResource(dataListModel)
                        .createDataListItem(issueItem).assertThat().existsInRepo();
        cmisApi.assignToUser(assignUser, "R:dl:issueAssignedTo").assertThat().userIsAssigned(assignUser)
            .then().assignToUser(anotherUser, "R:dl:issueAssignedTo");
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify manager user is able to delete contact data list with no items")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanDeleteContactDataList() throws Exception
    {
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .createDataList(dataListModel).and().assertThat().existsInRepo()
                    .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact")
                        .then().delete().assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify manager user is not able to create contact data list items with same name twice")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisContentAlreadyExistsException.class)
    public void managerCannotCreateContactDataListWithSameNameTwice() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact")
                    .then().usingResource(dataListModel)
                        .createDataListItem(contactItem).assertThat().existsInRepo()
                        .then().usingResource(dataListModel)
                            .then().createDataListItem(contactItem);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify manager user is not able to create contact data list items at item location")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisInvalidArgumentException.class,
            expectedExceptionsMessageRegExp="Parent folder is not a folder!*")
    public void managerCannotCreateContactDataListItemInsideItem() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .and().assertThat().objectHasProperty("dl:dataListItemType", "dl:contact")
                    .then().usingResource(dataListModel)
                        .createDataListItem(contactItem).assertThat().existsInRepo()
                            .then().usingResource(contactItem)
                                .createDataListItem(contactItem);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify manager user is not able to simple delete contact data list with items")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE}, expectedExceptions=CmisConstraintException.class,
            expectedExceptionsMessageRegExp="Could not delete folder with at least one child!*")
    public void managerCannotSimpleDeleteContactDataListWithItems() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                    .then().usingResource(dataListModel)
                        .createDataListItem(contactItem).assertThat().existsInRepo()
                            .then().usingResource(dataListModel).delete();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify manager user is able to delete(delete tree) contact data list with items in it")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void managerCanDeleteTreeContactDataListWithItems() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(contactItem).assertThat().existsInRepo()
                         .then().usingResource(dataListModel)
                             .deleteFolderTree().assertThat().doesNotExistInRepo()
                                 .and().usingResource(contactItem).assertThat().doesNotExistInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator user is not able to delete(delete tree) contact data list with items in it created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void collaboratorCannotDeleteTreeContactDataListWithItems() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(contactItem).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .usingResource(dataListModel)
                            .deleteFolderTree().assertThat()
                                .hasFailedDeletedObject(dataListModel.getNodeRef())
                                    .and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor user is not able to delete(delete tree) contact data list with items in it created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void contributorCannotDeleteTreeContactDataListWithItems() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(contactItem).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .usingResource(dataListModel)
                            .deleteFolderTree().assertThat()
                                .hasFailedDeletedObject(dataListModel.getNodeRef())
                                    .and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer user is not able to delete(delete tree) contact data list with items in it created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void consumerCannotDeleteTreeContactDataListWithItems() throws Exception
    {
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(contactItem).assertThat().existsInRepo()
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .usingResource(dataListModel)
                            .deleteFolderTree().assertThat()
                                .hasFailedDeletedObject(dataListModel.getNodeRef())
                                    .and().assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to delete(delete tree) contact data list with items in it created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS, TestGroup.REQUIRE_SHARE})
    public void nonInvitedUserCannotDeleteTreeContactDataListWithItems() throws Exception
    {
        UserModel nonInvited = dataUser.createRandomTestUser();
        Map<String, Object> itemProperties = new HashMap<String, Object>();
        itemProperties.put(PropertyIds.OBJECT_TYPE_ID, "D:dl:contact");
        itemProperties.put(PropertyIds.NAME, RandomData.getRandomAlphanumeric());
        itemProperties.put("dl:contactFirstName", "John");
        DataListItemModel contactItem = new DataListItemModel(itemProperties);
        
        dataListModel = DataListModel.getRandomDataListModel("dl:contact");
        cmisApi.authenticateUser(manager).usingSite(testSite)
            .then().createDataList(dataListModel).and().assertThat().existsInRepo()
                .then().usingResource(dataListModel)
                    .createDataListItem(contactItem).assertThat().existsInRepo()
                    .then().authenticateUser(nonInvited)
                        .usingResource(dataListModel)
                            .deleteFolderTree().assertThat()
                                .hasFailedDeletedObject(dataListModel.getNodeRef())
                                    .and().assertThat().existsInRepo();
    }
}
