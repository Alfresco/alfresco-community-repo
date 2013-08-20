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
package org.alfresco.web.app;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

   private ServletContext servletContext;

   /**
    * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
    */
   public void contextInitialized(ServletContextEvent event)
   {
       // make sure that the spaces store in the repository exists
      this.servletContext = event.getServletContext();
      WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      
      // If no context has been initialised, exit silently so config changes can be made
      if (ctx == null)
      {
          return;
      }
      
      ServiceRegistry registry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
      TransactionService transactionService = registry.getTransactionService();
      NodeService nodeService = registry.getNodeService();
      SearchService searchService = registry.getSearchService();
      NamespaceService namespaceService = registry.getNamespaceService();
      AuthenticationContext authenticationContext = (AuthenticationContext) ctx
            .getBean("authenticationContext");

      // repo bootstrap code for our client
      UserTransaction tx = null;
      NodeRef companySpaceNodeRef = null;
      try
      {
         tx = transactionService.getUserTransaction();
         tx.begin();
         authenticationContext.setSystemUserAsCurrentUser();

         // get and setup the initial store ref and root path from config
         StoreRef storeRef = Repository.getStoreRef(servletContext);
         
         // get root path
         String rootPath = Application.getRootPath(servletContext);

         // Extract company space id and store it in the Application object
         companySpaceNodeRef = Repository.getCompanyRoot(nodeService, searchService, namespaceService, storeRef, rootPath);
         Application.setCompanyRootId(companySpaceNodeRef.getId());
         
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
             authenticationContext.clearCurrentSecurityContext();
          }
          catch (Exception ex) {}
      }
   }

   
   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(ServletContextEvent event)
   {
       // NOOP
   }

   /**
    * Session created listener
    */
   public void sessionCreated(HttpSessionEvent event)
   {
      if (logger.isDebugEnabled())
         logger.debug("HTTP session created: " + event.getSession().getId());
   }

   /**
    * Session destroyed listener
    */
   public void sessionDestroyed(HttpSessionEvent event)
   {
      if (logger.isDebugEnabled())
         logger.debug("HTTP session destroyed: " + event.getSession().getId());
   }
}
