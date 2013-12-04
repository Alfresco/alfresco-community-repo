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
package org.alfresco.module.org_alfresco_module_rm.report.action;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Base class for the report action classes
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public abstract class BaseReportAction extends RMActionExecuterAbstractBase implements ReportModel
{
    /** Constants for the parameters passed from the UI */
    public static final String REPORT_TYPE = "reportType";
    public static final String DESTINATION = "destination";

    /** I18N */
    private static final String MSG_PARAM_NOT_SUPPLIED = "rm.action.parameter-not-supplied";

    /** report service */
    protected ReportService reportService;

    /**
     * @param reportService report service
     */
    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
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

        QName reportType = getReportType(action);
        Report report = reportService.generateReport(reportType, actionedUponNodeRef, MimetypeMap.MIMETYPE_HTML, addProperties(actionedUponNodeRef));

        NodeRef destination = getDestination(action);
        NodeRef filedReport = reportService.fileReport(destination, report);

        String filedReportName = (String) nodeService.getProperty(filedReport, ContentModel.PROP_NAME);
        action.setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, filedReportName);
    }

    /**
     * Gives other action classes to pass additional properties for the template model
     *
     * @return Properties which are passed to the template model
     */
    protected abstract Map<String, Serializable> addProperties(NodeRef nodeRef);

    /**
     * Retrieves the value of the given parameter. If the parameter has not been passed from the UI an error will be thrown
     *
     * @param action        The action
     * @param parameter     The parameter for which the value should be retrieved
     * @return The value of the given parameter
     */
    private String getParameterValue(Action action, String parameter)
    {
        String paramValue = (String) action.getParameterValue(parameter);
        if (StringUtils.isBlank(paramValue) == true)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PARAM_NOT_SUPPLIED, parameter));
        }
        return paramValue;
    }

    /**
     * Helper method for getting the destination.
     *
     * @param action    The action
     * @return The file plan node reference
     */
    private NodeRef getDestination(Action action)
    {
        String destination = getParameterValue(action, DESTINATION);
        return new NodeRef(destination);
    }

    /**
     * Helper method for getting the report type.
     *
     * @param action    The action
     * @return The report type
     */
    private QName getReportType(Action action)
    {
        String reportType = getParameterValue(action, REPORT_TYPE);
        return QName.createQName(reportType, namespaceService);
    }
}
