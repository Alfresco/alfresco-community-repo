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

package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 * Manual event parameter constraint
 * 
 * @author Craig Tan
 */
public class ManualEventParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "rm-ac-manual-events";
    
    private RecordsManagementEventService recordsManagementEventService;
    
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService)
    {
        this.recordsManagementEventService = recordsManagementEventService;
    }
          
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        List<RecordsManagementEvent> events = recordsManagementEventService.getEvents();
        Map<String, String> result = new HashMap<>(events.size());
        for (RecordsManagementEvent event : events)
        {
            RecordsManagementEventType eventType = recordsManagementEventService.getEventType(event.getType());
            if (eventType != null && !eventType.isAutomaticEvent())
            {
                result.put(event.getName(), event.getDisplayLabel());
            }
        }        
        return result;
    }    
    
    
}
