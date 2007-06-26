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
package org.alfresco.web.ui.repo.converter;

import java.nio.charset.Charset;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.i18n.I18NUtil;

/**
 * Converter class to convert a Charset to a String
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class CharsetConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.CharsetConverter";

   /**
    * {@inheritDoc}
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
   {
      return value;
   }

   /**
    * {@inheritDoc}
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
   {
      if (value == null)
      {
         throw new IllegalArgumentException(I18NUtil.getMessage("error_charset_null"));
      }
      
      String result = null;
      
      if (value instanceof Charset)
      {
         result = ((Charset)value).name();
      }
      else if (value != null)
      {
         result = value.toString();
      }
      
      return result;
   }
}
