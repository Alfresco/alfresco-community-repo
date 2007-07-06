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
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.config.JNDIConstants;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wcm.LinkValidationState;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.repo.component.UIActions;

/**
 * Base class for all the link validation report JSF components.
 * 
 * @author gavinc
 */
public abstract class AbstractLinkValidationReportComponent extends SelfRenderingComponent
{
   protected LinkValidationState state;
   
   // ------------------------------------------------------------------------------
   // Component implementation

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.state = (LinkValidationState)values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.state;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      // the child components are rendered explicitly during the encodeBegin()
   }
  
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Returns the name and path for the given avm path
    * 
    * @param avmPath The path to split
    * @return A String array with the name in the first position and the path in the
    *         second position.
    */
   protected String[] getFileNameAndPath(String avmPath)
   {
      String fileName = avmPath;
      String filePath = avmPath;
      
      int idx = avmPath.lastIndexOf("/");
      if (idx != -1)
      {
         fileName = avmPath.substring(idx+1);
         
         int appbaseIdx = avmPath.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE);
         if (appbaseIdx != -1)
         {
            filePath = avmPath.substring(appbaseIdx+JNDIConstants.DIR_DEFAULT_APPBASE.length(), idx);
         }
         else
         {
            filePath = avmPath.substring(0, idx);
         }
      }
      
      return new String[] {fileName, filePath};
   }
   
   /**
    * Constructs a comma separated list of broken links for the given avm path
    * 
    * @param avmPath The avm path to get the broken links for
    * @param linkState The current link valiation state
    * @return Comma separated list of broken links
    */
   protected String getBrokenLinks(FacesContext context, String avmPath, LinkValidationState linkState)
   {
      List<String> brokenLinks = linkState.getBrokenLinksForFile(avmPath);
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (String link : brokenLinks)
      {
         if (first == false)
         {
            builder.append("<br/>");
         }
         else
         {
            first = false;
         }
         
         builder.append("<img src='");
         builder.append(context.getExternalContext().getRequestContextPath());
         builder.append("/images/icons/broken_link.gif' style='vertical-align: -4px;' />");
         builder.append(parseBrokenLink(link));
      }
      
      return builder.toString();
   }
   
   /**
    * Removes the virtaulisation server host name from the link if appropriate
    * 
    * @param linkUrl The URL that is broken
    * @return Parsed URL
    */
   protected String parseBrokenLink(String linkUrl)
   {
      String link = linkUrl;
      
      if (linkUrl.startsWith("http://") && linkUrl.indexOf("www--sandbox") != -1)
      {
         // remove the virtualisation server host name
         int idx = linkUrl.indexOf("/", 7);
         if (idx != -1)
         {
            link = linkUrl.substring(idx);
         }
      }
      
      // truncate the link if it is longer than 100 chars
      String title = link;
      if (link.length() > 60)
      {
         link = link.substring(0, 60) + "...";
      }
      
      return "<span title='" + title + "'>&nbsp;" + link + "</span>";
   }
   
   /**
    * Renders the HTML to display a file and it's optional broken links
    * 
    * @param out ResponseWriter instance to write to
    * @param context FacesContext
    * @param fileName Name of the file
    * @param filePath Path to the file
    * @param brokenLinks List of broken links in the file
    * @throws IOException
    */
   protected void renderFile(ResponseWriter out, FacesContext context,
            String fileName, String filePath, String brokenLinks) throws IOException
   {
      out.write("<table cellpadding='0' cellspacing='0'><tr><td valign='top'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write(getIcon(fileName));
      out.write("' style='margin: 5px;' /></td>");
      out.write("<td width='100%'><div style='padding: 5px;'><div style='font-weight: bold;'>");
      out.write(fileName);
      out.write("</div><div style='padding-top: 2px;'>");
      out.write(filePath);
      out.write("</div>");
      
      if (brokenLinks != null && brokenLinks.length() > 0)
      {
         out.write("<div style='padding-top: 4px; color: #888;'>");
         out.write(Application.getMessage(context, "broken_links"));
         out.write(":</div><div style='padding-top: 2px;'>");
         out.write(brokenLinks);
         out.write("</div>");
      }
      
      out.write("</div></td></tr></table>");
   }
   
   /**
    * Renders the "No items to display" message
    * 
    * @param out ResponseWriter instance to write to
    * @param context FacesContext
    * @throws IOException
    */
   protected void renderNoItems(ResponseWriter out, FacesContext context)
      throws IOException
   {
      out.write("<tr><td><div style='padding: 6px;'>");
      out.write(Application.getMessage(context, "no_items"));
      out.write("</div></td></tr>");
   }
   
   /**
    * Returns the icon to use given a file name
    * 
    * @param fileName File name to find an icon for
    * @return The path to the icon to use
    */
   protected String getIcon(String fileName)
   {
      // work out what icon to use
      String icon = "/images/filetypes32/html.gif";
      String ext = "";
      int idx = fileName.indexOf(".");
      if (idx != -1)
      {
         ext = fileName.substring(idx);
      }
      
      if (ext.equals(".xml"))
      {
         icon = "/images/icons/webform_large.gif";
      }
      
      return icon;
   }
   
   /**
    * Aquire the UIActions component for the specified action group ID.
    * Search for the component in the child list or create as needed. 
    * 
    * @param id      ActionGroup id of the UIActions component
    * 
    * @return UIActions component
    */
   @SuppressWarnings("unchecked")
   protected UIActions aquireUIActions(String id, String store)
   {
      UIActions uiActions = null;
      String componentId = id + '_' + store;
      
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (componentId.equals(component.getId()))
         {
            uiActions = (UIActions)component;
            break;
         }
      }
      
      if (uiActions == null)
      {
         javax.faces.application.Application facesApp = FacesContext.getCurrentInstance().getApplication();
         uiActions = (UIActions)facesApp.createComponent("org.alfresco.faces.Actions");
         uiActions.setShowLink(false);
         uiActions.getAttributes().put("styleClass", "inlineAction");
         uiActions.setId(componentId);
         uiActions.setParent(this);
         uiActions.setValue(id);
         
         this.getChildren().add(uiActions);
      }
      
      return uiActions;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return The LinkValidationState object holding the report information
    */
   public LinkValidationState getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.state = (LinkValidationState)vb.getValue(getFacesContext());
      }
      
      return this.state;
   }
   
   /**
    * @param value The LinkValidationState object to get the summary info from
    */
   public void setValue(LinkValidationState value)
   {
      this.state = value;
   }
}
