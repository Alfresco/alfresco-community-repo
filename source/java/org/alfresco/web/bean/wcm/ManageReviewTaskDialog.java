/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.bean.wcm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.linkvalidation.LinkValidationReport;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.repository.NodeRef;
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
   
   protected String store;
   protected String webapp;
   protected NodeRef webProjectRef;
   protected AVMBrowseBean avmBrowseBean;
   
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
         
         // reset any previous link validation state
         this.avmBrowseBean.setLinkValidationState(null);
         this.avmBrowseBean.setLinkValidationMonitor(null);
         
         // try and retrieve the link validation report from the workflow
         // store, if present setup the validation state on AVMBrowseBean
         this.store = this.workflowPackage.getStoreRef().getIdentifier();
         
         // get the web project noderef for the workflow store
         String stagingStore = AVMUtil.getStoreId(this.store);
         this.webProjectRef = AVMUtil.getWebProjectNodeFromStore(stagingStore);
         
         PropertyValue val = this.getAvmService().getStoreProperty(this.store, 
                  SandboxConstants.PROP_LINK_VALIDATION_REPORT);
         if (val != null)
         {
            LinkValidationReport report = (LinkValidationReport)val.getSerializableValue();
            if (report != null)
            {
               String reportStore = report.getStore();
               this.webapp = report.getWebapp();
               
               if (logger.isDebugEnabled())
                  logger.debug("Found link validation report for webapp '" + 
                               AVMUtil.buildStoreWebappPath(reportStore, this.webapp) + "'");
               
               LinkValidationState state = new LinkValidationState(report);
               this.avmBrowseBean.setLinkValidationState(state);
               
               if (logger.isDebugEnabled())
                  logger.debug("Stored link validation state: " + state);
            }
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
      NodeRef testServer = DeploymentUtil.findAllocatedTestServer(this.store);
      if (testServer != null)
      {
         params.put("updateTestServer", "true");
      }
      
      Application.getDialogManager().setupParameters(params);
      
      return "dialog:deployWebsite";
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
         List<NodeRef> testServers = DeploymentUtil.findTestServers(this.webProjectRef, false);
         result = new Boolean(testServers != null && testServers.size() > 0);
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
      PropertyValue propVal = getAvmService().getStoreProperty(this.store, 
               SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
      
      return (propVal != null);
   }
}
