/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.report.generator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportGenerator;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class BaseReportGenerator implements ReportGenerator
{
    protected ReportService reportService;
 
    protected NamespaceService namespaceService;
    
    protected String reportTypeName;
    protected QName reportType;
    
    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setReportTypeName(String reportTypeName)
    {
        this.reportTypeName = reportTypeName;
    }
    
    @Override
    public QName getReportType()
    {
        return reportType;
    }
    
    public void init()
    {
        // convert type name to QName
        reportType = QName.createQName(reportTypeName, namespaceService);
        
        // register report generator
        reportService.registerReportGenerator(this);
    }
    
    @Override
    public Report generateReport(NodeRef reportedUponNodeRef, String mimetype)
    {
        String reportName = generateReportName(reportedUponNodeRef);
        Map<QName, Serializable> reportProperties = generateReportProperties(reportedUponNodeRef);
        ContentReader contentReader = generateReportContent(reportedUponNodeRef, mimetype);
        return new ReportInfo(reportType, reportName, reportProperties, contentReader);        
    }
    
    protected abstract String generateReportName(NodeRef reportedUponNodeRef);
    
    
    protected Map<QName, Serializable> generateReportProperties(NodeRef reportedUponNodeRef)
    {
        // default implementation
        return new HashMap<QName, Serializable>(0);
    }
    
    protected abstract ContentReader generateReportContent(NodeRef reportedUponNodeRef, String mimetype);

}
