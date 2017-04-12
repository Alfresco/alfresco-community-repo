/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.fileplancomponents;

import static org.alfresco.rest.rm.community.base.TestData.RECORD_CATEGORY_NAME;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_CATEGORY_TITLE;
import static org.alfresco.rest.rm.community.base.TestData.RECORD_FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_COMPLETED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.CONTENT;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createRecordCategoryModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordContent;
import org.alfresco.rest.rm.community.model.record.RecordProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordCategoryAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for Read Records API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class ReadRecordTests extends BaseRMRestTest
{
    public static final String CATEGORY_NAME = TestData.RECORD_CATEGORY_NAME + getRandomAlphanumeric();

    public static final String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();
    public static final String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();

    private Record electronicRecord = Record.builder()
                                         .name(ELECTRONIC_RECORD_NAME)
                                         .nodeType(CONTENT_TYPE)
                                         .build();

    private  Record nonelectronicRecord = Record.builder()
                                             .properties(RecordProperties.builder()
                                                           .description(NONELECTRONIC_RECORD_NAME)
                                                           .title("Title")
                                                           .build())
                                             .name(NONELECTRONIC_RECORD_NAME)
                                             .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                             .build();

    /**
     * Given a record category or a container which can't contain records
     * When I try to read the children filtering the results to records
     * Then I receive an empty list
     */
    @DataProvider(name="invalidContainersForRecords")
    public  String[][] getInvalidContainersForRecords() throws Exception
    {
        return new String[][] {
            { FILE_PLAN_ALIAS },
            { TRANSFERS_ALIAS },
            { createCategoryFolderInFilePlan().getParentId()}
        };
    }
    @Test
    (
    dataProvider ="invalidContainersForRecords",
    description ="Reading records from invalid containers"
    )
    public void readRecordsFromInvalidContainers(String container) throws Exception
    {
        Record electronicRecord = Record.builder()
                                              .name(ELECTRONIC_RECORD_NAME)
                                              .nodeType(CONTENT_TYPE)
                                              .content(RecordContent.builder().mimeType("text/plain").build())
                                              .build();
        Record nonelectronicRecord = Record.builder()
                                                 .properties(RecordProperties.builder()
                                                                .description("Description")
                                                                .title("Title")
                                                                .build())
                                                 .name(NONELECTRONIC_RECORD_NAME)
                                                 .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                 .build();
        //create records
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        recordFolderAPI.createRecord(electronicRecord, container);
        assertStatusCode(BAD_REQUEST);
        recordFolderAPI.createRecord(nonelectronicRecord, container);
        assertStatusCode(BAD_REQUEST);

        if(FILE_PLAN_ALIAS.equals(container))
        {
            getRestAPIFactory().getFilePlansAPI().getRootRecordCategories(container, "")
                                                    .assertThat()//check the list returned is not empty
                                                    .entriesListIsNotEmpty().assertThat().paginationExist();
            //check response status code
            assertStatusCode(OK);
        }
        else if(TRANSFERS_ALIAS.equals(container))
        {
            getRestAPIFactory().getTransferContainerAPI().getTransfers(container, "where=(isFile=true)")
                                                            .assertThat()//check the list returned is empty
                                                            .entriesListIsEmpty().assertThat().paginationExist();
            //check response status code
            assertStatusCode(OK);
        }
        else
        {
            String recordCategoryId = getRestAPIFactory().getRecordCategoryAPI().getRecordCategory(container).getId();
            assertStatusCode(OK);
            getRestAPIFactory().getRecordCategoryAPI().getRecordCategoryChildren(recordCategoryId)
                                                            .assertThat()//check the list returned is empty
                                                            .entriesListCountIs(1).assertThat().paginationExist();
            String nodeType = getRestAPIFactory().getRecordCategoryAPI().getRecordCategoryChildren(recordCategoryId).getEntries().get(0).getEntry().getNodeType();
            assertEquals(nodeType, RECORD_FOLDER_TYPE);
            //check response status code
            assertStatusCode(OK);
        }
    }


    /**
     * Given a record
     * When I try to read the meta-data
     * Then I successfully receive the meta-data values for that record
     */
    @Test
    public void readRecordMetadata() throws Exception
    {
        String RELATIVE_PATH = "/" + CATEGORY_NAME + getRandomAlphanumeric() + "/folder";

        RecordCategory recordCategoryModel = createRecordCategoryModel(RECORD_CATEGORY_NAME, RECORD_CATEGORY_TITLE);
        String recordCategoryId = getRestAPIFactory().getFilePlansAPI().createRootRecordCategory(recordCategoryModel, FILE_PLAN_ALIAS).getId();

        //create the containers from the relativePath
        RecordCategoryChild recordFolderModel = RecordCategoryChild.builder()
                                                    .name(RECORD_FOLDER_NAME)
                                                    .nodeType(RECORD_FOLDER_TYPE)
                                                    .relativePath(RELATIVE_PATH)
                                                    .build();

        String recordFolderId = getRestAPIFactory().getRecordCategoryAPI().createRecordCategoryChild(recordFolderModel, recordCategoryId,  "include=" + PATH).getId();

        //create electronic record
        String recordWithContentId = getRestAPIFactory().getRecordFolderAPI().createRecord(electronicRecord, recordFolderId, getFile(IMAGE_FILE)).getId();

        //Get the record created
        Record recordWithContent= getRestAPIFactory().getRecordsAPI().getRecord(recordWithContentId, "include="+IS_COMPLETED +"," + CONTENT);

        //Check the metadata returned
        assertTrue(recordWithContent.getName().startsWith(ELECTRONIC_RECORD_NAME));
        assertNotNull(recordWithContent.getContent().getEncoding());
        assertEquals(recordWithContent.getNodeType(),CONTENT_TYPE);
        assertNotNull(recordWithContent.getContent().getEncoding());
        assertNotNull(recordWithContent.getContent().getMimeType());
        assertNotNull(recordWithContent.getAspectNames());
        assertFalse(recordWithContent.getName().equals(ELECTRONIC_RECORD_NAME));
        assertTrue(recordWithContent.getName().contains(recordWithContent.getProperties().getIdentifier()));
        assertStatusCode(OK);

        //create non-electronic record
        String nonElectronicRecordId = getRestAPIFactory().getRecordFolderAPI().createRecord(nonelectronicRecord, recordFolderId).getId();
        //Get the record created
        Record nonElectronicRecord = getRestAPIFactory().getRecordsAPI().getRecord(nonElectronicRecordId, "include=" + IS_COMPLETED +"," + CONTENT);

        //Check the metadata returned
        assertTrue(nonElectronicRecord.getName().startsWith(NONELECTRONIC_RECORD_NAME));
        assertEquals(nonElectronicRecord.getContent(), null);
        assertEquals(nonElectronicRecord.getNodeType(), NON_ELECTRONIC_RECORD_TYPE);
        assertNotNull(nonElectronicRecord.getAspectNames());
        assertEquals(nonElectronicRecord.getProperties().getDescription(), NONELECTRONIC_RECORD_NAME);
        assertFalse(nonElectronicRecord.getName().equals(NONELECTRONIC_RECORD_NAME));
        assertTrue(nonElectronicRecord.getName().contains(nonElectronicRecord.getProperties().getIdentifier()));
        assertStatusCode(OK);
    }

    /**
     * Given an electronic record
     * When I try to read the content
     * Then I successfully receive the content of the record
     */
    @Test
    public void readRecordContent() throws Exception
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();

        String RECORD_ELECTRONIC = "Record " + getRandomAlphanumeric();
        String RECORD_ELECTRONIC_BINARY = "Binary Record" + getRandomAlphanumeric();
        String existentRecordCategoryId = createCategoryFolderInFilePlan().getParentId();

        String RELATIVE_PATH = "/" + CATEGORY_NAME + getRandomAlphanumeric() + "/folder";

        // create the containers from the relativePath
        RecordCategoryChild recordFolder = RecordCategoryChild.builder()
                                                    .name(RECORD_FOLDER_NAME)
                                                    .nodeType(RECORD_FOLDER_TYPE)
                                                    .relativePath(RELATIVE_PATH)
                                                    .build();
        RecordCategoryAPI recordCategoryAPI = getRestAPIFactory().getRecordCategoryAPI();
        String folderId = recordCategoryAPI.createRecordCategoryChild(recordFolder, existentRecordCategoryId).getId();

        // text file as an electronic record
        Record recordText = Record.builder()
                                        .name(RECORD_ELECTRONIC)
                                        .nodeType(CONTENT_TYPE)
                                        .build();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        String recordId = recordFolderAPI.createRecord(recordText, folderId, createTempFile(RECORD_ELECTRONIC, RECORD_ELECTRONIC)).getId();
        assertEquals(recordsAPI.getRecordContent(recordId).asString(), RECORD_ELECTRONIC);
        // Check status code
        assertStatusCode(OK);

        // binary file as an electronic record
        Record recordBinary = Record.builder()
            .name(RECORD_ELECTRONIC_BINARY)
            .nodeType(CONTENT_TYPE)
            .build();

        String binaryRecordId = recordFolderAPI.createRecord(recordBinary, folderId, getFile(IMAGE_FILE)).getId();
        // binary content, therefore compare respective SHA1 checksums in order to verify this is identical content
        try
        (
            InputStream recordContentStream = recordsAPI.getRecordContent(binaryRecordId).asInputStream();
            FileInputStream localFileStream = new FileInputStream(getFile(IMAGE_FILE));
        )
        {
            assertEquals(DigestUtils.sha1(recordContentStream), DigestUtils.sha1(localFileStream));
        }
        assertStatusCode(OK);

        // electronic record with no content
        Record recordNoContent = Record.builder()
                                                    .name(RECORD_ELECTRONIC)
                                                    .nodeType(CONTENT_TYPE)
                                                    .build();
        String recordNoContentId = recordFolderAPI.createRecord(recordNoContent,folderId).getId();
        assertTrue(recordsAPI.getRecordContent(recordNoContentId).asString().isEmpty());
        assertStatusCode(OK);
    }
    /**
     * Given a non-electronic record
     * When I try to read the content
     * Then I am informed that the record has no content
     */
    @Test
    public void readNonElectronicRecordContent() throws Exception
    {

        String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();
        String folderId = createCategoryFolderInFilePlan().getId();
        Record record = Record.builder()
                                    .name(NONELECTRONIC_RECORD_NAME)
                                    .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                    .build();

        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        String nonElectronicRecord = recordFolderAPI.createRecord(record, folderId).getId();

        getRestAPIFactory().getRecordsAPI().getRecordContent(nonElectronicRecord);
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a container (eg record folder, record category, etc)
     * When I try to read the content
     * Then I receive an error
     */
    @DataProvider(name="noContentNodes")
    public  String[][] getNonRecordTypes() throws Exception
    {
        return new String[][] {
            { getFilePlan(FILE_PLAN_ALIAS).getId() },
            { getTransferContainer(TRANSFERS_ALIAS).getId() },
            { createCategoryFolderInFilePlan().getParentId()}
        };
    }
    @Test
    (
        dataProvider = "noContentNodes",
        description = "Reading records from invalid containers"
    )
    public void readContentFromInvalidContainers(String container) throws Exception
    {
        getRestAPIFactory().getRecordsAPI().getRecordContent(container).asString();
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a container that is a record folder
     * When I try to record the containers records
     * Then I receive a list of all the records contained within the record folder
     */
    @Test
    public void readRecordsFromRecordFolder() throws Exception
    {
        final int NUMBER_OF_RECORDS = 5;
        String containerId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        // Create Electronic Records
        ArrayList<Record> children = new ArrayList<Record>();
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            //build the electronic record
            Record record =  Record.builder()
                        .name(ELECTRONIC_RECORD_NAME + i)
                        .nodeType(CONTENT_TYPE)
                        .build();
            //create a child
            Record child = recordFolderAPI.createRecord(record, containerId, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i ));

            children.add(child);
        }
        //Create NonElectronicRecords
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            Record nonelectronicRecord =  Record.builder()
                        .properties(RecordProperties.builder()
                                    .description("Description")
                                    .title("Title")
                                    .build())
                        .name(NONELECTRONIC_RECORD_NAME+i)
                        .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                        .build();
            //create records
            Record child = recordFolderAPI.createRecord(nonelectronicRecord, containerId);

            children.add(child);
        }

        // List children from API
        RecordFolderCollection apiChildren = (RecordFolderCollection) recordFolderAPI.getRecordFolderChildren(containerId).assertThat().entriesListIsNotEmpty();

        // Check status code
        assertStatusCode(OK);


        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
        {
            Record record = c.getEntry();
            assertNotNull(record.getId());
            logger.info("Checking child " + record.getId());

            try
            {
                // Find this child in created children list
                Record createdComponent = children.stream()
                            .filter(child -> child.getId().equals(record.getId()))
                            .findFirst()
                            .get();

                // Created by
                assertEquals(record.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent Id set correctly
                assertEquals(record.getParentId(), containerId);

                //check the record name
                assertTrue(record.getName().equals(createdComponent.getName()));
                assertTrue(createdComponent.getName().contains(createdComponent.getProperties().getIdentifier()));
                assertEquals(createdComponent.getNodeType(), record.getNodeType());

            }
            catch (NoSuchElementException e)
            {
                fail("No child element for " + record.getId());
            }
        });
    }

    /**
     * Given a container that is a unfiled record folder
     * When I try to record the containers records
     * Then I receive a list of all the records contained within the unfiled record folder
     */
    @Test
    public void readRecordsFromUnfiledRecordFolder() throws Exception
    {
        final int NUMBER_OF_RECORDS = 5;
        String containerId = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, "Unfiled Folder " + getRandomAlphanumeric(), UNFILED_RECORD_FOLDER_TYPE).getId();
        //we have unfiled record folder
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();
        // Create Electronic Records
        ArrayList<UnfiledContainerChild> children = new ArrayList<UnfiledContainerChild>();
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            //build the electronic record
            UnfiledContainerChild record =  UnfiledContainerChild.builder()
                        .name(ELECTRONIC_RECORD_NAME + i)
                        .nodeType(CONTENT_TYPE)
                        .build();
            //create a child
            UnfiledContainerChild child = unfiledRecordFoldersAPI.uploadRecord(record, containerId, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i ));

            children.add(child);
        }
        //Create NonElectronicRecords
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            UnfiledContainerChild nonelectronicRecord =  UnfiledContainerChild.builder()
                        .properties(UnfiledContainerChildProperties.builder()
                                    .description("Description")
                                    .title("Title")
                                    .build())
                        .name(NONELECTRONIC_RECORD_NAME+i)
                        .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                        .build();
            //create records
            UnfiledContainerChild child = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonelectronicRecord, containerId);

            children.add(child);
        }

        // List children from API
        UnfiledContainerChildCollection apiChildren = (UnfiledContainerChildCollection) unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(containerId).assertThat().entriesListIsNotEmpty();

        // Check status code
        assertStatusCode(OK);


        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
        {
            UnfiledContainerChild record = c.getEntry();
            assertNotNull(record.getId());
            logger.info("Checking child " + record.getId());

            try
            {
                // Find this child in created children list
                UnfiledContainerChild createdComponent = children.stream()
                            .filter(child -> child.getId().equals(record.getId()))
                            .findFirst()
                            .get();

                // Created by
                assertEquals(record.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent Id set correctly
                assertEquals(record.getParentId(), containerId);
                assertTrue(record.getIsRecord());

                // Boolean properties related to node type
                assertFalse(record.getIsUnfiledRecordFolder());

                //check the record name
                assertTrue(record.getName().equals(createdComponent.getName()));
                assertTrue(createdComponent.getName().contains(createdComponent.getProperties().getIdentifier()));
                assertEquals(createdComponent.getNodeType(), record.getNodeType());

            }
            catch (NoSuchElementException e)
            {
                fail("No child element for " + record.getId());
            }
        });
    }
}
