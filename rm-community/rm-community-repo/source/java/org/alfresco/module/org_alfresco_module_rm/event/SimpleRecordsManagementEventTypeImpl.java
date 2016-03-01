 
package org.alfresco.module.org_alfresco_module_rm.event;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Simple records management event type implementation
 *
 * @author Roy Wetherall
 */
public class SimpleRecordsManagementEventTypeImpl implements RecordsManagementEventType, BeanNameAware
{
    /** Display label lookup prefix */
    protected static final String LOOKUP_PREFIX = "rmeventservice.";

    /** Name */
    public static final String NAME = "rmEventType.simple";

    /** Records management event service */
    private RecordsManagementEventService recordsManagementEventService;

    /** Name */
    private String name;

    /**
     * @return Records management event service
     */
    protected RecordsManagementEventService getRecordsManagementEventService()
    {
        return this.recordsManagementEventService;
    }

    /**
     * Set the records management event service
     *
     * @param recordsManagementEventService     records management service
     */
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService)
    {
        this.recordsManagementEventService = recordsManagementEventService;
    }

    /**
     * Initialisation method
     */
    public void init()
    {
        getRecordsManagementEventService().registerEventType(this);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#isAutomaticEvent()
     */
    public boolean isAutomaticEvent()
    {
        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType#getDisplayLabel()
     */
    public String getDisplayLabel()
    {
        return I18NUtil.getMessage(LOOKUP_PREFIX + getName());
    }
}
