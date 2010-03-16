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
package org.alfresco.repo.domain.propval;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_class</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyClassEntity
{
    private Long id;
    private Class<?> javaClass;
    private String javaClassName;
    private String javaClassNameShort;
    private long javaClassNameCrc;
    
    public PropertyClassEntity()
    {
    }
    
    @Override
    public int hashCode()
    {
        return (javaClass == null ? 0 : javaClass.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof PropertyClassEntity)
        {
            PropertyClassEntity that = (PropertyClassEntity) obj;
            return EqualsHelper.nullSafeEquals(this.javaClass, that.javaClass);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyClassEntity")
          .append("[ ID=").append(id)
          .append(", javaClass=").append(javaClass)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-class pair
     */
    public Pair<Long, Class<?>> getEntityPair()
    {
        return new Pair<Long, Class<?>>(id, getJavaClass());
    }
    
    public Class<?> getJavaClass()
    {
        if (javaClass == null && javaClassName != null)
        {
            try
            {
                javaClass = Class.forName(javaClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new AlfrescoRuntimeException(
                        "Property class '" + javaClassName + "' is not available to the VM");
            }
        }
        return javaClass;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setJavaClass(Class<?> javaClass)
    {
        this.javaClass = javaClass;
        this.javaClassName = javaClass.getName();
        Pair<String, Long> crcPair = CrcHelper.getStringCrcPair(javaClassName, 32, true, true);
        this.javaClassNameShort = crcPair.getFirst();
        this.javaClassNameCrc = crcPair.getSecond();
    }

    public String getJavaClassName()
    {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;
    }

    public String getJavaClassNameShort()
    {
        return javaClassNameShort;
    }

    public void setJavaClassNameShort(String javaClassNameShort)
    {
        this.javaClassNameShort = javaClassNameShort;
    }

    public long getJavaClassNameCrc()
    {
        return javaClassNameCrc;
    }

    public void setJavaClassNameCrc(long javaClassNameCrc)
    {
        this.javaClassNameCrc = javaClassNameCrc;
    }
}
