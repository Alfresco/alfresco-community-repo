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
package org.alfresco.web.ui.common.converter;

import java.text.DecimalFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.web.app.Application;

/**
 * Converter class to convert the size of an item in bytes into a readable KB/MB form.
 * 
 * @author Kevin Roast
 */
public class ByteSizeConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.ByteSizeConverter";

   private static final String MSG_POSTFIX_KB = "kilobyte";
   private static final String MSG_POSTFIX_MB = "megabyte";
   private static final String MSG_POSTFIX_GB = "gigabyte";
   
   private static final String NUMBER_PATTERN = "###,###.##";
   
   /**
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
   {
      return Long.parseLong(value);
   }

   /**
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
   {
      long size;
      if (value instanceof Long)
      {
         size = (Long)value;
      }
      else if (value instanceof String)
      {
         try
         {
            size = Long.parseLong((String)value);
         }
         catch (NumberFormatException ne)
         {
            return (String)value;
         }
      }
      else
      {
         return "";
      }
      
      // get formatter
      // TODO: can we cache this instance...? DecimalFormat is not threadsafe! Need threadlocal instance.
      DecimalFormat formatter = new DecimalFormat(NUMBER_PATTERN);
      
      StringBuilder buf = new StringBuilder();
      
      if (size < 999999)
      {
         double val = ((double)size) / 1024.0;
         buf.append(formatter.format(val))
            .append(' ')
            .append(Application.getMessage(context, MSG_POSTFIX_KB));
      }
      else if (size < 999999999)
      {
         double val = ((double)size) / 1048576.0;
         buf.append(formatter.format(val))
            .append(' ')
            .append(Application.getMessage(context, MSG_POSTFIX_MB));
      }
      else
      {
         double val = ((double)size) / 1073741824.0;
         buf.append(formatter.format(val))
            .append(' ')
            .append(Application.getMessage(context, MSG_POSTFIX_GB));
      }
      
      return buf.toString();
   }
}
