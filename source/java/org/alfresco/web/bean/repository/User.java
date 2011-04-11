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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Bean that represents the currently logged in user
 * 
 * @author gavinc
 */
public final class User implements SessionUser
{
   private static final long serialVersionUID = -90577901805847829L;

   private String companyRootId;   
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
         String firstName = (String)service.getProperty(this.person, ContentModel.PROP_FIRSTNAME);
         String lastName = (String)service.getProperty(this.person, ContentModel.PROP_LASTNAME);
         this.fullName = (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : "");
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
    * @return Retrieves the company home space
    */
   public String getCompanyRootId()
   {
      return this.companyRootId;
   }

   /**
    * @param companyRootId Sets the id of the company home space
    */
   public void setCompanyRootId(String companyRootId)
   {
      this.companyRootId = companyRootId;
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
   Preferences getPreferences(FacesContext fc)
   {
      if (this.preferences == null)
      {
         this.preferences = new Preferences(getUserPreferencesRef(
               FacesContextUtils.getRequiredWebApplicationContext(fc)));
      }
      return this.preferences;
   }
   
   /**
    * @return The Preferences for the User
    */
   Preferences getPreferences(ServletContext sc)
   {
      if (this.preferences == null)
      {
         this.preferences = new Preferences(getUserPreferencesRef(
               WebApplicationContextUtils.getRequiredWebApplicationContext(sc)));
      }
      return this.preferences;
   }
   
   /**
    * Get or create the node used to store user preferences.
    * Utilises the 'configurable' aspect on the Person linked to this user.
    */
   synchronized NodeRef getUserPreferencesRef(WebApplicationContext context)
   {
        final ServiceRegistry registry = (ServiceRegistry) context.getBean("ServiceRegistry");
        final NodeService nodeService = registry.getNodeService();
        final SearchService searchService = registry.getSearchService();
        final NamespaceService namespaceService = registry.getNamespaceService();
        final TransactionService txService = registry.getTransactionService();
        final ConfigurableService configurableService = (ConfigurableService) context.getBean("ConfigurableService");
        RetryingTransactionHelper txnHelper = registry.getRetryingTransactionHelper();
        return txnHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {

            public NodeRef execute() throws Throwable
            {
                NodeRef prefRef = null;
                NodeRef person = getPerson();
                if (nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE) == false)
                {
                    // if the repository is in read-only mode just return null
                    if (txService.isReadOnly())
                    {
                        return null;
                    }
                    else
                    {
                        // create the configuration folder for this Person node
                        configurableService.makeConfigurable(person);
                    }
                }

                // target of the assoc is the configurations folder ref
                NodeRef configRef = configurableService.getConfigurationFolder(person);
                if (configRef == null)
                {
                    throw new IllegalStateException("Unable to find associated 'configurations' folder for node: "
                            + person);
                }

                String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
                List<NodeRef> nodes = searchService.selectNodes(configRef, xpath, null, namespaceService, false);

                if (nodes.size() == 1)
                {
                    prefRef = nodes.get(0);
                }
                else
                {
                    // create the preferences Node for this user (if repo is not read-only)
                    if (txService.isReadOnly() == false)
                    {
                        ChildAssociationRef childRef = nodeService.createNode(configRef,
                                    ContentModel.ASSOC_CONTAINS, QName.createQName(
                                    NamespaceService.APP_MODEL_1_0_URI, "preferences"), 
                                    ContentModel.TYPE_CMOBJECT);

                       prefRef = childRef.getChildRef();
                    }
                }
                return prefRef;
            }
        });
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
   
   /**
    * Returns the full name of the user plus their userid in the form [id]
    * 
    * @param nodeService The node service instance
    * @param user The user to get the full name for
    * @return The full name and userid
    */
   public static String getFullNameAndUserId(NodeService nodeService, NodeRef user)
   {
      String fullName = getFullName(nodeService, user);
      String userId = (String)nodeService.getProperties(user).get(ContentModel.PROP_USERNAME);

      StringBuilder nameAndId = new StringBuilder();
      if (fullName != null && fullName.length() > 0 && fullName.equals("null") == false)
      {
         nameAndId.append(fullName);
         nameAndId.append(" ");
      }
      
      nameAndId.append("[");
      nameAndId.append(userId);
      nameAndId.append("]");
      
      return nameAndId.toString();
   }
}
