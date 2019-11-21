package org.alfresco.rest.tags;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class GetTagsTests extends TagsDataPrep
{

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        init();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Verify user with Manager role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void getTagsWithManagerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithCollaboratorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithContributorRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithConsumerRole() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Failed authentication get tags call returns status code 401 with Manager role")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void failedAuthenticationReturnsUnauthorizedStatus() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        userModel = dataUser.createRandomTestUser();
        userModel.setPassword("user wrong password");
        dataUser.addUserToSite(userModel, siteModel, UserRole.SiteManager);
        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if maxItems is invalid status code returned is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void maxItemsInvalidValueTest() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=abc").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if skipCount is invalid status code returned is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void skipCountInvalidValueTest() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("skipCount=abc").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that file tag is retrieved")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void fileTagIsRetrieved() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that folder tag is retrieved")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void folderTagIsRetrieved() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", folderTagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify site Manager is able to get tags using properties parameter."
                    + "Check that properties filter is applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteManagerIsAbleToRetrieveTagsWithPropertiesParameter() throws Exception
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("maxItems=5000&properties=tag").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase())
                .and().entriesListDoesNotContain("id");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get tags and use skipCount parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useSkipCountCheckPagination() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel).withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        RestTagModel firstTag = returnedCollection.getEntries().get(0).onModel();
        RestTagModel secondTag = returnedCollection.getEntries().get(1).onModel();
        RestTagModelsCollection tagsWithSkipCount = restClient.withParams("skipCount=2").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        tagsWithSkipCount.assertThat().entriesListDoesNotContain("tag", firstTag.getTag())
                .assertThat().entriesListDoesNotContain("tag", secondTag.getTag());
        tagsWithSkipCount.assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get tags and use maxItems parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useMaxItemsParameterCheckPagination() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel).withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        RestTagModel firstTag = returnedCollection.getEntries().get(0).onModel();
        RestTagModel secondTag = returnedCollection.getEntries().get(1).onModel();
        RestTagModelsCollection tagsWithMaxItems = restClient.withParams("maxItems=2").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        tagsWithMaxItems.assertThat().entriesListContains("tag", firstTag.getTag())
                .assertThat().entriesListContains("tag", secondTag.getTag())
                .assertThat().entriesListCountIs(2);
        tagsWithMaxItems.assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With manager get tags and use high skipCount parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useHighSkipCountCheckPagination() throws Exception
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("skipCount=20000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListIsEmpty()
                .getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("count").is("0")
                .and().field("skipCount").is(20000)
                .and().field("totalItems").isNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With Collaborator user get tags and use maxItems with value zero. Check default error model schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useMaxItemsWithValueZeroCheckDefaultErrorModelSchema() throws Exception
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .withParams("maxItems=0").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With Manager user delete tag. Check it is not retrieved anymore.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void checkThatDeletedTagIsNotRetrievedAnymore() throws Exception
    {
        String removedTag = RandomData.getRandomName("tag3");

        RestTagModel deletedTag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).addTag(removedTag);

        restClient.withCoreAPI().usingResource(document).deleteTag(deletedTag);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListDoesNotContain("tag", removedTag.toLowerCase());
    }
}
