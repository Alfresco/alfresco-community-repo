package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestFavoriteSiteModel;
import org.alfresco.rest.model.RestPersonFavoritesModel;
import org.alfresco.rest.model.RestPersonFavoritesModelsCollection;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.LinkModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddFavoriteSiteTests extends RestTest
{
    private UserModel userModel, adminUser;
    private SiteModel publicSite, privateSite, moderatedSite;
    private FileModel document;
    private FolderModel folder;
    private RestFavoriteSiteModel restFavoriteSiteModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        
        publicSite = dataSite.usingUser(userModel).createPublicRandomSite();
        privateSite = dataSite.usingUser(userModel).createPrivateRandomSite();
        moderatedSite = dataSite.usingUser(userModel).createModeratedRandomSite();

        document = dataContent.usingSite(publicSite).usingUser(adminUser).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folder = dataContent.usingSite(publicSite).usingUser(adminUser).createFolder();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify manager user add a favorite site with Rest API and response is successful (201)")
    public void managerUserAddFavoriteSiteWithSuccess() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, publicSite, UserRole.SiteManager);

        restFavoriteSiteModel = restClient.authenticateUser(managerUser).withCoreAPI().usingUser(managerUser).addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());

        restClient.withCoreAPI().usingUser(managerUser).addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT);
        restClient.assertLastError().containsSummary(String.format("%s is already a favourite site", publicSite.getId()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify a manager user is NOT Authorized to add a favorite site with Rest API when authentication fails (401)")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void managerUserNotAuthorizedFailsToAddFavoriteSite() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, publicSite, UserRole.SiteManager);
        managerUser.setPassword("newpassword");

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify collaborator user add a favorite site with Rest API and response is successful (201)")
    public void collaboratorUserAddFavoriteSiteWithSuccess() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, publicSite, UserRole.SiteCollaborator);

        restFavoriteSiteModel = restClient.authenticateUser(collaboratorUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify contributor user add a favorite site with Rest API and response is successful (201)")
    public void contributorUserAddFavoriteSiteWithSuccess() throws Exception
    {
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, publicSite, UserRole.SiteContributor);

        restFavoriteSiteModel = restClient.authenticateUser(contributorUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify consumer user add a favorite site with Rest API and response is successful (201)")
    public void consumerUserAddFavoriteSiteWithSuccess() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, publicSite, UserRole.SiteConsumer);

        restFavoriteSiteModel = restClient.authenticateUser(consumerUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify admin user add a favorite site with Rest API and response is successful (201)")
    public void adminUserAddFavoriteSiteWithSuccess() throws Exception
    {
        restFavoriteSiteModel = restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager user is not able to add a favorite site when the site is already favorite - status code (409)")
    public void managerUserAddFavoriteSiteAlreadyAFavoriteSite() throws Exception
    {
        publicSite = dataSite.usingUser(userModel).createPublicRandomSite();
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, publicSite, UserRole.SiteManager);

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withCoreAPI().usingUser(managerUser).addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT);
        restClient.assertLastError()
                .containsSummary(String.format("%s is already a favourite site", publicSite.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(String.format("Site %s is already a favourite site", publicSite.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user doesn't have permission to delete favorites of admin user with Rest API and status code is 403")
    public void userIsNotAbleToAddFavoriteSiteOfAnotherUser() throws Exception
    {
        publicSite = dataSite.usingUser(userModel).createPublicRandomSite();
        UserModel contributorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(contributorUser, publicSite, UserRole.SiteContributor);
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, publicSite, UserRole.SiteConsumer);

        restClient.authenticateUser(consumerUser).withCoreAPI()
                .usingUser(contributorUser).addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager user is able to add as favorite a private site.")
    public void managerAddsPrivateSiteAsFavorite() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, privateSite, UserRole.SiteManager);

        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(managerUser).addFavoriteSite(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestPersonFavoritesModelsCollection favorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        favorites.assertThat().entriesListContains("targetGuid", privateSite.getGuidWithoutVersion())
                .and().paginationField("totalItems").is("1");
        favorites.getOneRandomEntry().onModel().getTarget().getSite()
                .assertThat()
                .field("description").is(privateSite.getDescription())
                .and().field("id").is(privateSite.getId())
                .and().field("visibility").is(privateSite.getVisibility().toString())
                .and().field("title").is(privateSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager user is able to add as favorite a moderated site.")
    public void managerAddsModeratedSiteAsFavorite() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, moderatedSite, UserRole.SiteManager);

        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(managerUser).addFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        RestPersonFavoritesModelsCollection favorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        favorites.assertThat().entriesListContains("targetGuid", moderatedSite.getGuidWithoutVersion())
                .and().paginationField("totalItems").is("1");
        favorites.getOneRandomEntry().onModel().getTarget().getSite()
                .assertThat()
                .field("description").is(moderatedSite.getDescription())
                .and().field("id").is(moderatedSite.getId())
                .and().field("visibility").is(moderatedSite.getVisibility().toString())
                .and().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user removes a site from favorites using '-me-' in place of personId with Rest API and response is successful (201)")
    public void addFavoriteSiteWithSuccessUsingMeAsPersonId() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingMe().addFavoriteSite(moderatedSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager user removes a site from its favorites and adds it again and response is successful (204)")
    public void managerUserRemovesFavoriteSiteAndAddItAgain() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, publicSite, UserRole.SiteManager);
        dataSite.usingUser(managerUser).usingSite(publicSite).addSiteToFavorites();

        restClient.authenticateUser(managerUser).withCoreAPI().usingAuthUser().removeFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingUser(managerUser).addSiteToFavorites(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Bug(id="ACE-2413")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify empty user is not able to add a site from favorites and response is (400)")
    public void emptyUserIsNotAbleToRemoveFavoriteSite() throws Exception
    {
        UserModel emptyUser = new UserModel("", "password");
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(emptyUser).addFavoriteSite(publicSite);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "emptyUser"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent user is not able to add a site from favorites and response is (404)")
    public void inexistentUserIsNotAbleToRemoveFavoriteSite() throws Exception
    {
        UserModel inexistentUser = new UserModel("inexistentUser", "password");
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(inexistentUser).addFavoriteSite(publicSite);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "inexistentUser"))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id="REPO-1827")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to add a favorite site when the 'id' is empty - status code (400)")
    public void userAddFavoriteSiteWithEmptySiteId() throws Exception
    {
        publicSite.setId("");
        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError()
                .containsSummary("siteId is null")
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't have permission to add a favorite site of another user with Rest API and status code is 403")
    public void adminIsNotAbleToAddFavoriteSiteOfAnotherUser() throws Exception
    {
        UserModel collaboratorUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(collaboratorUser, publicSite, UserRole.SiteCollaborator);
        restClient.authenticateUser(collaboratorUser)
                .withCoreAPI().usingUser(adminUser).addFavoriteSite(publicSite);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="MNT-17338")
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user doesn't have permission to add a favorite site of another user with Rest API and status code is 403")
    public void userIsNotAbleToAddFavoriteSiteOfAdmin() throws Exception
    {
        UserModel consumerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(consumerUser, publicSite, UserRole.SiteConsumer);
        restClient.authenticateUser(adminUser)
                .withCoreAPI().usingUser(consumerUser).addFavoriteSite(publicSite);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify add favorite site using empty body - status code is 400")
    public void addFavoriteSiteWithEmptyBody() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"ids\": \"local\"}" ,
                "people/{personId}/favorite-sites", adminUser.getUsername());
        restClient.processModel(RestPersonFavoritesModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field " + "\"ids\""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify add favorite site using empty body - status code is 400")
    public void addFavoriteSiteWithEmptyRequiredFieldsBody() throws Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"\": \"\"}",
                "people/{personId}/favorite-sites", adminUser.getUsername());
        restClient.processModel(RestPersonFavoritesModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field " + "\"\""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify non member user is not able to add a private favorite site - status code is 404")
    public void userAddFavoriteSiteUserNotMemberOfPrivateSite() throws Exception
    {
        UserModel user = dataUser.usingAdmin().createRandomTestUser();
        restClient.authenticateUser(user).withCoreAPI()
                .usingAuthUser().addFavoriteSite(privateSite);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, privateSite.getId()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify non member user is able to add a moderated favorite site")
    public void userAddFavoriteSiteUserNotMemberOfModeratedSite() throws Exception
    {
        UserModel user = dataUser.usingAdmin().createRandomTestUser();
        restClient.authenticateUser(user).withCoreAPI().usingAuthUser()
                .addFavoriteSite(moderatedSite);

        RestPersonFavoritesModelsCollection favorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        favorites.assertThat().entriesListContains("targetGuid", moderatedSite.getGuidWithoutVersion())
                .and().paginationField("totalItems").is("1");
        favorites.getOneRandomEntry().onModel().getTarget().getSite()
                .assertThat()
                .field("description").is(moderatedSite.getDescription())
                .and().field("id").is(moderatedSite.getId())
                .and().field("visibility").is(moderatedSite.getVisibility().toString())
                .and().field("title").is(moderatedSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Verify non member user is able to add a public favorite site")
    public void userAddFavoriteSiteUserNotMemberOfPublicSite() throws Exception
    {
        UserModel user = dataUser.usingAdmin().createRandomTestUser();
        restFavoriteSiteModel =  restClient.authenticateUser(user).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restFavoriteSiteModel.assertThat().field("id").is(publicSite.getId());

        RestPersonFavoritesModelsCollection favorites = restClient.withCoreAPI().usingAuthUser().getFavorites();
        favorites.assertThat().entriesListContains("targetGuid", publicSite.getGuidWithoutVersion())
                .and().paginationField("totalItems").is("1");
        favorites.getOneRandomEntry().onModel().getTarget().getSite()
                .assertThat()
                .field("description").is(publicSite.getDescription())
                .and().field("id").is(publicSite.getId())
                .and().field("visibility").is(publicSite.getVisibility().toString())
                .and().field("title").is(publicSite.getTitle());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Check that if id does not exist, status code is 404")
    public void addFavoriteSiteUsingInvalidId() throws Exception
    {
        SiteModel site = dataSite.usingUser(adminUser).createPublicRandomSite();
        String id = publicSite.getId();
        site.setId("invalidID");

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(site);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidID"));

        publicSite.setId(id);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides site in id but id is of a file status code is 404")
    public void addSiteToFavoritesUsingFileId() throws Exception
    {
        String id = publicSite.getId();
        publicSite.setId(document.getNodeRefWithoutVersion());

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRefWithoutVersion()));

        publicSite.setId(id);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides site in id but id is of a folder status code is 404")
    public void addSiteToFavoritesUsingFolderId() throws Exception
    {
        String id = publicSite.getId();
        publicSite.setId(folder.getNodeRefWithoutVersion());

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, folder.getNodeRefWithoutVersion()));

        publicSite.setId(id);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides site in id but id is of a comment status code is 404")
    public void addSiteToFavoriteUsingCommentId() throws Exception
    {
        publicSite = dataSite.usingUser(userModel).createPublicRandomSite();
        String id = publicSite.getId();
        FileModel file = dataContent.usingSite(publicSite).usingUser(adminUser).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestCommentModel comment = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).addComment("This is a comment");
        file.setNodeRef(comment.getId());
        publicSite.setId(comment.getId());

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        publicSite.setId(id);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides site in id but id is of a tag status code is 404")
    public void addSiteFavoriteUsingTagId() throws Exception
    {
        FileModel file = dataContent.usingSite(publicSite).usingUser(adminUser).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel returnedModel = restClient.authenticateUser(adminUser).withCoreAPI().usingResource(file).addTag("random_tag");
        file.setNodeRef(returnedModel.getId());
        publicSite.setId(returnedModel.getId());

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(publicSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, returnedModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if id does not describe a site, file, or folder status code is 400")
    public void addFavoriteUsingInvalidGuid() throws Exception
    {
        LinkModel link = dataLink.usingAdmin().usingSite(publicSite).createRandomLink();
        SiteModel site = dataSite.usingUser(adminUser).createPublicRandomSite();
        site.setId(link.getNodeRef());

        restClient.authenticateUser(adminUser).withCoreAPI().usingAuthUser().addFavoriteSite(site);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, site.getId().split("/")[3]));
    }
}
