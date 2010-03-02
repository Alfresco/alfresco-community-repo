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
package org.alfresco.web.ui.wcm.tag;

import javax.faces.component.UIComponent;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * Tag class that allows the DeploymentReports component to be used on a JSP page.
 * 
 * @author gavinc
 */
public class DeploymentReportsTag extends BaseComponentTag
{
   private String value;
   private String showPrevious;
   private String dateFilter;
   private String attempt;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.DeploymentReports";
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
      
      setStringProperty(component, "value", this.value);
      setStringProperty(component, "dateFilter", this.dateFilter);
      setStringProperty(component, "attempt", this.attempt);
      setBooleanProperty(component, "showPrevious", this.showPrevious);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.showPrevious = null;
      this.dateFilter = null;
      this.attempt = null;
   }
   
   /**
    * Set the value (NodeRef of the web project)
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   public void setShowPrevious(String showPrevious)
   {
      this.showPrevious = showPrevious;
   }

   public void setDateFilter(String dateFilter)
   {
      this.dateFilter = dateFilter;
   }

   public void setAttempt(String attempt)
   {
      this.attempt = attempt;
   }
}
