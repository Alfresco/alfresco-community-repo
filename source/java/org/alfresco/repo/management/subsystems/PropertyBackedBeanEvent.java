package org.alfresco.repo.management.subsystems;

import java.util.List;

import org.springframework.context.ApplicationEvent;

/**
 * A base class for events emitted by {@link PropertyBackedBean}s.
 * 
 * @author dward
 */
public abstract class PropertyBackedBeanEvent extends ApplicationEvent
{
    private static final long serialVersionUID = -5414152423990988923L;

    /** The ID of the bean that emitted the event. */
    private List<String> sourceId;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanEvent(PropertyBackedBean source)
    {
        super(source);
        this.sourceId = source.getId();
    }

    /**
     * Gets the ID of the bean that emitted the event.
     * 
     * @return the ID
     */
    public List<String> getSourceId()
    {
        return this.sourceId;
    }
}
