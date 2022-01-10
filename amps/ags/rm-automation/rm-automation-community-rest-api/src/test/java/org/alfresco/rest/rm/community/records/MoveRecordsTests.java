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

import static org.alfresco.rest.rm.community.base.TestData.ELECTRONIC_RECORD_NAME;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentAspects.RECORD_SEARCH_ASPECT;
import static org.alfresco.rest.rm.community.utils.CoreUtil.createBodyForMoveCopy;
import static org.alfresco.rest.rm.community.utils.CoreUtil.toContentModel;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.OK;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.test.AlfrescoTest;
import org.testng.annotations.Test;

/**
 * Move records tests
 *
 * @author Claudia Agache
 * @since 3.3
 */
public class MoveRecordsTests extends BaseRMRestTest
{
    @Test (description = "rma:recordSearch aspect is reapplied after record move")
    @AlfrescoTest (jira = "RM-7060")
    public void moveRecord() throws Exception
    {
        String parentFolderId = createCategoryFolderInFilePlan().getId();
        String targetFolderId = createCategoryFolderInFilePlan().getId();
        String electronicRecordId = createElectronicRecord(parentFolderId, ELECTRONIC_RECORD_NAME).getId();
        STEP("Move record from one folder to the other");
        getRestAPIFactory().getNodeAPI(toContentModel(electronicRecordId)).move(createBodyForMoveCopy(targetFolderId));
        assertStatusCode(OK);

        STEP("Check the record still has rma:recordSearch aspect.");
        assertTrue(hasAspect(electronicRecordId, RECORD_SEARCH_ASPECT), "recordSearch aspect is lost after move!");
    }
}
