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
package org.alfresco.rest.rm.community.records;

import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.base.TestData.NONELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.RECORD_SEARCH_ASPECT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.model.record.RecordContent;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.report.Bug;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * File Unfiled Record Action REST API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class FileRecordsTests extends BaseRMRestTest
{
    private UnfiledContainerChild electronicRecord = UnfiledContainerChild.builder()
                                                                          .name(ELECTRONIC_RECORD_NAME)
                                                                          .nodeType(CONTENT_TYPE)
                                                                          .content(RecordContent.builder().mimeType("text/plain").build())
                                                                          .build();

    private UnfiledContainerChild nonelectronicRecord = UnfiledContainerChild.builder()
                                                                             .properties(UnfiledContainerChildProperties.builder()
                                                                                                                        .description(NONELECTRONIC_RECORD_NAME)
                                                                                                                        .title("Title")
                                                                                                                        .build())
                                                                             .name(NONELECTRONIC_RECORD_NAME)
                                                                             .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                                             .build();

    private String targetFolderId, folderToLink, closedFolderId, unfiledRecordFolderId;

    @BeforeClass (alwaysRun = true)
    public void setupFileRecordsTests()
    {
        // create 3 record folders and close one of them
        targetFolderId = createCategoryFolderInFilePlan().getId();
        folderToLink = createCategoryFolderInFilePlan().getId();
        closedFolderId = createCategoryFolderInFilePlan().getId();
        closeFolder(closedFolderId);

        // create one unfiled record folder
        unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
    }

    /**
     * Invalid  containers where electronic and non-electronic records can be filed
     */
    @DataProvider (name = "invalidContainersToFile")
    public Object[][] getFolderContainers()
    {
        return new String[][] {
            { FILE_PLAN_ALIAS},
            { UNFILED_RECORDS_CONTAINER_ALIAS},
            { TRANSFERS_ALIAS },
            // an arbitrary record category
            { createRootCategory(getAdminUser(), "Category " + getRandomAlphanumeric()).getId()},
            // an arbitrary unfiled records folder
            { createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId() }
        };
    }

    /**
     * Returns the ids of unfiled electronic and non-electronic records from Unfiled Records container
     */
    @DataProvider (name = "unfiledRecordsFromUnfiledRecordsContainer")
    public Object[][] getRecordsFromUnfiledRecordsContainer()
    {
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        return new String[][] {
            { unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS,
                    createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME)).getId()},
            { unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS).getId()}
        };
    }

    /**
     * Returns the ids of unfiled electronic and non-electronic records from an Unfiled Record Folder
     */
    @DataProvider (name = "unfiledRecordsFromUnfiledRecordFolder")
    public Object[][] getRecordsFromUnfiledRecordFolder()
    {
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();

        return new String[][] {
            { unfiledRecordFoldersAPI.uploadRecord(electronicRecord, unfiledRecordFolderId,
                    createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME)).getId()},
            { unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonelectronicRecord, unfiledRecordFolderId).getId()}
        };
    }

    /**
     * Given an unfiled record in the root unfiled record container
     * And an open record folder
     * When I file the unfiled record into the record folder
     * Then the record is filed
     */
    @Test (dataProvider = "unfiledRecordsFromUnfiledRecordsContainer")
    @AlfrescoTest (jira = "RM-7060")
    public void fileRecordFromUnfiledContainerToOpenFolder(String unfiledRecordId) throws Exception
    {
        // file the record to the folder created
        Record recordFiled = fileRecordToFolder(unfiledRecordId, targetFolderId);
        // check the response status
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(recordFiled.getParentId(), targetFolderId);

        // check the record is filed to the record folder
        assertTrue(isRecordChildOfRecordFolder(unfiledRecordId, targetFolderId), unfiledRecordId + " is not filed to " + targetFolderId);

        // check the record doesn't exist into unfiled record container
        assertFalse(isRecordChildOfUnfiledContainer(unfiledRecordId), unfiledRecordId + " exists in Unfiled Records");
        assertTrue(hasAspect(unfiledRecordId, RECORD_SEARCH_ASPECT), "recordSearch aspect is lost after filing!");
    }

    /**
     * Given an unfiled record in a unfiled record folder
     * And an open record folder
     * When I file the unfiled record into the record folder
     * Then the record is filed
     */
    @Test (dataProvider = "unfiledRecordsFromUnfiledRecordFolder")
    @AlfrescoTest (jira = "RM-7060")
    public void fileRecordFromUnfiledRecordFolderToOpenFolder(String unfiledRecordId) throws Exception
    {
        // file the record to the folder created
        Record recordFiled = fileRecordToFolder(unfiledRecordId, targetFolderId);
        // check the response status
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(recordFiled.getParentId(), targetFolderId);

        // check the record is filed to the record folder
        assertTrue(isRecordChildOfRecordFolder(unfiledRecordId, targetFolderId), unfiledRecordId + " is not filed to " + targetFolderId);

        // check the record doesn't exist into unfiled record folder
        assertFalse(isRecordChildOfUnfiledRecordFolder(unfiledRecordId),
                unfiledRecordId + " exists in " + unfiledRecordFolderId);
        assertTrue(hasAspect(unfiledRecordId, RECORD_SEARCH_ASPECT), "recordSearch aspect is lost after filing!");
    }

    /**
     * Given an unfiled record in the root unfiled record container
     * And a closed record folder
     * When I file the unfiled record into the record folder
     * Then I get an unsupported operation exception
     *
     */
    @Test (dataProvider = "unfiledRecordsFromUnfiledRecordsContainer")
    public void fileRecordFromUnfiledContainerToClosedFolder(String unfiledRecordId)
    {
        // file the record to the closed record folder
        fileRecordToFolder(unfiledRecordId, closedFolderId);
        // check the response status
        assertStatusCode(FORBIDDEN);

        // check the record is not filed to the record folder
        assertFalse(isRecordChildOfRecordFolder(unfiledRecordId, closedFolderId), unfiledRecordId + " is filed to " + closedFolderId);

        // check the record exist into unfiled record container
        assertTrue(isRecordChildOfUnfiledContainer(unfiledRecordId), unfiledRecordId + " doesn't exist in Unfiled Records");
    }

    /**
     * Given an unfiled record in a unfiled record folder
     * And a closed record folder
     * When I file the unfiled record into the record folder
     * Then I get an unsupported operation exception
     *
     */
    @Test(dataProvider = "unfiledRecordsFromUnfiledRecordFolder")
    public void fileRecordFromUnfiledRecordFolderToClosedFolder(String unfiledRecordId)
    {
        // file the record into the closed folder created
        fileRecordToFolder(unfiledRecordId, closedFolderId);
        // check the response status
        assertStatusCode(FORBIDDEN);

        // check the record is not filed into the record folder
        assertFalse(isRecordChildOfRecordFolder(unfiledRecordId, closedFolderId), unfiledRecordId + " is filed to " + closedFolderId);

        // check the record exist into unfiled record folder
        assertTrue(isRecordChildOfUnfiledRecordFolder(unfiledRecordId),
                unfiledRecordId + " doesn't exist in " + unfiledRecordFolderId);
    }

    /**
     * Given a filed record in a record folder
     * And an open record folder
     * When I file the filed record into the record folder
     * Then the record is filed in both locations
     */
    @Test (dataProvider = "unfiledRecordsFromUnfiledRecordsContainer")
    @Bug (id = "RM-4578")
    public void linkRecordInto(String unfiledRecordId)
    {
        // file the record to the open folder created
        Record recordFiled = fileRecordToFolder(unfiledRecordId, targetFolderId);
        // check the response status
        assertStatusCode(CREATED);

        // link the record to the second folder
        Record recordLink = fileRecordToFolder(unfiledRecordId, folderToLink);
        assertStatusCode(CREATED);
        assertEquals(recordLink.getParentId(), targetFolderId);

        // check the record is added into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertTrue(recordFolderAPI.getRecordFolderChildren(targetFolderId)
                                  .getEntries()
                                  .stream()
                                  .anyMatch(c -> c.getEntry().getId().equals(recordFiled.getId()) &&
                                          c.getEntry().getParentId().equals(targetFolderId)));

        // check the record has a link in the second folder
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderToLink)
                                  .getEntries().stream()
                                  .anyMatch(c -> c.getEntry().getId().equals(recordFiled.getId()) &&
                                          c.getEntry().getParentId().equals(targetFolderId) &&
                                          !c.getEntry().getParentId().equals(folderToLink)));
    }

    /**
     * Given an unfiled container or filed record
     * And a container that is NOT a record folder
     * When I file the unfiled container or filed record to the container
     * Then I get an unsupported operation exception
     */
    @Test
    (
        dataProvider = "invalidContainersToFile",
        description = "File the unfiled record to the container that is not a record folder"
    )
    public void invalidContainerToFile(String containerId)
    {
        // create records
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();

        UnfiledContainerChild recordElectronic = unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        // file the records to a container that is not a record folder
        fileRecordToFolder(recordElectronic.getId(), containerId);
        assertStatusCode(BAD_REQUEST);

        fileRecordToFolder(recordNonElect.getId(), containerId);
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Files the given record in the target record folder.
     *
     * @param recordId       the id of the record to be filed
     * @param targetFolderId the id of the target record folder
     */
    private Record fileRecordToFolder(String recordId, String targetFolderId)
    {
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(targetFolderId).build();
        return getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, recordId);
    }

    /**
     * Returns whether any child of the record folder match the provided record
     *
     * @param recordId       the record id
     * @param recordFolderId the record folder id
     * @return true if any child of the record folder match the provided record, false otherwise
     */
    private boolean isRecordChildOfRecordFolder(String recordId, String recordFolderId)
    {
        return getRestAPIFactory().getRecordFolderAPI()
                                  .getRecordFolderChildren(recordFolderId)
                                  .getEntries()
                                  .stream()
                                  .anyMatch(c -> c.getEntry().getId().equals(recordId));
    }

    /**
     * Returns whether any child of the unfiled record folder match the provided record
     *
     * @param recordId the record id
     * @return true if any child of the unfiled record folder match the provided record, false otherwise
     */
    private boolean isRecordChildOfUnfiledRecordFolder(String recordId)
    {
        return getRestAPIFactory().getUnfiledRecordFoldersAPI()
                                  .getUnfiledRecordFolderChildren(unfiledRecordFolderId)
                                  .getEntries()
                                  .stream()
                                  .anyMatch(c -> c.getEntry().getId().equals(recordId));
    }

    /**
     * Returns whether any child of the unfiled container match the provided record
     *
     * @param recordId the record id
     * @return true if any child of the unfiled container match the provided record, false otherwise
     */
    private boolean isRecordChildOfUnfiledContainer(String recordId)
    {
        return getRestAPIFactory().getUnfiledContainersAPI()
                                  .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                                  .getEntries()
                                  .stream()
                                  .anyMatch(c -> c.getEntry().getId().equals(recordId));
    }
}
