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

import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.rest.rm.community.requests.gscore.api.FilesAPI.PARENT_ID_PARAM;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordBodyFile;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * API tests for rejecting records
 * @author Ross Gale
 * @since 3.1
 */
public class RejectRecordTests extends BaseRMRestTest
{
    private final static String REJECT_REASON = "Just because";
    private SiteModel publicSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder, linkRecordFolder;

    @Autowired
    private RecordsAPI recordsAPI;
    @Autowired
    private RulesAPI rulesAPI;

    @BeforeClass (alwaysRun = true)
    public void setUp()
    {
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        linkRecordFolder = createFolder(recordCategory.getId(), getRandomName("linkRecordFolder"));
    }

    /**
     * Test that when rejecting a linked record that the link is also removed
     */
    @Test
    @AlfrescoTest(jira = "RM-6869")
    public void rejectLinkedRecord()
    {
        STEP("Create a document in the collaboration site");
        FileModel testFile = dataContent.usingSite(publicSite)
                                        .usingAdmin()
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare document as record with a location parameter value");
        Record record = getRestAPIFactory().getFilesAPI()
                                           .usingParams(String.format("%s=%s", PARENT_ID_PARAM, recordFolder.getId()))
                                           .declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Link record to new folder");
        RecordBodyFile linkRecordBody = RecordBodyFile.builder().targetParentId(linkRecordFolder.getId()).build();
        getRestAPIFactory().getRecordsAPI().fileRecord(linkRecordBody, record.getId());

        STEP("Verify the linked record has been added");
        assertTrue(isMatchingRecordInRecordFolder(testFile, linkRecordFolder), "Linked record not created");

        STEP("Reject record");
        recordsAPI.rejectRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), record.getName(), REJECT_REASON);

        STEP("Check record has been rejected");
        assertFalse(isMatchingRecordInRecordFolder(testFile, recordFolder), "Record rejection failure");

        STEP("Verify the linked record has been removed");
        assertFalse(isMatchingRecordInRecordFolder(testFile, linkRecordFolder), "Record link not removed");
    }

    /**
     * Test that rejecting a completed record is not possible
     */
    @Test
    @AlfrescoTest(jira = "RM-6881")
    public void rejectCompletedRecord()
    {
        STEP("Create a document in the collaboration site");
        FileModel testFile = dataContent.usingSite(publicSite)
                                        .usingAdmin()
                                        .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create a record folder with a reject rule");
        RecordCategoryChild folderWithRule = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule().title("name").description("description")
                                                      .applyToChildren(true).rejectReason(REJECT_REASON)
                                                      .actions(Collections.singletonList(ActionsOnRule.REJECT.getActionValue()));
        rulesAPI.createRule(getAdminUser().getUsername(), getAdminUser().getPassword(), NODE_PREFIX + folderWithRule.getId(), ruleDefinition);


        STEP("Declare document as record to Unfiled Records folder");
        Record record = getRestAPIFactory().getFilesAPI().declareAsRecord(testFile.getNodeRefWithoutVersion());
        assertStatusCode(CREATED);

        STEP("Complete, then file the record to the folder with rule");
        completeRecord(record.getId());
        RecordBodyFile recordBodyFile = RecordBodyFile.builder().targetParentId(folderWithRule.getId()).build();
        getRestAPIFactory().getRecordsAPI().fileRecord(recordBodyFile, record.getId());
        assertStatusCode(CREATED);

        STEP("Check record hasn't been rejected through rule");
        assertTrue(isMatchingRecordInRecordFolder(testFile, folderWithRule), "Record rejection succeeded!");

        STEP("Reject record directly through api");
        recordsAPI.rejectRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), SC_INTERNAL_SERVER_ERROR,
                record.getName(), REJECT_REASON);
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp()
    {
        deleteRecordCategory(recordCategory.getId());
        dataSite.deleteSite(publicSite);
    }
}
