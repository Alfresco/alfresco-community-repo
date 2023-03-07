package org.alfresco.rest.favorites;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.*;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddFavoritesTests extends RestTest
{
    private UserModel adminUserModel;
    private SiteModel siteModel;
    private ListUserWithRoles usersWithRoles;
    private RestPersonFavoritesModel restPersonFavoritesModel;
    private FileModel document, document1;
    private FolderModel folder, folder1;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();        
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);

        document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folder = dataContent.usingSite(siteModel).usingUser(adminUserModel).createFolder();
        document1 = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folder1 = dataContent.usingSite(siteModel).usingUser(adminUserModel).createFolder();
    }

    //    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsNotAbleToAddToFavoritesIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
              description = "Verify Collaborator user add site to favorites with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.and().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
              description = "Verify Contributor user add site to favorites with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToAddToFavorites() throws JsonToModelConversionException, Exception
    {
        restPersonFavoritesModel= restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.and().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
              description = "Verify Consumer user add site to favorites with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToAddToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                  .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.and().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user add site to favorites with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void adminIsAbleToAddToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());
    }

    @Bug(id = "MNT-17157")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if a favorite already exists with the specified id status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFavoriteTwice() throws Exception
    {
        SiteModel site = dataSite.usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).createPublicRandomSite();
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(site);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withCoreAPI().usingAuthUser().addSiteToFavorites(site);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Add to favorites a file for a Manager, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToAddFileToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingAuthUser().addFileToFavorites(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(document.getNodeRefWithoutVersion());

        RestFileModel restFileModel = restPersonFavoritesModel.getTarget().getFile();
        restFileModel.assertThat().field("mimeType").is("text/plain").and()
                .field("isFile").is("true").and()
                .field("isFolder").is("false").and()
                .field("createdBy").is(adminUserModel.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Add to favorites a folder for a Manager, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToAddFolderToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingAuthUser().addFolderToFavorites(folder);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(folder.getNodeRefWithoutVersion());

        RestFolderModel restFolderModel = restPersonFavoritesModel.getTarget().getFolder();
        restFolderModel.assertThat().field("isFile").is("false").and()
                .field("isFolder").is("true").and()
                .field("createdBy").is(adminUserModel.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Add to favorites a site for a Manager, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.SANITY })
    public void managerIsAbleToAddSiteToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());

        RestSiteModel restSiteModel = restPersonFavoritesModel.getTarget().getSite();
        restSiteModel.assertThat().field("visibility").is(siteModel.getVisibility()).and()
                .field("guid").is(siteModel.getGuid()).and()
                .field("description").is(siteModel.getDescription()).and()
                .field("id").is(siteModel.getId()).and()
                .field("title").is(siteModel.getTitle());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify add favorite specifying -me- string in place of <personid> for request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void userIsAbleToAddFavoriteWhenUsingMeAsUsername() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        restPersonFavoritesModel = restClient.authenticateUser(user)
                .withCoreAPI().usingMe().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());

        restClient.authenticateUser(user).withCoreAPI().usingAuthUser().getFavoriteSites().assertThat().entriesListContains("guid", siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a file for a Collaborator, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddFileToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingAuthUser().addFileToFavorites(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(document.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a file for a Contributor, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToAddFileToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingAuthUser().addFileToFavorites(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(document.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a file for a Consumer, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToAddFileToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingAuthUser().addFileToFavorites(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(document.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a folder for a Collaborator, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddFolderToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).addFolderToFavorites(folder);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(folder.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a folder for a Contributor, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToAddFolderToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingAuthUser().addFolderToFavorites(folder);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(folder.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a folder for a Consumer, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToAddFolderToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).addFolderToFavorites(folder);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(folder.getNodeRefWithoutVersion());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a site for a Collaborator, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddSiteToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a site for a Contributor, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void contributorIsAbleToAddSiteToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Add to favorites a site for a Consumer, check it was added")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void consumerIsAbleToAddSiteToFavorites() throws Exception
    {
        restPersonFavoritesModel = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(siteModel.getGuid());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if target guid does not describe a site, file, or folder status code is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFavoriteUsingInvalidGuid() throws Exception
    {
        LinkModel link = dataLink.usingAdmin().usingSite(siteModel).createRandomLink();
        SiteModel site = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        site.setGuid(link.getNodeRef());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(site);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, site.getGuid().split("/")[3]));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if personId does not exist, status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFavoriteUsingInexistentUser() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI().usingUser(new UserModel("random_user", "random_password")).addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "random_user"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if target guid does not exist, status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFavoriteUsingInexistentGuid() throws Exception
    {
        SiteModel site = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        site.setGuid("random_guid");

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(site);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "random_guid"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides file in target but guid is of a folder status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFileToFavoritesUsingFolderGuid() throws Exception
    {
        String nodeRef = document.getNodeRef();
        document.setNodeRef(folder.getNodeRef());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(document);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                adminUserModel.getUsername(), folder.getNodeRefWithoutVersion()));

        document.setNodeRef(nodeRef);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify add favorite, perform getFavorites call, check value is updated")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyGetFavoritesAfterFavoritingSite() throws Exception
    {
        UserModel user = dataUser.createRandomTestUser();
        restPersonFavoritesModel = restClient.authenticateUser(user)
                .withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(user).withCoreAPI().usingAuthUser().getFavoriteSites()
                .assertThat().entriesListContains("guid", siteModel.getGuid());
    }

    //    @Bug(id = "MNT-17158", description="Not a bug, If we need to stop comments from being favorited then an improvement/enhancement request should be raised against the platform.")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify add file favorite with comment id returns status code 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFileFavoriteUsingCommentId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestCommentModel comment = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addComment("This is a comment");
        file.setNodeRef(comment.getId());

        restPersonFavoritesModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(file);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(file.getNodeRef());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify add file favorite with tag id returns status code 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFileFavoriteUsingTagId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel returnedModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addTag("random_tag");
        file.setNodeRef(returnedModel.getId());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFileToFavorites(file);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                adminUserModel.getUsername(), returnedModel.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides site in target but id is of a file status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addSiteToFavoritesUsingFileId() throws Exception
    {
        String guid = siteModel.getGuid();
        siteModel.setGuid(document.getNodeRefWithoutVersion());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, document.getNodeRefWithoutVersion()));

        siteModel.setGuid(guid);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides folder in target but guid is of a file status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFolderToFavoritesUsingFileGuid() throws Exception
    {
        String nodeRef = folder1.getNodeRef();
        folder1.setNodeRef(document1.getNodeRef());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFolderToFavorites(folder1);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(document1.getNodeRef());

        folder1.setNodeRef(nodeRef);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides file in target but guid is of a site status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addSiteToFavoritesUsingFolderId() throws Exception
    {
        SiteModel newSiteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        newSiteModel.setGuid(folder.getNodeRef());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addSiteToFavorites(newSiteModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, folder.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user provides folder in target but guid is of a site status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void addFolderToFavoritesUsingSiteId() throws Exception
    {
        FolderModel newFolder = dataContent.usingSite(siteModel).usingUser(adminUserModel).createFolder();
        newFolder.setNodeRef(siteModel.getGuid());

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingAuthUser().addFolderToFavorites(newFolder);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.RELATIONSHIP_NOT_FOUND,
                adminUserModel.getUsername(), siteModel.getGuid()));
    }

    @Bug(id = "REPO-1061")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Check that if user does not have permission to favorite a site status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyFavoriteASiteIfTheUserDoesNotHavePermission() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(adminUserModel).createPrivateRandomSite();
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingAuthUser().addSiteToFavorites(privateSite);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, privateSite.getGuid()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .containsErrorKey(RestErrorModel.NOT_FOUND_ERRORKEY)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-4000")
    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify the response of favorite a sie with empty body at request")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION })
    public void verifyFavoriteASiteWithEmptyBody() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withCoreAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "", "people/{personId}/favorites", adminUserModel.getUsername());
        restClient.processModel(RestPersonFavoritesModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "No content to map due to end-of-input"));
    }
}
