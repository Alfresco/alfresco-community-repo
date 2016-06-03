package org.alfresco.repo.management.subsystems;

/**
 * An event emitted a {@link PropertyBackedBean} is destroyed.
 * 
 * @author dward
 */
public class PropertyBackedBeanUnregisteredEvent extends PropertyBackedBeanEvent
{
    private static final long serialVersionUID = 4154847737689541132L;

    private final boolean isPermanent;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source of the event
     */
    public PropertyBackedBeanUnregisteredEvent(PropertyBackedBean source, boolean isPermanent)
    {
        super(source);
        this.isPermanent = isPermanent;
    }

    /**
     * Is the component being destroyed forever, i.e. should persisted values be removed?
     * 
     * @return <code>true</code> if the bean is being destroyed forever. On server shutdown, this value would be
     *         <code>false</code>, whereas on the removal of a dynamically created instance, this value would be
     *         <code>true</code>.
     */
    public boolean isPermanent()
    {
        return isPermanent;
    }
}
