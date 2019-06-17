/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
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
import org.alfresco.service.namespace.QName;

import org.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(CreateRecordAction.class);

    /** Action name */
    public static final String NAME = "create-record";

    /** Parameter names */
    public static final String PARAM_FILE_PLAN = "file-plan";
    public static final String PARAM_HIDE_RECORD = "hide-record";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_ENCODED = "encoded";


    /** Node service */
    private NodeService nodeService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Authentication util */
    private AuthenticationUtil authenticationUtil;

    /** Record service */
    private RecordService recordService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param authenticationUtil    authentication util
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
            hideRecord = hideRecordValue.booleanValue();
        }

        if (pathParameter != null && !pathParameter.isEmpty())
        {
            if (action.getParameterValue(PARAM_ENCODED) != null && (Boolean) action.getParameterValue(PARAM_ENCODED))
            {
                destinationRecordFolder = resolvePath(filePlan, decode(pathParameter));
            }
            else
            {
                destinationRecordFolder = resolvePath(filePlan, pathParameter);
            }
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
        params.add(new ParameterDefinitionImpl(PARAM_ENCODED, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_ENCODED)));
    }

    /**
     * Helper method to get the target record folder node reference from the action path parameter
     *
     * @param filePlan      The filePlan containing the path
     * @param pathParameter The path
     * @return The NodeRef of the resolved path
     */
    private NodeRef resolvePath(NodeRef filePlan, final String pathParameter)
    {
        NodeRef destinationFolder;

        if (filePlan == null)
        {
            filePlan = getDefaultFilePlan();
        }

        final String[] pathElementsArray = StringUtils.tokenizeToStringArray(pathParameter, "/", false, true);
        if ((pathElementsArray != null) && (pathElementsArray.length > 0))
        {
            destinationFolder = resolvePath(filePlan, Arrays.asList(pathElementsArray));

            // destination must be a record folder
            QName nodeType = nodeService.getType(destinationFolder);
            if (!nodeType.equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the destination path is not a record folder.");
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the destination path could not be found.");
        }
        return destinationFolder;
    }

    /**
     * Helper method to recursively get the next path element node reference from the action path parameter
     *
     * @param parent The parent of the path elements
     * @param pathElements The path elements still to be resolved
     * @return The NodeRef of the resolved path element
     */
    private NodeRef resolvePath(NodeRef parent, List<String> pathElements)
    {
        NodeRef nodeRef;
        String childName = pathElements.get(0);

        nodeRef = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, childName);

        if (nodeRef == null)
        {
            throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the destination path could not be found.");
        }
        else
        {
            QName nodeType = nodeService.getType(nodeRef);
            if (nodeType.equals(RecordsManagementModel.TYPE_HOLD_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_TRANSFER_CONTAINER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER))
            {
                throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the destination path is invalid.");
            }
        }
        if (pathElements.size() > 1)
        {
            nodeRef = resolvePath(nodeRef, pathElements.subList(1, pathElements.size()));
        }
        return nodeRef;
    }

    /**
     * Helper method to get the default RM filePlan
     *
     * @return The NodeRef of the default RM filePlan
     */
    private NodeRef getDefaultFilePlan()
    {
        NodeRef filePlan = authenticationUtil.runAsSystem(new org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork()
            {
                return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            }
        });

        // if the file plan is still null, raise an exception
        if (filePlan == null)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Unable to execute " + NAME + " action, because the fileplan path could not be determined.  Make sure at least one file plan has been created.");
                throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the fileplan path could not be determined.");
            }
        }
        return filePlan;
    }

    /**
     * Helper method to decode path string
     *
     * @param pathParameter The path string to be decoded
     * @return The decoded path string
     */
    private String decode(String pathParameter)
    {
        String decodedPathParameter;
        try
        {
            decodedPathParameter = URLDecoder.decode(pathParameter, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new AlfrescoRuntimeException("Unable to execute " + NAME + " action, because the destination path could not be decoded.");
        }
        return decodedPathParameter;
    }
}
