package org.alfresco.rest.tags;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.Test;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public class DeleteTagsTests extends TagsDataPrep
{
    /**
     * Check we can delete a tag by its id.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteTag()
    {
        STEP("Create a tag assigned to a document and send a request to delete it.");
        document = dataContent.usingUser(adminUserModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel aTag = createTagForDocument(document);

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingTag(aTag).deleteTag();
        restClient.assertStatusCodeIs(NO_CONTENT);

        STEP("Ensure that the tag has been deleted by sending a GET request and receiving 404.");
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().getTag(aTag);
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Attempt to delete a tag as a regular user and received BAD_REQUEST
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteTagAsRegularUser_andFail()
    {
        STEP("Create a tag assigned to a document and attempt to delete as a non-admin user");
        document = dataContent.usingUser(dataUser.getAdminUser()).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel aTag = createTagForDocument(document);

        restClient.authenticateUser(userModel).withCoreAPI().usingTag(aTag).deleteTag();
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to manage a tag");
    }

    /**
     * Check we receive 404 error when trying to delete a tag with a non-existent id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteNonExistentTag()
    {
        STEP("Get tag with non-existent id");
        final String id = "non-existing-dummy-id";
        final RestTagModel tagModel = createTagModelWithId(id);

        STEP("Attempt to delete tag with non-existent id and receive 404");
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingTag(tagModel).deleteTag();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }
}
