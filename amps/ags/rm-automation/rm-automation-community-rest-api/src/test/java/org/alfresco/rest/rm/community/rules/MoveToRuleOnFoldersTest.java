/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplan.FilePlan;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.ConditionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildEntry;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.*;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.*;
import static org.testng.Assert.assertNotNull;


public class MoveToRuleOnFoldersTest extends BaseRMRestTest{

    private String unfiledRecordsNodeRef;

    private RecordCategoryChild recordFolder2;
    private RecordCategoryChild recordFolder1;
    private String nonElectronicId;

    private Record electronicRecord;
    private UnfiledContainer unfiledContainer;
    private String ruleType = ConditionsOnRule.UPDATE.getWhenConditionValue();
    private UserModel rmAdmin;
    public RecordCategory RecordCategoryOne;
    public RestNodeModel RecordCategoryCopy;
    private RecordCategoryChild recordFolder;
    public static final String RECORD_FOLDER_ONE = "record-folder-one";
    private final String TEST_PREFIX = generateTestPrefix(MoveToRuleOnFoldersTest.class);
    // private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    private final String RECORD_CATEGORY_ONE = TEST_PREFIX + "category";

    private final String recordName = "Test record";
    private final String recordTitle = recordName + " title";
    private final String recordDescription = recordName + " description";
    @Autowired
    private RulesAPI rulesAPI;
    @Autowired
    private HoldsAPI holdsAPI;

    @Autowired
    private RoleService roleService;

    @Autowired
    public RecordsAPI recordsAPI;


    @BeforeClass(alwaysRun = true)
    public void precondition()
    {
        //create RM site
        createRMSiteIfNotExists();
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);
        //create root category, create folders , add electronic and non electronic records
        RecordCategoryOne = createRootCategory(RECORD_CATEGORY_ONE);
        recordFolder1=createRecordFolder(RecordCategoryOne.getId(), getRandomName("recFolder"));
       // recordFolder1_id = createRecordFolder(RecordCategoryOne.getId(), getRandomName("recFolder")).getId();
        recordFolder2 = createFolder(getAdminUser(),RecordCategoryOne.getId(),getRandomName("recFolder"));

        String CatName=RecordCategoryOne.getName();
        String folder2name=recordFolder2.getName();
        String recfolder2_path="/"+CatName+"/"+folder2name;
        STEP("CREATE ELECTRONIC RECORD");
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder1.getId(), getFile(IMAGE_FILE));
        STEP("Check the electronic record has been created");
        assertStatusCode(CREATED);


        STEP("Create a non-electronic record by completing some of the fields");
        // Use these properties for non-electronic record to be created
        String title = "Title " + getRandomAlphanumeric();
        String description = "Description " + getRandomAlphanumeric();
        String box = "Box "+ getRandomAlphanumeric();
        String file = "File " + getRandomAlphanumeric();
        String shelf = "Shelf " + getRandomAlphanumeric();
        String storageLocation = "Storage Location " + getRandomAlphanumeric();
        String name = "Record " + getRandomAlphanumeric();
        Random random = new Random();
        Integer numberOfCopies = random.nextInt(MAX_VALUE);
        Integer physicalSize = random.nextInt(MAX_VALUE);

        // Set values of all available properties for the non electronic records
        Record nonElectrinicRecordModel = createFullNonElectronicRecordModel(name, title, description, box, file, shelf, storageLocation, numberOfCopies, physicalSize);
        // Create non-electronic record
        nonElectronicId = recordFolderAPI.createRecord(nonElectrinicRecordModel, recordFolder1.getId()).getId();
        STEP("Check the non-electronic record has been created");
        assertStatusCode(CREATED);

        STEP("create a rule MOVE_TO for folder 1");
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description1")
            .runInBackground(true).title(title)
            .actions(Collections.singletonList(ActionsOnRule.MOVE_TO.getActionValue())).ruleType(ruleType).path(recfolder2_path);
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX +recordFolder1.getId() , ruleDefinition);



        STEP("Update metadata for Non-Electronic Record");
        org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record nonelecrecord = recordsAPI.getRecord(nonElectronicId);
        String nonelecnewName = getModifiedPropertyValue(nonElectrinicRecordModel.getName());
        String nonelecnewTitle = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getTitle());
        String nonelecnewDescription = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(nonelecnewName, nonelecnewDescription, nonelecnewTitle),nonelecrecord.getId());
        assertStatusCode(OK);

        STEP("Update metadata for Electronic Record");
        Record elecrecord = recordsAPI.getRecord(electronicRecord.getId());
        String elecnewName = getModifiedPropertyValue(electronicRecord.getName());
        String elecnewTitle = getModifiedPropertyValue(electronicRecord.getProperties().getTitle());
        String elecnewDescription = getModifiedPropertyValue(electronicRecord.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(elecnewName, elecnewDescription, elecnewTitle),elecrecord.getId());
        assertStatusCode(OK);

        STEP("CHECK IF E AND NON-E RECORDS MOVED  TO FOLDER2");
        //update the e and non-e records which have been moved to folder2 . if update is asserted as ok then record is present
        STEP("Update metadata for Non-Electronic Record");
        nonelecrecord = recordsAPI.getRecord(nonElectronicId);
        nonelecnewName = getModifiedPropertyValue(nonElectrinicRecordModel.getName());
        nonelecnewTitle = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getTitle());
        nonelecnewDescription = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(nonelecnewName, nonelecnewDescription, nonelecnewTitle),nonelecrecord.getId());
        assertStatusCode(OK);

        STEP("Update metadata for Electronic Record");
        elecrecord = recordsAPI.getRecord(electronicRecord.getId());
        elecnewName = getModifiedPropertyValue(electronicRecord.getName());
        elecnewTitle = getModifiedPropertyValue(electronicRecord.getProperties().getTitle());
        elecnewDescription = getModifiedPropertyValue(electronicRecord.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(elecnewName, elecnewDescription, elecnewTitle),elecrecord.getId());
        assertStatusCode(OK);

        STEP("Delete E and Non-E RECORDS IN FOLDER 2");
        recordsAPI.deleteRecord(electronicRecord.getId());
        assertStatusCode(NO_CONTENT);
        recordsAPI.deleteRecord(nonElectronicId);
        assertStatusCode(NO_CONTENT);
        STEP("RULE CREATION FOR FOLDER 1 WITHOUT RUNNING IN BACKGROUND");

        RuleDefinition ruleDefinition_notinbackground = RuleDefinition.createNewRule().title("name").description("description1")
            .runInBackground(false).title(title)
            .actions(Collections.singletonList(ActionsOnRule.MOVE_TO.getActionValue())).ruleType(ruleType).path(recfolder2_path);
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX +recordFolder1.getId() , ruleDefinition);

        STEP("CREATE E AND NON E RECORDS");
        electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder1.getId(), getFile(IMAGE_FILE));
        STEP("Check the electronic record has been created");
        assertStatusCode(CREATED);
        nonElectronicId = recordFolderAPI.createRecord(nonElectrinicRecordModel, recordFolder1.getId()).getId();
        STEP("Check the non-electronic record has been created");
        assertStatusCode(CREATED);


        STEP("UPDATE METADATA");
        STEP("Update metadata for Non-Electronic Record");

         nonelecrecord = recordsAPI.getRecord(nonElectronicId);
         nonelecnewName = getModifiedPropertyValue(nonElectrinicRecordModel.getName());
         nonelecnewTitle = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getTitle());
         nonelecnewDescription = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(nonelecnewName, nonelecnewDescription, nonelecnewTitle),nonelecrecord.getId());
        assertStatusCode(OK);

        STEP("Update metadata for Electronic Record");
         elecrecord = recordsAPI.getRecord(electronicRecord.getId());
         elecnewName = getModifiedPropertyValue(electronicRecord.getName());
         elecnewTitle = getModifiedPropertyValue(electronicRecord.getProperties().getTitle());
         elecnewDescription = getModifiedPropertyValue(electronicRecord.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(elecnewName, elecnewDescription, elecnewTitle),elecrecord.getId());
        assertStatusCode(OK);

        STEP("CHECK IF E AND NON-E RECORDS MOVED  TO FOLDER2");
        //update the e and non-e records which have been moved to folder2 . if update is asserted as ok then record is present
        STEP("Update metadata for Non-Electronic Record");
        nonelecrecord = recordsAPI.getRecord(nonElectronicId);
        nonelecnewName = getModifiedPropertyValue(nonElectrinicRecordModel.getName());
        nonelecnewTitle = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getTitle());
        nonelecnewDescription = getModifiedPropertyValue(nonElectrinicRecordModel.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(nonelecnewName, nonelecnewDescription, nonelecnewTitle),nonelecrecord.getId());
        assertStatusCode(OK);

        STEP("Update metadata for Electronic Record");
        elecrecord = recordsAPI.getRecord(electronicRecord.getId());
        elecnewName = getModifiedPropertyValue(electronicRecord.getName());
        elecnewTitle = getModifiedPropertyValue(electronicRecord.getProperties().getTitle());
        elecnewDescription = getModifiedPropertyValue(electronicRecord.getProperties().getDescription());
        recordsAPI.updateRecord(createRecordModel(elecnewName, elecnewDescription, elecnewTitle),elecrecord.getId());
        assertStatusCode(OK);


    }

    @Test
    public void MoveToRuleFoldersTest()
    {
        System.out.println("PRECONDITION PASSED");
    }

    @AfterClass(alwaysRun = true)
    public void cleanMoveToRuleOnFoldersTest()
    {
        deleteRecordCategory(RecordCategoryOne.getId());

        getDataUser().deleteUser(rmAdmin);
    }

    private String getModifiedPropertyValue(String originalValue) {
        /* to be used to append to modifications */
        String MODIFIED_PREFIX = "modified_";
        return MODIFIED_PREFIX + originalValue;
    }
}

