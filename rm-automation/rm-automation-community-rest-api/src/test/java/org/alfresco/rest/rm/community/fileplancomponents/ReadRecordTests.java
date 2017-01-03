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

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.base.TestData;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentContent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.community.requests.FilePlanComponentAPI;
import org.alfresco.rest.rm.community.requests.RecordsAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * Read Records API
 *
 * @author Rodica Sutu
 * @since 2.6
 */
public class ReadRecordTests extends BaseRestTest
{

    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;
    @Autowired
    private RecordsAPI recordsAPI;

    @Autowired
    private DataUser dataUser;

    String CATEGORY_NAME=TestData.CATEGORY_NAME +getRandomAlphanumeric();

    String ELECTRONIC_RECORD_NAME = "Record electronic" + getRandomAlphanumeric();
    String NONELECTRONIC_RECORD_NAME = "Record nonelectronic" + getRandomAlphanumeric();

    FilePlanComponent electronicRecord = FilePlanComponent.builder()
                                                          .name(ELECTRONIC_RECORD_NAME)
                                                          .nodeType(CONTENT_TYPE.toString())
                                                          .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                          .build();
    
    FilePlanComponent nonelectronicRecord = FilePlanComponent.builder()
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
            { FILE_PLAN_ALIAS.toString() },
            { TRANSFERS_ALIAS.toString() },
            { HOLDS_ALIAS.toString() },
            { createCategory(FILE_PLAN_ALIAS.toString(), CATEGORY_NAME).getId()}
        };
    }
    @Test
    (
    dataProvider ="invalidContainersForRecords",
    description ="Reading records from invalid containers"
    )
    public void readRecordsFromInvalidContainers(String container) throws Exception
    {

        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        FilePlanComponent electronicRecord = FilePlanComponent.builder()
                                                              .name(ELECTRONIC_RECORD_NAME)
                                                              .nodeType(CONTENT_TYPE.toString())
                                                              .content(FilePlanComponentContent.builder().mimeType("text/plain").build())
                                                              .build();
        FilePlanComponent nonelectronicRecord= FilePlanComponent.builder()
                                                                .properties(FilePlanComponentProperties.builder()
                                                                                                       .description("Description")
                                                                                                       .title("Title")
                                                                                                       .build())
                                                                .name(NONELECTRONIC_RECORD_NAME)
                                                                .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
                                                                .build();
        //create records
        filePlanComponentAPI.createFilePlanComponent(electronicRecord,container);
        filePlanComponentAPI.createFilePlanComponent(nonelectronicRecord, container);

        // List children from API
        filePlanComponentAPI.withParams("where=(isFile=true)").listChildComponents(container)
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);
    }
    //TODO MAYBE Update AC ??
    /**
     * Given a record
     * When I try to read the children
     * Then I receive error
     */
    @Test
    public void readChildrenOnRecordsString() throws Exception
    {
        String RELATIVE_PATH="CATEGORY"+ getRandomAlphanumeric()+"/FOLDER";

        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        //create records in Unfiled Container
        FilePlanComponent recordElecInUnfiled = filePlanComponentAPI.createFilePlanComponent(electronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS.toString());
        FilePlanComponent recordNonElecInUnfiled = filePlanComponentAPI.createFilePlanComponent(nonelectronicRecord, UNFILED_RECORDS_CONTAINER_ALIAS.toString());

        // List children for the electronic Record
        filePlanComponentAPI.withParams("where=(isFile=true)").listChildComponents(recordElecInUnfiled.getId())
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // List children for the nonElectronic Record
        filePlanComponentAPI.withParams("where=(isFile=true)").listChildComponents(recordNonElecInUnfiled.getId())
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        //Update the Records objects
        electronicRecord.setRelativePath(RELATIVE_PATH);
        nonelectronicRecord.setRelativePath(RELATIVE_PATH);

        //create records in Unfiled Container
        FilePlanComponent recordElecFromRecordFolder = filePlanComponentAPI.createFilePlanComponent(electronicRecord, FILE_PLAN_ALIAS.toString());
        FilePlanComponent recordNonElecFromRecordFolder = filePlanComponentAPI.createFilePlanComponent(nonelectronicRecord, FILE_PLAN_ALIAS.toString());

        // List children for the electronic Record
        filePlanComponentAPI.withParams("where=(isFile=true)").listChildComponents(recordElecFromRecordFolder.getId())
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        // List children for the nonElectronic Record
        filePlanComponentAPI.withParams("where=(isFile=true)").listChildComponents(recordNonElecFromRecordFolder.getId())
                            //check the list returned is empty
                            .assertThat().entriesListIsEmpty().assertThat().paginationExist();
        // Check status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);
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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //create the containers from the relativePath
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                                                          .name(FOLDER_NAME)
                                                          .nodeType(RECORD_FOLDER_TYPE.toString())
                                                          .relativePath(RELATIVE_PATH)
                                                          .build();
        String folderId = filePlanComponentAPI.createFilePlanComponent(recordFolder, FILE_PLAN_ALIAS.toString()).getId();
        //create electronic record
        //String recordWithContentId =
        FilePlanComponent fpc =  filePlanComponentAPI.createElectronicRecord(electronicRecord, createTempFile(ELECTRONIC_RECORD_NAME, ELECTRONIC_RECORD_NAME), folderId);//.getId();
        //Get the record created
        FilePlanComponent recordWithContent=filePlanComponentAPI.withParams("include = "+ IS_COMPLETED).getFilePlanComponent(fpc.getId());
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
        assertEquals(recordWithContent.getProperties().getDescription(),ELECTRONIC_RECORD_NAME);
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);

        //create non-electronic record
        String nonElectronicRecordId = filePlanComponentAPI.createFilePlanComponent(nonelectronicRecord, folderId).getId();
        //Get the record created
        FilePlanComponent nonElectronicRecord = filePlanComponentAPI.withParams("include = " + IS_COMPLETED).getFilePlanComponent(nonElectronicRecordId);

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
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(OK);
    }

    /**
     * Given an electronic record
     * When I try to read the content
     * Then I successfully receive the content of the record
     */
    @Test
    public void readRecordContent() throws Exception
    {
        String RECORD_ELECTRONIC= "Record " + getRandomAlphanumeric();
        String RELATIVE_PATH="/"+CATEGORY_NAME+ getRandomAlphanumeric()+"/folder";
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        //create the containers from the relativePath
        FilePlanComponent recordFolder = FilePlanComponent.builder()
                                                    .name(FOLDER_NAME)
                                                    .nodeType(RECORD_FOLDER_TYPE.toString())
                                                    .relativePath(RELATIVE_PATH)
                                                    .build();
        String folderId=filePlanComponentAPI.createFilePlanComponent(recordFolder,FILE_PLAN_ALIAS.toString()).getId();
        //
        FilePlanComponent record = FilePlanComponent.builder()
                                                    .name(RECORD_ELECTRONIC)
                                                    .nodeType(CONTENT_TYPE.toString())
                                                    .build();
        String recordId =filePlanComponentAPI.createElectronicRecord(record, createTempFile(RECORD_ELECTRONIC, RECORD_ELECTRONIC), folderId).getId();

        recordsAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        assertEquals(recordsAPI.getRecordContentText(recordId),RECORD_ELECTRONIC);
        // Check status code
        recordsAPI.usingRestWrapper().assertStatusCodeIs(OK);

        FilePlanComponent recordNoContent = FilePlanComponent.builder()
                                                    .name(RECORD_ELECTRONIC)
                                                    .nodeType(CONTENT_TYPE.toString())
                                                    .build();
        String recordNoContentId=filePlanComponentAPI.createFilePlanComponent(recordNoContent,folderId).getId();
        assertTrue(recordsAPI.getRecordContentText(recordNoContentId).toString().isEmpty());
        recordsAPI.usingRestWrapper().assertStatusCodeIs(OK);
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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        String nonElectronicRecord=filePlanComponentAPI.createFilePlanComponent(record,FILE_PLAN_ALIAS.toString()).getId();

        recordsAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        assertTrue(recordsAPI.getRecordContentText(nonElectronicRecord).toString().isEmpty());
        recordsAPI.usingRestWrapper().assertStatusCodeIs(OK);

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
        recordsAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        recordsAPI.getRecordContentText(container).toString();
        recordsAPI.usingRestWrapper().assertStatusCodeIs(BAD_REQUEST);
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
            { createCategoryFolderInFilePlan(dataUser.getAdminUser(), FILE_PLAN_ALIAS.toString()).getId() },
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
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

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
            FilePlanComponent child = filePlanComponentAPI.createElectronicRecord(record, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i ), containerId);
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
            FilePlanComponent child=filePlanComponentAPI.createFilePlanComponent(nonelectronicRecord, containerId);
            children.add(child);
        }
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // List children from API
        FilePlanComponentsCollection apiChildren =
            (FilePlanComponentsCollection) filePlanComponentAPI.listChildComponents(containerId).assertThat().entriesListIsNotEmpty();

        // Check status code
        restWrapper.assertStatusCodeIs(OK);


        // Check listed children against created list
        apiChildren.getEntries().forEach(c ->
            {
                FilePlanComponent filePlanComponent = c.getFilePlanComponent();
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
                    assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

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

}
