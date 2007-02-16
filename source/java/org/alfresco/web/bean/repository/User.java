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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;

/**
 * Bean that represents the currently logged in user
 * 
 * @author gavinc
 */
public final class User
{
   private String homeSpaceId;
   private String userName;
   private String ticket;
   private NodeRef person;
   private String fullName = null;
   private Boolean administrator = null;
   
   private Preferences preferences = null;
   
   /**
    * Constructor
    * 
    * @param userName constructor for the user
    */
   public User(String userName, String ticket, NodeRef person)
   {
      if (userName == null || ticket == null || person == null)
      {
         throw new IllegalArgumentException("All user details are mandatory!");
      }
      
      this.userName = userName;  
      this.ticket = ticket;
      this.person = person;
   }
   
   /**
    * Forces a clear of any cached or calcluated values
    */
   public void reset()
   {
      this.fullName = null;
      this.administrator = null;
      this.preferences = null;
   }
   
   /**
    * @return The user name
    */
   public String getUserName()
   {
      return this.userName;
   }
   
   /**
    * Return the full name of the Person this User represents 
    * 
    * @param service        NodeService to use
    * 
    * @return The full name
    */
   public String getFullName(NodeService service)
   {
      if (this.fullName == null)
      {
         String lastName = (String)service.getProperty(this.person, ContentModel.PROP_LASTNAME);
         this.fullName = service.getProperty(this.person, ContentModel.PROP_FIRSTNAME) +
                         (lastName != null ? (" " + lastName) : "");
      }
      
      return this.fullName;
   }
   
   /**
    * @return Retrieves the user's home space (this may be the id of the company home space)
    */
   public String getHomeSpaceId()
   {
      return this.homeSpaceId;
   }

   /**
    * @param homeSpaceId Sets the id of the users home space
    */
   public void setHomeSpaceId(String homeSpaceId)
   {
      this.homeSpaceId = homeSpaceId;
   }

   /**
    * @return Returns the ticket.
    */
   public String getTicket()
   {
      return this.ticket;
   }
   
   /**
    * @return Returns the person NodeRef
    */
   public NodeRef getPerson()
   {
      return this.person;
   }
   
   /**
    * @return If the current user has Admin Authority
    */
   public boolean isAdmin()
   {
      if (administrator == null)
      {
         administrator = Repository.getServiceRegistry(FacesContext.getCurrentInstance())
               .getAuthorityService().hasAdminAuthority();
      }
      
      return administrator;
   }
   
   /**
    * @return The Preferences for the User
    */
   Preferences getPreferences()
   {
      if (this.preferences == null)
      {
         this.preferences = new Preferences(getUserPreferencesRef());
      }
      return this.preferences;
   }
   
   /**
    * Get or create the node used to store user preferences.
    * Utilises the 'configurable' aspect on the Person linked to this user.
    */
   synchronized NodeRef getUserPreferencesRef()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ServiceRegistry registry = Repository.getServiceRegistry(fc);
      NodeService nodeService = registry.getNodeService();
      SearchService searchService = registry.getSearchService();
      NamespaceService namespaceService = registry.getNamespaceService();
      ConfigurableService configurableService = Repository.getConfigurableService(fc);
      
      NodeRef person = Application.getCurrentUser(fc).getPerson();
      if (nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE) == false)
      {
         // create the configuration folder for this Person node
     	   configurableService.makeConfigurable(person);
      }
      
      // target of the assoc is the configurations folder ref
      NodeRef configRef = configurableService.getConfigurationFolder(person);
      if (configRef == null)
      {
         throw new IllegalStateException("Unable to find associated 'configurations' folder for node: " + person);
      }
      
      String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
      List<NodeRef> nodes = searchService.selectNodes(
            configRef,
            xpath,
            null,
            namespaceService,
            false);
      
      NodeRef prefRef;
      if (nodes.size() == 1)
      {
         prefRef = nodes.get(0);
      }
      else
      {
         // create the preferences Node for this user
         ChildAssociationRef childRef = nodeService.createNode(
               configRef,
               ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "preferences"),
               ContentModel.TYPE_CMOBJECT);
         
         prefRef = childRef.getChildRef();
      }
      
      return prefRef;
   }
   
   /**
    * Returns the full name of the user represented by the given NodeRef
    * 
    * @param nodeService The node service instance
    * @param user The user to get the full name for
    * @return The full name
    */
   public static String getFullName(NodeService nodeService, NodeRef user)
   {
      Map<QName, Serializable> props = nodeService.getProperties(user);
      String firstName = (String)props.get(ContentModel.PROP_FIRSTNAME);
      String lastName = (String)props.get(ContentModel.PROP_LASTNAME);
      String fullName = firstName + ((lastName != null && lastName.length() > 0) ? " " + lastName : "");
      
      return fullName;
   }
}
