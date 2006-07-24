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
