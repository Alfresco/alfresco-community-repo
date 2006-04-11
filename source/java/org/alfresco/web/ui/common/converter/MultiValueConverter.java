/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.common.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.repo.RepoConstants;

/**
 * Converter class to convert a List of multiple values into a comma
 * separated list.
 * 
 * @author gavinc
 */
public class MultiValueConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.MultiValueConverter";
   
   /**
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException
   {
      List<String> items = new ArrayList<String>();
      StringTokenizer tokenizer = new StringTokenizer(value, ",");
      while (tokenizer.hasMoreTokens())
      {
         items.add(tokenizer.nextToken());
      }
      
      return items;
   }

   /**
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
         throws ConverterException
   {
      String result = null;
      
      if (value instanceof Collection)
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
               Converter boolLabel = context.getApplication().createConverter(
                     RepoConstants.ALFRESCO_FACES_BOOLEAN_CONVERTER);
               buffer.append(boolLabel.getAsString(context, component, obj));
            }
            else
            {
               buffer.append(obj.toString());
            }
         }
         
         result = buffer.toString();
      }
      else if (value != null)
      {
         result = value.toString();
      }
      
      return result;
   }
}
