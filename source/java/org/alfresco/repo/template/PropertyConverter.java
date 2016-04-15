/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.ArrayList;
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
            List<Serializable> result = new ArrayList<Serializable>(list.size());
            for (int i=0; i<list.size(); i++)
            {
                // add each item to a new list as the repo can return unmodifiable lists
                result.add(convertProperty(list.get(i), name, services, resolver));
            }
            value = (Serializable)result;
        }
        
        return value;
    }
}