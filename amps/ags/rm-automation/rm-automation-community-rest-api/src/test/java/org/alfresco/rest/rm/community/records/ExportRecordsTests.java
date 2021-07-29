/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import static org.alfresco.rest.core.RestRequest.simpleRequest;
import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.utils.FilePlanComponentsUtil.createTempFile;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_OK;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.requests.gscore.api.RecordFolderAPI;
import org.alfresco.rest.v0.ExportAPI;
import org.alfresco.rest.v0.RecordCategoriesAPI;
import org.alfresco.rest.v0.RecordFoldersAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.report.log.Step;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class contains tests for
 * Export REST API of RM Site
 *
 * @author Shubham Jain
 * @since 3.5.0
 */
public class ExportRecordsTests extends BaseRMRestTest
{
    private RecordCategory rootCategory;

    private RecordCategoryChild recordFolder;

    private Record record;

    private List<Record> recordList = new ArrayList<>();

    @Autowired
    private ExportAPI exportAPI;

    @Autowired
    private RecordCategoriesAPI categoriesAPI;

    @Autowired
    private RecordsAPI recordsAPI;

    @BeforeClass (alwaysRun = true)
    public void ExportRecordsTestsBeforeClass()
    {
        STEP("Create root level category");
        rootCategory = createRootCategory(getRandomName("Category"));

        STEP("Create the record folder inside the rootCategory");
        recordFolder = createRecordFolder(rootCategory.getId(), getRandomName("Folder"));

    }

    @AfterTest
    public void ExportRecordsTestsAfterTest(){

        STEP("Delete the created Records");
        for (int i = 0; i < recordList.size(); i++)
        {
            recordsAPI.deleteRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), recordList.get(i).getName(),
                    rootCategory.getName(), recordFolder.getName());

        }
        recordList.clear();
    }

    @AfterClass
    public void ExportRecordsTestsAfter()
    {

        STEP("Delete the created folder");
        deleteRecordFolder(recordFolder.getId());

        STEP("Delete the created rootCategory");
        categoriesAPI.deleteCategory(getAdminUser().getUsername(), getAdminUser().getPassword(), rootCategory.getName());

    }


    @DataProvider (name = "CreateRecordsInRecordFolder")
    public Object[][] getCreatedRecordsFromRecordFolder()
    {
        STEP("Created a records in RM site with size varying from 4 MB to 200 MB");
        for(int i = 4; i<200; i++){
            createRecord("Record_"+i+"MB",i*1000000);
            i=i+20;
        }

        return recordList.stream()
                          .map(record -> new Object[] { record })
                          .toArray(Object[][]::new);
    }


    /**
     * Given a Root Category
     * And an open record folder
     * When I upload a Record with size greater than 4 MB
     * Then I should be able to export it using APT
     */
    @Test (description = "Testing the Export functionality on RM site suing API for records of size >4MB",
            priority = 1,enabled = true)
    @AlfrescoTest (jira = "APPS-986")
    public void ExportRecord() throws Exception
    {

        STEP("Create an electronic record in RM site with size > 4MB");
        createRecord("Record_5MB", 5000000);

        STEP("Export the created record with size greater than 4 MB and verifying the expected response code");
        exportAPI.exportRecord(getAdminUser().getUsername(), getAdminUser().getPassword(),
                SC_OK, record.getName());

    }

    @Test (description = "Testing the Export functionality on RM site using API for multiple records at once with collective size of more than 4MB",
            priority = 2,enabled = true)
    public void ExportRecords() throws Exception
    {

        STEP("Create multiple electronic records with collective size greater than 4MB");

        createRecord("Record1", 1000000);
        createRecord("Record2", 2000000);
        createRecord("Record3", 3000000);

        STEP("Export all the created records at once and verifying the expected response code");
        exportAPI.exportRecords(getAdminUser().getUsername(), getAdminUser().getPassword(),
                SC_OK, recordList);

    }

    @Test (description = "Testing the Export functionality on RM site using API for recordFolder having records of size >4MB",
            priority = 3,enabled = true)
    public void ExportRecordFolder() throws Exception
    {
        STEP("Create an electronic record with size greater than 4 MB");
        createRecord("Record_5MB", 5000000);

        STEP("Export the Record Folder containing uploaded record with size greater than 4 MB and verifying the expected response code");
        exportAPI.exportRecordFolder(getAdminUser().getUsername(), getAdminUser().getPassword(),
                SC_OK, recordFolder.getName());
    }

    @Test (description = "Testing the Export functionality on RM site using API for records from size 4MB to 200MB",
            dataProvider = "CreateRecordsInRecordFolder",priority = 4,enabled = true)
    public void ExportRecordMultiple(Record record) throws Exception
    {
        //Export the created record and verifying the expected response code
        exportAPI.exportRecord(getAdminUser().getUsername(), getAdminUser().getPassword(),
                SC_OK, record.getName());
    }

    /**
     * Create a Record with a specific size in RM Site inside already created Record Folder
     * @param recordName Name of the record to be created
     * @param sizeInBytes Size of the record to be created in Bytes
     * @return Created record with defined size
     */
    public Record createRecord(String recordName, int sizeInBytes)
    {
        record = getRestAPIFactory().getRecordFolderAPI().createRecord(Record.builder().name(recordName)
                                                                             .nodeType(CONTENT_TYPE).build(), recordFolder.getId(),
                createTempFile("TempFile", sizeInBytes));
        recordList.add(record);
        return record;
    }
}
