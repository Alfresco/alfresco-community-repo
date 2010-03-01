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
 * Value based implementation of int attribute.
 * @author britt
 */
public class IntAttributeValue extends AttributeValue implements IntAttribute
{
    private static final long serialVersionUID = -7547112946658496030L;

    private int fData;
    
    public IntAttributeValue(int value)
    {
        fData = value;
    }
    
    public IntAttributeValue(IntAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getIntValue();
    }
    
    public Type getType()
    {
        return Type.INT;
    }

    public Serializable getRawValue()
    {
        return Integer.valueOf(fData);
    }

    @Override
    public int getIntValue()
    {
        return fData;
    }

    @Override
    public void setIntValue(int value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return Integer.toString(fData);
    }
}
