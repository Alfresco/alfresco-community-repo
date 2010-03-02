/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.repo.tag.shelf;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;
import org.alfresco.web.ui.repo.component.shelf.UIShortcutsShelfItem;

/**
 * @author Kevin Roast
 */
public class ShortcutsShelfItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ShortcutsShelfItem";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // self rendering component
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringBindingProperty(component, "value", this.value);
      if (isValueReference(this.clickActionListener))
      {
         MethodBinding vb = getFacesContext().getApplication().createMethodBinding(this.clickActionListener, ACTION_CLASS_ARGS);
         ((UIShortcutsShelfItem)component).setClickActionListener(vb);
      }
      else
      {
         throw new FacesException("Click Action listener method binding incorrectly specified: " + this.clickActionListener);
      }
      if (isValueReference(this.removeActionListener))
      {
         MethodBinding vb = getFacesContext().getApplication().createMethodBinding(this.removeActionListener, ACTION_CLASS_ARGS);
         ((UIShortcutsShelfItem)component).setRemoveActionListener(vb);
      }
      else
      {
         throw new FacesException("Remove Action listener method binding incorrectly specified: " + this.clickActionListener);
      }
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.value = null;
      this.clickActionListener = null;
      this.removeActionListener = null;
   }
   
   /**
    * Set the value used to bind the shortcuts list to the component
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the clickActionListener
    *
    * @param clickActionListener     the clickActionListener
    */
   public void setClickActionListener(String clickActionListener)
   {
      this.clickActionListener = clickActionListener;
   }
   
   /**
    * Set the removeActionListener
    *
    * @param removeActionListener     the removeActionListener
    */
   public void setRemoveActionListener(String removeActionListener)
   {
      this.removeActionListener = removeActionListener;
   }


   /** the clickActionListener */
   private String clickActionListener;
   
   /** the removeActionListener */
   private String removeActionListener;
   
   /** the value */
   private String value;
}
