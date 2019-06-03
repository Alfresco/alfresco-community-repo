/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildCollection;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildEntry;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
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
    private SiteModel publicSite;
    private RecordCategory recordCategory;
    private RecordCategoryChild recordFolder;

    @BeforeClass (alwaysRun = true)
    public void setUp() throws Exception
    {
        createRMSiteIfNotExists();
        publicSite = dataSite.usingAdmin().createPublicRandomSite();
        recordCategory = createRootCategory(getRandomName("recordCategory"));
        recordFolder = createFolder(recordCategory.getId(), getRandomName("recordFolder"));
    }

    /**
     * Test that when rejecting a linked record that the link is also removed
     */
    @Test
    @AlfrescoTest(jira = "RM-6869")
    public void declareAndFileToValidLocationUsingActionsAPI() throws Exception
    {
        STEP("Create a document in the collaboration site");
        FileModel testFile = dataContent.usingSite(publicSite)
                              .usingAdmin()
                              .createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare document as record with a location parameter value");
        getRestAPIFactory().getActionsAPI(getAdminUser()).declareAndFile(testFile,
            Utility.buildPath(recordCategory.getName(), recordFolder.getName()));

        STEP("Link record to new folder");
        getRestAPIFactory().getActionsAPI().linkRecord(testFile, recordCategory.getName() + "/" + recordFolder.getName() + "_2");
        RecordCategoryChildCollection recordFolders = getRestAPIFactory().getRecordCategoryAPI().getRecordCategoryChildren(recordCategory.getId());
        Optional<RecordCategoryChildEntry> linkedFolder = recordFolders.getEntries().stream().filter(child -> child.getEntry().getName().equals(recordFolder.getName() + "_2"))
                                                            .findFirst();
        if (linkedFolder.isPresent())
        {
            STEP("Verify the linked record has been added");
            assertFalse(getRestAPIFactory().getRecordFolderAPI().getRecordFolderChildren(linkedFolder.get().getEntry().getId()).isEmpty());

            STEP("Reject record");
            getRestAPIFactory().getActionsAPI().rejectRecord(testFile, "Just because");

            STEP("Check record has been rejected");
            assertFalse(isMatchingRecordInRecordFolder(testFile, recordFolder));

            STEP("Verify the linked record has been removed");
            assertTrue(getRestAPIFactory().getRecordFolderAPI().getRecordFolderChildren(linkedFolder.get().getEntry().getId()).isEmpty());
        }
    }

    @AfterClass (alwaysRun = true)
    public void cleanUp()
    {
        deleteRecordCategory(recordCategory.getId());
        dataSite.deleteSite(publicSite);
    }
}
