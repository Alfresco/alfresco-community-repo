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

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.v0.RecordsAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.model.FileModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Declare records tests
 *
 * @author Claudia Agache
 * @since 2.7
 */
public class DeclareRecordTests extends BaseRMRestTest
{
    @Autowired
    RecordsAPI recordsAPI;

    /**
     * <pre>
     * Given a file that has version declared as record
     * When the file is declared as record
     * Then the action is successful
     * </pre>
     */
    @Test (description = "Declaring as record a file that has a version declared as record is successful")
    @AlfrescoTest (jira = "RM-6786")
    public void declareAsRecordAFileWithARecordVersion() throws Exception
    {
        STEP("Create a file.");
        testSite = dataSite.usingAdmin().createPublicRandomSite();
        FileModel testFile = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Declare file version as record and check that record is successfully created.");
        recordsAPI.declareDocumentVersionAsRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), testSite.getId(),
                testFile.getName());

        STEP("Declare file as record and check that record is successfully created.");
        recordsAPI.declareDocumentAsRecord(getAdminUser().getUsername(), getAdminUser().getPassword(), testSite.getId(),
                testFile.getName());
    }

    @AfterClass
    public void cleanUp()
    {
        STEP("Clean up.");
        dataSite.deleteSite(testSite);
    }
}
