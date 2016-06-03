package org.alfresco.repo.management.subsystems;

import java.util.List;


/**
 * An event emitted after a {@link PropertyBackedBean} is started.
 * 
 * @author dward
 */
public class PropertyBackedBeanStartedEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 6019157155489029474L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanStartedEvent(PropertyBackedBean source)
    {
        super(source);
    }    
}
