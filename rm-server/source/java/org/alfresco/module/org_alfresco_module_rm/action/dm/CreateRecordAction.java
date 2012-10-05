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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

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
    /** Action name */
    public static final String NAME = "create-record";
    
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Record service */
    private RecordService recordService;
    
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
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
    {
        // skip everything if the actioned upon node reference is already a record
        if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD) == false)
        {
            // TODO we should use the file plan passed as a parameter
            // grab the file plan
            List<NodeRef> filePlans = recordsManagementService.getFilePlans();
            if (filePlans.size() == 1)
            {
                // TODO parameterise the action with the file plan
                final NodeRef filePlan = filePlans.get(0);
    
                // run record creation as system
                AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork() throws Exception
                    {
                        // create record from existing document
                        recordService.createRecordFromDocument(filePlan, actionedUponNodeRef);
                        
                        return null;
                    }
                });            
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to find file plan.");
            }       
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        // TODO eventually we will need to pass in the file plan as a parameter
        // TODO .. or the RM site
    }
   
}
