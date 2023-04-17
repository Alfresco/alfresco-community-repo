package org.alfresco.rest.tags;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.fail;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/4/2016.
 */
@Test(groups = {TestGroup.REQUIRE_SOLR})
public class UpdateTagTests extends TagsDataPrep
{
    private RestTagModel oldTag;
    private String randomTag = "";

    @BeforeMethod(alwaysRun=true)
    public void addTagToDocument()
    {
        restClient.authenticateUser(adminUserModel);
        oldTag = restClient.withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("old").toLowerCase());
        randomTag = RandomData.getRandomName("tag").toLowerCase();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Verify Admin user updates tags and status code is 200")
    @Bug(id="REPO-1828")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void adminIsAbleToUpdateTags()
    {
        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(randomTag);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify Manager user can't update tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void managerIsNotAbleToUpdateTag()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Collaborator user can't update tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorIsNotAbleToUpdateTagCheckDefaultErrorModelSchema()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Verify Contributor user can't update tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsNotAbleToUpdateTag()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.SANITY, description = "Verify Consumer user can't update tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToUpdateTag()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.SANITY, description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToUpdateTagIfAuthenticationFails()
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        String managerPassword = siteManager.getPassword();
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager);
        restClient.withCoreAPI().usingTag(oldTag).update(randomTag);
        siteManager.setPassword(managerPassword);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to update tag with invalid id")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsNotAbleToUpdateTagWithInvalidId()
    {
        String invalidTagId = "invalid-id";
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag").toLowerCase());
        tag.setId(invalidTagId);
        restClient.withCoreAPI().usingTag(tag).update(RandomData.getRandomName("tag").toLowerCase());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, invalidTagId));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to update tag with empty id")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsNotAbleToUpdateTagWithEmptyId()
    {
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag").toLowerCase());
        tag.setId("");
        restClient.withCoreAPI().usingTag(tag).update(RandomData.getRandomName("tag").toLowerCase());
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError().containsSummary(RestErrorModel.PUT_EMPTY_ARGUMENT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify admin is not able to update tag with invalid body")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsNotAbleToUpdateTagWithEmptyBody()
    {
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag").toLowerCase());
        restClient.withCoreAPI().usingTag(tag).update("");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.BLANK_TAG);
    }

    @Bug(id="ACE-5629")
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is not able to update tag with invalid body containing '|' symbol")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Ignore
    public void adminIsNotAbleToUpdateTagWithInvalidBodyScenario1()
    {
        String invalidTagBody = "|.\"/<>*";
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag").toLowerCase());
        try
        {
            Utility.sleep(500, 20000, () ->
            {
                restClient.withCoreAPI().usingTag(tag).update(invalidTagBody);
                restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                          .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_TAG, invalidTagBody));
            });
        }
        catch (InterruptedException e)
        {
            fail("Test interrupted while waiting for error status code.");
        }
    }

    @Bug(id="ACE-5629")
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin is not able to update tag with invalid body without '|' symbol")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Ignore
    public void adminIsNotAbleToUpdateTagWithInvalidBodyScenario2()
    {
        String invalidTagBody = ".\"/<>*";
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(document).addTag(RandomData.getRandomName("tag"));
        try
        {
            Utility.sleep(500, 20000, () ->
            {
                restClient.withCoreAPI().usingTag(tag).update(invalidTagBody);
                    restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                    .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_TAG, invalidTagBody));
            });
        }
        catch (InterruptedException e)
        {
            fail("Test interrupted while waiting for error status code.");
        }
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user can provide large string for new tag value.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    @Ignore
    public void adminIsAbleToUpdateTagsProvideLargeStringTag()
    {
        String largeStringTag = RandomStringUtils.randomAlphanumeric(10000).toLowerCase();

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingTag(oldTag).update(largeStringTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(largeStringTag);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user can provide short string for new tag value.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    public void adminIsAbleToUpdateTagsProvideShortStringTag()
    {
        String shortStringTag = RandomStringUtils.randomAlphanumeric(2).toLowerCase();

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingTag(oldTag).update(shortStringTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(shortStringTag);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user can provide string with special chars for new tag value.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    @Ignore
    public void adminIsAbleToUpdateTagsProvideSpecialCharsStringTag()
    {
        String specialCharsString = RandomData.getRandomName("!@#$%^&*()'\".,<>-_+=|\\").toLowerCase();

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingTag(oldTag).update(specialCharsString);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(specialCharsString);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user can provide existing tag for new tag value.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    @Ignore
    public void adminIsAbleToUpdateTagsProvideExistingTag()
    {
        String existingTag = RandomData.getRandomName("oldTag").toLowerCase();
        RestTagModel oldExistingTag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).addTag(existingTag);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingTag(oldExistingTag).update(existingTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(existingTag);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user can delete a tag, add tag and update it.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    @Ignore
    public void adminDeleteTagAddTagUpdateTag()
    {
        restClient.authenticateUser(adminUserModel)
                .withCoreAPI().usingResource(document).deleteTag(oldTag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        String newTag = RandomData.getRandomName("addTag").toLowerCase();
        RestTagModel newTagModel = restClient.withCoreAPI().usingResource(document).addTag(newTag);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        returnedModel = restClient.withCoreAPI().usingTag(newTagModel).update(newTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(newTag);
        returnedModel.assertThat().field("id").isNotNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user can update a tag, delete tag and add it.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    @Bug(id="REPO-1828")
    @Ignore
    public void adminUpdateTagDeleteTagAddTag()
    {
        String newTag = RandomData.getRandomName("addTag").toLowerCase();

        returnedModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingTag(oldTag).update(newTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedModel.assertThat().field("tag").is(newTag);
        returnedModel.assertThat().field("id").isNotNull();

        restClient.withCoreAPI().usingResource(document).deleteTag(returnedModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withCoreAPI().usingResource(document).addTag(newTag);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
        description = "Verify Admin user updates orphan tags and status code is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void adminIsAbleToUpdateOrphanTag()
    {
        STEP("Update orphan tag and expect 200");
        final String newTagName = RandomData.getRandomName("new").toLowerCase();
        returnedModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingTag(orphanTag).update(newTagName);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestTagModel expected = RestTagModel.builder().id(orphanTag.getId()).tag(newTagName).create();
        returnedModel.assertThat().isEqualTo(expected);
    }

    @Test (groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void canUpdateTagAndGetCount()
    {
        STEP("Create an orphaned tag");
        String tagName = RandomData.getRandomName("tag").toLowerCase();
        RestTagModel createdTag = RestTagModel.builder().tag(tagName).create();
        RestTagModel tag = restClient.authenticateUser(adminUserModel).withCoreAPI().createSingleTag(createdTag);

        STEP("Update tag and request the count field");
        String newTagName = RandomData.getRandomName("new").toLowerCase();
        returnedModel = restClient.authenticateUser(adminUserModel).withCoreAPI().include("count").usingTag(tag).update(newTagName);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestTagModel expected = RestTagModel.builder().id(tag.getId()).tag(newTagName).count(0).create();
        returnedModel.assertThat().isEqualTo(expected);
    }
}
