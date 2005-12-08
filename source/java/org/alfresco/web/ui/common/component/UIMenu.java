/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.common.component;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.web.ui.common.Utils;

/**
 * @author Kevin Roast
 */
public class UIMenu extends SelfRenderingComponent
{
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Controls";
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
      
      // output a textual label with an optional icon to show the menu
      String menuId = getNextMenuId(context);
      out.write("<a href='#' onclick=\"javascript:_toggleMenu(event, '");
      out.write(menuId);
      out.write("');return false;\"");
      outputAttribute(out, getAttributes().get("style"), "style");
      outputAttribute(out, getAttributes().get("styleClass"), "class");
      outputAttribute(out, getTooltip(), "title");
      out.write('>');
      
      // output label text
      String label = getLabel();
      if (label != null)
      {
         out.write("<span>");
         out.write(Utils.encode(label));
         out.write("</span>");
      }
      
      // output image
      if (getAttributes().get("image") != null)
      {
         out.write(Utils.buildImageTag(context, (String)getAttributes().get("image"), null, "absmiddle"));
      }
      
      out.write("</a>");
      
      // output the hidden DIV section to contain the menu item table
      // also output the javascript handlers used to hide the menu after a delay of non-use
      out.write("<br><div id='");
      out.write(menuId);
      out.write("' style=\"position:absolute;display:none;padding-left:2px;\">");
      out.write("<table border=0 cellpadding=0");
      outputAttribute(out, getAttributes().get("itemSpacing"), "cellspacing");
      outputAttribute(out, getAttributes().get("menuStyle"), "style");
      outputAttribute(out, getAttributes().get("menuStyleClass"), "class");
      out.write(">");
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
      
      // end the menu table and the hidden DIV section
      out.write("</table></div>");
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.label = (String)values[1];
      this.tooltip = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.label;
      values[2] = this.tooltip;
      return values;
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      ValueBinding vb = getValueBinding("label");
      if (vb != null)
      {
         this.label = (String)vb.getValue(getFacesContext());
      }
      return this.label;
   }

   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return Returns the tooltip.
    */
   public String getTooltip()
   {
      ValueBinding vb = getValueBinding("tooltip");
      if (vb != null)
      {
         this.tooltip = (String)vb.getValue(getFacesContext());
      }
      return this.tooltip;
   }

   /**
    * @param tooltip The tooltip to set.
    */
   public void setTooltip(String tooltip)
   {
      this.tooltip = tooltip;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Return the next usable menu DIV id in a sequence
    * 
    * @param context       FacesContext
    * 
    * @return next menu ID
    */
   private String getNextMenuId(FacesContext context)
   {
      Integer val = (Integer)context.getExternalContext().getRequestMap().get(MENU_ID_KEY);
      if (val == null)
      {
         val = Integer.valueOf(0);
      }
      
      // build next id in sequence
      String id = getClientId(context) + '_' + val.toString();
      
      // save incremented value in the request ready for next menu component instance
      val = Integer.valueOf( val.intValue() + 1 );
      context.getExternalContext().getRequestMap().put(MENU_ID_KEY, val);
      
      return id;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private members
   
   private final static String MENU_ID_KEY = "__awc_menu_id";
   
   private String label;
   
   private String tooltip;
}
