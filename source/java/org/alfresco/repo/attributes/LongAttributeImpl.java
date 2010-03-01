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
 * Long valued attribute.
 * @author britt
 */
public class LongAttributeImpl extends AttributeImpl implements LongAttribute
{
    private static final long serialVersionUID = 1625735137563940631L;
    
    private long fValue;
    
    public LongAttributeImpl()
    {
    }
    
    public LongAttributeImpl(long value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public LongAttributeImpl(LongAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getLongValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public Type getType()
    {
        return Type.LONG;
    }

    public Serializable getRawValue()
    {
        return Long.valueOf(fValue);
    }

    @Override
    public long getLongValue()
    {
        return fValue;
    }

    @Override
    public void setLongValue(long value)
    {
       fValue = value;
    }

    @Override
    public String toString()
    {
        return Long.toString(fValue);
    }
}
