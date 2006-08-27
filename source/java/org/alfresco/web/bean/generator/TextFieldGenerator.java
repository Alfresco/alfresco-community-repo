package org.alfresco.web.bean.generator;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class TextFieldGenerator extends BaseComponentGenerator
{
   private int size = 35;
   private int maxLength = 1024;
   
   /**
    * @return Returns the default size for a text field
    */
   public int getSize()
   {
      return size;
   }

   /**
    * @param size Sets the size of a text field
    */
   public void setSize(int size)
   {
      this.size = size;
   }

   /**
    * @return Returns the max length for the text field
    */
   public int getMaxLength()
   {
      return maxLength;
   }

   /**
    * @param maxLength Sets the max length of the text field
    */
   public void setMaxLength(int maxLength)
   {
      this.maxLength = maxLength;
   }

   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
      FacesHelper.setupComponentId(context, component, id);

      component.getAttributes().put("size", this.size);
      component.getAttributes().put("maxlength", this.maxLength);
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = null;
      
      if (propertySheet.inEditMode())
      {
         // if the field has the list of values constraint 
         // and it is editable a SelectOne component is 
         // required otherwise create the standard edit component
         ListOfValuesConstraint constraint = getListOfValuesConstraint(
               context, propertySheet, item);
         
         PropertyDefinition propDef = this.getPropertyDefinition(context, 
               propertySheet.getNode(), item.getName());
         
         if (constraint != null && item.isReadOnly() == false &&
             propDef != null && propDef.isProtected() == false)
         {
            component = context.getApplication().createComponent(
                  UISelectOne.COMPONENT_TYPE);
            FacesHelper.setupComponentId(context, component, item.getName());
            
            // create the list of choices
            UISelectItems itemsComponent = (UISelectItems)context.getApplication().
               createComponent("javax.faces.SelectItems");
            
            List<SelectItem> items = new ArrayList<SelectItem>(3);
            List<String> values = constraint.getAllowedValues();
            for (String value : values)
            {
               items.add(new SelectItem(value, value));
            }
            
            itemsComponent.setValue(items);
            
            // add the items as a child component
            component.getChildren().add(itemsComponent);
         }
         else
         {
            // use the standard component in edit mode
            component = generate(context, item.getName());
         }
      }
      else
      {
         // create an output text component in view mode
         component = createOutputTextComponent(context, item.getName());
      }
      
      return component;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking, 
         String idSuffix)
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
      else if (component instanceof UISelectOne)
      {
         // when there is a list of values constraint there
         // will always be a value so validation is not required.
      }
      else
      {
         // setup the client validation rule with real time validation enabled
         super.setupMandatoryValidation(context, propertySheet, item, 
               component, true, idSuffix);
      
         // add event handler to kick off real time checks
         component.getAttributes().put("onkeyup", "processButtonState();");
      }
   }
   
   /**
    * Retrieves the list of values constraint for the item, if it has one
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param item The item being generated
    * @return The constraint if the item has one, null otherwise
    */
   protected ListOfValuesConstraint getListOfValuesConstraint(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      ListOfValuesConstraint lovConstraint = null;
      
      // get the property definition for the item
      PropertyDefinition propertyDef = getPropertyDefinition(context,
               propertySheet.getNode(), item.getName());
      
      if (propertyDef != null)
      {
         // go through the constaints and see if it has the
         // list of values constraint
         List<ConstraintDefinition> constraints = propertyDef.getConstraints();
         for (ConstraintDefinition constraintDef : constraints)
         {
            Constraint constraint = constraintDef.getConstraint();
               
            if (constraint instanceof ListOfValuesConstraint)
            {
               lovConstraint = (ListOfValuesConstraint)constraint;
               break;
            }
         }
      }
      
      return lovConstraint;
   }
}
