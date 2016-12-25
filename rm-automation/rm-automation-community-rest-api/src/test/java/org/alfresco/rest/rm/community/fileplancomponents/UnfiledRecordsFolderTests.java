/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.HOLD_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.HOLD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentModel;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unfiled Records folder CRUD API tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UnfiledRecordsFolderTests extends BaseRestTest
{
    @Autowired
    private DataUser dataUser;

    /** invalid root level types, at unfiled records root level these shouldn't be possible to create */

@DataProvider(name = "invalidRootTypes")
    public Object[][] createData1() {
     return new Object[][] {
       { FILE_PLAN_TYPE },
       { RECORD_CATEGORY_TYPE },
       { RECORD_FOLDER_TYPE },
       { HOLD_TYPE },
       { HOLD_CONTAINER_TYPE },
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
        String folderName = "Folder " + getRandomAlphanumeric();
        String folderTitle = folderName + " Title";
        String folderDescription = folderName + " Description";

        // Build unfiled records folder properties
        FilePlanComponentModel unfiledFolder = FilePlanComponentModel.builder()
                .name(folderName)
                .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                .properties(FilePlanComponentProperties.builder()
                                .title(folderTitle)
                                .description(folderDescription)
                                .build())
                .build();

        FilePlanComponentModel filePlanComponent = getFilePlanComponentsAPI().createFilePlanComponent(unfiledFolder, UNFILED_RECORDS_CONTAINER_ALIAS);

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned file plan component
        assertFalse(filePlanComponent.getIsCategory());
        assertFalse(filePlanComponent.getIsFile());
        assertFalse(filePlanComponent.getIsRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(filePlanComponent.getName(), folderName);
        assertEquals(filePlanComponent.getNodeType(), UNFILED_RECORD_FOLDER_TYPE);

        assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties filePlanComponentProperties = filePlanComponent.getProperties();
        assertEquals(filePlanComponentProperties.getTitle(), folderTitle);
        assertEquals(filePlanComponentProperties.getDescription(), folderDescription);
    }

    /**
     * Negative test to verify only unfiled record folders can be created at root level
     */
    @Test
    (
        dataProvider = "invalidRootTypes",
        description = "Only unfiled records folders can be created at unfiled records root level"
    )
    public void onlyRecordFoldersCanBeCreatedAtUnfiledRecordsRoot(String filePlanComponentType)
    {
        String folderName = "Folder " + getRandomAlphanumeric();
        String folderTitle = folderName + " Title";
        String folderDescription = folderName + " Description";

        logger.info("creating " + filePlanComponentType);

        // Build unfiled records folder properties
        FilePlanComponentModel unfiledFolder = FilePlanComponentModel.builder()
            .name(folderName)
            .nodeType(filePlanComponentType)
            .properties(FilePlanComponentProperties.builder()
                            .title(folderTitle)
                            .description(folderDescription)
                            .build())
            .build();

        try
        {
            getFilePlanComponentsAPI().createFilePlanComponent(unfiledFolder, UNFILED_RECORDS_CONTAINER_ALIAS);
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
        String parentFolderName = "Parent Folder " + getRandomAlphanumeric();
        String childFolderName = "Child Folder " + getRandomAlphanumeric();
        String childFolderTitle = childFolderName + " Title";
        String childFolderDescription = childFolderName + " Description";

        // No need for fine control, create it using utility function
        FilePlanComponentModel parentFolder = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS, parentFolderName);
        assertEquals(parentFolderName, parentFolder.getName());

        // Build the unfiled records folder properties
        FilePlanComponentModel unfiledFolder = FilePlanComponentModel.builder()
                .name(childFolderName)
                .nodeType(UNFILED_RECORD_FOLDER_TYPE)
                .properties(FilePlanComponentProperties.builder()
                        .title(childFolderTitle)
                        .description(childFolderDescription)
                        .build())
                .build();

        // Create it as a child of parentFolder
        FilePlanComponentModel childFolder = getFilePlanComponentsAPI().createFilePlanComponent(unfiledFolder, parentFolder.getId());

        // Verify the status code
        assertStatusCode(CREATED);

        // Verify the returned file plan component
        assertFalse(childFolder.getIsCategory());
        assertFalse(childFolder.getIsFile());
        assertFalse(childFolder.getIsRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(childFolder.getName(), childFolderName);
        assertEquals(childFolder.getNodeType(), UNFILED_RECORD_FOLDER_TYPE);
        assertEquals(childFolder.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties childProperties = childFolder.getProperties();
        assertEquals(childProperties.getTitle(), childFolderTitle);
        assertEquals(childProperties.getDescription(), childFolderDescription);

        // Does this child point to its parent?
        assertEquals(childFolder.getParentId(), parentFolder.getId());

        // Does child's parent point to it?
        // Perform another call as our parentFolder had been executed before childFolder existed
        FilePlanComponentsCollection parentsChildren = getFilePlanComponentsAPI().listChildComponents(parentFolder.getId());
        assertStatusCode(OK);
        List<String> childIds = parentsChildren.getEntries()
            .stream()
            .map(c -> c.getFilePlanComponentModel().getId())
            .collect(Collectors.toList());

        // Child folder is listed in parent
        assertTrue(childIds.contains(childFolder.getId()));

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
        String folderName = "Folder To Modify" + getRandomAlphanumeric();

        // No need for fine control, create it using utility function
        FilePlanComponentModel folderToModify = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS, folderName);
        assertEquals(folderName, folderToModify.getName());

        // Build the properties which will be updated
        FilePlanComponentModel folderToUpdate = FilePlanComponentModel.builder()
            .name(modified + folderToModify.getName())
            .properties(FilePlanComponentProperties.builder().
                    title(modified + folderToModify.getProperties().getTitle()).
                    description(modified + folderToModify.getProperties().getDescription())
                    .build())
            .build();

        // Update the unfiled records folder
        getFilePlanComponentsAPI().updateFilePlanComponent(folderToUpdate, folderToModify.getId());
        // Verify the status code
        assertStatusCode(OK);

        // This is to ensure the change was actually applied, rather than simply trusting the object returned by PUT
        FilePlanComponentModel renamedFolder = getFilePlanComponentsAPI().getFilePlanComponent(folderToModify.getId());

        // Verify the returned file plan component
        assertEquals(modified + folderToModify.getName(), renamedFolder.getName());
        assertEquals(modified + folderToModify.getProperties().getTitle(), renamedFolder.getProperties().getTitle());
        assertEquals(modified + folderToModify.getProperties().getDescription(), renamedFolder.getProperties().getDescription());
    }

    /**
     * Given an unfiled record folder
     * When I delete the unfiled record folder via the ReST API
     * Then the unfiled record folder is deleted
     *
     * @throws Exception for failed actions
     */
    @Test(description = "Delete unfiled record folder")
    public void deleteUnfiledRecordsFolder() throws Exception
    {
        String folderName = "Folder To Delete" + getRandomAlphanumeric();

        // Create folderToDelete
        FilePlanComponentModel folderToDelete = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS, folderName);
        assertEquals(folderName, folderToDelete.getName());

        // Delete folderToDelete
        getFilePlanComponentsAPI().deleteFilePlanComponent(folderToDelete.getId());

        // Verify the status code
        assertStatusCode(NO_CONTENT);

        // Deleted component should no longer be retrievable
        getFilePlanComponentsAPI().getFilePlanComponent(folderToDelete.getId());
        assertStatusCode(NOT_FOUND);
    }
}
