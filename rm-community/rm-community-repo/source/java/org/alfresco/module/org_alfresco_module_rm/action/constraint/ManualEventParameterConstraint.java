 

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
        Map<String, String> result = new HashMap<String, String>(events.size());
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
