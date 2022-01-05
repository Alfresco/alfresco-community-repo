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

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_OK;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.ExportAPI;
import org.alfresco.test.AlfrescoTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains tests for testing the Export functionality on RM site
 *
 * @author Shubham Jain
 * @since 7.1.0
 */
public class ExportRecordsTests extends BaseRMRestTest
{
    private RecordCategory rootCategory;

    private RecordCategoryChild recordFolder;

    @Autowired
    private ExportAPI exportAPI;

    @BeforeClass (alwaysRun = true)
    public void exportRecordsTestsBeforeClass()
    {
        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("Create the record folder inside the rootCategory");
        recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("Folder"));

    }

    @DataProvider (name = "CreateRMNodes")
    public Object[][] getRMNodeID()
    {
        return new String[][] {
                { createRecord("Record_4MB", 4).getId() },
                { createRecord("Record_200MB", 200).getId() },
                { recordFolder.getId() }
        };
    }

    /**
     * Given a record with size > 4 MB
     * When I export the record using API
     * Then the request is successful
     */
    @Test (description = "Testing the RM Export functionality for records of size >4MB and Record " +
            "Folder containing records with size >4MB",
            dataProvider = "CreateRMNodes")
    @AlfrescoTest (jira = "APPS-986")
    public void exportRMNodeTest(String nodeID)
    {
        STEP("Export the created record/record folder with size greater than 4 MB and verifying the expected response" +
                " code");
        exportAPI.exportRMNode(getAdminUser().getUsername(), getAdminUser().getPassword(), SC_OK, nodeID);
    }

    /**
     * I would change this to
     * Given a list of records with a size > 4MB
     * When I export the records
     * Then the request is succesfull
     */
    @Test (description = "Testing the RM Export functionality using API for a list of Records at once with " +
            "collective size of more than 4MB")
    public void exportRecordsTest()
    {
        STEP("Export all the created records at once and verifying the expected response code");
        exportAPI.exportRMNodes(getAdminUser().getUsername(), getAdminUser().getPassword(),
                SC_OK, asList(createRecord("Record_2MB", 2).getId(), createRecord("Record_3MB", 3).getId()));
    }

    /**
     * Create a Record with a specific size in RM Site inside already created Record Folder
     *
     * @param recordName      Name of the record to be created
     * @param sizeInMegaBytes Size of the record to be created in MegaBytes
     * @return Created record with defined size
     */
    public Record createRecord(String recordName, int sizeInMegaBytes)
    {
        return getRestAPIFactory().getRecordFolderAPI().createRecord(Record.builder().name(recordName)
                                                                           .nodeType(CONTENT_TYPE).build(), recordFolder.getId(),
                createTempFile("TempFile", sizeInMegaBytes));
    }

    @AfterClass (alwaysRun = true)
    public void exportRecordsTestsAfter()
    {
        STEP("Delete the created rootCategory along with corresponding record folders/records present in it");
        getRestAPIFactory().getRecordCategoryAPI().deleteRecordCategory(rootCategory.getId());
    }
}
