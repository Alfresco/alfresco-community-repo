package org.alfresco.rest.comments;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;

public class DeleteCommentTests extends RestTest
{
    private UserModel adminUserModel, networkUserModel;

    private FileModel document;
    private SiteModel siteModel;
    private RestCommentModelsCollection comments;
    private RestCommentModel commentModel;
    private ListUserWithRoles usersWithRoles;
    private String commentText = "This is a new comment";

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        networkUserModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(adminUserModel);
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();        
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel,UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify Admin user deletes comments with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void adminIsAbleToDeleteComments() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);        
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify User gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToDeleteCommentIfAuthenticationFails() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("New comment addded by admin");
        UserModel nonexistentModel = new UserModel("nonexistentUser", "nonexistentPassword");
        restClient.authenticateUser(nonexistentModel);
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Manager user deletes own comments and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToDeleteComments() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("New comment added by Manager");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user deletes own comments and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToDeleteComments() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("New comment added by Collaborator");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Contributor user deletes own comments and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void contributorIsAbleToDeleteComments() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("New comment added by Contributor");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Consumer user can't delete comments created by admin user and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToDeleteComments() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Admin user can't delete comments with inexistent ID and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userIsNotAbleToDeleteInexistentComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = new RestCommentModel();
        commentModel.setId("inexistent");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);        
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Admin user can't delete comments with inexistend NodeId and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userIsNotAbleToDeleteCommentWithInexistentNodeId() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        FileModel inexistentDocument = new FileModel();
        inexistentDocument.setNodeRef("inexistent");
        restClient.withCoreAPI().usingResource(inexistentDocument).deleteComment(commentModel);        
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Admin user can't delete deleted comments and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userIsNotAbleToDeleteDeletedComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);        
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingResource(document).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Manager user deletes comment created by admin"
            + " and status code is 204. Check with getComments for validation")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToDeleteCommentCreatedByOthers() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
         
        commentModel = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(file).addComment(commentText);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListDoesNotContain("content", commentText);
        comments.getPagination().assertThat().field("totalItems").is("0").and().field("count").is("0");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user can delete comment created by self"
            + " and status code is 204. Check with getComments for validation")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToDeleteCommentCreatedBySelf() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .withCoreAPI().usingResource(file).addComment(commentText);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListDoesNotContain("content", commentText);
        comments.getPagination().assertThat().field("totalItems").is("0").and().field("count").is("0");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Contributor user deletes comment created by self"
            + " and status code is 204. Check with getComments for validation")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @Bug(id = "ACE-4614")
    public void contributorIsAbleToDeleteCommentCreatedBySelf() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
  
        commentModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .withCoreAPI().usingResource(file).addComment(commentText);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListDoesNotContain("content", commentText);
        comments.getPagination().assertThat().field("totalItems").is("0").and().field("count").is("0");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Consumer user cannot delete comment created by admin"
            + " and status code is 403. Check with getComments for validation and check default error model schema.")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToDeleteCommentCreatedByOthersDefaultErrorModelSchema() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
                    
        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(commentText);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .statusCodeIs(HttpStatus.FORBIDDEN)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", commentText);
        comments.getPagination().assertThat().field("totalItems").is("1").and().field("count").is("1");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Manager can delete comment with version number")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void usingManagerDeleteCommentWithVersionNumber() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        File updatedContent = Utility.getResourceTestDataFile("sampleContent.txt");

        restClient.withCoreAPI().usingNode(file).usingParams("majorVersion=true&name=newfile.txt").updateNodeContent(updatedContent);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(commentText);
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify Manager user cannot delete comment with invalid node "
                    + "and status code is 404. Check with getComments for validation")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void usingManagerDeleteCommentWithInvalidNode() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
      
        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(commentText);
        file.setNodeRef("invalid");
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(file.getNodeRef() + " was not found");
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(file.getNodeRef() + " was not found");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS}, executionType = ExecutionType.REGRESSION,
            description = "Verify deleteComment from node with invalid network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void deleteCommentWithInvalidNetworkId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(commentText);
        networkUserModel.setDomain("invalidNetwork");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);      
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.COMMENTS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify deleteComment from node with empty network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void deleteCommentWithEmptyNetworkId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        commentModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment(commentText);
        networkUserModel.setDomain("");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(file).deleteComment(commentModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
