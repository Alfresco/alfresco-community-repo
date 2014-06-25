/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.module.org_alfresco_module_rm.action.impl.FileReportAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.GUID;

/**
 * File report action unit test
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class FileReportActionTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    public void testFileReport()
    {
        fileReport(MimetypeMap.MIMETYPE_HTML);
    }

    public void testfileReportDefaultMimetype()
    {
        fileReport(null);
    }

    private void fileReport(final String mimeType)
    {
        // create record folder
        final NodeRef recordFolder = recordFolderService.createRecordFolder(rmContainer, GUID.generate());

        // create hold
        final NodeRef hold = holdService.createHold(filePlan, "holdName", "holdReason", "holdDescription");

        doTestInTransaction(new FailureTest()
        {
            @Override
            public void run() throws Exception
            {
                // close the record folder
                recordFolderService.closeRecordFolder(recordFolder);

                // execute action
                executeAction(mimeType, recordFolder, hold);
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // reopen the record folder
                nodeService.setProperty(recordFolder, PROP_IS_CLOSED, false);

                // execute action
                executeAction(mimeType, recordFolder, hold);

                return null;
            }
        }, ADMIN_USER);
    }

    private void executeAction(String mimeType, NodeRef recordFolder, NodeRef hold)
    {
        Action action = actionService.createAction(FileReportAction.NAME);
        if (StringUtils.isNotBlank(mimeType))
        {
            action.setParameterValue(FileReportAction.MIMETYPE, mimeType);
        }
        action.setParameterValue(FileReportAction.DESTINATION, recordFolder.toString());
        action.setParameterValue(FileReportAction.REPORT_TYPE, "rmr:destructionReport");
        actionService.executeAction(action, hold);
    }
}
