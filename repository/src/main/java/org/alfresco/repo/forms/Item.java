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
package org.alfresco.repo.forms;

import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Represents an item a form is generated for.
 * <p>This class can be augmented with the item's type and a 
 * representational URL by the form processor used to process
 * the item.</p> 
 * 
 * @author Gavin Cornwell
 */
public class Item
{
    protected String kind;
    protected String id;
    protected String type;
    protected String url;
    
    /**
     * Constructs an item.
     * 
     * @param kind The kind of item, for example, 'node', 'task'
     * @param id The identifier of the item
     */
    public Item(String kind, String id)
    {
        ParameterCheck.mandatoryString("kind", kind);
        ParameterCheck.mandatoryString("id", id);
       
        this.kind = kind;
        this.id = id;
    }

    /**
     * Returns the kind of item.
     * 
     * @return The kind of item
     */
    public String getKind()
    {
        return this.kind;
    }

    /**
     * Returns the identifier of the item
     * 
     * @return The identifier of the item
     */
    public String getId()
    {
        return this.id;
    }
    
    /**
     * Returns the type of the item the form is for, could be a content model type, a
     * workflow task type, an XML schema etc.
     * 
     * @return The type of the item
     */
    public String getType()
    {
        return this.type;
    }
    
    /**
     * Returns a URL that represents the item
     * 
     * @return A URL representing the item
     */
    public String getUrl()
    {
        return this.url;
    }
    
    /**
     * Sets the type of the item
     * 
     * @param type The type
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Sets the URL that represents the item
     * 
     * @param url The URL
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("[");
        builder.append(this.kind).append("]").append(this.id);
        
        if (this.type != null)
        {
            builder.append(", type=").append(this.type);
        }
        if (this.url != null)
        {
            builder.append(", url=").append(this.url);
        }
        
        return builder.toString();
    }
}
