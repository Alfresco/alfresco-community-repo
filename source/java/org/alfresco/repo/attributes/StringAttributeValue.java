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
 * Value based implementation of a String attribute.
 * @author britt
 */
public class StringAttributeValue extends AttributeValue implements
        StringAttribute
{
    private static final long serialVersionUID = -5702787670770131672L;

    private String fData;
    
    public StringAttributeValue(String value)
    {
        fData = value;
    }
    
    public StringAttributeValue(StringAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getStringValue();
    }
    
    public Type getType()
    {
        return Type.STRING;
    }

    public Serializable getRawValue()
    {
        return fData;
    }

    @Override
    public String getStringValue()
    {
        return fData;
    }

    @Override
    public void setStringValue(String value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return fData;
    }
}
