package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import io.restassured.RestAssured;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestSiteContainerModel;
import org.alfresco.rest.model.RestSiteContainerModelsCollection;
import org.alfresco.rest.model.RestSiteEntry;
import org.alfresco.rest.model.RestSiteMemberModelsCollection;
import org.alfresco.rest.model.RestSiteMembershipRequestModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrationFullTests1 extends IntegrationTest
{
    UserModel testUser1, testUser2, testUser3;
    SiteModel testSitePublic, testSiteModerated;

    /**
     * Scenario 87
     * 1. Using RestApi Admin user creates a new group1
     * 2. Using CMIS create test users: U1, U2 and U3
     * 3. Using RestApi add users U2 and U3 to group1
     * 4. U1 creates public test site1 using CMIS
     * 5. U1 creates file1 using CMIS, WebDav, FTP
     * 6. U1 apply Acl permission in the new documents for testGroup with role Site Consumer using CMIS
     * 7. Get permissions for U2 using CMIS
     * 8. U2 edits files created with CMIS, WebDav, FTP using WebDav
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify document permissions are added for a group of users")
    public void addDocumentPermissionsForGroupOfUsers() throws Exception
    {
        STEP("1. Using RestApi Admin user creates a new group1");
        GroupModel group1 = dataGroup.createRandomGroup();
        
        STEP("2. Using CMIS create test users: U1, U2 and U3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        testUser3 = dataUser.createRandomTestUser();

        STEP("3. Using RestApi add users U2 and U3 to group1");
        dataGroup.usingUser(testUser2).addUserToGroup(group1);
        dataGroup.usingUser(testUser3).addUserToGroup(group1);
        
        STEP("4. U1 creates public test site1 using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("5. U1 creates file1 using CMIS, WebDav, FTP");
        FileModel cmisFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFile(cmisFile);
        FileModel webDavFile = FileModel.getRandomFileModel(FileType.HTML);
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(webDavFile);
        FileModel ftpFile = FileModel.getRandomFileModel(FileType.XML);
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(ftpFile);

        STEP("6. U1 apply Acl permission in the new documents for testGroup with role Site Consumer using CMIS");
        cmisAPI.usingResource(cmisFile).addAcl(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(webDavFile).addAcl(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(ftpFile).addAcl(group1, UserRole.SiteConsumer);

        STEP("7. Get permissions for U2 using CMIS");
        cmisAPI.usingResource(cmisFile).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(webDavFile).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(ftpFile).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);

        STEP("8. U2 edits files created with CMIS, WebDav, FTP using WebDav");
        String newContent = "new content";
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(cmisFile).update(newContent)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN)
                .usingResource(webDavFile).update(newContent)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN)
                .usingResource(ftpFile).update(newContent)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN);
    }
    
    /**
     * Scenario 88
     * 1. Using RestApi Admin user creates a new group1
     * 2. Using CMIS create test users: U1, U2 and U3
     * 3. Using RestApi add users U2 and U3 to group1
     * 4. U1 creates public test site1 using CMIS
     * 5. U1 creates folder1 using CMIS, WebDav, FTP
     * 6. U1 apply Acl permission in the new folders for testGroup with role Site Consumer using CMIS
     * 7. Get permissions for U2 using CMIS
     * 8. U2 edits folders created with CMIS, WebDav, FTP using WebDav
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify folder permissions are added for a group of users")
    public void addFolderPermissionsForGroupOfUsers() throws Exception
    {
        STEP("1. Using RestApi Admin user creates a new group1");
        GroupModel group1 = dataGroup.createRandomGroup();
        
        STEP("2. Using CMIS create test users: U1, U2 and U3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        testUser3 = dataUser.createRandomTestUser();

        STEP("3. Using RestApi add users U2 and U3 to group1");
        dataGroup.usingUser(testUser2).addUserToGroup(group1);
        dataGroup.usingUser(testUser3).addUserToGroup(group1);
        
        STEP("4. U1 creates public test site1 using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("5. U1 creates folders using CMIS, WebDav, FTP");
        FolderModel cmisFolder = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(cmisFolder);
        FolderModel webDavFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(webDavFolder);
        FolderModel ftpFolder = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(ftpFolder);

        STEP("6. U1 apply Acl permission in the new folders for testGroup with role Site Consumer using CMIS");
        cmisAPI.usingResource(cmisFolder).addAcl(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(webDavFolder).addAcl(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(ftpFolder).addAcl(group1, UserRole.SiteConsumer);

        STEP("7. Get permissions for U2 using CMIS");
        cmisAPI.usingResource(cmisFolder).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(webDavFolder).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);
        cmisAPI.usingResource(ftpFolder).assertThat().permissionIsSetForGrup(group1, UserRole.SiteConsumer);
        
        STEP("8. U2 edits folders created with CMIS, WebDav, FTP using WebDav");
        String newName = "newName";
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(cmisFolder).rename(newName)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN)
                .usingResource(webDavFolder).rename(newName)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN)
                .usingResource(ftpFolder).rename(newName)
                    .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN);
    }
    
    /**
     * Scenario 102
     * 1. Using CMIS create test users: U1 and U2
     * 2. U1 creates moderated test site1 using CMIS
     * 3. U2 creates site membership request to site1 with RestAPI
     * 4. U2 gets site membership request, check new message using RestAPI
     * 5. U2 updates site membership request with a new message using RestAPI
     * 6. U2 gets site membership request, check new message using RestAPI
     * 7. U1 changes site visibility to private site using RestAPI
     * 8. U2 updates site membership request with a new message using RestAPI
     * 9. U2 gets site membership request, check new message using RestAPI
     * 10. U1 change site visibility to moderated site RestAPI
     * 11. U2 gets site membership request, check new message using RestAPI
     * 12. U2 updates site membership request with a new message using RestAPI
     * 13. U2 get site membership request, check new message using RestAPI
     * 14. U1 user1 changes site visibility to public using RestAPI
     * 15. U2 updates site membership request with a new message with RestAPI
     * 16. U2 gets site membership request, check new message using RestAPI
     * 17. U1 changes site visibility to moderated site RestAPI");
     * 18. U2 gets site membership request, check new message using RestAPI");
     * 19. U2 updates new site membership request using RestAPI
     * 20. U1 approves site membership using RestAPI
     * 21. U2 gets site membership request - no request using RestAPI
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can update or not site membership request when site visibility is updated")
    public void updateSiteMembershipRequestWhenSiteVisibilityIsUpdated() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates moderated test site1 using CMIS");
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();
        
        STEP("3. U2 creates site membership request to site1 with RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingMe().addSiteMembershipRequest("Please add me 1", testSiteModerated, "Request1");
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("4. U2 gets site membership request, check new message using RestAPI");
        RestSiteMembershipRequestModel request = restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 1");
        
        STEP("5. U2 updates site membership request with a new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().updateSiteMembershipRequest(testSiteModerated, "Please add me 2");
        request.assertThat().field("message").is("Please add me 2");
        
        STEP("6. U2 gets site membership request, check new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 2");
        
        STEP("7. U1 changes site visibility to private site using RestAPI");
        dataSite.usingUser(testUser1).updateSiteVisibility(testSiteModerated, Visibility.PRIVATE);
        
        STEP("8. U2 updates site membership request with a new message using RestAPI");
        restAPI.withCoreAPI().usingMe().updateSiteMembershipRequest(testSiteModerated, "Please add me 2");
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        
        STEP("9. U2 gets site membership request, check new message using RestAPI");
        restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        
        STEP("10. U1 change site visibility to moderated site RestAPI");
        dataSite.usingUser(testUser1).updateSiteVisibility(testSiteModerated, Visibility.MODERATED);
        
        STEP("11. U2 gets site membership request, check new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 2");
        
        STEP("12. U2 updates site membership request with a new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().updateSiteMembershipRequest(testSiteModerated, "Please add me 3");
        request.assertThat().field("message").is("Please add me 3");
        
        STEP("13. U2 get site membership request, check new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 3");
        
        STEP("14. U1 user1 changes site visibility to public using RestAPI");
        dataSite.usingUser(testUser1).updateSiteVisibility(testSiteModerated, Visibility.PUBLIC);
        
        STEP("15. U2 updates site membership request with a new message with RestAPI");
        restAPI.withCoreAPI().usingMe().updateSiteMembershipRequest(testSiteModerated, "Please add me 4");
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        
        STEP("16. U2 gets site membership request, check new message using RestAPI");
        restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        
        STEP("17. U1 changes site visibility to moderated site RestAPI");
        dataSite.usingUser(testUser1).updateSiteVisibility(testSiteModerated, Visibility.MODERATED);
        
        STEP("18. U2 gets site membership request, check new message using RestAPI");
        request = restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 3");
        
        STEP("19. U2 updates new site membership request using RestAPI");
        request = restAPI.withCoreAPI().usingMe().updateSiteMembershipRequest(testSiteModerated, "Please add me 5");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        request.assertThat().field("message").is("Please add me 5");
        
        STEP("20. U1 approves site membership using RestAPI");
        RestTaskModel taskModel = restAPI.withWorkflowAPI().getTasks().getTaskModelByDescription(testSiteModerated);
        workflow.approveSiteMembershipRequest(testUser1.getUsername(), testUser1.getPassword(), taskModel.getId(), true, "Approve");
        
        STEP("21. U2 gets site membership request - no request using RestAPI");
        restAPI.withCoreAPI().usingMe().getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        RestSiteEntry site= restAPI.withCoreAPI().usingMe().getSiteMembership(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        site.assertThat().field("id").is(testSiteModerated.getId())
            .and().field("role").isNotEmpty();
    }

    /**
     * Scenario 104
     * 1. Using CMIS create test user A1
     * 2. A1 creates public test site1 using CMIS
     * 3. Using CMIS create test user U1
     * 4. A1 adds U1 to site1 as site manager using RestAPI
     * 5. A1 get site members using RestAPI
     * 6. Using CMIS create test user U2
     * 7. A1 adds U2 to site1 as site collaborator using RestAPI
     * 8. A1 get site members using RestAPI
     * 9. Using CMIS create test user U3
     * 10. A1 add U3 to site1 as site contributor using RestAPI
     * 11. A1 get site members using RestAPI
     * 12. Using CMIS create test user U4
     * 13. A1 add U4 to site1 as site consumer using RestAPI
     * 14. A1 get site members using RestAPI
     * 15. Using CMIS delete test user U1
     * 16. A1 get site members using RestAPI
     * 17. Using CMIS delete test user U2
     * 18. A1 get site members using RestAPI
     * 19. Using CMIS delete test user U3
     * 20. A1 get site members using RestAPI
     * 21. Using CMIS delete test user U4
     * 22. A1 get site members using RestAPI
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get site members when users are added to site or deleted")
    public void getSiteMembersWhenUsersAreAddedOrDeleted() throws Exception
    {        
        STEP("1. Using CMIS create test user A1");
        UserModel testUser = dataUser.createRandomTestUser();

        STEP("2. A1 creates public test site1 using CMIS");
        testSitePublic = dataSite.usingUser(testUser).createPublicRandomSite();
        
        STEP("3. Using CMIS create test user U1");
        UserModel user1 = dataUser.createRandomTestUser();
        
        STEP("4. A1 adds U1 to site1 as site manager using RestAPI");
        user1.setUserRole(UserRole.SiteManager);
        restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).addPerson(user1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);   
        
        STEP("5. A1 get site members using RestAPI");
        RestSiteMemberModelsCollection members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("2");
        members.assertThat().entriesListContains("id", user1.getUsername())
            .and().entriesListContains("role", UserRole.SiteManager.toString());
        
        STEP("6. Using CMIS create test user U2");
        UserModel user2 = dataUser.createRandomTestUser();
        
        STEP("7. A1 adds U2 to site1 as site collaborator using RestAPI");
        user2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).addPerson(user2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED); 
        
        STEP("8. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("3");
        members.assertThat().entriesListContains("id", user2.getUsername())
            .and().entriesListContains("role", UserRole.SiteCollaborator.toString());
        
        STEP("9. Using CMIS create test user U3");
        UserModel user3 = dataUser.createRandomTestUser();
        
        STEP("10. A1 add U3 to site1 as site contributor using RestAPI");
        user3.setUserRole(UserRole.SiteContributor);
        restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).addPerson(user3);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED); 
        
        STEP("11. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("4");
        members.assertThat().entriesListContains("id", user3.getUsername())
            .and().entriesListContains("role", UserRole.SiteContributor.toString());
        
        STEP("12. Using CMIS create test user U4");
        UserModel user4 = dataUser.createRandomTestUser();
        
        STEP("13. A1 add U4 to site1 as site consumer using RestAPI");
        user4.setUserRole(UserRole.SiteConsumer);
        restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).addPerson(user4);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED); 
        
        STEP("14. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("5");
        members.assertThat().entriesListContains("id", user4.getUsername())
            .and().entriesListContains("role", UserRole.SiteConsumer.toString());
        
        STEP("15. Using CMIS delete test user U1");
        dataUser.deleteUser(user1);
        
        STEP("16. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("4");
        members.assertThat().entriesListDoesNotContain("id", user1.getUsername());
        
        STEP("17. Using CMIS delete test user U2");
        dataUser.deleteUser(user2);
        
        STEP("18. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("3");
        members.assertThat().entriesListDoesNotContain("id", user2.getUsername());
        
        STEP("19. Using CMIS delete test user U3");
        dataUser.deleteUser(user3);
        
        STEP("20. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("2");
        members.assertThat().entriesListDoesNotContain("id", user3.getUsername());
        
        STEP("21. Using CMIS delete test user U4");
        dataUser.deleteUser(user4);
        
        STEP("22. A1 get site members using RestAPI");
        members = restAPI.authenticateUser(testUser).withCoreAPI().usingSite(testSitePublic).getSiteMembers();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        members.assertThat().paginationField("count").is("1");
        members.assertThat().entriesListDoesNotContain("id", user4.getUsername());
    }
    
    /**
     * Scenario 105
     * 1. Using CMIS create test user U1
     * 2. U1 creates public test site1 using CMIS
     * 3. U1 creates file1 using Webdav
     * 4. U1 updates file1 properties using CMIS
     * 5. U1 gets file1 properties using CMIS
     * 6. U1 renames file1 using Webdav
     * 7. U1 updates file1 content using FTP
     * 8. U1 adds tags to file1 using RestAPI
     * 9. U1 updates file1 properties using CMIS
     * 10. U1 gets file1 properties using CMIS
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify file properties with file properties are updated")
    public void checkFilePropertiesWhenFileIsUpdated() throws Exception
    {
        STEP("1. Using CMIS create test user U1");
        UserModel testUser = dataUser.createRandomTestUser();

        STEP("2. U1 creates public test site1 using CMIS");
        testSitePublic = dataSite.usingUser(testUser).createPublicRandomSite();
        
        STEP("3. U1 creates file1 using Webdav");
        FileModel file1 = FileModel.getRandomFileModel(FileType.MSWORD2007);
        file1.setContent("content1");
        webDavProtocol.authenticateUser(testUser).usingSite(testSitePublic).createFile(file1);
       
        STEP("4. U1 updates file1 properties using CMIS");
        String newDescription = "New-" + file1.getDescription();
        String newTitle = "New title-" +file1.getTitle();
        cmisAPI.authenticateUser(testUser).usingSite(testSitePublic).usingResource(file1).updateProperty("cmis:description", newDescription)
            .updateProperty("cm:title", newTitle);
        
        STEP("5. U1 gets file1 properties using CMIS");
        cmisAPI.authenticateUser(testUser).usingResource(file1).assertThat().contentPropertyHasValue("cmis:description", newDescription)
            .assertThat().contentPropertyHasValue("cm:title", newTitle);
        
        STEP("6. U1 renames file1 using Webdav");
        String newName = "Edited-" + file1.getName();
        webDavProtocol.authenticateUser(testUser).usingSite(testSitePublic).usingResource(file1).rename(newName);
        
        STEP("7. U1 updates file1 content using FTP");
        String newContent = "New " + file1.getContent();
        ftpProtocol.authenticateUser(testUser).usingSite(testSitePublic).usingResource(file1).update(newContent)   
            .assertThat().contentIs(newContent);
        
        STEP("8. U1 adds tags to file1 using RestAPI");
        String newTag = RandomData.getRandomName("tag");
        RestTagModel tag = restAPI.authenticateUser(testUser).withCoreAPI().usingResource(file1).addTag(newTag);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("9. U1 updates file1 properties using CMIS");
        newTitle = "New title2-" + file1.getTitle();
        cmisAPI.authenticateUser(testUser).usingSite(testSitePublic).usingResource(file1).updateProperty("cm:title", newTitle);
        
        STEP("10. U1 gets file1 properties using CMIS");
        cmisAPI.authenticateUser(testUser).usingResource(file1).assertThat().contentPropertyHasValue("cmis:description", newDescription)
            .assertThat().contentPropertyHasValue("cm:title", newTitle)
            .assertThat().contentPropertyHasValue("cmis:name", newName)
            .assertThat().contentPropertyHasValue("cm:taggable", tag.getId())
            .assertThat().contentIs(newContent);
    }
}
