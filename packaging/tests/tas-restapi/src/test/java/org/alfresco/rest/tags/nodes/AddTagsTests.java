package org.alfresco.rest.tags.nodes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.rest.tags.TagsDataPrep;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/7/2016.
 */
@Test(groups = {TestGroup.REQUIRE_SOLR})
public class AddTagsTests extends TagsDataPrep
{
    private FileModel contributorDoc;
    private String tag1, tag2;
    private RestTagModelsCollection returnedCollection;

    @BeforeMethod(alwaysRun = true)
    public void generateRandomTagsList()
    {
        tag1 = RandomData.getRandomName("tag");
        tag2 = RandomData.getRandomName("tag");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user adds multiple tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsAbleToAddTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedCollection.assertThat().entriesListContains("tag", tag1)
            .and().entriesListContains("tag", tag2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify Manager user adds multiple tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void managerIsAbleToAddTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        returnedCollection = restClient.withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedCollection.assertThat().entriesListContains("tag", tag1)
            .and().entriesListContains("tag", tag2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS },
            executionType = ExecutionType.SANITY, description = "Verify Collaborator user adds multiple tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToAddTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        returnedCollection = restClient.withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedCollection.assertThat().entriesListContains("tag", tag1)
            .and().entriesListContains("tag", tag2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user doesn't have permission to add multiple tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsNotAbleToAddTagsToAnotherContent() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Contributor user adds multiple tags to his content with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsAbleToAddTagsToHisContent() throws Exception
    {
        userModel = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(userModel);
        contributorDoc = dataContent.usingSite(siteModel).usingUser(userModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        returnedCollection = restClient.withCoreAPI().usingResource(contributorDoc).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedCollection.assertThat().entriesListContains("tag", tag1)
            .and().entriesListContains("tag", tag2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Consumer user doesn't have permission to add multiple tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToAddTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToAddTagsIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        String managerPassword = siteManager.getPassword();
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager).withCoreAPI().usingResource(document).addTags(tag1, tag2);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
        siteManager.setPassword(managerPassword);
    }
    
    @TestRail(section = { TestGroup.REST_API,
            TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Verify include count parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsUsingCountParam() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String tagName = RandomData.getRandomName("tag");
        returnedModel = restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addTag(RandomData.getRandomName("tag"));
        RestTagModelsCollection tagsWithIncludeParamCount = restClient.withParams("include=count").withCoreAPI().getTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        for (RestTagModel tagModel : tagsWithIncludeParamCount.getEntries())
        {
            if (tagModel != null && tagModel.getTag() != null)
            {
                if (tagModel.getTag().equals(tagName))
                {
                    Assert.assertEquals(tagModel.getCount().intValue(), 1);
                }
            }
        }
       
    }
}