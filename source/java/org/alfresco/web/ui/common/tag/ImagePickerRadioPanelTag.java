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
