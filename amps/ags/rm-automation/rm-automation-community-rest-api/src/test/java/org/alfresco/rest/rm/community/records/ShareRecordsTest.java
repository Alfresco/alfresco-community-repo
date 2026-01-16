/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.alfresco.dataprep.ContentService;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;

/**
 * Tests to cover share action for records
 * 
 * @author Kavit Shah
 */
public class ShareRecordsTest extends BaseRMRestTest
{

    /** data prep services */
    @Autowired
    private RecordsAPI service;
    @Autowired
    private ContentService contentService;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    /** Constants */
    private final String TEST_PREFIX = generateTestPrefix(ShareRecordsTest.class);
    private final String CATEGORY = "CategoryWithSharedRecords" + TEST_PREFIX;
    private final String FOLDER = "FolderWithSharedRecords" + TEST_PREFIX;
    private final String ELECTRONIC_RECORD = "ELECTRONIC_RECORD" + TEST_PREFIX;
    private final String NONELECTRONIC_REC = "NON_ELECTRONIC_RECORD" + TEST_PREFIX;
    private RecordCategory category;
    private RecordCategoryChild recordCategoryChild;

    /**
     * Given a record When admin tries to share it via API Then the record can't be shared
     */
    @Test
    @AlfrescoTest(jira = "RM-5308")
    public void shareRecordViaApi()
    {
        // create RM Site
        createRMSiteIfNotExists();

        // create a category
        category = createRootCategory(CATEGORY);

        // create folder
        recordCategoryChild = createFolder(category.getId(), FOLDER);

        createNonElectronicRecord(recordCategoryChild.getId(), NONELECTRONIC_REC);

        // create record to be shared
        createElectronicRecord(recordCategoryChild.getId(), ELECTRONIC_RECORD);

        // get the node id for the ELECTRONIC_RECORD created
        String nodeRefRec1 = contentService.getNodeRefByPath(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),
                "/Sites/" + RM_SITE_ID + "/documentLibrary/" + CATEGORY + "/" + FOLDER + "/" + service.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, ELECTRONIC_RECORD));
        // check record can't be shared
        assertFalse("The record has been succesfully shared",
                service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec1).getKey());
        // check the error code when trying to share a record
        assertEquals("The API response code is not " + HttpStatus.SC_INTERNAL_SERVER_ERROR, service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec1).getValue(),
                String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));

        // get the node id for NONELECTRONIC_REC created
        String nodeRefRec2 = contentService.getNodeRefByPath(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),
                "/Sites/" + RM_SITE_ID + "/documentLibrary/" + CATEGORY + "/" + FOLDER + "/" + service.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, NONELECTRONIC_REC));
        // check record can't be shared
        assertFalse("The record has been succesfully shared",
                service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                        getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec2).getKey());
        // check the error code when trying to share a record
        assertEquals("The API response code is not " + HttpStatus.SC_INTERNAL_SERVER_ERROR, service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec2).getValue(),
                String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    }

    @AfterClass
    public void cleanupCategory()
    {
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, recordCategoryChild.getName());
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, category.getName());
        deleteRecordCategory(category.getId());
    }
}
