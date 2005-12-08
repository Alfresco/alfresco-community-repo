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
 * 
 * Tag handler for an Input UI Component specific for Date input.
 * 
 * This tag collects the user params needed to specify an Input component to allow
 * the user to enter a date. It specifies the renderer as below to be our Date
 * specific renderer. This renderer is configured in the faces-config.xml.  
 */
public class InputDatePickerTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      // we are require an Input component to manage our state
      // this is just a convention name Id - not an actual class
      return "javax.faces.Input";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the renderer type is a convention name Id - not an actual class
      // see the <render-kit> in faces-config.xml
      return "org.alfresco.faces.DatePickerRenderer";
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.startYear = "1990";
      this.yearCount = "10";
      this.value = null;
      this.showTime = null;
      this.disabled = null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      // set the properties of tag into the component
      setIntProperty(component, "startYear", this.startYear);
      setIntProperty(component, "yearCount", this.yearCount);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "showTime", this.showTime);
      setBooleanProperty(component, "disabled", this.disabled);
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
    * Set the startYear
    *
    * @param startYear     the startYear
    */
   public void setStartYear(String startYear)
   {
      this.startYear = startYear;
   }

   /**
    * Set the yearCount
    *
    * @param yearCount     the yearCount
    */
   public void setYearCount(String yearCount)
   {
      this.yearCount = yearCount;
   }
   
   /**
    * Determines whether the time is rendered
    * 
    * @param showTime true to allow the time to be edited
    */
   public void setShowTime(String showTime)
   {
      this.showTime = showTime;
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
   
   private String startYear = "1990";
   private String yearCount = "10";
   private String value = null;
   private String showTime = null;
   private String disabled = null;
}
