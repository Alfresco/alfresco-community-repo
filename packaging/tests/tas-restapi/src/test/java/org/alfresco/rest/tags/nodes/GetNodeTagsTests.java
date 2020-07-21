package org.alfresco.rest.tags.nodes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.tags.TagsDataPrep;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class GetNodeTagsTests extends TagsDataPrep
{
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
                executionType = ExecutionType.SANITY, description = "Verify site Manager is able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void siteManagerIsAbleToRetrieveNodeTags() throws Exception
    {        
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));

        returnedCollection = restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify site Collaborator is able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteCollaboratorIsAbleToRetrieveNodeTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));

        returnedCollection = restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase()); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify site Contributor is able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteContributorIsAbleToRetrieveNodeTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));

        returnedCollection = restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify site Consumer is able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteConsumerIsAbleToRetrieveNodeTags() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));

        returnedCollection = restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
            executionType = ExecutionType.REGRESSION, description = "Verify admin is able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsAbleToRetrieveNodeTags() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedCollection = restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat()
            .entriesListContains("tag", documentTagValue.toLowerCase())
            .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, 
            executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to get node tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id = "MNT-16904", description = "fails only on environment with tenants")
    public void unauthenticatedUserIsNotAbleToRetrieveNodeTags() throws Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password"));
        restClient.withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that using invalid value for skipCount parameter returns status code 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void invalidSkipCountTest() throws Exception
    {
        restClient.withParams("skipCount=abc").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "abc"));

        restClient.withParams("skipCount=-1").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that using invalid value for maxItems parameter returns status code 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void invalidMaxItemsTest() throws Exception
    {
        restClient.withParams("maxItems=abc").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "abc"));

        restClient.withParams("maxItems=-1").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user without permissions returns status code 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void userWithoutPermissionsTest() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(adminUserModel).createModeratedRandomSite();
        FileModel moderatedDocument = dataContent.usingSite(moderatedSite).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(dataUser.createRandomTestUser()).withCoreAPI().usingResource(moderatedDocument).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if node does not exist returns status code 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void nonexistentNodeTest() throws Exception
    {
        FileModel badDocument = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String nodeRef = RandomStringUtils.randomAlphanumeric(10);
        badDocument.setNodeRef(nodeRef);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(badDocument).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nodeRef));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that if node id is empty returns status code 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void emptyNodeIdTest() throws Exception
    {
        FileModel badDocument = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        badDocument.setNodeRef("");

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(badDocument).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify folder tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION})
    public void folderTagsTest() throws Exception
    {
        FolderModel folder = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        restClient.withCoreAPI().usingResource(folder).addTag(documentTagValue);
        restClient.withCoreAPI().usingResource(folder).addTag(documentTagValue2);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(folder).getNodeTags()
                .assertThat()
                .entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify site Manager is able to get node tags using properties parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void siteManagerIsAbleToRetrieveNodeTagsWithPropertiesParameter() throws Exception
    {
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withParams("properties=tag").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.assertThat().entriesListContains("tag", documentTagValue.toLowerCase())
                .and().entriesListContains("tag", documentTagValue2.toLowerCase())
                .and().entriesListDoesNotContain("id");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that Collaborator user is not able to get node tags using site id instead of node id")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorGetNodeTagsUseSiteIdInsteadOfNodeId() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        file.setNodeRef(siteModel.getId());
        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(file).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, file.getNodeRef()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get node tags and use skipCount parameter. Check pagination and maxItems")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useSkipCountCheckPaginationAndMaxItems() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel)
                .withParams("skipCount=1").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("count").isGreaterThan(1);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get node tags and use maxItems parameter. Check pagination")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void useMaxItemsParameterCheckPagination() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel)
                .withParams("maxItems=1").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.getPagination().assertThat().field("maxItems").is(1)
                .and().field("hasMoreItems").is("true")
                .and().field("count").is("1")
                .and().field("skipCount").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Using manager user get only one tag.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void usingManagerGetOnlyOneTag() throws Exception
    {
        FileModel file = dataContent.usingAdmin().usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addTag(documentTagValue);

        returnedCollection = restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(file).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("totalItems").is("1")
                .and().field("count").is("1")
                .and().field("skipCount").is("0");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Using admin get last 2 tags and skip first 2 tags")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminUserGetLast2TagsAndSkipFirst2Tags() throws Exception
    {
        String firstTag = "1st tag";
        String secondTag = "2nd tag";
        String thirdTag = "3rd tag";
        String fourthTag = "4th tag";
        FileModel file = dataContent.usingAdmin().usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUserModel).withCoreAPI().usingResource(file).addTag(firstTag);
        restClient.withCoreAPI().usingResource(file).addTag(secondTag);
        restClient.withCoreAPI().usingResource(file).addTag(thirdTag);
        restClient.withCoreAPI().usingResource(file).addTag(fourthTag);

        returnedCollection = restClient.withParams("skipCount=2").withCoreAPI().usingResource(file).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        returnedCollection.assertThat().entriesListContains("tag", thirdTag.toLowerCase())
                .and().entriesListContains("tag", fourthTag.toLowerCase());
        returnedCollection.getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("totalItems").is("4")
                .and().field("count").is("2")
                .and().field("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "With admin get node tags and use maxItems=0.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithZeroMaxItems() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel)
                .withParams("maxItems=0").withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that using high skipCount parameter returns status code 200.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void getTagsWithHighSkipCount() throws Exception
    {
        returnedCollection = restClient.authenticateUser(adminUserModel).withParams("skipCount=10000")
                .withCoreAPI().usingResource(document).getNodeTags();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedCollection.getPagination().assertThat().field("maxItems").is(100)
                .and().field("hasMoreItems").is("false")
                .and().field("count").is("0")
                .and().field("skipCount").is("10000");
        returnedCollection.assertThat().entriesListCountIs(0);
    }
}