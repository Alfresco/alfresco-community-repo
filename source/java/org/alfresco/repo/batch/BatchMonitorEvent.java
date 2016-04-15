package org.alfresco.repo.batch;

import org.springframework.context.ApplicationEvent;

/**
 * An event alerting listeners to the existence of a new {@link BatchMonitor}.
 * 
 * @author dward
 */
public class BatchMonitorEvent extends ApplicationEvent
{

    private static final long serialVersionUID = -5787104103292355106L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public BatchMonitorEvent(BatchMonitor source)
    {
        super(source);
    }

    /**
     * Gets the source batch monitor.
     * 
     * @return the batch monitor
     */
    public BatchMonitor getBatchMonitor()
    {
        return (BatchMonitor) getSource();
    }

}
