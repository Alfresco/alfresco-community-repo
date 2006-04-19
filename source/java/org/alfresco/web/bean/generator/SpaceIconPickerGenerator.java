package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.component.UIListItems;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates the image picker component with rounded corners for selecting
 * an icon for a space.
 * 
 * @author gavinc
 */
public class SpaceIconPickerGenerator extends BaseComponentGenerator
{
   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      // create the outer component
      UIComponent component = context.getApplication().
            createComponent(RepoConstants.ALFRESCO_FACES_IMAGE_PICKER);
      
      // setup the outer component
      component.setRendererType(RepoConstants.ALFRESCO_FACES_RADIO_PANEL_RENDERER);
      FacesHelper.setupComponentId(context, component, id);
      component.getAttributes().put("columns", new Integer(6));
      component.getAttributes().put("spacing", new Integer(4));
      component.getAttributes().put("panelBorder", "blue");
      component.getAttributes().put("panelBgcolor", "#D3E6FE");
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void setupProperty(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      // do the standard setup
      super.setupProperty(context, propertySheet, item, propertyDef, component);
      
      // if the property sheet is in edit mode we also need to setup the 
      // list of icons the user can select from
      if (propertySheet.inEditMode())
      {
         // create the list items child component
         UIListItems items = (UIListItems)context.getApplication().
               createComponent(RepoConstants.ALFRESCO_FACES_LIST_ITEMS);
         
         // setup the value binding for the list of icons, this needs
         // to be sensitive to the bean used for the property sheet
         // we therefore need to get the value binding expression and
         // extract the bean name and then add '.icons' to the end,
         // this means any page that uses this component must supply
         // a getIcons method that returns a List of UIListItem's
         ValueBinding binding = propertySheet.getValueBinding("value");
         String expression = binding.getExpressionString();
         String beanName = expression.substring(2, expression.indexOf(".")+1);
         if (beanName.equals("DialogManager") || beanName.equals("WizardManager"))
         {
            // deal with the special dialog and wizard manager beans by 
            // adding .bean
            beanName = beanName + "bean.";
         }
         String newExpression = "#{" + beanName + "icons}";
         
         ValueBinding vb = context.getApplication().createValueBinding(newExpression);
         items.setValueBinding("value", vb);
         
         // add the list items component to the image picker component
         component.getChildren().add(items);
      }
   }
}
