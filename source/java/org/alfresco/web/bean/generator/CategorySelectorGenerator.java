package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a category selector component.
 * 
 * @author gavinc
 */
public class CategorySelectorGenerator extends BaseComponentGenerator
{
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      // the category selector component is used whatever mode the property sheet is in
      return generate(context, item.getName());
   }

   @Override
   protected void setupMandatoryValidation(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component, boolean realTimeChecking, String idSuffix)
   {
      if (component instanceof UIMultiValueEditor)
      {
         // Override the setup of the mandatory validation 
         // so we can send the _current_value id suffix.
         // We also enable real time so the page load
         // check disables the ok button if necessary, as the user
         // adds or removes items from the multi value list the 
         // page will be refreshed and therefore re-check the status.
         
         super.setupMandatoryValidation(context, propertySheet, item, 
               component, true, "_current_value");
      }
      else
      {
         // Override the setup of the mandatory validation 
         // so we can send the _selected id suffix.
         // We also enable real time so the page load check disabled
         // the ok button if necessary, as the control is used the
         // page will be refreshed and therefore re-check the status.
         
         super.setupMandatoryValidation(context, propertySheet, item, 
               component, true, "_selected");
      }
   }

   @Override
   protected ControlType getControlType()
   {
      return ControlType.SELECTOR;
   }
}
