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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;

/**
 * Simple class to recursively convert property values to bean objects accessable by Templates.
 * 
 * @author Kevin Roast
 */
public class PropertyConverter
{
    public Serializable convertProperty(
            Serializable value, QName name, ServiceRegistry services, TemplateImageResolver resolver)
    {
        // perform conversions from Java objects to template compatable instances
        if (value == null)
        {
            return null;
        }
        else if (value instanceof NodeRef)
        {
            // NodeRef object properties are converted to new TemplateNode objects
            // so they can be used as objects within a template
            value = new TemplateNode(((NodeRef)value), services, resolver);
        }
        else if (value instanceof List)
        {
            // recursively convert each value in the collection
            List<Serializable> list = (List<Serializable>)value;
            for (int i=0; i<list.size(); i++)
            {
                list.set(i, convertProperty(list.get(i), name, services, resolver));
            }
        }
        
        return value;
    }
}
