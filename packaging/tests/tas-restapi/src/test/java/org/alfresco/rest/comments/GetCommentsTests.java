package org.alfresco.rest.comments;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.LinkModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetCommentsTests extends RestTest
{
    private FileModel file, document, document1;
    private SiteModel siteModel, privateSiteModel;
    private UserModel adminUserModel, userModel, networkUserModel;
    private ListUserWithRoles usersWithRoles;
    private String comment = "This is a new comment";
    private String comment2 = "This is a 2nd comment";
    private String comment3 = "This is a 3rd comment";
    private RestCommentModelsCollection comments;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        networkUserModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        privateSiteModel = dataSite.usingUser(adminUserModel).createPrivateRandomSite();
        document = dataContent.usingSite(privateSiteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        document1 = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingResource(document1).addComment(comment);
        restClient.withCoreAPI().usingResource(document).addComment(comment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(document).addComment(comment2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(document).addComment(comment3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.SANITY,
            description= "Verify Admin user gets comments with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void adminIsAbleToRetrieveComments() throws Exception
    {
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI()
                    .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.SANITY,
            description= "Verify Manager user gets status code 401 if authentication call fails")    
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void managerIsNotAbleToRetrieveCommentIfAuthenticationFails() throws Exception
    {
        UserModel nonexistentModel = new UserModel("nonexistentUser", "nonexistentPassword");
        restClient.authenticateUser(nonexistentModel).withCoreAPI()
                .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Manager user gets comments created by admin user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToRetrieveComments() throws Exception
    {
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI()
                    .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Contributor user gets comments created by admin user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveComments() throws Exception
    {
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                    .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Collaborator user gets comments created by admin user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveComments() throws Exception
    {
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                    .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Consumer user gets comments created by admin user with Rest API and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveComments() throws Exception
    {
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment);
    }
    

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Manager user gets comments created by another user and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToRetrieveCommentsCreatedByAnotherUser() throws Exception
    {
        userModel = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        String contentManager = "This is a new comment added by " + userModel.getUsername();
        restClient.authenticateUser(userModel).withCoreAPI()
                .usingResource(document1).addComment(contentManager);
        restClient.assertStatusCodeIs(HttpStatus.CREATED); 
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI()
                .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", contentManager)
                .and().entriesListContains("content", comment);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify admin user gets comments created by another user and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveCommentsCreatedByAnotherUser() throws Exception
    {
        userModel = usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator);
        String contentCollaborator = "This is a new comment added by " + userModel.getUsername();
        restClient.authenticateUser(userModel).withCoreAPI()
                .usingResource(document1).addComment(contentCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.CREATED); 
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingResource(document1).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", contentCollaborator)
                .and().entriesListContains("content", comment);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify request returns status 403 if the user does not have permission read comments on the node")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void uninvitedUserCanNotGetCommentsFromPrivateSite() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI()
                .usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify user gets comments without the first 2 and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void skipFirst2Comments() throws Exception
    {
        comments = restClient.authenticateUser(adminUserModel).withParams("skipCount=2")
                .withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment)
                .and().paginationField("count").is("1");
        comments.assertThat().paginationField("skipCount").is("2");
        comments.assertThat().paginationField("totalItems").is("3");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify invalid request returns status code 400 for invalid maxItems or skipCount")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void checkStatusCodeForInvalidMaxItems() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=0")
                .withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("Only positive values supported for maxItems");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can't get comments for node with ID that does not exist and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanNotGetCommentsOnNonExistentFile() throws Exception
    {
        FileModel nonexistentFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        nonexistentFile.setNodeRef("ABC");
        restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingResource(nonexistentFile).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nonexistentFile.getNodeRef()));
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can't get comments for node that exists but is not a document or a folder and status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanNotGetCommentsOnLink() throws Exception
    {
        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        FileModel fileWithNodeRefFromLink = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        fileWithNodeRefFromLink.setNodeRef(link.getNodeRef().replace("workspace://SpacesStore/", "workspace%3A%2F%2FSpacesStore%2F"));
        restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingResource(fileWithNodeRefFromLink).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError()
            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, fileWithNodeRefFromLink.getNodeRef()))
            .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify get comments from node with invalid network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void getCommentsWithInvalidNetwork() throws Exception
    {
        networkUserModel.setDomain("invalidNetwork");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify get comments from node with empty network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void getCommentsWithEmptyNetwork() throws Exception
    {
        networkUserModel.setDomain("");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify that if manager adds one comment, it will be returned in getComments response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithManagerAndCheckThatCommentIsReturned() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(file).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment)
            .getPagination().assertThat().field("totalItems").is("1")
            .assertThat().field("count").is("1");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify that if collaborator adds one comment, it will be returned in getComments response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithCollaboratorAndCheckThatCommentIsReturned() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .withCoreAPI().usingResource(file).addComment(comment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment)
            .getPagination().assertThat().field("totalItems").is("1")
            .assertThat().field("count").is("1");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify that if contributor adds one comment, it will be returned in getComments response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @Bug(id = "ACE-4614")
    public void addCommentWithContributorAndCheckThatCommentIsReturned() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .withCoreAPI().usingResource(file).addComment(comment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment)
            .getPagination().assertThat().field("totalItems").is("1")
            .assertThat().field("count").is("1");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify that consumer cannot add a comment and no comments will be returned in getComments response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithConsumerAndCheckThatCommentIsNotReturned() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .withCoreAPI().usingResource(file).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().paginationField("totalItems").is("0");
        comments.assertThat().paginationField("count").is("0");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Add one comment with Manager and check that returned person is the right one")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithManagerCheckReturnedPersonIsTheRightOne() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        UserModel user1 = dataUser.createRandomTestUser();
        dataUser.addUserToSite(user1, siteModel, UserRole.SiteManager);
        
        restClient.authenticateUser(user1).withCoreAPI().usingResource(file).addComment(comment);    
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        comments.getOneRandomEntry().onModel().getCreatedBy().assertThat().field("firstName").is(user1.getUsername() + " FirstName")
            .assertThat().field("lastName").is("LN-" + user1.getUsername());        
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Add one comment with Collaborator and check that returned company details are correct")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithCollaboratorCheckReturnedCompanyDetails() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(file).addComment(comment);    
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        comments.getOneRandomEntry().onModel().getCreatedBy().getCompany()
                .assertThat().field("organization").isNull()
                .assertThat().field("address1").isNull()
                .assertThat().field("address2").isNull()
                .assertThat().field("address3").isNull()
                .assertThat().field("postcode").isNull()
                .assertThat().field("telephone").isNull()
                .assertThat().field("fax").isNull()
                .assertThat().field("email").isNull();        
    }  
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Add 2 comments with Manager and Collaborator users and verify valid request using skipCount. Check that param is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addTwoCommentsWithManagerCollaboratorVerifySkipCountParamIsApplied() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(file).addComment(comment);    
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(file).addComment(comment2);    

        comments = restClient.authenticateUser(adminUserModel).withParams("skipCount=1")
                .withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment)
                .and().paginationField("count").is("1");
        comments.assertThat().paginationField("skipCount").is("1");
        comments.assertThat().paginationField("totalItems").is("2");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Add 2 comments with Admin and Collaborator users and verify valid request using maxItems. Check that param is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addTwoCommentsWithAdminCollaboratorVerifyMaxItemsParamIsApplied() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(comment);    
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(file).addComment(comment2);    
        
        comments = restClient.authenticateUser(adminUserModel).withParams("maxItems=1")
                .withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment2)
                .and().paginationField("count").is("1");
        comments.assertThat().paginationField("totalItems").is("2");
    }
  
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Add 2 comments with Manager and Admin users and verify valid request using properties. Check that param is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addTwoCommentsWithAdminManagerVerifyPropertiesParamIsApplied() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(comment);    
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(file).addComment(comment2);    
        
        comments = restClient.authenticateUser(adminUserModel).withParams("properties=createdBy,modifiedBy")
                .withCoreAPI().usingResource(file).getNodeComments();
        comments.assertThat().entriesListIsNotEmpty()
                .and().paginationField("count").is("2");
        comments.assertThat().paginationField("totalItems").is("2");
        
        comments.getEntries().get(0).onModel().getCreatedBy()
            .assertThat().field("firstName").is(usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUsername() + " FirstName")
            .assertThat().field("lastName").is("LN-" + usersWithRoles.getOneUserWithRole(UserRole.SiteManager).getUsername());     
        
        comments.getEntries().get(1).onModel().getCreatedBy()
            .assertThat().field("firstName").is("Administrator")
            .assertThat().field("id").is("admin");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Check default error model schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addTwoCommentsWithManagerCheckDefaultErrorModelSchema() throws Exception
    {
        file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).addComments(comment, comment2);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).usingParams("maxItems=0").getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
        restClient.assertLastError().containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
        restClient.assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
        restClient.assertLastError().descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
        restClient.assertLastError().stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
