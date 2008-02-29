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

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.wcm.component.UIDeploymentReports;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Views the deployment reports created as a result of the last deployment attempt
 * 
 * @author gavinc
 */
public class ViewDeploymentReportDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -8054466371051782132L;
   
   protected String store;
   protected NodeRef attempt;
   protected String attemptDate;
   protected boolean panelExpanded = false;
   protected String dateFilter = UIDeploymentReports.FILTER_DATE_WEEK;
   
   private static final Log logger = LogFactory.getLog(ViewDeploymentReportDialog.class);
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.store = parameters.get("store");
      this.dateFilter = UIDeploymentReports.FILTER_DATE_WEEK;
      this.panelExpanded = false;
      this.attempt = null;
      this.attemptDate = null;
      
      if (this.store == null || this.store.length() == 0)
      {
         throw new IllegalArgumentException("store parameter is mandatory");
      }

      if (logger.isDebugEnabled())
         logger.debug("Initialising dialog to view deployment report for: " + 
                  this.store);
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }
   
   @Override
   public String getContainerDescription()
   {
      String desc = null;
      
      if (attempt == null)
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), 
                  "deployment_report_desc");
      }
      else
      {
         desc = Application.getMessage(FacesContext.getCurrentInstance(), 
                  "deployment_previous_report_desc");
      }
         
      return desc;
   }

   @Override
   public String getContainerTitle()
   {
      String title = null;
      
      if (attempt == null)
      {
         title = Application.getMessage(FacesContext.getCurrentInstance(), 
                  "deployment_report_title");
      }
      else
      {
         String pattern = Application.getMessage(FacesContext.getCurrentInstance(), 
                  "deployment_previous_report_title");
         title = MessageFormat.format(pattern, this.attemptDate);
      }
      
      return title;
   }
   
   // ------------------------------------------------------------------------------
   // Event handlers

   /**
    * Action handler called when the Date filter is changed by the user
    */
   public void dateFilterChanged(ActionEvent event)
   {
      UIModeList filterComponent = (UIModeList)event.getComponent();
      setDateFilter(filterComponent.getValue().toString());
   }
   
   /**
    * Action handler called when a deployment attempt is selected
    */
   public void attemptSelected(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String attempt = params.get("attemptRef");
      if (attempt != null && attempt.length() != 0)
      {
         this.attemptDate = params.get("attemptDate");
         this.attempt = new NodeRef(attempt);
      }
   }
   
   /**
    * Action handler called when the panel is expanded or collapsed
    */
   public void panelToggled(ActionEvent event)
   {
      this.panelExpanded = !this.panelExpanded;
   }
   
   /**
    * Action handler called when user wants to go back to the last report 
    */
   public void showLastReport(ActionEvent event)
   {
      this.attempt = null;
      this.attemptDate = null;
      this.panelExpanded = false;
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   public String getStore()
   {
      return this.store;
   }

   public String getDateFilter()
   {
      return this.dateFilter;
   }

   public void setDateFilter(String dateFilter)
   {
      this.dateFilter = dateFilter;
   }

   public NodeRef getAttempt()
   {
      return this.attempt;
   }
   
   public boolean getPanelExpanded()
   {
      return this.panelExpanded;
   }
}