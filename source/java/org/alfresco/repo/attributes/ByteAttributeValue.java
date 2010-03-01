/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.attributes;

import java.io.Serializable;

/**
 * Value based implementation of byte attribute.
 * @author britt
 */
public class ByteAttributeValue extends AttributeValue implements ByteAttribute
{
    private static final long serialVersionUID = -5011945743563985072L;

    private byte fData;
    
    public ByteAttributeValue(byte value)
    {
        fData = value;
    }
    
    public ByteAttributeValue(ByteAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getByteValue();
    }
    
    public Type getType()
    {
        return Type.BYTE;
    }

    public Serializable getRawValue()
    {
        return Byte.valueOf(fData);
    }

    @Override
    public byte getByteValue()
    {
        return fData;
    }

    @Override
    public void setByteValue(byte value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return Byte.toString(fData);
    }
}
