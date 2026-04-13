/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.springframework.http.HttpStatus.*;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.NONELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.*;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.RecordContent;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.test.AlfrescoTest;

public class UnfiledRecordsRuleTests extends BaseRMRestTest
{

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    private final String TEST_PREFIX = generateTestPrefix(CreateCategoriesTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private RecordCategory Category2;
    private RecordCategoryChild Folder2;
    @Autowired
    private RulesAPI rulesAPI;

    @Test
    @AlfrescoTest(jira = "RM-2794")
    public void unfiledRecordsRule()
    {

        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");

        STEP("Create record categories and record folders");
        Category2 = createRootCategory(getRandomName("recordCategory"));
        Folder2 = createFolder(Category2.getId(), getRandomName("recordFolder"));

        STEP("Get the unfiled records container");
        UnfiledContainer container = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);

        // Check the response code
        assertStatusCode(OK);

        // create a rule
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
                .applyToChildren(true)
                .actions(Collections.singletonList(ActionsOnRule.FILE_TO.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + container.getId(), ruleDefinition);

        // upload an electronic record
        UnfiledContainerChild electronicRecord = UnfiledContainerChild.builder()
                .name(ELECTRONIC_RECORD_NAME)
                .nodeType(CONTENT_TYPE)
                .content(RecordContent.builder().mimeType("text/plain").build())
                .build();
        assertStatusCode(OK);

        // create a non-electronic record
        UnfiledContainerChild nonelectronicRecord = UnfiledContainerChild.builder()
                .properties(UnfiledContainerChildProperties.builder()
                        .description(NONELECTRONIC_RECORD_NAME)
                        .title("Title")
                        .build())
                .name(NONELECTRONIC_RECORD_NAME)
                .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                .build();
        assertStatusCode(OK);

        // delete the record created, delete the rule from UnfilledRecord page, delete the category created
        rulesAPI.deleteAllRulesOnContainer(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + container.getId());
        deleteRecordCategory(Category2.getId());
        assertStatusCode(NO_CONTENT);

    }

}
