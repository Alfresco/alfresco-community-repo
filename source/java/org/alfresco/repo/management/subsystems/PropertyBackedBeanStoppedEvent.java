package org.alfresco.repo.management.subsystems;

import java.util.List;


/**
 * An event emitted before a {@link PropertyBackedBean} is stopped.
 * 
 * @author dward
 */
public class PropertyBackedBeanStoppedEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = -8096989839647678810L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanStoppedEvent(PropertyBackedBean source)
    {
        super(source);
    }    
}
