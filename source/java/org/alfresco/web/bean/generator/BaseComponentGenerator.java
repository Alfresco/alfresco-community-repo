package org.alfresco.web.bean.generator;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIProperty;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

public abstract class BaseComponentGenerator implements IComponentGenerator
{
   private static Log logger = LogFactory.getLog(BaseComponentGenerator.class);
   
   private DataDictionary dataDictionary;
   
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
   
   /**
    * Creates a wrapper component around the given component to enable the user
    * to edit multiple values.
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param item The item being generated
    * @param component The component to wrap if necessary
    * @param field true if the property being enabled is a field style 
    *        component i.e. text field or checkbox. false if the component
    *        is a selector style component i.e. category selector
    */
   protected UIComponent enableForMultiValue(FacesContext context, UIPropertySheet propertySheet,
         PropertySheetItem item, UIComponent component, boolean field)
   {
      UIComponent multiValueComponent = component;
      
      // NOTE: Associations have built in support for multiple values so we only deal
      //       with UIProperty instances in here currently
      
      if (item instanceof UIProperty)
      {
         // if the property is multi-valued create a multi value editor wrapper component
         String id = "multi_" + item.getName();
         multiValueComponent = context.getApplication().createComponent(
               RepoConstants.ALFRESCO_FACES_MULTIVALUE_EDITOR);
         FacesHelper.setupComponentId(context, multiValueComponent, id);
            
         // set the renderer depending on whether the item is a 'field' or a 'selector'
         if (field)
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
                  item.getName() + "']}";
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
    * Sets up any converters configured for the item
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param item The item being generated
    * @param component The component to disable
    */
   protected void setupConverter(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component)
   {
      // if the item has a converter, create it and apply it
      String converter = item.getConverter();
      if (converter != null && component instanceof UIOutput)
      {
         try
         {
            Converter conv = context.getApplication().createConverter(converter);
            ((UIOutput)component).setConverter(conv);
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + converter + " could not be applied");
         }
      }
   }
   
   /**
    * Creates an output text component 
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
