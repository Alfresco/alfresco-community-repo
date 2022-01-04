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

package org.alfresco.module.org_alfresco_module_rm.action.dm;

import static org.alfresco.module.org_alfresco_module_rm.action.dm.RecordActionUtils.resolvePath;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.action.dm.RecordActionUtils.Services;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Creates a new record from an existing content object.
 *
 * Note:  This is a 'normal' dm action, rather than a records management action.
 *
 * @author Roy Wetherall
 */
public class CreateRecordAction extends AuditableActionExecuterAbstractBase
                                implements RecordsManagementModel
{
    /** Action name */
    public static final String NAME = "create-record";

    /** Parameter names */
    public static final String PARAM_FILE_PLAN = "file-plan";
    public static final String PARAM_HIDE_RECORD = "hide-record";
    public static final String PARAM_PATH = "path";

    /**
     * Node service
     */
    private NodeService nodeService;

    /**
     * File plan service
     */
    private FilePlanService filePlanService;

    /**
     * Authentication util
     */
    private AuthenticationUtil authenticationUtil;

    /** Record service */
    private RecordService recordService;

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param authenticationUtil authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }


    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        NodeRef filePlan = (NodeRef) action.getParameterValue(PARAM_FILE_PLAN);

        // resolve destination record folder if path supplied
        NodeRef destinationRecordFolder = null;
        String pathParameter = (String) action.getParameterValue(PARAM_PATH);

        // indicate whether the record should be hidden or not (default not)
        boolean hideRecord = false;
        Boolean hideRecordValue = ((Boolean) action.getParameterValue(PARAM_HIDE_RECORD));
        if (hideRecordValue != null)
        {
            hideRecord = hideRecordValue;
        }

        if (pathParameter != null && !pathParameter.isEmpty())
        {
            RecordActionUtils.Services services = new Services(nodeService, filePlanService, authenticationUtil);
            destinationRecordFolder = resolvePath(services, filePlan, pathParameter, NAME);
        }

        synchronized (this)
        {
            // create record from existing document
            recordService.createRecord(filePlan, actionedUponNodeRef, destinationRecordFolder, !hideRecord);

            if (destinationRecordFolder != null)
            {
                recordService.file(actionedUponNodeRef);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        // NOTE:  commented out for now so that it doesn't appear in the UI ... enable later when multi-file plan support is added
        //params.add(new ParameterDefinitionImpl(PARAM_FILE_PLAN, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_FILE_PLAN)));
        params.add(new ParameterDefinitionImpl(PARAM_PATH, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PATH)));
        params.add(new ParameterDefinitionImpl(PARAM_HIDE_RECORD, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_HIDE_RECORD)));
    }
}
