package org.alfresco.web.ui.repo.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.extensions.surf.util.I18NUtil;
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
