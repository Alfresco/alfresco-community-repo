package org.alfresco.web.bean.generator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.BaseAssociationEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIProperty;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet.ClientValidation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

public abstract class BaseComponentGenerator implements IComponentGenerator
{
   private static Log logger = LogFactory.getLog(BaseComponentGenerator.class);
   
   protected enum ControlType { FIELD, SELECTOR; }
   
   private DataDictionary dataDictionary;
   
   @SuppressWarnings("unchecked")
   public UIComponent generateAndAdd(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item)
   {
      UIComponent component = null;
      
      if (item instanceof UIProperty)
      {
         // get the property definition
         PropertyDefinition propertyDef = getPropertyDefinition(context,
               propertySheet.getNode(), item.getName());

         // create the component and add it to the property sheet
         component = createComponent(context, propertySheet, item);
         
         // setup the component for multi value editing if necessary
         component = setupMultiValuePropertyIfNecessary(context, propertySheet, 
               item, propertyDef, component);
         
         // setup common aspects of the property i.e. value binding
         setupProperty(context, propertySheet, item, propertyDef, component);
         
         // add the component now, it needs to be added before the validations
         // are setup as we need access to the component id, which in turn needs 
         // to have a parent to get the correct id
         item.getChildren().add(component);
         
         // setup the component for mandatory validation if necessary
         setupMandatoryPropertyIfNecessary(context, propertySheet, item, 
               propertyDef, component);
         
         // setup any constraints the property has
         setupConstraints(context, propertySheet, item, propertyDef, component); 
         
         // setup any converter the property needs
         setupConverter(context, propertySheet, item, propertyDef, component);
      }
      else
      {
         // get the association definition
         AssociationDefinition assocationDef = this.getAssociationDefinition(context, 
               propertySheet.getNode(), item.getName());
         
         // create the component and add it to the property sheet
         component = createComponent(context, propertySheet, item);
         
         // setup common aspects of the association i.e. value binding
         setupAssociation(context, propertySheet, item, assocationDef, component);
         
         // add the component now, it needs to be added before the validations
         // are setup as we need access to the component id, which needs have a
         // parent to get the correct id
         item.getChildren().add(component);
         
         // setup the component for mandatory validation if necessary
         setupMandatoryAssociationIfNecessary(context, propertySheet, item, 
               assocationDef, component);
         
         // setup any converter the association needs
         setupConverter(context, propertySheet, item, assocationDef, component);
      }
      
      return component;
   }
   
   
   /**
    * Creates the component for the given proerty sheet item.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param item The property or association being generated
    * @return The newly created component
    */
   @SuppressWarnings("unchecked")
   protected UIComponent createComponent(FacesContext context, UIPropertySheet propertySheet,
         PropertySheetItem item)
   {
      UIComponent component = null;
      
      if (item instanceof UIProperty)
      {
         if (propertySheet.inEditMode())
         {
            // use the standard component in edit mode
            component = generate(context, item.getName());
         }
         else
         {
            // create an output text component in view mode
            component = createOutputTextComponent(context, item.getName());
         }
      }
      else
      {
         // create the standard association component
         component = generate(context, item.getName());
      }
      
      return component;
   }
   
   /**
    * Creates the converter with the given id and adds it to the component.
    * 
    * @param context FacesContext
    * @param converterId The name of the converter to create
    * @param component The component to add the converter to
    */
   protected void createAndSetConverter(FacesContext context, String converterId, 
         UIComponent component)
   {
      if (converterId != null && component instanceof UIOutput)
      {
         try
         {
            Converter conv = context.getApplication().createConverter(converterId);
            ((UIOutput)component).setConverter(conv);
         }
         catch (NullPointerException npe)
         {
            // workaround a NPE bug in MyFaces
            logger.warn("Converter " + converterId + " could not be applied");
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + converterId + " could not be applied");
         }
      }
   }
   
   /**
    * Creates an output text component.
    * 
    * @param context FacesContext
    * @param id Optional id to set
    * @return The new component
    */
   protected UIOutput createOutputTextComponent(FacesContext context, String id)
   {
      UIOutput component = (UIOutput)context.getApplication().createComponent(
            ComponentConstants.JAVAX_FACES_OUTPUT);
      
      component.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
      FacesHelper.setupComponentId(context, component, id);
      
      return component;
   }
   
   /**
    * Sets up the property component i.e. setting the value binding
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet
    * @param item The parent component
    * @param propertyDef The property definition
    * @param component The component representing the property
    */
   @SuppressWarnings("unchecked")
   protected void setupProperty(FacesContext context, UIPropertySheet propertySheet,
         PropertySheetItem item, PropertyDefinition propertyDef, UIComponent component)
   {
      // create and set the value binding
      ValueBinding vb = null;
      
      if (propertyDef != null)
      {
         vb = context.getApplication().createValueBinding(
            "#{" + propertySheet.getVar() + ".properties[\"" + 
            propertyDef.getName().toString() + "\"]}");
      }
      else
      {
         vb = context.getApplication().createValueBinding(
            "#{" + propertySheet.getVar() + ".properties[\"" + 
            item.getName() + "\"]}");
      }
      
      component.setValueBinding("value", vb);
      
      // disable the component if it is read only or protected
      // or if the property sheet is in view mode
      if (propertySheet.inEditMode() == false || item.isReadOnly() || 
              (propertyDef != null && propertyDef.isProtected())) 
      {
         component.getAttributes().put("disabled", Boolean.TRUE);
      }
   }
   
   /**
    * Sets up the association component i.e. setting the value binding
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet
    * @param item The parent component
    * @param associationDef The association definition
    * @param component The component representing the association
    */
   @SuppressWarnings("unchecked")
   protected void setupAssociation(FacesContext context, UIPropertySheet propertySheet,
         PropertySheetItem item, AssociationDefinition associationDef, UIComponent component)
   {
      // create and set the value binding
      ValueBinding vb = context.getApplication().createValueBinding(
            "#{" + propertySheet.getVar() + "}");
      component.setValueBinding("value", vb);
      
      // set the association name and set to disabled if appropriate
      ((BaseAssociationEditor)component).setAssociationName(
            associationDef.getName().toString());
      
      // disable the component if it is read only or protected
      // or if the property sheet is in view mode
      if (propertySheet.inEditMode() == false || item.isReadOnly() || 
              (associationDef != null && associationDef.isProtected())) 
      {
         component.getAttributes().put("disabled", Boolean.TRUE);
      }
   }
   
   /**
    * Creates a wrapper component around the given component to enable the user
    * to edit multiple values.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param property The property being generated
    * @param propertyDef The data dictionary definition for the property
    * @param component The component representing the property
    * @return A wrapped component if the property is multi-valued or the 
    *         original component if it is not multi-valued
    */
   @SuppressWarnings("unchecked")
   protected UIComponent setupMultiValuePropertyIfNecessary(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      UIComponent multiValueComponent = component;
      
      if (propertySheet.inEditMode() && property.isReadOnly() == false && 
          propertyDef != null && propertyDef.isProtected() == false &&
          propertyDef.isMultiValued())
      {
         // if the property is multi-valued create a multi value editor wrapper component
         String id = "multi_" + property.getName();
         multiValueComponent = context.getApplication().createComponent(
               RepoConstants.ALFRESCO_FACES_MULTIVALUE_EDITOR);
         FacesHelper.setupComponentId(context, multiValueComponent, id);
            
         // set the renderer depending on whether the item is a 'field' or a 'selector'
         if (getControlType() == ControlType.FIELD)
         {
            multiValueComponent.setRendererType(RepoConstants.ALFRESCO_FACES_FIELD_RENDERER);
         }
         else
         {
            multiValueComponent.setRendererType(RepoConstants.ALFRESCO_FACES_SELECTOR_RENDERER);
            
            // set the value binding for the wrapped component and the lastItemAdded attribute of
            // the multi select component, needs to point somewhere that can hold any object, it
            // will store the item last added by the user.
            String expr = "#{MultiValueEditorBean.lastItemsAdded['" +
                  property.getName() + "']}";
            ValueBinding vb = context.getApplication().createValueBinding(expr);
            multiValueComponent.setValueBinding("lastItemAdded", vb);
            component.setValueBinding("value", vb);
         }
         
         // add the original component as a child of the wrapper
         multiValueComponent.getChildren().add(component);
      }
      
      return multiValueComponent;
   }
   
   /**
    * Sets up a mandatory validation rule for the given property.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param property The property being generated
    * @param propertyDef The data dictionary definition of the property
    * @param component The component representing the property
    */
   @SuppressWarnings("unchecked")
   protected void setupMandatoryPropertyIfNecessary(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      // only setup validations if the property sheet is in edit mode,
      // validation is enabled and the property is declared as mandatory
      if (propertySheet.inEditMode() && propertySheet.isValidationEnabled() &&
          propertyDef != null && propertyDef.isMandatory())
      {
         setupMandatoryValidation(context, propertySheet, property, component, false);
         setupMandatoryMarker(context, property);
      }
   }

   /**
    * Sets up a mandatory validation rule for the given association.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param association The association being generated
    * @param associationDef The data dictionary definition of the association
    * @param component The component representing the association
    */
   protected void setupMandatoryAssociationIfNecessary(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem association, 
         AssociationDefinition associationDef, UIComponent component)
   {
      // only setup validations if the property sheet is in edit mode,
      // validation is enabled and the association is declared as mandatory
      if (propertySheet.inEditMode() && propertySheet.isValidationEnabled() &&
          associationDef != null && associationDef.isTargetMandatory())
      {
         setupMandatoryValidation(context, propertySheet, association, component, false);
         setupMandatoryMarker(context, association);
      }
   }
   
   /**
    * Sets up a client mandatory validation rule with the property
    * sheet for the given item.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet to add the validation rule to
    * @param item The item being generated
    * @param component The component representing the item
    * @param realTimeChecking true to make the client validate as the user types
    */
   protected void setupMandatoryValidation(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem item, 
         UIComponent component, boolean realTimeChecking)
   {
      List<String> params = new ArrayList<String>(3);
      
      // add the value parameter
      String value = "document.getElementById('" +
            component.getClientId(context) + "').value";
      params.add(value);
      
      // add the validation failed message to show (use the value of the 
      // label component of the given item)
      String msg = Application.getMessage(context, "validation_mandatory");
      params.add("'" + MessageFormat.format(msg, new Object[] {item.getResolvedDisplayLabel()}) + "'");
      
      // add the validation case to the property sheet
      propertySheet.addClientValidation(new ClientValidation("validateMandatory",
            params, realTimeChecking));
   }
   
   /**
    * Sets up the marker to show that the item is mandatory.
    * 
    * @param context FacesContext
    * @param item The item being generated
    */
   @SuppressWarnings("unchecked")
   protected void setupMandatoryMarker(FacesContext context, PropertySheetItem item)
   {
      // create an output text component and set value to "*"
      UIOutput component = createOutputTextComponent(context, null);
      component.setValue("*");
      
      // add marker as child to the property sheet item
      item.getChildren().add(component);
   }
   
   /**
    * Sets up client validation rules for any constraints the property has.
    * 
    * @param context FacesContext
    * propertySheet The property sheet being generated
    * @param property The property being generated
    * @param propertyDef The data dictionary definition of the property
    * @param component The component representing the property
    */
   protected void setupConstraints(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      // only setup constraints if the property sheet is in edit mode,
      // validation is enabled
      if (propertySheet.inEditMode() && propertySheet.isValidationEnabled() &&
          propertyDef != null)
      {
         List<ConstraintDefinition> constraints = propertyDef.getConstraints();
         for (ConstraintDefinition constraintDef : constraints)
         {
            Constraint constraint = constraintDef.getConstraint();
               
            if (constraint instanceof RegexConstraint)
            {
               setupRegexConstraint(context, propertySheet, property, component,
                     (RegexConstraint)constraint, false);
            }
            else if (constraint instanceof StringLengthConstraint)
            {
               setupStringLengthConstraint(context, propertySheet, property, component,
                     (StringLengthConstraint)constraint, false);
            }
            else if (constraint instanceof NumericRangeConstraint)
            {
               setupNumericRangeConstraint(context, propertySheet, property, component,
                     (NumericRangeConstraint)constraint, false);
            }
            else
            {
               logger.warn("Unrecognized constaint object: " + constraint.getClass().getName());
            }
         }
      }
   }
   
   /**
    * Sets up a default validation rule for the regular expression constraint
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet to add the validation rule to
    * @param property The property being generated
    * @param component The component representing the property
    * @param constraint The constraint to setup
    * @param realTimeChecking true to make the client validate as the user types
    */
   protected void setupRegexConstraint(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         UIComponent component, RegexConstraint constraint, 
         boolean realTimeChecking)
   {
      String expression = constraint.getExpression();
      boolean requiresMatch = constraint.getRequiresMatch();
      
      List<String> params = new ArrayList<String>(3);
      
      // add the value parameter
      String value = "document.getElementById('" +
            component.getClientId(context) + "').value";
      params.add(value);
      
      // add the min parameter
      params.add("'" + expression + "'");
      
      // add the max parameter
      params.add(Boolean.toString(requiresMatch));
      
      // add the validation failed message to show
      String msg = null;
      if (requiresMatch)
      {
         msg = Application.getMessage(context, "validation_regex");
      }
      else
      {
         msg = Application.getMessage(context, "validation_regex_not_match");
      }
      params.add("'" + MessageFormat.format(msg, new Object[] 
            {property.getResolvedDisplayLabel(), expression}) + "'");
      
      // add the validation case to the property sheet
      propertySheet.addClientValidation(new ClientValidation("validateRegex",
         params, realTimeChecking));
   }
   
   /**
    * Sets up a default validation rule for the string length constraint
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet to add the validation rule to
    * @param property The property being generated
    * @param component The component representing the property
    * @param constraint The constraint to setup
    * @param realTimeChecking true to make the client validate as the user types
    */
   protected void setupStringLengthConstraint(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         UIComponent component, StringLengthConstraint constraint,
         boolean realTimeChecking)
   {
      int min = constraint.getMinLength();
      int max = constraint.getMaxLength();
      
      List<String> params = new ArrayList<String>(3);
      
      // add the value parameter
      String value = "document.getElementById('" +
            component.getClientId(context) + "').value";
      params.add(value);
      
      // add the min parameter
      params.add(Integer.toString(min));
      
      // add the max parameter
      params.add(Integer.toString(max));
      
      // add the validation failed message to show
      String msg = Application.getMessage(context, "validation_string_length");
      params.add("'" + MessageFormat.format(msg, new Object[] 
            {property.getResolvedDisplayLabel(), min, max}) + "'");
      
      // add the validation case to the property sheet
      propertySheet.addClientValidation(new ClientValidation("validateStringLength",
         params, realTimeChecking));
   }
   
   /**
    * Sets up a default validation rule for the numeric range constraint
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet to add the validation rule to
    * @param property The property being generated
    * @param component The component representing the property
    * @param constraint The constraint to setup
    * @param realTimeChecking true to make the client validate as the user types
    */
   protected void setupNumericRangeConstraint(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         UIComponent component, NumericRangeConstraint constraint,
         boolean realTimeChecking)
   {
      double min = constraint.getMinValue();
      double max = constraint.getMaxValue();
      
      List<String> params = new ArrayList<String>(3);
      
      // add the value parameter
      String value = "document.getElementById('" +
            component.getClientId(context) + "').value";
      params.add(value);
      
      // add the min parameter
      params.add(Double.toString(min));
      
      // add the max parameter
      params.add(Double.toString(max));
      
      // add the validation failed message to show
      String msg = Application.getMessage(context, "validation_numeric_range");
      params.add("'" + MessageFormat.format(msg, new Object[] 
            {property.getResolvedDisplayLabel(), min, max}) + "'");
      
      // add the validation case to the property sheet
      propertySheet.addClientValidation(new ClientValidation("validateNumberRange",
         params, false));
   }
   
   /**
    * Sets up the appropriate converter for the given property
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param property The property being generated
    * @param propertyDef The data dictionary definition of the property
    * @param component The component representing the property
    */
   protected void setupConverter(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem property, 
         PropertyDefinition propertyDef, UIComponent component)
   {
      if (property.getConverter() != null)
      {
         // create and add the custom converter
         createAndSetConverter(context, property.getConverter(), component);
      }
      else if (propertySheet.inEditMode() == false && 
               propertyDef != null && propertyDef.isMultiValued())
      {
         // if there isn't a custom converter and the property is
         // multi-valued add the multi value converter as a default
         createAndSetConverter(context, RepoConstants.ALFRESCO_FACES_MULTIVALUE_CONVERTER,
               component);
      }
   }
   
   /**
    * Sets up the appropriate converter for the given association
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param association The association being generated
    * @param associationDef The data dictionary definition of the property
    * @param component The component representing the association
    */
   protected void setupConverter(FacesContext context, 
         UIPropertySheet propertySheet, PropertySheetItem association, 
         AssociationDefinition associationDef, UIComponent component)
   {
      if (association.getConverter() != null)
      {
         // create and add the custom converter
         createAndSetConverter(context, association.getConverter(), component);
      }
   }
   
   /**
    * Returns the type of the control being generated
    * 
    * @return The type of the control either a FIELD or a SELECTOR
    */
   protected ControlType getControlType()
   {
      return ControlType.FIELD;
   }
   
   /**
    * Retrieve the PropertyDefinition for the given property name on the given node
    * 
    * @param node The node to get the property definition from
    * @param propertyName The name of the property
    * @return PropertyDefinition for the node or null if a definition can not be found
    */
   protected PropertyDefinition getPropertyDefinition(FacesContext context,
         Node node, String propertyName)
   {
      return getDataDictionary(context).getPropertyDefinition(node, propertyName);
   }
   
   /**
    * Retrieve the AssociationDefinition for the given property name on the given node
    * 
    * @param node The node to get the association definition from
    * @param associationName The name of the property
    * @return AssociationDefinition for the node or null if a definition can not be found
    */
   protected AssociationDefinition getAssociationDefinition(FacesContext context,
         Node node, String associationName)
   {
      return getDataDictionary(context).getAssociationDefinition(node, associationName);
   }
   
   private DataDictionary getDataDictionary(FacesContext context)
   {
      if (this.dataDictionary == null)
      {
         this.dataDictionary = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(Application.BEAN_DATA_DICTIONARY);
      }
      
      return this.dataDictionary;
   }
}
