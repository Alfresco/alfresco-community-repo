/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.report.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * File Report Action
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FileReportAction extends RMActionExecuterAbstractBase
                              implements ReportModel
{
    /** report service */
    protected ReportService reportService;
    
    /** file plan service */
    protected FilePlanService filePlanService;
    
    /** report type string value */
    private String reportType;
    
    /**
     * @param reportService report service
     */
    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
    }
     
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * @param reportType    report type string value
     */
    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }
    
    /**
     * @return  QName   report type
     */
    protected QName getReportType()
    {
        ParameterCheck.mandatory("this.reportType", reportType);        
        return QName.createQName(reportType, namespaceService);
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // TODO check that the actionedUponNodeRef is in a state to generate a destruction report
        //      ie: is it eligable for destruction .. use fileDestructionReport capability!
       
        // TODO allow the mimetype of the report to be specified as a parameter
       
        NodeRef filePlan = filePlanService.getFilePlan(actionedUponNodeRef);
        if (filePlan == null)
        {
            throw new AlfrescoRuntimeException("Unable to file destruction report, because file plan could not be resolved.");
        }
        
        Report report = reportService.generateReport(getReportType(), actionedUponNodeRef);
        reportService.fileReport(filePlan, report);
        
    }    
}