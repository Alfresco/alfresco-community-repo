/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Report service implementation unit test.
 * 
 * @author Roy Wetherall
 */
public class ReportServiceImplTest extends BaseRMTestCase implements ReportModel
{
    private ReportService reportService;
    private RecordsManagementActionService recordsManagementActionService;
    
    @Override
    protected boolean isRecordTest()
    {
        return false;
    }
    
    @Override
    protected void initServices()
    {
        super.initServices();
        
        reportService = (ReportService)applicationContext.getBean("ReportService");
        recordsManagementActionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
    }

    public void testGetReportTypes() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Set<QName> reportTypes = reportService.getReportTypes();
                
                assertNotNull(reportTypes);
                assertFalse(reportTypes.isEmpty());
        
                for (QName reportType : reportTypes)
                {
                    System.out.println(reportType.toString());
                }
                
                return null;
            }
        });
    }
    
    public void testGenerateReport() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Report report = reportService.generateReport(TYPE_DESTRUCTION_REPORT, rmFolder);
                
                System.out.println(report.getReportName());
                System.out.println(report.getReportContent().getContentString());
                
                return null;
            }
        });   
    }
    
    public void testFileReport() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Report report = reportService.generateReport(TYPE_DESTRUCTION_REPORT, rmFolder);
                NodeRef reportNodeRef =  reportService.fileReport(filePlan, report);
                
                assertNotNull(reportNodeRef);
                assertTrue(recordService.isRecord(reportNodeRef));
                assertFalse(recordService.isFiled(reportNodeRef));
                assertEquals(TYPE_DESTRUCTION_REPORT, nodeService.getType(reportNodeRef));
                
                return null;
            }
        });   
    }
    
    public void testFileDestructionReportAction() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                recordsManagementActionService.executeRecordsManagementAction(rmFolder, DestroyAction.NAME);                
                recordsManagementActionService.executeRecordsManagementAction(rmFolder, "fileDestructionReport");                
                return null;
            }                        
        });         
    }
}
