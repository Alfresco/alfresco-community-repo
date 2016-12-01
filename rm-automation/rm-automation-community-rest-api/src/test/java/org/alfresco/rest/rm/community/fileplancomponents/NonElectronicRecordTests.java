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
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentFields.IS_CLOSED;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    
    /**
     * Given a parent container that is NOT a record folder or an unfiled record folder
     * When I try to create a non-electronic record within the parent container
     * Then nothing happens
     * And an error is reported
     * @throws Exception 
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
     * Given a parent container that is a record folder
     * And the record folder is open
     * When I try to create a non-electronic record within the parent container
     * Then the non-electronic record is created
     * And the details of the new record are returned
     * @throws Exception if record can't be created
     */
    @Test(description = "Non-electronic record can be created in open record folder")
    public void canCreateInOpenFolder() throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());
        
        // create root category
        FilePlanComponent recordCategory = filePlanComponentAPI.createFilePlanComponent(
            new FilePlanComponent("Category " + getRandomAlphanumeric(), 
                RECORD_CATEGORY_TYPE.toString(),
                new FilePlanComponentProperties()), 
            FILE_PLAN_ALIAS.toString());
        
        // create record folder as a child of recordCategory
        FilePlanComponent recordFolder = filePlanComponentAPI.withParams("include=" + IS_CLOSED)
            .createFilePlanComponent(new FilePlanComponent("Folder " + getRandomAlphanumeric(), 
                RECORD_FOLDER_TYPE.toString(),
                new FilePlanComponentProperties()), 
            recordCategory.getId());
        
        // the folder should be open
        assertFalse(recordFolder.isClosed());
        
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
            recordFolder.getId()).getId();
        
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
}
