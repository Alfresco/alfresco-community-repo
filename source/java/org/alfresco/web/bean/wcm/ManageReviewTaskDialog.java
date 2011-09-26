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
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.ManageTaskDialog;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Manage Task" dialog when dealing
 * with review related WCM tasks.
 * 
 * @author gavinc
 */
public class ManageReviewTaskDialog extends ManageTaskDialog
{
   private static final long serialVersionUID = 59524560340308134L;
   
   private static final String MSG_WEB_PRJ_DOES_NOT_EXIST = "error_webprj_does_not_exist";
   
   protected String store;
   protected String webapp;
   protected NodeRef webProjectRef;
   protected AVMBrowseBean avmBrowseBean;
   transient private PermissionService permissionService;
   
   private static final Log logger = LogFactory.getLog(ManageReviewTaskDialog.class);
   
   
   // ------------------------------------------------------------------------------
   // Implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
  
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         // try and retrieve the link validation report from the workflow
         // store, if present setup the validation state on AVMBrowseBean
         this.store = this.workflowPackage.getStoreRef().getIdentifier();
         
         // get the web project noderef for the workflow store
         String wpStoreId = WCMUtil.getWebProjectStoreId(this.store);
         this.webProjectRef = getWebProjectService().getWebProjectNodeFromStore(WCMUtil.getWebProjectStoreId(this.store));
         
         if (this.webProjectRef == null)
         {
             String mesg = MessageFormat.format(Application.getMessage(context, MSG_WEB_PRJ_DOES_NOT_EXIST), wpStoreId);
             throw new AlfrescoRuntimeException(mesg);
         }
         
         // commit the changes
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(formatErrorMessage(e), e);
      }
   }

   // ------------------------------------------------------------------------------
   // Event handlers
   
   public String viewLinkReport()
   {
      if (logger.isDebugEnabled())
         logger.debug("Viewing link validation report for: " + 
                      AVMUtil.buildStoreWebappPath(this.store, this.webapp));
      
      Map<String, String> params = new HashMap<String, String>(3);
      params.put("store", this.store);
      params.put("webapp", this.webapp);
      params.put("compareToStaging", "true");
      Application.getDialogManager().setupParameters(params);
      
      return "dialog:linkValidation";
   }
   
   public String viewDeployReport()
   {
      if (logger.isDebugEnabled())
         logger.debug("Viewing deployment report for store: " + store);
      
      Map<String, String> params = new HashMap<String, String>(3);
      params.put("store", this.store);
      Application.getDialogManager().setupParameters(params);
      
      return "dialog:viewDeploymentReport";
   }
   
   public String deploy()
   {
      if (logger.isDebugEnabled())
         logger.debug("Deploying workflow store: " + this.store);
      
      Map<String, String> params = new HashMap<String, String>(4);
      params.put("store", this.store);
      params.put("webproject", this.webProjectRef.toString());
      params.put("calledFromTaskDialog", Boolean.TRUE.toString());
      
      // if a test server has already been allocated inform the dialog
      // that an update is needed
      List<NodeRef> testServers = DeploymentUtil.findAllocatedTestServers(this.store);
      if (!testServers.isEmpty())
      {
         params.put("updateTestServer", "true");
      }
      
      Application.getDialogManager().setupParameters(params);
      
      return "dialog:deployWebsite";
   }
   
   public String releaseTestServer()
   {   
       if (logger.isDebugEnabled())
           logger.debug("Release test server for store: " + store);

       Map<String, String> params = new HashMap<String, String>(3);
       params.put("store", this.store);
       Application.getDialogManager().setupParameters(params);

       return "dialog:releaseTestServer";
   }
   
   // ------------------------------------------------------------------------------
   // Getters and Setters
   
   /**
    * @param avmBrowseBean AVMBrowseBean instance
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param permissionService The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   protected PermissionService getPermissionService()
   {
      if (permissionService == null)
      {
         permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      }
      return permissionService;
   }
   
   /**
    * @return Determines if there are any test servers configured for the
    *         web project this task belongs to
    */
   @SuppressWarnings("unchecked")
   public boolean getTestServersAvailable()
   {
      // NOTE: This method is called a lot as it is referenced as a value binding
      //       expression in a 'rendered' attribute, we therefore cache the result
      //       on a per request basis
      
      Boolean result = null;
      
      FacesContext context = FacesContext.getCurrentInstance();
      Map request = context.getExternalContext().getRequestMap();
      if (request.get(AVMBrowseBean.REQUEST_BEEN_DEPLOYED_RESULT) == null)
      {
         result = Boolean.FALSE;
         
         if (this.webProjectRef != null && getPermissionService().hasPermission(webProjectRef, PermissionService.READ_PROPERTIES).equals(AccessStatus.ALLOWED))
         {
            final ServiceRegistry serviceRegistry = Repository.getServiceRegistry(context);
            final NodeRef projectRef = this.webProjectRef;

            TransactionService transactionService = serviceRegistry.getTransactionService();
            RetryingTransactionCallback<List<NodeRef>> findTestServers = new RetryingTransactionCallback<List<NodeRef>>()
            {
                public List<NodeRef> execute() throws Exception
                {
                    return serviceRegistry.getDeploymentService().findTestDeploymentServers(projectRef, false);
                }
            };
            List<NodeRef> testServers = transactionService.getRetryingTransactionHelper().doInTransaction(findTestServers);

            if (testServers != null)
            {
               result = new Boolean(testServers != null && testServers.size() > 0);
            }
         }
         
         request.put(AVMBrowseBean.REQUEST_BEEN_DEPLOYED_RESULT, result);
      }
      else
      {
         result = (Boolean)request.get(AVMBrowseBean.REQUEST_BEEN_DEPLOYED_RESULT);
      }
      
      return result.booleanValue();
   }
   
   /**
    * @return Determines whether a deployment has been attempted
    */
   public boolean getDeployAttempted()
   {
     PropertyValue propVal = null;
      try
      {
          propVal = getAvmService().getStoreProperty(this.store, 
               SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
      }
      catch (AVMNotFoundException nfe)
      {
          // ignore - eg. web project deleted
      }
      
      return (propVal != null);
   }
   
   /**
    * 
    */
   /**
    * @return Determines whether a test server is allocated
    */
   public boolean getTestServerAllocated()
   {
       // if a test server has already been allocated inform the dialog
       // that an update is needed
       List<NodeRef> testServers = DeploymentUtil.findAllocatedTestServers(this.store);
       if (!testServers.isEmpty())
       {
          return true;
       }
       return false;
   }
}
