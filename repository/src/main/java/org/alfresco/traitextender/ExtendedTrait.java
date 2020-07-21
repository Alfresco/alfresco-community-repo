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
