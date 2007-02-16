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
package org.alfresco.web.ui.repo.component.shelf;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;

/**
 * @author Kevin Roast
 */
public class UIShelf extends SelfRenderingComponent
{
   // ------------------------------------------------------------------------------
   // Component Impl 

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Shelf";
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.groupPanel = (String)values[1];
      this.groupBgcolor = (String)values[2];
      this.selectedGroupPanel = (String)values[3];
      this.selectedGroupBgcolor = (String)values[4];
      this.innerGroupPanel = (String)values[5];
      this.innerGroupBgcolor = (String)values[6];
      this.groupExpandedActionListener = (MethodBinding)values[7];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[8];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.groupPanel;
      values[2] = this.groupBgcolor;
      values[3] = this.selectedGroupPanel;
      values[4] = this.selectedGroupBgcolor;
      values[5] = this.innerGroupPanel;
      values[6] = this.innerGroupBgcolor;
      values[7] = this.groupExpandedActionListener;
      return values;
   }
   
   /** 
    * @param binding    The MethodBinding to call when the Group expand action is performed by the user
    */
   public void setGroupExpandedActionListener(MethodBinding binding)
   {
      this.groupExpandedActionListener = binding;
   }
   
   /** 
    * @return The MethodBinding to call for the Group expand action
    */
   public MethodBinding getGroupExpandedActionListener()
   {
      return this.groupExpandedActionListener;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      // we encoded the value to start with our Id
      if (value != null && value.length() != 0)
      {
         int sepIndex = value.indexOf(NamingContainer.SEPARATOR_CHAR);
         int groupIndex = Integer.parseInt( value.substring(0, sepIndex) );
         boolean expanded = Boolean.parseBoolean( value.substring(sepIndex + 1) );
         
         // fire an event here to indicate the change that occured
         ShelfEvent event = new ShelfEvent(this, groupIndex, expanded);
         this.queueEvent(event);
      }
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ShelfEvent)
      {
         ShelfEvent shelfEvent = (ShelfEvent)event;
         
         // set the new expanded state of the appropriate shelf item
         int index = 0;
         for (Iterator i=this.getChildren().iterator(); i.hasNext(); index++)
         {
            UIComponent child = (UIComponent)i.next();
            if (index == shelfEvent.Index && child instanceof UIShelfGroup)
            {
               // found correct child - set the new state
               ((UIShelfGroup)child).setExpanded(shelfEvent.Expanded);
               break;
            }
         }
         
         // if an action event is registered to be notified then fire that next
         if (getGroupExpandedActionListener() != null)
         {
            Utils.processActionMethod(getFacesContext(), getGroupExpandedActionListener(), shelfEvent);
         }
      }
      else
      {
         super.broadcast(event);
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
      
      out.write("<div id=\"shelf\" class=\"shelf\">");
      out.write("<table border=0 cellspacing=4 cellpadding=0 width=100%>");
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      // output each shelf group in turn
      int index = 0;
      for (Iterator i=this.getChildren().iterator(); i.hasNext(); index++)
      {
         UIComponent child = (UIComponent)i.next();
         if (child instanceof UIShelfGroup)
         {
            UIShelfGroup group = (UIShelfGroup)child;
            if (group.isRendered() == true)
            {
               // output the surrounding structure then call the component to render itself and children
               boolean isExpanded = group.isExpanded();      // TODO: get this from Shelf or ShelfGroup?
               out.write("<tr><td>");
               
               String contextPath = context.getExternalContext().getRequestContextPath();
               
               // output appropriate panel start section and bgcolor
               String groupPanel;
               String groupBgcolor;
               if (isExpanded == false)
               {
                  groupPanel = getGroupPanel();
                  groupBgcolor = getGroupBgcolor();
               }
               else
               {
                  groupPanel = getSelectedGroupPanel();
                  groupBgcolor = getSelectedGroupBgcolor();
               }
               if (groupBgcolor == null)
               {
                  groupBgcolor = PanelGenerator.BGCOLOR_WHITE;
               }
               if (groupPanel != null)
               {
                  PanelGenerator.generatePanelStart(out, contextPath, groupPanel, groupBgcolor);
               }
               
               // output appropriate expanded icon state
               out.write("<div style='padding-top:2px;padding-bottom:4px'><nobr>");
               out.write("<a href='#' onclick=\"");
               // encode value as the index of the ShelfGroup clicked and the new state
               String value = Integer.toString(index) + NamingContainer.SEPARATOR_CHAR + Boolean.toString(!isExpanded);
               out.write(Utils.generateFormSubmit(context, this, getHiddenFieldName(), value));
               out.write("\">");
               if (isExpanded == true)
               {
                  out.write(Utils.buildImageTag(context, WebResources.IMAGE_EXPANDED, 11, 11, ""));
               }
               else
               {
                  out.write(Utils.buildImageTag(context, WebResources.IMAGE_COLLAPSED, 11, 11, ""));
               }
               out.write("</a>&nbsp;");
               
               // output title label text
               String label = group.getLabel();
               out.write("<span");
               outputAttribute(out, group.getAttributes().get("style"), "style");
               outputAttribute(out, group.getAttributes().get("styleClass"), "class");
               out.write('>');
               out.write(Utils.encode(label));
               out.write("</span>");
               out.write("</nobr></div>");
               
               if (isExpanded == true)
               {
                  // if this is the expanded group, output the inner panel 
                  String innerGroupPanel = getInnerGroupPanel();
                  String innerGroupBgcolor = getInnerGroupBgcolor();
                  if (innerGroupBgcolor == null)
                  {
                     innerGroupBgcolor = PanelGenerator.BGCOLOR_WHITE;
                  }
                  if (innerGroupPanel != null)
                  {
                     PanelGenerator.generatePanelStart(out, contextPath, innerGroupPanel, innerGroupBgcolor);
                  }
                  
                  // allow child components to render themselves
                  Utils.encodeRecursive(context, group);
                  
                  if (innerGroupPanel != null)
                  {
                     PanelGenerator.generatePanelEnd(out, contextPath, innerGroupPanel);
                  }
               }
               
               // output panel and group end elements
               PanelGenerator.generatePanelEnd(out, contextPath, groupPanel);
               out.write("</td></tr>");
            }
         }
      }
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
    */
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      out.write("</table></div>");
   }

   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }

   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 
   
   /**
    * @return Returns the group panel name.
    */
   public String getGroupPanel()
   {
      ValueBinding vb = getValueBinding("groupPanel");
      if (vb != null)
      {
         this.groupPanel = (String)vb.getValue(getFacesContext());
      }
      
      return this.groupPanel;
   }
   
   /**
    * @param groupPanel    The group panel name to set.
    */
   public void setGroupPanel(String groupPanel)
   {
      this.groupPanel = groupPanel;
   }
   
   /**
    * @return Returns the group background colour.
    */
   public String getGroupBgcolor()
   {
      ValueBinding vb = getValueBinding("groupBgcolor");
      if (vb != null)
      {
         this.groupBgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.groupBgcolor;
   }
   
   /**
    * @param groupBgcolor    The group background colour to set.
    */
   public void setGroupBgcolor(String groupBgcolor)
   {
      this.groupBgcolor = groupBgcolor;
   }
   
   /**
    * @return Returns the selected group panel name.
    */
   public String getSelectedGroupPanel()
   {
      ValueBinding vb = getValueBinding("selectedGroupPanel");
      if (vb != null)
      {
         this.selectedGroupPanel = (String)vb.getValue(getFacesContext());
      }
      
      return this.selectedGroupPanel;
   }
   
   /**
    * @param selectedGroupPanel    The selected group panel name to set.
    */
   public void setSelectedGroupPanel(String selectedGroupPanel)
   {
      this.selectedGroupPanel = selectedGroupPanel;
   }
   
   /**
    * @return Returns the selected group background colour.
    */
   public String getSelectedGroupBgcolor()
   {
      ValueBinding vb = getValueBinding("selectedGroupBgcolor");
      if (vb != null)
      {
         this.selectedGroupBgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.selectedGroupBgcolor;
   }
   
   /**
    * @param selectedGroupBgcolor    The selected group background colour to set.
    */
   public void setSelectedGroupBgcolor(String selectedGroupBgcolor)
   {
      this.selectedGroupBgcolor = selectedGroupBgcolor;
   }
   
   /**
    * @return Returns the inner group panel name.
    */
   public String getInnerGroupPanel()
   {
      ValueBinding vb = getValueBinding("innerGroupPanel");
      if (vb != null)
      {
         this.innerGroupPanel = (String)vb.getValue(getFacesContext());
      }
      
      return this.innerGroupPanel;
   }
   
   /**
    * @param innerGroupPanel    The inner group panel name to set.
    */
   public void setInnerGroupPanel(String innerGroupPanel)
   {
      this.innerGroupPanel = innerGroupPanel;
   }
   
   /**
    * @return Returns the inner group background colour.
    */
   public String getInnerGroupBgcolor()
   {
      ValueBinding vb = getValueBinding("innerGroupBgcolor");
      if (vb != null)
      {
         this.innerGroupBgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.innerGroupBgcolor;
   }
   
   /**
    * @param innerGroupBgcolor    The inner group background colour to set.
    */
   public void setInnerGroupBgcolor(String innerGroupBgcolor)
   {
      this.innerGroupBgcolor = innerGroupBgcolor;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * We use a hidden field name on the assumption that very few shelf instances will
    * be present on a single page.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      return getClientId(getFacesContext());
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the an action event relevant to the Shelf.
    */
   public static class ShelfEvent extends ActionEvent
   {
      public ShelfEvent(UIComponent component, int index, boolean expanded)
      {
         super(component);
         Expanded = expanded;
         Index = index;
      }
      
      public boolean Expanded;
      public int Index;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   /** component properties */
   private String groupPanel;
   private String groupBgcolor;
   private String selectedGroupPanel;
   private String selectedGroupBgcolor;
   private String innerGroupPanel;
   private String innerGroupBgcolor;
   private MethodBinding groupExpandedActionListener;
}
