 
package org.alfresco.module.org_alfresco_module_rm.report;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Report service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ReportService
{
    /**
     * Register a report generator with the report service.
     *
     * @param reportGenerator   report generator
     */
    void registerReportGenerator(ReportGenerator reportGenerator);

    /**
     * Get a list of the available report types.
     *
     * @return {@link Set}<{@link QName}> list of the available report types
     */
    Set<QName> getReportTypes();

    /**
     * Generate a report of the given type and reported upon node reference.
     *
     * @param reportType            report type
     * @param reportedUponNodeRef   reported upon node reference
     * @return {@link Report}       generated report
     */
    Report generateReport(QName reportType, NodeRef reportedUponNodeRef);

    /**
     * Generate a report for a specified mimetype.
     * 
     * @see #generateReport(QName, NodeRef)
     *
     * @param reportType            report type
     * @param reportedUponNodeRef   report upon node reference
     * @param mimetype              report mimetype
     * @return {@link Report}       generated report
     */
    Report generateReport(QName reportType, NodeRef reportedUponNodeRef, String mimetype);

    /**
     * File report in the given destination. If the given node reference is a file plan node
     * reference the report will be filed in the unfiled records container.
     *
     * @param nodeRef   node reference
     * @param report    report
     * @return NodeRef  node reference of the filed report
     */
    NodeRef fileReport(NodeRef nodeRef, Report report);
}
