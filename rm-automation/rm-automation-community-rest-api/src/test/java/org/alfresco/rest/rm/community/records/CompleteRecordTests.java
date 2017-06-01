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
package org.alfresco.rest.rm.community.records;

import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains the tests for
 * Complete Record Action REST API
 *
 * @author Sara Aspery
 * @since 2.6
 */
public class CompleteRecordTests extends BaseRMRestTest
{
    private static final Boolean COMPLETE = true;
    private static final Boolean INCOMPLETE = false;
    private static final String parameters = "include=isCompleted";

    /**
     * Incomplete records with mandatory meta-data present
     */
    @DataProvider (name = "IncompleteRecordsMandatoryMetadataPresent")
    public Object[][] getIncompleteRecordsMandatoryMetadataPresent() throws Exception
    {
        createRMSiteIfNotExists();
        createMandatoryMetadata();

        // create record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        //create electronic record in record folder
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId,
            getFile(IMAGE_FILE));
        assertStatusCode(CREATED);
        setMandatoryMetadata(electronicRecord);
        // TODO verfiy mandatory metadata is present

        //create non-electronic record in record folder
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);
        setMandatoryMetadata(nonElectronicRecord);

        return new String[][]
            {
                // an arbitrary record folder
                { electronicRecord.getId(), nonElectronicRecord.getId() },
            };
    }

    /**
     * Incomplete records with mandatory meta-data missing
     */
    @DataProvider (name = "IncompleteRecordsMandatoryMetadataMissing")
    public Object[][] getIncompleteRecordsMandatoryMetadataMissing() throws Exception
    {
        createRMSiteIfNotExists();
        createMandatoryMetadata();

        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        //create electronic record in record folder
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId,
            getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        //create non-electronic record in record folder
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        return new String[][]
            {
                // an arbitrary record folder
                { electronicRecord.getId(), nonElectronicRecord.getId() },
            };
    }

    // TODO repeat previous 2 providers but with config set to not chk for mandatory data

    /**
     * Document to be completed is not a record
     */
    @DataProvider (name = "Supplied node is not a record")
    // TODO include is a document but not a record

    public Object[][] getNodesWhichAreNotRecords() throws Exception
    {
        //create record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId,
            getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        //create non-electronic record in record folder
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        return new String[][]
            {
                { createCategoryFolderInFilePlan().getId() },

            };
    }

    /**
     * Incomplete records with mandatory meta-data missing
     */
    @DataProvider (name = "FrozenRecords")
    public Object[][] getFrozenRecords() throws Exception
    {
        // TODO consider adding method to BaseRMRestTest eg. createRMSiteIfNotExists(DOD5015);
        createRMSiteIfNotExists();

        // TODO add custom metadata to record model, and do not populate it
        //create electronic record in record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId,
            getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        //create non-electronic record in record folder
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        return new String[][]
            {
                // an arbitrary record folder
                { electronicRecord.getId(), nonElectronicRecord.getId() },
            };
    }

    // TODO Add test for authentication fails (see yaml file)
    // TODO Add test for user does not have permission to complete record

    /**
     * <pre>
     * Given the repository is configured to check mandatory data before completing a record
     * And an incomplete record with all mandatory meta-data present
     * When I complete the record
     * Then the record is successfully completed
     * </pre>
     */
    @Test
        (
            dataProvider = "IncompleteRecordsMandatoryMetadataPresent",
            description = "Can complete electronic and non-electronic records with mandatory metadata present"
        )
    @AlfrescoTest (jira = "RM-4431")
    public void completeRecordWithMandatoryMetadataPresent(String electronicRecordId, String nonElectronicRecordId)
        throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record electronicRecord = recordsAPI.getRecord(electronicRecordId);
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicRecordId);

        for (Record record : Arrays.asList(electronicRecord, nonElectronicRecord))
        {
            Record recordModel;
            // Verify the record is incomplete
            recordModel = recordsAPI.getRecord(record.getId(), parameters);
            assertEquals(recordModel.getIsCompleted(), INCOMPLETE);

            // Complete record
            recordModel = recordsAPI.completeRecord(record.getId(), parameters);
            assertStatusCode(CREATED);

            // Verify the record has been completed
            assertEquals(recordModel.getIsCompleted(), COMPLETE);
        }
    }

    /**
     * <pre>
     * Given the repository is configured to check mandatory data before completing a record
     * And an incomplete record with missing mandatory meta-data
     * When I complete the record
     * Then I receive an error indicating that I can't complete the operation,
     * because some of the mandatory meta-data of the record is missing
     * </pre>
     */
    @Test
        (
            dataProvider = "IncompleteRecordsMandatoryMetadataMissing",
            description = "Cannot complete electronic and non-electronic records with mandatory metadata missing"
        )
    @AlfrescoTest (jira = "RM-4431")
    public void completeRecordWithMandatoryMetadataMissing(String electronicRecordId, String nonElectronicRecordId)
        throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record electronicRecord = recordsAPI.getRecord(electronicRecordId);
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicRecordId);

        for (Record record : Arrays.asList(nonElectronicRecord))
        {
            Record recordModel;
            // Verify the record is incomplete
            recordModel = recordsAPI.getRecord(record.getId(), parameters);
            assertEquals(recordModel.getIsCompleted(), INCOMPLETE);

            //Verify the record has missing mandatory metadata
            // TODO change next line to get custom metadata and check not populated
            //assertEquals(recordModel.  .getProperties()  .getTitle(), INCOMPLETE);

            // Complete record
            recordModel = recordsAPI.completeRecord(record.getId(), parameters);
            assertStatusCode(UNPROCESSABLE_ENTITY);

            // Verify the record has not been completed
            assertEquals(recordModel.getIsCompleted(), INCOMPLETE);
        }
    }

    /**
     * <pre>
     * Given a document that is not a record or any non-document node
     * When I complete the item
     * Then I receive an unsupported operation error
     * </pre>
     */
    @Test
        (
            dataProvider = "Supplied node is not a record",
            description = "Cannot complete a document that is not a record"
        )
    @AlfrescoTest (jira = "RM-4431")
    public void completeNonRecord(String nonRecordId)
        throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record recordModel;
        recordModel = recordsAPI.completeRecord(nonRecordId, parameters);
        assertStatusCode(BAD_REQUEST);
    }

    /**
     * <pre>
     * Given a record that is already completed
     * When I complete the record
     * Then I receive an error indicating that I can't complete the operation, because the record is already complete
     * </pre>
     */
    @Test
        (
            dataProvider = "IncompleteRecordsMandatoryMetadataPresent",
            description = "Cannot complete a record that is already completed"
        )
    @AlfrescoTest (jira = "RM-4431")
    public void completeAlreadyCompletedRecord(String electronicRecordId, String nonElectronicRecordId)
        throws Exception
    {
        // Get the recordsAPI
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record electronicRecord = recordsAPI.getRecord(electronicRecordId);
        Record nonElectronicRecord = recordsAPI.getRecord(nonElectronicRecordId);

        for (Record record : Arrays.asList(nonElectronicRecord))
        {
            Record recordModel;
            // If the record is incomplete, complete it
            recordModel = recordsAPI.getRecord(record.getId(), parameters);
            if (recordModel.getIsCompleted().equals(INCOMPLETE))
            {
                recordModel = recordsAPI.completeRecord(record.getId(), parameters);
            }

            // Verify the record is already completed
            assertEquals(recordModel.getIsCompleted(), COMPLETE);

            // Complete record
            recordModel = recordsAPI.completeRecord(record.getId(), parameters);
            assertStatusCode(UNPROCESSABLE_ENTITY);
        }
    }


    private void createMandatoryMetadata()
    {
    }

    private void setMandatoryMetadata(Record record)
    {
    }
}
