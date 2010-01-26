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
            return new ScriptNode(result, this.serviceRegistry);
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
}
