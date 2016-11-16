/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.rm.fileplancomponents;

import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.NAME;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.NODE_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_DESCRIPTION;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentFields.PROPERTIES_TITLE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.FILE_PLAN_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.HOLD_CONTAINER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.HOLD_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.TRANSFER_CONTAINER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.UNFILED_CONTAINER_TYPE;
import static org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType.UNFILED_RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.jglue.fluentjson.JsonBuilderFactory.buildObject;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.base.BaseRestTest;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentType;
import org.alfresco.rest.rm.model.fileplancomponents.FilePlanComponentsCollection;
import org.alfresco.rest.rm.requests.FilePlanComponentAPI;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * Unfiled Records folder CRUD API tests
 *
 * @author Kristijan Conkas
 * @since 1.0
 */
public class UnfiledRecordsFolderTests extends BaseRestTest
{
    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    private DataUser dataUser;

    /** invalid root level types, at root level these shouldn't be possible to create */
    private static final List<FilePlanComponentType> INVALID_ROOT_TYPES = Arrays.asList(
        FILE_PLAN_TYPE,
        RECORD_CATEGORY_TYPE,
        RECORD_FOLDER_TYPE,
        HOLD_TYPE,
        HOLD_CONTAINER_TYPE,
        TRANSFER_CONTAINER_TYPE,
        UNFILED_CONTAINER_TYPE);
    
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
        // Authenticate with admin user
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        
        String folderName = "Folder " + getRandomAlphanumeric();
        String folderTitle = folderName + " Title";
        String folderDescription = folderName + " Description";
        
        // Build the record category properties
        JsonObject unfiledFolderProperties = buildObject()
            .add(NAME, folderName)
            .add(NODE_TYPE, UNFILED_RECORD_FOLDER_TYPE.toString())
            .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, folderTitle)
                .add(PROPERTIES_DESCRIPTION, folderDescription)
                .end()
                .getJson();
        
        FilePlanComponent filePlanComponent = filePlanComponentAPI.createFilePlanComponent(unfiledFolderProperties, 
            UNFILED_RECORDS_CONTAINER_ALIAS.toString());

        // Verify the status code
        restWrapper.assertStatusCodeIs(CREATED);        
        
        // Verify the returned file plan component
        assertFalse(filePlanComponent.isIsCategory());
        assertFalse(filePlanComponent.isIsFile());
        assertFalse(filePlanComponent.isIsRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(filePlanComponent.getName(), folderName);
        assertEquals(filePlanComponent.getNodeType(), UNFILED_RECORD_FOLDER_TYPE.toString());
        assertFalse(filePlanComponent.isHasRetentionSchedule());

        assertEquals(filePlanComponent.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties filePlanComponentProperties = filePlanComponent.getProperties();
        assertEquals(filePlanComponentProperties.getTitle(), folderTitle);
        assertEquals(filePlanComponentProperties.getDescription(), folderDescription);
    }
    
    /**
     * Negative test to verify only unfiled record folders can be created at root level
     */
    @Test(description = "Only unfiled records folders can be created at unfiled records root level")
    public void onlyRecordFoldersCanBeCreatedAtUnfiledRecordsRoot()
    {
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String folderName = "Folder " + getRandomAlphanumeric();
        String folderTitle = folderName + " Title";
        String folderDescription = folderName + " Description";

        INVALID_ROOT_TYPES.stream()
            .peek(a -> logger.info("creating " + a.toString()))
            .forEach(t -> {
                JsonObject unfiledFolderProperties = buildObject()
                    .add(NAME, folderName)
                    .add(NODE_TYPE, t.toString())
                    .addObject(PROPERTIES)
                    .add(PROPERTIES_TITLE, folderTitle)
                    .add(PROPERTIES_DESCRIPTION, folderDescription)
                    .end()
                    .getJson();
                try
                {
                    filePlanComponentAPI.createFilePlanComponent(unfiledFolderProperties, 
                        UNFILED_RECORDS_CONTAINER_ALIAS.toString());
                }
                catch (Exception error)
                {
                }
    
                // Verify the status code
                restWrapper.assertStatusCodeIs(UNPROCESSABLE_ENTITY);
            });
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
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        String parentFolderName = "Parent Folder " + getRandomAlphanumeric();
        String childFolderName = "Child Folder " + getRandomAlphanumeric();
        String childFolderTitle = childFolderName + " Title";
        String childFolderDescription = childFolderName + " Description";
        
        // no need for fine control, create it using utility function
        FilePlanComponent parentFolder = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), parentFolderName);
        assertEquals(parentFolderName, parentFolder.getName());
     
        // Build the record category properties
        JsonObject unfiledFolderProperties = buildObject()
            .add(NAME, childFolderName)
            .add(NODE_TYPE, UNFILED_RECORD_FOLDER_TYPE.toString())
            .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, childFolderTitle)
                .add(PROPERTIES_DESCRIPTION, childFolderDescription)
                .end()
                .getJson();
        
        // create it as a child of parentFolder
        FilePlanComponent childFolder = filePlanComponentAPI.createFilePlanComponent(unfiledFolderProperties, 
            parentFolder.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(CREATED);        
        
        // Verify the returned file plan component
        assertFalse(childFolder.isIsCategory());
        assertFalse(childFolder.isIsFile());
        assertFalse(childFolder.isIsRecordFolder()); // it is not a _normal_ record folder!

        assertEquals(childFolder.getName(), childFolderName);
        assertEquals(childFolder.getNodeType(), UNFILED_RECORD_FOLDER_TYPE.toString());
        assertFalse(childFolder.isHasRetentionSchedule());

        assertEquals(childFolder.getCreatedByUser().getId(), dataUser.getAdminUser().getUsername());

        // Verify the returned file plan component properties
        FilePlanComponentProperties childProperties = childFolder.getProperties();
        assertEquals(childProperties.getTitle(), childFolderTitle);
        assertEquals(childProperties.getDescription(), childFolderDescription);
        
        // does this child point to its parent?
        assertEquals(childFolder.getParentId(), parentFolder.getId());
      
        // does child's parent point to it? 
        // perform another call as our parentFolder was executed before childFolder existed
        FilePlanComponentsCollection parentsChildren = filePlanComponentAPI.listChildComponents(parentFolder.getId());
        restWrapper.assertStatusCodeIs(OK);
        List<String> childIds = parentsChildren.getEntries()
            .stream()
            .map(c -> c.getFilePlanComponent().getId())
            .collect(Collectors.toList());
        
        // child folder is listed in parent
        assertTrue(childIds.contains(childFolder.getId()));
        
        // there is only one child
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
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        String modified = "Modified ";
        String folderName = "Folder To Modify" + getRandomAlphanumeric();
        
        // no need for fine control, create it using utility function
        FilePlanComponent folderToModify = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), folderName);
        assertEquals(folderName, folderToModify.getName());
        
        // Build the properties which will be updated
        JsonObject updateFolderProperties = buildObject()
            .add(NAME, modified + folderToModify.getName())
            .addObject(PROPERTIES)
                .add(PROPERTIES_TITLE, modified + folderToModify.getProperties().getTitle())
                .add(PROPERTIES_DESCRIPTION, modified + folderToModify.getProperties().getDescription())
                .end()
            .getJson();

        // Update the unfiled records folder
        filePlanComponentAPI.updateFilePlanComponent(updateFolderProperties, folderToModify.getId());
        // Verify the status code
        restWrapper.assertStatusCodeIs(OK);
        
        // this is to ensure the change was actually applied, rather than simply trusting the object returned by PUT
        FilePlanComponent renamedFolder = filePlanComponentAPI.getFilePlanComponent(folderToModify.getId());

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
        RestWrapper restWrapper = filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        String folderName = "Folder To Delete" + getRandomAlphanumeric();
        
        // no need for fine control, create it using utility function
        FilePlanComponent folderToModify = createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), folderName);
        assertEquals(folderName, folderToModify.getName());
        
        // delete folderToModify
        filePlanComponentAPI.deleteFilePlanComponent(folderToModify.getId());

        // Verify the status code
        restWrapper.assertStatusCodeIs(NO_CONTENT);

        // Deleted component should no longer be retrievable
        filePlanComponentAPI.getFilePlanComponent(folderToModify.getId());
        restWrapper.assertStatusCodeIs(NOT_FOUND);
    }
}
