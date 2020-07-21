package org.alfresco.rest.comments;

import java.util.List;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/10/2016.
 */
public class AddCommentsTests extends RestTest
{     
    private UserModel adminUserModel, networkUserModel;
    private FileModel document;
    private SiteModel siteModel;
    private DataUser.ListUserWithRoles usersWithRoles;
    private RestCommentModelsCollection comments;
    private String comment1, comment2;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        networkUserModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(adminUserModel);
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
    }

    @BeforeMethod(alwaysRun = true)
    public void generateRandomComments()
    {
        comment1 = RandomData.getRandomName("comment1");
        comment2 = RandomData.getRandomName("comment2");
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify admin user adds multiple comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void adminIsAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(document).addComments(comment1, comment2)
                  .assertThat().entriesListIsNotEmpty()
                  .and().entriesListContains("content", comment1)
                  .and().entriesListContains("content", comment2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user gets status code 401 on post multiple comments call")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.SANITY })
    public void unauthenticatedUserIsNotAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.noAuthentication()
                  .withCoreAPI().usingResource(document).addComments(comment1, comment2);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Manager user adds multiple comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void managerIsAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                  .withCoreAPI().usingResource(document).addComments(comment1, comment2)
                  .assertThat().entriesListIsNotEmpty()
                  .and().entriesListContains("content", comment1)
                  .and().entriesListContains("content", comment2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Contributor user adds multiple comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void contributorIsAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingResource(document).addComments(comment1, comment2)
                .assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("content", comment1)
                .and().entriesListContains("content", comment2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user adds multiple comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                   .withCoreAPI().usingResource(document).addComments(comment1, comment2)
                   .assertThat().paginationExist().and().entriesListIsNotEmpty()
                   .and().entriesListContains("content", comment1)
                   .and().entriesListContains("content", comment2);
                      
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, description = "Verify Consumer user adds multiple comments with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void consumerIsAbleToAddComments() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingResource(document).addComments(comment1, comment2);
        
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can not add comments to a not joined private site. Status code returned is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanNotAddCommentsToANotJoinedPrivateSite() throws Exception
    {
        SiteModel sitePrivateNotJoined = dataSite.createPrivateRandomSite();
        FileModel file = dataContent.usingSite(sitePrivateNotJoined).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).addComments(comment1, comment2);         
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can't add comments to a node with ID that does not exist and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanNotAddCommentsOnNonexistentFile() throws Exception
    {       
        FileModel nonexistentFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        nonexistentFile.setNodeRef("ABC");
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(nonexistentFile).addComments(comment1,comment2);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
        .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nonexistentFile.getNodeRef()));
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can't add comments to a node that exists but is not a document or a folder and status code is 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    @Bug(id = "MNT-16904")
    public void userCanNotAddCommentsOnLink() throws Exception
    { 
        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        FileModel fileWithNodeRefFromLink = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        fileWithNodeRefFromLink.setNodeRef(link.getNodeRef());
        
        restClient.authenticateUser(adminUserModel).withCoreAPI()
                .usingResource(fileWithNodeRefFromLink).addComments(comment1,comment2);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.UNABLE_TO_LOCATE);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify User can add comments with the same content as one existing comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void userCanAddCommentWithTheSameContentAsExistingOne() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String sameComment = comment1;
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .withCoreAPI().usingResource(file).addComments(comment1, sameComment)
            .assertThat().paginationExist().and().entriesListIsNotEmpty()
            .and().entriesListContains("content", comment1)
            .and().entriesListContains("content", sameComment);          
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI()
            .usingResource(file).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListIsNotEmpty();
        List<RestCommentModel> commentsList = comments.getEntries();
        commentsList.get(0).onModel().assertThat().field("content").is(comment1);
        commentsList.get(1).onModel().assertThat().field("content").is(sameComment);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify add comments from node with invalid network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentsWithInvalidNetworkId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        
        networkUserModel.setDomain("invalidNetwork");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(file).addComments(comment1,comment2);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Verify add comments from node with empty network id returns status code 401")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentsWithEmptyNetworkId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
      
        networkUserModel.setDomain("");
        restClient.authenticateUser(networkUserModel).withCoreAPI().usingResource(file).addComments(comment1,comment2);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that comments cannot be added to another comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentsToAComment() throws JsonToModelConversionException, Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestCommentModel commentEntry = restClient.authenticateUser(adminUserModel)
                  .withCoreAPI().usingResource(file).addComment(comment1);                  
        file.setNodeRef(commentEntry.getId());
        
        restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(file).addComments(comment1, comment2);
        
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_COMMENT);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that comments cannot be added to a tag")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentsToATag() throws JsonToModelConversionException, Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel tag = restClient.withCoreAPI().usingResource(file).addTag("randomTag");
        
        file.setNodeRef(tag.getId());
        
        restClient.authenticateUser(adminUserModel)
            .withCoreAPI().usingResource(file).addComments(comment1, comment2);
        
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_COMMENT);
    }

    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Using Manager user verify that you can provide a large string for one comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addLongCommentsWithManagerAndCheckThatCommentIsReturned() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String longString = RandomStringUtils.randomAlphanumeric(10000);
        String longString1 = RandomStringUtils.randomAlphanumeric(90000);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
        .withCoreAPI().usingResource(document).addComments(longString, longString1);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", longString);
        comments.assertThat().entriesListContains("content", longString1);
        comments.assertThat().paginationField("totalItems").is("2");
        comments.assertThat().paginationField("count").is("2");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Using Manager user verify that you can provide a short string for one comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addShortCommentsWithManagerAndCheckThatCommentIsReturned() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String shortString = RandomStringUtils.randomAlphanumeric(2);
        String shortString1 = RandomStringUtils.randomAlphanumeric(1);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
        .withCoreAPI().usingResource(document).addComments(shortString, shortString1);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", shortString);
        comments.assertThat().entriesListContains("content", shortString1);
        comments.assertThat().paginationField("totalItems").is("2");
        comments.assertThat().paginationField("count").is("2");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Using Collaborator user verify that you can provide a string with special characters for one comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addCommentsWithSpecialCharsWithCollaboratorCheckCommentIsReturned() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String specialCharsString = "!@#$%^&*()'\".,<>-_+=|\\";
        String shortString = RandomStringUtils.randomAlphanumeric(2);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
        .withCoreAPI().usingResource(document).addComments(specialCharsString, shortString);                  
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", specialCharsString);  
        comments.assertThat().entriesListContains("content", shortString);
        comments.assertThat().paginationField("totalItems").is("2");
        comments.assertThat().paginationField("count").is("2");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Using Manager user verify that you can not provide an empty string for one comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addEmptyStringCommentsWithManagerCheckCommentIsReturned() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String emptyString = "";
        String spaceString = " ";
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
        .withCoreAPI().usingResource(document).addComments(emptyString, spaceString);                  
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.NON_NULL_COMMENT);
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Using Collaborator user verify that you can provide several comments in one request")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void addSeveralCommentsWithCollaboratorCheckCommentsAreReturned() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        String charString = RandomStringUtils.randomAlphanumeric(10);
        String charString1 = RandomStringUtils.randomAlphanumeric(10);
        String charString2 = RandomStringUtils.randomAlphanumeric(10);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
        .withCoreAPI().usingResource(document).addComments(comment1, comment2, charString, charString1, charString2);                 
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        comments = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", comment1);
        comments.assertThat().paginationField("totalItems").is("5");
        comments.assertThat().paginationField("count").is("5");
    }
    
    @TestRail(section={TestGroup.REST_API, TestGroup.COMMENTS}, executionType= ExecutionType.REGRESSION,
            description= "Provide invalid request body parameter and check default error model schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.COMMENTS, TestGroup.REGRESSION })
    public void invalidRequestBodyParameterCheckErrorModelSchema() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        String postBody = JsonBodyGenerator.keyValueJson("content2", comment1);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, postBody, "nodes/{nodeId}/comments", document.getNodeRef());
        restClient.processModel(RestCommentModel.class, request);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsErrorKey(String.format(RestErrorModel.UNRECOGNIZED_FIELD, "content2"));
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.UNRECOGNIZED_FIELD, "content2"));
        restClient.assertLastError().descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
        restClient.assertLastError().stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
