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
package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

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
public final class Preferences implements Serializable
{
   private static final long serialVersionUID = 722840612660970723L;
   
   private NodeRef preferencesRef;
   private transient NodeService nodeService;
   private Map<String, Serializable> cache = new HashMap<String, Serializable>(16, 1.0f);
   
   /**
    * Package level constructor
    */
   Preferences(NodeRef prefRef)
   {
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
      Serializable value = null;
      
      if (this.preferencesRef != null)
      {
         value = this.cache.get(name);
         
         if (value == null)
         {
            QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
            value = getNodeService().getProperty(this.preferencesRef, qname);
            this.cache.put(name, value);
         }
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
      if (this.preferencesRef != null)
      {
         QName qname = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);
      
         // persist the property to the repo
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
         
            getNodeService().setProperty(this.preferencesRef, qname, value);
         
            tx.commit();
         
            // update the cache
            this.cache.put(name, value);
         }
         catch (Throwable err)
         {
            // we cannot update the properties if a user is no longer authenticated
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
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
