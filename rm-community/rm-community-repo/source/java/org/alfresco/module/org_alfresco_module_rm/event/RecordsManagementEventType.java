 
package org.alfresco.module.org_alfresco_module_rm.event;

/**
 * Records management event type interface
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementEventType
{
    /**
     * Get the name of the records management event type
     * 
     * @return  String  event type name
     */
    String getName();
    
    /**
     * Gets the display label of the event type
     * 
     * @return  String  display label
     */
    String getDisplayLabel();
    
    /**
     * Indicates whether the event is automatic or not
     * 
     * @return  boolean     true if automatic, false otherwise
     */
    boolean isAutomaticEvent();
}
