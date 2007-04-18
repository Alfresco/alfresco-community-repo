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
        fValue = attr.getFloatValue();
        AVMDAOs.Instance().fAttributeDAO.save(this);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.FLOAT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#getFloatValue()
     */
    @Override
    public float getFloatValue()
    {
        return fValue;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#setFloatValue(float)
     */
    @Override
    public void setFloatValue(float value)
    {
        fValue = value;
    }
}
