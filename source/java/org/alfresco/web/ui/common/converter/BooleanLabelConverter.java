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
