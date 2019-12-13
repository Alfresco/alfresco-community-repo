package org.alfresco.rest.tags.nodes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.tags.TagsDataPrep;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/4/2016.
 */
@Test(groups = {TestGroup.REQUIRE_SOLR})
public class DeleteTagTests extends TagsDataPrep
{
    private RestTagModel tag;
    private FileModel contributorDoc;

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify Admin user deletes tags with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsAbleToDeleteTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));
        
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingResource(document).getNodeTags()
                .assertThat().entriesListDoesNotContain("tag", tag.getTag());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user deletes tags created by admin user with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void managerIsAbleToDeleteTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));
        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Collaborator user deletes tags created by admin user with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToDeleteTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user can't delete tags created by admin user with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsNotAbleToDeleteTagsForAnotherUserContent() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user deletes tags created by him with Rest API and status code is 204")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsAbleToDeleteTagsForHisContent() throws Exception
    {
        userModel = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(userModel);
        contributorDoc = dataContent.usingSite(siteModel).usingUser(userModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);;
        tag = restClient.withCoreAPI().usingResource(contributorDoc).addTag(RandomData.getRandomName("tag"));
        
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer user can't delete tags created by admin user with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToDeleteTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToDeleteTagIfAuthenticationFails() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        String managerPassword = siteManager.getPassword();
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager);
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
        siteManager.setPassword(managerPassword);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if user has no permission to remove tag returned status code is 403. Check default error model schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void deleteTagWithUserWithoutPermissionCheckDefaultErrorModelSchema() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        RestTagModel tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        restClient.authenticateUser(dataUser.createRandomTestUser());
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if node does not exist returned status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void deleteTagForANonexistentNode() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        RestTagModel tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        FileModel document = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String nodeRef = RandomStringUtils.randomAlphanumeric(10);
        document.setNodeRef(nodeRef);
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nodeRef));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if tag does not exist returned status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void deleteTagThatDoesNotExist() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        RestTagModel tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        tag.setId("abc");
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if tag id is empty returned status code is 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void deleteTagWithEmptyId() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        RestTagModel tag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        tag.setId("");
        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that folder tag can be deleted")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void deleteFolderTag() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();;
        restClient.authenticateUser(adminUserModel);
        RestTagModel tag = restClient.withCoreAPI().usingResource(folderModel).addTag(RandomData.getRandomName("tag"));
        restClient.withCoreAPI().usingResource(folderModel).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withCoreAPI().usingResource(folderModel).getNodeTags()
                .assertThat().entriesListDoesNotContain("tag", tag.getTag());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Manager user can't delete deleted tag.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id = "ACE-5455")
    public void managerCannotDeleteDeletedTag() throws Exception
    {
        tag = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user can delete long tag.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void userCollaboratorCanDeleteLongTag() throws Exception
    {
        String longTag = RandomStringUtils.randomAlphanumeric(800);

        tag = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).addTag(longTag);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Manager user can delete short tag.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void managerCanDeleteShortTag() throws Exception
    {
        String shortTag = RandomStringUtils.randomAlphanumeric(10);

        tag = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).addTag(shortTag);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Admin can delete tag then add it again.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminRemovesTagAndAddsItAgain() throws Exception
    {
        String tagValue = RandomStringUtils.randomAlphanumeric(10);

        tag = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).addTag(tagValue);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        tag = restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).addTag(tagValue);
        RestTagModel returnedTag = restClient.withCoreAPI().getTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(tagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Manager user can delete tag added by another user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void managerCanDeleteTagAddedByAnotherUser() throws Exception
    {
        tag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withCoreAPI().usingResource(document).addTag(RandomStringUtils.randomAlphanumeric(10));

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).deleteTag(tag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }
}