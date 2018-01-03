/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
import static org.testng.AssertJUnit.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.model.record.RecordContent;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledContainerAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.utility.report.Bug;
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
                                                                  .nodeType(CONTENT_TYPE.toString())
                                                                  .content(RecordContent.builder().mimeType("text/plain").build())
                                                                  .build();

    private UnfiledContainerChild nonelectronicRecord = UnfiledContainerChild.builder()
                                                                     .properties(UnfiledContainerChildProperties.builder()
                                                                                                            .description(NONELECTRONIC_RECORD_NAME)
                                                                                                            .title("Title")
                                                                                                            .build())
                                                                     .name(NONELECTRONIC_RECORD_NAME)
                                                                     .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                     .build();

    /**
     * Invalid  containers where electronic and non-electronic records can be filed
     */
    @DataProvider (name = "invalidContainersToFile")
    public String[][] getFolderContainers() throws Exception
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
     * Given an unfiled record in the root unfiled record container
     * And an open record folder
     * When I file the unfiled record into the record folder
     * Then the record is filed
     */
    @Test
    public void fileRecordIntoExistingFolderFromUnfiledContainer() throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();

        // create records
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        UnfiledContainerChild recordElectronic = unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS,
                                                                                                  createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        Record recordFiled = recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        // check the response status
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(recordFiled.getParentId(),folderId);

        // check the record is filed into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderId)
                                         .getEntries()
                                         .stream()
                                         .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // check the record doesn't exist into unfiled record container
        assertFalse(unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // file the non-electronic record into the folder created
        Record nonElectRecordFiled = recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status code
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(nonElectRecordFiled.getParentId(), folderId);

        // check the record is added into the record folder
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderId)
                                         .getEntries()
                                         .stream()
                                         .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));

        // check the record doesn't exist into unfiled record container
        assertFalse(unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));
    }

    /**
     * Given an unfiled record in a unfiled record folder
     * And an open record folder
     * When I file the unfiled record into the record folder
     * Then the record is filed
     */
    @Test
    public void fileRecordIntoExistingFolderFromUnfiledRecordFolder() throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();

        // create records
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();

        String unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();

        UnfiledContainerChild recordElectronic = unfiledRecordFoldersAPI.uploadRecord(electronicRecord, unfiledRecordFolderId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonelectronicRecord, unfiledRecordFolderId);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        Record recordFiled = recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        // check the response status
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(recordFiled.getParentId(),folderId);

        // check the record is filed into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // check the record doesn't exist into unfiled record folder
        assertFalse(unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(unfiledRecordFolderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // file the non-electronic record into the folder created
        Record nonElectRecordFiled = recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status code
        assertStatusCode(CREATED);
        // check the parent id for the record returned
        assertEquals(nonElectRecordFiled.getParentId(), folderId);

        // check the record is added into the record folder
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));

        // check the record doesn't exist into unfiled record folder
        assertFalse(unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(unfiledRecordFolderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));
    }

    /**
     * Given an unfiled record in the root unfiled record container
     * And a closed record folder
     * When I file the unfiled record into the record folder
     * Then I get an unsupported operation exception
     *
     */
    @Test
    public void fileRecordIntoCloseFolderFromUnfiledContainer() throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();
        closeFolder(folderId);
        // create records
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();

        UnfiledContainerChild recordElectronic = unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        // check the response status
        assertStatusCode(FORBIDDEN);

        // check the record is not filed into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertFalse(recordFolderAPI.getRecordFolderChildren(folderId)
                                      .getEntries()
                                      .stream()
                                      .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // check the record exist into unfiled record container
        assertTrue(unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // file the non-electronic record into the folder created
        recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status code
        assertStatusCode(FORBIDDEN);

        // check the record is not added into the record folder
        assertFalse(recordFolderAPI.getRecordFolderChildren(folderId)
                                        .getEntries()
                                        .stream()
                                        .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));

        // check the record  exist into unfiled record container
        assertTrue(unfiledContainersAPI.getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS)
                    .getEntries().stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));
    }

    /**
     * Given an unfiled record in a unfiled record folder
     * And a closed record folder
     * When I file the unfiled record into the record folder
     * Then I get an unsupported operation exception
     *
     */
    @Test
    public void fileRecordIntoCloseFolderFromUnfiledRecordFolder() throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create a record folder
        String folderId = createCategoryFolderInFilePlan().getId();
        closeFolder(folderId);
        // create records
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();

        String unfiledRecordFolderId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
        UnfiledContainerChild recordElectronic = unfiledRecordFoldersAPI.uploadRecord(electronicRecord, unfiledRecordFolderId, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonelectronicRecord, unfiledRecordFolderId);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderId).build();
        recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        // check the response status
        assertStatusCode(FORBIDDEN);

        // check the record is not filed into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertFalse(recordFolderAPI.getRecordFolderChildren(folderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // check the record exist into unfiled record folder
        assertTrue(unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(unfiledRecordFolderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordElectronic.getId())));

        // file the non-electronic record into the folder created
        recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status code
        assertStatusCode(FORBIDDEN);

        // check the record is not added into the record folder
        assertFalse(recordFolderAPI.getRecordFolderChildren(folderId)
                    .getEntries()
                    .stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));

        // check the record  exist into unfiled record folder
        assertTrue(unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(unfiledRecordFolderId)
                    .getEntries().stream()
                    .anyMatch(c -> c.getEntry().getId().equals(recordNonElect.getId())));
    }

    /**
     * Given a filed record in a record folder
     * And an open record folder
     * When I file the filed record into the record folder
     * Then the record is filed in both locations
     */
    @Test
    @Bug(id="RM-4578")
    public void linkRecordInto() throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create a record folder
        String parentFolderId = createCategoryFolderInFilePlan().getId();

        // create records
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();
        UnfiledContainerChild recordElectronic = unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(parentFolderId).build();
        Record recordFiled = recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        Record nonElectronicFiled = recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status
        assertStatusCode(CREATED);

        // create the second folder
        String folderToLink = createCategoryFolderInFilePlan().getId();
        recordBodyFile = RecordBodyFile.builder().targetParentId(folderToLink).build();

        // check the response status
        assertStatusCode(CREATED);
        // link the electronic record
        Record recordLink = recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        assertTrue(recordLink.getParentId().equals(parentFolderId));
        // check the response status code
        assertStatusCode(CREATED);
        // link the nonelectronic record
        Record nonElectronicLink = recordsAPI.fileRecord(recordBodyFile, nonElectronicFiled.getId());
        assertStatusCode(CREATED);
        assertTrue(nonElectronicLink.getParentId().equals(parentFolderId));

        // check the record is added into the record folder
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        assertTrue(recordFolderAPI.getRecordFolderChildren(parentFolderId)
                                       .getEntries()
                                       .stream()
                                       .anyMatch(c -> c.getEntry().getId().equals(recordFiled.getId()) &&
                                           c.getEntry().getParentId().equals(parentFolderId)));

        // check the record doesn't exist into unfiled record container
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderToLink)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getEntry().getId().equals(recordFiled.getId()) &&
                                         c.getEntry().getParentId().equals(parentFolderId) &&
                                              !c.getEntry().getParentId().equals(folderToLink)));
        // check the record is added into the record folder
        assertTrue(recordFolderAPI.getRecordFolderChildren(parentFolderId)
                                      .getEntries()
                                      .stream()
                                      .anyMatch(c -> c.getEntry().getId().equals(nonElectronicFiled.getId()) &&
                                          c.getEntry().getParentId().equals(parentFolderId)));

        // check the record doesn't exist into unfiled record container
        assertTrue(recordFolderAPI.getRecordFolderChildren(folderToLink)
                                      .getEntries().stream()
                                      .anyMatch(c -> c.getEntry().getId().equals(nonElectronicFiled.getId()) &&
                                        c.getEntry().getParentId().equals(parentFolderId) &&
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
    public void invalidContainerToFile(String containerId) throws Exception
    {
        // get API instances
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        // create records
        UnfiledContainerAPI unfiledContainersAPI = getRestAPIFactory().getUnfiledContainersAPI();

        UnfiledContainerChild recordElectronic = unfiledContainersAPI.uploadRecord(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME));
        UnfiledContainerChild recordNonElect = unfiledContainersAPI.createUnfiledContainerChild(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS);

        // file the record into the folder created
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(containerId).build();
        recordsAPI.fileRecord(recordBodyFile, recordElectronic.getId());
        assertStatusCode(BAD_REQUEST);

        recordsAPI.fileRecord(recordBodyFile, recordNonElect.getId());
        // check the response status
        assertStatusCode(BAD_REQUEST);
    }
}
