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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
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
    /** report name */
    private static final String REPORT_NAME = "testReportName";
    
    /** actioned upon node reference */
    private NodeRef actionedUponNodeRef;
    
    /** mocked action */
    private @Mock Action mockedAction;
    
    /** mocked report */
    private @Mock Report mockedReport;
    
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
    
    /**
     * given the file report action is executed, ensure the service interactions and returned result
     * are correct.
     */
    @Test
    public void fileReport()
    {
        // == given ==
        
        // data
        NodeRef destination = generateNodeRef();
        NodeRef filedReport = generateNodeRef();
        String reportType = "rma:destructionReport";
        QName reportTypeQName = QName.createQName(RM_URI, "destructionReport");
        String mimetype = MimetypeMap.MIMETYPE_HTML;
        
        // set action parameter values
        mockActionParameterValue(FileReportAction.MIMETYPE, mimetype);
        mockActionParameterValue(FileReportAction.DESTINATION, destination.toString());
        mockActionParameterValue(FileReportAction.REPORT_TYPE, reportType);
        
        // setup service interactions
        doReturn(mockedReport).when(mockedReportService).generateReport(reportTypeQName, actionedUponNodeRef, mimetype);
        doReturn(filedReport).when(mockedReportService).fileReport(destination, mockedReport);
        doReturn(REPORT_NAME).when(mockedNodeService).getProperty(filedReport, ContentModel.PROP_NAME);
        
        // == when ==
        
        // execute action
        fileReportAction.executeImpl(mockedAction, actionedUponNodeRef);
        
        // == then ==
        
        // verify interactions
        verify(mockedReportService, times(1)).generateReport(reportTypeQName, actionedUponNodeRef, mimetype);
        verify(mockedReportService, times(1)).fileReport(destination, mockedReport);
        verify(mockedNodeService, times(1)).getProperty(filedReport, ContentModel.PROP_NAME);
        verify(mockedAction, times(1)).setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, REPORT_NAME);        
    }
    
    /**
     * given the file report action is executed with no mimetype set, ensure that a report is generated
     * with the default mimetype.
     */
    @Test
    public void fileReportDefaultMimetype()
    {
        // == given ==
        
        // data
        NodeRef destination = generateNodeRef();
        NodeRef filedReport = generateNodeRef();
        String reportType = "rma:destructionReport";
        QName reportTypeQName = QName.createQName(RM_URI, "destructionReport");
        String mimetype = MimetypeMap.MIMETYPE_HTML;
        
        // set action parameter values
        mockActionParameterValue(FileReportAction.DESTINATION, destination.toString());
        mockActionParameterValue(FileReportAction.REPORT_TYPE, reportType);
        
        // setup service interactions
        doReturn(mockedReport).when(mockedReportService).generateReport(reportTypeQName, actionedUponNodeRef, mimetype);
        doReturn(filedReport).when(mockedReportService).fileReport(destination, mockedReport);
        doReturn(REPORT_NAME).when(mockedNodeService).getProperty(filedReport, ContentModel.PROP_NAME);
        
        // == when ==
        
        // execute action
        fileReportAction.executeImpl(mockedAction, actionedUponNodeRef);
        
        // == then ==
        
        // verify interactions
        verify(mockedReportService, times(1)).generateReport(reportTypeQName, actionedUponNodeRef, mimetype);
        verify(mockedReportService, times(1)).fileReport(destination, mockedReport);
        verify(mockedNodeService, times(1)).getProperty(filedReport, ContentModel.PROP_NAME);
        verify(mockedAction, times(1)).setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, REPORT_NAME); 
        
    }
}
