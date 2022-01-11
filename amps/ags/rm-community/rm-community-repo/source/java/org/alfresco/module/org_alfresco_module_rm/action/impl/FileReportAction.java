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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.report.Report;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * File report generic action.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class FileReportAction extends RMActionExecuterAbstractBase implements ReportModel
{
    /** action name */
    public static final String NAME = "fileReport";

    /** Constants for the parameters passed from the UI */
    public static final String REPORT_TYPE = "reportType";

    public static final String DESTINATION = "destination";

    public static final String MIMETYPE = "mimetype";

    /** I18N */
    private static final String MSG_PARAM_NOT_SUPPLIED = "rm.action.parameter-not-supplied";

    /** Report service */
    private ReportService reportService;

    /** Capability service */
    private CapabilityService capabilityService;

    /**
     * @return Report service
     */
    protected ReportService getReportService()
    {
        return this.reportService;
    }

    /**
     * @return Capability service
     */
    protected CapabilityService getCapabilityService()
    {
        return this.capabilityService;
    }

    /**
     * @param reportService report service
     */
    public void setReportService(ReportService reportService)
    {
        this.reportService = reportService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // get the mimetype of the report
        String mimetype = (String) action.getParameterValue(MIMETYPE);
        if (mimetype == null || mimetype.isEmpty())
        {
            mimetype = MimetypeMap.MIMETYPE_HTML;
        }

        // get the report type
        QName reportType = getReportType(action);

        // get the destination
        final NodeRef destination = getDestination(action);

        // Check the filing permission only capability for the destination
        checkFilingPermissionOnlyCapability(destination);

        // generate the report
        final Report report = getReportService().generateReport(reportType, actionedUponNodeRef, mimetype);

        // file the report as system
        NodeRef filedReport = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork()
            {
                return getReportService().fileReport(destination, report);
            }
        });

        // return the report name
        String filedReportName = (String) getNodeService().getProperty(filedReport, ContentModel.PROP_NAME);
        action.setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, filedReportName);
    }

    /**
     * Checks if the destination is frozen, closed, cut off or not. In case if it is an exception will be thrown.
     *
     * @param nodeRef The destination node reference for which the capability should be checked
     */
    private void checkFilingPermissionOnlyCapability(NodeRef nodeRef)
    {
        if (AccessStatus.DENIED.equals(capabilityService.getCapability("FillingPermissionOnly").hasPermission(nodeRef)))
        {
            throw new AlfrescoRuntimeException("You don't have filing permission on the destination or the destination is either frozen, closed or cut off!");
        }
    }

    /**
     * Retrieves the value of the given parameter. If the parameter has not been
     * passed from the UI an error will be thrown
     *
     * @param action The action
     * @param parameter The parameter for which the value should be retrieved
     * @return The value of the given parameter
     */
    private String getParameterValue(Action action, String parameter)
    {
        String paramValue = (String) action.getParameterValue(parameter);
        if (StringUtils.isBlank(paramValue)) { throw new AlfrescoRuntimeException(I18NUtil.getMessage(
                MSG_PARAM_NOT_SUPPLIED, parameter)); }
        return paramValue;
    }

    /**
     * Helper method for getting the destination.
     *
     * @param action The action
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
     * @param action The action
     * @return The report type
     */
    private QName getReportType(Action action)
    {
        String reportType = getParameterValue(action, REPORT_TYPE);
        return QName.createQName(reportType, getNamespaceService());
    }
}
