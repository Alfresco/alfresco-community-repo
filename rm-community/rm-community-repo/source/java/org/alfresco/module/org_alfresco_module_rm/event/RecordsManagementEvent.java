 
package org.alfresco.module.org_alfresco_module_rm.event;

import org.alfresco.util.ParameterCheck;

/**
 * Records management event 
 * 
 * @author Roy Wetherall
 * @since 1.0
 */
public class RecordsManagementEvent
{ 
    /** Records management event type */
    private RecordsManagementEventType type;
    
    /** Records management event name */
    private String name;
    
    /** Records management display label */
    private String displayLabel;
    
    /**
     * Constructor
     * 
     * @param type          event type
     * @param name          event name
     * @param displayLabel  event display label
     */
    public RecordsManagementEvent(RecordsManagementEventType type, String name, String displayLabel)
    {
        ParameterCheck.mandatory("type", type);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("displayLabel", displayLabel);
        
        this.type =  type;
        this.name = name;
        this.displayLabel = displayLabel;
    }
    
    /**
     * Get records management type name
     * 
     * @return  String records management event type name
     */
    public String getType()
    {
        return type.getName();
    }
    
    /**
     * Get the records management event type.
     * 
     * @return {@link RecordsManagementEventType}   records management event type 
     * 
     * @since 2.2
     */
    public RecordsManagementEventType getRecordsManagementEventType()
    {
        return type;
    }
    
    /**
     * Event name
     * 
     * @return String   event name
     */
    public String getName()
    {
        return this.name;
    }
    
    /**
     * 
     * @return
     */
    public String getDisplayLabel()
    {
        return displayLabel;
    }    
}
