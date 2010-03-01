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
 * An integer attribute.
 * @author britt
 */
public class IntAttributeImpl extends AttributeImpl implements IntAttribute
{
    private static final long serialVersionUID = -7721943599145354015L;

    private int fValue;
    
    public IntAttributeImpl()
    {
    }
    
    public IntAttributeImpl(int value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public IntAttributeImpl(IntAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getIntValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public Type getType()
    {
        return Type.INT;
    }

    public Serializable getRawValue()
    {
        return Integer.valueOf(fValue);
    }

    @Override
    public int getIntValue()
    {
        return fValue;
    }

    @Override
    public void setIntValue(int value)
    {
        fValue = value;
    }

    @Override
    public String toString()
    {
        return Integer.toString(fValue);
    }
}
