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

package org.alfresco.rest.rm.community.smoke;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderCollection;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.rm.community.records.FileUnfiledRecordsTests;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class FileVersionAsRecordTests extends BaseRMRestTest {

    private UserModel nonRMuser,rmManager;
    private SiteModel testSite;
    private FileModel document, documentDeclared;
    private RecordCategory category_manager, category_admin;
    private RecordCategoryChild folder_admin, folder_manager ;
    private static final String CATEGORY_MANAGER = "catManager" + generateTestPrefix(FileAsRecordTests.class);
    private static final String CATEGORY_ADMIN = "catAdmin" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_MANAGER = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_ADMIN = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);

    @Autowired
    private DataSite dataSite;
    @Autowired
    private DataContent dataContent;
    @Autowired
    private RoleService roleService;

    @BeforeClass(alwaysRun = true)
    public void preconditionForFileVersionAsRecordTests()
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

    }

    @Test
    @AlfrescoTest (jira = "APPS-1625")
    public void fileVersionAsRecordToUnfiledRecordContainer() throws Exception
    {

        AtomicReference<RecordFolderCollection> apiChildren = new AtomicReference<>();

        STEP("Create a document with the user without RM role");
        FileModel inplaceRecord = dataContent.usingSite(testSite).usingUser(rmManager)
            .createContent(new FileModel("declareAndFileToIntoUnfiledRecordFolder",
                FileType.TEXT_PLAIN));

        STEP("Click on Declare and file without selecting a record folder");
        getRestAPIFactory().getActionsAPI(rmManager).declareAndFile(inplaceRecord,"");

        STEP("Check the file is declared in unfiled record folder");
        Assert.assertTrue(isMatchingRecordInUnfiledRecords(inplaceRecord), "Record should be filed to Unfiled Records folder");


    }


}
