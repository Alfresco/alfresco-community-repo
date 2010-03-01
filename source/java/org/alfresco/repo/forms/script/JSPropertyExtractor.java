/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.forms.script;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JSPropertyExtractor
{
    //TODO Add logging.
    private StringBuilder getCapitalisedPropertyName(String name)
    {
        // Capitalise the first letter of the name.
        StringBuilder capitalisedPropertyName = new StringBuilder();
        capitalisedPropertyName.append(name.substring(0, 1).toUpperCase());
        if (name.length() > 1)
        {
            capitalisedPropertyName.append(name.substring(1));
        }
        return capitalisedPropertyName;
    }

    boolean propertyExists(String propertyName, Object jsObject)
    {
        return this.resolveMethod(propertyName, jsObject) != null;
    }
    
    Object extractProperty(String propertyName, Object jsObject)
    {
        Method resolvedMethod = resolveMethod(propertyName, jsObject);

        if (resolvedMethod == null)
        {
            return null;
        } else
        {
            try
            {
                Object propertyValue = resolvedMethod.invoke(jsObject,
                        new Object[0]);
                // TODO Value conversion?

                return propertyValue;
            } catch (IllegalArgumentException e)
            {
                return null;
            } catch (IllegalAccessException e)
            {
                return null;
            } catch (InvocationTargetException e)
            {
                return null;
            }
        }
    }

    private Method resolveMethod(String propertyName, Object jsObject)
    {
        StringBuilder capitalisedPropertyName = getCapitalisedPropertyName(propertyName);
        String nameOfPotentialGetMethod = new StringBuilder("get").append(
                capitalisedPropertyName).toString();
        String nameOfPotentialIsMethod = new StringBuilder("is").append(
                capitalisedPropertyName).toString();

        // Class.getMethods() only retrieves public methods.
        Method[] availableMethods = jsObject.getClass().getMethods();
        Method resolvedMethod = null;
        for (Method method : availableMethods)
        {
            // If a wrapped object has both a getFoo() AND an isFoo() method,
            // Rhino selects the getFoo() method. We are doing the same.
            if (resolvedMethod == null && nameOfPotentialIsMethod.equals(method.getName())
                    && method.getParameterTypes().length == 0)
            {
                resolvedMethod = method;
            }
            // intentionally not an else-if
            if (nameOfPotentialGetMethod.equals(method.getName()) && method.getParameterTypes().length == 0)
            {
                resolvedMethod = method;
                break;
            }
        }
        return resolvedMethod;
    }

}
