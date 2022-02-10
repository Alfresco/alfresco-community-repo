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
package org.alfresco.rest.rm.community.records;

import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.IMAGE_FILE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createNonElectronicRecordModel;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.getFile;
import static org.alfresco.rest.rm.community.utils.RMSiteUtil.createDOD5015RMSiteModel;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.testng.Assert.assertEquals;

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
    private static final String PARAMETERS = "include=isCompleted";

    /**
     * Incomplete records with mandatory meta-data missing
     */
    @DataProvider (name = "IncompleteRecordsMandatoryMetadataMissing")
    public Object[][] getIncompleteRecordsMandatoryMetadataMissing()
    {
        //create RM site
        createRMSite(createDOD5015RMSiteModel());

        // create electronic and non-electronic records
        return createAndVerifyRecordsInFolder();
    }

    /**
     * Incomplete records with mandatory meta-data present
     */
    @DataProvider (name = "IncompleteRecordsMandatoryMetadataPresent")
    public Object[][] getIncompleteRecordsMandatoryMetadataPresent()
    {
        // create electronic and non-electronic records
        return createAndVerifyRecordsInFolder();
    }

    /**
     * <pre>
     * Given the repository is configured to check mandatory data before completing a record
     * And an incomplete record with its mandatory meta-data missing
     * When I complete the record
     * Then I receive an error indicating that I can't complete the operation,
     * because some of the mandatory meta-data of the record is missing
     * </pre>
     */
    @Test
        (
            dataProvider = "IncompleteRecordsMandatoryMetadataMissing",
            description = "Cannot complete electronic and non-electronic records with mandatory metadata missing",
            priority = 1
        )
    @AlfrescoTest (jira = "RM-4431")
    public void completeRecordWithMandatoryMetadataMissing(Record record)
    {
        verifyRecordCompletionStatus(record, INCOMPLETE);

        // Complete record
        completeRecord(record);
        assertStatusCode(UNPROCESSABLE_ENTITY);

        verifyRecordCompletionStatus(record, INCOMPLETE);
    }

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
    public void completeRecordWithMandatoryMetadataPresent(Record record)
    {
        verifyRecordCompletionStatus(record, INCOMPLETE);

        // Complete record
        completeRecord(record);
        assertStatusCode(CREATED);

        verifyRecordCompletionStatus(record, COMPLETE);
    }

    /**
     * <pre>
     * Given a document that is not a record or any non-document node
     * When I complete the item
     * Then I receive an unsupported operation error
     * </pre>
     */
    @Test (description = "Cannot complete a document that is not a record")
    @AlfrescoTest (jira = "RM-4431")
    public void completeNonRecord()
    {
        // Get the recordsAPI
        getRestAPIFactory().getRecordsAPI()
                           .completeRecord(createCategoryFolderInFilePlan().getId(), PARAMETERS);
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
    public void completeAlreadyCompletedRecord(Record record)
    {
        verifyRecordCompletionStatus(record, INCOMPLETE);

        // Complete record
        completeRecord(record);
        assertStatusCode(CREATED);

        verifyRecordCompletionStatus(record, COMPLETE);

        // Complete record
        completeRecord(record);
        assertStatusCode(UNPROCESSABLE_ENTITY);
    }

    /**
     * Helper method to create records and and assert successful creation
     */
    private Record[][] createAndVerifyRecordsInFolder()
    {
        RecordFolderAPI recordFolderAPI = getRestAPIFactory().getRecordFolderAPI();

        // create record folder
        String recordFolderId = createCategoryFolderInFilePlan().getId();

        // create electronic record in record folder
        Record electronicRecord = recordFolderAPI.createRecord(createElectronicRecordModel(), recordFolderId, getFile(IMAGE_FILE));
        assertStatusCode(CREATED);

        // create non-electronic record in record folder
        Record nonElectronicRecord = recordFolderAPI.createRecord(createNonElectronicRecordModel(), recordFolderId);
        assertStatusCode(CREATED);

        return new Record[][]
        {
            { electronicRecord },
            { nonElectronicRecord }
        };
    }

    /**
     * Helper method to verify record is complete or incomplete
     */
    private void verifyRecordCompletionStatus(Record record, Boolean completionStatus)
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        Record recordModel = recordsAPI.getRecord(record.getId(), PARAMETERS);
        assertEquals(recordModel.getIsCompleted(), completionStatus);
    }

    /**
     * Helper method to complete a record
     */
    private void completeRecord(Record record)
    {
        RecordsAPI recordsAPI = getRestAPIFactory().getRecordsAPI();
        recordsAPI.completeRecord(record.getId(), PARAMETERS);
    }
}
