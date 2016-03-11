package org.alfresco.module.org_alfresco_module_rm.report;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Report generator interface.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ReportGenerator
{
    /**
     * @return {@link QName} report type
     */
    QName getReportType();

    /**
     * Generate report.
     * 
     * @param reportedUponNodeRef
     * @param mimetype
     * @return
     */
    Report generateReport(NodeRef reportedUponNodeRef, String mimetype);
}
