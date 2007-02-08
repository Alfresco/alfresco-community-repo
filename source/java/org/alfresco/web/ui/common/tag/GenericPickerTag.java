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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.ui.common.tag;

import javax.faces.FacesException;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

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
      setBooleanProperty(component, "multiSelect", this.multiSelect);
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
      this.multiSelect = null;
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
   
   /**
    * Set the multiSelect
    *
    * @param mutliSelect      the multiSelect
    */
   public void setMultiSelect(String multiSelect)
   {
      this.multiSelect = multiSelect;
   }

   /** the multiSelect */
   private String multiSelect;
   
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
