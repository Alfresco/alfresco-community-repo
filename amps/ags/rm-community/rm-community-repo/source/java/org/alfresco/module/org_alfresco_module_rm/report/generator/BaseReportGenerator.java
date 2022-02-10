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

package org.alfresco.module.org_alfresco_module_rm.report.generator;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportGenerator;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Base report generator.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class BaseReportGenerator implements ReportGenerator
{
    /** report service */
    protected ReportService reportService;

    /** namespace service */
    protected NamespaceService namespaceService;

    /** report type qualified name */
    protected QName reportType;

    /**
     * @param reportService report service
     */
    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param reportType    report type
     */
    public void setReportType(QName reportType)
    {
        this.reportType = reportType;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportGenerator#getReportType()
     */
    @Override
    public QName getReportType()
    {
        return reportType;
    }

    /**
     * Init method
     */
    public void init()
    {
        // ensure required values have been set
        ParameterCheck.mandatory("reportType", reportType);

        // register report generator
        reportService.registerReportGenerator(this);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportGenerator#generateReport(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public Report generateReport(NodeRef reportedUponNodeRef, String mimetype)
    {
        ParameterCheck.mandatory("reportedUponNodeRef", reportedUponNodeRef);
        ParameterCheck.mandatoryString("mimetype", mimetype);

        // check the applicability of the report generator for the given reported upon node
        checkReportApplicability(reportedUponNodeRef);

        // generate the report name
        String reportName = generateReportName(reportedUponNodeRef, mimetype);

        // generate the report meta-data
        Map<QName, Serializable> reportProperties = generateReportMetadata(reportedUponNodeRef);

        // generate the report content
        ContentReader contentReader = generateReportContent(reportedUponNodeRef, mimetype, generateReportTemplateContext(reportedUponNodeRef));

        // return the report information object
        return new ReportInfo(reportType, reportName, reportProperties, contentReader);
    }

    /**
     * Checks whether the report generator is applicable given the reported upon node reference.
     * <p>
     * Throws AlfrescoRuntimeException if applicability fails, with reason.
     *
     * @param  reportedUponNodeRef          reported upon node reference
     */
    protected abstract void checkReportApplicability(NodeRef reportedUponNodeRef);

    /**
     * Generate the report name
     */
    protected abstract String generateReportName(NodeRef reportedUponNodeRef, String mimetype);

    /**
     * Generate the report template context.
     */
    protected abstract Map<String, Serializable> generateReportTemplateContext(NodeRef reportedUponNodeRef);

    /**
     * Generate report meta-data
     */
    protected abstract Map<QName, Serializable> generateReportMetadata(NodeRef reportedUponNodeRef);

    /**
     * Generate report content
     */
    protected abstract ContentReader generateReportContent(NodeRef reportedUponNodeRef, String mimetype, Map<String, Serializable> properties);
}
