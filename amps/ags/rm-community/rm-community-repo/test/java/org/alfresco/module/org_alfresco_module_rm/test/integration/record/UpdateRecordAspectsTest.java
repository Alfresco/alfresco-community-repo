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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * remove aspects test
 *
 * @author Ramona Popa
 * @since 2.6
 */
public class UpdateRecordAspectsTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * RM-4926
     * RM specific aspects can be removed only by System user
     */
    public void testRemoveRMAspectsFromElectronicRecord() throws Exception
    {
        final NodeRef record = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create file plan structure and a record
                NodeRef rc = filePlanService.createRecordCategory(filePlan, GUID.generate());
                NodeRef recordFolder = recordFolderService.createRecordFolder(rc, GUID.generate());

                return recordService.createRecordFromContent(recordFolder, GUID.generate(), TYPE_CONTENT, null, null);
            }
        });

        doTestInTransaction(new FailureTest(IntegrityException.class)
        {
            @Override
            public void run()
            {
                nodeService.removeAspect(record, ASPECT_RECORD);
            }
        });

        doTestInTransaction(new FailureTest(IntegrityException.class)
        {
            @Override
            public void run()
            {
                nodeService.removeAspect(record, ASPECT_FILE_PLAN_COMPONENT);
            }
        });

        doTestInTransaction(new FailureTest(IntegrityException.class)
        {
            @Override
            public void run()
            {
                nodeService.removeAspect(record, ASPECT_RECORD_COMPONENT_ID);
            }
        });

        doTestInTransaction(new FailureTest(IntegrityException.class)
        {
            @Override
            public void run()
            {
                nodeService.removeAspect(record, ASPECT_COMMON_RECORD_DETAILS);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                nodeService.removeAspect(record, ASPECT_RECORD);
                nodeService.removeAspect(record, ASPECT_FILE_PLAN_COMPONENT);
                nodeService.removeAspect(record, ASPECT_RECORD_COMPONENT_ID);
                nodeService.removeAspect(record, ASPECT_COMMON_RECORD_DETAILS);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}
