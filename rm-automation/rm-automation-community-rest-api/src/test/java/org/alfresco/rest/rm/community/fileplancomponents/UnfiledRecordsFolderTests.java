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

import static java.time.LocalDateTime.now;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.PATH;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createUnfiledContainerChildModel;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildCollection;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledRecordFolder;
import org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
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
    /** invalid root level types, at unfiled records root level these shouldn't be possible to create */

    @DataProvider(name = "invalidRootTypes")
    public String[][] createData()
    {
        return new String[][]
        {
            { FILE_PLAN_TYPE },
            { RECORD_CATEGORY_TYPE },
            { RECORD_FOLDER_TYPE },
            { TRANSFER_CONTAINER_TYPE },
            { UNFILED_CONTAINER_TYPE }
        };
    }

    /**
     * Given the unfiled record container root
     * When I create an unfiled record folder via the ReST API
     * Then a root unfiled record folder is created
     *
     * @throws Exception if folder couldn't be created
     */
    @Test(description = "Create root unfiled records folder")
    public void createRootUnfiledRecordsFolder() throws Exception
    {
        String unfiledRecordFolderName = "UnfiledRecordFolder-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecordFolderChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, unfiledRecordFolderName, UNFILED_RECORD_FOLDER_TYPE);

        assertNotNull(unfiledRecordFolderChild.getId());

        // Verify the returned file plan component
        assertFalse(unfiledRecordFolderChild.getIsRecord());
        assertTrue(unfiledRecordFolderChild.getIsUnfiledRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(unfiledRecordFolderChild.getName(), unfiledRecordFolderName);
        assertEquals(unfiledRecordFolderChild.getNodeType(), UNFILED_RECORD_FOLDER_TYPE);

        assertEquals(unfiledRecordFolderChild.getCreatedByUser().getId(), getAdminUser().getUsername());

        UnfiledRecordFolder unfiledRecordFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledRecordFolderChild.getId());
        // Verify the returned file plan component properties
        UnfiledContainerChildProperties unfiledRecordFolderChildProperties = unfiledRecordFolder.getProperties();
        assertEquals(unfiledRecordFolderChildProperties.getTitle(), FilePlanComponentsUtil.TITLE_PREFIX + unfiledRecordFolderName);
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
        description = "Create a folder based on the relativePath. " +
            "Containers in the relativePath that do not exist are created before the node is created"
    )
    public void createUnfiledRecordFolderWithRelativePath() throws Exception
    {
        String unfiledRecordFolderName1 = "UnfiledRecordFolder-" + getRandomAlphanumeric();
        UnfiledContainerChild unfiledRecordFolderChild1 = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, unfiledRecordFolderName1, UNFILED_RECORD_FOLDER_TYPE);

        String unfiledRecordFolderName2 = "UnfiledRecordFolder-" + getRandomAlphanumeric();

        // relativePath specify the container structure to create relative to the record folder to be created
        String relativePath = now().getYear() + "/" + now().getMonth() + "/" + now().getDayOfMonth();

        // The record folder to be created
        UnfiledContainerChild unfiledFolderModel =UnfiledContainerChild.builder()
                .name(unfiledRecordFolderName2)
                .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                .relativePath(relativePath)
                .build();
                
        UnfiledContainerChild unfiledRecordFolderChild = getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(unfiledFolderModel, unfiledRecordFolderChild1.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned details for the record folder
        assertFalse(unfiledRecordFolderChild.getIsRecord());
        assertTrue(unfiledRecordFolderChild.getIsUnfiledRecordFolder());
        assertTrue(UNFILED_RECORD_FOLDER_TYPE.equals(unfiledRecordFolderChild.getNodeType()));

        // Check the path return contains the relativePath
        assertTrue(unfiledRecordFolderChild.getPath().getName().contains(relativePath));

        // Check the parent is a folder, not a record
        assertTrue(getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledRecordFolderChild.getParentId()).getNodeType().equals(UNFILED_RECORD_FOLDER_TYPE));

        // Check the created folder from the server
        UnfiledRecordFolder recordFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledRecordFolderChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Check the path return contains the relativePath
        assertTrue(recordFolder.getPath().getName().contains(relativePath));

        // New relative path only a part of containers need to be created before the record folder
        String newRelativePath = now().getYear() + "/" + now().getMonth() + "/" + (now().getDayOfMonth() + 1);

        String unfiledRecordFolderName3 = "UnfiledRecordFolder-" + getRandomAlphanumeric();
        // The record folder to be created
        UnfiledContainerChild newUnfiledFolderModel =UnfiledContainerChild.builder()
                .name(unfiledRecordFolderName3)
                .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                .relativePath(newRelativePath)
                .build();
                
        UnfiledContainerChild newUnfiledRecordFolderChild = getRestAPIFactory().getUnfiledRecordFoldersAPI().createUnfiledRecordFolderChild(newUnfiledFolderModel, unfiledRecordFolderChild1.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(CREATED);
        // Check the API response code
        assertStatusCode(CREATED);

        // Verify the returned properties for the unfiled record folder
        assertTrue(newUnfiledRecordFolderChild.getIsUnfiledRecordFolder());
        assertFalse(newUnfiledRecordFolderChild.getIsRecord());

        // Check the path return contains the newRelativePath
        assertTrue(newUnfiledRecordFolderChild.getPath().getName().contains(newRelativePath));

        // Check the parent is a folder, not a record
        assertFalse(getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(newUnfiledRecordFolderChild.getParentId()).equals(UNFILED_RECORD_FOLDER_TYPE));

        // Check the folder created on the server
        UnfiledRecordFolder newRecordFolder = getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(newUnfiledRecordFolderChild.getId(), "include=" + PATH);

        // Check the API response code
        assertStatusCode(OK);

        // Check the path return contains the newRelativePath
        assertTrue(newRecordFolder.getPath().getName().contains(newRelativePath));
    }


    /**
     * Negative test to check that invalid types cannot be created at unfiled container root level
     * Only unfiled record folders and records can be created into unfiled container
     */
    @Test
    (
        dataProvider = "invalidRootTypes",
        description = "Only unfiled records folders  can be created at unfiled records root level"
    )
    public void onlyRecordFoldersCanBeCreatedAtUnfiledRecordsRoot(String filePlanComponentType)
    {
        String unfiledRecordFolderName = "UnfiledRecordFolder-" + getRandomAlphanumeric();

        logger.info("creating " + filePlanComponentType);

        // Build unfiled records folder properties
        UnfiledContainerChild unfiledFolderModel = createUnfiledContainerChildModel(unfiledRecordFolderName, filePlanComponentType);

        try
        {
            getRestAPIFactory().getUnfiledContainersAPI().createUnfiledContainerChild(unfiledFolderModel, UNFILED_RECORDS_CONTAINER_ALIAS);
        }
        catch (Exception error)
        {
        }

        // Verify the status code
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Given an unfiled record folder
     * When I create an unfiled record folder via the ReST API
     * Then an unfiled record folder is created within the unfiled record folder
     *
     * @throws Exception for failed actions
     */
    @Test(description = "Child unfiled records folder can be created in a parent unfiled records folder")
    public void childUnfiledRecordsFolderCanBeCreated() throws Exception
    {
        String unfiledParentFolderName = "UnfiledParentFolder" + getRandomAlphanumeric();
        String unfiledChildFolderName = "UnfiledChildFolder " + getRandomAlphanumeric();
        String rootUnfiledFolderName = "RootUnfiledFolder" + getRandomAlphanumeric();

        //create root unfiled record folder
        UnfiledContainerChild createUnfiledContainerChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, rootUnfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        // No need for fine control, create it using utility function
        UnfiledContainerChild unfiledParentFolder = createUnfiledRecordsFolderChild(createUnfiledContainerChild.getId(),
                unfiledParentFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledParentFolderName, unfiledParentFolder.getName());

        // Build the unfiled records folder properties
        UnfiledContainerChild unfiledChildFolderModel = UnfiledContainerChild.builder().name(unfiledChildFolderName)
                .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                .properties(UnfiledContainerChildProperties.builder().title(FilePlanComponentsUtil.TITLE_PREFIX + unfiledChildFolderName)
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
     *
     * @throws Exception for failed actions
     */
    @Test(description = "Unfiled record folder")
    public void editUnfiledRecordsFolder() throws Exception
    {
        String modified = "Modified ";
        String unfiledFolderName = "UnfiledFolderToModify" + getRandomAlphanumeric();
        String rootUnfiledFolderName = "RootUnfiledFolder" + getRandomAlphanumeric();

        //create root unfiledRecordFolder
        UnfiledContainerChild createUnfiledContainerChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS,rootUnfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        // No need for fine control, create it using utility function
        UnfiledContainerChild unfiledFolderToModify =  createUnfiledRecordsFolderChild(createUnfiledContainerChild.getId(),
                unfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledFolderName, unfiledFolderToModify.getName());

        // Build the properties which will be updated
        UnfiledRecordFolder unfiledChildFolderModel = UnfiledRecordFolder.builder().name(modified + unfiledFolderName)
                .properties(UnfiledContainerChildProperties.builder().title(modified + unfiledFolderToModify.getProperties().getTitle())
                        .description(modified + unfiledFolderToModify.getProperties().getDescription()).build())
                .build();

        // Update the unfiled records folder
        getRestAPIFactory().getUnfiledRecordFoldersAPI().updateUnfiledRecordFolder(unfiledChildFolderModel, unfiledFolderToModify.getId());
        // Verify the status code
        assertStatusCode(OK);

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
     *
     * @throws Exception for failed actions
     */
    @Test(description = "Delete unfiled record folder")
    public void deleteUnfiledRecordsFolder() throws Exception
    {
        String unfiledFolderName = "UnfiledFolderToDelete" + getRandomAlphanumeric();
        String nonElectronicRecordName = "NonElectronicRecord" + getRandomAlphanumeric();
        String electronicRecordName = "ElectronicRecord" + getRandomAlphanumeric();
        String rootUnfiledFolderName = "RootUnfiledFolder" + getRandomAlphanumeric();

        //create root unfiled record folder
        UnfiledContainerChild createUnfiledContainerChild = createUnfiledContainerChild(UNFILED_RECORDS_CONTAINER_ALIAS, rootUnfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        // Create unfiledFolderToDelete
        UnfiledContainerChild unfiledFolderToDelete =  createUnfiledRecordsFolderChild(createUnfiledContainerChild.getId(),
                unfiledFolderName, UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(unfiledFolderName, unfiledFolderToDelete.getName());

        // Create a non electronic record under unfiledFolderToDelete
        UnfiledContainerChild nonElectronicRecord =  createUnfiledRecordsFolderChild(unfiledFolderToDelete.getId(),
                nonElectronicRecordName, NON_ELECTRONIC_RECORD_TYPE);
        assertTrue(nonElectronicRecord.getParentId().equals(unfiledFolderToDelete.getId()));

        // Create an electronic record under unfiledFolderToDelete
        UnfiledContainerChild electronicRecord =  createUnfiledRecordsFolderChild(unfiledFolderToDelete.getId(),
                electronicRecordName, CONTENT_TYPE);
        assertTrue(electronicRecord.getParentId().equals(unfiledFolderToDelete.getId()));

        // Delete folderToDelete
        getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(unfiledFolderToDelete.getId());

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        getRestAPIFactory().getUnfiledRecordFoldersAPI().getUnfiledRecordFolder(unfiledFolderToDelete.getId());
        assertStatusCode(NOT_FOUND);
    }
 
    @AfterMethod
    @AfterClass (alwaysRun = true)
    public void tearDown() throws Exception
    {
        UnfiledContainerChildCollection listedChildren = getRestAPIFactory().getUnfiledContainersAPI()
                .getUnfiledContainerChildren(UNFILED_RECORDS_CONTAINER_ALIAS);

        listedChildren.getEntries().forEach(UnfiledContainerChildEntry -> 
        {
            if (UnfiledContainerChildEntry.getEntry().getIsRecord())
            {
                getRestAPIFactory().getRecordsAPI().deleteRecord(UnfiledContainerChildEntry.getEntry().getId());
            }
            else
            {
                getRestAPIFactory().getUnfiledRecordFoldersAPI().deleteUnfiledRecordFolder(UnfiledContainerChildEntry.getEntry().getId());
            }
        });
    }
}
