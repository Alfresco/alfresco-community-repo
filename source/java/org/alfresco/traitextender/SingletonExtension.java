
package org.alfresco.traitextender;

/**
 * A singleton extension API implementor. The singleton extension continues to
 * exist after the extensible has been collected. The instance of this extension
 * is shared among {@link Extensible}s defining extension-points that this
 * extension is bound to.The {@link Trait} it requires is set at call-time on
 * the local thread.
 *
 * @author Bogdan Horje
 */
public abstract class SingletonExtension<E, T extends Trait>
{
    private ThreadLocal<T> localTrait = new ThreadLocal<>();

    private Class<T> traitClass;

    public SingletonExtension(Class<T> traitClass)
    {
        super();
        this.traitClass = traitClass;
    }

    public boolean acceptsTrait(Object trait)
    {
        return trait != null && acceptsTraitClass(trait.getClass());
    }

    public boolean acceptsTraitClass(Class<?> aTraitClass)
    {
        return traitClass.isAssignableFrom(aTraitClass);
    }

    void setTrait(T trait)
    {
        localTrait.set(trait);
    }

    /**
     * @return the {@link Trait} instance of the current execution extension
     *         call.
     */
    protected T getTrait()
    {
        return localTrait.get();
    }

}
