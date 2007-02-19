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

import java.util.Collection;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.alfresco.web.app.Application;

/**
 * Converter class to convert a Boolean value (including null) into a human readable form.
 * 
 * @author Kevin Roast
 */
public class BooleanLabelConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.BooleanLabelConverter";
   
   private static final String MSG_YES = "yes";
   private static final String MSG_NO  = "no";
   
   /**
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException
   {
      return Boolean.valueOf(value);
   }

   /**
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
         throws ConverterException
   {
      ResourceBundle bundle = Application.getBundle(context);
      
      String result = bundle.getString(MSG_NO);
      
      if (value instanceof Boolean)
      {
         result = ((Boolean)value).booleanValue() ? bundle.getString(MSG_YES) : bundle.getString(MSG_NO);
      }
      else if (value instanceof Collection)
      {
         StringBuilder buffer = new StringBuilder();
         for (Object obj : (Collection)value)
         {
            if (buffer.length() != 0)
            {
               buffer.append(", ");
            }
            
            if (obj instanceof Boolean)
            {
               buffer.append(((Boolean)obj).booleanValue() ? 
                     bundle.getString(MSG_YES) : bundle.getString(MSG_NO));
            }
            else
            {
               buffer.append(obj.toString());
            }
         }
         
         result = buffer.toString();
      }
      
      return result;
   }
}
