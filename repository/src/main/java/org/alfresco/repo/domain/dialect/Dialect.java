/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.domain.dialect;

/**
 * Class partially copied from patched hibernate 3.2.6
 * 
 * @since 6.0
 */
public class Dialect
{
    private final TypeNames typeNames = new TypeNames();

    protected Dialect()
    {
    }

    public String toString()
    {
        return getClass().getName();
    }

    /**
     * Subclasses register a type name for the given type code and maximum
     * column length. <tt>$l</tt> in the type name with be replaced by the
     * column length (if appropriate).
     *
     * @param code The {@link java.sql.Types} typecode
     * @param capacity The maximum length of database type
     * @param name The database type name
     */
    protected void registerColumnType(int code, int capacity, String name)
    {
        typeNames.put( code, capacity, name );
    }

    /**
     * Subclasses register a type name for the given type code. <tt>$l</tt> in
     * the type name with be replaced by the column length (if appropriate).
     *
     * @param code The {@link java.sql.Types} typecode
     * @param name The database type name
     */
    protected void registerColumnType(int code, String name)
    {
        typeNames.put( code, name );
    }
}
