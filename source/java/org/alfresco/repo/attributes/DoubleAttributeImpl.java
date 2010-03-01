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

import org.alfresco.repo.avm.AVMDAOs;

/**
 * Persistent double attribute implementation.
 * @author britt
 */
public class DoubleAttributeImpl extends AttributeImpl implements DoubleAttribute
{
    private static final long serialVersionUID = 6615023606094278263L;

    private double fValue;
    
    public DoubleAttributeImpl()
    {
    }

    public DoubleAttributeImpl(double value)
    {
        fValue = value;   
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public DoubleAttributeImpl(DoubleAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getDoubleValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public Type getType()
    {
        return Type.DOUBLE;
    }

    public Serializable getRawValue()
    {
        return Double.valueOf(fValue);
    }

    @Override
    public double getDoubleValue()
    {
        return fValue;
    }

    @Override
    public void setDoubleValue(double value)
    {
        fValue = value;
    }

    @Override
    public String toString()
    {
        return Double.toString(fValue);
    }
}
