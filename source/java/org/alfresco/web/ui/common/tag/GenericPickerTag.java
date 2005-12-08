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
package org.alfresco.web.ui.common.tag;

import javax.faces.FacesException;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import org.alfresco.web.ui.common.component.UIGenericPicker;

/**
 * @author Kevin Roast
 */
public class GenericPickerTag extends BaseComponentTag
{
   private final static Class QUERYCALLBACK_CLASS_ARGS[] = {int.class, String.class};
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.GenericPicker";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setBooleanProperty(component, "showFilter", this.showFilter);
      setBooleanProperty(component, "showContains", this.showContains);
      setBooleanProperty(component, "showAddButton", this.showAddButton);
      setBooleanProperty(component, "filterRefresh", this.filterRefresh);
      setStringProperty(component, "addButtonLabel", this.addButtonLabel);
      setActionProperty((UICommand)component, this.action);
      setActionListenerProperty((UICommand)component, this.actionListener);
      setIntProperty(component, "width", this.width);
      setIntProperty(component, "height", this.height);
      setStringBindingProperty(component, "filters", this.filters);
      if (queryCallback != null)
      {
         if (isValueReference(queryCallback))
         {
            MethodBinding b = getFacesContext().getApplication().createMethodBinding(queryCallback, QUERYCALLBACK_CLASS_ARGS);
            ((UIGenericPicker)component).setQueryCallback(b);
         }
         else
         {
            throw new FacesException("Query Callback method binding incorrectly specified: " + queryCallback);
         }
      }
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.showFilter = null;
      this.showContains = null;
      this.showAddButton = null;
      this.addButtonLabel = null;
      this.action = null;
      this.actionListener = null;
      this.width = null;
      this.height = null;
      this.queryCallback = null;
      this.filters = null;
      this.filterRefresh = null;
   }
   
   /**
    * Set the showFilter
    *
    * @param showFilter     the showFilter
    */
   public void setShowFilter(String showFilter)
   {
      this.showFilter = showFilter;
   }

   /**
    * Set the showContains
    *
    * @param showContains     the showContains
    */
   public void setShowContains(String showContains)
   {
      this.showContains = showContains;
   }

   /**
    * Set the showAddButton
    *
    * @param showAddButton     the showAddButton
    */
   public void setShowAddButton(String showAddButton)
   {
      this.showAddButton = showAddButton;
   }

   /**
    * Set the addButtonLabel
    *
    * @param addButtonLabel     the addButtonLabel
    */
   public void setAddButtonLabel(String addButtonLabel)
   {
      this.addButtonLabel = addButtonLabel;
   }

   /**
    * Set the action
    *
    * @param action     the action
    */
   public void setAction(String action)
   {
      this.action = action;
   }

   /**
    * Set the actionListener
    *
    * @param actionListener     the actionListener
    */
   public void setActionListener(String actionListener)
   {
      this.actionListener = actionListener;
   }

   /**
    * Set the width
    *
    * @param width     the width
    */
   public void setWidth(String width)
   {
      this.width = width;
   }

   /**
    * Set the height
    *
    * @param height     the height
    */
   public void setHeight(String height)
   {
      this.height = height;
   }

   /**
    * Set the queryCallback
    *
    * @param queryCallback     the queryCallback
    */
   public void setQueryCallback(String queryCallback)
   {
      this.queryCallback = queryCallback;
   }
   
   /**
    * Set the filters
    *
    * @param filters     the filters
    */
   public void setFilters(String filters)
   {
      this.filters = filters;
   }
   
   /**
    * Set the filterRefresh
    *
    * @param filterRefresh     the filterRefresh
    */
   public void setFilterRefresh(String filterRefresh)
   {
      this.filterRefresh = filterRefresh;
   }


   /** the filterRefresh */
   private String filterRefresh;
   
   /** the filters */
   private String filters;
      
   /** the queryCallback */
   private String queryCallback;

   /** the showFilter */
   private String showFilter;

   /** the showContains */
   private String showContains;

   /** the showAddButton */
   private String showAddButton;

   /** the addButtonLabel */
   private String addButtonLabel;

   /** the action */
   private String action;

   /** the actionListener */
   private String actionListener;

   /** the width */
   private String width;

   /** the height */
   private String height;
}
