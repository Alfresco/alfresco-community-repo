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
