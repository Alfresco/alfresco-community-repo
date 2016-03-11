package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;

/**
 * Disposition action interface
 * 
 * @author Roy Wetherall
 */
public interface DispositionActionDefinition
{
    /**
     * Get the NodeRef that represents the disposition action definition
     * 
     * @return NodeRef of disposition action definition
     */
    NodeRef getNodeRef();

    /**
     * Get disposition action id
     * 
     * @return String id
     */
    String getId();

    /**
     * Get the index of the action within the disposition instructions
     * 
     * @return int disposition action index
     */
    int getIndex();

    /**
     * Get the name of disposition action
     * 
     * @return String name
     */
    String getName();

    /**
     * Get the display label of the disposition action
     * 
     * @return String name's display label
     */
    String getLabel();

    /**
     * Get the description of the disposition action
     * 
     * @return String description
     */
    String getDescription();

    /**
     * Get the period for the disposition action
     * 
     * @return Period disposition period
     */
    Period getPeriod();

    /**
     * Property to which the period is relative to
     * 
     * @return QName property name
     */
    QName getPeriodProperty();

    /**
     * List of events for the disposition
     * 
     * @return List<RecordsManagementEvent> list of events
     */
    List<RecordsManagementEvent> getEvents();

    /**
     * Indicates whether the disposition action is eligible when the earliest
     * event is complete, otherwise all events must be complete before
     * eligibility.
     * 
     * @return boolean true if eligible on first action complete, false
     *         otherwise
     */
    boolean eligibleOnFirstCompleteEvent();

    /**
     * Get the location of the disposition (can be null)
     * 
     * @return String disposition location
     */
    String getLocation();

    /**
     * Get the ghost on destroy from the disposition
     * 
     * @return boolean the gost on destroy flag (on applicable to destroy
     *         actions)
     */
    String getGhostOnDestroy();

}
