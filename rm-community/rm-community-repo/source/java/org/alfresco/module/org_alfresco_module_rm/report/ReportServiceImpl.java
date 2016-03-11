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
    private Map<QName, ReportGenerator> registry = new HashMap<QName, ReportGenerator>();

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
     * @see org.alfresco.module.org_alfresco_module_rm.report.ReportService#fileReport(org.alfresco.module.org_alfresco_module_rm.report.Report)
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
