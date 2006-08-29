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
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Tag class to allow the content selector component to be added to a JSP page
 * 
 * @author gavinc
 */
public class ContentSelectorTag extends HtmlComponentTag
{
   private String availableOptionsSize;
   private String disabled;
   private String value;
   private String multiSelect;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ContentSelector";
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
      
      setStringStaticProperty(component, "availableOptionsSize", this.availableOptionsSize);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "disabled", this.disabled);
      setBooleanProperty(component, "multiSelect", this.multiSelect);
   }
   
   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * @param availableOptionsSize Sets the size of the available options size when 
    *        multiple items can be selected
    */
   public void setAvailableOptionsSize(String availableOptionsSize)
   {
      this.availableOptionsSize = availableOptionsSize;
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
   
   /**
    * Sets whether the component should be rendered in a disabled state
    * 
    * @param disabled true to render the component in a disabled state
    */
   public void setDisabled(String disabled)
   {
      this.disabled = disabled;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#release()
    */
   public void release()
   {
      this.availableOptionsSize = null;
      this.disabled = null;
      this.value = null;
      this.multiSelect = null;

      super.release();
   }
}
