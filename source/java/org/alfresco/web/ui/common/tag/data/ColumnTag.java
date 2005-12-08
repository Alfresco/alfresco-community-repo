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
public class ColumnTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.RichListColumn";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is renderer by the parent
      return null;
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.primary = null;
      this.actions = null;
      this.width = null;
      this.style = null;
      this.styleClass = null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setBooleanProperty(component, "primary", this.primary);
      setBooleanProperty(component, "actions", this.actions);
      setStringProperty(component, "width", this.width);
      setStringProperty(component, "style", this.style);
      setStringProperty(component, "styleClass", this.styleClass);
   }
   
   
   // ------------------------------------------------------------------------------
   // Tag properties

   /**
    * Set if this is the primary column
    *
    * @param primary     the primary if "true", otherwise false
    */
   public void setPrimary(String primary)
   {
      this.primary = primary;
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
    * Set if this is the actions column
    *
    * @param actions     the actions if "true", otherwise false
    */
   public void setActions(String actions)
   {
      this.actions = actions;
   }


   /** the actions */
   private String actions;
   
   /** the style */
   private String style;

   /** the styleClass */
   private String styleClass;

   /** the width */
   private String width;

   /** the primary */
   private String primary;
}
