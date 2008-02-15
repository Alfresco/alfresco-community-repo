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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.linkvalidation.HrefValidationProgress;
import org.alfresco.linkvalidation.LinkValidationAction;
import org.alfresco.linkvalidation.LinkValidationReport;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.wcm.component.UILinkValidationReport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the link validation dialog.
 * 
 * The dialog is in one of 2 modes, either running a report showing progress
 * or showing the results of an executed link check.
 * 
 * @author gavinc
 */
public class LinkValidationDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 3459041471664931826L;

   protected AVMBrowseBean avmBrowseBean;
   
   transient private AVMService avmService;
   transient private ActionService actionService;
   
   private String store;
   private String webapp;
   private String webappPath;
   private String initialTab;
   private String title;
   private NodeRef webappPathRef;
   private boolean runningReport = false;
   private boolean update = false;
   private boolean compareToStaging = false;
   private boolean sectionsExpanded = false;
   
   private static final Log logger = LogFactory.getLog(LinkValidationDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // check required params are present
      this.store = parameters.get("store");
      this.webapp = parameters.get("webapp");
      ParameterCheck.mandatoryString("store", this.store);
      ParameterCheck.mandatoryString("webapp", this.webapp);
      
      // setup context for dialog
      this.webappPath = AVMUtil.buildStoreWebappPath(this.store, this.webapp);
      this.webappPathRef = AVMNodeConverter.ToNodeRef(-1, this.webappPath);
      this.initialTab = UILinkValidationReport.DEFAULT_INTIAL_TAB;
      
      this.runningReport = false;
      String modeParam = parameters.get("mode");
      if (modeParam != null && modeParam.equalsIgnoreCase("runReport"))
      {
         this.runningReport = true;
      }
      
      this.update = false;
      String updateParam = parameters.get("update");
      if (updateParam != null && updateParam.equals("true"))
      {
         this.update = true;
      }
      
      this.compareToStaging = false;
      String compareToStagingParam = parameters.get("compareToStaging");
      if (compareToStagingParam != null && compareToStagingParam.equalsIgnoreCase("true"))
      {
         this.compareToStaging = true;
      }
      
      // work out title for dialog by examining store type
      FacesContext context = FacesContext.getCurrentInstance(); 
      if (this.getAvmService().getStoreProperty(this.store, 
               SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN) != null)
      {
         String pattern = Application.getMessage(context, "link_validaton_dialog_title_user");
         String user = this.store.substring(
                  this.store.indexOf(AVMUtil.STORE_SEPARATOR)+AVMUtil.STORE_SEPARATOR.length());
         this.title = MessageFormat.format(pattern, 
                  new Object[] {user});
      }
      else if (this.getAvmService().getStoreProperty(this.store, 
               SandboxConstants.PROP_SANDBOX_STAGING_MAIN) != null)
      {
         this.title = Application.getMessage(context, "link_validaton_dialog_title_staging");
      }
      else if (this.getAvmService().getStoreProperty(this.store, 
               SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN) != null)
      {
         this.title = Application.getMessage(context, "link_validaton_dialog_title_workflow");
      }

      if (logger.isDebugEnabled())
      {
         if (this.runningReport)
         {
            if (this.update)
               logger.debug("Starting update link validation check for webapp '" + this.webappPath + "'");
            else
               logger.debug("Starting initial link validation check for webapp '" + this.webappPath + "'");
         }
         else
         {
            logger.debug("Showing link validation report for webapp '" + this.webappPath + "'");
         }
      }

      // execute the report if required
      if (this.runningReport)
      {
         executeReport();
      }
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Starting fresh link validation check for webapp '" + this.webappPath + "'");
      
      // indicate we need a new report produced then execute
      this.update = false;
      this.runningReport = true;
      executeReport();
      
      // reset the isFinished flag so we can run the report again
      this.isFinished = false;
      
      return null;
   }
   
   @Override
   public String getContainerTitle()
   {
      return this.title;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return this.runningReport;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "rerun_report");
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   // ------------------------------------------------------------------------------
   // Event handlers
   
   public String linkCheckCompleted()
   {
      String outcome = null;
      
      // indicate that we are now showing a report
      this.runningReport = false;

      if (logger.isDebugEnabled())
      {
         if (this.update)
            logger.debug("Link check has completed, updating state object");
         else
            logger.debug("Link check has completed, creating state object");
      }
      
      // the link validation report should be stored as a store property
      // on the store the link check was run on, retrieve it and see if
      // the link check was successful.
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
  
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         PropertyValue val = this.getAvmService().getStoreProperty(this.store, 
                  SandboxConstants.PROP_LINK_VALIDATION_REPORT);
         if (val != null)
         {
            LinkValidationReport report = (LinkValidationReport)val.getSerializableValue();
            if (report != null)
            {
               // setup the context required by the reporting components to display the results
               if (this.update)
               {
                  this.avmBrowseBean.getLinkValidationState().updateState(report);
               }
               else
               {
                  LinkValidationState state = new LinkValidationState(report);
                  this.avmBrowseBean.setLinkValidationState(state);
               }
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
      
      return outcome;
   }
   
   public void toggleSections(ActionEvent event)
   {
      this.sectionsExpanded = !this.sectionsExpanded;

      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String tab = params.get("tab");

      if (tab != null)
      {
         this.initialTab = tab;
      }
   }
   
   /**
    * Sets up the dialog to update the status and display the differences
    * 
    * @return The outcome, null to stay on this page
    */
   public String updateStatus()
   {
      if (logger.isDebugEnabled())
         logger.debug("Updating status for link validation report for webapp '" + this.webappPath + "'");
      
      // indicate we need an update report produced then execute
      this.update = true;
      this.runningReport = true;
      executeReport();
      
      return null;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   protected void executeReport()
   {
      if (logger.isDebugEnabled())
         logger.debug("Creating LinkValidationAction to run report for webapp '" + this.webappPath + "'");
      
      // create context required to run and monitor the link check
      HrefValidationProgress monitor = new HrefValidationProgress();
      Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
      args.put(LinkValidationAction.PARAM_MONITOR, monitor);
      args.put(LinkValidationAction.PARAM_COMPARE_TO_STAGING, new Boolean(this.compareToStaging));
      this.avmBrowseBean.setLinkValidationMonitor(monitor);
      
      // create and execute the action in the background
      Action action = this.getActionService().createAction(LinkValidationAction.NAME, args);
      this.getActionService().executeAction(action, this.webappPathRef, false, true);
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @return true if the dialog is currently running a report
    */
   public boolean getRunningReport()
   {
      return this.runningReport;
   }
   
   /**
    * @return true if the dialog is currently showing a report
    */
   public boolean getShowingReport()
   {
      return !this.runningReport;
   }
   
   /**
    * @return true if the broken links and generated files section are expanded
    */
   public boolean getSectionsExpanded()
   {
      return sectionsExpanded;
   }

   /**
    * @param sectionsExpanded true if the broken links and generated 
    *                         files section are expanded
    */
   public void setSectionsExpanded(boolean sectionsExpanded)
   {
      this.sectionsExpanded = sectionsExpanded;
   }
   
   /**
    * @return The initial tab to be selected
    */
   public String getInitialTab()
   {
      return initialTab;
   }

   /**
    * @param initialTab Sets the initial tab to be selected
    */
   public void setInitialTab(String initialTab)
   {
      this.initialTab = initialTab;
   }

   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (this.avmService == null)
      {
         this.avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return this.avmService;
   }

   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }

   protected ActionService getActionService()
   {
      if (this.actionService == null)
      {
         this.actionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getActionService();
      }
      return this.actionService;
   }
   
}
