/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.tagging.script;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Script object representing the tagging service.
 * 
 * @author Roy Wetherall
 */
public class ScriptTaggingService extends BaseScopableProcessorExtension
{
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
    /**
     * Sets the Service Registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }

    /**
     * Get all the tags available in a store
     * 
     * @param store     store reference
     * @return String[] tag names
     */
    public String[] getTags(String store)
    {
        StoreRef storeRef = new StoreRef(store);
        List<String> result = this.serviceRegistry.getTaggingService().getTags(storeRef);
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * Get all the tags available in a store based on a text filter
     * 
     * @param store     store reference
     * @param filter    tag filter
     * @return String[] tag names
     */
    public String[] getTags(String store, String filter)
    {
        StoreRef storeRef = new StoreRef(store);
        List<String> result = this.serviceRegistry.getTaggingService().getTags(storeRef, filter);
        return (String[])result.toArray(new String[result.size()]);
    }

    /**
     * Get a tag by name if available in a store
     * 
     * @param store     store reference
     * @param tag       tag name
     * @return ScriptNode   tag node, or null if not found
     */
    public ScriptNode getTag(String store, String tag)
    {
        StoreRef storeRef = new StoreRef(store);
        NodeRef result = this.serviceRegistry.getTaggingService().getTagNodeRef(storeRef, tag);
        if (result != null)
        {
            return new ScriptNode(result, this.serviceRegistry, this.getScope());
        }
        return null;
    }

    /**
     * Create a tag in a given store
     * 
     * @param store     store reference
     * @param tag       tag name
     * @return ScriptNode   newly created tag node, or null if unable to create
     */
    public ScriptNode createTag(String store, String tag)
    {
        StoreRef storeRef = new StoreRef(store);
        NodeRef result = this.serviceRegistry.getTaggingService().createTag(storeRef, tag);
        if (result != null)
        {
            return new ScriptNode(result, this.serviceRegistry);
        }
        return null;
    }
    
    /**
     * delete tag at the given store
     * 
     * @param store     store reference
     * @param tag       tag name
     */
    public void deleteTag(String store, String tag)
    {
        StoreRef storeRef = new StoreRef(store);
        this.serviceRegistry.getTaggingService().deleteTag(storeRef, tag);
    }
}
