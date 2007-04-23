/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.SERIALIZABLE;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#getSerializableValue()
     */
    @Override
    public Serializable getSerializableValue()
    {
        return fValue;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#setSerializableValue(java.io.Serializable)
     */
    @Override
    public void setSerializableValue(Serializable value)
    {
        fValue = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fValue.toString();
    }
}
