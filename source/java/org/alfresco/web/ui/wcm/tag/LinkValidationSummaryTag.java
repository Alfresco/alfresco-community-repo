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

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

/**
 * Tag class for adding the UILinkValidationSummary component to a JSP page.
 * 
 * @author gavinc
 */
public class LinkValidationSummaryTag extends HtmlComponentTag
{
   private String value;
   private String showPanel;
   private String showTitle;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.LinkValidationSummary";
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
      setBooleanProperty(component, "showPanel", this.showPanel);
      setBooleanProperty(component, "showTitle", this.showTitle);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.showPanel = null;
      this.showTitle = null;
   }
   
   /**
    * @param value the value (the list of servers to deploy to)
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * @param showPanel false to hide the surrounding panel
    */
   public void setShowPanel(String showPanel)
   {
      this.showPanel = showPanel;
   }

   /**
    * @param showTitle false to hide the title within the panel
    */
   public void setShowTitle(String showTitle)
   {
      this.showTitle = showTitle;
   }
}
