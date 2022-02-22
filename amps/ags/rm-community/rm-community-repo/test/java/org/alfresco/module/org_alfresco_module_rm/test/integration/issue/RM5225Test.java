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
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Integration test for RM-5225
 *
 * Copy of record from a RM site does not append the correct id at the end of the record name.
 */
public class RM5225Test extends BaseRMTestCase
{
    /**
     * Given the RM site, a record category created in the fileplan, a record folder containing a record
     * When we create a copy from the existing record
     * Then the created record name contains both the name of the record from which it was created and the unique identifier of the current record.
     */
    public void testCopyToRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private NodeRef copiedRecord;

            public void given()
            {

                /** Create record category. */
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());

                /** Create record folder. */
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());

                /** File record. */
                record = utils.createRecord(recordFolder, GUID.generate());
            }

            public void when()
            {
                /** Create a copy of the original record */
                copiedRecord = recordService.createRecordFromCopy(filePlan, record);
            }

            public void then()
            {
                /** Check if the copied record contains the name of the record from which is copied. */
                assertTrue(nodeService.getProperty(copiedRecord, PROP_NAME).toString().contains(nodeService.getProperty(record, PROP_NAME).toString()));
                /** Check if the copied record name contains its unique id. */
                assertTrue(nodeService.getProperty(copiedRecord, PROP_NAME).toString().contains(nodeService.getProperty(copiedRecord, PROP_IDENTIFIER).toString()));
            }
        });
    }
}
