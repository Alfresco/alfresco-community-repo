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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wcm.LinkValidationState;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF component that shows the summary information for a link
 * validation report.
 * 
 * @author gavinc
 */
public class UILinkValidationSummary extends AbstractLinkValidationReportComponent
{
   private static Log logger = LogFactory.getLog(UILinkValidationSummary.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.LinkValidationSummary";
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      // get the link validation state object to get the data from
      ResponseWriter out = context.getResponseWriter();
      LinkValidationState linkState = getValue();
      
      if (logger.isDebugEnabled())
         logger.debug("Rendering summary from state object: " + linkState);
      
      // resolve all the strings holding data
      ResourceBundle bundle = Application.getBundle(context);
      String pattern = bundle.getString("files_links_checked");
      Date initialCheck = linkState.getInitialCheckCompletedAt();
      String initialCheckTime = Utils.getDateTimeFormat(context).format(initialCheck);
      String initialCheckSummary = MessageFormat.format(pattern, 
               new Object[] {initialCheckTime, linkState.getInitialNumberFilesChecked(), 
                             linkState.getInitialNumberLinksChecked()});
      pattern = bundle.getString("files_links_broken");
      String initialBrokenSummary = MessageFormat.format(pattern, 
               new Object[] {linkState.getInitialNumberBrokenFiles(), linkState.getInitialNumberBrokenLinks()});
      pattern = bundle.getString("files_links_still_broken");
      String stillBroken = MessageFormat.format(pattern, 
               new Object[] {linkState.getNumberBrokenFiles(), linkState.getNumberBrokenLinks()});
      pattern = bundle.getString("broken_links_fixed");
      String linksFixed = MessageFormat.format(pattern, 
               new Object[] {linkState.getNumberFixedLinks()});
      
      // get the action to update the current status
      UICommand updateStatusAction = aquireAction(context, "update_status_" + linkState.getStore());
      
      // render the summary area
      out.write("<div class='linkValidationSummaryPanel'><div class='linkValidationReportTitle'>");
      out.write(bundle.getString("summary"));
      out.write("</div><table cellpadding='0' cellspacing='0'><tr>");
      out.write("<td valign='top' class='linkValidationReportSubTitle'>");
      out.write(bundle.getString("initial_check"));
      out.write(":</td><td><div style='margin-bottom: 6px;'>");
      out.write(initialCheckSummary);
      out.write("</div><div style='margin-bottom: 14px;'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/broken_link.gif'' style='vertical-align: -4px; padding-right: 4px;'/>");
      out.write(initialBrokenSummary);
      out.write("</div></td></tr>");
      out.write("<tr><td class='linkValidationReportSubTitle'>");
      out.write(bundle.getString("current_status"));
      out.write(":</td><td><div><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/broken_link.gif' style='vertical-align: -4px; padding-right: 4px;' >");
      out.write(stillBroken);
      out.write("<img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/fixed_link.gif' style='vertical-align: -4px; padding-left: 6px; padding-right: 4px;' >");
      out.write(linksFixed);
      out.write("&nbsp;&nbsp;");
      Utils.encodeRecursive(context, updateStatusAction);
      out.write("</div></td></tr>");
      out.write("</table></div>");
   }

   @SuppressWarnings("unchecked")
   private UICommand aquireAction(FacesContext context, String actionId)
   {
      UICommand action = null;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UICommand)facesApp.createComponent(UICommand.COMPONENT_TYPE);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, "update_status"));
         MethodBinding binding = facesApp.createMethodBinding("#{DialogManager.bean.updateStatus}", 
                  new Class[] {});
         action.setAction(binding);
         this.getChildren().add(action);
      }
      
      return action;
   }
}
