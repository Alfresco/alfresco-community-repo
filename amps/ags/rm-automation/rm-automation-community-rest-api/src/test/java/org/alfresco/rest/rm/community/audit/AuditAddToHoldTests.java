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
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.ADD_TO_HOLD;
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
    private RMAuditService rmAuditService;
    @Autowired
    private HoldsAPI holdsAPI;
    @Autowired
    private RoleService roleService;

    private UserModel rmAdmin, rmManagerNoReadOnHold, rmManagerNoReadOnNode;
    private SiteModel privateSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder;
    private List<AuditEntry> auditEntries;
    private final List<String> holdsList = asList(HOLD1, HOLD2);
    private List<String> holdsListRef = new ArrayList<>();
    private String hold1NodeRef;

    @BeforeClass (alwaysRun = true)
    public void preconditionForAuditAddToHoldTests()
    {
        STEP("Create 2 holds.");
        hold1NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(),
                getAdminUser().getPassword(), HOLD1, HOLD_REASON, HOLD_DESCRIPTION);
        String hold2NodeRef = holdsAPI.createHoldAndGetNodeRef(getAdminUser().getUsername(), getAdminUser().getPassword(), HOLD2, HOLD_REASON, HOLD_DESCRIPTION);
        holdsListRef = asList(hold1NodeRef, hold2NodeRef);

        STEP("Create a new record category with a record folder.");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createRecordFolder(recordCategory.getId(), PREFIX + "recFolder");

        STEP("Create an user with full rights to add content to a hold.");
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);

        STEP("Create a collaboration site.");
        privateSite = dataSite.usingUser(rmAdmin).createPrivateRandomSite();

        STEP("Create users without rights to add content to a hold.");
        rmManagerNoReadOnHold = roleService.createUserWithSiteRoleRMRoleAndPermission(privateSite,
                UserRole.SiteManager, recordCategory.getId(), UserRoles.ROLE_RM_MANAGER, UserPermissions.PERMISSION_FILING);
        rmManagerNoReadOnNode = roleService.createUserWithRMRoleAndRMNodePermission(UserRoles.ROLE_RM_MANAGER.roleId,
                hold1NodeRef, UserPermissions.PERMISSION_FILING);
    }

    /**
     * Data provider with valid nodes that can be added to a hold
     *
     * @return the node id, the node name and the node path
     */
    @DataProvider (name = "validNodesForAddToHold")
    public Object[][] getValidNodesForAddToHold()
    {
        FileModel contentToBeAdded = dataContent.usingAdmin().usingSite(privateSite)
                                                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RecordCategoryChild recordFolderToBeAdded = createRecordFolder(recordCategory.getId(), PREFIX + "recFolderToBeAdded");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");
        String recordFolderPath = removeLastSlash(buildPath(FILE_PLAN_PATH, recordCategory.getName(),
                recordFolderToBeAdded.getName()));
        String recordPath = removeLastSlash(buildPath(FILE_PLAN_PATH, recordCategory.getName(),
                recordFolder.getName(), recordToBeAdded.getName()));
        String contentPath = "/Company Home" + contentToBeAdded.getCmisLocation();

        return new String[][]
        {
            // a record folder
            { recordFolderToBeAdded.getId(), recordFolderToBeAdded.getName(), recordFolderPath },
            // a record
            { recordToBeAdded.getId(), recordToBeAdded.getName(), recordPath },
            //an active content,
            { contentToBeAdded.getNodeRefWithoutVersion(), contentToBeAdded.getName(), contentPath }
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
     *      path of the node
     */
    @Test (dataProvider = "validNodesForAddToHold")
    public void addToHoldEventIsAudited(String nodeId, String nodeName, String nodePath)
    {
        rmAuditService.clearAuditLog();

        STEP("Add node to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), nodeId, HOLD1);

        STEP("Check the audit log contains the entry for the add to hold event.");
        rmAuditService.checkAuditLogForEvent(getAdminUser(), ADD_TO_HOLD, rmAdmin, nodeName, nodePath,
                asList(ImmutableMap.of("new", nodeName, "previous", "", "name", "Name"),
                        ImmutableMap.of("new", HOLD1, "previous", "", "name", "Hold Name")));
    }

    /**
     * Given an unsuccessful add to hold action
     * When I view the audit log
     * Then the add to hold event isn't audited
     */
    @Test
    public void unsuccessfulAddToHoldIsNotAudited()
    {
        STEP("Create a new record");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        rmAuditService.clearAuditLog();

        STEP("Try to add the record to a hold by an user with no rights.");
        holdsAPI.addItemsToHolds(rmManagerNoReadOnHold.getUsername(), rmManagerNoReadOnHold.getPassword(),
                SC_INTERNAL_SERVER_ERROR, Collections.singletonList(recordToBeAdded.getId()),
                Collections.singletonList(hold1NodeRef));

        STEP("Check the audit log doesn't contain the entry for the unsuccessful add to hold.");
        assertTrue("The list of events should not contain Add to Hold entry ",
                rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), ADD_TO_HOLD).isEmpty());
    }

    /**
     * Given a not empty record folder is added to a hold
     * When I view the audit log
     * Then only an entry has been created in the audit log for the record folder added
     */
    @Test
    public void addToHoldIsNotAuditedForRecordFolderChildren()
    {
        STEP("Create a new record folder with a record inside");
        RecordCategoryChild notEmptyRecFolder = createRecordFolder(recordCategory.getId(), PREFIX + "notEmptyRecFolder");
        Record record = createElectronicRecord(notEmptyRecFolder.getId(), PREFIX + "record");

        rmAuditService.clearAuditLog();

        STEP("Add record folder to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), notEmptyRecFolder.getId(), HOLD1);

        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), ADD_TO_HOLD);

        STEP("Check the audit log contains only an entry for add to hold.");
        assertEquals("The list of events should contain only an entry", 1, auditEntries.size());
        assertTrue("The list of events should not contain Add to Hold entry for the record",
                auditEntries.stream().noneMatch(entry -> entry.getNodeName().equals(record.getName())));
    }

    /**
     * Given a record is added to multiple holds
     * When I view the audit log
     * Then multiple entries have been created in the audit log for each add to hold event
     */
    @Test
    public void addToHoldIsAuditedInBulkAddition()
    {
        STEP("Create a new record");
        Record recordToBeAdded = createElectronicRecord(recordFolder.getId(), PREFIX + "record");

        rmAuditService.clearAuditLog();

        STEP("Add record to multiple holds.");
        holdsAPI.addItemsToHolds(rmAdmin.getUsername(), rmAdmin.getPassword(),
                Collections.singletonList(recordToBeAdded.getId()), holdsList);

        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), ADD_TO_HOLD);

        STEP("Check the audit log contains entries for both additions.");
        assertEquals("The list of events should contain Add to Hold entries for both holds", 2, auditEntries.size());
        assertTrue("The hold name value for the first add to hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                        ImmutableMap.of("new", HOLD1, "previous", "", "name", "Hold Name"))));
        assertTrue("The hold name value for the second add to hold is not audited.",
                auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                        ImmutableMap.of("new", HOLD2, "previous", "", "name", "Hold Name"))));
    }

    /**
     * Given a document is added to a hold
     * When I view the audit log as an user with no Read permissions over the document
     * Then the add to hold entry isn't visible
     */
    @Test
    public void addToHoldAuditEntryNotVisible()
    {
        STEP("Create a new file");
        FileModel contentToBeAdded = dataContent.usingAdmin().usingSite(privateSite)
                                                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        rmAuditService.clearAuditLog();

        STEP("Add file to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), contentToBeAdded.getNodeRefWithoutVersion(), HOLD1);

        STEP("Check that an user with no Read permissions can't see the entry for the add to hold event.");
        assertTrue("The list of events should not contain Add to Hold entry ",
            rmAuditService.getAuditEntriesFilteredByEvent(rmManagerNoReadOnNode, ADD_TO_HOLD).isEmpty());
    }

    /**
     * Given a document is added to a hold
     * When I view the audit log as an user with no Read permissions over the hold
     * Then the the hold name is replaced in the add to hold entry
     */
    @Test
    public void addToHoldAuditEntryHoldNameNotVisible()
    {
        STEP("Create a new file");
        FileModel contentToBeAdded = dataContent.usingAdmin().usingSite(privateSite)
                                                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        rmAuditService.clearAuditLog();

        STEP("Add file to hold.");
        holdsAPI.addItemToHold(rmAdmin.getUsername(), rmAdmin.getPassword(), contentToBeAdded.getNodeRefWithoutVersion(), HOLD1);

        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(rmManagerNoReadOnHold, ADD_TO_HOLD);

        STEP("Check that an user with no Read permissions can't see the hold name in the add to hold event.");
        String replacementHoldName = "You don't have permission to view this hold.";
        assertEquals("The list of events should contain the Add to Hold entry", 1, auditEntries.size());
        assertTrue("The hold name should not be visible in the Add to Hold entry ",
            auditEntries.stream().anyMatch(entry -> entry.getChangedValues().contains(
                ImmutableMap.of("new", replacementHoldName, "previous", "", "name", "Hold Name"))));
    }

    @AfterClass (alwaysRun = true)
    public void cleanUpAuditAddToHoldTests()
    {
        holdsListRef.forEach(holdRef -> holdsAPI.deleteHold(getAdminUser(), holdRef));
        dataSite.usingAdmin().deleteSite(privateSite);
        asList(rmAdmin, rmManagerNoReadOnHold, rmManagerNoReadOnNode).forEach(user -> getDataUser().usingAdmin().deleteUser(user));
        deleteRecordCategory(recordCategory.getId());
    }
}
