/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Locale;

import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.EqualsHelper;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * Custom type to hide the persistence of {@link java.util.Locale locale} instances.
 * 
 * @author Derek Hulley
 */
public class LocaleUserType implements UserType
{
    private static int[] SQL_TYPES = new int[] {Types.VARCHAR};
    
    public Class returnedClass()
    {
        return Locale.class;
    }

    /**
     * @see #SQL_TYPES
     */
    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    public boolean isMutable()
    {
        return false;
    }

    public boolean equals(Object x, Object y) throws HibernateException
    {
        return EqualsHelper.nullSafeEquals(x, y);
    }

    public int hashCode(Object x) throws HibernateException
    {
        return x.hashCode();
    }

    public Object deepCopy(Object value) throws HibernateException
    {
        // the qname is immutable
        return value;
    }

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException
    {
        String localeStr = rs.getString(names[0]);
        if (localeStr == null)
        {
            return null;
        }
        else
        {
            Locale locale = DefaultTypeConverter.INSTANCE.convert(Locale.class, localeStr);
            return locale;
        }
    }

    public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException
    {
        // we want to ensure that the value is consistent w.r.t. the use of '_'
        if (value == null)
        {
            stmt.setNull(index, Types.VARCHAR);
        }
        else
        {
            String localeStr = value.toString();
            if (localeStr.length() < 6)
            {
                localeStr += "_";
            }
            stmt.setString(index, localeStr);
        }
    }

    public Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        // qname is immutable
        return original;
    }

    public Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        // qname is serializable
        return cached;
    }

    public Serializable disassemble(Object value) throws HibernateException
    {
        // locale is serializable
        return (Locale) value;
    }
}
