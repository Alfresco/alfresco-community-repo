package org.alfresco.repo.management.subsystems;



/**
 * An event emitted before a {@link PropertyBackedBean} updates a property.
 * 
 * @author Alan Davis
 */
public class PropertyBackedBeanSetPropertyEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 7421919310357212865L;
    
    private String name;
    private String value;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanSetPropertyEvent(PropertyBackedBean source, String name, String value)
    {
        super(source);
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
