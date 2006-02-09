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
package org.alfresco.repo.template;

import java.util.HashMap;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateNode;

/**
 * An abstract Map class that can be used process the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public abstract class BaseTemplateMap extends HashMap implements Cloneable
{
    protected TemplateNode parent;
    protected ServiceRegistry services = null;
    
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public BaseTemplateMap(TemplateNode parent, ServiceRegistry services)
    {
        super(1, 1.0f);
        this.services = services;
        this.parent = parent;
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public abstract Object get(Object key);
}
