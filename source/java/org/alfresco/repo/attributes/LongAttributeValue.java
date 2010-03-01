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
 * Value based implementation of a long attribute.
 * @author britt
 */
public class LongAttributeValue extends AttributeValue implements LongAttribute
{
    private static final long serialVersionUID = 3978001405238962585L;
    
    private long fData;
    
    public LongAttributeValue(long value)
    {
        fData = value;
    }

    public LongAttributeValue(LongAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getLongValue();
    }
    
    public Type getType()
    {
        return Type.LONG;
    }

    public Serializable getRawValue()
    {
        return Long.valueOf(fData);
    }

    @Override
    public long getLongValue()
    {
        return fData;
    }

    @Override
    public void setLongValue(long value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return Long.toString(fData);
    }
}
