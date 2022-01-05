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
package org.alfresco.rest.rm.community.unfiledrecordfolders;

import static java.time.LocalDateTime.now;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createUnfiledContainerChildModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.base.DataProviderClass;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledRecordFolder;
import org.alfresco.rest.rm.community.requests.gscore.api.UnfiledRecordFolderAPI;
import org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unfiled Records folder CRUD API tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UnfiledRecordsFolderTests extends BaseRMRestTest
{
    public static final String ELECTRONIC_RECORD_NAME = getRandomName("Record electronic");
    public static final String NONELECTRONIC_RECORD_NAME = getRandomName("Record nonelectronic");

    private RecordCategory rootCategory;
    private UnfiledContainerChild unfiledRecordFolder, rootUnfiledRecordFolder, unfiledRecord;

    @BeforeClass(alwaysRun = true)
    public void preconditionUnfiledRecordsFolderTests()
    {
        rootCategory = createRootCategory(getRandomName("CATEGORY NAME"));
        rootUnfiledRecordFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                getRandomName("RootUnfiledRecFolder"), UNFILED_RECORD_FOLDER_TYPE);
        unfiledRecord = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,
                getRandomName("Unfiled Record"), CONTENT_TYPE);
    }
    /**
     * valid root level types, at unfiled record folder  level these  possible to create
     */
    @DataProvider (name = "validChildren")
    public Object[][] childrenForUnfiledRecord()
    {
        return new String[][]
                {
                        { UNFILED_RECORD_FOLDER_TYPE },
                        { CONTENT_TYPE },
                        { NON_ELECTRONIC_RECORD_TYPE }
                };
    }

    /**
     * Invalid  containers that cannot be updated/deleted with record folder endpoint
     */
    @DataProvider (name = "invalidNodesForDelete")
    public Object[][] getInvalidNodes()
    {
        return new String[][] {
                { getRestAPIFactory().getFilePlansAPI().getFilePlan(FILE_PLAN_ALIAS).getId() },
                { getRestAPIFactory().getUnfiledContainersAPI().getUnfiledContainer(UNFILED_RECORDS_CONTAINER_ALIAS).getId() },
                { getRestAPIFactory().getTransferContainerAPI().getTransferContainer(TRANSFERS_ALIAS).getId() },
                // an arbitrary record category
                { rootCategory.getId() },
                // an arbitrary unfiled records folder
                {createRecordFolder(rootCategory.getId(), getRandomName("recFolder")).getId()},
                {unfiledRecord.getId() }
        };
    }

    /**
     * <pre>
     * Given that I want to create an unfiled record folder
     * When I use the API with the relativePath
     * Then the folders specified in the relativePath that don't exist are created
     * </pre>
     */
    @Test
    (
        description = "Create a child into unfiled record folder based on the relativePath. " +
            "Containers in the relativePath that do not exist are created before the node is created",
        dataProvider = "validChildren"
    )
    public void createUnfiledRecordFolderWithRelativePath(String  nodeType)
    {
        // relativePath specify the container structure to create relative to the record folder to be created
        String relativePath = now().getYear() + "/" + now().getMonth() + "/" + now().getDayOfMonth();

        // The record folder to be created
        UnfiledContainerChild unfiledChildModel = UnfiledContainerChild.builder()
                                                                       .name(getRandomName("UnfiledRecordFolder"))
                                                                       .nodeType(nodeType)
                                                                       .relativePath(relativePath)
                                                                       .build();

        UnfiledContainerChild unfiledRecordFolderChild = getRestAPIFactory().getUnfiledRecordFoldersAPI()
                                                                            .createUnfiledRecordFolderChild(unfiledChildModel, rootUnfiledRecordFolder.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned node type
        assertEquals(unfiledRecordFolderChild.getNodeType(), nodeType);

        // Check the path return contains the relativePath
        assertTrue(unfiledRecordFolderChild.getPath().getName().contains(relativePath));

        // Check the parent is a folder, not a record
        assertEquals(getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledRecordFolderChild.getParentId()).getNodeType(),
                UNFILED_RECORD_FOLDER_TYPE);

        // New relative path only a part of containers need to be created before the record folder
        String newRelativePath = now().getYear() + "/" + now().getMonth() + "/" + (now().getDayOfMonth() + 1);

        // The record folder to be created
        UnfiledContainerChild newUnfiledFolderModel = UnfiledContainerChild.builder()
                                                                           .name(getRandomName("UnfiledRecordFolder"))
                                                                           .nodeType(nodeType)
                                                                           .relativePath(newRelativePath)
                                                                           .build();

        UnfiledContainerChild newUnfiledRecordFolderChild = getRestAPIFactory().getUnfiledRecordFoldersAPI()
                                                                               .createUnfiledRecordFolderChild(newUnfiledFolderModel, rootUnfiledRecordFolder.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Check the path return contains the newRelativePath
        assertTrue(newUnfiledRecordFolderChild.getPath().getName().contains(newRelativePath));

        // Check the parent is a folder, not a record
        assertFalse(getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(newUnfiledRecordFolderChild.getParentId()).equals(UNFILED_RECORD_FOLDER_TYPE));
        // Verify the returned node type
        assertEquals(newUnfiledRecordFolderChild.getNodeType(), nodeType);
    }

    /**
     * Negative test to check that invalid types cannot be created at unfiled container root level
     * Only unfiled record folders and records can be created into unfiled container
     */
    @Test
    (
        dataProvider = "invalidRootTypes",
        dataProviderClass = DataProviderClass.class,
        description = "Only unfiled records folders and records  can be created as children for unfiled container root"
    )
    public void createInvalidUnfiledChildren(String filePlanComponentType)
    {
        // Build unfiled records folder properties
        UnfiledContainerChild unfiledFolderModel = createUnfiledContainerChildModel(getRandomName("UnfiledRecFolder"), filePlanComponentType);

        getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(unfiledFolderModel, rootUnfiledRecordFolder.getId());

        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given an unfiled record folder
     * When I create an unfiled record folder via the ReST API
     * Then an unfiled record folder is created within the unfiled record folder
     */
    @Test(description = "Child unfiled records folder can be created in a parent unfiled records folder")
    public void childUnfiledRecordsFolderCanBeCreated()
    {
        String unfiledParentFolderName = "UnfiledParentFolder" + getRandomAlphanumeric();
        String unfiledChildFolderName = "UnfiledChildFolder " + getRandomAlphanumeric();

        // No need for fine control, create it using utility function
        UnfiledContainerChild unfiledParentFolder = createUnfiledRecordsFolderChild(rootUnfiledRecordFolder.getId(),
                unfiledParentFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledParentFolderName, unfiledParentFolder.getName());

        // Build the unfiled records folder properties
        UnfiledContainerChild unfiledChildFolderModel =
                    UnfiledContainerChild.builder()
                                            .name(unfiledChildFolderName)
                                            .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                                            .properties(UnfiledContainerChildProperties.builder()
                                                                .title(FilePlanComponentsUtil.TITLE_PREFIX + unfiledChildFolderName)
                                                                .description(FilePlanComponentsUtil.DESCRIPTION_PREFIX + unfiledChildFolderName).build())
                                        .build();

        // Create it as a child of parentFolder
        UnfiledContainerChild unfiledChildFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI()
                .createUnfiledRecordFolderChild(unfiledChildFolderModel, unfiledParentFolder.getId());

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned unfiled child folder
        assertTrue(unfiledChildFolder.getIsUnfiledRecordFolder());
        assertFalse(unfiledChildFolder.getIsRecord());

        assertEquals(unfiledChildFolder.getName(), unfiledChildFolderName);
        assertEquals(unfiledChildFolder.getNodeType(), UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledChildFolder.getCreatedByUser().getId(), getAdminUser().getUsername());

        // Verify the returned file plan component properties
        UnfiledRecordFolder unfiledChildRecordFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI()
                .getUnfiledRecordFolder(unfiledChildFolder.getId());
        // Verify the returned file plan component properties
        UnfiledContainerChildProperties unfiledChildFolderProperties = unfiledChildRecordFolder.getProperties();
        assertEquals(unfiledChildFolderProperties.getTitle(), FilePlanComponentsUtil.TITLE_PREFIX + unfiledChildFolderName);
        assertEquals(unfiledChildFolderProperties.getDescription(), FilePlanComponentsUtil.DESCRIPTION_PREFIX + unfiledChildFolderName);

        // Does this child point to its parent?
        assertEquals(unfiledChildFolder.getParentId(), unfiledParentFolder.getId());

        // Does child's parent point to it?
        // Perform another call as our parentFolder had been executed before childFolder existed
        UnfiledContainerChildCollection parentsChildren = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolderChildren(unfiledParentFolder.getId());
        assertStatusCode(OK);
        List<String> childIds = parentsChildren.getEntries()
            .stream()
            .map(c -> c.getEntry().getId())
            .collect(Collectors.toList());

        // Child folder is listed in parent
        assertTrue(childIds.contains(unfiledChildFolder.getId()));

        // There should be only one child
        assertEquals(1, childIds.size());
    }

    /**
     * Given an unfiled record folder
     * When I modify the unfiled record folder details via the ReST API
     * Then the details of the unfiled record folder are modified
     */
    @Test(description = "Unfiled record folder")
    public void editUnfiledRecordsFolder()
    {
        String modified = "Modified ";
        String unfiledFolderName = "UnfiledFolderToModify" + getRandomAlphanumeric();

        // No need for fine control, create it using utility function
        UnfiledContainerChild unfiledFolderToModify =  createUnfiledRecordsFolderChild(rootUnfiledRecordFolder.getId(),
                unfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledFolderName, unfiledFolderToModify.getName());

        // Build the properties which will be updated
        UnfiledRecordFolder unfiledChildFolderModel =
                    UnfiledRecordFolder.builder()
                                         .name(modified + unfiledFolderName)
                                         .properties
                                             (UnfiledContainerChildProperties.builder()
                                                                                .title(modified + unfiledFolderToModify.getProperties().getTitle())
                                                                                .description(modified + unfiledFolderToModify.getProperties().getDescription())
                                                                                .build()
                                             )
                                       .build();




        // Update the unfiled records folder
        UnfiledRecordFolder updatedRecordFolder=getRestAPIFactory().getUnfiledRecordFoldersAPI().updateUnfiledRecordFolder(unfiledChildFolderModel, unfiledFolderToModify.getId());

        // Verify the status code
        assertStatusCode(OK);
        // Verify the returned file plan component
        assertEquals(unfiledChildFolderModel.getName(),
                updatedRecordFolder.getName());
        assertEquals(unfiledChildFolderModel.getProperties().getTitle(),
                updatedRecordFolder.getProperties().getTitle());
        assertEquals(unfiledChildFolderModel.getProperties().getDescription(),
                updatedRecordFolder.getProperties().getDescription());
        // This is to ensure the change was actually applied, rather than simply trusting the object returned by PUT
        UnfiledRecordFolder renamedUnfiledFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledFolderToModify.getId());

        // Verify the returned file plan component
        assertEquals(modified + unfiledFolderToModify.getName(), renamedUnfiledFolder.getName());
        assertEquals(modified + unfiledFolderToModify.getProperties().getTitle(), renamedUnfiledFolder.getProperties().getTitle());
        assertEquals(modified + unfiledFolderToModify.getProperties().getDescription(), renamedUnfiledFolder.getProperties().getDescription());
    }

    /**
     * Given an unfiled record folder and some records inside
     * When I delete the unfiled record folder via the ReST API
     * Then the unfiled record folder is deleted and its content too
     */
    @Test(description = "Delete unfiled record folder")
    public void deleteUnfiledRecordsFolder()
    {
        String unfiledFolderName = "UnfiledFolderToDelete" + getRandomAlphanumeric();
        String nonElectronicRecordName = "NonElectronicRecord" + getRandomAlphanumeric();
        String electronicRecordName = "ElectronicRecord" + getRandomAlphanumeric();

        // Create unfiledFolderToDelete
        UnfiledContainerChild unfiledFolderToDelete =  createUnfiledRecordsFolderChild(rootUnfiledRecordFolder.getId(),
                unfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledFolderName, unfiledFolderToDelete.getName());

        // Create a non electronic record under unfiledFolderToDelete
        UnfiledContainerChild nonElectronicRecord =  createUnfiledRecordsFolderChild(unfiledFolderToDelete.getId(),
                nonElectronicRecordName, NON_ELECTRONIC_RECORD_TYPE);
        assertEquals(nonElectronicRecord.getParentId(), unfiledFolderToDelete.getId());

        // Create an electronic record under unfiledFolderToDelete
        UnfiledContainerChild electronicRecord =  createUnfiledRecordsFolderChild(unfiledFolderToDelete.getId(),
                electronicRecordName, CONTENT_TYPE);
        assertEquals(electronicRecord.getParentId(), unfiledFolderToDelete.getId());

        // Delete folderToDelete
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledFolderToDelete.getId());

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledFolderToDelete.getId());
        assertStatusCode(NOT_FOUND);
    }

    /**
     * <pre>
     * Given other nodes type than unfiled record folders exists
     * When I use the API from unfiled record-folders to delete the nodes
     * Then the request fails
     * </pre>
     */
    @Test
    (
        description = "Delete invalid nodes type with the DELETE unfiled record folders request",
        dataProvider = "invalidNodesForDelete"
    )
    public void deleteInvalidNodesUnfiled(String nodeId)
    {
        // Delete the nodes with record-folders end-point
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(nodeId);

        // Check the response status code
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * Given a container that is a unfiled record folder
     * When I try to record the containers records
     * Then I receive a list of all the records contained within the unfiled record folder
     */
    @Test
    public void readRecordsFromUnfiledRecordFolder()
    {
        unfiledRecordFolder = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, getRandomName("UnfiledRecordFolder"), UNFILED_RECORD_FOLDER_TYPE);
        String containerId = unfiledRecordFolder.getId();

        //we have unfiled record folder
        UnfiledRecordFolderAPI unfiledRecordFoldersAPI = getRestAPIFactory().getUnfiledRecordFoldersAPI();

        ArrayList<UnfiledContainerChild> children = new ArrayList<>();
        for (int i = 0; i < 5; i++)
        {
            // Create Electronic Records
            UnfiledContainerChild record = UnfiledContainerChild.builder()
                                                                .name(ELECTRONIC_RECORD_NAME + i)
                                                                .nodeType(CONTENT_TYPE)
                                                                .build();
            UnfiledContainerChild child = unfiledRecordFoldersAPI.uploadRecord(record, containerId, createTempFile(ELECTRONIC_RECORD_NAME + i, ELECTRONIC_RECORD_NAME + i));
            children.add(child);

            //Create NonElectronicRecords
            UnfiledContainerChild nonelectronicRecord = UnfiledContainerChild.builder()
                                                                             .properties(UnfiledContainerChildProperties.builder()
                                                                                                                        .description("Description")
                                                                                                                        .title("Title")
                                                                                                                        .build())
                                                                             .name(NONELECTRONIC_RECORD_NAME + i)
                                                                             .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                                                             .build();
            child = unfiledRecordFoldersAPI.createUnfiledRecordFolderChild(nonelectronicRecord, containerId);
            children.add(child);
        }

        // List children from API
        UnfiledContainerChildCollection apiChildren = (UnfiledContainerChildCollection) unfiledRecordFoldersAPI.getUnfiledRecordFolderChildren(containerId,"include=properties").assertThat().entriesListIsNotEmpty();

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
                                                                 .orElseThrow();

                // Created by
                assertEquals(record.getCreatedByUser().getId(), getAdminUser().getUsername());

                // Is parent Id set correctly
                assertEquals(record.getParentId(), containerId);
                assertTrue(record.getIsRecord());

                // Boolean properties related to node type
                assertFalse(record.getIsUnfiledRecordFolder());

                //check the record name
                assertEquals(createdComponent.getName(), record.getName(),
                        "The record name " + record.getName() + " is not equal with the record name returned when creating the record " + createdComponent.getName());
                String identifier = " \\(" + record.getProperties().getIdentifier() + "\\)";
                String regex= "(" + NONELECTRONIC_RECORD_NAME + "|" + ELECTRONIC_RECORD_NAME + ")" + "[0-9]+" + identifier;
                assertTrue(record.getName().matches(regex),
                            "The record name:" + record.getName() + " doesn't match the expression " + regex);
                assertTrue(createdComponent.getName().contains(createdComponent.getProperties().getIdentifier()));
                assertEquals(createdComponent.getNodeType(), record.getNodeType());

            }
            catch (NoSuchElementException e)
            {
                fail("No child element for " + record.getId());
            }
        });
    }

    @AfterClass (alwaysRun = true)
    public void tearDown()
    {
        deleteRecordCategory(rootCategory.getId());
        getRestAPIFactory().getRecordsAPI().deleteRecord(unfiledRecord.getId());
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(rootUnfiledRecordFolder.getId());
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledRecordFolder.getId());
    }
}
