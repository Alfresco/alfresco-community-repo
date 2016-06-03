/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.jacksonextensions;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the result of 1 or more executions.  This object will be rendered as JSON
 *
 * @author Gethin James
 */
public class ExecutionResult
{
    private final Object root;
    private boolean anEmbeddedEntity;
    private final Map<String,Object> embedded = new HashMap<String,Object>();
    private final Map<String,Object> related = new HashMap<String,Object>();
    private final BeanPropertiesFilter filter;
    
    public ExecutionResult(Object root,BeanPropertiesFilter filter)
    {
        super();
        this.root = root;
        this.filter = filter;
        this.anEmbeddedEntity = false;
    }

    /**
     * @return the filter
     */
    public BeanPropertiesFilter getFilter()
    {
        return this.filter;
    }
    
    public Object getRoot()
    {
        return this.root;
    }
    
    /**
     * Adds embeddeds object to the enclosing root object
     * @param embedded objects to add
     */
    public void addEmbedded(Map<String,Object> embedded)
    {
        this.embedded.putAll(embedded);
    }
    
    /**
     * Adds related object to the enclosing root object
     * @param related objects to add
     */
    public void addRelated(Map<String,Object> related)
    {
        this.related.putAll(related);
    }

    /**
     * Is this object and embedded entity
     * 
     * @return boolean - true if it is embedded, defaults to false
     */
    public boolean isAnEmbeddedEntity()
    {
        return this.anEmbeddedEntity;
    }
    
    /**
     * Is this object and embedded entity
     * 
     * @param anEmbeddedEntity - true if it is embedded, defaults to false
     */   
    public void setAnEmbeddedEntity(boolean anEmbeddedEntity)
    {
        this.anEmbeddedEntity = anEmbeddedEntity;
    }

    
    /**
     * Returns the Map of related objects
     * 
     * @return Map
     */
    public Map<String, Object> getRelated()
    {
        return this.related;
    }

    /**
     * Returns the Map of embedded objects
     * 
     * @return Map
     */
    public Map<String, Object> getEmbedded()
    {
        return this.embedded;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ExecutionResult [root=");
        builder.append(this.root);
        builder.append(", anEmbeddedEntity=");
        builder.append(this.anEmbeddedEntity);
        builder.append(", embedded=");
        builder.append(this.embedded);
        builder.append(", related=");
        builder.append(this.related);
        builder.append(", filter=");
        builder.append(this.filter);
        builder.append("]");
        return builder.toString();
    }

 

}
