package org.alfresco.rest.categories;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public class DeleteCategoriesTests extends RestTest {

    private UserModel user;


    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        STEP("Create a user");
        user = dataUser.createRandomTestUser();
    }

    /**
     * Check we can delete a category.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategory()
    {
        RestCategoryModel aCategory = createCategory();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(NO_CONTENT);
    }

    /**
     * Check we get an error when trying to delete a category as a non-admin user.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategoryAsRegularUser_andFail()
    {
        RestCategoryModel aCategory = createCategory();
        restClient.authenticateUser(user).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to delete a category");
    }

    /**
     * Check we receive 404 error when trying to delete a category with a non-existent node id.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteNonExistentCategory()
    {
        STEP("Get category with non-existent id");
        final RestCategoryModel rootCategory = new RestCategoryModel();
        final String id = "non-existing-dummy-id";
        rootCategory.setId(id);

        STEP("Attempt to delete category with non-existent id and receive 404");
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(rootCategory).deleteCategory();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Attempt to delete a category when providing a node id that doesn't belong to a category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategory_givenNonCategoryNodeId()
    {
        STEP("Create a site and a folder inside it");
        final SiteModel site = dataSite.usingUser(user).createPublicRandomSite();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        String id = folder.getNodeRef();

        final RestCategoryModel aCategory = new RestCategoryModel();
        aCategory.setId(id);

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(BAD_REQUEST).assertLastError().containsSummary("Node id does not refer to a valid category");
    }

    public RestCategoryModel createCategory()
    {
        final RestCategoryModel rootCategory = new RestCategoryModel();
        rootCategory.setId("-root-");
        final RestCategoryModel aCategory = new RestCategoryModel();
        aCategory.setName(RandomData.getRandomName("Category"));
        final RestCategoryModel createdCategory = restClient.authenticateUser(dataUser.getAdminUser())
                .withCoreAPI()
                .usingCategory(rootCategory)
                .createSingleCategory(aCategory);
        restClient.assertStatusCodeIs(CREATED);

        return createdCategory;
    }
}
