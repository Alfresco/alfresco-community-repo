package org.alfresco.rest.tags;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.OK;

import java.util.Set;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class GetTagsTests extends TagsDataPrep
{

    private static final String FIELD_ID = "id";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COUNT = "count";

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Verify user with Manager role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void getTagsWithManagerRole()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Collaborator role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithCollaboratorRole()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Contributor role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithContributorRole()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify user with Consumer role gets tags using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithConsumerRole()
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, description = "Failed authentication get tags call returns status code 401 with Manager role")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void failedAuthenticationReturnsUnauthorizedStatus()
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
    public void maxItemsInvalidValueTest()
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=abc").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if skipCount is invalid status code returned is 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void skipCountInvalidValueTest()
    {
        restClient.authenticateUser(adminUserModel).withParams("skipCount=abc").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that file tag is retrieved")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void fileTagIsRetrieved()
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that folder tag is retrieved")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void folderTagIsRetrieved()
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", folderTagValue.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify site Manager is able to get tags using properties parameter."
                    + "Check that properties filter is applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteManagerIsAbleToRetrieveTagsWithPropertiesParameter()
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("maxItems=5000&properties=tag").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase())
                .and().entriesListDoesNotContain("id");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get tags and use skipCount parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useSkipCountCheckPagination()
    {
        returnedCollection = restClient.authenticateUser(adminUserModel).withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);

        RestTagModel firstTag = returnedCollection.getEntries().get(0).onModel();
        RestTagModel secondTag = returnedCollection.getEntries().get(1).onModel();
        RestTagModelsCollection tagsWithSkipCount = restClient.withParams("skipCount=2").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);

        tagsWithSkipCount.assertThat().entriesListDoesNotContain("tag", firstTag.getTag())
                .assertThat().entriesListDoesNotContain("tag", secondTag.getTag());
        tagsWithSkipCount.assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get tags and use maxItems parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useMaxItemsParameterCheckPagination()
    {
        returnedCollection = restClient.authenticateUser(adminUserModel).withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);

        RestTagModel firstTag = returnedCollection.getEntries().get(0).onModel();
        RestTagModel secondTag = returnedCollection.getEntries().get(1).onModel();
        RestTagModelsCollection tagsWithMaxItems = restClient.withParams("maxItems=2").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);

        tagsWithMaxItems.assertThat().entriesListContains("tag", firstTag.getTag())
                .assertThat().entriesListContains("tag", secondTag.getTag())
                .assertThat().entriesListCountIs(2);
        tagsWithMaxItems.assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With manager get tags and use high skipCount parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useHighSkipCountCheckPagination()
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("skipCount=20000").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(OK);
        returnedCollection.assertThat().entriesListIsEmpty()
                .getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("count").is("0")
                .and().field("skipCount").is(20000)
                .and().field("totalItems").is(0);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With Collaborator user get tags and use maxItems with value zero. Check default error model schema")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useMaxItemsWithValueZeroCheckDefaultErrorModelSchema()
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
    public void checkThatDeletedTagIsNotRetrievedAnymore()
    {
        String removedTag = getRandomName("tag3");

        RestTagModel deletedTag = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(document).addTag(removedTag);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingTag(deletedTag).deleteTag();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
        returnedCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListDoesNotContain("tag", removedTag.toLowerCase());
    }

    /**
     * Verify if exact name filter can be applied.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_withSingleNameFilter()
    {
        STEP("Get tags with names filter using EQUALS and expect one item in result");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(tag='" + documentTag.getTag() + "')")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entrySetMatches("tag", Set.of(documentTagValue.toLowerCase()));
    }

    /**
     * Verify if multiple names can be applied as a filter.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_withTwoNameFilters()
    {
        STEP("Get tags with names filter using IN and expect two items in result");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(tag IN ('" + documentTag.getTag() + "', '" + folderTag.getTag() + "'))")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entrySetMatches("tag", Set.of(documentTagValue.toLowerCase(), folderTagValue.toLowerCase()));
    }

    /**
     * Verify if alike name filter can be applied.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_whichNamesStartsWithOrphan()
    {
        STEP("Get tags with names filter using MATCHES and expect one item in result");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(tag MATCHES ('orphan*'))")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entrySetContains("tag", orphanTag.getTag().toLowerCase());
    }

    /**
     * Verify that tags can be filtered by exact name and alike name at the same time.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_withExactNameAndAlikeFilters()
    {
        STEP("Get tags with names filter using EQUALS and MATCHES and expect four items in result");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(tag='" + orphanTag.getTag() + "' OR tag MATCHES ('*tag*'))")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entrySetContains("tag", documentTagValue.toLowerCase(), documentTagValue2.toLowerCase(), folderTagValue.toLowerCase(), orphanTag.getTag().toLowerCase());
    }

    /**
     * Verify if multiple alike filters can be applied.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_withTwoAlikeFilters()
    {
        STEP("Get tags applying names filter using MATCHES twice and expect four items in result");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(tag MATCHES ('orphan*') OR tag MATCHES ('tag*'))")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entrySetContains("tag", documentTagValue.toLowerCase(), documentTagValue2.toLowerCase(), folderTagValue.toLowerCase(), orphanTag.getTag().toLowerCase());
    }

    /**
  * Verify that providing incorrect field name in where query will result with 400 (Bad Request).
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_withWrongWherePropertyNameAndExpect400()
    {
        STEP("Try to get tags with names filter using EQUALS and wrong property name and expect 400");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(name=gat)")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary("Where query error: property with name: name is not expected");
    }

    /**
     * Verify tht AND operator is not supported in where query and expect 400 (Bad Request).
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void testGetTags_queryAndOperatorNotSupported()
    {
        STEP("Try to get tags applying names filter using AND operator and expect 400");
        returnedCollection = restClient.authenticateUser(adminUserModel)
            .withParams("where=(name=tag AND name IN ('tag-', 'gat'))")
            .withCoreAPI()
            .getTags();

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary("An invalid WHERE query was received. Unsupported Predicate");
    }

    /**
     * Verify if count field is present for searched tags.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void testGetTags_includingCount()
    {
        STEP("Get tags including count and verify if it is present in the response");
        final RestTagModelsCollection searchedTags = restClient.withCoreAPI().include(FIELD_COUNT).getTags();

        restClient.assertStatusCodeIs(OK);
        searchedTags.assertThat().entriesListIsNotEmpty()
                .assertThat().entriesListContains(FIELD_COUNT)
                .assertThat().entriesListContains(FIELD_TAG)
                .assertThat().entriesListContains(FIELD_ID);
    }

    /**
     * Verify if count field is not present for searched tags.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void testGetTags_notIncludingCount()
    {
        STEP("Get tags, not including count and verify if it is not in the response");
        final RestTagModelsCollection searchedTags = restClient.withCoreAPI().getTags();

        restClient.assertStatusCodeIs(OK);
        searchedTags.assertThat().entriesListIsNotEmpty()
                .assertThat().entriesListDoesNotContain(FIELD_COUNT)
                .assertThat().entriesListContains(FIELD_TAG)
                .assertThat().entriesListContains(FIELD_ID);
    }
}
