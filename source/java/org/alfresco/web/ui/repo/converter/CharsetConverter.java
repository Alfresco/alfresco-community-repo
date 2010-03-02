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
package org.alfresco.web.ui.repo.converter;

import java.nio.charset.Charset;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.extensions.surf.util.I18NUtil;

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
