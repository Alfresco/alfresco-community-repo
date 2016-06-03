package org.alfresco.repo.management.subsystems;

/**
 * An event emitted after a {@link PropertyBackedBean} is initialized.
 * 
 * @author dward
 */
public class PropertyBackedBeanRegisteredEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = -2860105961131524745L;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanRegisteredEvent(PropertyBackedBean source)
    {
        super(source);
    }    
}
