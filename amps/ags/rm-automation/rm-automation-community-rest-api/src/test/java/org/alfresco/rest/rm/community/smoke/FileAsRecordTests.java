/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.testng.Assert.*;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.user.UserPermissions.PERMISSION_FILING;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderCollection;
import org.alfresco.rest.rm.community.model.user.UserRoles;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

public class FileAsRecordTests extends BaseRMRestTest
{

    private static final String CATEGORY_MANAGER = "catManager" + generateTestPrefix(FileAsRecordTests.class);
    private static final String CATEGORY_ADMIN = "catAdmin" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_MANAGER = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);
    private static final String FOLDER_ADMIN = "recordFolder" + generateTestPrefix(FileAsRecordTests.class);

    private UserModel nonRMuser, rmManager;
    private SiteModel testSite;
    private FileModel document, documentDeclared;
    private RecordCategory category_manager, category_admin;
    private RecordCategoryChild folder_admin, folder_manager;
    @Autowired
    private DataSite dataSite;
    @Autowired
    private DataContent dataContent;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RecordCategoriesAPI recordCategoriesAPI;

    /**
     * Create preconditions:
     *
     * <pre>
     *     1. RM site is created
     *     2. Two users: user without RM role and a user with RM manager role
     *     3. Two Record categories with one folder each
     *     4. User with RM MANAGER role has Filling permission over one category
     * </pre>
     */
    @BeforeClass(alwaysRun = true)
    public void preconditionForFileAsRecordRecordTests()
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
        folder_admin = createFolder(category_admin.getId(), FOLDER_ADMIN);
        folder_manager = createFolder(category_manager.getId(), FOLDER_MANAGER);

        STEP("Create an rm user and give filling permission over CATEGORY_MANAGER record category");
        RecordCategory recordCategory = new RecordCategory().builder()
                .id(category_manager.getId())
                .build();
        rmManager = roleService.createCollaboratorWithRMRoleAndPermission(testSite, recordCategory,
                UserRoles.ROLE_RM_MANAGER, PERMISSION_FILING);
    }

    /**
     * Given I have selected the record folder I want to file my declared record to When I confirm the action Then the dialog closes And the document is now shown as a record in the collaboration site And if I navigated to the record folder, as any user who had the right permissions, then I would see the record filed
     */
    @Test
    @AlfrescoTest(jira = "RM-6780")
    public void checkFileAsRecordToRecordFolder() throws Exception
    {

        AtomicReference<RecordFolderCollection> apiChildren = new AtomicReference<>();
        STEP("Create a document with the user with RM role");
        documentDeclared = dataContent.usingSite(testSite).usingUser(rmManager)
                .createContent(new FileModel("checkDeclareAndFileToRecordFolder", FileType.TEXT_PLAIN));

        STEP("Declare and file into  a record folder the document uploaded");

        getRestAPIFactory().getActionsAPI(rmManager).declareAndFile(documentDeclared,
                Utility.buildPath(CATEGORY_MANAGER, FOLDER_MANAGER));

        STEP("Check the file is a record within the collaboration site");

        try
        {
            Utility.sleep(1000, 40000, () -> {
                JSONObject collaboratorSearchJson = getSearchApi().liveSearchForDocuments(rmManager.getUsername(),
                        rmManager.getPassword(),
                        documentDeclared.getName());
                assertTrue("Rm Manager not able to find the document.", collaboratorSearchJson.getJSONArray("items").length() != 0);
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        STEP("Check the record is filed into the record folder.");
        // Get children from API
        // List children from API
        try
        {
            Utility.sleep(1000, 40000, () -> {
                apiChildren.set((RecordFolderCollection) getRestAPIFactory()
                        .getRecordFolderAPI(rmManager).getRecordFolderChildren(folder_manager.getId(), "include=properties")
                        .assertThat().entriesListIsNotEmpty().assertThat().entriesListIsNotEmpty());
            });
        }
        catch (InterruptedException e)
        {
            fail("InterruptedException received while waiting for results.");
        }

        assertEquals(apiChildren.get()
                .getEntries()
                .get(0)
                .getEntry()
                .getProperties()
                .getOriginalName(), documentDeclared.getName());
    }

    /**
     * Given I have selected the "File As Record" action When I confirm the action without selecting a location to file to Then the record is declared in the unfiled folder
     */
    @Test
    @AlfrescoTest(jira = "RM-6780")
    public void fileAsRecordToUnfiledRecordFolder() throws Exception
    {
        STEP("Create a document with the user without RM role");
        FileModel inplaceRecord = dataContent.usingSite(testSite).usingUser(rmManager)
                .createContent(new FileModel("declareAndFileToIntoUnfiledRecordFolder",
                        FileType.TEXT_PLAIN));

        STEP("Click on Declare and file without selecting a record folder");
        getRestAPIFactory().getActionsAPI(rmManager).declareAndFile(inplaceRecord, "");

        STEP("Check the file is declared in unfiled record folder");
        Assert.assertTrue(isMatchingRecordInUnfiledRecords(inplaceRecord), "Record should be filed to Unfiled Records folder");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpForFileAsRecordRecordTests()
    {
        STEP("Delete the collaboration site");
        dataSite.usingUser(nonRMuser).deleteSite(testSite);

        STEP("Empty the trashcan.");
        restClient.authenticateUser(nonRMuser).withCoreAPI().usingTrashcan().deleteNodeFromTrashcan(toContentModel(testSite.getId()));

        getRestAPIFactory()
                .getUnfiledContainersAPI(rmManager)
                .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                .getEntries()
                .stream()
                .forEach(x -> getRestAPIFactory()
                        .getRecordsAPI()
                        .deleteRecord(x.getEntry().getId()));

        STEP("Cleanup Documents inside folders");

        STEP("Delete folders");
        getRestAPIFactory().getRecordFolderAPI().deleteRecordFolder(folder_admin.getId());
        getRestAPIFactory().getRecordFolderAPI().deleteRecordFolder(folder_manager.getId());

        STEP("Delete categories");
        recordCategoriesAPI.deleteCategory(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), category_manager.getName());
        recordCategoriesAPI.deleteCategory(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), category_admin.getName());

        STEP("Delete Users");
        dataUser.deleteUser(nonRMuser);
        dataUser.deleteUser(rmManager);
    }
}
