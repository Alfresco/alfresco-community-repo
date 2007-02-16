/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.wizard;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

/**
 * Base class for all wizard beans providing common functionality
 * 
 * @author gavinc
 */
public abstract class BaseWizardBean extends BaseDialogBean implements IWizardBean
{
   private static final String MSG_NOT_SET = "value_not_set";
   
   public String next()
   {
      return null;
   }
   
   public String back()
   {
      return null;
   }
   
   public boolean getNextButtonDisabled()
   {
      return false;
   }
   
   public String getNextButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "next_button");
   }
   
   public String getBackButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "back_button");
   }

   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "finish_button");
   }

   public String getStepTitle()
   {
      return null;
   }

   public String getStepDescription()
   {
      return null;
   }
   
   /**
    * Build summary table from the specified list of Labels and Values
    * 
    * @param labels     Array of labels to display
    * @param values     Array of values to display
    * 
    * @return summary table HTML
    */
   protected String buildSummary(String[] labels, String[] values)
   {
      if (labels == null || values == null || labels.length != values.length)
      {
         throw new IllegalArgumentException("Labels and Values passed to summary must be valid and of equal length.");
      }
      
      String msg = Application.getMessage(FacesContext.getCurrentInstance(), MSG_NOT_SET);
      String notSetMsg = "&lt;" + msg + "&gt;";
      
      StringBuilder buf = new StringBuilder(512);
      
      buf.append("<table cellspacing='4' cellpadding='2' border='0' class='summary'>");
      for (int i=0; i<labels.length; i++)
      {
         String value = values[i];
         buf.append("<tr><td valign='top'><b>");
         buf.append(labels[i]);
         buf.append(":</b></td><td>");
         buf.append(value != null ? value : notSetMsg);
         buf.append("</td></tr>");
      }
      buf.append("</table>");
      
      return buf.toString();
   }
   
   @Override
   protected String getDefaultCancelOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }
}
