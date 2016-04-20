package org.alfresco.repo.search.impl.lucene.index;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of a significant event relating to a Lucene index. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 */
public class IndexEvent extends ApplicationEvent
{

    private static final long serialVersionUID = -4616231785087405506L;

    /** The event description. */
    private final String description;

    /** Its instance count. */
    private final int count;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source index monitor
     * @param description
     *            the event description
     * @param count
     *            its instance count
     */
    public IndexEvent(IndexMonitor source, String description, int count)
    {
        super(source);
        this.description = description;
        this.count = count;
    }

    /**
     * Gets the source index monitor.
     * 
     * @return the index monitor
     */
    public IndexMonitor getIndexMonitor()
    {
        return (IndexMonitor) getSource();
    }

    /**
     * Gets the event description.
     * 
     * @return the description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Gets the event instance count.
     * 
     * @return the count
     */
    public int getCount()
    {
        return this.count;
    }

}
