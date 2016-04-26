package org.alfresco.web.ui.common.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

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
      StringTokenizer tokenizer = new StringTokenizer(value, ", ");
      while (tokenizer.hasMoreTokens())
      {
         items.add(tokenizer.nextToken().trim());
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
            
            buffer.append(obj.toString());
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
