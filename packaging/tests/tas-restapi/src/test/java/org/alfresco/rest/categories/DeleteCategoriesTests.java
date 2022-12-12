package org.alfresco.rest.categories;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCategoryModel;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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
     * Check we can delete a category
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategory()
    {
        RestCategoryModel aCategory = createCategory();
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(NO_CONTENT);
    }

    /**
     * Check we get an error when trying to delete a category as a non-admin user
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteCategoryAsRegularUser_andFail()
    {
        RestCategoryModel aCategory = createCategory();
        restClient.authenticateUser(user).withCoreAPI().usingCategory(aCategory).deleteCategory();
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to create a category");
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
