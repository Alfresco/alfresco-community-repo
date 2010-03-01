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
 * Persistent implemantation of a Serializable attribute.
 * @author britt
 */
public class SerializableAttributeImpl extends AttributeImpl implements
        SerializableAttribute
{
    private static final long serialVersionUID = -4499585229435904817L;

    private Serializable fValue;
    
    public SerializableAttributeImpl()
    {
    }
    
    public SerializableAttributeImpl(Serializable value)
    {
        fValue = value;
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    public SerializableAttributeImpl(SerializableAttribute attr)
    {
        super(attr.getAcl());
        fValue = attr.getSerializableValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);        
    }
    
    public Type getType()
    {
        return Type.SERIALIZABLE;
    }

    public Serializable getRawValue()
    {
        return fValue;
    }

    @Override
    public Serializable getSerializableValue()
    {
        return fValue;
    }

    @Override
    public void setSerializableValue(Serializable value)
    {
        fValue = value;
    }

    @Override
    public String toString()
    {
        return fValue.toString();
    }
}
