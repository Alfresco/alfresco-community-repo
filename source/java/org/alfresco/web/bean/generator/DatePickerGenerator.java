package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
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
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class DatePickerGenerator extends BaseComponentGenerator
{
   private static final String MSG_DATE = "date_pattern";
   
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
      FacesHelper.setupComponentId(context, component, id);
      component.getAttributes().put("yearCount", new Integer(30));
      component.getAttributes().put("style", "margin-right: 7px;");
               
      return component;
   }

   public UIComponent generate(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = null;
      
      if (propertySheet.inEditMode())
      {
         // use the standard date picker component
         component = generate(context, item.getName());
         
         // get the property definition
         PropertyDefinition propertyDef = getPropertyDefinition(context,
               propertySheet.getNode(), item.getName());
         
         // disable the component if it is read only or protected
         if (item.isReadOnly() || (propertyDef != null && propertyDef.isProtected()))
         {
            component.getAttributes().put("disabled", Boolean.TRUE);
         }
         else
         {
            // if the item is multi valued we need to wrap the standard component
            if (propertyDef != null && propertyDef.isMultiValued())
            {
               component = enableForMultiValue(context, propertySheet, item, component, true);
            }
         }
      }
      else
      {
         // create an output text component in view mode
         component = createOutputTextComponent(context, item.getName());
      }
      
      if (item.getConverter() != null)
      {
         // setup the converter if one was specified
         setupConverter(context, propertySheet, item, component);
      }
      else
      {
         // use the default converter for the date component
         // we can cast this as we know it is an UIOutput type
         ((UIOutput)component).setConverter(getDefaultConverter(context));
      }
      
      return component;
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
