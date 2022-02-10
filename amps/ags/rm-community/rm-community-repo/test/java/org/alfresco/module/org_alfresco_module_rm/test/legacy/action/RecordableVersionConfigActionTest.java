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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import static org.alfresco.module.org_alfresco_module_rm.action.dm.RecordableVersionConfigAction.NAME;
import static org.alfresco.module.org_alfresco_module_rm.action.dm.RecordableVersionConfigAction.PARAM_VERSION;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.ALL;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.MAJOR_ONLY;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.NONE;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Recordable version config action test
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordableVersionConfigActionTest extends BaseRMTestCase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    public void testRecordableVersionConfigAction()
    {
        // Uncommented due to the failures on bamboo. Also this is related to RM-1758
        /*
        doTestInTransaction(new Test<Void>()
        {
            final NodeRef document1 = fileFolderService.create(dmFolder, "aDocument", ContentModel.TYPE_CONTENT).getNodeRef();
            public Void run()
            {
                Action action = actionService.createAction(NAME);
                action.setParameterValue(PARAM_VERSION, MAJOR_ONLY.toString());
                actionService.executeAction(action, document1);
                return null;
            }

            public void test(Void result) throws Exception
            {
                Serializable version = nodeService.getProperty(document1, PROP_RECORDABLE_VERSION_POLICY);
                assertNotNull(version);
                assertEquals(MAJOR_ONLY.toString(), (String) version);
            };
        },
        dmCollaborator);
        */

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                Action action = actionService.createAction(NAME);
                action.setParameterValue(PARAM_VERSION, ALL.toString());
                actionService.executeAction(action, dmFolder);
                return null;
            }

            public void test(Void result) throws Exception
            {
                assertNull(nodeService.getProperty(dmFolder, PROP_RECORDABLE_VERSION_POLICY));
            }
        },
        dmCollaborator);

        doTestInTransaction(new Test<Void>()
        {
            final NodeRef document2 = fileFolderService.create(dmFolder, "another document", ContentModel.TYPE_CONTENT).getNodeRef();
            public Void run()
            {
                Action action = actionService.createAction(NAME);
                action.setParameterValue(PARAM_VERSION, NONE.toString());
                actionService.executeAction(action, document2);
                return null;
            }

            public void test(Void result) throws Exception
            {
                assertNull(nodeService.getProperty(document2, PROP_RECORDABLE_VERSION_POLICY));
            }
        },
        dmCollaborator);


        doTestInTransaction(new Test<Void>()
        {
            final NodeRef document3 = fileFolderService.create(dmFolder, "testfile.txt", ContentModel.TYPE_CONTENT).getNodeRef();
            public Void run()
            {
                Action createAction = actionService.createAction(CreateRecordAction.NAME);
                createAction.setParameterValue(CreateRecordAction.PARAM_FILE_PLAN, filePlan);
                actionService.executeAction(createAction, document3);

                Action action = actionService.createAction(NAME);
                action.setParameterValue(PARAM_VERSION, MAJOR_ONLY.toString());
                actionService.executeAction(action, document3);
                return null;
            }

            public void test(Void result) throws Exception
            {
                assertNull(nodeService.getProperty(document3, PROP_RECORDABLE_VERSION_POLICY));
            }
        },
        dmCollaborator);
    }
}
