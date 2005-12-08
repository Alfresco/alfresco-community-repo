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
package org.alfresco.web.app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.transaction.UserTransaction;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.portlet.AlfrescoFacesPortlet;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * ServletContextListener implementation that initialises the application.
 * 
 * NOTE: This class must appear after the Spring context loader listener
 * 
 * @author gavinc
 */
public class ContextListener implements ServletContextListener, HttpSessionListener
{
   private static Log logger = LogFactory.getLog(ContextListener.class);

   private static final String ADMIN = "admin";
   private static final String ADMIN_FIRSTNAME = "Repository";
   private static final String ADMIN_LASTNAME = "Administrator";

   private ServletContext servletContext;

   /**
    * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
    */
   public void contextInitialized(ServletContextEvent event)
   {
      // make sure that the spaces store in the repository exists
      this.servletContext = event.getServletContext();
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
      ServiceRegistry registry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
      TransactionService transactionService = registry.getTransactionService();
      NodeService nodeService = registry.getNodeService();
      SearchService searchService = registry.getSearchService();
      NamespaceService namespaceService = registry.getNamespaceService();
      AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx
            .getBean("authenticationComponent");

      // repo bootstrap code for our client
      UserTransaction tx = null;
      NodeRef companySpaceNodeRef = null;
      try
      {
         tx = transactionService.getUserTransaction();
         tx.begin();
         authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

         // get and setup the initial store ref from config
         StoreRef storeRef = Repository.getStoreRef(servletContext);

         // check the repository exists, create if it doesn't
         if (nodeService.exists(storeRef) == false)
         {
            throw new AlfrescoRuntimeException("Store not created prior to application startup: " + storeRef);
         }

         // get hold of the root node
         NodeRef rootNodeRef = nodeService.getRootNode(storeRef);

         // see if the company home space is present
         String rootPath = Application.getRootPath(servletContext);
         if (rootPath == null)
         {
            throw new AlfrescoRuntimeException("Root path has not been configured");
         }

         List<NodeRef> nodes = searchService.selectNodes(rootNodeRef, rootPath, null, namespaceService, false);
         if (nodes.size() == 0)
         {
            throw new AlfrescoRuntimeException("Root path not created prior to application startup: " + rootPath);
         }

         // Extract company space id and store it in the Application object
         companySpaceNodeRef = nodes.get(0);
         Application.setCompanyRootId(companySpaceNodeRef.getId());

         // check the admin user exists, create if it doesn't
         MutableAuthenticationDao dao = (MutableAuthenticationDao) ctx.getBean("alfDaoImpl");

         // this is required to setup the ACEGI context before we can check
         // for the user
         if (!dao.userExists(ADMIN))
         {
            ConfigService configService = (ConfigService) ctx.getBean(Application.BEAN_CONFIG_SERVICE);
            // default to password of "admin" if we don't find config for it
            String password = ADMIN;
            ConfigElement adminConfig = configService.getGlobalConfig().getConfigElement("admin");
            if (adminConfig != null)
            {
               List<ConfigElement> children = adminConfig.getChildren();
               if (children.size() != 0)
               {
                  // try to find the config element for the initial
                  // password
                  ConfigElement passElement = children.get(0);
                  if (passElement.getName().equals("initial-password"))
                  {
                     password = passElement.getValue();
                  }
               }
            }

            // create the Authentication instance for the "admin" user
            AuthenticationService authService = (AuthenticationService) ctx.getBean("authenticationService");
            authService.createAuthentication(ADMIN, password.toCharArray());
         }
         
         PersonService personService = (PersonService) ctx.getBean("personService");
         if (!personService.personExists(ADMIN))
         {
             // create the node to represent the Person instance for the
             // admin user
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(7, 1.0f);
            props.put(ContentModel.PROP_USERNAME, ADMIN);
            props.put(ContentModel.PROP_FIRSTNAME, ADMIN_FIRSTNAME);
            props.put(ContentModel.PROP_LASTNAME, ADMIN_LASTNAME);
            props.put(ContentModel.PROP_HOMEFOLDER, companySpaceNodeRef);
            props.put(ContentModel.PROP_EMAIL, "");
            props.put(ContentModel.PROP_ORGID, "");
          
            personService.createPerson(props);
         }
         // set the store's GUEST access if we are allowed to modify permissions
         if (!transactionService.isReadOnly())
         {
             PermissionService permissionService = (PermissionService) ctx.getBean("permissionService");
             permissionService.setPermission(rootNodeRef, permissionService.getAllAuthorities(), PermissionService.GUEST, true);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try
         {
            if (tx != null)
            {
               tx.rollback();
            }
         }
         catch (Exception ex) {}
         
         logger.error("Failed to initialise ", e);
         throw new AlfrescoRuntimeException("Failed to initialise ", e);
      }
      finally
      {
          try
          {
             authenticationComponent.clearCurrentSecurityContext();
          }
          catch (Exception ex) {}
      }
   }

   /**
    * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
    */
   public void contextDestroyed(ServletContextEvent event)
   {
      WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
      Scheduler quartz = (Scheduler) ctx.getBean("schedulerFactory");
      try
      {
         quartz.shutdown(true);
      }
      catch (SchedulerException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   /**
    * Session created listener
    */
   public void sessionCreated(HttpSessionEvent event)
   {
      if (logger.isDebugEnabled()) logger.debug("HTTP session created: " + event.getSession().getId());
   }

   /**
    * Session destroyed listener
    */
   public void sessionDestroyed(HttpSessionEvent event)
   {
      if (logger.isDebugEnabled()) logger.debug("HTTP session destroyed: " + event.getSession().getId());

      User user;
      if (Application.inPortalServer() == false)
      {
         user = (User)event.getSession().getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
      }
      else
      {
         user = (User)event.getSession().getAttribute(AlfrescoFacesPortlet.MANAGED_BEAN_PREFIX + AuthenticationHelper.AUTHENTICATION_USER);
      }
      
      if (user != null)
      {
         // invalidate ticket
         WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
         AuthenticationService authService = (AuthenticationService) ctx.getBean("authenticationService");
         authService.invalidateTicket(user.getTicket());
         event.getSession().removeAttribute(AuthenticationHelper.AUTHENTICATION_USER);
      }
   }
}
