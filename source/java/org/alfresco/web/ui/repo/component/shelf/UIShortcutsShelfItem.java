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
import java.util.List;
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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;

/**
 * JSF Component providing UI for a list of user defined shortcuts to favorite nodes.
 * 
 * @author Kevin Roast
 */
public class UIShortcutsShelfItem extends UIShelfItem
{
   // ------------------------------------------------------------------------------
   // Component Impl
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = values[1];
      this.clickActionListener = (MethodBinding)values[2];
      this.removeActionListener = (MethodBinding)values[3];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[4];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      values[2] = this.clickActionListener;
      values[3] = this.removeActionListener;
      
      return (values);
   }
   
   /**
    * Get the value (for this component the value is used as the List of shortcut nodes)
    *
    * @return the value
    */
   public Object getValue()
   {
      if (this.value == null)
      {
         ValueBinding vb = getValueBinding("value");
         if (vb != null)
         {
            this.value = vb.getValue(getFacesContext());
         }
      }
      return this.value;
   }

   /**
    * Set the value (for this component the value is used as the List of shortcut nodes)
    *
    * @param value     the value
    */
   public void setValue(Object value)
   {
      this.value = value;
   }
   
   /** 
    * @param binding    The MethodBinding to call when Click is performed by the user
    */
   public void setClickActionListener(MethodBinding binding)
   {
      this.clickActionListener = binding;
   }
   
   /** 
    * @return The MethodBinding to call when Click is performed by the user
    */
   public MethodBinding getClickActionListener()
   {
      return this.clickActionListener;
   }
   
   /** 
    * @param binding    The MethodBinding to call when Remove is performed by the user
    */
   public void setRemoveActionListener(MethodBinding binding)
   {
      this.removeActionListener = binding;
   }
   
   /** 
    * @return The MethodBinding to call when Remove is performed by the user
    */
   public MethodBinding getRemoveActionListener()
   {
      return this.removeActionListener;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      if (value != null && value.length() != 0)
      {
         // decode the values - we are expecting an action identifier and an index
         int sepIndex = value.indexOf(NamingContainer.SEPARATOR_CHAR);
         int action = Integer.parseInt(value.substring(0, sepIndex));
         int index = Integer.parseInt(value.substring(sepIndex + 1));
         
         // raise an event to process the action later in the lifecycle
         ShortcutEvent event = new ShortcutEvent(this, action, index);
         this.queueEvent(event);
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
      List<Node> items = (List<Node>)getValue();
      out.write(SHELF_START);
      if (items != null)
      {
         DictionaryService dd = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
         
         for (int i=0; i<items.size(); i++)
         {
            Node item = items.get(i);
            
            out.write("<tr><td width=16>");
            if (dd.isSubClass(item.getType(), ContentModel.TYPE_FOLDER))
            {
               // start row with correct node icon
               String icon = (String)item.getProperties().get("app:icon");
               if (icon != null)
               {
                  icon = "/images/icons/" + icon + "-16.gif";
               }
               else
               {
                  icon = WebResources.IMAGE_SPACE;
               }
               out.write(Utils.buildImageTag(context, icon, 16, 16, null, null, "absmiddle"));
            }
            else if (dd.isSubClass(item.getType(), ContentModel.TYPE_CONTENT))
            {
               String image = Utils.getFileTypeImage(item.getName(), true);
               out.write(Utils.buildImageTag(context, image, null, "absmiddle"));
            }
            
            // output cropped item label - we also output with no breaks, this is ok
            // as the copped label will ensure a sensible maximum width
            out.write("</td><td width=100%><nobr>&nbsp;");
            out.write(buildActionLink(ACTION_CLICK_ITEM, i, item.getName()));
            
            // output actions
            out.write("</nobr></td><td align=right><nobr>");
            out.write(buildActionLink(ACTION_REMOVE_ITEM, i, Application.getMessage(context, MSG_REMOVE_ITEM), WebResources.IMAGE_REMOVE));
            // TODO: add view details action here?
            
            // end actions cell and end row
            out.write("</nobr></td></tr>");
         }
      }
      
      out.write(SHELF_END);
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ShortcutEvent)
      {
         // found an event we should handle
         ShortcutEvent shortcutEvent = (ShortcutEvent)event;
         
         List<Node> items = (List<Node>)getValue();
         if (items != null && items.size() > shortcutEvent.Index)
         {
            // process the action
            switch (shortcutEvent.Action)
            {
               case ACTION_CLICK_ITEM:
                  Utils.processActionMethod(getFacesContext(), getClickActionListener(), shortcutEvent);
                  break;
               case ACTION_REMOVE_ITEM:
                  Utils.processActionMethod(getFacesContext(), getRemoveActionListener(), shortcutEvent);
                  break;
            }
         }
      }
      else
      {
         super.broadcast(event);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * We use a hidden field name on the assumption that only one Shortcut Shelf item
    * instance is present on a single page.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      return getClientId(getFacesContext());
   }
   
   /**
    * Build HTML for an link representing a Shortcut action
    * 
    * @param action     action indentifier to represent
    * @param index      index of the Node item this action relates too
    * @param text       of the action to display
    * 
    * @return HTML for action link
    */
   private String buildActionLink(int action, int index, String text)
   {
      FacesContext context = getFacesContext(); 
      
      StringBuilder buf = new StringBuilder(200);
      
      buf.append("<a href='#' onclick=\"");
      // generate JavaScript to set a hidden form field and submit
      // a form which request attributes that we can decode
      buf.append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), encodeValues(action, index)));
      buf.append("\">");
      
      buf.append(Utils.cropEncode(text));
      
      buf.append("</a>");
      
      return buf.toString();
   }
   
   /**
    * Build HTML for an link representing a clipboard action
    * 
    * @param action     action indentifier to represent
    * @param index      index of the clipboard item this action relates too
    * @param text       of the action to display
    * @param image      image icon to display
    * 
    * @return HTML for action link
    */
   private String buildActionLink(int action, int index, String text, String image)
   {
      FacesContext context = getFacesContext(); 
      
      StringBuilder buf = new StringBuilder(256);
      
      buf.append("<a href='#' onclick=\"");
      // generate JavaScript to set a hidden form field and submit
      // a form which request attributes that we can decode
      buf.append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), encodeValues(action, index)));
      buf.append("\">");
      
      if (image != null)
      {
         buf.append(Utils.buildImageTag(context, image, text));
      }
      else
      {
         buf.append(Utils.encode(text));
      }
      
      buf.append("</a>");
      
      return buf.toString();
   }
   
   /**
    * Encode the specified values for output to a hidden field
    * 
    * @param action     Action identifer
    * @param index      Index of the Node item the action is for
    * 
    * @return encoded values
    */
   private static String encodeValues(int action, int index)
   {
      return Integer.toString(action) + NamingContainer.SEPARATOR_CHAR + Integer.toString(index);
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the an action relevant to the Shortcut element.
    */
   public static class ShortcutEvent extends ActionEvent
   {
      public ShortcutEvent(UIComponent component, int action, int index)
      {
         super(component);
         Action = action;
         Index = index;
      }
      
      public int Action;
      public int Index;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data
   
   /** I18N messages */
   private static final String MSG_REMOVE_ITEM = "remove_item";
   
   private final static int ACTION_CLICK_ITEM = 0;
   private final static int ACTION_REMOVE_ITEM = 1;
   
   /** for this component the value is used as the List of shortcut Nodes */
   private Object value = null;
   
   /** action listener called when a Click action occurs */
   private MethodBinding clickActionListener;
   
   /** action listener called when a Remove action occurs */
   private MethodBinding removeActionListener;
}
