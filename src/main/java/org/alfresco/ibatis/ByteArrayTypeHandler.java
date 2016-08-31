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
package org.alfresco.ibatis;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.alfresco.ibatis.SerializableTypeHandler.DeserializationException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * MyBatis 3.x TypeHandler for <tt>_byte[]</tt> to <b>BLOB</b> types.
 * 
 * @author sglover
 * @since 5.0
 */
public class ByteArrayTypeHandler implements TypeHandler
{
    /**
     * @throws DeserializationException if the object could not be deserialized
     */
    public Object getResult(ResultSet rs, String columnName) throws SQLException
    {
        byte[] ret = null;
        try
        {
            byte[] bytes = rs.getBytes(columnName);
            if(bytes != null && !rs.wasNull())
            {
                ret = bytes;
            }
        }
        catch (Throwable e)
        {
            throw new DeserializationException(e);
        }
        return ret;
    }

    @Override
    public Object getResult(ResultSet rs, int columnIndex) throws SQLException
    {
        byte[] ret = null;
        try
        {
            byte[] bytes = rs.getBytes(columnIndex);
            if(bytes != null && !rs.wasNull())
            {
                ret = bytes;
            }
        }
        catch (Throwable e)
        {
            throw new DeserializationException(e);
        }
        return ret;
    }

    public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException
    {
        if (parameter == null)
        {
            ps.setNull(i, Types.BINARY);
        }
        else
        {
            try
            {
                ps.setBytes(i, (byte[])parameter);
            }
            catch (Throwable e)
            {
                throw new SerializationException(e);
            }
        }
    }
    
    public Object getResult(CallableStatement cs, int columnIndex) throws SQLException 
    {
        throw new UnsupportedOperationException("Unsupported");
    }

    /**
     * @return          Returns the value given
     */
    public Object valueOf(String s)
    {
        return s;
    }
    
    /**
     * Marker exception to allow deserialization issues to be dealt with by calling code.
     * If this exception remains uncaught, it will be very difficult to find and rectify
     * the data issue.
     * 
     * @author sglover
     * @since 5.0
     */
    public static class DeserializationException extends RuntimeException
    {
        private static final long serialVersionUID = 4673487701048985340L;

        public DeserializationException(Throwable cause)
        {
            super(cause);
        }
    }
    
    /**
     * Marker exception to allow serialization issues to be dealt with by calling code.
     * Unlike with {@link DeserializationException deserialization}, it is not important
     * to handle this exception neatly.
     *   
     * @author sglover
     * @since 5.0
     */
    public static class SerializationException extends RuntimeException
    {
        private static final long serialVersionUID = 962957884262870228L;

        public SerializationException(Throwable cause)
        {
            super(cause);
        }
    }
}
