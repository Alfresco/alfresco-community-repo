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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import org.alfresco.web.ui.common.Utils;

/**
 * @author Kevin Roast
 */
public class UISelectList extends UICommand
{
   private Boolean multiSelect;
   private String buttonLabel;
   
   
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default Constructor
    */
   public UISelectList()
   {
      setRendererType(null);
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
      this.multiSelect = (Boolean)values[1];
      this.buttonLabel = (String)values[2];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.multiSelect;
      values[2] = this.buttonLabel;
      return (values);
   }
   
   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void decode(FacesContext context, UIComponent component)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName(context, component);
      String value = (String)requestMap.get(fieldId);
      
      // we encoded the value to start with our Id
      if (value != null && value.startsWith(component.getClientId(context) + NamingContainer.SEPARATOR_CHAR))
      {
         String selectedValue = value.substring(component.getClientId(context).length() + 1);
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
      
      // get the child components and look for compatible ListItem objects
      for (Iterator i = getChildren().iterator(); i.hasNext(); /**/)
      {
         UIComponent child = (UIComponent)i.next();
         if (child instanceof UIListItems)
         {
            // get the value of the list items component and iterate through it's collection
            Object listItems = ((UIListItems)child).getValue();
            if (listItems instanceof Collection)
            {
               for (Iterator iter = ((Collection)listItems).iterator(); iter.hasNext(); /**/)
               {
                  UIListItem item = (UIListItem)iter.next();
                  if (item.isRendered())
                  {
                     renderItem(context, out, item);
                  }
               }
            }
         }
         else if (child instanceof UIListItem)
         {
            if (child.isRendered())
            {
               // found a valid UIListItem child to render
               UIListItem item = (UIListItem)child;
               renderItem(context, out, item);
            }
         }
      }
   }
   
   /**
    * Render a list item in the appropriate selection mode
    * 
    * @param context    FacesContext
    * @param out        ResponseWriter
    * @param item       UIListItem representing the item to render
    */
   private void renderItem(FacesContext context, ResponseWriter out, UIListItem item)
   {
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed property accessors 
   
   /**
    * Get the multi-select rendering flag
    *
    * @return true for multi-select rendering, false otherwise
    */
   public boolean isMultiSelect()
   {
      ValueBinding vb = getValueBinding("multiSelect");
      if (vb != null)
      {
         this.multiSelect = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.multiSelect != null)
      {
         return this.multiSelect.booleanValue();
      }
      else
      {
         // return the default
         return false;
      }
   }

   /**
    * Set true for multi-select rendering, false otherwise
    *
    * @param multiSelect      True for multi-select
    */
   public void setMultiSelect(boolean multiSelect)
   {
      this.multiSelect = multiSelect;
   }
   
   /**
    * @return Returns the action button label.
    */
   public String getButtonLabel()
   {
      ValueBinding vb = getValueBinding("buttonLabel");
      if (vb != null)
      {
         this.buttonLabel = (String)vb.getValue(getFacesContext());
      }
      
      return this.buttonLabel;
   }

   /**
    * @param buttonLabel      The action button label to set.
    */
   public void setButtonLabel(String buttonLabel)
   {
      this.buttonLabel = buttonLabel;
   }
   
   
   /**
    * We use a hidden field name based on the parent form component Id and
    * the string "selectlist" to give a hidden field name that can be shared by all
    * SelectList components within a single UIForm component.
    * 
    * @return hidden field name
    */
   private static String getHiddenFieldName(FacesContext context, UIComponent component)
   {
      UIForm form = Utils.getParentForm(context, component);
      return form.getClientId(context) + NamingContainer.SEPARATOR_CHAR + "selectlist";
   }
}
