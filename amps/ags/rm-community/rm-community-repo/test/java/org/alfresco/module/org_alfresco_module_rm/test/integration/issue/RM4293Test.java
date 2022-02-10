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
 *
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Test for RM-4293
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RM4293Test  extends BaseRMTestCase
{
    public void testDeleteSpecialContainers()  throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
                assertNotNull(holdContainer);

                try
                {
                    fileFolderService.delete(holdContainer);
                    fail("This should have thrown an exception");
                }
                catch (IntegrityException e)
                {
                    // ("Hold Container can't be deleted.")
                }
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef transferContainer = filePlanService.getTransferContainer(filePlan);
                assertNotNull(transferContainer);
                try
                {
                    fileFolderService.delete(transferContainer);
                    fail("This should have thrown an exception");
                }
                catch (IntegrityException e)
                {
                    // ("Transfer Container can't be deleted.")
                }
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef unfiledRecordContainer = filePlanService.getUnfiledContainer(filePlan);
                assertNotNull(unfiledRecordContainer);

                try
                {
                    fileFolderService.delete(unfiledRecordContainer);
                    fail("This should have thrown an exception");
                }
                catch (IntegrityException e)
                {
                    // ("Unfiled Record Container can't be deleted.")
                }
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                try
                {
                    fileFolderService.delete(filePlan);
                    fail("This should have thrown an exception");
                }
                catch (IntegrityException e)
                {
                    // ("FilePlan can't be deleted.")
                }
                return null;
            }
        });
    }
}
