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

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.UNFILED_RECORDS_CONTAINER_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.Random;

import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.FilePlanComponentAPI;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Create/File Non-Electronic Record into Unfiled Record Container/Record Folder ReST API tests
 *
 * @author Kristijan Conkas
 * @since 2.6
 */
public class NonElectronicRecordTests extends BaseRestTest
{
    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    private DataUser dataUser;

    /** Valid root containers where non-electronic records can be created */
    @DataProvider(name = "validContainers")
    public Object[][] rootContainers() throws Exception {
        return new Object[][] {
            // an arbitrary record folder
            { createFolderInFilePlan(dataUser.getAdminUser(), FILE_PLAN_ALIAS.toString()) },
            // unfiled records root
            { filePlanComponentAPI.getFilePlanComponent(UNFILED_RECORDS_CONTAINER_ALIAS.toString()) },
            // an arbitrary unfiled records folder
            { createUnfiledRecordsFolder(UNFILED_RECORDS_CONTAINER_ALIAS.toString(), "Unfiled Folder " + getRandomAlphanumeric()) }
        };
    }
    
    /**
     * <pre>
     * Given a parent container that is NOT a record folder or an unfiled record folder
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test(description = "Non-electronic record can't be created as a child of invalid parent Id")
    public void noCreateForInvalidParentIds() throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        
        // non-electronic record object to be used for create tests
        FilePlanComponent nonElectronicRecord = new FilePlanComponent(
            "Record " + getRandomAlphanumeric(),
            NON_ELECTRONIC_RECORD_TYPE.toString(),
            new FilePlanComponentProperties());
        
        // create record category, non-electronic records can't be its children
        FilePlanComponent recordCategory = filePlanComponentAPI.createFilePlanComponent(
            new FilePlanComponent("Category " + getRandomAlphanumeric(), 
                RECORD_CATEGORY_TYPE.toString(),
                new FilePlanComponentProperties()), 
            FILE_PLAN_ALIAS.toString());
        
        // iterate through all invalid parent containers and try to create/file an electronic record
        asList(FILE_PLAN_ALIAS.toString(), TRANSFERS_ALIAS.toString(), HOLDS_ALIAS.toString(), recordCategory.getId())
            .stream()
            .forEach(id -> 
            {
                try
                {
                    filePlanComponentAPI.createFilePlanComponent(nonElectronicRecord, id);
                }
                catch (Exception error)
                {
                }

                // Verify the status code
                filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(UNPROCESSABLE_ENTITY);
            });
    }
    
    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * <pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * </pre>
     * @throws Exception if record can't be created
     */
    @Test
    (
        dataProvider = "validContainers",
        description = "Non-electronic records can be created in valid containers"
    )
    public void canCreateInValidContainers(FilePlanComponent container) throws Exception
    {
        logger.info("Root container:\n" + toJson(container));
        if (container.getNodeType().equals(RECORD_FOLDER_TYPE.toString()))
        {
            // only record folders can be open or closed
            assertFalse(container.getProperties().getIsClosed());
        }
        
        // use these properties for non-electronic record to be created
        String title = "Title " + getRandomAlphanumeric();
        String description = "Description " + getRandomAlphanumeric();
        String box = "Box "+ getRandomAlphanumeric();
        String file = "File " + getRandomAlphanumeric();
        String shelf = "Shelf " + getRandomAlphanumeric();
        String location = "Location " + getRandomAlphanumeric();
        
        Random random = new Random();
        Integer copies = random.nextInt(Integer.MAX_VALUE);
        Integer size = random.nextInt(Integer.MAX_VALUE);
        
        // set values of all available properties
        FilePlanComponentProperties properties = new FilePlanComponentProperties(title, description);
        properties.setBox(box);
        properties.setFile(file);
        properties.setShelf(shelf);
        properties.setLocation(location);
        properties.setNumberOfCopies(copies);
        properties.setPhysicalSize(size);
        
        // create non-electronic record
        String nonElectronicId = filePlanComponentAPI.createFilePlanComponent(
            new FilePlanComponent("Record " + getRandomAlphanumeric(), 
                NON_ELECTRONIC_RECORD_TYPE.toString(),
                properties), 
            container.getId()).getId();
        
        // verify the create request status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);
        
        // get newly created non-electonic record and verify its properties
        FilePlanComponent nonElectronicRecord = filePlanComponentAPI.getFilePlanComponent(nonElectronicId);
        
        assertEquals(title, nonElectronicRecord.getProperties().getTitle());
        assertEquals(description, nonElectronicRecord.getProperties().getDescription());
        assertEquals(box, nonElectronicRecord.getProperties().getBox());
        assertEquals(file, nonElectronicRecord.getProperties().getFile());
        assertEquals(shelf, nonElectronicRecord.getProperties().getShelf());
        assertEquals(location, nonElectronicRecord.getProperties().getLocation());
        assertEquals(copies, nonElectronicRecord.getProperties().getNumberOfCopies());
        assertEquals(size, nonElectronicRecord.getProperties().getPhysicalSize());
    }
    
    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is closed 
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test(description = "Non-electronic record can't be created in closed record folder")
    public void noCreateInClosedFolder() throws Exception
    {
        FilePlanComponent recordFolder = createFolderInFilePlan(dataUser.getAdminUser(), FILE_PLAN_ALIAS.toString());
        
        // the folder should be open
        assertFalse(recordFolder.getProperties().getIsClosed());
        
        // close the folder
        closeFolder(recordFolder.getId());
        
        // try to create it, this should fail and throw an exception
        try
        {
            filePlanComponentAPI.createFilePlanComponent(
                new FilePlanComponent("Record " + getRandomAlphanumeric(), 
                    NON_ELECTRONIC_RECORD_TYPE.toString(),
                    new FilePlanComponentProperties()), 
                recordFolder.getId()).getId();
        } 
        catch (Exception e)
        {
        }
        
        // verify the status code
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(UNPROCESSABLE_ENTITY);
    }

    /**
     * <pre>
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * and
     * <pre>
     * Given a parent container that is an unfiled record folder or the root unfiled record container
     * When I try to create a non-electronic record within the parent container
     * And I do not provide all the required mandatory property values
     * Then nothing happens
     * And an error is reported
     * </pre>
     * @throws Exception if prerequisites can't be created
     */
    @Test
    (
        dataProvider = "validContainers", 
        description = "Non-electronic record can only be created if all mandatory properties are given"
    )
    public void allMandatoryPropertiesRequired(FilePlanComponent container) throws Exception
    {
        logger.info("Root container:\n" + toJson(container));
        if (container.getNodeType().equals(RECORD_FOLDER_TYPE.toString()))
        {
            // only record folders can be open or closed
            assertFalse(container.getProperties().getIsClosed());
        }
        
        // component without name and title
        FilePlanComponent noNameOrTitle = getDummyNonElectronicRecord(); 
        
        // component with title only
        FilePlanComponent titleOnly = getDummyNonElectronicRecord();
        FilePlanComponentProperties properties = new FilePlanComponentProperties();
        properties.setTitle("Title " + getRandomAlphanumeric());
        titleOnly.setProperties(properties);

        // component with name only
        FilePlanComponent nameOnly = getDummyNonElectronicRecord();
        nameOnly.setName("Name " + getRandomAlphanumeric());

        // try to create invalid components 
        asList(noNameOrTitle, titleOnly, nameOnly).stream().forEach(c -> 
        {
            try
            {
                logger.info("Creating non-electronic record with body:\n" + toJson(c));
            }
            catch (Exception error)
            {
            }
            
            // this should fail and throw an exception
            try
            {                
                filePlanComponentAPI.createFilePlanComponent(c, container.getId());
            } 
            catch (Exception e)
            {
            }

            // verify the status code is BAD_REQUEST
            filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(BAD_REQUEST);
        });        
    }
    
    /**
     * Helper function to return an empty FilePlanComponent for non-electronic record
     * @return
     */
    private FilePlanComponent getDummyNonElectronicRecord()
    {
        FilePlanComponent component = new FilePlanComponent();
        component.setNodeType(NON_ELECTRONIC_RECORD_TYPE.toString());
        return component;
    }
    
    /**
     * Helper method to create a randomly-named <category>/<folder> structure in fileplan
     * @return record folder
     * @throws Exception on failed creation
     */
    private FilePlanComponent createFolderInFilePlan(UserModel user, String parentId) throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(user);
        
        // create root category
        FilePlanComponent recordCategory = createCategory(parentId, "Category " + getRandomAlphanumeric());
        
        // and return a folder underneath
        return createFolder(recordCategory.getId(), "Folder " + getRandomAlphanumeric());
    }
}
