/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Wraps the notion of preferences and settings for a User.
 * Caches values until they are overwritten with a new value.
 * 
 * @author Kevin Roast
 */
public final class Preferences
{
   private NodeRef preferencesRef;
   private NodeService nodeService;
   private Map<String, Serializable> cache = new HashMap<String, Serializable>(16, 1.0f);
   
   /**
    * Package level constructor
    */
   Preferences(NodeRef prefRef)
   {
      if (prefRef == null)
      {
         throw new IllegalArgumentException("Preferences NodeRef cannot be null.");
      }
      this.preferencesRef = prefRef;
   }
   
   /**
    * Get a serialized preferences value.
    *  
    * @param name    Name of the value to retrieve.
    * 
    * @return The value or null if not found/set.
    */
   public Serializable getValue(String name)
   {
      Serializable value = this.cache.get(name);
      if (value == null)
      {
         QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
         value = getNodeService().getProperty(this.preferencesRef, qname);
         this.cache.put(name, value);
      }
      return value;
   }
   
   /**
    * Set a serialized preference value.
    * 
    * @param name    Name of the value to set.
    * @param value   Value to set.
    */
   public void setValue(String name, Serializable value)
   {
      QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
      // persist the property to the repo
      getNodeService().setProperty(this.preferencesRef, qname, value);
      // update the cache
      this.cache.put(name, value);
   }
   
   /**
    * @return the NodeService instance.
    */
   private NodeService getNodeService()
   {
      if (this.nodeService == null)
      {
         this.nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return this.nodeService;
   }
}
