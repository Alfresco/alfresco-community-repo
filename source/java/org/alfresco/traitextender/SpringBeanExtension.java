
package org.alfresco.traitextender;

/**
 * A {@link SingletonExtension} extension-API implementor defined as a
 * spring-bean.<br>
 * Handles also spring-bundle extension registrations.
 *
 * @author Bogdan Horje
 */
public abstract class SpringBeanExtension<E, T extends Trait> extends SingletonExtension<E, T>
{
    private SpringExtensionPoint extensionPoint;

    public SpringBeanExtension(Class<T> traitClass)
    {
        super(traitClass);
    }

    public void setExtensionPoint(SpringExtensionPoint extensionPoint)
    {
        this.extensionPoint = extensionPoint;
    }

    public void register(RegistryExtensionBundle bundle) throws InvalidExtension
    {
        extensionPoint.register(bundle,
                                this);
    }
}
