/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
      this.startYear = null;
      this.yearCount = null;
      this.value = null;
      this.showTime = null;
      this.disabled = null;
      this.initIfNull = null;
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
      setBooleanProperty(component, "initialiseIfNull", this.initIfNull);
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
   
   /**
    * Sets whether today's date should be shown initially if the underlying
    * model value is null. This will also hide the None button thus disallowing
    * the user to set the date back to null.
    * 
    * @param initialiseIfNull true to show today's date instead of 'None'
    */
   public void setInitialiseIfNull(String initialiseIfNull)
   {
      this.initIfNull = initialiseIfNull;
   }
   
   private String startYear = null;
   private String yearCount = null;
   private String value = null;
   private String showTime = null;
   private String disabled = null;
   private String initIfNull = null;
}
