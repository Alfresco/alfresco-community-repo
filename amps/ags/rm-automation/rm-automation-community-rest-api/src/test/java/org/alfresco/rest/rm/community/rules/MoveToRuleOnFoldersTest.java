package org.alfresco.rest.rm.community.rules;

import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainer;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Random;

import static java.lang.Integer.MAX_VALUE;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.*;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertNotNull;


public class MoveToRuleOnFoldersTest extends BaseRMRestTest{

    private String unfiledRecordsNodeRef;

    private String recordFolder2;
    private String nonElectronicId;

    private Record electronicRecord;
    private UnfiledContainer unfiledContainer;

    private UserModel rmAdmin;
    private RecordCategory RecordCategoryOne;
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
    private RecordsAPI recordsAPI;

    @BeforeClass(alwaysRun = true)
    public void precondition()
    {
        //create RM site
        createRMSiteIfNotExists();
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);
        /**
         * Removes rules on File Plan and Unfiled Records
         */
        final String holdName = TEST_PREFIX + "holdToBeDeleted";
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), holdName);
        unfiledContainer = getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS);

        unfiledRecordsNodeRef = NODE_PREFIX + unfiledContainer.getId();
        rulesAPI.deleteAllRulesOnContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), unfiledRecordsNodeRef);

        //create root category, create folders , add electronic and non electronic records
        RecordCategoryOne = createRootCategory(RECORD_CATEGORY_ONE);
        String recordFolder1 = createRecordFolder(RecordCategoryOne.getId(), getRandomName("recFolder")).getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolder1, getFile(IMAGE_FILE));
        STEP("Check the electronic record has been created");
        assertStatusCode(CREATED);

        // Generate update metadata
        String newName = getModifiedPropertyValue(electronicRecord.getName());
        String newTitle = getModifiedPropertyValue(electronicRecord.getProperties().getTitle());
        String newDescription = getModifiedPropertyValue(electronicRecord.getProperties().getDescription());

        // Update record:EDIT electronic and non electronic metadata [PENDING]

//        recordsAPI.updateRecord(createRecordModel(newName, newDescription, newTitle), electronicRecord.getId());
//        assertStatusCode(OK);



        //create non electronic record
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
        nonElectronicId = recordFolderAPI.createRecord(nonElectrinicRecordModel, recordFolder1).getId();
//        STEP("Check the non-electronic record has been created");
        assertStatusCode(CREATED);

        // move the electronic and nonelectronic record from folder1 to folder2
        STEP("Create the record folder2 inside the rootCategory");
         recordFolder2 = createCategoryFolderInFilePlan().getId();

//        //create a rule for completing record for folder 2

        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description1")
            .applyToChildren(true).title(title)
            .actions(Collections.singletonList(ActionsOnRule.COMPLETE_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX +recordFolder2, ruleDefinition);

    }
    @Test
    public void MoveToRuleFoldersTest()
    {    STEP("Move electronic record from folder1 to folder2");
        RestNodeModel electronicDocRestNodeModel = getRestAPIFactory()
            .getNodeAPI(toContentModel(electronicRecord.getId()))
            .move(createBodyForMoveCopy(recordFolder2));
        assertStatusCode(OK);

        STEP("Move non-electronic record from folder1 to folder2");

        RestNodeModel nonelectronicDocRestNodeModel = getRestAPIFactory()
            .getNodeAPI(toContentModel(nonElectronicId))
            .move(createBodyForMoveCopy(recordFolder2));
        assertStatusCode(OK);

    }

    private String getModifiedPropertyValue(String originalValue) {
        /* to be used to append to modifications */
        String MODIFIED_PREFIX = "modified_";
        return MODIFIED_PREFIX + originalValue;
    }
    }


