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
package org.alfresco.web.ui.common.component.data;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author Kevin Roast
 */
public class UIColumn extends UIComponentBase
{
   // ------------------------------------------------------------------------------
   // Component implementation 
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Data";
   }
   
   /**
    * Return the UI Component to be used as the header for this column
    * 
    * @return UIComponent
    */
   public UIComponent getHeader()
   {
      return getFacet("header");
   }
   
   /**
    * Return the UI Component to be used as the footer for this column
    * 
    * @return UIComponent
    */
   public UIComponent getFooter()
   {
      return getFacet("footer");
   }
   
   /**
    * Return the UI Component to be used as the large icon for this column
    * 
    * @return UIComponent
    */
   public UIComponent getLargeIcon()
   {
      return getFacet("large-icon");
   }
   
   /**
    * Return the UI Component to be used as the small icon for this column
    * 
    * @return UIComponent
    */
   public UIComponent getSmallIcon()
   {
      return getFacet("small-icon");
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.primary = ((Boolean)values[1]).booleanValue();
      this.actions = ((Boolean)values[2]).booleanValue();
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = (this.primary ? Boolean.TRUE : Boolean.FALSE);
      values[2] = (this.actions ? Boolean.TRUE : Boolean.FALSE);
      return (values);
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return true if this is the primary column
    */
   public boolean getPrimary()
   {
      ValueBinding vb = getValueBinding("primary");
      if (vb != null)
      {
         this.primary = (Boolean)vb.getValue(getFacesContext());
      }
      return this.primary;
   }
   
   /**
    * @param primary    True if this is the primary column, false otherwise
    */
   public void setPrimary(boolean primary)
   {
      this.primary = primary;
   }
   
   /**
    * @return true if this is the column containing actions for the current row
    */
   public boolean getActions()
   {
      ValueBinding vb = getValueBinding("actions");
      if (vb != null)
      {
         this.actions = (Boolean)vb.getValue(getFacesContext());
      }
      return this.actions;
   }
   
   /**
    * @param actions    True if this is the column containing actions for the current row
    */
   public void setActions(boolean actions)
   {
      this.actions = actions;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private data 
   
   private boolean primary = false;
   private boolean actions = false;
}
