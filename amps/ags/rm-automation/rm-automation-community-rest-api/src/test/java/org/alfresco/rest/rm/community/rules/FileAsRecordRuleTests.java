package org.alfresco.rest.rm.community.rules;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.rm.community.model.user.UserRoles;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;

import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

@AlfrescoTest (jira = "APPS-36")
public class FileAsRecordRuleTests extends BaseRMRestTest
{
    private static final String CATEGORY_MANAGER = "categoryManager" + generateTestPrefix(FileAsRecordRuleTests.class);
    private static final String CATEGORY_ADMIN = "categoryAdmin" + generateTestPrefix(FileAsRecordRuleTests.class);
    private static final String FOLDER_MANAGER = "recordFolder" + generateTestPrefix(FileAsRecordRuleTests.class);
    private static final String FOLDER_ADMIN = "recordFolder" + generateTestPrefix(FileAsRecordRuleTests.class);

    private UserModel nonRMUser,rmManager;
    private SiteModel publicSite;
    private FolderModel testFolder;
    private FileModel inPlaceRecord;
    private RecordCategory category_manager,category_admin;
    private RecordCategoryChild folder_admin,folder_manager;
    private static final String RULE_NAME = "File as Record Rule";
    private static final String FOLDER_MANAGER_PATH = "/" + CATEGORY_MANAGER + "/" + FOLDER_MANAGER;
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
        publicSite = dataSite.usingUser(nonRMUser).createPublicRandomSite();


        STEP("Create two categories with two folders");
        category_manager = createRootCategory(CATEGORY_MANAGER);
        category_admin = createRootCategory(CATEGORY_ADMIN);
        folder_admin = createFolder(category_admin.getId(),FOLDER_ADMIN);
        folder_manager = createFolder(category_manager.getId(),FOLDER_MANAGER);

        STEP("Create an rm user and give filling permission over CATEGORY_MANAGER record category");
        RecordCategory recordCategory = new RecordCategory().builder()
            .id(category_manager.getId()).build();

        rmManager = roleService.createCollaboratorWithRMRoleAndPermission(publicSite, recordCategory,
            UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);

        STEP("Create a collaboration folder with a rule set to declare and file as record to a record folder");
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
            .applyToChildren(true)
            .actions(Collections.singletonList(ActionsOnRule.DECLARE_AS_RECORD.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);
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
        testFolder = dataContent.usingSite(publicSite)
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
        testFolder = dataContent.usingSite(publicSite)
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
    }
    /**
     * Given a record folder location has been selected for a "Declare and File as Record" action within a rule
     * And I have permissions and capabilities to file to that record folder
     * When I trigger the rule
     * Then the file is filed directly to the selected record folder from the file plan
     */

    @Test
    public void triggerDeclareToRecordFolderRuleAsUserWithPermissions()
    {
        STEP("Create as rmManager a new file into the folderWithRule in order to trigger the rule");
        FileModel testFile = dataContent.usingUser(rmManager).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    /**
     * Given a record folder location has been selected for a "Declare and File as Record" action within a rule
     * And I don't have permissions and capabilities to file to that record folder
     * When I trigger the rule
     * Then the file is not declared as record
     */

    @Test
    public void triggerDeclareToRecordFolderRuleAsUserWithoutPermissions()
    {
        STEP("Create as nonRMuser a new file into the folderWithRule in order to trigger the rule");
        FileModel testFile = dataContent.usingUser(nonRMUser).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    /**
     * Given I have not selected a record folder location
     * When the rule is triggered
     * Then the file is declared as record to the Unfiled Records folder
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

        STEP("Create as nonRMuser a new file into the previous folder in order to trigger the rule");
        inPlaceRecord = dataContent.usingUser(nonRMUser).usingResource(testFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    @AfterClass(alwaysRun = true)
    public void cleanupDeclareAsRecordRuleTests()
    {
        STEP("Delete the collaboration site");
        dataSite.usingUser(nonRMUser).deleteSite(publicSite);

        STEP("Delete Users");
        dataUser.deleteUser(nonRMUser);
        dataUser.deleteUser(rmManager);

        STEP("Delete categories");
        getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(FILE_PLAN_ALIAS).getEntries().forEach(recordCategoryEntry ->
            deleteRecordCategory(recordCategoryEntry.getEntry().getId()));
        getRestAPIFactory().getRecordsAPI().deleteRecord(inPlaceRecord.getNodeRefWithoutVersion());
    }
}