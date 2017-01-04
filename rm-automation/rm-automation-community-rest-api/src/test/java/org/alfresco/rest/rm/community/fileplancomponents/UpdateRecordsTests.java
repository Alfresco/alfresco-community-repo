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

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.FILE_PLAN_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.HOLDS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAlias.TRANSFERS_ALIAS;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.rest.rm.community.util.PojoUtility.toJson;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponent;
import org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentProperties;
import org.alfresco.rest.rm.community.requests.igCoreAPI.FilePlanComponentAPI;
import org.alfresco.test.AlfrescoTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Update records tests
 * <br>
 * These tests only test the update of electronic and non-electronic tests
 * <p>
 * @author Kristijan Conkas
 * @since 2.6
 */
public class UpdateRecordsTests extends BaseRMRestTest
{
    /** Valid root containers where electronic records can be created */
    @DataProvider(name = "invalidParentContainers")
    public Object[][] invalidContainers() throws Exception
    {
        return new Object[][]
        {
            // record category
            { getFilePlanComponent(createCategoryFolderInFilePlan().getParentId()) },
            // file plan root
            { getFilePlanComponent(FILE_PLAN_ALIAS) },
            // transfers
            { getFilePlanComponent(TRANSFERS_ALIAS) },
            // holds
            { getFilePlanComponent(HOLDS_ALIAS) },
        };
    }

    /* to be used to append to modifications */
    private final String MODIFIED_PREFIX = "modified_";
    
    /**
     * <pre>
     * Given an incomplete record
     * When I try to update the records meta-data
     * Then the record is successfully updated
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Incomplete records can be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void incompleteRecordsCanBeUpdated(FilePlanComponent recordFolder) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        
        // create electronic and non-electronic records in a folder
        FilePlanComponent electronicRecord = filePlanComponentsAPI.createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, recordFolder.getId());
        assertStatusCode(CREATED);
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);
        
        for (FilePlanComponent record: Arrays.asList(electronicRecord, nonElectronicRecord)) {            
            // generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            FilePlanComponent updateRecord = FilePlanComponent.builder()
                .name(newName)
                .properties(FilePlanComponentProperties.builder()
                    .description(newDescription)
                    .title(newTitle)
                    .build())
                .build();

            // update record
            filePlanComponentsAPI.updateFilePlanComponent(updateRecord, record.getId());
            assertStatusCode(OK);

            // verify the update got applied
            FilePlanComponent updatedRecord = filePlanComponentsAPI.getFilePlanComponent(record.getId());
            assertEquals(updatedRecord.getName(), newName);
            assertEquals(updatedRecord.getProperties().getTitle(), newTitle);
            assertEquals(updatedRecord.getProperties().getDescription(), newDescription);
        }
    }
    
    /**
     * <pre>
     * Given a complete record
     * When I try to update the records meta-data
     * Then it fails
     * And and the records meta-data is unchanged
     * </pre>
     * @throws Exception
     */
    @Test
    (
        dataProvider = "validRootContainers",
        description = "Complete records can be updated"
    )
    @AlfrescoTest(jira="RM-4362")
    public void completeRecordsCantBeUpdated(FilePlanComponent recordFolder) throws Exception
    {
        FilePlanComponentAPI filePlanComponentsAPI = getRestAPIFactory().getFilePlanComponentsAPI();
        
        // create electronic and non-electronic records in a folder
        FilePlanComponent electronicRecord = filePlanComponentsAPI.createElectronicRecord(createElectronicRecordModel(), IMAGE_FILE, recordFolder.getId());
        assertStatusCode(CREATED);
        closeRecord(electronicRecord);
       
        FilePlanComponent nonElectronicRecord = filePlanComponentsAPI.createFilePlanComponent(createNonElectronicRecordModel(), recordFolder.getId());
        assertStatusCode(CREATED);
        closeRecord(nonElectronicRecord);
        
        for (FilePlanComponent record: Arrays.asList(electronicRecord, nonElectronicRecord)) {
            // generate update metadata
            String newName = getModifiedPropertyValue(record.getName());
            String newTitle = getModifiedPropertyValue(record.getProperties().getTitle());
            String newDescription = getModifiedPropertyValue(record.getProperties().getDescription());

            FilePlanComponent updateRecord = FilePlanComponent.builder()
                .name(newName)
                .properties(FilePlanComponentProperties.builder()
                    .description(newDescription)
                    .title(newTitle)
                    .build())
                .build();

            // attempt to update record
            filePlanComponentsAPI.updateFilePlanComponent(updateRecord, record.getId());
            assertStatusCode(BAD_REQUEST);

            // verify the original record metatada has been retained
            FilePlanComponent updatedRecord = filePlanComponentsAPI.getFilePlanComponent(record.getId());
            assertEquals(updatedRecord.getName(), record.getName());
            assertEquals(updatedRecord.getProperties().getTitle(), record.getProperties().getTitle());
            assertEquals(updatedRecord.getProperties().getDescription(), record.getProperties().getTitle());
        }
    }
    
    /**
     * Helper method to generate modified property value based on original value
     * @param originalValue original value
     * @return modified value
     */
    private String getModifiedPropertyValue(String originalValue)
    {
        return MODIFIED_PREFIX + originalValue;
    }
}
