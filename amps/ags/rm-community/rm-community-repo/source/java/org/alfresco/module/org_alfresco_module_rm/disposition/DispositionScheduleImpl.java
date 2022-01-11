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

package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Disposition instructions implementation
 * 
 * @author Roy Wetherall
 */
public class DispositionScheduleImpl implements DispositionSchedule,
                                                RecordsManagementModel
{
    private NodeService nodeService;
    private RecordsManagementServiceRegistry services;
    private NodeRef dispositionDefinitionNodeRef;
    /** authentication helper */
    private AuthenticationUtil authenticationUtil;


    private List<DispositionActionDefinition> actions;
    private Map<String, DispositionActionDefinition> actionsById;
    
    //If name is not the same as node-uuid, then action will be stored here too
    //Fix for ALF-2588
    private Map<String, DispositionActionDefinition> actionsByName;
    
    /** Map of disposition definitions by disposition action name */
    private Map<String, DispositionActionDefinition> actionsByDispositionActionName;

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
    
    public DispositionScheduleImpl(RecordsManagementServiceRegistry services, NodeService nodeService,  NodeRef nodeRef)
    {
        // TODO check that we have a disposition definition node reference
        
        this.dispositionDefinitionNodeRef = nodeRef;
        this.nodeService = nodeService;
        this.services = services;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return this.dispositionDefinitionNodeRef;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#getDispositionAuthority()
     */
    public String getDispositionAuthority()
    {
        return (String)this.nodeService.getProperty(this.dispositionDefinitionNodeRef, PROP_DISPOSITION_AUTHORITY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#getDispositionInstructions()
     */
    public String getDispositionInstructions()
    {
        return (String)this.nodeService.getProperty(this.dispositionDefinitionNodeRef, PROP_DISPOSITION_INSTRUCTIONS);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#isRecordLevelDisposition()
     */
    public boolean isRecordLevelDisposition()
    {
        return authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                Boolean value = (Boolean)nodeService.getProperty(dispositionDefinitionNodeRef, PROP_RECORD_LEVEL_DISPOSITION);
                if (value != null)
                {
                    return value;
                }
                return false;
            }
        });
    }

    /**
     * Get disposition action definition
     * 
     * @param   id                              action definition identifier
     * @return  DispositionActionDefinition     disposition action definition
     */
    public DispositionActionDefinition getDispositionActionDefinition(String id)
    {
        if (this.actions == null)
        {
            authenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    getDispositionActionsImpl();
                    return null;
                }
            });
        }

        DispositionActionDefinition actionDef = this.actionsById.get(id);
        if (actionDef == null)
        {
            actionDef = this.actionsByName.get(id);
        }
        return actionDef;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#getDispositionActionDefinitionByName(java.lang.String)
     */
    @Override
    public DispositionActionDefinition getDispositionActionDefinitionByName(String name)
    {
        if (this.actionsByDispositionActionName == null)
        {
            getDispositionActionsImpl();
        }
        return actionsByDispositionActionName.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule#getDispositionActionDefinitions()
     */
    public List<DispositionActionDefinition> getDispositionActionDefinitions()
    {
        if (this.actions == null)
        {
            getDispositionActionsImpl();
        }
        
        return this.actions;
    }
    
    /**
     * Get the disposition actions into the local cache
     */
    private void getDispositionActionsImpl()
    {
        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(
                                                      this.dispositionDefinitionNodeRef, 
                                                      ASSOC_DISPOSITION_ACTION_DEFINITIONS, 
                                                      RegexQNamePattern.MATCH_ALL);
        this.actions = new ArrayList<>(assocs.size());
        this.actionsById = new HashMap<>(assocs.size());
        this.actionsByName = new HashMap<>(assocs.size());
        this.actionsByDispositionActionName = new HashMap<>(assocs.size());
        int index = 0;
        for (ChildAssociationRef assoc : assocs)
        {            
            DispositionActionDefinition da = new DispositionActionDefinitionImpl(services.getRecordsManagementEventService(), services.getRecordsManagementActionService(), nodeService, assoc.getChildRef(), index); 
            actions.add(da);
            actionsById.put(da.getId(), da);
            index++;
            
            String actionNodeName = (String) nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME);
            if (!actionNodeName.equals(da.getId()))
            {
                //It was imported and now has new ID. Old ID may present in old files.
                actionsByName.put(actionNodeName, da);
            }
            
            String actionDefintionName = (String)nodeService.getProperty(assoc.getChildRef(), PROP_DISPOSITION_ACTION_NAME);
            if (actionDefintionName != null)
            {
                actionsByDispositionActionName.put(actionDefintionName, da);
            }
        }
    } 
}
