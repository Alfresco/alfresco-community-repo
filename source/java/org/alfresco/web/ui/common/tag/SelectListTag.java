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

import javax.faces.component.UIComponent;

/**
 * @author Kevin Roast
 */
public class SelectListTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.SelectList";
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
      setBooleanProperty(component, "multiSelect", this.multiSelect);
      setBooleanProperty(component, "activeSelect", this.activeSelect);
      setStringStaticProperty(component, "var", this.var);
      setStringProperty(component, "itemStyle", this.itemStyle);
      setStringProperty(component, "itemStyleClass", this.itemStyleClass);
      setStringProperty(component, "value", this.value);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.multiSelect = null;
      this.activeSelect = null;
      this.var = null;
      this.itemStyle = null;
      this.itemStyleClass = null;
      this.value = null;
   }

   /**
    * Set the multi-select mode 
    *
    * @param multiSelect     the multi-select mode 
    */
   public void setMultiSelect(String multiSelect)
   {
      this.multiSelect = multiSelect;
   }
   
   /**
    * Set the active selection mode
    *
    * @param activeSelect     the active selection mode
    */
   public void setActiveSelect(String activeSelect)
   {
      this.activeSelect = activeSelect;
   }
   
   /**
    * Set the variable name for row item context
    *
    * @param var     the variable name for row item context
    */
   public void setVar(String var)
   {
      this.var = var;
   }
   
   /**
    * Set the item Style
    *
    * @param itemStyle     the item Style
    */
   public void setItemStyle(String itemStyle)
   {
      this.itemStyle = itemStyle;
   }

   /**
    * Set the item Style Class
    *
    * @param itemStyleClass     the item Style Class
    */
   public void setItemStyleClass(String itemStyleClass)
   {
      this.itemStyleClass = itemStyleClass;
   }
   
   /**
    * Set the selected value
    *
    * @param value     the selected value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /** the selected value */
   private String value;

   /** the itemStyle */
   private String itemStyle;

   /** the itemStyleClass */
   private String itemStyleClass;

   /** the multi-select mode */
   private String multiSelect;

   /** the active selection mode */
   private String activeSelect;
   
   /** the variable name for row item context */
   private String var;
}
