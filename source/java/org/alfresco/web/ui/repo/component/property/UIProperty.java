/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Component to represent an individual property within a property sheet
 * 
 * @author gavinc
 */
public class UIProperty extends PropertySheetItem
{
   private static Log logger = LogFactory.getLog(UIProperty.class);
   private static Log missingPropsLogger = LogFactory.getLog("alfresco.missingProperties");

   /**
    * Default constructor
    */
   public UIProperty()
   {
      // set the default renderer
      setRendererType("org.alfresco.faces.PropertyRenderer");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Property";
   }

   protected String getIncorrectParentMsg()
   {
      return "The property component must be nested within a property sheet component";
   }

   protected void generateItem(FacesContext context, UIPropertySheet propSheet) throws IOException
   {
      Node node = propSheet.getNode();
      String propertyName = (String)getName();

      DataDictionary dd = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(Application.BEAN_DATA_DICTIONARY);
      PropertyDefinition propDef = dd.getPropertyDefinition(node, propertyName);
      
      if (propDef == null)
      {
         // there is no definition for the node, so it may have been added to
         // the node as an additional property, so look for it in the node itself.
         // Or, if the ignoreIfMissing flag is set to false, show the property 
         if (node.hasProperty(propertyName) || getIgnoreIfMissing() == false)
         {
            String displayLabel = (String)getDisplayLabel();
            if (displayLabel == null)
            {
               displayLabel = propertyName;
            }
            
            // generate the label and generic control
            generateLabel(context, propSheet, displayLabel);
            generateControl(context, propSheet, propertyName);
         }
         else
         {
            // warn the user that the property was not found anywhere
            if (missingPropsLogger.isWarnEnabled())
               missingPropsLogger.warn("Failed to find property '" + propertyName + "' for node: " + node.getNodeRef().toString());
         }
      }
      else
      {
         String displayLabel = (String)getDisplayLabel();
         if (displayLabel == null)
         {
            // try and get the repository assigned label
            displayLabel = propDef.getTitle();
            
            // if the label is still null default to the local name of the property
            if (displayLabel == null)
            {
               displayLabel = propDef.getName().getLocalName();
            }
         }
         
         // generate the label and type specific control
         generateLabel(context, propSheet, displayLabel);
         generateControl(context, propSheet, propDef);
      }
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context JSF context
    * @param propSheet The property sheet this property belongs to
    * @param propDef The definition of the property to create the control for
    */
   @SuppressWarnings("unchecked")
   private void generateControl(FacesContext context, UIPropertySheet propSheet,
         PropertyDefinition propDef)
   {
      UIComponent control = null;
      
      // get type info for the property
      DataTypeDefinition dataTypeDef = propDef.getDataType();
      QName typeName = dataTypeDef.getName();
      
      String componentGeneratorName = this.getComponentGenerator(); 
      
      // use the default component generator if there wasn't an overridden one
      if (componentGeneratorName == null)
      {
         // work out which generator to use by the type of the property
         if (typeName.equals(DataTypeDefinition.TEXT))
         {
            componentGeneratorName = RepoConstants.GENERATOR_TEXT_FIELD;
         }
         else if (typeName.equals(DataTypeDefinition.MLTEXT))
         {
            componentGeneratorName = RepoConstants.GENERATOR_MLTEXT_FIELD;
         }
         else if (typeName.equals(DataTypeDefinition.BOOLEAN))
         {
            componentGeneratorName = RepoConstants.GENERATOR_CHECKBOX;
         }
         else if (typeName.equals(DataTypeDefinition.CATEGORY))
         {
            componentGeneratorName = RepoConstants.GENERATOR_CATEGORY_SELECTOR;
         }
         else if (typeName.equals(DataTypeDefinition.DATETIME))
         {
            componentGeneratorName = RepoConstants.GENERATOR_DATETIME_PICKER;
         }
         else if (typeName.equals(DataTypeDefinition.DATE))
         {
            componentGeneratorName = RepoConstants.GENERATOR_DATE_PICKER;
         }
         else
         {
            // default to a text field
            componentGeneratorName = RepoConstants.GENERATOR_TEXT_FIELD;
         }
      }
      
      // retrieve the component generator and generate the control
      control = FacesHelper.getComponentGenerator(context, componentGeneratorName).
            generateAndAdd(context, propSheet, this);
      
      // if we're in edit mode ensure that we don't allow editing of system properties or scenarios we don't support
      if (propSheet.inEditMode())
      {
         // if we are trying to edit a system property type set it to read-only as  these are internal 
         // properties that shouldn't be edited.
         if (!FacesHelper.getComponentGenerator(context, componentGeneratorName).isEnabledInEditMode(context, control, propDef))
         {
            logger.warn("Setting property " + propDef.getName().toString() + " to read-only as it can not be edited");
            control.getAttributes().put("disabled", Boolean.TRUE);
         }         
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + propDef.getName().toString() + 
                      "' and added it to component " + this);
   }
   
   /**
    * Generates an appropriate control for the given property name
    * 
    * @param context JSF context
    * @param propSheet The property sheet this property belongs to
    * @param propName The name of the property to create a control for
    */
   private void generateControl(FacesContext context, UIPropertySheet propSheet, String propName)
   {
      String componentGeneratorName = this.getComponentGenerator(); 
      
      if (componentGeneratorName == null)
      {
         componentGeneratorName = RepoConstants.GENERATOR_TEXT_FIELD;
      }
      
      UIComponent control = FacesHelper.getComponentGenerator(context, componentGeneratorName).
            generateAndAdd(context, propSheet, this);
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + propName +  
                      "' and added it to component " + this);
   }
}
