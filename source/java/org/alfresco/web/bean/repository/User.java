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

import java.util.List;

import javax.faces.context.FacesContext;

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
   
   /** cached ref to our user preferences node */
   private NodeRef preferencesFolderRef = null;
   
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
    * Get or create the node used to store user preferences.
    * Utilises the 'configurable' aspect on the Person linked to this user.
    */
   public synchronized NodeRef getUserPreferencesRef()
   {
      if (this.preferencesFolderRef == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         ServiceRegistry registry = Repository.getServiceRegistry(fc);
         NodeService nodeService = registry.getNodeService();
         SearchService searchService = registry.getSearchService();
         NamespaceService namespaceService = registry.getNamespaceService();
         ConfigurableService configurableService = Repository.getConfigurableService(fc);
         
         NodeRef person = Application.getCurrentUser(fc).getPerson();
         if (nodeService.hasAspect(person, ContentModel.ASPECT_CONFIGURABLE) == false)
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
         
         this.preferencesFolderRef = prefRef;
      }
      
      return this.preferencesFolderRef;
   }
}
