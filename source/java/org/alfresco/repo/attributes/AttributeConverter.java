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
 * Handles conversions between persistent and value based Attributes.
 * 
 * @see Attribute.Type#getAttributeImpl(Attribute)
 * @see Attribute.Type#getAttributeValue(Attribute)
 * 
 * @author britt
 */
public class AttributeConverter
{
    /**
     * Convert an Attribute (recursively) to a persistent attribute. This persists
     * the newly created Attribute immediately.
     * @param from The Attribute to clone.
     * @return The cloned persistent Attribute.
     */
    public Attribute toPersistent(Attribute from)
    {
        AttributeImpl attributeEntity = from.getAttributeImpl();
        // Done
        return attributeEntity;
    }

    public Attribute toValue(Attribute from)
    {
        AttributeValue attributeValue = from.getAttributeValue();
        AVMDAOs.Instance().fAttributeDAO.evictFlat(from);
        // Done
        return attributeValue;
    }
}
