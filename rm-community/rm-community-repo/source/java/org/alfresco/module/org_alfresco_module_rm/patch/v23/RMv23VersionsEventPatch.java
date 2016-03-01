 
package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * RM v2.3 patch that creates the versions event.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RMv23VersionsEventPatch extends AbstractModulePatch
{
	/** event details */
	private static final String EVENT_TYPE = "rmEventType.versioned";
	private static final String EVENT_NAME = "versioned";
	private static final String EVENT_I18N = "rmevent.versioned";
	
	/** records management event service */
    private RecordsManagementEventService recordsManagementEventService;
    
    /**
     * @param recordsManagementEventService	records management event service
     */
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService) 
    {
		this.recordsManagementEventService = recordsManagementEventService;
	}
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
    	// add versions event
        recordsManagementEventService.addEvent(EVENT_TYPE, EVENT_NAME, I18NUtil.getMessage(EVENT_I18N));
    }
    
}
