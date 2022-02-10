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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionResult;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileReportAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.StringUtils;

/**
 * Report service implementation unit test.
 *
 * @author Roy Wetherall
 */
public class ReportServiceImplTest extends BaseRMTestCase implements ReportModel
{
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
                // Destruction Report
                Report destructionReport = generateDestructionReport();
                System.out.println(destructionReport.getReportName());
                System.out.println(destructionReport.getReportContent().getContentString());

                // Transfer Report
                Report transferReport = reportService.generateReport(TYPE_TRANSFER_REPORT, getTransferObject(), MimetypeMap.MIMETYPE_HTML);
                System.out.println(transferReport.getReportName());
                System.out.println(transferReport.getReportContent().getContentString());

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
                // Destruction Report
                NodeRef destructionReportNodeRef = fileDestructionReport();
                assertNotNull(destructionReportNodeRef);
                assertTrue(recordService.isRecord(destructionReportNodeRef));
                assertFalse(recordService.isFiled(destructionReportNodeRef));
                assertEquals(TYPE_DESTRUCTION_REPORT, nodeService.getType(destructionReportNodeRef));

                // Transfer Report
                NodeRef transferReportNodeRef = fileTransferReport();
                assertNotNull(transferReportNodeRef);
                assertTrue(recordService.isRecord(transferReportNodeRef));
                assertFalse(recordService.isFiled(transferReportNodeRef));
                assertEquals(TYPE_TRANSFER_REPORT, nodeService.getType(transferReportNodeRef));

                return null;
            }
        });
    }

    /**
     * Helper method to generate a destruction report
     *
     * @return Destruction report
     */
    private Report generateDestructionReport()
    {
        return reportService.generateReport(TYPE_DESTRUCTION_REPORT, rmFolder);
    }
    
    /**
     * Helper method to file a destruction report
     *
     * @return Node reference of the destruction report
     */
    private NodeRef fileDestructionReport()
    {
        Report destructionReport = generateDestructionReport();
        return reportService.fileReport(filePlan, destructionReport);
    }

    /**
     * Helper method to file a transfer report
     *
     * @return Node reference of the transfer report
     */
    private NodeRef fileTransferReport()
    {
        Report transferReport = reportService.generateReport(TYPE_TRANSFER_REPORT, getTransferObject(), MimetypeMap.MIMETYPE_HTML);
        return reportService.fileReport(filePlan, transferReport);
    }

    public void testFileDestructionReportAction() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                rmActionService.executeRecordsManagementAction(rmFolder, CompleteEventAction.NAME, params);
                
                rmActionService.executeRecordsManagementAction(rmFolder, CutOffAction.NAME);
                rmActionService.executeRecordsManagementAction(rmFolder, DestroyAction.NAME);
                
                Map<String, Serializable> fileReportParams = new HashMap<>(2);
                fileReportParams.put(FileReportAction.REPORT_TYPE, "rmr:destructionReport");
                fileReportParams.put(FileReportAction.DESTINATION, filePlan.toString());
                rmActionService.executeRecordsManagementAction(rmFolder, FileReportAction.NAME, fileReportParams);
                return null;
            }
        });
    }

    public void testFileTransferReportAction() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                // Create transfer report for the transfer object
                Map<String, Serializable> params = new HashMap<>(2);
                params.put(FileReportAction.REPORT_TYPE, "rmr:transferReport");
                params.put(FileReportAction.DESTINATION, filePlan.toString());
                RecordsManagementActionResult transferReportAction = rmActionService.executeRecordsManagementAction(getTransferObject(), FileReportAction.NAME, params);
                // Check transfer report result
                String transferReportName = (String) transferReportAction.getValue();
                assertFalse(StringUtils.isBlank(transferReportName));
                return null;
            }
        });
    }

    /**
     * Helper method for creating a transfer object
     *
     * @return Node reference of the transfer object
     */
    private NodeRef getTransferObject()
    {
        NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
        utils.createDispositionSchedule(
                            recordCategory,
                            CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS,
                            CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY,
                            false,  // record level
                            true,   // set the default actions
                            true);  // extended disposition schedule

        NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());

        // Set the record folder identifier
        String identifier = identifierService.generateIdentifier(TYPE_RECORD_FOLDER, recordCategory);
        nodeService.setProperty(recordFolder, PROP_IDENTIFIER, identifier);

        // Complete event
        Map<String, Serializable> params = new HashMap<>(1);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
        rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);

        // Cut off folder
        rmActionService.executeRecordsManagementAction(recordFolder, CutOffAction.NAME);

        // Transfer folder
        RecordsManagementActionResult transferAction = rmActionService.executeRecordsManagementAction(recordFolder, TransferAction.NAME);
        NodeRef transferObject = (NodeRef) transferAction.getValue();
        assertTrue(transferObject != null);

        return transferObject;
    }
}
