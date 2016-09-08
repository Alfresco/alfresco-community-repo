/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
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
    private static Log logger = LogFactory.getLog(CreateRecordAction.class);

    /** Action name */
    public static final String NAME = "create-record";

    /** Parameter names */
    public static final String PARAM_FILE_PLAN = "file-plan";
    public static final String PARAM_HIDE_RECORD = "hide-record";

    /** Sync Model URI */
    static final String SYNC_MODEL_1_0_URI = "http://www.alfresco.org/model/sync/1.0";
    /** Synced aspect */
    static final QName ASPECT_SYNCED = QName.createQName(SYNC_MODEL_1_0_URI, "synced");

    /** Record service */
    private RecordService recordService;

    /** Node service */
    private NodeService nodeService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** File plan authentication service */
    private FilePlanAuthenticationService filePlanAuthenticationService;

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

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
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param filePlanAuthenticationService file plan authentication service
     */
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {

        if (nodeService.exists(actionedUponNodeRef) == false)
        {
            // do not create record if the actioned upon node does not exist!
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can not create record, because " + actionedUponNodeRef.toString() + " does not exist.");
            }
        }
        else if (dictionaryService.isSubClass(nodeService.getType(actionedUponNodeRef), ContentModel.TYPE_CONTENT) == false)
        {
            // TODO eventually we should support other types .. either as record folders or as composite records
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can not create record, because " + actionedUponNodeRef.toString() + " is not a supported type.");
            }
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD) == true)
        {
            // Do not create record if the actioned upon node is already a record!
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can not create record, because " + actionedUponNodeRef.toString() + " is already a record.");
            }
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
        {
            // We can not create records from working copies
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can node create record, because " + actionedUponNodeRef.toString() + " is a working copy.");
            }

        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS) == true)
        {
            // can not create a record from a previously rejected one
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can not create record, because " + actionedUponNodeRef.toString() + " has previously been rejected.");
            }
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_SYNCED) == true)
        {
            // can't declare the record if the node is sync'ed
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("Can't declare as record, because " + actionedUponNodeRef.toString() + " is synched content.");
            }
        }
        else
        {
            NodeRef filePlan = (NodeRef)action.getParameterValue(PARAM_FILE_PLAN);
            if (filePlan == null)
            {
                // TODO .. eventually make the file plan parameter required
                filePlan = filePlanAuthenticationService.runAsRmAdmin(new RunAsWork<NodeRef>()
                {
                    @Override
                    public NodeRef doWork() throws Exception
                    {
                        return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
                    }
                });

                // if the file plan is still null, raise an exception
                if (filePlan == null)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Can not create record, because the default file plan can not be determined.  Make sure at least one file plan has been created.");
                    }
                    throw new AlfrescoRuntimeException("Can not create record, because the default file plan can not be determined.");
                }
            }
            else
            {
                // verify that the provided file plan is actually a file plan
                if (filePlanService.isFilePlan(filePlan) == false)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Can not create record, because the provided file plan node reference is not a file plan.");
                    }
                    throw new AlfrescoRuntimeException("Can not create record, because the provided file plan node reference is not a file plan.");
                }
            }

            // indicate whether the record should be hidden or not (default not)
            boolean hideRecord = false;
            Boolean hideRecordValue = ((Boolean)action.getParameterValue(PARAM_HIDE_RECORD));
            if (hideRecordValue != null)
            {
                hideRecord = hideRecordValue.booleanValue();
            }

            // create record from existing document
            recordService.createRecord(filePlan, actionedUponNodeRef, !hideRecord);
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
        params.add(new ParameterDefinitionImpl(PARAM_HIDE_RECORD, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_HIDE_RECORD)));
    }

}
