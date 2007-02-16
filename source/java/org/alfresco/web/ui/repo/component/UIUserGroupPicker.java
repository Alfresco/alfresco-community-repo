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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;

/**
 * Seld rendering component tied to the EmailSpaceUsersDialog bean. Renders a hierarchy of
 * user/group authorities. Each authority can be (de)selected and groups can be expanded/collapsed
 * to display and select from the child authorities in the group. Nested groups are supported.
 * 
 * @author Kevin Roast
 */
public class UIUserGroupPicker extends UICommand
{
   /** action ids */
   public final static int ACTION_NONE = -1;
   public final static int ACTION_EXPANDCOLLAPSE = 0;
   public final static int ACTION_SELECT = 1;
   
   private static String SELECTED_AUTHORITY = "_check";
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * Default constructor
    */
   public UIUserGroupPicker()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.UserGroupPicker";
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName(context);
      String value = (String)requestMap.get(fieldId);
      
      if (value != null && value.length() != 0)
      {
         // decode the values - we are expecting an action identifier and an authority name
         int sepIndex = value.indexOf(NamingContainer.SEPARATOR_CHAR);
         int action = Integer.parseInt(value.substring(0, sepIndex));
         String authority = value.substring(sepIndex + 1);
         
         // queue an event
         PickerEvent event = new PickerEvent(this, action, authority);
         queueEvent(event);
      }
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      ResourceBundle bundle = Application.getBundle(context);
      
      String clientId = getClientId(context);
      
      // start outer table
      out.write("<table width=100% border=0 cellspacing=0 cellpadding=0 class='userGroupPickerList'>");
      
      // get the data that represents the users/groups to display
      List<Map> userGroups = (List<Map>)getValue();
      if (userGroups != null)
      {
         for (Map authority : userGroups)
         {
            String authorityId = (String)authority.get("id");
            
            out.write("<tr><td width=100%><table width=100% border=0 cellspacing=3 cellpadding=0><tr>");
            
            // walk parent hierarchy to calculate width of this cell
            int width = 16;
            Map parent = (Map)authority.get("parent");
            while (parent != null)
            {
               width += 16;
               parent = (Map)parent.get("parent");
            }
            out.write("<td width=");
            out.write(Integer.toString(width));
            out.write(" align=right>");
            
            // output expanded/collapsed icon if authority is a group
            boolean expanded = false;
            boolean isGroup = (Boolean)authority.get("isGroup");
            if (isGroup)
            {
               // either output the expanded or collapsed selectable widget
               expanded = (Boolean)authority.get("expanded");
               String image = expanded ? WebResources.IMAGE_EXPANDED : WebResources.IMAGE_COLLAPSED; 
               out.write(Utils.buildImageTag(context, image, 11, 11, "",
                     generateFormSubmit(context, ACTION_EXPANDCOLLAPSE, authorityId)));
            }
            out.write("</td><td width=16>");
            
            // output selected checkbox if not expanded and not a duplicate
            boolean duplicate = (Boolean)authority.get("duplicate");
            if (duplicate == false && (isGroup == false || expanded == false))
            {
               boolean selected = (Boolean)authority.get("selected");
               out.write("<input type='checkbox' value='' name='");
               out.write(clientId + NamingContainer.SEPARATOR_CHAR + SELECTED_AUTHORITY);
               out.write("' onclick=\"");
               out.write(generateFormSubmit(context, ACTION_SELECT, authorityId));
               out.write('"');
               if (selected)
               {
                  out.write(" CHECKED");
               }
               out.write('>');
            }
            out.write("</td><td width=16>");
            
            // output icon
            out.write(Utils.buildImageTag(context, (String)authority.get("icon"), 16, 16, ""));
            out.write("</td><td>");
            
            // output textual information
            if (duplicate)
            {
               out.write("<span style='color:#93a8b2'>");
            }
            out.write((String)authority.get("fullName"));
            out.write(" (");
            out.write((String)authority.get("roles"));
            out.write(")");
            if (duplicate)
            {
               out.write("</span>");
            }
            out.write("</td>");
            
            out.write("</tr></table></td></tr>");
         }
      }
      
      out.write("</table>");
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * We use a hidden field per picker instance on the page.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName(FacesContext context)
   {
      return getClientId(context);
   }
   
   /**
    * Generate FORM submit JavaScript for the specified action
    *  
    * @param context    FacesContext
    * @param action     Action index
    * @param authority  Authority Id of the action source
    * 
    * @return FORM submit JavaScript
    */
   private String generateFormSubmit(FacesContext context, int action, String authority)
   {
      return Utils.generateFormSubmit(context, this, getHiddenFieldName(context),
            Integer.toString(action) + NamingContainer.SEPARATOR_CHAR + authority);
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the an action relevant to the User Group picker component.
    */
   public static class PickerEvent extends ActionEvent
   {
      public PickerEvent(UIComponent component, int action, String authority)
      {
         super(component);
         Action = action;
         Authority = authority;
      }
      
      public String Authority;
      public int Action;
   }
}
