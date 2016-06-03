/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
