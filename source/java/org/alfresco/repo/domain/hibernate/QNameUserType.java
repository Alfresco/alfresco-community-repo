/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * Custom type to hide the persistence of {@link org.alfresco.service.namespace.QName qname}
 * instances.
 * 
 * @author Derek Hulley
 */
public class QNameUserType implements UserType
{
    private static int[] SQL_TYPES = new int[] {Types.VARCHAR};
    
    public Class returnedClass()
    {
        return QName.class;
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
        String qnameStr = rs.getString(names[0]);
        if (qnameStr == null)
        {
            return null;
        }
        else
        {
            QName qname = QName.createQName(qnameStr);
            return qname;
        }
    }

    public void nullSafeSet(PreparedStatement stmt, Object value, int index) throws HibernateException, SQLException
    {
        // convert the qname to a string
        stmt.setString(index, value.toString());
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
        // qname is serializable
        return (QName) value;
    }
}
