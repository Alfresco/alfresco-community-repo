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
import org.alfresco.util.GUID;

/**
 * Test for RM-1814
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RM1814Test extends BaseRMTestCase
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    public void testRM1814() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
                holdService.addToHold(hold, recordTwo);
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                relationshipService.addRelationship(CUSTOM_REF_VERSIONS.getLocalName(), recordOne, recordThree);
                return null;
            }
        });

        doTestInTransaction(new FailureTest
        (
            "Target node is in a hold."
        )
        {
            @Override
            public void run() throws Exception
            {
                relationshipService.addRelationship(CUSTOM_REF_OBSOLETES.getLocalName(), recordOne, recordTwo);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                relationshipService.addRelationship(CUSTOM_REF_SUPPORTS.getLocalName(), recordOne, recordFour);
                return null;
            }
        });
    }
}
