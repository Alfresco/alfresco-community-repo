package org.alfresco.rest.ratings;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestRatingModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
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

public class GetRatingsTests extends RestTest
{
    private SiteModel siteModel;
    private UserModel adminUser, userModel;    
    private ListUserWithRoles usersWithRoles;
    private RestRatingModelsCollection restRatingModelsCollection;
    private FileModel document;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();

        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Manager is able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
    public void managerIsAbleToRetrieveDocumentRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(adminUser).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModelsCollection = restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModelsCollection.assertNodeHasFiveStarRating().assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Collaborator is able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToRetrieveDocumentRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(adminUser).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModelsCollection = restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModelsCollection.assertNodeHasFiveStarRating().assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Contributor is able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void contributorIsAbleToRetrieveDocumentRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(adminUser).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModelsCollection = restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModelsCollection.assertNodeHasFiveStarRating().assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Consumer is able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void consumerIsAbleToRetrieveDocumentRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(adminUser).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));

        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModelsCollection = restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModelsCollection.assertNodeHasFiveStarRating().assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.REGRESSION, description = "Admin user is able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveDocumentRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(folderModel)
                .createContent(DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restRatingModelsCollection = restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restRatingModelsCollection.assertNodeHasFiveStarRating().assertNodeIsLiked();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS },
            executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to retrieve document ratings")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToRetrieveRatings() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUser).usingSite(siteModel).createFolder();
        document = dataContent.usingUser(adminUser).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
        
        restClient.authenticateUser(adminUser);
        restClient.withCoreAPI().usingResource(document).likeDocument();
        restClient.withCoreAPI().usingResource(document).rateStarsToDocument(5);

        restClient.authenticateUser(new UserModel("random user", "random password"));

        restClient.withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check that rating for invalid maxItems status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkInvalidMaxItemsStatusCode() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restClient.authenticateUser(adminUser).withParams("maxItems=0").withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Only positive values supported for maxItems");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check that rating for invalid skipCount status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkInvalidSkipCountStatusCode() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restClient.authenticateUser(adminUser).withParams("skipCount=AB").withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Invalid paging parameter skipCount:AB");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "If nodeId does not exist status code is 404 when a document is liked")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void addLikeUsingInvalidNodeId() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        document.setNodeRef(RandomStringUtils.randomAlphanumeric(20));

        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getRatings();

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check that rating value is TRUE for a like rating")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkRatingValueIsTrueForLikedDoc() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restRatingModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restRatingModelsCollection.assertThat().entriesListContains("myRating", "true")
                .assertThat().entriesListContains("id", "likes");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check that rating value is an INTEGER value for stars rating")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkRatingValueIsIntegerForStarsRating() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restRatingModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        restRatingModelsCollection.assertThat().entriesListContains("myRating", "5")
                .assertThat().entriesListContains("id", "fiveStar");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check default error schema in case of failure")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkDefaultErrorSchema() throws Exception
    {
        FileModel document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        document.setNodeRef("abc");
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "abc"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check maxItems and skipCount parameters")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkMaxItemsAndSkipCountParameters() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restRatingModelsCollection = restClient.authenticateUser(adminUser).withParams("maxItems=1", "skipCount=1").withCoreAPI().usingResource(document).getRatings();
        restRatingModelsCollection.assertThat().entriesListCountIs(1);
        restRatingModelsCollection.getPagination().assertThat().field("maxItems").is("1")
                .and().field("skipCount").is("1");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check totalItems and hasMoreitems parameters")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void checkTotalItemsAndHasMoreItemsParameters() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restRatingModelsCollection = restClient.authenticateUser(adminUser).withParams("maxItems=1").withCoreAPI().usingResource(document).getRatings();
        restRatingModelsCollection.assertThat().entriesListCountIs(1);
        restRatingModelsCollection.getPagination().assertThat().field("hasMoreItems").is("true")
                .and().field("count").is("1").and().field("maxItems").is("1");
    }

    @Bug(id = "REPO-1831")
    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get ratings for a document to which authenticated user does not have access")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void userIsNotAbleToGetRatingsOfDocumentToWhichItHasNoAccess() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FileModel file = dataContent.usingSite(privateSite).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restRatingModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingResource(file).getRatings();
        restRatingModelsCollection.assertThat().entriesListIsEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Check high value for skipCount parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingsUsingHighValueForSkipCount() throws Exception
    {
        document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).rateStarsToDocument(5);
        restClient.authenticateUser(userModel).withCoreAPI().usingNode(document).likeDocument();
        
        restRatingModelsCollection = restClient.authenticateUser(adminUser).withParams("skipCount=100").withCoreAPI().usingResource(document).getRatings();
        restRatingModelsCollection.getPagination().assertThat().field("skipCount").is("100");
        restRatingModelsCollection.assertThat().entriesListIsEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.RATINGS }, executionType = ExecutionType.REGRESSION,
            description = "Get ratings using site id instead of node id")
    @Test(groups = { TestGroup.REST_API, TestGroup.RATINGS, TestGroup.REGRESSION })
    public void getRatingsUsingSiteId() throws Exception
    {
        FileModel document = dataContent.usingSite(siteModel).usingAdmin().createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        document.setNodeRef(siteModel.getId());
        restClient.authenticateUser(adminUser).withCoreAPI().usingResource(document).getRatings();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, siteModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}