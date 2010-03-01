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

package org.alfresco.repo.attributes;

import java.io.Serializable;

/**
 * Value based implementation of a short attribute.
 * @author britt
 */
public class ShortAttributeValue extends AttributeValue implements
        ShortAttribute
{
    private static final long serialVersionUID = -2224950695651369979L;

    private short fData;
    
    public ShortAttributeValue(short value)
    {
        fData = value;
    }
    
    public ShortAttributeValue(ShortAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getShortValue();    
    }
    
    public Type getType()
    {
        return Type.SHORT;
    }

    public Serializable getRawValue()
    {
        return Short.valueOf(fData);
    }

    @Override
    public short getShortValue()
    {
        return fData;
    }

    @Override
    public void setShortValue(short value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return Short.toString(fData);
    }
}
