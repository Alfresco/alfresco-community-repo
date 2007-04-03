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

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author britt
 *
 */
public class MapAttributeImpl extends AttributeImpl implements MapAttribute
{
    public MapAttributeImpl()
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.MAP;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#clear()
     */
    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
        super.clear();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#entrySet()
     */
    @Override
    public Set<Entry<String, Attribute>> entrySet()
    {
        // TODO Auto-generated method stub
        return super.entrySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#get(java.lang.String)
     */
    @Override
    public Attribute get(String key)
    {
        // TODO Auto-generated method stub
        return super.get(key);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#keySet()
     */
    @Override
    public Set<String> keySet()
    {
        // TODO Auto-generated method stub
        return super.keySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#put(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void put(String key, Attribute value)
    {
        // TODO Auto-generated method stub
        super.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#remove(java.lang.String)
     */
    @Override
    public void remove(String key)
    {
        // TODO Auto-generated method stub
        super.remove(key);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#values()
     */
    @Override
    public Collection<Attribute> values()
    {
        // TODO Auto-generated method stub
        return super.values();
    }
}
