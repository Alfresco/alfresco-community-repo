/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.rm.community.smoke;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertFalse;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;

public class CreateCategoriesTests extends BaseRMRestTest {

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    private RecordCategory rootCategory;
    private final String TEST_PREFIX = generateTestPrefix(CreateCategoriesTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private RecordCategory Category1;
    private RecordCategory Category2;
    private RecordCategory SubCategory1;
    private RecordCategory SubCategory2;

    @BeforeClass(alwaysRun = true)
    public void preconditionForCreateCategoriesTests()
    {
        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
            getAdminUser().getPassword(),
            "Administrator");

        STEP("Create two category");
        Category1 = createRootCategory(getRandomName("Category1"));

        Category2= createRootCategory(getRandomName("Category2"));

        STEP("Create Sub category");
        RecordCategoryChild subCategory1 = createRecordCategory(Category1.getId(), getRandomName("subCategory1"));
        RecordCategoryChild subCategory2 = createRecordCategory(Category2.getId(), getRandomName("subCategory2"));

    }


    @Test @AlfrescoTest(jira = "RM-2756")
    public void createCategories() throws Exception {

        FilePlan filePlan = getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS);

        STEP("copy category 1 to File Plan.");
        getRestAPIFactory().getNodeAPI(toContentModel(Category1.getId())).copy(createBodyForMoveCopy(filePlan.getId()));

        STEP("copy category 1 to category 2");
        getRestAPIFactory().getNodeAPI(toContentModel(Category1.getId())).copy(createBodyForMoveCopy(Category2.getId()));

        String categoryName = "Category name " + getRandomAlphanumeric();
        String categoryTitle = "Category title " + getRandomAlphanumeric();


        // Create the root record category
        RecordCategory Category1 = createRootCategory(categoryName, categoryTitle);

        String newCategoryName = "Rename " + categoryName;

        // Build the properties which will be updated
        RecordCategory  recordCategoryUpdated = Category1.builder().name(newCategoryName).build();

        // Update the record category
        RecordCategory renamedRecordCategory = getRestAPIFactory().getRecordCategoryAPI().updateRecordCategory(recordCategoryUpdated,Category1.getId());
        // Verify the status code
        assertStatusCode(OK);

        // verify renamed component and editTitle component still has this parent
        assertEquals(renamedRecordCategory.getParentId(), filePlan.getId());

        STEP("move category 1 edited copy to File Plan");
        getRestAPIFactory().getNodeAPI(toContentModel(renamedRecordCategory.getId())).move(createBodyForMoveCopy(filePlan.getId()));
        assertStatusCode(OK);

        // delete All the categories
        deleteRecordCategory(Category1.getId());
        deleteRecordCategory(Category2.getId());
    }
}
