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
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a new record from an existing content object.
 * 
 * Note:  This is a 'normal' dm action, rather than a records management action.
 * 
 * @author Roy Wetherall
 */
public class CreateRecordAction extends ActionExecuterAbstractBase
                                implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(CreateRecordAction.class);
    
    /** Action name */
    public static final String NAME = "create-record";
    
    /** Parameter names */
    public static final String PARAM_FILE_PLAN = "file-plan";
    
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Record service */
    private RecordService recordService;
    
    /** Node service */
    private NodeService nodeService;
    
    /**
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
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
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {       
        if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD) == true)
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
        else 
        {
            // run record creation as system
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {   
                    NodeRef filePlan = (NodeRef)action.getParameterValue(PARAM_FILE_PLAN);
                    if (filePlan == null)
                    {
                        List<NodeRef> filePlans = recordsManagementService.getFilePlans();
                        if (filePlans.size() == 1)
                        {
                            filePlan = filePlans.get(0);
                        }
                        else
                        {
                            if (logger.isDebugEnabled() == true)
                            {
                                logger.debug("Can not create record, because the default file plan can not be determined.");
                            }
                            throw new AlfrescoRuntimeException("Can not create record, because the default file plan can not be determined.");
                        } 
                    }
                    else
                    {
                        // verify that the provided file plan is actually a file plan
                        if (recordsManagementService.isFilePlan(filePlan) == false)
                        {
                            if (logger.isDebugEnabled() == true)
                            {
                                logger.debug("Can not create record, because the provided file plan node reference is not a file plan.");
                            }
                            throw new AlfrescoRuntimeException("Can not create record, because the provided file plan node reference is not a file plan.");
                        }
                    }
            
                    // create record from existing document
                    recordService.createRecordFromDocument(filePlan, actionedUponNodeRef);
                    
                    return null;
                }
            });                                       
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        // Optional parameter used to specify the file plan
        params.add(new ParameterDefinitionImpl(PARAM_FILE_PLAN, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_FILE_PLAN)));
    }
   
}
