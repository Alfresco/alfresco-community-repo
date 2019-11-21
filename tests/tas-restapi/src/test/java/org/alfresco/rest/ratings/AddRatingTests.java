package org.alfresco.rest.ratings;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.LinkModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class AddRatingTests extends RestTest
{
    private UserModel userModel;
    private SiteModel siteModel;
    private UserModel adminUser;
    private ListUserWithRoles usersWithRoles;
    private RestRatingModel returnedRatingModel; // placeholder for returned model

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.createRandomTestUser();
        adminUser = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.SANITY, description = "Verify user with Manager role is able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(document)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role is able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(document)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role is able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingResource(document)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role is able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingResource(document)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify admin user is able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void adminIsAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to post like rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToLikeDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(new UserModel("random user", "random password")).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Verify user with Manager role is able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToAddStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(document)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role is able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingResource(document)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();

    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role is able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToAddStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingResource(document)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role is able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToAddStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingResource(document)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.REGRESSION }, 
            executionType = ExecutionType.SANITY, description = "Verify admin user is able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void adminIsAbleToAddStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).rateStarsToDocument(3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("3").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to post stars rating to a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToRateStarsToDocument() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(new UserModel("random user", "random password")).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify that if unknown rating scheme is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void unknownRatingSchemeReturnsBadRequest() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"invalidRate\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "invalidRate"))
                .containsErrorKey(String.format(RestErrorModel.INVALID_RATING, "invalidRate"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that if nodeId does not exist status code 404 is returned")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void invalidNodeIdReturnsNotFound() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        document.setNodeRef(RandomStringUtils.randomAlphanumeric(10));
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRef()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that if nodeId provided cannot be rated 405 status code is returned")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void likeResourceThatCannotBeRated() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        document.setNodeRef(link.getNodeRef().replace("workspace://SpacesStore/", "workspace%3A%2F%2FSpacesStore%2F"));
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRef()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Verify that manager is able to like a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToLikeAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();

        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(folderModel)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Verify that manager is able to like a site")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToLikeASite() throws Exception
    {
        FolderModel folderModel = new FolderModel();
        folderModel.setNodeRef(siteModel.getGuid());

        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(folderModel)
                .likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();

        restClient.withCoreAPI().usingResource(folderModel).getRatings()
                .assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Verify that manager is able to rate a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToRateAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(folderModel)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.SANITY },
            executionType = ExecutionType.REGRESSION, description = "Verify that manager is able to rate a site")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToRateASite() throws Exception
    {
        FolderModel folderModel = new FolderModel();
        folderModel.setNodeRef(siteModel.getGuid());
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingResource(folderModel)
                .rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();

        restClient.withCoreAPI().usingResource(folderModel).getRatings()
                .assertNodeHasFiveStarRating();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that adding like again has no effect on a file")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void fileCanBeLikedTwice() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that adding rate again has no effect on a file")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void fileCanBeRatedTwice() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that rate is not added if empty rating object is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingEmptyRatingObject() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that if empty rate id is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingEmptyValueForId() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                // The message is shortened for comparison as there is a Java object id in the message (random)
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "N/A"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that if empty rating is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingEmptyValueForMyRating() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"likes\", \"myRating\":\"\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.NULL_LIKE_RATING));

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"fiveStar\", \"myRating\":\"\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.NULL_FIVESTAR_RATING));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that user is not able to rate a comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRatingToAComment() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        RestCommentModel comment = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addComment("This is a comment");
        document.setNodeRef(comment.getId());

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.CANNOT_RATE))
                .containsErrorKey(RestErrorModel.CANNOT_RATE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(String.format(RestErrorModel.CANNOT_RATE));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Verify that user is not able to rate a tag")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRatingToATag() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        RestTagModel tag = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addTag("randomTag");
        document.setNodeRef(tag.getId());

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(String.format(RestErrorModel.CANNOT_RATE));

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(String.format(RestErrorModel.CANNOT_RATE));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Contributor is able to like a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToLikeAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();

        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                .usingResource(folderModel)
                .likeDocument();

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Collaborator is able to like a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToLikeAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingResource(folderModel)
                .likeDocument();

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Consumer is able to like a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToLikeAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                .usingResource(folderModel)
                .likeDocument();

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Contributor is able to rate a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToRateAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI()
                .usingResource(folderModel)
                .rateStarsToDocument(5);

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Collaborator is able to rate a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRateAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingResource(folderModel)
                .rateStarsToDocument(5);

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Consumer is able to rate a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToRateAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI()
                .usingResource(folderModel)
                .rateStarsToDocument(5);

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that adding like again has no effect on a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void folderCanBeLikedTwice() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folderModel).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folderModel).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that adding rate again has no effect on a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void folderCanBeRatedTwice() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folderModel).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folderModel).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if invalid rate id is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingInvalidValueForId() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"like\"}");

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "like"))
                .containsErrorKey(String.format(RestErrorModel.INVALID_RATING, "like"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that rate id is case sensitive is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingInvalidValueForIdCaseSensitive() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"Likes\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "Likes"))
                .containsErrorKey(String.format(RestErrorModel.INVALID_RATING, "Likes"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"FiveStar\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "FiveStar"))
                .containsErrorKey(String.format(RestErrorModel.INVALID_RATING, "FiveStar"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if invalid rating is provided status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingInvalidValueForMyRating() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"likes\", \"myRating\":\"skiped\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NULL_LIKE_RATING));

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"fiveStar\", \"myRating\":\"string\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NULL_FIVESTAR_RATING));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Like file created by a different user")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addLikeToAnotherUserFile() throws Exception
    {
        UserModel user = dataUser.usingAdmin().createRandomTestUser();
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        returnedRatingModel = restClient.authenticateUser(user).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Rate a file created by a different user")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRatingToAnotherUserFile() throws Exception
    {
        UserModel user = dataUser.usingAdmin().createRandomTestUser();
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        returnedRatingModel = restClient.authenticateUser(user).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        returnedRatingModel.assertThat().field("myRating").is("5")
                .and().field("id").is("fiveStar")
                .and().field("aggregate").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Add Rate Using Boolean Value For 'myRating'")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addRateUsingBooleanValueForMyRating() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"fiveStar\", \"myRating\":\"true\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(RestErrorModel.NULL_FIVESTAR_RATING)
                .containsErrorKey(RestErrorModel.NULL_FIVESTAR_RATING)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Add Like Using Integer Value For 'myRating'")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addLikeUsingIntegerValueForMyRating() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"likes\", \"myRating\":\"2\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(RestErrorModel.NULL_LIKE_RATING)
                .containsErrorKey(RestErrorModel.NULL_LIKE_RATING)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user is not able to like his own comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void userCannotLikeHisOwnComment() throws Exception
    {
        UserModel user = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        FolderModel folderModel = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(user).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        RestCommentModel comment = restClient.authenticateUser(user).withCoreAPI().usingResource(document).addComment("This is a comment");
        document.setNodeRef(comment.getId());

        returnedRatingModel = restClient.authenticateUser(user).withCoreAPI().usingResource(document).likeDocument();
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.CANNOT_RATE))
                .containsErrorKey(RestErrorModel.CANNOT_RATE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER).stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user is not able to rate his own comment")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void userCannotRateHisOwnComment() throws Exception
    {
        UserModel user = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        FolderModel folderModel = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(user).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        RestCommentModel comment = restClient.authenticateUser(user).withCoreAPI().usingResource(document).addComment("This is a comment");
        document.setNodeRef(comment.getId());

        returnedRatingModel = restClient.authenticateUser(user).withCoreAPI().usingResource(document).rateStarsToDocument(5);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.CANNOT_RATE))
                .containsErrorKey(RestErrorModel.CANNOT_RATE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Collaborator is NOT able to add a negative rating to a file")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToAddANegativeRatingToAFile() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingResource(document)
                .rateStarsToDocument(-5);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(RestErrorModel.RATING_OUT_OF_BOUNDS)
                .containsErrorKey(RestErrorModel.RATING_OUT_OF_BOUNDS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Collaborator is NOT able to add a high rating to a file")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToAddAHighRatingToAFile() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        returnedRatingModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI()
                .usingResource(document)
                .rateStarsToDocument(10);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(RestErrorModel.RATING_OUT_OF_BOUNDS)
                .containsErrorKey(RestErrorModel.RATING_OUT_OF_BOUNDS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    //    @Bug(id = "MNT-17375", description = "Won't Fix, the error message is not ideal and a little cryptic but it does provide the reason i.e. ratingSchemeId is null.")
    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Do not provide field - 'id'")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void notProvideIdLikeFile() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"myRating\":\"true\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "null"))
                .containsErrorKey(String.format(RestErrorModel.INVALID_RATING, "null"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Do not provide field - 'myRating'")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void notProvideMyRatingRateFile() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).addInvalidRating("{\"id\":\"likes\"}");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(RestErrorModel.NULL_LIKE_RATING)
                .containsErrorKey(RestErrorModel.NULL_LIKE_RATING)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}