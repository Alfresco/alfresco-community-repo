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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.web.app.servlet.FacesHelper;

/**
 * Converter class to convert a Locale into a language libelle 
 * 
 * @author Yannick Pignot
 */
public class LanguageConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.LanguageConverter";

   /**
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
   {   
      if(value == null)
      {
         throw new IllegalArgumentException(I18NUtil.getMessage("error_locale_null"));
      } 
      else
      {
         return value;
      }
   }

   /**
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
   {
      if(value == null)
      {
         throw new IllegalArgumentException(I18NUtil.getMessage("error_locale_null"));
      }
      
      //    if the component's rendrer type is  javax.faces.Text, return 
      //    the language label correponding to the received language code (as string) or the received locale
      else if(component.getRendererType().equalsIgnoreCase("javax.faces.Text"))
      {
         ContentFilterLanguagesService contentFilterLanguagesService = (ContentFilterLanguagesService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ContentFilterLanguagesService");
         
         return contentFilterLanguagesService.getLabelByCode(value.toString());
      }
      //    else don't modify 
      else
      {
         return value.toString();
      }                            
   }   
}
