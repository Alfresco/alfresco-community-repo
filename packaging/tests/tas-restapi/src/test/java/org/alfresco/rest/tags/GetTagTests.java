package org.alfresco.rest.tags;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class GetTagTests extends TagsDataPrep
{

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify admin user gets tag using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsAbleToGetTag() throws Exception
    {
        RestTagModel returnedTag = restClient.authenticateUser(adminUserModel).withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(documentTagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Verify user with Manager role gets tag using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void userWithManagerRoleIsAbleToGetTag() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));

        RestTagModel returnedTag = restClient.withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(documentTagValue.toLowerCase())
                   .assertThat().field("id").is(documentTag.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role gets tag using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void userWithCollaboratorRoleIsAbleToGetTag() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        RestTagModel returnedTag = restClient.withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(documentTagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role gets tag using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void userWithContributorRoleIsAbleToGetTag() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        RestTagModel returnedTag = restClient.withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(documentTagValue.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role gets tag using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void userWithConsumerRoleIsAbleToGetTag() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        RestTagModel returnedTag = restClient.withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(documentTagValue.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Verify Manager user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void managerIsNotAbleToGetTagIfAuthenticationFails() throws Exception
    {
        UserModel managerUser = dataUser.usingAdmin().createRandomTestUser();
        String managerPassword = managerUser.getPassword();
        dataUser.addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        managerUser.setPassword("wrongPassword");
        restClient.authenticateUser(managerUser).withCoreAPI().getTag(documentTag);
        managerUser.setPassword(managerPassword);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if tag id is invalid status code returned is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void invalidTagIdTest() throws Exception
    {
        String tagId = documentTag.getId();
        documentTag.setId("random_tag_value");
        restClient.authenticateUser(adminUserModel).withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "random_tag_value"));
        documentTag.setId(tagId);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Check that properties filter is applied when getting tag using Manager user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void checkPropertiesFilterIsApplied() throws Exception
    {
        RestTagModel returnedTag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("properties=id,tag").withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("id").is(documentTag.getId())
                .assertThat().field("tag").is(documentTag.getTag().toLowerCase())
                .assertThat().fieldsCount().is(2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Check that Manager user can get tag of a folder.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagOfAFolder() throws Exception
    {
        RestTagModel returnedTag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().getTag(folderTag);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedTag.assertThat().field("tag").is(folderTagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.REGRESSION, description = "Check default error model schema. Use invalid skipCount parameter.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void checkDefaultErrorModelSchema() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("skipCount=abc").withCoreAPI().getTag(documentTag);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"))
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}