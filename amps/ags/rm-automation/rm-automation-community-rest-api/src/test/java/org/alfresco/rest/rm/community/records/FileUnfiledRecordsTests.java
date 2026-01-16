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

package org.alfresco.rest.rm.community.records;

import static org.springframework.http.HttpStatus.CREATED;

import static org.alfresco.rest.core.v0.APIUtils.convertHTTPResponseToJSON;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

public class FileUnfiledRecordsTests extends BaseRMRestTest
{

    private final String TEST_PREFIX = generateTestPrefix(FileUnfiledRecordsTests.class);
    private final String RM_ADMIN = TEST_PREFIX + "rm_admin";
    public static final String NODE_REF_WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private UserModel testUser;
    private SiteModel testSite;
    private String unfiledRecordFolderId;
    private UserModel RmAdminUser;
    private RecordCategory rootCategory;
    private RecordCategoryChild recordFolder;
    private final String recordName = "RM-2790 record";
    private final String recordTitle = recordName + " title";
    private final String recordDescription = recordName + " description";
    /**
     * data prep services
     */
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    @Autowired
    private RecordsAPI recordsAPI;

    @BeforeClass(alwaysRun = true)
    public void preConditions()
    {

        STEP("Create RM Site");
        createRMSiteIfNotExists();

        STEP("Create RM Admin user");
        rmRolesAndActionsAPI.createUserAndAssignToRole(getAdminUser().getUsername(), getAdminUser().getPassword(), RM_ADMIN,
                getAdminUser().getPassword(),
                "Administrator");

        RmAdminUser = new UserModel(RM_ADMIN, getAdminUser().getPassword());

        STEP("Create collab_user user");
        testUser = getDataUser().createRandomTestUser();
        testSite = dataSite.usingAdmin().createPublicRandomSite();

        // invite collab_user to Collaboration site with Contributor role
        getDataUser().addUserToSite(testUser, testSite, UserRole.SiteContributor);
    }

    @Test
    @AlfrescoTest(jira = "RM-2790")
    public void fileUnfiledRecords() throws Exception
    {

        STEP("Upload the document to test site and then make it reacord");
        // Upload document in a folder in a collaboration site
        FileModel uploadedDocbyCollabUser = dataContent.usingSite(testSite)
                .usingUser(testUser)
                .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        // declare uploadedDocument as record
        Record uploadedDocRecordbyCollabUser = getRestAPIFactory().getFilesAPI(testUser)
                .declareAsRecord(uploadedDocbyCollabUser.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Create root level category");
        rootCategory = createRootCategory(RmAdminUser, getRandomName("Category"));

        STEP("Create the record folder inside the rootCategory");
        recordFolder = createFolder(RmAdminUser, rootCategory.getId(), getRandomName("Folder"));

        STEP("Create a non-electronic record by completing some of the fields");
        Map<Enum<?>, String> non_electronic_records_properties = new HashMap<>();
        non_electronic_records_properties.put(BaseAPI.RMProperty.TITLE, recordTitle);
        non_electronic_records_properties.put(BaseAPI.RMProperty.DESCRIPTION, recordDescription);
        non_electronic_records_properties.put(BaseAPI.RMProperty.NAME, recordName);
        non_electronic_records_properties.put(BaseAPI.RMProperty.PHYSICAL_SIZE, "");
        non_electronic_records_properties.put(BaseAPI.RMProperty.NUMBER_OF_COPIES, "");
        non_electronic_records_properties.put(BaseAPI.RMProperty.SHELF, "");
        non_electronic_records_properties.put(BaseAPI.RMProperty.STORAGE_LOCATION, "");
        non_electronic_records_properties.put(BaseAPI.RMProperty.BOX, "");
        non_electronic_records_properties.put(BaseAPI.RMProperty.FILE, "");

        HttpResponse nonElectronicRecordHttpResponse = recordsAPI.createNonElectronicRecord(getAdminUser().getUsername(),
                getAdminUser().getPassword(), non_electronic_records_properties, rootCategory.getName(), recordFolder.getName());

        String nonElectronicRecordId = getNodeRef(nonElectronicRecordHttpResponse);

        STEP("Check the non-electronic record has been created");
        assertStatusCode(CREATED);

        STEP("Create a electronic record by completing some of the fields");
        Map<BaseAPI.RMProperty, String> electronic_records_properties = new HashMap<>();
        electronic_records_properties.put(BaseAPI.RMProperty.DESCRIPTION, recordDescription);
        electronic_records_properties.put(BaseAPI.RMProperty.NAME, recordName);

        recordsAPI.uploadElectronicRecord(RmAdminUser.getUsername(),
                RmAdminUser.getPassword(), electronic_records_properties, recordFolder.getName(), CMISUtil.DocumentType.TEXT_PLAIN);

        CmisObject electronicRecord = recordsAPI.getRecord(RmAdminUser.getUsername(),
                RmAdminUser.getPassword(), recordFolder.getName(), electronic_records_properties.get(BaseAPI.RMProperty.NAME));

        STEP("Check the electronic record has been created");
        assertStatusCode(CREATED);

        STEP("Create a root folder under FilePlan - Unfiled");
        String unFiledFolder = createUnFileFolder();

        STEP("Move all the Unfiled Records to unFiledFolder");
        RestNodeModel uploadDocRestNodeModel = getRestAPIFactory()
                .getNodeAPI(toContentModel(uploadedDocRecordbyCollabUser.getId()))
                .move(createBodyForMoveCopy(unFiledFolder));

        RestNodeModel nonElectronicDocRestNodeModel = getRestAPIFactory()
                .getNodeAPI(toContentModel(nonElectronicRecordId))
                .move(createBodyForMoveCopy(unFiledFolder));

        RestNodeModel electronicDocRestNodeModel = getRestAPIFactory()
                .getNodeAPI(toContentModel(electronicRecord.getId()))
                .move(createBodyForMoveCopy(unFiledFolder));

        STEP("Move all the Record present in the unFiledFolder to Folder inside Root Category");

        getRestAPIFactory()
                .getNodeAPI(toContentModel(uploadDocRestNodeModel.getId()))
                .move(createBodyForMoveCopy(recordFolder.getId()));

        getRestAPIFactory()
                .getNodeAPI(toContentModel(nonElectronicDocRestNodeModel.getId()))
                .move(createBodyForMoveCopy(recordFolder.getId()));

        getRestAPIFactory()
                .getNodeAPI(toContentModel(electronicDocRestNodeModel.getId()))
                .move(createBodyForMoveCopy(recordFolder.getId()));

        getRestAPIFactory().getRecordsAPI().deleteRecord(uploadDocRestNodeModel.getId());
        getRestAPIFactory().getRecordsAPI().deleteRecord(nonElectronicDocRestNodeModel.getId());
        getRestAPIFactory().getRecordsAPI().deleteRecord(electronicDocRestNodeModel.getId());

        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
        unfiledRecordFoldersAPI.deleteUnfiledRecordFolder(unFiledFolder);

        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        String recordFolderId = recordFolder.getId();
        recordFolderAPI.deleteRecordFolder(recordFolderId);
    }

    @AfterClass(alwaysRun = true)
    public void deletePreConditions()
    {
        STEP("Delete the created rootCategory along with corresponding record folders/records present in it");
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory.getId());
    }

    private String createUnFileFolder()
    {
        String categoryName = "RM-2790 record Category name " + getRandomAlphanumeric();

        unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                categoryName + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
        return unfiledRecordFolderId;
    }

    private String getNodeRef(HttpResponse httpResponse)
    {
        return convertHTTPResponseToJSON(httpResponse).getString("persistedObject")
                .replace(NODE_REF_WORKSPACE_SPACES_STORE, "");
    }
}
