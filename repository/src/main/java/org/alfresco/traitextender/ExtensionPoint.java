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
