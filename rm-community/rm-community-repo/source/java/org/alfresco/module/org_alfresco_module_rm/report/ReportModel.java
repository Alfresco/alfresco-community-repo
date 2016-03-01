 
package org.alfresco.module.org_alfresco_module_rm.report;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management report qualified names
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ReportModel
{
    /** Namespace details */
    String RMR_URI = "http://www.alfresco.org/model/recordsmanagementreport/1.0";
    String RMR_PREFIX = "rmr";

    /** base report type */
    QName TYPE_REPORT = QName.createQName(RMR_URI, "report");
    
    /** destruction report type */
    QName TYPE_DESTRUCTION_REPORT = QName.createQName(RMR_URI, "destructionReport");
    
    /** transfer report type */
    QName TYPE_TRANSFER_REPORT = QName.createQName(RMR_URI, "transferReport");
    
    /** 
     * hold report type
     * @since 2.2
     */
    QName TYPE_HOLD_REPORT = QName.createQName(RMR_URI, "holdReport");
}
