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
package org.alfresco.module.org_alfresco_module_rm.test.integration.recordfolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Delete record folder test.
 * 
 * @author Roxana Lucanu
 * @since 2.4
 *
 */
public class DeleteRecordFolderTest extends BaseRMTestCase 
{
    // delete a destroyed record folder
    public void testDeleteDestroyedRecordFolder() throws Exception
    {

        final NodeRef testFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create folder
                NodeRef testFolder = recordFolderService.createRecordFolder(rmContainer, "Peter Edward Francis");

                // complete event
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(testFolder, CompleteEventAction.NAME, params);

                // cutoff folder
                rmActionService.executeRecordsManagementAction(testFolder, CutOffAction.NAME);
                
                // destroy folder
                rmActionService.executeRecordsManagementAction(testFolder, DestroyAction.NAME);

                return testFolder;
            }

            @Override
            public void test(NodeRef testFolder) throws Exception
            {
                // take a look at delete capability
                Capability deleteCapability = capabilityService.getCapability("DeleteRecordFolder");
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, deleteCapability.evaluate(testFolder));
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                fileFolderService.delete(testFolder);
                return null;
            }
        });
    }

}
