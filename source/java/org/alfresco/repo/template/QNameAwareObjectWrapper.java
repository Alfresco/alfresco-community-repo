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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.template;

import java.util.Map;

import org.alfresco.service.namespace.QNameMap;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Kevin Roast
 */
public class QNameAwareObjectWrapper extends DefaultObjectWrapper
{
    /**
     * Override to support wrapping of a QNameNodeMap by our custom wrapper object
     */
    public TemplateModel wrap(Object obj) throws TemplateModelException
    {
        if (obj instanceof QNameMap)
        {
            return new QNameHash((QNameMap)obj, this);
        }
        else
        {
            return super.wrap(obj);
        }
    }
    
    
    /**
     * Inner class to support clone of QNameNodeMap
     */
    class QNameHash extends SimpleHash
    {
        /**
         * Constructor
         * 
         * @param map
         * @param wrapper
         */
        public QNameHash(QNameMap map, ObjectWrapper wrapper)
        {
            super(map, wrapper);
        }
        
        /**
         * Override to support clone of a QNameNodeMap object
         */
        protected Map copyMap(Map map)
        {
            if (map instanceof QNameMap)
            {
                return (Map)((QNameMap)map).clone();
            }
            else
            {
                return super.copyMap(map);
            }
        }
    }
}
