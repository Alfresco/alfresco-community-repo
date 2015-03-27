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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit test for file report action.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class FileReportActionUnitTest extends BaseUnitTest
{
    /** actioned upon node reference */
    private NodeRef actionedUponNodeRef;

    /** mocked action */
    private @Mock Action mockedAction;

    /** file report action */
    private @InjectMocks FileReportAction fileReportAction;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before()
    {
        super.before();

        // actioned upon node reference
        actionedUponNodeRef = generateRecord();

        // mocked action
        fileReportAction.setAuditable(false);
    }

    /**
     * Helper to mock an action parameter value
     */
    private void mockActionParameterValue(String name, String value)
    {
        doReturn(value).when(mockedAction).getParameterValue(name);
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
        fileReportAction.executeImpl(mockedAction, actionedUponNodeRef);

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
        fileReportAction.executeImpl(mockedAction, actionedUponNodeRef);

        // == then ==
        verifyZeroInteractions(mockedReportService, mockedNodeService);
    }
}
