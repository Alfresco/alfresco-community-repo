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
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.*;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.test.AlfrescoTest;

public class BasicRulesIntegrationTests extends BaseRMRestTest
{

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    private final static String title = "Rule to complete";
    private final static String description = "Rule to describe";
    private final String TEST_PREFIX = generateTestPrefix(CreateCategoriesTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    @Autowired
    private RulesAPI rulesAPI;

    @Test
    @AlfrescoTest(jira = "RM-2794")
    public void basicRulesIntegration()
    {

        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");

        STEP("Create record categories and record folders");
        RecordCategory Category = createRootCategory(getRandomName("recordCategory"));
        String recordFolder1 = createRecordFolder(Category.getId(), getRandomName("recFolder")).getId();

        // create a rule for completing a record
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description1")
                .applyToChildren(true).title(title)
                .actions(Collections.singletonList(ActionsOnRule.COMPLETE_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + Category.getId(), ruleDefinition);

        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        // create two electronic record in record folder
        String electronicRecordId1 = createElectronicRecord(recordFolder1, ELECTRONIC_RECORD_NAME).getId();
        String electronicRecordId2 = createElectronicRecord(recordFolder1, ELECTRONIC_RECORD_NAME).getId();
        assertStatusCode(CREATED);

        // Update the rules for record Category
        rulesAPI.updateRule(getAdminUser().getUsername(), getAdminUser().getPassword(),
                NODE_PREFIX + Category.getId(), ruleDefinition.description("description").id(description));

        // Delete the root category and rules
        deleteRecordCategory(Category.getId());
        rulesAPI.deleteAllRulesOnContainer(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + Category.getId());
    }

}
