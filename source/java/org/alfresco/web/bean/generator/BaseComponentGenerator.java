package org.alfresco.web.bean.generator;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIAssociation;
import org.alfresco.web.ui.repo.component.property.UIChildAssociation;
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
    * Disables the given component if the item is read only or is defined in the 
    * model as protected
    * 
    * @param context FacesContext
    * @param propertySheet The property sheet being generated
    * @param item The item being generated
    * @param component The component to disable
    */
   protected void disableIfReadOnlyOrProtected(FacesContext context, UIPropertySheet propertySheet, 
         PropertySheetItem item, UIComponent component)
   {
      if (item instanceof UIProperty)
      {
         PropertyDefinition propertyDef = getPropertyDefinition(context,
               propertySheet.getNode(), item.getName());
         if (item.isReadOnly() || (propertyDef != null && propertyDef.isProtected()))
         {
            component.getAttributes().put("disabled", Boolean.TRUE);
         }
      }
      else if (item instanceof UIAssociation || item instanceof UIChildAssociation)
      {
         AssociationDefinition assocDef = getAssociationDefinition(context, 
               propertySheet.getNode(), item.getName());
         if (item.isReadOnly() || (assocDef != null && assocDef.isProtected()))
         {
            component.getAttributes().put("disabled", Boolean.TRUE);
         }
      }
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
