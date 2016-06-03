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
