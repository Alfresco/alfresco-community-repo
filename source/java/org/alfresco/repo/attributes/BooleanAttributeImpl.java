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
 * @author britt
 *
 */
public class BooleanAttributeImpl extends AttributeImpl implements
        BooleanAttribute 
{
    private static final long serialVersionUID = 8483440613101900682L;

    private boolean fValue;

    public BooleanAttributeImpl()
    {
    }

    public BooleanAttributeImpl(boolean value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public BooleanAttributeImpl(BooleanAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getBooleanValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    @Override
    public boolean getBooleanValue() 
    {
        return fValue;
    }
    
    @Override
    public void setBooleanValue(boolean value) 
    {
        fValue = value;
    }

    public Type getType() 
    {
        return Type.BOOLEAN;
    }

    public Serializable getRawValue()
    {
        return Boolean.valueOf(fValue);
    }

    @Override
    public String toString()
    {
        return fValue ? "true" : "false";
    }
}
