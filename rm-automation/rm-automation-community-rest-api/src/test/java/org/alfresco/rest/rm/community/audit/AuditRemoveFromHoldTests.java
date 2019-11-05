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
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.REMOVE_FROM_HOLD;
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
 * This class contains the tests that check the remove from hold event is audited
 *
 * @author Claudia Agache
 * @since 3.3
 */
@AlfrescoTest (jira = "RM-6859")
public class AuditRemoveFromHoldTests extends BaseRMRestTest
{
    private final String PREFIX = generateTestPrefix(AuditRemoveFromHoldTests.class);
    private final String HOLD1 = PREFIX + "hold1";
    private final String HOLD2 = PREFIX + "hold2";
    private final String HOLD3 = PREFIX + "hold3";
    private final String HOLD_TO_BE_DELETED = PREFIX + "holdToBeDeleted";

    @Autowired
    private RMAuditAPI rmAuditAPI;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManagerNoRightsOnHold, rmManagerNoRightsOnNode;
    private SiteModel privateSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder, heldRecordFolder;
    private Record heldRecord;
    private List<AuditEntry> auditEntries;
    private List<String> holdsList = asList(HOLD1, HOLD2, HOLD3);
    private AuditEntry auditEntry;
    private FileModel heldContent;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditRemoveFromHoldTests() throws Exception
    {
        STEP("Create an user with full rights to remove content from a hold.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);

        STEP("Create a collaboration site.");
        privateSite = dataSite.usingUser(rmAdmin).createPrivateRandomSite();

        STEP("Create new holds.");
        String hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(),
                HOLD1, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.createHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.createHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD3, HOLD_REASON, HOLD_DESCRIPTION);
        holdsAPI.createHold(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD_TO_BE_DELETED,
                HOLD_REASON, HOLD_DESCRIPTION);

        STEP("Create a new record category with a record folder.");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createRecordFolder(recordCategory.getId(), getRandomName("recFolder"));

        STEP("Create some held items");
        heldContent = dataContent.usingAdmin().usingSite(privateSite)
                                 .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        heldRecordFolder = createRecordFolder(recordCategory.getId(), PREFIX + "heldRecFolder");
        heldRecord = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        holdsAPI.addItemsToHolds(getAdminUser().getUsername(), getAdminUser().getPassword(),
                asList(heldContent.getNodeRefWithoutVersion(), heldRecordFolder.getId(), heldRecord.getId()),
                holdsList);

        STEP("Create users without rights to remove content from a hold.");
        rmManagerNoRightsOnHold = roleService.createUserWithSiteRoleRMRoleAndPermission(privateSite,
                UserRole.SiteManager, recordCategory.getId(), UserRoles.ROLE_RM_MANAGER, UserPermissions.PERMISSION_FILING);
        rmManagerNoRightsOnNode = roleService.createUserWithRMRoleAndRMNodePermission(UserRoles.ROLE_RM_MANAGER.roleId,
                hold1NodeRef, UserPermissions.PERMISSION_FILING);
    }

    /**
     * Data provider with valid nodes that can be removed from a hold
     *
     * @return the node id and the node name
     * @throws Exception
     */
    @DataProvider (name = "validNodesForRemoveFromHold")
    public Object[][] getValidNodesForRemoveFromHold()
    {
        return new String[][]
        {
            // a record folder
            { heldRecordFolder.getId(), heldRecordFolder.getName() },
            // a record
            { heldRecord.getId(), heldRecord.getName() },
            //an active content,
            { heldContent.getNodeRefWithoutVersion(), heldContent.getName() }
        };
    }

    /**
     * Data provider with invalid users that can not remove content from a hold
     *
     * @return the userModel
     */
    @DataProvider (name = "invalidUsersForRemoveFromHold")
    public Object[][] getInvalidUsersForRemoveFromHold()
    {
        return new UserModel[][]
        {
            { rmManagerNoRightsOnHold },
            { rmManagerNoRightsOnNode }
        };
    }

    /**
     * Given a document/record/record folder is removed from a hold
     * When I view the audit log
     * Then an entry has been created in the audit log that contains the following:
     *      name of the hold
     *      name of the document/record/record folder removed
     *      user who removed the content
     *      date the content was removed
     */
    @Test (dataProvider = "validNodesForRemoveFromHold")
    public void removeFromHoldEventIsAudited(String nodeId, String nodeName)
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Remove node from hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), nodeId, HOLD3);

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                REMOVE_FROM_HOLD.event);

        STEP("Check the audit log contains the entry for the remove from hold.");
        assertFalse("The list of events should contain Remove From Hold entry ", auditEntries.isEmpty());
        auditEntry = auditEntries.get(0);
        assertTrue("The list of events is not filtered by Remove From Hold",
                auditEntry.getEvent().equals(REMOVE_FROM_HOLD.eventDisplayName));
        assertTrue("The hold name value for the remove from hold is not audited.",
                auditEntry.getNodeName().equals(HOLD3));
        assertTrue("The user who removed the node from the hold is not audited.",
                auditEntry.getUserName().equals(rmAdmin.getUsername()));
        assertFalse("The date when the add to hold occurred is not audited.", auditEntry.getTimestamp().isEmpty());
        //TODO check content name
    }

    /**
     * Given a not empty hold is deleted
     * When I view the audit log
     * Then an entry has been created in the audit log for each item removed from that hold
     */
    @Test
    public void removeFromHoldAuditedWhenHoldIsDeleted()
    {
        STEP("Add a file to the hold that will be deleted");
        holdsAPI.addItemToHold(getAdminUser().getUsername(), getAdminUser().getPassword(),
                heldContent.getNodeRefWithoutVersion(), HOLD_TO_BE_DELETED);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Delete the hold.");
        holdsAPI.deleteHold(rmAdmin.getUsername(), rmAdmin.getPassword(), HOLD_TO_BE_DELETED);

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                REMOVE_FROM_HOLD.event);

        STEP("Check the audit log contains the entry for the remove from hold.");
        assertFalse("The list of events should contain Remove From Hold entry ", auditEntries.isEmpty());
        assertTrue("The hold name value for the remove from hold is not audited.",
                auditEntries.get(0).getNodeName().equals(HOLD_TO_BE_DELETED));
    }

    /**
     * Given an unsuccessful remove from hold action
     * When I view the audit log
     * Then the remove from hold event isn't audited
     */
    @Test
    public void unsuccessfulRemoveFromHoldIsNotAudited()
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Try to remove the record from a hold by an user with no rights.");
        try
        {
            holdsAPI.removeItemFromHold(rmManagerNoRightsOnHold.getUsername(), rmManagerNoRightsOnHold.getPassword(),
                    heldRecord.getId(), HOLD1);
            fail("Remove from hold action was successful.");
        }
        catch (Exception e)
        {
            STEP("Get the list of audit entries for the remove from hold event.");
            auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                    REMOVE_FROM_HOLD.event);

            STEP("Check the audit log doesn't contain the entry for the unsuccessful remove from hold.");
            assertTrue("The list of events should not contain remove from hold entry ", auditEntries.isEmpty());
        }
    }

    /**
     * Given a not empty record folder is removed from a hold
     * When I view the audit log
     * Then only an entry has been created in the audit log for the record folder removed
     */
    @Test
    public void removeFromHoldNotAuditedForRecordFolderChildren() throws Exception
    {
        STEP("Create a new record folder with a record inside");
        RecordCategoryChild notEmptyRecFolder = createRecordFolder(recordCategory.getId(), PREFIX + "notEmptyRecFolder");
        createElectronicRecord(notEmptyRecFolder.getId(), PREFIX + "record");

        STEP("Add the record folder to a hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Remove record folder from hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                REMOVE_FROM_HOLD.event);

        STEP("Check the audit log contains only an entry for remove from hold.");
        assertEquals("The list of events should not contain Remove from Hold entry for the record", 1,
                auditEntries.size());
        //TODO check content name
    }

    /**
     * Given a document/record/record folder is removed from multiple holds
     * When I view the audit log
     * Then multiple entries have been created in the audit log for each remove from hold event
     */
    @Test
    public void removeFromHoldIsAuditedInBulkRemoval()
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Remove record folder from multiple holds.");
        holdsAPI.removeItemsFromHolds(rmAdmin.getUsername(), rmAdmin.getPassword(),
                Collections.singletonList(heldRecordFolder.getId()), asList(HOLD1, HOLD2));

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditAPI.getRMAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword(), 100,
                REMOVE_FROM_HOLD.event);

        STEP("Check the audit log contains entries for both removal.");
        assertEquals("The list of events should contain remove from Hold entries for both holds", 2,
                auditEntries.size());
        assertTrue("The hold name value for the first remove from hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getNodeName().equals(HOLD1)));
        assertTrue("The hold name value for the second remove from hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getNodeName().equals(HOLD2)));
    }

    /**
     * Given a document/record/record folder is removed from a hold
     * When I view the audit log as an user with no Read permissions over the hold or the node
     * Then the remove from hold entry isn't visible
     */
    @Test (dataProvider = "invalidUsersForRemoveFromHold")
    public void removeFromHoldAuditEntryNotVisible(UserModel user)
    {
        STEP("Clean audit logs.");
        rmAuditAPI.clearAuditLog(getAdminUser().getUsername(), getAdminUser().getPassword());

        STEP("Remove held content from a hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), heldContent.getNodeRefWithoutVersion(), HOLD1);

        STEP("Get the list of audit entries for the remove from hold event as an user with no Read permissions.");
        auditEntries = rmAuditAPI.getRMAuditLog(user.getUsername(), user.getPassword(), 100, REMOVE_FROM_HOLD.event);

        STEP("Check the audit log doesn't contain the entry for the remove from hold event.");
        assertTrue("The list of events should not contain Remove from Hold entry ", auditEntries.isEmpty());
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditRemoveFromHoldTests()
    {
        holdsList.forEach(hold -> holdsAPI.deleteHold(getAdminUser().getUsername(), getAdminUser().getPassword(), hold));
        dataSite.usingAdmin().deleteSite(privateSite);
        asList(rmAdmin, rmManagerNoRightsOnHold, rmManagerNoRightsOnNode).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(recordCategory.getId());
    }
}
