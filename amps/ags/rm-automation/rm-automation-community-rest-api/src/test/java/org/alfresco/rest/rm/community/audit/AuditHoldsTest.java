/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
import static org.alfresco.rest.rm.community.model.audit.AuditEvents.REMOVE_FROM_HOLD;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.audit.AuditEvents;
import org.alfresco.rest.rm.community.model.hold.Hold;
import org.alfresco.rest.rm.community.model.hold.HoldChild;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
public class AuditHoldsTest extends BaseRMRestTest {
    private final String PREFIX = generateTestPrefix(AuditAddToHoldTests.class);
    private final String HOLD1 = PREFIX + "hold1";
    private SiteModel publicSite;
    private FileModel testFile;
    @Autowired
    private RMAuditService rmAuditService;
    @Autowired
    private RoleService roleService;
    private UserModel rmAdmin;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder1,recordFolder2;
    private List<AuditEntry> auditEntries;
    private String hold1NodeRef;
    public static final String RECORD_FOLDER_THREE = "record-folder-three";
    @BeforeClass(alwaysRun = true)
    public void preconditionForAuditAddToHoldTests()
    {
        createRMSiteIfNotExists();
        rmAdmin = roleService.createUserWithRMRole(UserRoles.ROLE_RM_ADMIN.roleId);

        STEP("Create a hold");

        hold1NodeRef = getRestAPIFactory()
                .getFilePlansAPI(rmAdmin)
                .createHold(Hold.builder().name(HOLD1).description(HOLD_DESCRIPTION).reason(HOLD_REASON).build(), FILE_PLAN_ALIAS)
                .getId();

        STEP("Create a collaboration site with a test file.");
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        testFile = dataContent.usingAdmin().usingSite(publicSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create a record category with 2 folders and 1 record");
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder1 = createRecordFolder(recordCategory.getId(), PREFIX + "recFolder1");
        recordFolder2 = createRecordFolder(recordCategory.getId(), PREFIX + "recFolder2");
        Record recordToBeAdded = createElectronicRecord(recordFolder1.getId(), PREFIX + "record");
        assertStatusCode(CREATED);

        STEP("Add some items to the hold, then remove them from the hold");
        final List<String> itemsList = asList(testFile.getNodeRefWithoutVersion(), recordToBeAdded.getId(), recordFolder2.getId());
        getRestAPIFactory().getHoldsAPI(rmAdmin).addChildToHold(HoldChild.builder().id(recordToBeAdded.getId()).build(), hold1NodeRef);
        for(String childId : itemsList)
        {
            getRestAPIFactory().getHoldsAPI(rmAdmin).deleteHoldChild(hold1NodeRef, childId);
        }

        STEP("Delete the record folder that was held");
        getRestAPIFactory().getRecordFolderAPI().deleteRecordFolder(recordFolder2.getId());

        STEP("Rename the parent of the record that was held");
        RecordFolder recordFolder = RecordFolder.builder().name(RECORD_FOLDER_THREE).build();
        getRestAPIFactory().getRecordFolderAPI().updateRecordFolder(recordFolder, recordFolder1.getId());
    }
    /**
     * Data provider with hold events that have links to held items
     *
     * @return the hold events
     */
    @DataProvider (name = "holdsEvents")
    public Object[][] getHoldEvents()
    {
        return new AuditEvents[][]
            {
                { ADD_TO_HOLD },
                { REMOVE_FROM_HOLD }
            };
    }
    @Test (dataProvider = "holdsEvents")
    public void checkItemPathLink(AuditEvents event) {
        auditEntries = rmAuditService.getAuditEntriesFilteredByEvent(getAdminUser(), event);
        assertFalse("Audit results should not be empty",auditEntries.size()==0);
        final String auditedEvent = event + " - " + testFile.getName();
        assertTrue("Audit results should contain one " + auditedEvent + " event",auditEntries.stream().anyMatch(e -> e.getEvent().startsWith(event.eventDisplayName)));
        STEP("Check the audit log contains only an entry for add to hold.");
        assertThat(auditEntries, is(not(empty())));
    }
    @AfterClass(alwaysRun = true)
    private void cleanup() {
        dataSite.usingAdmin().deleteSite(publicSite);
        deleteRecordFolder(recordFolder1.getId());
        deleteRecordFolder(recordFolder2.getId());
        deleteRecordCategory(recordCategory.getId());
        rmAuditService.clearAuditLog();
    }
}
