 
package org.alfresco.module.org_alfresco_module_rm.event;

import java.util.List;

/**
 * Records management event service interface
 *
 * @author Roy Wetherall
 */
public interface RecordsManagementEventService
{
    /**
     * Register an event type
     *
     * @param eventType     event type
     */
    void registerEventType(RecordsManagementEventType eventType);

    /**
     * Get a list of the event types
     *
     * @return  List<RecordsManagementEventType>    list of the event types
     */
    List<RecordsManagementEventType> getEventTypes();

    /**
     * Get the records management event type
     *
     * @param eventType                     name
     * @return RecordsManagementEventType   event type
     */
    RecordsManagementEventType getEventType(String eventTypeName);

    /**
     * Get the list of available events
     *
     * @return  List<RecordsManagementEvent>    list of events
     */
    List<RecordsManagementEvent> getEvents();

    /**
     * Get a records management event given its name.  Returns null if the event name is not
     * recognised.
     *
     * @param eventName                 event name
     * @return RecordsManagementEvent   event
     */
    RecordsManagementEvent getEvent(String eventName);

    /**
     * Indicates whether a particular event exists.  Returns true if it does, false otherwise.
     *
     * @param eventName     event name
     * @return boolean      true if event exists, false otherwise
     */
    boolean existsEvent(String eventName);

    /**
     * Indicates whether a particular event can be created. Returns true if it is possible, false otherwise.
     *
     * @param eventDisplayLabel event display label
     * @param eventName event name
     * @return true if the event can be created, false otherwise
     */
    boolean canCreateEvent(String eventDisplayLabel, String eventName);

    /**
     * Indicates whether a particular event can be edited. Returns true if it is possible, false otherwise.
     *
     * @param eventDisplayLabel event display label
     * @param eventName event name
     * @param eventType event type
     * @return true if the event can be edited, false otherwise
     */
    boolean canEditEvent(String eventDisplayLabel, String eventName, String eventType);

    /**
     * Add an event
     *
     * @param eventType             event type
     * @param eventName             event name
     * @param eventDisplayLabel     event display label
     */
    RecordsManagementEvent addEvent(String eventType, String eventName, String eventDisplayLabel);

    /**
     * Remove an event
     *
     * @param eventName     event name
     */
    void removeEvent(String eventName);

}
