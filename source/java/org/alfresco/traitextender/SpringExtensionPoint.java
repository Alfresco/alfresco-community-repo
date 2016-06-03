
package org.alfresco.traitextender;

/**
 * An {@link ExtensionPoint} spring bean wrapper with spring registering
 * life-cycle management.<br>
 * Works in conjunction with {@link SpringBeanExtension}s and
 * {@link SpringExtensionBundle}s to define spring based {@link ExtensionBundle}
 * s of singleton extensions.
 *
 * @author Bogdan Horje
 */
public class SpringExtensionPoint
{
    private String trait;

    private String extension;

    public void setTrait(String trait)
    {
        this.trait = trait;
    }

    public void setExtension(String extension)
    {
        this.extension = extension;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void register(RegistryExtensionBundle bundle, SpringBeanExtension<?, ?> extensionBean)
                throws InvalidExtension
    {

        try
        {
            Class<?> extensionAPI = Class.forName(extension);
            Class<? extends Trait> traitAPI = (Class<? extends Trait>) Class.forName(trait);

            // perform RTTIs in order to avoid later cast exceptions

            if (!Trait.class.isAssignableFrom(traitAPI))
            {
                throw new InvalidExtension("Non " + Trait.class + " spring extension point : " + traitAPI);
            }

            if (!extensionBean.acceptsTraitClass(traitAPI))
            {
                throw new InvalidExtension("Unsupported trait class : " + traitAPI + " in extension point targeting "
                            + extensionBean);
            }

            bundle.register(new ExtensionPoint(extensionAPI,
                                               traitAPI),
                            new SingletonExtensionFactory(extensionBean,
                                                          extensionAPI));

        }
        catch (InvalidExtension error)
        {
            throw error;
        }
        catch (ClassNotFoundException error)
        {
            throw new InvalidExtension("Extension point definition class not found.",
                                       error);
        }

    }
}
