/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
