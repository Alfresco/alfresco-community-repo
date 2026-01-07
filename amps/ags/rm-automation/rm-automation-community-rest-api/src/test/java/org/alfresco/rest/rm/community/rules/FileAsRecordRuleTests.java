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

package org.alfresco.rest.rm.community.rules;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;

import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.smoke.FileAsRecordTests;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;

@AlfrescoTest (jira = "APPS-36")
public class FileAsRecordRuleTests extends BaseRMRestTest
{
    private UserModel nonRMUser, rmManager;
    private RecordCategory category_manager, category_admin;
    private RecordCategoryChild folder_admin, folder_manager ;
    private static final String CATEGORY_MANAGER = "catManager" + generateTestPrefix(FileAsRecordTests.class);
    private static final String CATEGORY_ADMIN = "catAdmin" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_MANAGER = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_ADMIN = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);
    private FolderModel testFolder;
    private FileModel document,inPlaceRecord;


    @Autowired
    private RoleService roleService;
    @Autowired
    private RulesAPI rulesAPI;

    /**
     * Create preconditions:
     * 1. RM site is created
     * 2. Two users: user without RM role and a user with RM manager role
     * 3. Two Record categories with one folder each
     * 4. User with RM MANAGER role has Filling permission over one category
     * 5. A collaboration folder with rule set to declare and file as record to a record folder
     **/
    @BeforeClass(alwaysRun = true)
    public void preconditionForDeclareFileAsRecordRuleTests()
    {
        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create a user");
        nonRMUser = dataUser.createRandomTestUser("testUser");

        STEP("Create a collaboration site");
        testSite = dataSite.usingUser(nonRMUser).createPublicRandomSite();

        STEP("Create two categories with two folders");
        category_manager = createRootCategory(CATEGORY_MANAGER);
        category_admin = createRootCategory(CATEGORY_ADMIN);
        folder_admin = createFolder(category_admin.getId(),FOLDER_ADMIN);
        folder_manager = createFolder(category_manager.getId(),FOLDER_MANAGER);

        STEP("Create an rm user and give filling permission over CATEGORY_MANAGER record category");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        rmManager = roleService.createCollaboratorWithRMRoleAndPermission(testSite, recordCategory,
            UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);

        STEP("Create a collaboration folder with a rule set to declare and file as record to a record folder");
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);

        assertStatusCode(CREATED);
    }
    /**
     * Given I am a user that can create a rule on a folder in a collaboration site
     * When I am creating the rule
     * Then I have the option of adding a "Declare and File as Record" action to the rule
     * <p>
     * Given I am creating a rule
     * When I add the "Declare and File as Record" action to the rule
     * Then I am able to select the record folder I want the declared record to be filed to
     * <p>
     * Given I am configuring a "Declare and File as Record" action within a rule
     * And I have at least one records management role (eg RM User)
     * When I am selecting the record folder location to file the declared record to
     * Then I see the record folders in the file plan that I have file access to as the creator of the record
     **/
    @Test
    public void declareAsRecordRuleAsRMUserWithFilingPermissions() {
        STEP("Create a collaboration folder");
        testFolder = dataContent.usingSite(testSite)
            .usingUser(rmManager)
            .createFolder();

        STEP("Create a rule with Declare as Record action and check that user can select a record folder.");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);

        assertStatusCode(CREATED);
    }
    /**
     * Given I am configuring a "Declare and File as Record" action within a rule
     * And I don't have a records management role
     * When I am selecting the record folder location to file the declared record to
     * Then I can see only the file plan
     */
    @Test
    public void declareAsRecordRuleAsNonRMUser()
    {
        STEP("Create a collaboration folder");
        testFolder = dataContent.usingSite(testSite)
            .usingUser(nonRMUser)
            .createFolder();

        STEP("Create a rule with Declare as Record action and check that user can select a record folder.");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(nonRMUser.getUsername(), nonRMUser.getPassword(), NODE_PREFIX + testFolder.getNodeRef(), ruleDefinition);

        assertStatusCode(CREATED);
    }

    /**
     * Given I have not selected a record folder location
     * When the rule is triggered
     * Then the file is declared as record to the UnFiled Records folder
     */
    @Test
    public void triggerDeclareToUnfiledRuleAsNonRMUser()
    {
        STEP("Create a collaboration folder with a rule set to declare and file as record without a record folder location");

        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);

        assertStatusCode(CREATED);

        STEP("Create as nonRMUser a new file into the previous folder in order to trigger the rule");
        inPlaceRecord = dataContent.usingUser(nonRMUser).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

//      Verify that declared record is in Unfilled Records Folder
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        List<UnfiledContainerChildEntry> matchingRecords = unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .filter(e -> e.getEntry().getId().equals(inPlaceRecord.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());
    }

    @AfterClass(alwaysRun = true)
    public void cleanupDeclareAsRecordRuleTests()
    {
        STEP("Delete the collaboration site");
        dataSite.usingUser(nonRMUser).deleteSite(testSite);

        STEP("Delete Users");
        dataUser.deleteUser(nonRMUser);
        dataUser.deleteUser(rmManager);

        STEP("Delete categories");
        getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
            deleteRecordCategory(recordCategoryEntry.getEntry().getId()));
    }
}
