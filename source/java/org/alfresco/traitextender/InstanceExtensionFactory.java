
package org.alfresco.traitextender;

import java.lang.reflect.Constructor;

/**
 * Creates extension sub classes that are extension API implementors once per
 * extensible-extension point definition.
 *
 * @author Bogdan Horje
 */
public class InstanceExtensionFactory<I extends InstanceExtension<E, T>, T extends Trait, E> implements
            ExtensionFactory<E>
{
    private Class<? extends I> extensionClass;

    private Class<T> traitAPI;

    public <C extends I> InstanceExtensionFactory(Class<C> extensionClass, Class<T> traitAPI,
                Class<? extends E> extensionAPI)
    {
        super();
        this.extensionClass = extensionClass;
        this.traitAPI = traitAPI;

        if (!extensionAPI.isAssignableFrom(extensionClass))
        {
            throw new InvalidExtension("Extension class " + extensionClass + " is incompatible with extensio API "
                        + extensionAPI);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TO extends Trait> E createExtension(TO traitObject)
    {
        try
        {
            // Trait RTTI will be performed anyway at Constructor#newInstance
            // invocation time
            T tTrait = (T) traitObject;

            Constructor<? extends I> c = extensionClass.getConstructor(traitAPI);
            return (E) c.newInstance(tTrait);
        }
        catch (Exception error)
        {
            throw new RuntimeException(error);
        }
    }

    @Override
    public boolean canCreateExtensionFor(ExtensionPoint<?, ?> point)
    {
        return point.getExtensionAPI().isAssignableFrom(extensionClass)
                    && traitAPI.isAssignableFrom(point.getTraitAPI());
    }

}
