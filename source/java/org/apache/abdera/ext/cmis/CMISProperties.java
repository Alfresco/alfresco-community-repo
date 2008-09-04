/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.apache.abdera.ext.cmis;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ElementWrapper;


/**
 * CMIS Properties Element Wrapper for the Abdera ATOM library.
 * 
 * Encapsulates access and modification of CMIS extension values to ATOM.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public class CMISProperties extends ElementWrapper
{
    /**
     * @param internal
     */
    public CMISProperties(Element internal)
    {
        super(internal);
    }

    /**
     * @param factory
     */
    public CMISProperties(Factory factory)
    {
        super(factory, CMISConstants.PROPERTIES);
    }

    /**
     * Gets all property names
     * 
     * @return  list of property names
     */
    public List<String> getNames()
    {
        List<CMISProperty> props = getElements();
        List<String> names = new ArrayList<String>(props.size());
        for (CMISProperty prop : props)
        {
            names.add(prop.getName());
        }
        return names;
    }
    
    /**
     * Finds property by name
     * 
     * @param name  property name
     * @return  property
     */
    public CMISProperty find(String name)
    {
        List<Element> elements = getElements();
        for (Element element : elements)
        {
            if (element instanceof CMISProperty)
            {
                CMISProperty prop = (CMISProperty)element;
                if (prop.getName().equals(name))
                {
                    return prop;
                }
            }
        }
        return null;
    }
        
}
