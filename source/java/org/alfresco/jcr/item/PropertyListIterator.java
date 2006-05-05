/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;


/**
 * Alfresco implementation of a Property Iterator
 * 
 * @author David Caruana
 */
public class PropertyListIterator extends AbstractRangeIterator
    implements PropertyIterator
{
    private List<PropertyImpl> properties;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param properties  property list
     */
    public PropertyListIterator(List<PropertyImpl> properties)
    {
        this.properties = properties;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.PropertyIterator#nextProperty()
     */
    public Property nextProperty()
    {
        long position = skip();
        PropertyImpl propertyImpl = properties.get((int)position);
        return propertyImpl.getProxy();
    }

    
    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return properties.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextProperty();
    }

}
