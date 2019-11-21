package org.alfresco.rest.comments;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.LinkModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateCommentTests extends RestTest
{
    private UserModel adminUserModel, networkUserModel;
    private FileModel document;
    private SiteModel siteModel;
    private RestCommentModel commentModel, returnedCommentModel;
    private RestCommentModelsCollection comments;;
    private ListUserWithRoles usersWithRoles;
    private String firstComment = "This is a new comment";
    private String updatedComment = "This is the updated comment";

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);
        networkUserModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify Admin user updates comments and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void adminIsAbleToUpdateHisComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by admin");
        String updatedContent = "This is the updated comment with admin user";
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, updatedContent)      
                   .assertThat().field("content").isNotEmpty()
                   .and().field("content").is(updatedContent);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user gets status code 401 on update comment call")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToUpdateComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("To be updated by unauthenticated user.");
        UserModel incorrectUserModel = new UserModel("userName", "password");
        restClient.authenticateUser(incorrectUserModel)
                  .withCoreAPI().usingResource(document).updateComment(commentModel, "try to update");
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify entry content in response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void checkEntryContentInResponse() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by admin");
        commentModel = restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with admin user");        
        restClient.assertStatusCodeIs(HttpStatus.OK);   
        commentModel.assertThat().field("content").is("This is the updated comment with admin user");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Manager user updates comments created by admin user and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToUpdateHisComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by manager");
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Manager user")
                .and().field("content").is("This is the updated comment with Manager user")
                .and().field("canEdit").is(true)
                .and().field("canDelete").is(true);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Contributor user can update his own comment and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void contributorIsAbleToUpdateHisComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by contributor");
        String updatedContent = "This is the updated comment with Contributor user";
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, updatedContent);
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Consumer user can not update comments created by admin user and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToUpdateComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by admin");
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Consumer user");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                                      .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user can update his own comment and status code is 200")
//    @Bug(id="REPO-1011")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToUpdateHisComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by collaborator");
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Collaborator user");        
        restClient.assertStatusCodeIs(HttpStatus.OK);   
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user can not update comments of another user and status code is 200")
//  @Bug(id="MNT-2502",description="seems it's one old issue: also logged as MNT-2502, MNT-2346")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToUpdateCommentOfAnotherUser() throws Exception
    {
      restClient.authenticateUser(adminUserModel);
      commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by admin");
      restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Collaborator user");        
      restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify update comment with inexistent nodeId returns status code 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void canNotUpdateCommentIfNodeIdIsNotSet() throws Exception
    {
        restClient.authenticateUser(adminUserModel);

        FolderModel content = FolderModel.getRandomFolderModel();
        content.setNodeRef("node ref that does not exist");
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.withCoreAPI().usingResource(content).updateComment(commentModel, "This is the updated comment.");                
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary("node ref that does not exist was not found");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify if commentId is not set the status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void canNotUpdateCommentIfCommentIdIsNotSet() throws Exception
    {
        restClient.authenticateUser(adminUserModel);

        RestCommentModel comment = new RestCommentModel();
        String id = "comment id that does not exist";
        comment.setId(id);
        restClient.withCoreAPI().usingResource(document).updateComment(comment, "This is the updated comment."); 
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, id));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify can not update comment if NodeId is neither document or folder and returns status code 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void canNotUpdateCommentIfNodeIdIsNeitherDocumentOrFolder() throws Exception
    {
        FileModel content = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        content = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(content).addComment("This is a new comment");
        
        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        content.setNodeRef(link.getNodeRef().replace("workspace://SpacesStore/", "workspace%3A%2F%2FSpacesStore%2F"));
        
        restClient.withCoreAPI().usingResource(content).updateComment(commentModel, "This is the updated comment.");                
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError()
            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, content.getNodeRef()))
            .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Admin user is not able to update with empty comment body and status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void adminIsNotAbleToUpdateWithEmptyCommentBody() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment added by admin");
        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
        .assertLastError().containsSummary("An invalid argument was received");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify updated comment by Manager is listed when calling getComments and status code is 200")
//    @Bug(id="REPO-1011")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void updatedCommentByManagerIsListed() throws Exception
    {
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingResource(document).addComment("This is a new comment added by collaborator");
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI()
        .usingResource(document).updateComment(commentModel, "This is the updated comment with Manager user"); 
        comments = restClient.withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);   
        comments.assertThat().entriesListContains("content", "This is the updated comment with Manager user");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Manager user can update a comment with a large string")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToUpdateACommentWithALargeString() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String longString = RandomStringUtils.randomAlphanumeric(10000);
        
        commentModel = restClient.authenticateUser(adminUserModel)
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        returnedCommentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(file).updateComment(commentModel, longString);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("content").is(longString);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Manager user can update a comment with a short string")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToUpdateACommentWithAShortString() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String shortString = RandomStringUtils.randomAlphanumeric(2);
        
        commentModel = restClient.authenticateUser(adminUserModel)
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        returnedCommentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(file).updateComment(commentModel, shortString);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("content").is(shortString);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Collaborator user can update a comment with special characters")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToUpdateACommentThatContainsSpecialChars() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String specialChars = "!@#$%^&*()'\".,<>-_+=|\\";
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        returnedCommentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .withCoreAPI().usingResource(file).updateComment(commentModel, specialChars);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("content").is(specialChars);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Check that you cannot update comment with Consumer then call getComments and check new comment is not listed")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void cannotUpdateCommentWithConsumerCallGetComments() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", firstComment)
            .and().entriesListDoesNotContain("content", updatedComment)
            .and().paginationField("totalItems").is("1");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Update comment with Contributor then call getComments and check new comment is listed")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @Bug(id = "ACE-4614")
    public void updateCommentWithContributorCallGetComments() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", updatedComment)
            .and().entriesListDoesNotContain("content", firstComment)
            .and().paginationField("totalItems").is("1");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Update comment with Collaborator then call getComments and check new comment is listed")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void updateCommentWithCollaboratorCallGetComments() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", updatedComment)
            .and().entriesListDoesNotContain("content", firstComment)
            .and().paginationField("totalItems").is("1");
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Update comment with Manager then check modified by information in response")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void updateCommentWithManagerCheckModifiedBy() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        UserModel manager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        returnedCommentModel = restClient.authenticateUser(manager).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("modifiedBy.id").is(manager.getUsername())
            .and().field("content").is(updatedComment); 
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Delete comment with Admin then try to update it")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void deleteCommentThenTryToUpdateIt() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel)
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Manager user can update a comment with multi byte content")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToUpdateACommentWithMultiByteContent() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String multiByte = "\ufeff\u6768\u6728\u91d1";
        
        commentModel = restClient.authenticateUser(adminUserModel)
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        returnedCommentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(file).updateComment(commentModel, multiByte);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("content").is(multiByte);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify Admin user can update a comment with properties parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void adminIsAbleToUpdateACommentWithPropertiesParameter() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel)
        .withCoreAPI().usingResource(file).addComment(firstComment);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        UserModel manager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        
        returnedCommentModel = restClient.authenticateUser(manager)
        .withParams("properties=createdBy,modifiedBy,canEdit,canDelete").withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCommentModel.assertThat().field("createdBy.id").is(adminUserModel.getUsername())
            .assertThat().field("modifiedBy.id").is(manager.getUsername())
            .assertThat().fieldsCount().is(4);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Update comment with invalid node")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void updateCommentUsingInvalidNodeId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(file).addComment(firstComment);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        file.setNodeRef(RandomStringUtils.randomAlphanumeric(20));
        restClient.withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, file.getNodeRef()));
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify update comment from node with invalid network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void updateCommentWithInvalidNetworkId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(firstComment);
        networkUserModel.setDomain("invalidNetwork");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED); 
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify User can not update comment to a not joined private site. Status code returned is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanNotUpdateCommentToANotJoinedPrivateSiteDefaultErrorModelSchema() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
               .withCoreAPI().usingResource(file).addComment(firstComment);         
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(newUser).withCoreAPI().usingResource(file).updateComment(commentModel, updatedComment);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
               .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
               .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
               .stackTraceIs(RestErrorModel.STACKTRACE)
               .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY);
    }
}
