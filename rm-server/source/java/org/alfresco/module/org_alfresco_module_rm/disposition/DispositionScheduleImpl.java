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
package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
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
    
    private List<DispositionActionDefinition> actions;
    private Map<String, DispositionActionDefinition> actionsById;
    
    //If name is not the same as node-uuid, then action will be stored here too
    //Fix for ALF-2588
    private Map<String, DispositionActionDefinition> actionsByName;
    
    /** Map of disposition definitions by disposition action name */
    private Map<String, DispositionActionDefinition> actionsByDispositionActionName;
    
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
        boolean result = false;
        Boolean value = (Boolean)this.nodeService.getProperty(this.dispositionDefinitionNodeRef, PROP_RECORD_LEVEL_DISPOSITION);
        if (value != null)
        {
            result = value.booleanValue();
        }            
        return result;
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
            getDispositionActionsImpl();
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
        this.actions = new ArrayList<DispositionActionDefinition>(assocs.size());
        this.actionsById = new HashMap<String, DispositionActionDefinition>(assocs.size()); 
        this.actionsByName = new HashMap<String, DispositionActionDefinition>(assocs.size()); 
        this.actionsByDispositionActionName = new HashMap<String, DispositionActionDefinition>(assocs.size());
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
