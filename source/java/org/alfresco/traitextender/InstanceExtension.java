
package org.alfresco.traitextender;

/**
 * Sub classes are extension API implementors that get instantiated once per
 * extensible-extension point definition.
 *
 * @author Bogdan Horje
 */
public abstract class InstanceExtension<E, T extends Trait>
{
    protected T trait;

    public InstanceExtension(T trait)
    {
        super();
        this.trait = trait;
    }

}
