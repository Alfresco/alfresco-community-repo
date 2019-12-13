package org.alfresco.rest.tags.nodes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.rest.tags.TagsDataPrep;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/3/2016.
 */
@Test(groups = {TestGroup.REQUIRE_SOLR})
public class AddTagTests extends TagsDataPrep
{
    private String tagValue;
    private RestTagModel returnedModel;
    private RestCommentModel returnedModelComment;
    private RestTagModelsCollection returnedModelTags;

    @BeforeMethod(alwaysRun = true)
    public void generateRandomTag()
    {
        tagValue = RandomData.getRandomName("tag");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify admin user adds tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void adminIsAbleToAddTag() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue)
            .and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY, 
            description = "Verify Manager user adds tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void managerIsAbleToTagAFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue)
            .and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify Collaborator user adds tags with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToTagAFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue)
            .and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify Contributor user doesn't have permission to add tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsNotAbleToAddTagToAnotherContent() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify Contributor user adds tags to his content with Rest API and status code is 201")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsAbleToAddTagToHisContent() throws Exception
    {
        userModel = usersWithRoles.getOneUserWithRole(UserRole.SiteContributor);
        restClient.authenticateUser(userModel);
        FileModel contributorDoc = dataContent.usingSite(siteModel).usingUser(userModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        returnedModel = restClient.withCoreAPI().usingResource(contributorDoc).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("id").isNotEmpty()
            .and().field("tag").is(tagValue);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify Consumer user doesn't have permission to add tags with Rest API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToTagAFile() throws Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer));
        restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify user gets status code 401 if authentication call fails")    
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void userIsNotAbleToAddTagIfAuthenticationFails() throws Exception
    {
        UserModel siteManager = usersWithRoles.getOneUserWithRole(UserRole.SiteManager);
        String managerPassword = siteManager.getPassword();
        siteManager.setPassword("wrongPassword");
        restClient.authenticateUser(siteManager);
        restClient.withCoreAPI().usingResource(document).addTag("tagUnauthorized");
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
        siteManager.setPassword(managerPassword);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that adding empty tag returns status code 400")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void emptyTagTest() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag("");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.NULL_ARGUMENT, "tag"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that adding tag with user that has no permissions returns status code 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagWithUserThatDoesNotHavePermissions() throws Exception
    {
        restClient.authenticateUser(dataUser.createRandomTestUser());
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that adding tag to a node that does not exist returns status code 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagToInexistentNode() throws Exception
    {
        String oldNodeRef = document.getNodeRef();
        String nodeRef = RandomStringUtils.randomAlphanumeric(10);
        document.setNodeRef(nodeRef);

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, nodeRef));
        document.setNodeRef(oldNodeRef);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify that manager is able to tag a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.SANITY })
    public void managerIsAbleToTagAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));
        returnedModel = restClient.withCoreAPI().usingResource(folderModel).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that tagged file can be tagged again")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagToATaggedFile() throws Exception
    {
        restClient.authenticateUser(adminUserModel);

        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();

        returnedModel = restClient.withCoreAPI().usingResource(document).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();

        returnedModel = restClient.withCoreAPI().usingResource(document).addTag("random_tag_value");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is("random_tag_value").and().field("id").isNotEmpty();

        restClient.withCoreAPI().usingResource(document).getNodeTags().assertThat()
                .entriesListContains("tag", tagValue.toLowerCase())
                .and().entriesListContains("tag", "random_tag_value");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that user cannot add invalid tag")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addInvalidTag() throws Exception
    {
        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingResource(document).addTag("-1~!|@#$%^&*()_=");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_TAG, "|"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor is able to tag a folder created by self")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void contributorIsAbleToTagAFolderCreatedBySelf() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(siteModel).createFolder();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor));
        returnedModel = restClient.withCoreAPI().usingResource(folderModel).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator is able to tag a folder")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void collaboratorIsAbleToTagAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));
        returnedModel = restClient.withCoreAPI().usingResource(folderModel).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that consumer is not able to tag a folder. Check default error model schema.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void consumerIsNotAbleToTagAFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .withCoreAPI().usingResource(folderModel).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that tagged folder can be tagged again")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagToATaggedFolder() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager));

        returnedModel = restClient.withCoreAPI().usingResource(folderModel).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is(tagValue).and().field("id").isNotEmpty();

        returnedModel = restClient.withCoreAPI().usingResource(folderModel).addTag("random_tag_value");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModel.assertThat().field("tag").is("random_tag_value").and().field("id").isNotEmpty();

        restClient.withCoreAPI().usingResource(folderModel).getNodeTags().assertThat()
                .entriesListContains("tag", tagValue.toLowerCase())
                .and().entriesListContains("tag", "random_tag_value")
                .and().entriesListCountIs(2);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Using collaborator provide more than one tag element")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void provideMoreThanOneTagElement() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();
        String tagValue1 = RandomData.getRandomName("tag1");
        String tagValue2 = RandomData.getRandomName("tag2");

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator));

        returnedModelTags = restClient.withCoreAPI().usingResource(folderModel).addTags(tagValue, tagValue1, tagValue2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        returnedModelTags.assertThat().entriesListContains("tag", tagValue)
                .and().entriesListContains("tag", tagValue1)
                .and().entriesListContains("tag", tagValue2)
                .and().entriesListCountIs(3);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager cannot add tag with special characters.")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagWithSpecialCharacters() throws Exception
    {
        FolderModel folderModel = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();
        String specialCharsTag = "!@#$%^&*()'\".,<>-_+=|\\";

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .withCoreAPI().usingResource(folderModel).addTag(specialCharsTag);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.INVALID_TAG, "|"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that you cannot tag a comment and it returns status code 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagToAComment() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        String comment = "comment for a tag";

        restClient.authenticateUser(adminUserModel);
        returnedModelComment = restClient.withCoreAPI().usingResource(file).addComment(comment);
        file.setNodeRef(returnedModelComment.getId());
        restClient.withCoreAPI().usingResource(file).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_TAG);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that you cannot tag a tag and it returns status code 405")
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS, TestGroup.REGRESSION })
    public void addTagToATag() throws Exception
    {
        FileModel file = dataContent.usingSite(siteModel).usingUser(adminUserModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        restClient.authenticateUser(adminUserModel);
        returnedModel = restClient.withCoreAPI().usingResource(file).addTag(tagValue);
        file.setNodeRef(returnedModel.getId());
        restClient.withCoreAPI().usingResource(file).addTag(tagValue);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError().containsSummary(RestErrorModel.CANNOT_TAG);
    }
}