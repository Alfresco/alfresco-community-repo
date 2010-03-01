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
 * Value based implementation of a boolean attribute.
 * @author britt
 */
public class BooleanAttributeValue extends AttributeValue implements
        BooleanAttribute
{
    private static final long serialVersionUID = 4019402783943642209L;

    private boolean fData;
    
    public BooleanAttributeValue(boolean value)
    {
        fData = value;
    }
    
    public BooleanAttributeValue(BooleanAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getBooleanValue();
    }
    
    public Type getType()
    {
        return Type.BOOLEAN;
    }

    public Serializable getRawValue()
    {
        return Boolean.valueOf(fData);
    }

    @Override
    public boolean getBooleanValue()
    {
        return fData;
    }

    @Override
    public void setBooleanValue(boolean value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return fData ? "true" : "false";
    }
}
