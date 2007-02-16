/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.repo.component.property.PropertySheetItem;

/**
 * Generates a component to represent a separator that uses the
 * property sheet display label configuration. The CSS class used
 * for the HTML representing the label can also be configured via
 * the <code>setStyleClass</code> method.
 * 
 * @author gavinc
 */
public class LabelSeparatorGenerator extends HtmlSeparatorGenerator
{
   protected String style = "margin-top: 6px; margin-bottom: 6px;";
   protected String styleClass;
   
   /**
    * Returns the CSS class configured to be used for this separator
    * 
    * @return The CSS class
    */
   public String getStyleClass()
   {
      return styleClass;
   }

   /**
    * Sets the CSS class to use for the separator
    * 
    * @param styleClass The CSS class
    */
   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }
   
   /**
    * Returns the CSS style configured to be used for this separator
    * 
    * @return The CSS style
    */
   public String getStyle()
   {
      return style;
   }

   /**
    * Sets the CSS style to use for the separator
    * 
    * @param style The CSS style
    */
   public void setStyle(String style)
   {
      this.style = style;
   }

   @Override
   protected String getResolvedHtml(UIComponent component, PropertySheetItem item)
   {
      StringBuilder htmlBuilder = new StringBuilder("<div");
      if (this.styleClass != null && this.styleClass.length() > 0)
      {
         htmlBuilder.append(" class=\"");
         htmlBuilder.append(this.styleClass);
         htmlBuilder.append("\"");
      }
      
      if (this.style != null && this.style.length() > 0)
      {
         htmlBuilder.append(" style=\"");
         htmlBuilder.append(this.style);
         htmlBuilder.append("\"");
      }
      
      // add the display label and close the div
      htmlBuilder.append(">&nbsp;");
      htmlBuilder.append(item.getDisplayLabel());
      htmlBuilder.append("</div>");
      
      return htmlBuilder.toString();
   }
}
