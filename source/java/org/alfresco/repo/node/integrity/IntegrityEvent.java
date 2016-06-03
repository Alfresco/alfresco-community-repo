package org.alfresco.repo.node.integrity;

import java.util.List;

/**
 * Stores information for all events in the system
 * 
 * @author Derek Hulley
 */
public interface IntegrityEvent
{
    /**
     * Checks integrity pertinent to the event
     * 
     * @param eventResults the list of event results that can be added to
     */
    public void checkIntegrity(List<IntegrityRecord> eventResults);
    
    public List<StackTraceElement[]> getTraces();
    
    public void addTrace(StackTraceElement[] trace);
}
