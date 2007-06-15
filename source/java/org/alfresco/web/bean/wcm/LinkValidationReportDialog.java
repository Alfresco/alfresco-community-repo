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

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the link validation report dialog.
 * 
 * @author gavinc
 */
public class LinkValidationReportDialog extends BaseDialogBean
{
   protected AVMBrowseBean avmBrowseBean;
   protected AVMService avmService;
   protected ActionService actionService;
   
   private String store;
   private String cancelOutcome;
   
   private static final Log logger = LogFactory.getLog(LinkValidationReportDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // setup context for dialog
      store = parameters.get("store");
      
      if (logger.isDebugEnabled())
         logger.debug("Showing link validation report for store '" + store + "'");
      
      String directView = parameters.get("directView");
      if (directView != null && directView.equals("true"))
      {
         this.cancelOutcome = super.getDefaultCancelOutcome();
      }
      else
      {
         this.cancelOutcome =  AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                               AlfrescoNavigationHandler.OUTCOME_SEPARATOR +
                               "browseWebsite";
      }
   }

   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("Re-running link validation report for store '" + store + "'");
      
      Map<String, String> params = new HashMap<String, String>(1);
      params.put("store", this.store);
      Application.getDialogManager().setupParameters(params);
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR +
             "dialog:runLinkValidation";
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
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

   @Override
   protected String getDefaultCancelOutcome()
   {
      return this.cancelOutcome;
   }
   
   // ------------------------------------------------------------------------------
   // Event handlers
   
   public String updateStatus()
   {
      if (logger.isDebugEnabled())
         logger.debug("Updating status for link validation report for store '" + store + "'");
      
      Map<String, String> params = new HashMap<String, String>(1);
      params.put("store", this.store);
      params.put("rerun", "true");
      Application.getDialogManager().setupParameters(params);
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR +
             "dialog:runLinkValidation";
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
