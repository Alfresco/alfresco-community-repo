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
import java.util.Collection;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;

/**
 * Disposition action implementation
 *
 * @author Roy Wetherall
 */
public class DispositionActionDefinitionImpl implements DispositionActionDefinition, RecordsManagementModel
{
    /** Name */
    private String name;

    /** Description */
    private String description;

    /** Label */
    private String label;

    /** Node service */
    private NodeService nodeService;

    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** Records management event service */
    private RecordsManagementEventService recordsManagementEventService;

    /** Disposition action node reference */
    private NodeRef dispositionActionNodeRef;

    /** Action index */
    private int index;

    /** Ghost on detroy */
    private String ghostOnDestroy;

    /**
     * Constructor
     *
     * @param recordsManagementEventService  records management event service
     * @param recordsManagementActionService records management action service
     * @param nodeService  node service
     * @param nodeRef   disposition action node reference
     * @param index     index of disposition action
     */
    public DispositionActionDefinitionImpl(RecordsManagementEventService recordsManagementEventService, RecordsManagementActionService recordsManagementActionService, NodeService nodeService, NodeRef nodeRef, int index)
    {
        this.recordsManagementEventService = recordsManagementEventService;
        this.recordsManagementActionService = recordsManagementActionService;
        this.nodeService = nodeService;
        this.dispositionActionNodeRef = nodeRef;
        this.index = index;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getNodeRef()
     */
    @Override
    public NodeRef getNodeRef()
    {
        return this.dispositionActionNodeRef;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getIndex()
     */
    @Override
    public int getIndex()
    {
        return this.index;
    }

    /**
     *  @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getId()
     */
    @Override
    public String getId()
    {
        return this.dispositionActionNodeRef.getId();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getDescription()
     */
    @Override
    public String getDescription()
    {
        if (description == null)
        {
            description = (String)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_DESCRIPTION);
        }
        return description;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getName()
     */
    @Override
    public String getName()
    {
        if (name == null)
        {
            name = (String)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_ACTION_NAME);
        }
        return name;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getLabel()
     */
    @Override
    public String getLabel()
    {
        if (label == null)
        {
            String name = getName();
            label = name;

            // get the disposition action from the RM action service
            RecordsManagementAction action = recordsManagementActionService.getDispositionAction(name);
            if (action != null)
            {
                label = action.getLabel();
            }
        }

        return label;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getPeriod()
     */
    @Override
    public Period getPeriod()
    {
        return (Period)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_PERIOD);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getPeriodProperty()
     */
    @Override
    public QName getPeriodProperty()
    {
        QName result = null;
        String value = (String)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_PERIOD_PROPERTY);
        if (value != null)
        {
            result = QName.createQName(value);
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getEvents()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<RecordsManagementEvent> getEvents()
    {
        List<RecordsManagementEvent> events = null;
        Collection<String> eventNames = (Collection<String>)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_EVENT);
        if (eventNames != null)
        {
            events = new ArrayList<>(eventNames.size());
            for (String eventName : eventNames)
            {
                RecordsManagementEvent event = recordsManagementEventService.getEvent(eventName);
                events.add(event);
            }
        }
        else
        {
            events = java.util.Collections.EMPTY_LIST;
        }
        return events;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#eligibleOnFirstCompleteEvent()
     */
    @Override
    public boolean eligibleOnFirstCompleteEvent()
    {
        boolean result = true;
        String value = (String)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_EVENT_COMBINATION);
        if (value != null && value.equals("and"))
        {
            result = false;
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getLocation()
     */
    @Override
    public String getLocation()
    {
        return (String)nodeService.getProperty(this.dispositionActionNodeRef, PROP_DISPOSITION_LOCATION);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition#getGhostOnDestroy()
     */
    @Override
    public String getGhostOnDestroy()
    {
        if (ghostOnDestroy == null)
        {
            ghostOnDestroy = (String) nodeService.getProperty(this.dispositionActionNodeRef,
                    PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY);
        }
        return ghostOnDestroy;
    }
}
