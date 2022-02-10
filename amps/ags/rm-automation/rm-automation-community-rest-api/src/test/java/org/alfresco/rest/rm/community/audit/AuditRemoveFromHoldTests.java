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
package org.alfresco.rest.rm.community.audit;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.base.TestData.HOLD_DESCRIPTION;
import static org.alfresco.rest.rm.community.base.TestData.HOLD_REASON;
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.REMOVE_FROM_HOLD;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.FILE_PLAN_PATH;
import static org.alfresco.utility.Utility.buildPath;
import static org.alfresco.utility.Utility.removeLastSlash;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.HoldsAPI;
import org.alfresco.rest.v0.service.RMAuditService;
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

    @Autowired
    private RMAuditService rmAuditService;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManagerNoReadOnHold, rmManagerNoReadOnNode;
    private SiteModel privateSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder, heldRecordFolder;
    private Record heldRecord;
    private List<AuditEntry> auditEntries;
    private final List<String> holdsList = asList(HOLD1, HOLD2, HOLD3);
    private List<String> holdsListRef = new ArrayList<>();
    private FileModel heldContent;
    private String hold1NodeRef;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditRemoveFromHoldTests()
    {
        STEP("Create an user with full rights to remove content from a hold.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);

        STEP("Create a collaboration site.");
        privateSite = dataSite.usingUser(rmAdmin).createPrivateRandomSite();

        STEP("Create new holds.");
        hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(),
                HOLD1, HOLD_REASON, HOLD_DESCRIPTION);
        String hold2NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);
        String hold3NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD3, HOLD_REASON, HOLD_DESCRIPTION);
        holdsListRef = asList(hold1NodeRef, hold2NodeRef, hold3NodeRef);

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
        rmManagerNoReadOnHold = roleService.createUserWithSiteRoleRMRoleAndPermission(privateSite,
                UserRole.SiteManager, recordCategory.getId(), UserRoles.ROLE_RM_MANAGER, UserPermissions.PERMISSION_FILING);
        rmManagerNoReadOnNode = roleService.createUserWithRMRoleAndRMNodePermission(UserRoles.ROLE_RM_MANAGER.roleId,
                hold1NodeRef, UserPermissions.PERMISSION_FILING);
    }

    /**
     * Data provider with valid nodes that can be removed from a hold
     *
     * @return the node id, the node name and the node path
     */
    @DataProvider (name = "validNodesForRemoveFromHold")
    public Object[][] getValidNodesForRemoveFromHold()
    {
        String recordFolderPath = removeLastSlash(buildPath(FILE_PLAN_PATH, recordCategory.getName(),
                heldRecordFolder.getName()));
        String recordPath = removeLastSlash(buildPath(FILE_PLAN_PATH, recordCategory.getName(),
                recordFolder.getName(), heldRecord.getName()));
        String contentPath = "/Company Home" + heldContent.getCmisLocation();

        return new String[][]
        {
            // a record folder
            { heldRecordFolder.getId(), heldRecordFolder.getName(), recordFolderPath },
            // a record
            { heldRecord.getId(), heldRecord.getName(), recordPath },
            //an active content,
            { heldContent.getNodeRefWithoutVersion(), heldContent.getName(), contentPath }
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
     *      path of the node
     */
    @Test (dataProvider = "validNodesForRemoveFromHold")
    public void removeFromHoldEventIsAudited(String nodeId, String nodeName, String nodePath)
    {
        rmAuditService.clearAuditLog();

        STEP("Remove node from hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), nodeId, HOLD3);

        STEP("Check the audit log contains the entry for the remove from hold event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), REMOVE_FROM_HOLD, rmAdmin, nodeName, nodePath,
                asList(ImmutableMap.of("new", "", "previous", nodeName, "name", "Name"),
                        ImmutableMap.of("new", "", "previous", HOLD3, "name", "Hold Name")));
    }

    /**
     * Given an unsuccessful remove from hold action
     * When I view the audit log
     * Then the remove from hold event isn't audited
     */
    @Test
    public void unsuccessfulRemoveFromHoldIsNotAudited()
    {
        rmAuditService.clearAuditLog();

        STEP("Try to remove the record from a hold by an user with no rights.");
        holdsAPI.removeItemsFromHolds(rmManagerNoReadOnHold.getUsername(), rmManagerNoReadOnHold.getPassword(),
                SC_INTERNAL_SERVER_ERROR, Collections.singletonList(heldRecord.getId()),
                Collections.singletonList(hold1NodeRef));

        STEP("Check the audit log doesn't contain the entry for the unsuccessful remove from hold.");
        assertTrue("The list of events should not contain remove from hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), REMOVE_FROM_HOLD).isEmpty());
    }

    /**
     * Given a not empty record folder is removed from a hold
     * When I view the audit log
     * Then only an entry has been created in the audit log for the record folder removed
     */
    @Test
    public void removeFromHoldNotAuditedForRecordFolderChildren()
    {
        STEP("Create a new record folder with a record inside");
        RecordCategoryChild notEmptyRecFolder = createRecordFolder(recordCategory.getId(), PREFIX + "notEmptyRecFolder");
        Record record = createElectronicRecord(notEmptyRecFolder.getId(), PREFIX + "record");

        STEP("Add the record folder to a hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        rmAuditService.clearAuditLog();

        STEP("Remove record folder from hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), REMOVE_FROM_HOLD);

        STEP("Check the audit log contains only an entry for remove from hold.");
        assertEquals("The list of events should contain only an entry", 1, auditEntries.size());
        assertTrue("The list of events should not contain Remove from Hold entry for the record",
                auditEntries.stream().noneMatch(entry -> entry.getNodeName().equals(record.getName())));
    }

    /**
     * Given a record folder is removed from multiple holds
     * When I view the audit log
     * Then multiple entries have been created in the audit log for each remove from hold event
     */
    @Test
    public void removeFromHoldIsAuditedInBulkRemoval()
    {
        rmAuditService.clearAuditLog();

        STEP("Remove record folder from multiple holds.");
        holdsAPI.removeItemsFromHolds(rmAdmin.getUsername(), rmAdmin.getPassword(),
                Collections.singletonList(heldRecordFolder.getId()), asList(HOLD1, HOLD2));

        STEP("Get the list of audit entries for the remove from hold event.");
        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), REMOVE_FROM_HOLD);

        STEP("Check the audit log contains entries for both removal.");
        assertEquals("The list of events should contain remove from Hold entries for both holds", 2,
                auditEntries.size());
        assertTrue("The hold name value for the first remove from hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                        ImmutableMap.of("new", "", "previous", HOLD1, "name", "Hold Name"))));
        assertTrue("The hold name value for the second remove from hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                        ImmutableMap.of("new", "", "previous", HOLD2, "name", "Hold Name"))));
    }

    /**
     * Given a document/record/record folder is removed from a hold
     * When I view the audit log as an user with no Read permissions over the node
     * Then the remove from hold entry isn't visible
     */
    @Test
    public void removeFromHoldAuditEntryNotVisible()
    {
        STEP("Add content to a hold.");
        FileModel heldFile = dataContent.usingAdmin().usingSite(privateSite)
                                           .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), heldFile.getNodeRefWithoutVersion(), HOLD1);

        rmAuditService.clearAuditLog();

        STEP("Remove held content from the hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), heldFile.getNodeRefWithoutVersion(), HOLD1);

        STEP("Check that an user with no Read permissions can't see the entry for the remove from hold event.");
        assertTrue("The list of events should not contain Remove from Hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(rmManagerNoReadOnNode, REMOVE_FROM_HOLD).isEmpty());
    }

    /**
     * Given a document/record/record folder is removed from a hold
     * When I view the audit log as an user with no Read permissions over the hold
     * Then the the hold name is replaced in the remove from hold entry
     */
    @Test
    public void removeFromHoldAuditEntryHoldNameNotVisible()
    {
        STEP("Add content to a hold.");
        FileModel heldFile = dataContent.usingAdmin().usingSite(privateSite)
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), heldFile.getNodeRefWithoutVersion(), HOLD1);

        rmAuditService.clearAuditLog();

        STEP("Remove held content from the hold.");
        holdsAPI.removeItemFromHold(rmAdmin.getUsername(), rmAdmin.getPassword(), heldFile.getNodeRefWithoutVersion(), HOLD1);

        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(rmManagerNoReadOnHold, REMOVE_FROM_HOLD);

        STEP("Check that an user with no Read permissions can't see the hold name in the remove from hold event.");
        String replacementHoldName = "You don't have permission to view this hold.";
        assertEquals("The list of events should contain the Remove from Hold entry", 1, auditEntries.size());
        assertTrue("The hold name should not be visible in the Remove from Hold entry ",
            auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                ImmutableMap.of("new", "", "previous", replacementHoldName, "name", "Hold Name"))));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditRemoveFromHoldTests()
    {
        holdsListRef.forEach(holdRef -> holdsAPI.deleteHold(getAdminUser(), holdRef));
        dataSite.usingAdmin().deleteSite(privateSite);
        asList(rmAdmin, rmManagerNoReadOnHold, rmManagerNoReadOnNode).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        deleteRecordCategory(recordCategory.getId());
    }
}
