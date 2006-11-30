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
 * @author kevinr
 */
public class ListItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ListItem";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // this component is rendered by its parent container
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "tooltip", this.tooltip);
      setStringProperty(component, "label", this.label);
      setStringProperty(component, "description", this.description);
      setStringProperty(component, "image", this.image);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.tooltip = null;
      this.label = null;
      this.description = null;
      this.image = null;
      this.value = null;
      this.disabled = null;
   }
   
   /**
    * Set the tooltip
    *
    * @param tooltip     the tooltip
    */
   public void setTooltip(String tooltip)
   {
      this.tooltip = tooltip;
   }

   /**
    * Set the label
    *
    * @param label     the label
    */
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   /**
    * Set the description
    *
    * @param description     the description
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Set the image
    *
    * @param image     the image
    */
   public void setImage(String image)
   {
      this.image = image;
   }

   /**
    * Set the value to be selected initially 
    *
    * @param value     the value to be selected initially
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the disabled flag
    * 
    * @param disabled true to set this item as disabled
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }

   /** the tooltip */
   private String tooltip;

   /** the label */
   private String label;

   /** the image */
   private String image;

   /** the value to be selected initially */
   private String value;
   
   /** the disabled flag */
   private String disabled;
   
   /** the description */
   private String description;
}
