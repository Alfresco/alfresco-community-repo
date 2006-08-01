package org.alfresco.web.bean.generator;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a date picker component.
 * 
 * @author gavinc
 */
public class DatePickerGenerator extends BaseComponentGenerator
{
   private int yearCount = 30;
   private int startYear = new Date().getYear() + 1900 + 2;
   
   private static final String MSG_DATE = "date_pattern";
   
   /**
    * @return Returns the year to start counting back from
    */
   public int getStartYear()
   {
      return startYear;
   }

   /**
    * @param startYear Sets the year to start counting back from
    */
   public void setStartYear(int startYear)
   {
      this.startYear = startYear;
   }

   /**
    * @return Returns the number of years to show
    */
   public int getYearCount()
   {
      return yearCount;
   }

   /**
    * @param yearCount Sets the number of years to show
    */
   public void setYearCount(int yearCount)
   {
      this.yearCount = yearCount;
   }

   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
      FacesHelper.setupComponentId(context, component, id);
      component.getAttributes().put("startYear", this.startYear);
      component.getAttributes().put("yearCount", this.yearCount);
      component.getAttributes().put("style", "margin-right: 7px;");
               
      return component;
   }

   @Override
   protected void setupConverter(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      if (property.getConverter() != null)
      {
         // create and add the custom converter
         createAndSetConverter(context, property.getConverter(), component);
      }
      else
      {
         // use the default converter for the date component
         // we can cast this as we know it is an UIOutput type
         ((UIOutput)component).setConverter(getDefaultConverter(context));
      }
   }

   @Override
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      // a date picker will always have a date value so there
      // is no need to create a mandatory validation rule
   }
   
   /**
    * Retrieves the default converter for the date component
    * 
    * @param context FacesContext
    * @return XMLDateConverter
    */
   protected Converter getDefaultConverter(FacesContext context)
   {
      XMLDateConverter converter = (XMLDateConverter)context.getApplication().
            createConverter(RepoConstants.ALFRESCO_FACES_XMLDATE_CONVERTER);
      converter.setType("date");
      converter.setPattern(Application.getMessage(context, MSG_DATE));
      return converter;
   }
}
