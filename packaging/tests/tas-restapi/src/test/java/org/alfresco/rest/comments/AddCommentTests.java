package org.alfresco.rest.comments;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddCommentTests extends RestTest
{    
    private UserModel adminUserModel;
    private FileModel document;
    private SiteModel siteModel;
    private ListUserWithRoles usersWithRoles;
    private String comment;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();        
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify admin user adds comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void adminIsAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        String newContent = "This is a new comment added by " + adminUserModel.getUsername();
        restClient.withCoreAPI().usingResource(document).addComment(newContent)
                   .assertThat().field("content").isNotEmpty()
                   .and().field("content").is(newContent);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.edited", org.hamcrest.Matchers.is(false));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, 
            description = "Verify that comment can be retrieved after it is added")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void addCommentThenRetrieveComment() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(document).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestCommentModelsCollection comments = restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(document).getNodeComments();                  
        restClient.assertStatusCodeIs(HttpStatus.OK);

        comments.assertThat().entriesListContains("content", comment);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user gets status code 401 on post comments call")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
//  @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password"));
        restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Manager user adds comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        String contentSiteManger = "This is a new comment added by user with role: " + UserRole.SiteManager;
        RestCommentModel createdComment = restClient.withCoreAPI().usingResource(document).addComment(contentSiteManger);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        createdComment.assertThat().field("content").isNotEmpty()
                   .and().field("content").is(contentSiteManger);
        
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Contributor user adds comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @Bug(id="ACE-4614")
    public void contributorIsAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        String contentSiteContributor = "This is a new comment added by user with role" + UserRole.SiteContributor;
        RestCommentModel createdComment = restClient.withCoreAPI().usingResource(document).addComment(contentSiteContributor);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        createdComment.assertThat().field("content").isNotEmpty()
                   .and().field("content").is(contentSiteContributor);
        
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user adds comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        String contentSiteCollaborator = "This is a new comment added by user with role: " + UserRole.SiteCollaborator;
        restClient.withCoreAPI().usingResource(document).addComment(contentSiteCollaborator)
                   .assertThat().field("content").isNotEmpty()
                   .and().field("content").is(contentSiteCollaborator);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Consumer user can't add comments with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToAddComment() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        String contentSiteConsumer = "This is a new comment added by user with role: " + UserRole.SiteConsumer;
        restClient.withCoreAPI().usingResource(document).addComment(contentSiteConsumer);
        restClient
                   .assertStatusCodeIs(HttpStatus.FORBIDDEN)
                   .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that invalid request returns status code 404 for nodeId that does not exist")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentUsingInvalidNodeId() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        file.setNodeRef(RandomStringUtils.randomAlphanumeric(20));
        
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(file).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, file.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that request using nodeId that is neither document or folder returns 405")
    @Bug(id = "MNT-16904")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentUsingResourceThatIsNotFileOrFolder() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        FileModel fileWithNodeRefFromLink = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        fileWithNodeRefFromLink = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        fileWithNodeRefFromLink.setNodeRef(link.getNodeRef());

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(fileWithNodeRefFromLink).addComment(comment);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that adding comment using empty content returns 400 status code")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentUsingEmptyContent() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(document).addComment("");                  
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.NULL_ARGUMENT, "comment"));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify adding comment with the same content as one existing comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentTwice() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(document).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(document).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingResource(document).getNodeComments()
            .assertThat().entriesListContains("content", comment);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify comment cannot be added if user is not member of private site")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentWithNonMemberOfPrivateSite() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        UserModel member = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(adminUserModel).createPrivateRandomSite();
        FileModel file = dataContent.usingSite(privateSite).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(member)
                  .withCoreAPI().usingResource(file).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify comment cannot be added if empty network ID is provided")
//  @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentUsingEmptyNetworkId() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        UserModel member = dataUser.createRandomTestUser();
        member.setDomain("");
        
        restClient.authenticateUser(member)
                  .withCoreAPI().usingResource(document).addComment(comment);                  
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that comment cannot be added to another comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentToAComment() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestCommentModel commentEntry = restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(file).addComment(comment);                  
        file.setNodeRef(commentEntry.getId());
        
        restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(file).addComment(comment);
        
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_COMMENT);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that comment cannot be added to a tag")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentToATag() throws Exception
    {
        comment = RandomData.getRandomName("comment1");
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel tag = restClient.withCoreAPI().usingResource(document).addTag("randomTag");
        
        file.setNodeRef(tag.getId());
        
        restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(file).addComment(comment);
        
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_COMMENT);
    }
}
