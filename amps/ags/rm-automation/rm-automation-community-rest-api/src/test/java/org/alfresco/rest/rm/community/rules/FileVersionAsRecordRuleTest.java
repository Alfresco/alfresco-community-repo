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
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
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

public class FileVersionAsRecordRuleTest  extends BaseRMRestTest {

    private UserModel nonRMuser, rmManager;
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

    @BeforeClass(alwaysRun = true)
    public void createTestPrecondition()
    {


        STEP("Create the RM site if doesn't exist");
        createRMSiteIfNotExists();

        STEP("Create a user");
        nonRMuser = dataUser.createRandomTestUser("testUser");

        STEP("Create a collaboration site");
        testSite = dataSite.usingUser(nonRMuser).createPublicRandomSite();

        STEP("Create a document with the user without RM role");
        document = dataContent.usingSite(testSite)
            .usingUser(nonRMuser)
            .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create two categories with two folders");
        category_manager = createRootCategory(CATEGORY_MANAGER);
        category_admin = createRootCategory(CATEGORY_ADMIN);
        folder_admin = createFolder(category_admin.getId(),FOLDER_ADMIN);
        folder_manager = createFolder(category_manager.getId(),FOLDER_MANAGER);


        STEP("Create an rm user and give filling permission over CATEGORY_MANAGER record category");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId())
            .build();
        rmManager = roleService.createCollaboratorWithRMRoleAndPermission(testSite, recordCategory,
            UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);



        STEP("Create a collaboration folder with a rule set to declare and file version as record to a record folder");
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);

        assertStatusCode(CREATED);
    }

    @Test
    public void declareVersionAsRecordRuleAsRMUserWithFilingPermissions()
    {

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
        rulesAPI.createRule(rmManager.getUsername(), rmManager.getPassword(), NODE_PREFIX + testFolder.getNodeRef(), ruleDefinition);

        assertStatusCode(CREATED);

    }

    @Test
    public void declareVersionAsRecordRuleAsNonRMUser()
    {

        STEP("Create a collaboration folder");
        testFolder = dataContent.usingSite(testSite)
            .usingUser(nonRMuser)
            .createFolder();

        STEP("Create a rule with Declare as Record action and check that user can select a record folder.");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(nonRMuser.getUsername(), nonRMuser.getPassword(), NODE_PREFIX + testFolder.getNodeRef(), ruleDefinition);

        assertStatusCode(CREATED);

    }

    @Test
    public void triggerDeclareToUnfiledRuleAsNonRMUser() throws Exception {

        STEP("Create a collaboration folder with a rule set to declare and file as record without a record folder location");


        FileModel inplaceRecord = dataContent.usingSite(testSite).usingUser(nonRMuser)
            .createContent(new FileModel("declareAndFileToIntoUnfiledRecordFolder",
                FileType.TEXT_PLAIN));

        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(nonRMuser.getUsername(), nonRMuser.getPassword(), NODE_PREFIX + inplaceRecord.getNodeRef(), ruleDefinition);

        assertStatusCode(CREATED);

       STEP("Create as nonRMuser a new file into the previous folder in order to trigger the rule");
        inPlaceRecord = dataContent.usingUser(nonRMuser).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // verify the declared record is in Unfilled Records folder
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        List<UnfiledContainerChildEntry> matchingRecords = unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
            .getEntries()
            .stream()
            .filter(e -> e.getEntry().getId().equals(inplaceRecord.getNodeRefWithoutVersion()))
            .collect(Collectors.toList());


    }

    @AfterClass(alwaysRun = true)
    public void cleanupDeclareVersionAsRecordRuleTests()
    {

        STEP("Delete the collaboration site");
        dataSite.usingUser(nonRMuser).deleteSite(testSite);

        STEP("Delete Users");
        dataUser.deleteUser(nonRMuser);
        dataUser.deleteUser(rmManager);


        STEP("Delete categories");
        getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
            deleteRecordCategory(recordCategoryEntry.getEntry().getId()));
    }

}
