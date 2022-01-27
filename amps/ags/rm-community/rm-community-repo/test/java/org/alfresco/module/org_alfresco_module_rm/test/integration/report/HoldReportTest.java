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

package org.alfresco.module.org_alfresco_module_rm.test.integration.report;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.GUID;

/**
 * Hold report integration tests.
 * <p>
 * Relates to:
 *  - https://issues.alfresco.com/jira/browse/RM-1211
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldReportTest extends BaseRMTestCase implements ReportModel
{
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }
    
    /**
     * ensure that 'rmr:holdReport' is in the list of those available
     */
    public void testHoldReportTypeAvailable()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private Set<QName> reportTypes;
            
            public void when()
            {
                reportTypes = reportService.getReportTypes();
            }            
            
            public void then()
            {
                assertNotNull(reportTypes);
                assertTrue(reportTypes.contains(TYPE_HOLD_REPORT));
            }
        });        
    }
    
    /**
     * given that the reported upon node is not a hold, ensure that an error is raised when 
     * the report is generated.
     */
    public void testReportedUponNodeIsNotAHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class)
        {
            private NodeRef reportedUponNodeRef;
            
            public void given()
            {
                reportedUponNodeRef = recordFolderService.createRecordFolder(rmContainer, GUID.generate());
            }
            
            public void when()
            {
                reportService.generateReport(TYPE_HOLD_REPORT, reportedUponNodeRef);
            }   
            
            public void after()
            {
                // remove created folder
                nodeService.deleteNode(reportedUponNodeRef);
            }
        });        
    }
    
    /**
     * Given a hold that contains items, ensure the report is generated as expected
     */
    public void testGenerateHoldReport()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private static final String HOLD_NAME = "holdName";
            private static final String HOLD_REASON = "holdReason";
            private static final String HOLD_DESCRIPTION = "holdDescription";
            private static final String FOLDER1_NAME = "folder1Name";
            
            private NodeRef hold;
            private NodeRef folder1;
            private Report report;
            
            public void given()
            {
                // crate a hold
                hold = holdService.createHold(filePlan, HOLD_NAME, HOLD_REASON, HOLD_DESCRIPTION);

                // add some items to the hold
                folder1 = recordFolderService.createRecordFolder(rmContainer, FOLDER1_NAME);
                holdService.addToHold(hold, folder1);
                holdService.addToHold(hold, recordOne);
            }
            
            public void when()
            {
                report = reportService.generateReport(TYPE_HOLD_REPORT, hold, MimetypeMap.MIMETYPE_HTML);
            }  
            
            public void then()
            {
                assertNotNull(report);
                assertEquals(TYPE_HOLD_REPORT, report.getReportType());
                assertTrue(report.getReportProperties().isEmpty());
                
                // check the name has been generated correctly
                assertNotNull(report.getReportName());
                assertTrue(report.getReportName().contains("Hold Report"));
                assertTrue(report.getReportName().contains(HOLD_NAME));
                assertTrue(report.getReportName().contains(".html"));
                
                // check the content reader
                ContentReader reader = report.getReportContent();
                assertNotNull(reader);
                assertEquals(MimetypeMap.MIMETYPE_HTML, reader.getMimetype());
                
                // check the content
                String reportContent = reader.getContentString();
                assertNotNull(reportContent);
                assertTrue(reportContent.contains(HOLD_NAME));
                assertTrue(reportContent.contains(HOLD_REASON));
                assertTrue(reportContent.contains(HOLD_DESCRIPTION));
                assertTrue(reportContent.contains(FOLDER1_NAME));
                assertTrue(reportContent.contains("one"));
            }
            
            public void after()
            {
                holdService.deleteHold(hold);
            }
        });
    }
}
