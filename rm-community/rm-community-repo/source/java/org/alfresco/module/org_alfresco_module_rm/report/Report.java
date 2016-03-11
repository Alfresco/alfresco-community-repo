package org.alfresco.module.org_alfresco_module_rm.report;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * Report interface.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface Report
{
    /**
     * @return  {@link QName} report type
     */
    QName getReportType();
    
    /**
     * @return  {@link String}  report name
     */
    String getReportName();
    
    /**
     * @return  {@link Map}<{@link QName},{@link Serializable}>  report properties
     */
    Map<QName, Serializable> getReportProperties();
    
    /**
     * @return {@link ContentReader}  content reader to report content
     */
    ContentReader getReportContent();

}
