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
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

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
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the run link validation dialog.
 * 
 * @author gavinc
 */
public class LinkValidationRunDialog extends BaseDialogBean
{
   protected AVMBrowseBean avmBrowseBean;
   protected AVMService avmService;
   protected ActionService actionService;
   
   private String store;
   private NodeRef storeRef;
   private boolean rerun = false;
   
   private static final Log logger = LogFactory.getLog(LinkValidationRunDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // TODO: determine whether the virtualisation server is running, if it's not
      //       we need to throw an exception to reflect that
      
      // setup context for dialog
      store = parameters.get("store");
      
      String storePath = this.store + ":/";
      this.storeRef = AVMNodeConverter.ToNodeRef(-1, storePath);
      
      this.rerun = false;
      String rerunParam = parameters.get("rerun");
      if (rerunParam != null && rerunParam.equals("true"))
      {
         this.rerun = true;
      }
      
      if (logger.isDebugEnabled())
      {
         if (this.rerun)
            logger.debug("Starting re-run link validation check for store '" + store + "'");
         else
            logger.debug("Starting initial link validation check for store '" + store + "'");
      }
      
      // create context required to run and monitor the link check
      HrefValidationProgress monitor = new HrefValidationProgress();
      Map<String, Serializable> args = new HashMap<String, Serializable>(1, 1.0f);
      args.put(LinkValidationAction.PARAM_MONITOR, monitor);
      this.avmBrowseBean.setLinkValidationMonitor(monitor);
      
      // create and execute the action in the background
      Action action = this.actionService.createAction(LinkValidationAction.NAME, args);
      this.actionService.executeAction(action, this.storeRef, false, true);
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Event handlers
   
   public String linkCheckCompleted()
   {
      String outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      
      // the link validation report should be stored as a store property
      // on the store the link check was run on, retrieve it and see if
      // the link check was successful.
      
      PropertyValue val = this.avmService.getStoreProperty(this.store, 
               SandboxConstants.PROP_LINK_VALIDATION_REPORT);
      if (val != null)
      {
         LinkValidationReport report = (LinkValidationReport)val.getSerializableValue();
         if (report != null)
         {
            if (report.wasSuccessful())
            {
               // setup the context required by the reporting components to display the results
               if (this.rerun)
               {
                  this.avmBrowseBean.getLinkValidationState().updateState(report);
               }
               else
               {
                  LinkValidationState state = new LinkValidationState(this.store, report);
                  this.avmBrowseBean.setLinkValidationState(state);
               }
      
               Map<String, String> params = new HashMap<String, String>(1);
               params.put("store", this.store);
               Application.getDialogManager().setupParameters(params);
               
               outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                         AlfrescoNavigationHandler.OUTCOME_SEPARATOR +
                         "dialog:linkValidationReport";
            }
            else
            {
               FacesContext context = FacesContext.getCurrentInstance();
               String errorMsg = Application.getMessage(context, "link_validation_unknown_error");
               Throwable cause = report.getError();
               if (cause != null)
               {
                  errorMsg = Application.getMessage(context, "link_validation_error") + ": " +
                        cause.getMessage();
               }
               
               Utils.addErrorMessage(errorMsg);
            }
         }
      }
      
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
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
   
   /**
    * @param actionService The actionService to set.
    */
   public void setActionService(ActionService actionService)
   {
      this.actionService = actionService;
   }
}
