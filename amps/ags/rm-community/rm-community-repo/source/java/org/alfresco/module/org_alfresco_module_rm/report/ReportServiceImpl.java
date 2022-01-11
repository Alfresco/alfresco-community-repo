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

package org.alfresco.module.org_alfresco_module_rm.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Report service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ReportServiceImpl extends ServiceBaseImpl
                               implements ReportService
{
    /** record service */
    protected RecordService recordService;

    /** report generator registry */
    private Map<QName, ReportGenerator> registry = new HashMap<>();

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#registerReportGenerator(org.alfresco.module.org_alfresco_module_rm.report.ReportGenerator)
     */
    @Override
    public void registerReportGenerator(ReportGenerator reportGenerator)
    {
        ParameterCheck.mandatory("reportGenerator", reportGenerator);
        registry.put(reportGenerator.getReportType(), reportGenerator);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#getReportTypes()
     */
    @Override
    public Set<QName> getReportTypes()
    {
        return registry.keySet();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#generateReport(QName, NodeRef)
     */
    @Override
    public Report generateReport(QName reportType, NodeRef reportedUponNodeRef)
    {
        ParameterCheck.mandatory("reportType", reportType);
        ParameterCheck.mandatory("reportedUponNodeRef", reportedUponNodeRef);

        return generateReport(reportType, reportedUponNodeRef, MimetypeMap.MIMETYPE_HTML);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#generateReport(QName, NodeRef, String)
     */
    @Override
    public Report generateReport(QName reportType, NodeRef reportedUponNodeRef, String mimetype)
    {
        ParameterCheck.mandatory("reportType", reportType);
        ParameterCheck.mandatory("reportedUponNodeRef", reportedUponNodeRef);
        ParameterCheck.mandatoryString("mimetype", mimetype);

        // get the generator
        ReportGenerator generator = registry.get(reportType);

        // error is generator not found in registry
        if (generator == null)
        {
            throw new AlfrescoRuntimeException("Unable to generate report, because report type " + reportType.toString() + " does not correspond to a registered report type.");
        }

        // generate the report
        return generator.generateReport(reportedUponNodeRef, mimetype);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#fileReport(NodeRef, org.alfresco.module.org_alfresco_module_rm.report.Report)
     */
    @Override
    public NodeRef fileReport(NodeRef nodeRef, Report report)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("report", report);

        return recordService.createRecordFromContent(nodeRef,
                                                     report.getReportName(),
                                                     report.getReportType(),
                                                     report.getReportProperties(),
                                                     report.getReportContent());
    }
}
