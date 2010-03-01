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
 * A short attribute.
 * @author britt
 */
public class ShortAttributeImpl extends AttributeImpl implements ShortAttribute
{
    private static final long serialVersionUID = 583269625932011762L;

    private short fValue;
    
    public ShortAttributeImpl()
    {
    }
    
    public ShortAttributeImpl(short value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public ShortAttributeImpl(ShortAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getShortValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    @Override
    public short getShortValue()
    {
        return fValue;
    }

    @Override
    public void setShortValue(short value)
    {
        fValue = value;
    }

    public Type getType()
    {
        return Type.SHORT;
    }

    public Serializable getRawValue()
    {
        return Short.valueOf(fValue);
    }

    @Override
    public String toString()
    {
        return Short.toString(fValue);
    }
}
