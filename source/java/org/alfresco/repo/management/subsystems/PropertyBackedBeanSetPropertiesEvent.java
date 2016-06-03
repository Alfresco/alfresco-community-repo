package org.alfresco.repo.management.subsystems;

import java.util.Map;


/**
 * An event emitted before a {@link PropertyBackedBean} updates its properties.
 * 
 * @author Alan Davis
 */
public class PropertyBackedBeanSetPropertiesEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 7530572539759535003L;
    
    private Map<String, String> properties;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanSetPropertiesEvent(PropertyBackedBean source, Map<String, String> properties)
    {
        super(source);
        this.properties = properties;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }    
}
