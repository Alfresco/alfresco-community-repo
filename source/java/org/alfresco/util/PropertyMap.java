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
package org.alfresco.util;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.service.namespace.QName;

/**
 * Property map helper class.  
 * <p>
 * This class can be used as a short hand when a class of type
 * Map<QName, Serializable> is required.
 * 
 * @author Roy Wetherall
 */
public class PropertyMap extends HashMap<QName, Serializable>
{
    private static final long serialVersionUID = 8052326301073209645L;
    
    /**
     * @see HashMap#HashMap(int, float)
     */
    public PropertyMap(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }
    
    /**
     * @see HashMap#HashMap(int)
     */
    public PropertyMap(int initialCapacity)
    {
        super(initialCapacity);
    }
    
    /**
     * @see HashMap#HashMap()
     */
    public PropertyMap()
    {
        super();
    }
}
