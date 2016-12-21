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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.requests.FilePlanComponentAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.data.DataUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Create/File electronic records tests
 * <br>
 * These tests only test the creation and filing of electronic records, update at
 * present isn't implemented in the API under test.
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class DeleteRecordTests extends BaseRestTest
{
    @Autowired
    private FilePlanComponentAPI filePlanComponentAPI;

    @Autowired
    private DataUser dataUser;

    /** image resource file to be used for records body */
    private static final String IMAGE_FILE = "money.JPG";

    /**
     * <pre>
     * Given a record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     * 
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete an electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteElectronicRecord(FilePlanComponent container) throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // create an electronic record
        FilePlanComponent record = FilePlanComponent.builder()
            .name("Record " + getRandomAlphanumeric())
            .nodeType(CONTENT_TYPE.toString())
            .build();
        FilePlanComponent newRecord = filePlanComponentAPI.createElectronicRecord(record, IMAGE_FILE, container.getId());
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        deleteAndVerify(newRecord);
    }
    
    /**
     * <pre>
     * Given a record
     * And that I have the "Delete Record" capability
     * And write permissions
     * When I delete the record
     * Then it is deleted from the file plan
     * </pre>
     * 
     * @param container
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Admin user can delete a non-electronic record"
    )
    @AlfrescoTest(jira="RM-4363")
    public void adminCanDeleteNonElectronicRecord(FilePlanComponent container) throws Exception
    {
        filePlanComponentAPI.usingRestWrapper().authenticateUser(dataUser.getAdminUser());

        // create a non-electronic record
        FilePlanComponent record = FilePlanComponent.builder()
            .name("Record " + getRandomAlphanumeric())
            .nodeType(NON_ELECTRONIC_RECORD_TYPE.toString())
            .build();
        FilePlanComponent newRecord = filePlanComponentAPI.createFilePlanComponent(
            record,
            container.getId());
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(CREATED);

        deleteAndVerify(newRecord);
    }
    
    private void deleteAndVerify(FilePlanComponent record) throws Exception
    {
        // delete it and verify status
        filePlanComponentAPI.deleteFilePlanComponent(record.getId());
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(NO_CONTENT);
        
        // try to get deleted file plan component
        filePlanComponentAPI.getFilePlanComponent(record.getId());
        filePlanComponentAPI.usingRestWrapper().assertStatusCodeIs(NOT_FOUND);
    }
}
