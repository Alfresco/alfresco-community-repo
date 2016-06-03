
package org.alfresco.traitextender;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.util.ParameterCheck;

/**
 * {@link ExtensionBundle} that supports simple {@link ExtensionPoint} to
 * {@link ExtensionFactory} association registration.
 *
 * @author Bogdan Horje
 */
public class RegistryExtensionBundle implements ExtensionBundle
{

    private Map<ExtensionPoint<?, ?>, ExtensionFactory<?>> factories = new HashMap<>();

    private String id;

    public RegistryExtensionBundle(String id)
    {
        ParameterCheck.mandatory("id",
                                 id);
        this.id = id;
    }

    /**
     * Registers an association between the given {@link ExtensionPoint} and
     * {@link ExtensionFactory}.<br>
     * At {@link #start(Extender)} time all registered {@link ExtensionPoint}s
     * will be registered with the given {@link Extender}.<br>
     * At {@link #stop(Extender)} time all registered {@link ExtensionPoint}s
     * will be unregistered with the given {@link Extender}.<br>
     * 
     * @param point
     * @param factory
     */
    public <E, C extends E, M extends Trait> void register(ExtensionPoint<E, M> point, ExtensionFactory<C> factory)
    {
        factories.put(point,
                      factory);
    }

    @Override
    public void start(Extender extender)
    {
        Set<Entry<ExtensionPoint<?, ?>, ExtensionFactory<?>>> factoryEntries = factories.entrySet();
        for (Entry<ExtensionPoint<?, ?>, ExtensionFactory<?>> entry : factoryEntries)
        {
            extender.register(entry.getKey(),
                              entry.getValue());
        }
    }

    @Override
    public void stop(Extender extender)
    {
        Set<Entry<ExtensionPoint<?, ?>, ExtensionFactory<?>>> factoryEntries = factories.entrySet();
        for (Entry<ExtensionPoint<?, ?>, ExtensionFactory<?>> entry : factoryEntries)
        {
            extender.unregister(entry.getKey());
        }
    }

    @Override
    public String getId()
    {
        return id;
    }

}
