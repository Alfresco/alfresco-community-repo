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

/**
 * Integration test for RM-1887
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RM1887Test extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * Given that a record is unfiled
     * And an unfiled folder has been created
     * When I move the unfiled record into the unfiled folder
     * Then the filed date of the unfiled record remains unset
     */
    public void testMoveUnfiledRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef unfiledRecordFolder;
            private NodeRef unfiledRecord;

            public void given() throws Exception
            {
                // create unfiled folder
                unfiledRecordFolder = fileFolderService.create(filePlanService.getUnfiledContainer(filePlan), "my test folder", TYPE_UNFILED_RECORD_FOLDER).getNodeRef();

                // crate unfiled record
                unfiledRecord = recordService.createRecordFromContent(filePlan, "test.txt", TYPE_CONTENT, null, null);

                // check the record
                assertTrue(recordService.isRecord(unfiledRecord));
                assertFalse(recordService.isFiled(unfiledRecord));
            }

            public void when() throws Exception
            {
                // move the record into the unfiled folder
                fileFolderService.move(unfiledRecord, unfiledRecordFolder, null);
            }

            public void then()
            {
                // check the record
                assertTrue(recordService.isRecord(unfiledRecord));
                assertFalse(recordService.isFiled(unfiledRecord));
            }
        });

    }


}
