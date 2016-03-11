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
    
    private Map<QName, Serializable> reportProperties = new HashMap<QName, Serializable>(21);
    
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
