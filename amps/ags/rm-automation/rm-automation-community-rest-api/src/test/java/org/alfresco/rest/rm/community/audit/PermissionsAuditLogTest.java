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

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.model.audit.AuditEvents;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.RMAuditAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.service.RMAuditService;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AssertionErrors;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_REF_WORKSPACE_SPACES_STORE;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.*;
import static org.alfresco.rest.rm.community.records.SearchRecordsTests.ADMIN;
import static org.alfresco.rest.rm.community.records.SearchRecordsTests.ROLE_RM_USER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class PermissionsAuditLogTest extends BaseRMRestTest {

    private Optional<UserModel> rmAdmin;

    @Autowired
    private RecordFoldersAPI recordFoldersAPI;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;

    @Autowired
    private RMAuditAPI auditLog;
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    @Autowired
    private RMAuditService rmAuditService;

    private static final String TEST_PREFIX = generateTestPrefix(PermissionsAuditLogTest.class);
    private static final String AUDIT_USER = TEST_PREFIX + "user";
    private static final String AUDIT_CATEGORY = TEST_PREFIX + "category";
    private static final String AUDIT_SUB_CATEGORY = TEST_PREFIX + "sub-category";
    private static final String AUDIT_FOLDER = TEST_PREFIX + "folder";
    private static final String AUDIT_ELECTRONIC_RECORD = TEST_PREFIX + "electronic record";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    private RecordCategory category1,category2;

    private RecordCategoryChild recordFolder1;
    private Record electronicRecord;


    @BeforeClass (alwaysRun = true)
    public void permissionsAuditLogSetup()
    {
        createRMSiteIfNotExists();
        rmAdmin = Optional.ofNullable(getDataUser().createRandomTestUser());
        rmRolesAndActionsAPI.assignRoleToUser(
                getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),
                rmAdmin.get().getUsername(),
                "Administrator");
        auditLog.clearAuditLog(rmAdmin.get().getUsername(),rmAdmin.get().getPassword());
        category1 = createRootCategory(getRandomName("recordCategory"), DESCRIPTION);
        recordFolder1 = createFolder(category1.getId(),TITLE);
        electronicRecord = createElectronicRecord(recordFolder1.getId(),AUDIT_ELECTRONIC_RECORD,rmAdmin.get());
    }

    @Test
    public void categoryPermissionsAuditLog()
    {

        roleService.assignUserPermissionsOnCategoryAndRMRole(rmAdmin.get(),category1.getId(),PERMISSION_READ_RECORDS,ADMIN);

        List<AuditEntry> auditEntries= auditLog.getRMAuditLogAll(getAdminUser().getUsername(),getAdminUser().getPassword(),100);

        roleService.reassignUserPermissionsOnCategoryAndRMRole(rmAdmin.get(),category1.getId(),PERMISSION_READ_RECORDS,PERMISSION_FILING,ADMIN);

        AssertionErrors.assertTrue("Set Permission Event is not present.",auditEntries.stream().anyMatch(x -> x.getEvent().startsWith("Set Permission")));

    }
    @AfterClass(alwaysRun = true)
    private void permissionsAuditLogCleanup()
    {
        deleteRecord(electronicRecord.getId());
        deleteRecordFolder(recordFolder1.getId());
        deleteRecordCategory(category1.getId());
        dataUser.usingAdmin().deleteUser(new UserModel(rmAdmin.get().getUsername(), rmAdmin.get().getPassword()));
    }

}
