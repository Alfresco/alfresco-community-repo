/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;
import java.text.MessageFormat;

import javax.faces.FacesException;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UICategorySelector;
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
   private static final String MSG_ERROR_PROPERTY = "error_property";
   private static final String MSG_DATE_TIME = "date_time_pattern";
   private static final String MSG_DATE = "date_pattern";

   private static Log logger = LogFactory.getLog(UIProperty.class);

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

   /**
    * @see org.alfresco.web.ui.repo.component.property.PropertySheetItem#getIncorrectParentMsg()
    */
   protected String getIncorrectParentMsg()
   {
      return "The property component must be nested within a property sheet component";
   }

   /**
    * @see org.alfresco.web.ui.repo.component.property.PropertySheetItem#generateItem(javax.faces.context.FacesContext, org.alfresco.web.bean.repository.Node, java.lang.String)
    */
   protected void generateItem(FacesContext context, Node node, String var) throws IOException
   {
      String propertyName = (String)getName();

      DataDictionary dd = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(Application.BEAN_DATA_DICTIONARY);
      PropertyDefinition propDef = dd.getPropertyDefinition(node, propertyName);
      
      if (propDef == null)
      {
         // there is no definition for the node, so it may have been added to
         // the node as an additional property, so look for it in the node itself
         if (node.hasProperty(propertyName))
         {
            String displayLabel = (String)getDisplayLabel();
            if (displayLabel == null)
            {
               displayLabel = propertyName;
            }
            
            // generate the label and generic control
            generateLabel(context, displayLabel);
            generateControl(context, propertyName, var);
         }
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Failed to find property definition for property '" + propertyName + "'");
            
            // add an error message as the property is not defined in the data dictionary and 
            // not in the node's set of properties
            String msg = MessageFormat.format(Application.getMessage(context, MSG_ERROR_PROPERTY), new Object[] {propertyName});
            Utils.addErrorMessage(msg);
            
            if (logger.isDebugEnabled())
               logger.debug("Added global error message: " + msg);
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
         generateLabel(context, displayLabel);
         generateControl(context, propDef, var);
      }
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context JSF context
    * @param propDef The definition of the property to create the control for
    * @param varName Name of the variable the node is stored in the session as 
    *                (used for value binding expression)
    * @param parent The parent component for the control
    */
   private void generateControl(FacesContext context, PropertyDefinition propDef, 
                                String varName)
   {
      UIOutput control = null;
      ValueBinding vb = context.getApplication().
                        createValueBinding("#{" + varName + ".properties[\"" + 
                        propDef.getName().toString() + "\"]}");
      
      UIPropertySheet propSheet = (UIPropertySheet)this.getParent();
      
      DataTypeDefinition dataTypeDef = propDef.getDataType();
      QName typeName = dataTypeDef.getName();
         
      if (propSheet.getMode().equalsIgnoreCase(UIPropertySheet.VIEW_MODE))
      {
         // if we are in view mode simply output the text to the screen unless the type
         // of the property is a category
         if (typeName.equals(DataTypeDefinition.CATEGORY))
         {
            control = (UICategorySelector)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
            ((UICategorySelector)control).setDisabled(true);
         }
         else
         {
            control = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
            control.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         }
         
         // if it is a date or datetime property add the converter
         if (typeName.equals(DataTypeDefinition.DATETIME) )
         {
            XMLDateConverter conv = (XMLDateConverter)context.getApplication().
               createConverter(RepoConstants.ALFRESCO_FACES_XMLDATA_CONVERTER);
            conv.setType("both");
            conv.setPattern(Application.getMessage(context, MSG_DATE_TIME));
            control.setConverter(conv);
         }
         else if (typeName.equals(DataTypeDefinition.DATE))
         {
            XMLDateConverter conv = (XMLDateConverter)context.getApplication().
               createConverter(RepoConstants.ALFRESCO_FACES_XMLDATA_CONVERTER);
            conv.setType("date");
            conv.setPattern(Application.getMessage(context, MSG_DATE));
            control.setConverter(conv);
         }
      }
      else
      {
         // generate the appropriate input field 
         if (typeName.equals(DataTypeDefinition.BOOLEAN))
         {
            control = (UISelectBoolean)context.getApplication().
                  createComponent(ComponentConstants.JAVAX_FACES_SELECT_BOOLEAN);
            control.setRendererType(ComponentConstants.JAVAX_FACES_CHECKBOX);
         }
         else if (typeName.equals(DataTypeDefinition.CATEGORY))
         {
            control = (UICategorySelector)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
         }
         else if (typeName.equals(DataTypeDefinition.DATETIME))
         {
            control = (UIInput)context.getApplication().
                  createComponent(ComponentConstants.JAVAX_FACES_INPUT);
            control.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
            control.getAttributes().put("startYear", new Integer(1970));
            control.getAttributes().put("yearCount", new Integer(50));
            control.getAttributes().put("showTime", Boolean.valueOf(true));
            control.getAttributes().put("style", "margin-right: 7px;");
         }
         else if (typeName.equals(DataTypeDefinition.DATE))
         {
            control = (UIInput)context.getApplication().
                  createComponent(ComponentConstants.JAVAX_FACES_INPUT);
            control.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
            control.getAttributes().put("startYear", new Integer(1970));
            control.getAttributes().put("yearCount", new Integer(50));
            control.getAttributes().put("style", "margin-right: 7px;");
         }
         else
         {
            // any other type is represented as an input text field
            control = (UIInput)context.getApplication().
                  createComponent(ComponentConstants.JAVAX_FACES_INPUT);
            control.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
            control.getAttributes().put("size", "35");
            control.getAttributes().put("maxlength", "1024");
         }
         
         // if we are trying to edit a NodeRef or Path property type set it to read-only as 
         // these are internal properties that shouldn't be edited.
         if (typeName.equals(DataTypeDefinition.NODE_REF) || typeName.equals(DataTypeDefinition.PATH))
         {
            logger.warn("Setting property " + propDef.getName().toString() + " to read-only as it can not be edited");
            control.getAttributes().put("disabled", Boolean.TRUE);
         }
      
         // set control to disabled state if set to read only or if the
         // property definition says it is protected
         if (isReadOnly() || propDef.isProtected())
         {
            control.getAttributes().put("disabled", Boolean.TRUE);
         }
         
         // add a validator if the field is required
//         if (propDef.isMandatory())
//         {
//            control.setRequired(true);
//            LengthValidator val = (LengthValidator)context.getApplication().
//                                   createValidator("javax.faces.Length");
//            val.setMinimum(1);
//            control.addValidator(val);
//         }   
      }
      
      // set up the common aspects of the control
      control.setId(context.getViewRoot().createUniqueId());
      control.setValueBinding("value", vb);
      
      // if a converter has been specified we need to instantiate it
      // and apply it to the control
      if (getConverter() != null)
      {
         // catch null pointer exception to workaround bug in myfaces
         try
         {
            Converter conv = context.getApplication().createConverter(getConverter());
            control.setConverter(conv);
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + getConverter() + " could not be applied");
         }
      }
      
      // add the control itself
      this.getChildren().add(control);
      
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
    * @param propName The name of the property to create a control for
    * @param varName Name of the variable the node is stored in the session as 
    *                (used for value binding expression)
    * @param parent The parent component for the control
    */
   private void generateControl(FacesContext context, String propName, 
                                String varName)
   {
      ValueBinding vb = context.getApplication().
                        createValueBinding("#{" + varName + ".properties[\"" + 
                        propName + "\"]}");
      
      UIOutput control = null;
      UIPropertySheet propSheet = (UIPropertySheet)this.getParent();
      if (propSheet.getMode().equalsIgnoreCase(UIPropertySheet.VIEW_MODE))
      {
         // if we are in view mode simply output the text to the screen
         control = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
         control.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
      }
      else
      {
         // as we don't know the type of the property we can only output a text field 
         control = (UIInput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_INPUT);
         control.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         control.getAttributes().put("size", "35");
         control.getAttributes().put("maxlength", "1024");
      
         // set control to disabled state if set to read only
         if (isReadOnly())
         {
            control.getAttributes().put("disabled", Boolean.TRUE);
         }
      }
      
      // set the common attributes
      control.setId(context.getViewRoot().createUniqueId());
      control.setValueBinding("value", vb);
      
      // if a converter has been specified we need to instantiate it
      // and apply it to the control
      if (getConverter() != null)
      {
         try
         {
            Converter conv = context.getApplication().createConverter(getConverter());
            control.setConverter(conv);
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + getConverter() + " could not be applied");
         }
      }
      
      // add the control itself
      this.getChildren().add(control);
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + propName +  
                      "' and added it to component " + this);
   }
}
