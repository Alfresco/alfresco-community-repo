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
package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

/**
 * Tag to place the image picker component and radio renderer inside
 * a rounded corner panel
 * 
 * @author gavinc
 */
public class ImagePickerRadioPanelTag extends ImagePickerRadioTag
{
   private String panelBorder;
   private String panelBgcolor;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ImagePicker";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "org.alfresco.faces.RadioPanel";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "panelBorder", this.panelBorder);
      setStringProperty(component, "panelBgcolor", this.panelBgcolor);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.panelBorder = null;
      this.panelBgcolor = null;
   }

   public String getPanelBgcolor()
   {
      return panelBgcolor;
   }

   public void setPanelBgcolor(String panelBgcolor)
   {
      this.panelBgcolor = panelBgcolor;
   }

   public String getPanelBorder()
   {
      return panelBorder;
   }

   public void setPanelBorder(String panelBorder)
   {
      this.panelBorder = panelBorder;
   }
}
