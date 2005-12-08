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
package org.alfresco.web.ui.common.tag.data;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.BaseComponentTag;


/**
 * @author kevinr
 */
public class RichListTag extends BaseComponentTag
{
   // ------------------------------------------------------------------------------
   // Component methods 
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.RichList";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.RichListRenderer";
   }

   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.var = null;
      this.initialSortColumn = null;
      this.initialSortDescending = null;
      this.listConfig = null;
      this.viewMode = null;      
      this.style = null;
      this.styleClass = null;
      this.rowStyleClass = null;
      this.altRowStyleClass = null;
      this.headerStyleClass = null;
      this.width = null;
      this.pageSize = null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringBindingProperty(component, "value", this.value);
      setStringStaticProperty(component, "var", this.var);
      setStringProperty(component, "initialSortColumn", this.initialSortColumn);
      setBooleanProperty(component, "initialSortDescending", this.initialSortDescending);
      setStringProperty(component, "listConfig", this.listConfig);
      setStringProperty(component, "viewMode", this.viewMode);
      setStringProperty(component, "style", this.style);
      setStringProperty(component, "styleClass", this.styleClass);
      setStringProperty(component, "rowStyleClass", this.rowStyleClass);
      setStringProperty(component, "altRowStyleClass", this.altRowStyleClass);
      setStringProperty(component, "headerStyleClass", this.headerStyleClass);
      setStringProperty(component, "width", this.width);
      setIntProperty(component, "pageSize", this.pageSize);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean implementation 
   
   /**
    * Set the viewMode
    *
    * @param viewMode     the viewMode
    */
   public void setViewMode(String viewMode)
   {
      this.viewMode = viewMode;
   }
   
   /**
    * Set the pageSize
    *
    * @param pageSize     the pageSize
    */
   public void setPageSize(String pageSize)
   {
      this.pageSize = pageSize;
   }
   
   /**
    * Set the initialSortColumn
    *
    * @param initialSortColumn     the initialSortColumn
    */
   public void setInitialSortColumn(String initialSortColumn)
   {
      this.initialSortColumn = initialSortColumn;
   }
   
   /**
    * Set the initialSortDescending
    *
    * @param initialSortDescending     the initialSortDescending
    */
   public void setInitialSortDescending(String initialSortDescending)
   {
      this.initialSortDescending = initialSortDescending;
   }
   
   /**
    * Set the listConfig
    *
    * @param listConfig     the listConfig
    */
   public void setListConfig(String listConfig)
   {
      this.listConfig = listConfig;
   }

   /**
    * Set the value
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Set the var
    *
    * @param var     the var
    */
   public void setVar(String var)
   {
      this.var = var;
   }
   
   /**
    * Set the style
    *
    * @param style     the style
    */
   public void setStyle(String style)
   {
      this.style = style;
   }

   /**
    * Set the styleClass
    *
    * @param styleClass     the styleClass
    */
   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }
   
   /**
    * Set the the row CSS Class
    *
    * @param rowStyleClass     the the row CSS Class
    */
   public void setRowStyleClass(String rowStyleClass)
   {
      this.rowStyleClass = rowStyleClass;
   }

   /**
    * Set the alternate row CSS Class
    *
    * @param altRowStyleClass     the alternate row CSS Class
    */
   public void setAltRowStyleClass(String altRowStyleClass)
   {
      this.altRowStyleClass = altRowStyleClass;
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
    * Set the header row CSS Class
    *
    * @param headerStyleClass     the header row CSS Class
    */
   public void setHeaderStyleClass(String headerStyleClass)
   {
      this.headerStyleClass = headerStyleClass;
   }


   // ------------------------------------------------------------------------------
   // Private data
   
   /** the header row CSS Class */
   private String headerStyleClass;
   
   /** the row CSS Class */
   private String rowStyleClass;

   /** the alternate row CSS Class */
   private String altRowStyleClass;
   
   /** the style */
   private String style;

   /** the styleClass */
   private String styleClass;
   
   /** the width */
   private String width;

   /** the value */
   private String value;

   /** the var */
   private String var;

   /** the viewMode */
   private String viewMode;

   /** the pageSize */
   private String pageSize;

   /** the initialSortColumn */
   private String initialSortColumn;

   /** the initialSortDescending */
   private String initialSortDescending;

   /** the listConfig */
   private String listConfig;
}
