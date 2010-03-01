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

import org.alfresco.repo.avm.AVMDAOs;

/**
 * Persistent implementation of float attribute.
 * @author britt
 */
public class FloatAttributeImpl extends AttributeImpl implements FloatAttribute
{
    private static final long serialVersionUID = 8173803953645298153L;

    private float fValue;
    
    public FloatAttributeImpl()
    {
    }
    
    public FloatAttributeImpl(float value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public FloatAttributeImpl(FloatAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getFloatValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public Type getType()
    {
        return Type.FLOAT;
    }

    public Serializable getRawValue()
    {
        return Float.valueOf(fValue);
    }

    @Override
    public float getFloatValue()
    {
        return fValue;
    }

    @Override
    public void setFloatValue(float value)
    {
        fValue = value;
    }

    @Override
    public String toString()
    {
        return Float.toString(fValue);
    }
}
