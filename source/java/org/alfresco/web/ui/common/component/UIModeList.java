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

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

/**
 * @author Kevin Roast
 */
public class UIModeList extends UICommand
{
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default constructor
    */
   public UIModeList()
   {
      setRendererType("org.alfresco.faces.ModeListRenderer");
   }
   
   
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
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.iconColumnWidth = (Integer)values[1];
      this.horizontal = (Boolean)values[2];
      this.disabled = (Boolean)values[3];
      this.label = (String)values[4];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.iconColumnWidth;
      values[2] = this.horizontal;
      values[3] = this.disabled;
      values[4] = this.label;
      return (values);
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ModeListItemSelectedEvent)
      {
         // found an event for us, update the value for this component
         setValue( ((ModeListItemSelectedEvent)event).SelectedValue );
      }
      
      // default ActionEvent processing for a UICommand
      super.broadcast(event);
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed property accessors 
   
   /**
    * Get the horizontal rendering flag
    *
    * @return true for horizontal rendering, false otherwise
    */
   public boolean isHorizontal()
   {
      ValueBinding vb = getValueBinding("horizontal");
      if (vb != null)
      {
         this.horizontal = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.horizontal != null)
      {
         return this.horizontal.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Set true for horizontal rendering, false otherwise
    *
    * @param horizontal       the horizontal
    */
   public void setHorizontal(boolean horizontal)
   {
      this.horizontal = horizontal;
   }
   
   /**
    * Get the icon column width
    *
    * @return the icon column width
    */
   public int getIconColumnWidth()
   {
      ValueBinding vb = getValueBinding("iconColumnWidth");
      if (vb != null)
      {
         this.iconColumnWidth = (Integer)vb.getValue(getFacesContext());
      }
      
      if (this.iconColumnWidth != null)
      {
         return this.iconColumnWidth.intValue();
      }
      else
      {
         // return the default
         return 20;
      }
   }

   /**
    * Set the icon column width
    *
    * @param iconColumnWidth     the icon column width
    */
   public void setIconColumnWidth(int iconColumnWidth)
   {
      this.iconColumnWidth = Integer.valueOf(iconColumnWidth);
   }

   /**
    * Returns the disabled flag
    * 
    * @return true if the mode list is disabled
    */
   public boolean isDisabled()
   {
      ValueBinding vb = getValueBinding("disabled");
      if (vb != null)
      {
         this.disabled = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.disabled != null)
      {
         return this.disabled.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Sets whether the mode list is disabled
    * 
    * @param disabled   the disabled flag
    */
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
   
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
   
   
   // ------------------------------------------------------------------------------
   // Private data 

   /** the icon column width */
   private Integer iconColumnWidth;

   /** true for horizontal rendering, false otherwise */
   private Boolean horizontal = null;
   
   /** disabled flag */
   private Boolean disabled = null;
   
   /** the label */
   private String label;
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing a change in selection for a ModeList component.
    */
   public static class ModeListItemSelectedEvent extends ActionEvent
   {
      private static final long serialVersionUID = 3618135654274774322L;

      public ModeListItemSelectedEvent(UIComponent component, Object selectedValue)
      {
         super(component);
         SelectedValue = selectedValue;
      }
      
      public Object SelectedValue = null;
   }
}
