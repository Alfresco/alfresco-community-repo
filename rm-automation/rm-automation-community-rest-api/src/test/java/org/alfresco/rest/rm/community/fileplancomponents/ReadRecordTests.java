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

import static org.alfresco.rest.rm.community.base.TestData.FOLDER_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_COMPLETED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentContent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * Read Records API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class ReadRecordTests extends BaseRMRestTest
{
    String CATEGORY_NAME=TestData.CATEGORY_NAME +getRandomAlphanumeric();

    String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();
    String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();

    private FilePlanComponent electronicRecord = FilePlanComponent.builder()
                                                          .name(ELECTRONIC_RECORD_NAME)
                                                          .nodeType(CONTENT_TYPE.toString())
                                                          .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                          .build();

    private  FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
                                                             .properties(FilePlanComponentProperties.builder()
                                                                                                    .description(NONELECTRONIC_RECORD_NAME)
                                                                                                    .title("Title")
                                                                                                    .build())
                                                             .name(NONELECTRONIC_RECORD_NAME)
                                                             .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                             .build();

    /**
     * Given a record category or a container which can't contain records
     * When I try to read the children filtering the results to records
     * Then I receive an empty list
     */
    @DataProvider(name="invalidContainersForRecords")
    public  Object[][] getInvalidContainersForRecords() throws Exception
    {
        return new Object[][] {
            { FILE_PLAN_ALIAS },
            { TRANSFERS_ALIAS },
            { HOLDS_ALIAS },
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

        FilePlanComponent electronicRecord = FilePlanComponent.builder()
                                                              .name(ELECTRONIC_RECORD_NAME)
                                                              .nodeType(CONTENT_TYPE.toString())
                                                              .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                              .build();
        FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
                                                                 .properties(FilePlanComponentProperties.builder()
                                                                                                        .description("Description")
                                                                                                        .title("Title")
                                                                                                        .build())
                                                                 .name(NONELECTRONIC_RECORD_NAME)
                                                                 .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                 .build();
        //create records
        getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(electronicRecord, container);
        getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, container);


        // List children from API
        getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(container, "where=(isFile=true)")
                           .assertThat()//check the list returned is empty
                           .entriesListIsEmpty().assertThat().paginationExist();
        //check response status code
        assertStatusCode(OK);
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
               //create the containers from the relativePath
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                                                          .name(FOLDER_NAME)
                                                          .nodeType(RECORD_FOLDER_TYPE.toString())
                                                          .relativePath(RELATIVE_PATH)
                                                          .build();
        String folderId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(recordFolder, FILE_PLAN_ALIAS.toString()).getId();
        //create electronic record
        String recordWithContentId = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), folderId).getId();
        //Get the record created
        FilePlanComponent recordWithContent=getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(recordWithContentId, "include = "+IS_COMPLETED);
        //Check the metadata returned
        assertTrue(recordWithContent.getName().startsWith(ELECTRONIC_RECORD_NAME));
        assertTrue(recordWithContent.getIsFile());
        assertFalse(recordWithContent.getIsCategory());
        assertFalse(recordWithContent.getIsRecordFolder());
        assertNotNull(recordWithContent.getContent().getEncoding());
        assertEquals(recordWithContent.getNodeType(),CONTENT_TYPE.toString());
        assertNotNull(recordWithContent.getContent().getEncoding());
        assertNotNull(recordWithContent.getContent().getMimeType());
        assertNotNull(recordWithContent.getAspectNames());
        assertStatusCode(OK);

        //create non-electronic record
        String nonElectronicRecordId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, folderId).getId();
        //Get the record created
        FilePlanComponent nonElectronicRecord = getRestAPIFactory().getFilePlanComponentsAPI().getFilePlanComponent(nonElectronicRecordId, "include = " + IS_COMPLETED);

        //Check the metadata returned
        assertTrue(nonElectronicRecord.getName().startsWith(NONELECTRONIC_RECORD_NAME));
        assertTrue(nonElectronicRecord.getIsFile());
        assertFalse(nonElectronicRecord.getIsCategory());
        assertFalse(nonElectronicRecord.getIsRecordFolder());
        assertNotNull(nonElectronicRecord.getContent().getEncoding());
        assertEquals(nonElectronicRecord.getNodeType(), NON_ELECTRONIC_RECORD_TYPE.toString());
        assertNotNull(nonElectronicRecord.getContent().getEncoding());
        assertNotNull(nonElectronicRecord.getContent().getMimeType());
        assertNotNull(nonElectronicRecord.getAspectNames());
        assertEquals(nonElectronicRecord.getProperties().getDescription(), NONELECTRONIC_RECORD_NAME);
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
        String RECORD_ELECTRONIC = "Record " + getRandomAlphanumeric();
        String RELATIVE_PATH = "/"+CATEGORY_NAME+ getRandomAlphanumeric()+"/folder";
        //create the containers from the relativePath
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                                                    .name(FOLDER_NAME)
                                                    .nodeType(RECORD_FOLDER_TYPE.toString())
                                                    .relativePath(RELATIVE_PATH)
                                                    .build();
        String folderId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(recordFolder,FILE_PLAN_ALIAS.toString()).getId();
        //
        FilePlanComponent record = FilePlanComponent.builder()
                                                    .name(RECORD_ELECTRONIC)
                                                    .nodeType(CONTENT_TYPE.toString())
                                                    .build();
        String recordId = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(record, createTempFile(RECORD_ELECTRONIC, RECORD_ELECTRONIC), folderId).getId();

        assertEquals(getRestAPIFactory().getRecordsAPI().getRecordContentText(recordId),RECORD_ELECTRONIC);
        // Check status code
        assertStatusCode(OK);

        FilePlanComponent recordNoContent = FilePlanComponent.builder()
                                                    .name(RECORD_ELECTRONIC)
                                                    .nodeType(CONTENT_TYPE.toString())
                                                    .build();
        String recordNoContentId = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(recordNoContent,folderId).getId();
        assertTrue(getRestAPIFactory().getRecordsAPI().getRecordContentText(recordNoContentId).toString().isEmpty());
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
        FilePlanComponent record = FilePlanComponent.builder()
                                                    .name(NONELECTRONIC_RECORD_NAME)
                                                    .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                    .relativePath("/"+CATEGORY_NAME+getRandomAlphanumeric()+"/"+FOLDER_NAME)
                                                    .build();

        String nonElectronicRecord= getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(record,FILE_PLAN_ALIAS.toString()).getId();


        assertTrue(getRestAPIFactory().getRecordsAPI().getRecordContentText(nonElectronicRecord).toString().isEmpty());
        assertStatusCode(OK);

    }

    /**
     * Given a container (eg record folder, record category, etc)
     * When I try to read the content
     * Then I receive an error
     */
    @Test
    (
        dataProvider = "getContainers",
        dataProviderClass = TestData.class,
        description = "Reading records from invalid containers"
    )
    public void readContentFromInvalidContainers(String container) throws Exception
    {
        getRestAPIFactory().getRecordsAPI().getRecordContentText(container).toString();
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a container that is a record/unfiled folder
     * When I try to record the containers records
     * Then I receive a list of all the records contained within the record/unfiled folder
     */

    /** Valid root containers where electronic and non-electronic records can be created */
    @DataProvider (name = "folderContainers")
    public Object[][] getFolderContainers() throws Exception
    {
        return new Object[][] {
            // an arbitrary record folder
            { createCategoryFolderInFilePlan().getId()},
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), "Unfiled Folder " + getRandomAlphanumeric()).getId() }
        };
    }

    @Test
    (
        dataProvider ="folderContainers",
        description ="List the records from record folder/unfiled record folder"
    )
    public void readRecordsFromFolders(String containerId) throws Exception
    {
        final int NUMBER_OF_RECORDS = 5;
        //String RELATIVE_PATH = "/" + CATEGORY_NAME + getRandomAlphanumeric();

        // Create Electronic Records
        ArrayList<FilePlanComponent> children = new ArrayList<FilePlanComponent>();
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            //build de electronic record
            FilePlanComponent record = FilePlanComponent.builder()
                                                        .name(ELECTRONIC_RECORD_NAME +i)
                                                        .nodeType(CONTENT_TYPE.toString())
                                                        .build();
            //create a child
            FilePlanComponent child = getRestAPIFactory().getFilePlanComponentsAPI().createElectronicRecord(record, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i ), containerId);
            children.add(child);
        }
        //Create NonElectronicRecords
        for (int i = 0; i < NUMBER_OF_RECORDS; i++)
        {
            FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
                                                                     .properties(FilePlanComponentProperties.builder()
                                                                                                            .description("Description")
                                                                                                            .title("Title")
                                                                                                            .build())
                                                                     .name(NONELECTRONIC_RECORD_NAME+i)
                                                                     .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                     .build();
            //create records
            FilePlanComponent child= getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonelectronicRecord, containerId);
            children.add(child);
        }

        // List children from API
        FilePlanComponentsCollection apiChildren =
            (FilePlanComponentsCollection) getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(containerId).assertThat().entriesListIsNotEmpty();

        // Check status code
        assertStatusCode(OK);


        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
            {
                FilePlanComponent filePlanComponent = c.getFilePlanComponentModel();
                assertNotNull(filePlanComponent.getId());
                logger.info("Checking child " + filePlanComponent.getId());

                try
                {
                    // Find this child in created children list
                    FilePlanComponent createdComponent = children.stream()
                                                                 .filter(child -> child.getId().equals(filePlanComponent.getId()))
                                                                 .findFirst()
                                                                 .get();

                    // Created by
                    assertEquals(filePlanComponent.getCreatedByUser().getId(), getAdminUser().getUsername());

                    // Is parent Id set correctly
                    assertEquals(filePlanComponent.getParentId(), containerId);
                    assertTrue(filePlanComponent.getIsFile());

                    // Boolean properties related to node type
                    assertFalse(filePlanComponent.getIsRecordFolder());
                    assertFalse(filePlanComponent.getIsCategory());

                    //assertEquals(createdComponent.getName(), filePlanComponent.getName());
                    assertTrue(filePlanComponent.getName().startsWith(createdComponent.getName()));
                    assertEquals(createdComponent.getNodeType(), filePlanComponent.getNodeType());

                } catch (NoSuchElementException e)
                {
                    fail("No child element for " + filePlanComponent.getId());
                }
            }
            );
    }

    /**
     * Given a record
     * When I try to read the children
     * Then I receive error
     */
    @Test
    public void readChildrenOnRecordsString() throws Exception
    {
        String RELATIVE_PATH = "CATEGORY" + getRandomAlphanumeric() + "/FOLDER";
        FilePlanComponent electRecord = FilePlanComponent.builder()
                                                              .name(ELECTRONIC_RECORD_NAME)
                                                              .nodeType(CONTENT_TYPE.toString())
                                                              .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                              .build();
        FilePlanComponent nonElectronic = FilePlanComponent.builder()
                                                                 .properties(FilePlanComponentProperties.builder()
                                                                                                        .description(NONELECTRONIC_RECORD_NAME)
                                                                                                        .title("Title")
                                                                                                        .build())
                                                                 .name(NONELECTRONIC_RECORD_NAME)
                                                                 .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                 .build();

        //create records in Unfiled Container
        FilePlanComponent recordElecInUnfiled = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(electRecord, UNFILED_RECORDS_CONTAINER_ALIAS.toString());
        FilePlanComponent recordNonElecInUnfiled = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonElectronic, UNFILED_RECORDS_CONTAINER_ALIAS.toString());

        // List children for the electronic Record
        getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(recordElecInUnfiled.getId(), "where=(isFile=true)")
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        assertStatusCode(OK);

        // List children for the nonElectronic Record
        getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(recordNonElecInUnfiled.getId(), "where=(isFile=true)")
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        assertStatusCode(OK);

        //Update the Records objects
        electRecord.setRelativePath(RELATIVE_PATH);
        nonElectronic.setRelativePath(RELATIVE_PATH);

        //create records in Unfiled Container
        FilePlanComponent recordElecFromRecordFolder = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(electRecord, FILE_PLAN_ALIAS.toString());
        FilePlanComponent recordNonElecFromRecordFolder = getRestAPIFactory().getFilePlanComponentsAPI().createFilePlanComponent(nonElectronic, FILE_PLAN_ALIAS.toString());

        // List children for the electronic Record
        getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(recordElecFromRecordFolder.getId(), "where=(isFile=true)")
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
       assertStatusCode(OK);

        // List children for the nonElectronic Record
        getRestAPIFactory().getFilePlanComponentsAPI().listChildComponents(recordNonElecFromRecordFolder.getId(), "where=(isFile=true)")
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        assertStatusCode(OK);
    }
}
