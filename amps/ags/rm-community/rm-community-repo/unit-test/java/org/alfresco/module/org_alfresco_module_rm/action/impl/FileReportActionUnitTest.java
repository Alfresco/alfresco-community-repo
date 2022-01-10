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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.mockito.Mockito.verifyZeroInteractions;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.BaseActionUnitTest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for file report action.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class FileReportActionUnitTest extends BaseActionUnitTest
{
    /** actioned upon node reference */
    private NodeRef actionedUponNodeRef;

    /** file report action */
    private @InjectMocks FileReportAction fileReportAction;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before() throws Exception
    {
        super.before();

        // actioned upon node reference
        actionedUponNodeRef = generateRecord();

        // mocked action
        fileReportAction.setAuditable(false);
    }

    /**
     * given the destination is not set, ensure that an exception is thrown
     */
    @Test
    public void destinationNotSet()
    {
        // == given ==

        // set action parameter values
        mockActionParameterValue(FileReportAction.MIMETYPE, MimetypeMap.MIMETYPE_HTML);
        mockActionParameterValue(FileReportAction.REPORT_TYPE, "rma:destructionReport");

        // expected exception
        exception.expect(AlfrescoRuntimeException.class);

        // == when ==

        // execute action
        fileReportAction.executeImpl(getMockedAction(), actionedUponNodeRef);

        // == then ==
        verifyZeroInteractions(mockedReportService, mockedNodeService);
    }

    /**
     * given no report type set, ensure that an exception is thrown
     */
    @Test
    public void reportTypeNotSet()
    {
        // == given ==

        // set action parameter values
        mockActionParameterValue(FileReportAction.MIMETYPE, MimetypeMap.MIMETYPE_HTML);
        mockActionParameterValue(FileReportAction.DESTINATION, generateNodeRef().toString());

        // expected exception
        exception.expect(AlfrescoRuntimeException.class);

        // == when ==

        // execute action
        fileReportAction.executeImpl(getMockedAction(), actionedUponNodeRef);

        // == then ==
        verifyZeroInteractions(mockedReportService, mockedNodeService);
    }
}
