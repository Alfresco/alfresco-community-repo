package org.alfresco.rest.ratings;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetRatingTests extends RestTest
{
    private SiteModel siteModel;
    private UserModel adminUser, userModel, managerUser;
    private FileModel document;
    private ListUserWithRoles usersWithRoles;
    private RestRatingModel restRatingModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        managerUser = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        dataUser.addUserToSite(managerUser, siteModel, UserRole.SiteManager);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(DocumentType.TEXT_PLAIN);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.SANITY, description = "Verify user with Manager role is able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToRetrieveRating() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").is("5");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role is able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveRating() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").is("5");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role is able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveRating() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").is("5");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role is able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveRating() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").is("5");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify admin user is able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveRating() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        FileModel document = dataContent.usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(folderModel)
                .createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").is("5");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to retrieve rating of a document")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToRetrieveRating() throws Exception
    {
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restClient.authenticateUser(new UserModel("random user", "random password"));

        restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

        restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Check that using invalid ratingId for get rating call returns status code 400.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkInvalidRatingIdStatusCode() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "nodes/{nodeId}/ratings/{ratingId}", document.getNodeRef(), "invalid ratingId");
        restClient.processModel(RestRatingModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_RATING, "invalid ratingId"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Check that using invalid node ID for get rating call returns status code 404.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingUsingInvalidNodeId() throws Exception
    {
        document.setNodeRef(RandomStringUtils.randomAlphanumeric(20));
        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getFiveStarRating();

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Get rating of a file that has only likes.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFileThatHasOnlyLikes() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).likeDocument();

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("1");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Get rating of a file that has only stars.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFileThatHasOnlyStars() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("1");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Get rating of a file that has likes and stars.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFileThatHasLikesAndStars() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).likeDocument();
        restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModel = restClient.withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("1");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("1");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Get rating of a folder that has only likes.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFolderThatHasOnlyLikes() throws Exception
    {
        FolderModel firstFolderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(firstFolderModel).likeDocument();

        restRatingModel = restClient.withCoreAPI().usingResource(firstFolderModel).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes").and().field("myRating").is("true");
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("1");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, 
            executionType = ExecutionType.REGRESSION, description = "Get rating of a file that has no ratings.")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFileThatHasNoRatings() throws Exception
    {
        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");

        restRatingModel = restClient.withCoreAPI().usingResource(document).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, description = "Check default error schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkDefaultErrorSchema() throws Exception
    {
        FileModel document = dataContent.usingSite(siteModel).usingAdmin().createContent(DocumentType.TEXT_PLAIN);
        String randomNodeRef = RandomStringUtils.randomAlphanumeric(10);
        document.setNodeRef(randomNodeRef);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, randomNodeRef))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of a folder that has only stars")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFolderThatHasOnlyStars() throws Exception
    {
        FolderModel folder = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(folder).rateStarsToDocument(3);
        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar")
                .getAggregate().assertThat().field("numberOfRatings").is("1")
                .assertThat().field("average").is("3.0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of a folder that has both likes and stars")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFolderThatHasLikedAndStars() throws Exception
    {
        FolderModel folder = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(folder).rateStarsToDocument(3);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).likeDocument();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getFiveStarRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("fiveStar")
                .getAggregate().assertThat().field("numberOfRatings").is("1")
                .assertThat().field("average").is("3.0");

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getLikeRating();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModel.assertThat().field("id").is("likes")
                .assertThat().field("myRating").is("true");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of a folder that has no ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfFolderThatHasNoRatings() throws Exception
    {
        FolderModel folder = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getFiveStarRating();
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getLikeRating();
        restRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of a folder after rating was deleted")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getDeletedRatingOfAFolder() throws Exception
    {
        FolderModel folder = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(folder).rateStarsToDocument(3);
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(folder).deleteFiveStarRating();
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).likeDocument();
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).deleteLikeRating();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").getAggregate().assertThat().field("numberOfRatings").is("0")
                .assertThat().field("average").isNull();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(folder).getLikeRating();
        restRatingModel.assertThat().field("id").is("likes").assertThat().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of a file after rating was deleted")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getDeletedRatingOfAFile() throws Exception
    {
        FileModel file = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).rateStarsToDocument(3);
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).deleteFiveStarRating();
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).likeDocument();
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).deleteLikeRating();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").getAggregate().assertThat().field("numberOfRatings").is("0")
                .assertThat().field("average").isNull();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).getLikeRating();
        restRatingModel.assertThat().field("id").is("likes").assertThat().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of another user as admin")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfAnotherUserAsAdmin() throws Exception
    {
        FileModel file = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).rateStarsToDocument(3);
        restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).likeDocument();

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1").assertThat().field("average").is("3.0");

        restRatingModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).getLikeRating();
        restRatingModel.assertThat().field("id").is("likes").assertThat().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get rating of admin with another user")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingOfAdminWithAnotherUser() throws Exception
    {
        FileModel file = dataContent.usingUser(managerUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).rateStarsToDocument(3);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).likeDocument();

        restRatingModel = restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1").assertThat().field("average").is("3.0");

        restRatingModel = restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).getLikeRating();
        restRatingModel.assertThat().field("id").is("likes").assertThat().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, description = "Get five star rating")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getFiveStarRating() throws Exception
    {
        FileModel file = dataContent.usingUser(managerUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).rateStarsToDocument(5);

        restRatingModel = restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1").assertThat().field("average").is("5.0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION, description = "Get one star rating")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getOneStarRating() throws Exception
    {
        FileModel file = dataContent.usingUser(managerUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).rateStarsToDocument(1);

        restRatingModel = restClient.authenticateUser(managerUser).withCoreAPI().usingResource(file).getFiveStarRating();
        restRatingModel.assertThat().field("id").is("fiveStar").and().field("myRating").isNull()
                .getAggregate().assertThat().field("numberOfRatings").is("1").assertThat().field("average").is("1.0");
    }
}