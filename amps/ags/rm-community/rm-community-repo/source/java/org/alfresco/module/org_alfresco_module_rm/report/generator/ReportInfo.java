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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Report implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
/*package*/ class ReportInfo implements Report
{
    /** report type */
    private QName reportType;
    
    private String reportName;
    
    private Map<QName, Serializable> reportProperties = new HashMap<>(21);
    
    /** content reader */
    private ContentReader reportContent;
    
    /**
     * Default constructor.
     * 
     * @param reportType        report type
     * @param reportName        report name
     * @param reportProperties  report properties
     * @param reportContent     report content reader
     */
    public ReportInfo(QName reportType, String reportName, Map<QName, Serializable> reportProperties, ContentReader reportContent)
    {
        ParameterCheck.mandatory("reportType", reportType);
        ParameterCheck.mandatory("reportName", reportName);
        ParameterCheck.mandatory("reportContent", reportContent);
        
        this.reportType = reportType;
        this.reportName = reportName;
        this.reportProperties = reportProperties;
        this.reportContent = reportContent;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.Report#getReportType()
     */
    public QName getReportType()
    {
        return reportType;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.Report#getReportName()
     */
    @Override
    public String getReportName()
    {
        return reportName;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.Report#getReportProperties()
     */
    @Override
    public Map<QName, Serializable> getReportProperties()
    {
        return reportProperties;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.Report#getReportContent()
     */
    @Override
    public ContentReader getReportContent()
    {
        return reportContent;
    }
   
}
