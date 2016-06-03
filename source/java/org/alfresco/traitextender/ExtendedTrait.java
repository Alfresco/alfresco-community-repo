
package org.alfresco.traitextender;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Trait based extension reference holder.<br>
 * Keeps track of extension references for one extensible and allows the
 * collection of those extensions when the extensible is collected.
 * 
 * @author Bogdan Horje
 */
public class ExtendedTrait<T extends Trait>
{
    private ConcurrentHashMap<Class<?>, Object> extensions = new ConcurrentHashMap<Class<?>, Object>();

    private T trait;

    public ExtendedTrait(T trait)
    {
        super();
        this.trait = trait;
    }

    public T getTrait()
    {
        return trait;
    }

    public <E> E getExtension(Class<E> extensionAPI)
    {
        @SuppressWarnings("unchecked")
        E extension = (E) extensions.get(extensionAPI);

        return extension;
    }

    public synchronized <E> E extend(Class<E> extensionAPI, ExtensionFactory<E> factory)
    {
        @SuppressWarnings("unchecked")
        E extension = (E) extensions.get(extensionAPI);

        if (extension == null)
        {
            extension = factory.createExtension(trait);
            extensions.put(extensionAPI,
                           extension);

        }

        return extension;
    }
}
