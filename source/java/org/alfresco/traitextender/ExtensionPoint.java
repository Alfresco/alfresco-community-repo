
package org.alfresco.traitextender;

/**
 * Defines a two-way interfacing mechanism between a {@link Trait} exposing
 * object and an extension of that object.<br>
 * The extended object can call methods of the {@link #extensionAPI} which will
 * be able to interact with the extended object through the {@link #traitAPI}
 * interface it was paired with in the extension point. The actual circumstances
 * in which the extension methods are invoked are not defined by the extension
 * point.
 *
 * @author Bogdan Horje
 */
public class ExtensionPoint<E, M extends Trait>
{
    private Class<E> extensionAPI;

    private Class<M> traitAPI;

    public ExtensionPoint(Class<E> extensionAPI, Class<M> traitAPI)
    {
        super();
        this.extensionAPI = extensionAPI;
        this.traitAPI = traitAPI;
    }

    public Class<E> getExtensionAPI()
    {
        return this.extensionAPI;
    }

    public Class<M> getTraitAPI()
    {
        return this.traitAPI;
    }

    @Override
    public int hashCode()
    {
        return extensionAPI.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExtensionPoint)
        {
            ExtensionPoint<?, ?> pointObj = (ExtensionPoint<?, ?>) obj;
            return extensionAPI.equals(pointObj.extensionAPI) && traitAPI.equals(pointObj.traitAPI);
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "{" + extensionAPI.toString() + "," + traitAPI.toString() + "}";
    }
}
