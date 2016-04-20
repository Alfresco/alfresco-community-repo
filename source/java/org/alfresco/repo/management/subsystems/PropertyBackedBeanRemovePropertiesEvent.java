package org.alfresco.repo.management.subsystems;

import java.util.Collection;


/**
 * An event emitted before a {@link PropertyBackedBean} removes properties.
 * 
 * @author Alan Davis
 */
public class PropertyBackedBeanRemovePropertiesEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 7076784618042401540L;

    private Collection<String> properties;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanRemovePropertiesEvent(PropertyBackedBean source, Collection<String> properties)
    {
        super(source);
        this.properties = properties;
    }

    public Collection<String> getProperties()
    {
        return properties;
    }    
}
