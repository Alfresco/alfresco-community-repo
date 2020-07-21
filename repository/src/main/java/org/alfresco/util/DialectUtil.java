/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.util;

import org.alfresco.repo.domain.dialect.Dialect;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class DialectUtil
{
    /** The placeholder for the configured <code>Dialect</code> class name: <b>${db.script.dialect}</b> */
    public static final String PLACEHOLDER_DIALECT = "\\$\\{db\\.script\\.dialect\\}";

    /**
     * Replaces the dialect placeholder in the resource URL and attempts to find a
     * file for it. If not found, the dialect hierarchy will be walked until a
     * compatible resource is found. This makes it possible to have resources that
     * are generic to all dialects.
     *
     * @return The Resource, otherwise null
     */
    public static Resource getDialectResource(ResourcePatternResolver resourcePatternResolver, Class<?> dialectClass, String resourceUrl)
    {
        // replace the dialect placeholder
        String dialectResourceUrl = resolveDialectUrl(dialectClass, resourceUrl);
        // get a handle on the resource
        Resource resource = resourcePatternResolver.getResource(dialectResourceUrl);
        if (!resource.exists())
        {
            // it wasn't found. Get the superclass of the dialect and try again
            Class<?> superClass = dialectClass.getSuperclass();
            if (Dialect.class.isAssignableFrom(superClass))
            {
                // we still have a Dialect - try again
                return getDialectResource(resourcePatternResolver, superClass, resourceUrl);
            }
            else
            {
                // we have exhausted all options
                return null;
            }
        }
        else
        {
            // we have a handle to it
            return resource;
        }
    }

    /**
     * Takes resource URL containing the {@link DialectUtil#PLACEHOLDER_DIALECT
     * dialect placeholder text} and substitutes the placeholder with the name of
     * the given dialect's class.
     * <p/>
     * For example:
     * 
     * <pre>
     * resolveDialectUrl(MySQLInnoDBDialect.class, "classpath:alfresco/db/${db.script.dialect}/myfile.xml")
     * </pre>
     * 
     * would give the following String:
     * 
     * <pre>
     *   classpath:alfresco/db/org.hibernate.dialect.MySQLInnoDBDialect/myfile.xml
     * </pre>
     */
    public static String resolveDialectUrl(Class<?> dialectClass, String resourceUrl)
    {
        return resourceUrl.replaceAll(PLACEHOLDER_DIALECT, dialectClass.getName());
    }
}
