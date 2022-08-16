/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */

package org.alfresco.rest.rm.community.records;

import org.alfresco.dataprep.ContentService;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.v0.RMRolesAndActionsAPI;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.alfresco.rest.core.v0.BaseAPI.RM_SITE_ID;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Tests to cover share action for records
 * @author Kavit Shah
 */
public class ShareRecordsTest extends BaseRMRestTest {

    /** data prep services*/
    @Autowired
    private RecordsAPI service;
    @Autowired
    private ContentService contentService;
    @Autowired
    private RMRolesAndActionsAPI rmRolesAndActionsAPI;
    /** Constants*/
    private final String TEST_PREFIX = generateTestPrefix(ShareRecordsTest.class);
    private final String CATEGORY = "CategoryWithSharedRecords" + TEST_PREFIX;
    private final String FOLDER = "FolderWithSharedRecords" + TEST_PREFIX;
    private final String ELECTRONIC_RECORD = "ELECTRONIC_RECORD" + TEST_PREFIX;
    private final String NONELECTRONIC_REC = "NON_ELECTRONIC_RECORD" + TEST_PREFIX;
    private RecordCategory category;
    private RecordCategoryChild recordCategoryChild;
    /**
     * Given a record
     * When admin tries to share it via API
     * Then the record can't be shared
     */
    @Test
    @AlfrescoTest(jira = "RM-5308")
    public void shareRecordViaApi()
    {
        //create RM Site
        createRMSiteIfNotExists();

        //create a category
        category = createRootCategory(CATEGORY);

        //create folder
        recordCategoryChild = createFolder(category.getId(),FOLDER);

        createNonElectronicRecord(recordCategoryChild.getId(),NONELECTRONIC_REC);

        // create record to be shared
        createElectronicRecord(recordCategoryChild.getId(),ELECTRONIC_RECORD);

        //get the node id for the ELECTRONIC_RECORD created
        String nodeRefRec1= contentService.getNodeRefByPath(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            "/Sites/" + RM_SITE_ID + "/documentLibrary/" + CATEGORY + "/" + FOLDER + "/" + service.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, ELECTRONIC_RECORD));
        //check record can't be shared
        assertFalse("The record has been succesfully shared",
            service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(),nodeRefRec1 ).getKey());
        //check the error code when trying to share a record
        assertEquals("The API response code is not " + HttpStatus.SC_INTERNAL_SERVER_ERROR, service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec1).getValue(),
            String.valueOf( HttpStatus.SC_INTERNAL_SERVER_ERROR));

        //get the node id for NONELECTRONIC_REC created
        String nodeRefRec2 = contentService.getNodeRefByPath(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(),
            "/Sites/" + RM_SITE_ID + "/documentLibrary/" + CATEGORY + "/" + FOLDER + "/" + service.getRecordFullName(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), FOLDER, NONELECTRONIC_REC));
        //check record can't be shared
        assertFalse("The record has been succesfully shared",
            service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec2).getKey());
        //check the error code when trying to share a record
        assertEquals("The API response code is not " + HttpStatus.SC_INTERNAL_SERVER_ERROR, service.shareDocument(getDataUser().usingAdmin().getAdminUser().getUsername(),
                getDataUser().usingAdmin().getAdminUser().getPassword(), nodeRefRec2).getValue(),
            String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR));
    }

    @AfterClass
    public void cleanupCategory() {
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, recordCategoryChild.getName());
        rmRolesAndActionsAPI.deleteAllItemsInContainer(getDataUser().usingAdmin().getAdminUser().getUsername(),
            getDataUser().usingAdmin().getAdminUser().getPassword(), RM_SITE_ID, category.getName());
        deleteRecordCategory(category.getId());
    }
}
