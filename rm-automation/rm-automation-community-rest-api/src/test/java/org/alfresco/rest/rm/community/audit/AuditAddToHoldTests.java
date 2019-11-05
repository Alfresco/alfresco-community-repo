/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.community.audit;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.ADD_TO_HOLD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.List;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests that check the add to hold event is audited
 *
 * @author Claudia Agache
 * @since 3.3
 */
@AlfrescoTest (jira = "RM-6859")
public class AuditAddToHoldTests extends BaseRMRestTest
{
    private final String PREFIX = generateTestPrefix(AuditAddToHoldTests.class);
    private final String HOLD1 = PREFIX + "hold1";
    private final String HOLD2 = PREFIX + "hold2";

    @Autowired
    private RMAuditAPI rmAuditAPI;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManagerNoRightsOnHold, rmManagerNoRightsOnNode;
    private SiteModel privateSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder;
    private List<AuditEntry> auditEntries;
    private List<String> holdsList = asList(HOLD1, HOLD2);
    private AuditEntry auditEntry;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditAddToHoldTests() throws Exception
    {
        STEP("Create 2 holds.");
        String hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(),
                getAdminUser().getPassword(), HOLD1, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.createHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create a new record category with a record folder.");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createRecordFolder(recordCategory.getId(), PREFIX + "recFolder");

        STEP("Create an user with full rights to add content to a hold.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);

        STEP("Create a collaboration site.");
        privateSite = dataSite.usingUser(rmAdmin).createPrivateRandomSite();

        STEP("Create users without rights to add content to a hold.");
        rmManagerNoRightsOnHold = roleService.createUserWithSiteRoleRMRoleAndPermission(privateSite,
                UserRole.SiteManager, recordCategory.getId(), UserRoles.ROLE_RM_MANAGER, UserPermissions.PERMISSION_FILING);
        rmManagerNoRightsOnNode = roleService.createUserWithRMRoleAndRMNodePermission(UserRoles.ROLE_RM_MANAGER.roleId,
                hold1NodeRef, UserPermissions.PERMISSION_FILING);
    }

    /**
     * Data provider with valid nodes that can be added to a hold
     *
     * @return the node id and the node name
     * @throws Exception
     */
    @DataProvider (name = "validNodesForAddToHold")
    public Object[][] getValidNodesForAddToHold() throws Exception
    {
        FileModel contentToBeAdded = dataContent.usingAdmin().usingSite(privateSite)
                                                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RecordCategoryChild recordFolderToBeAdded = createRecordFolder(recordCategory.getId(), PREFIX + "recFolderToBeAdded");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        return new String[][]
        {
            // a record folder
            { recordFolderToBeAdded.getId(), recordFolderToBeAdded.getName() },
            // a record
            { recordToBeAdded.getId(), recordToBeAdded.getName() },
            //an active content,
            { contentToBeAdded.getNodeRefWithoutVersion(), contentToBeAdded.getName() }
        };
    }

    /**
     * Data provider with invalid users that can not add content to a hold
     *
     * @return the userModel
     */
    @DataProvider (name = "invalidUsersForAddToHold")
    public Object[][] getInvalidUsersForAddToHold()
    {
        return new UserModel[][]
        {
            { rmManagerNoRightsOnHold },
            { rmManagerNoRightsOnNode }
        };
    }

    /**
     * Given a document/record/record folder is added to a hold
     * When I view the audit log
     * Then an entry has been created in the audit log that contains the following:
     *      name of the hold
     *      name of the document/record/record folder added
     *      user who added the content
     *      date the content was added
     */
    @Test (dataProvider = "validNodesForAddToHold")
    public void addToHoldEventIsAudited(String nodeId, String nodeName)
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Add node to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), nodeId, HOLD1);

        STEP("Get the list of audit entries for the add to hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                ADD_TO_HOLD.event);

        STEP("Check the audit log contains the entry for the add to hold.");
        assertFalse("The list of events should contain Add To Hold entry ", auditEntries.isEmpty());
        auditEntry = auditEntries.get(0);
        assertTrue("The list of events is not filtered by Add To Hold",
                auditEntry.getEvent().equals(ADD_TO_HOLD.eventDisplayName));
        assertTrue("The hold name value for the add to hold is not audited.",
                auditEntry.getNodeName().equals(HOLD1));
        assertTrue("The user who added the node to the hold is not audited.",
                auditEntry.getUserName().equals(rmAdmin.getUsername()));
        assertFalse("The date when the add to hold occurred is not audited.", auditEntry.getTimestamp().isEmpty());
        //TODO check content name
    }

    /**
     * Given an unsuccessful add to hold action
     * When I view the audit log
     * Then the add to hold event isn't audited
     */
    @Test
    public void unsuccessfulAddToHoldIsNotAudited() throws Exception
    {
        STEP("Create a new record");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Try to add the record to a hold by an user with no rights.");
        try
        {
            holdsAPI.addItemToHold(rmManagerNoRightsOnHold.getUsername(), rmManagerNoRightsOnHold.getPassword(),
                    recordToBeAdded.getId(), HOLD1);
            fail("Add to hold action was successful.");
        }
        catch (Exception e)
        {
            STEP("Get the list of audit entries for the add to hold event.");
            auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                    ADD_TO_HOLD.event);

            STEP("Check the audit log doesn't contain the entry for the unsuccessful add to hold.");
            assertTrue("The list of events should not contain Add to Hold entry ", auditEntries.isEmpty());
        }
    }

    /**
     * Given a not empty record folder is added to a hold
     * When I view the audit log
     * Then only an entry has been created in the audit log for the record folder added
     */
    @Test
    public void addToHoldIsNotAuditedForRecordFolderChildren() throws Exception
    {
        STEP("Create a new record folder with a record inside");
        RecordCategoryChild notEmptyRecFolder = createRecordFolder(recordCategory.getId(), PREFIX + "notEmptyRecFolder");
        createElectronicRecord(notEmptyRecFolder.getId(), PREFIX + "record");

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Add record folder to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        STEP("Get the list of audit entries for the add to hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                ADD_TO_HOLD.event);

        STEP("Check the audit log contains only an entry for add to hold.");
        assertEquals("The list of events should not contain Add to Hold entry for the record", 1, auditEntries.size());
        //TODO check content name
    }

    /**
     * Given a document/record/record folder is added to multiple holds
     * When I view the audit log
     * Then multiple entries have been created in the audit log for each add to hold event
     */
    @Test
    public void addToHoldIsAuditedInBulkAddition() throws Exception
    {
        STEP("Create a new record");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Add record to multiple holds.");
        holdsAPI.addItemsToHolds(rmAdmin.getUsername(), rmAdmin.getPassword(),
                Collections.singletonList(recordToBeAdded.getId()), holdsList);

        STEP("Get the list of audit entries for the add to hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                ADD_TO_HOLD.event);

        STEP("Check the audit log contains entries for both additions.");
        assertEquals("The list of events should contain Add to Hold entries for both holds", 2, auditEntries.size());
        assertTrue("The hold name value for the first add to hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getNodeName().equals(HOLD1)));
        assertTrue("The hold name value for the second add to hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getNodeName().equals(HOLD2)));
    }

    /**
     * Given a document/record/record folder is added to a hold
     * When I view the audit log as an user with no Read permissions over the hold or the node
     * Then the add to hold entry isn't visible
     */
    @Test (dataProvider = "invalidUsersForAddToHold")
    public void addToHoldAuditEntryNotVisible(UserModel user)
    {
        STEP("Create a new file");
        FileModel contentToBeAdded = dataContent.usingAdmin().usingSite(privateSite)
                                                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Add file to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), contentToBeAdded.getNodeRefWithoutVersion(), HOLD1);

        STEP("Get the list of audit entries for the add to hold event as an user with no Read permissions.");
        auditEntries = rmAuditAPI.getRMAuditLog(user.getUsername(), user.getPassword(), 100, ADD_TO_HOLD.event);

        STEP("Check the audit log doesn't contain the entry for the add to hold event.");
        assertTrue("The list of events should not contain Add to Hold entry ", auditEntries.isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditAddToHoldTests()
    {
        holdsList.forEach(hold -> holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), hold));
        dataSite.usingAdmin().deleteSite(privateSite);
        asList(rmAdmin, rmManagerNoRightsOnHold, rmManagerNoRightsOnNode).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(recordCategory.getId());
    }
}
