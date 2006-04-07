package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.RepoConstants;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class DateTimePickerGenerator extends DatePickerGenerator
{
   private static final String MSG_DATE_TIME = "date_time_pattern";
   
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = super.generate(context, id);
      
      // add the attribute to show the time
      component.getAttributes().put("showTime", Boolean.valueOf(true));
      
      return component;
   }
   
   protected Converter getConverter(FacesContext context)
   {
      XMLDateConverter converter = (XMLDateConverter)context.getApplication().
            createConverter(RepoConstants.ALFRESCO_FACES_XMLDATA_CONVERTER);
      converter.setType("both");
      converter.setPattern(Application.getMessage(context, MSG_DATE_TIME));
      return converter;
   }
}
