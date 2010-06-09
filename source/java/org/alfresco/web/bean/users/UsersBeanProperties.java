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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Bean holding the properties for a Person node.
 * 
 * Used by the Create/EditUserWizard, UsersDialog and EditUserDetailsDetails to maitain user state during dialog.
 * 
 * Also provides access to a map of the properties to mutability which is used to disable appropriate controls on edit
 * dialogs if properties are externally mapped to LDAP or similar.
 */
public class UsersBeanProperties implements Serializable
{
   private static final long serialVersionUID = 8874192805959149144L;
   
   /** NodeService bean reference */
   transient private NodeService nodeService;
   
   /** SearchService bean reference */
   transient private SearchService searchService;
   
   /** AuthenticationService bean reference */
   transient private MutableAuthenticationService authenticationService;
   
   /** PersonService bean reference */
   transient private PersonService personService;
   
   /** ContentUsageService bean reference */
   transient private ContentUsageService contentUsageService;
   
   /** userRegistrySynchronizer bean reference */
   transient private UserRegistrySynchronizer userRegistrySynchronizer;
   
   /** Component reference for Users RichList control */
   private UIRichList usersRichList;
   
   /** action context */
   private Node person = null;
   private String password = null;
   private String oldPassword = null;
   private String confirm = null;
   private String searchCriteria = null;
   private String userName = null;
   private Map<String, Boolean> immutabilty = null;
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @return the nodeService
    */
   public NodeService getNodeService()
   {
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return nodeService;
   }

   /**
    * @return the searchService
    */
   public SearchService getSearchService()
   {
      if (searchService == null)
      {
         searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      }
      return searchService;
   }

   /**
    * @return the authenticationService
    */
   public MutableAuthenticationService getAuthenticationService()
   {
      if (authenticationService == null)
      {
         authenticationService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
      }
      return authenticationService;
   }

   /**
    * @return the personService
    */
   public PersonService getPersonService()
   {
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
   }

   /**
    * @return contentUsageService
    */
   public ContentUsageService getContentUsageService()
   {
      if (contentUsageService == null)
      {
         contentUsageService = (ContentUsageService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ContentUsageService");
      }
      return contentUsageService;
   }
   
   /**
    * @return userRegistrySynchronizer
    */
   public UserRegistrySynchronizer getUserRegistrySynchronizer()
   {
      if (userRegistrySynchronizer == null)
      {
         userRegistrySynchronizer = (UserRegistrySynchronizer)FacesContextUtils.getRequiredWebApplicationContext(
               FacesContext.getCurrentInstance()).getBean("userRegistrySynchronizer");
      }
      return userRegistrySynchronizer;
   }
   
   /**
    * @param nodeService        The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param searchServiceq     the search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * @param authenticationService  The AuthenticationService to set.
    */
   public void setAuthenticationService(MutableAuthenticationService authenticationService)
   {
      this.authenticationService = authenticationService;
   }

   /**
    * @param personService      The PersonService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }

   /**
    * @param contentUsageService    The ContentUsageService to set.
    */
   public void setContentUsageService(ContentUsageService contentUsageService)
   {
      this.contentUsageService = contentUsageService;
   }
   
   /**
    * @param userRegistrySynchronizer
    */
   public void setUserRegistrySynchronizer(UserRegistrySynchronizer userRegistrySynchronizer)
   {
      this.userRegistrySynchronizer = userRegistrySynchronizer;
   }

   /**
    * @return Returns the usersRichList.
    */
   public UIRichList getUsersRichList()
   {
      return this.usersRichList;
   }

   /**
    * @param usersRichList      The usersRichList to set.
    */
   public void setUsersRichList(UIRichList usersRichList)
   {
      this.usersRichList = usersRichList;
   }

   /**
    * @return Returns the search criteria
    */
   public String getSearchCriteria()
   {
      return searchCriteria;
   }

   /**
    * @param searchCriteria     The search criteria to select
    */
   public void setSearchCriteria(String searchCriteria)
   {
      this.searchCriteria = searchCriteria;
   }

   /**
    * @return Returns the confirm password.
    */
   public String getConfirm()
   {
      return this.confirm;
   }

   /**
    * @param confirm        The confirm password to set.
    */
   public void setConfirm(String confirm)
   {
      this.confirm = confirm;
   }

   /**
    * @return Returns the password.
    */
   public String getPassword()
   {
      return this.password;
   }

   /**
    * @param password       The password to set.
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * @return Returns the old password.
    */
   public String getOldPassword()
   {
      return this.oldPassword;
   }

   /**
    * @param oldPassword    The old password to set.
    */
   public void setOldPassword(String oldPassword)
   {
      this.oldPassword = oldPassword;
   }

   /**
    * @return Returns the person context.
    */
   public Node getPerson()
   {
      return this.person;
   }

   /**
    * @param person         The person context to set.
    */
   public void setPerson(final Node p)
   {
      // perform the set in a txn as certain bean calls require it
      FacesContext context = FacesContext.getCurrentInstance();
      RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
      RetryingTransactionCallback callback = new RetryingTransactionCallback()
      {
         public Object execute() throws Throwable
         {
            person = p;
            userName = (String)person.getProperties().get(ContentModel.PROP_USERNAME);
            
            // rebuild the property immutability map helper object
            immutabilty = new PropertyImmutabilityMap(
                  getUserRegistrySynchronizer().getPersonMappedProperties(userName));
            
            return null;
         }
      };
      try
      {
         txnHelper.doInTransaction(callback, false);
      }
      catch (Throwable e)
      {
         // reset the flag so we can re-attempt the operation
         if (e instanceof ReportedException == false)
         {
            Utils.addErrorMessage(e.getMessage(), e);
         }
         ReportedException.throwIfNecessary(e);
      }
   }
   
   public Long getUserUsage(String userName)
   {
      long usage = getContentUsageService().getUserUsage(userName);
      return (usage == -1 ? null : usage);
   }

   public Long getUserUsage()
   {
      long usage = getContentUsageService().getUserUsage(this.userName);
      return (usage == -1 ? null : usage);
   }

   public Long getUserQuota()
   {
      long quota = getContentUsageService().getUserQuota(this.userName);
      return (quota == -1 ? null : quota);
   }

   public boolean getUsagesEnabled()
   {
      return getContentUsageService().getEnabled();
   }

   public String getPersonDescription()
   {
      ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
      ContentReader reader = cs.getReader(this.person.getNodeRef(), ContentModel.PROP_PERSONDESC);
      if (reader != null && reader.exists())
      {
         return Utils.stripUnsafeHTMLTags(reader.getContentString()).replace("\r\n", "<p>");
      }
      else
      {
         return null;
      }
   }

   public String getAvatarUrl()
   {
      String avatarUrl = null;

      List<AssociationRef> refs = getNodeService().getTargetAssocs(this.person.getNodeRef(), ContentModel.ASSOC_AVATAR);
      if (refs.size() == 1)
      {
         NodeRef photoRef = refs.get(0).getTargetRef();
         String name = (String) getNodeService().getProperty(photoRef, ContentModel.PROP_NAME);
         avatarUrl = DownloadContentServlet.generateBrowserURL(photoRef, name);
      }

      return avatarUrl;
   }
   
   public Map<String, Boolean> getImmutability()
   {
      return this.immutabilty;
   }
   
   /**
    * Map of person property to immutability
    * The Map interface is implemented to allow JSF expressions such as
    * #{DialogBean.bean.properties.immutability.propertyname}
    */
   public class PropertyImmutabilityMap implements Map<String, Boolean>, Serializable
   {
      final private Set<QName> props;
      
      PropertyImmutabilityMap(Set<QName> props)
      {
         this.props = props;
      }
      
      public void clear()
      {
      }

      public boolean containsKey(Object k)
      {
         boolean contains = false;
         if (k instanceof String && ((String)k).length() != 0)
         {
            String s = (String)k;
            if (s.charAt(0) == '{' && s.indexOf('}') != -1)
            {
               contains = this.props.contains(k);
            }
            else
            {
               // simple property name - assume and apply CM namespace
               contains = this.props.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, s));
            }
         }
         return contains;
      }

      public boolean containsValue(Object v)
      {
         return false;
      }

      public Set<Entry<String, Boolean>> entrySet()
      {
         return null;
      }

      public Boolean get(Object k)
      {
         return containsKey(k);
      }

      public boolean isEmpty()
      {
         return this.props.size() != 0;
      }

      public Set<String> keySet()
      {
         return null;
      }

      public Boolean put(String k, Boolean v)
      {
         return null;
      }

      public void putAll(Map<? extends String, ? extends Boolean> m)
      {
      }

      public Boolean remove(Object k)
      {
         return null;
      }

      public int size()
      {
         return this.props.size();
      }

      public Collection<Boolean> values()
      {
         return null;
      }
   }
}
