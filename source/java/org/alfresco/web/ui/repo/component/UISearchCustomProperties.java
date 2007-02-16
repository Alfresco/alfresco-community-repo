/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.List;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.AdvancedSearchConfigElement;
import org.alfresco.web.config.AdvancedSearchConfigElement.CustomProperty;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class UISearchCustomProperties extends SelfRenderingComponent implements NamingContainer
{
   public static final String PREFIX_DATE_TO    = "to_";
   public static final String PREFIX_DATE_FROM  = "from_";
   
   private static final String VALUE = "value";
   
   private static final String MSG_TO   = "to";
   private static final String MSG_FROM = "from";
   
   private static Log logger = LogFactory.getLog(UISearchCustomProperties.class);
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.AdvancedSearch";
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      if (getChildCount() == 0)
      {
         createComponentsFromConfig(context);
      }
      
      // encode the components in a 2 column table
      out.write("<table cellspacing=2 cellpadding=2 border=0");
      outputAttribute(out, getAttributes().get("styleClass"), "class");
      outputAttribute(out, getAttributes().get("style"), "style");
      out.write('>');
      List<UIComponent> children = getChildren();
      int colCounter = 0;
      for (int i=0; i<children.size(); i++)
      {
         UIComponent component = children.get(i);
         if (component instanceof UIPanel)
         {
            out.write("<tr><td colspan=2>");
            Utils.encodeRecursive(context, component);
            out.write("</td></tr>");
            colCounter += 2;
         }
         else
         {
            if ((colCounter & 1) == 0)
            {
               out.write("<tr>");
            }
            out.write("<td>");
            Utils.encodeRecursive(context, component);
            out.write("</td>");
            if ((colCounter & 1) == 1)
            {
               out.write("</tr>");
            }
            colCounter++;
         }
      }
      out.write("</table>");
   }
   
   /**
    * Build the components from the Advanced Search config entries
    * 
    * @param context FacesContext
    */
   @SuppressWarnings("unchecked")
   private void createComponentsFromConfig(FacesContext context)
   {
      DictionaryService dd = Repository.getServiceRegistry(context).getDictionaryService();
      AdvancedSearchConfigElement config = (AdvancedSearchConfigElement)Application.getConfigService(
            context).getConfig("Advanced Search").getConfigElement(AdvancedSearchConfigElement.CONFIG_ELEMENT_ID);
      
      // create an appropriate component for each custom property
      // using the DataDictionary to look-up labels and value types
      String beanBinding = (String)getAttributes().get("bean") + '.' + (String)getAttributes().get("var");
      List<CustomProperty> props = config.getCustomProperties();
      if (props != null)
      {
         for (CustomProperty property : props)
         {
            try
            {
               // try to find the Property definition for the specified Type or Aspect
               PropertyDefinition propDef = null;
               if (property.Type != null)
               {
                  QName type = Repository.resolveToQName(property.Type);
                  TypeDefinition typeDef = dd.getType(type);
                  if (typeDef == null)
                  {
                     throw new AlfrescoRuntimeException("No Type Definition found for: " + property.Type + " - Was an Aspect expected?");
                  }
                  propDef = typeDef.getProperties().get(Repository.resolveToQName(property.Property));
               }
               else if (property.Aspect != null)
               {
                  QName aspect = Repository.resolveToQName(property.Aspect);
                  AspectDefinition aspectDef = dd.getAspect(aspect);
                  if (aspectDef == null)
                  {
                     throw new AlfrescoRuntimeException("No Aspect Definition found for: " + property.Aspect + " - Was a Type expected?");
                  }
                  propDef = aspectDef.getProperties().get(Repository.resolveToQName(property.Property));
               }
               
               // if we found a def, then we can build components to represent it
               if (propDef != null)
               {
                  // resolve display label I18N message
                  String label;
                  if (property.LabelId != null && property.LabelId.length() != 0)
                  {
                     label = Application.getMessage(context, property.LabelId);
                  }
                  else
                  {
                     // or use dictionary label or QName as last resort
                     label = propDef.getTitle() != null ? propDef.getTitle() : propDef.getName().getLocalName();
                  }
                  
                  // special handling for Date and DateTime
                  DataTypeDefinition dataTypeDef = propDef.getDataType();
                  if (DataTypeDefinition.DATE.equals(dataTypeDef.getName()) || DataTypeDefinition.DATETIME.equals(dataTypeDef.getName()))
                  {
                     getChildren().add( generateControl(context, propDef, label, beanBinding) );
                  }
                  else
                  {
                     getChildren().add( generateLabel(context, label) );
                     getChildren().add( generateControl(context, propDef, null, beanBinding) );
                  }
               }
            }
            catch (DictionaryException ddErr)
            {
               logger.warn("Error building custom properties for Advanced Search: " + ddErr.getMessage());
            }
         }
      }
   }
   
   /**
    * Generates a JSF OutputText component/renderer
    * 
    * @param context JSF context
    * @param displayLabel The display label text
    * @param parent The parent component for the label
    * 
    * @return UIComponent
    */
   private UIComponent generateLabel(FacesContext context, String displayLabel)
   {
      UIOutput label = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
      label.setId(context.getViewRoot().createUniqueId());
      label.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
      label.setValue(displayLabel + ": ");
      return label;
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context       JSF context
    * @param propDef       The definition of the property to create the control for
    * @param displayLabel  Display label for the component
    * @param beanBinding   Combined name of the value bound bean and variable used for value binding expression
    * 
    * @return UIComponent
    */
   @SuppressWarnings("unchecked")
   private UIComponent generateControl(FacesContext context, PropertyDefinition propDef, String displayLabel, String beanBinding)
   {
      UIComponent control = null;
      
      DataTypeDefinition dataTypeDef = propDef.getDataType();
      QName typeName = dataTypeDef.getName();
      
      javax.faces.application.Application facesApp = context.getApplication();
      
      // create default value binding to a Map of values with a defined name
      ValueBinding vb = facesApp.createValueBinding(
            "#{" + beanBinding + "[\"" + propDef.getName().toString() + "\"]}");
      
      // generate the appropriate input field
      if (typeName.equals(DataTypeDefinition.BOOLEAN))
      {
         control = (UISelectBoolean)facesApp.createComponent(ComponentConstants.JAVAX_FACES_SELECT_BOOLEAN);
         control.setRendererType(ComponentConstants.JAVAX_FACES_CHECKBOX);
         control.setValueBinding(VALUE, vb);
      }
      else if (typeName.equals(DataTypeDefinition.CATEGORY))
      {
         control = (UICategorySelector)facesApp.createComponent(RepoConstants.ALFRESCO_FACES_CATEGORY_SELECTOR);
         control.setValueBinding(VALUE, vb);
      }
      else if (typeName.equals(DataTypeDefinition.DATETIME) || typeName.equals(DataTypeDefinition.DATE))
      {
         Boolean showTime = Boolean.valueOf(typeName.equals(DataTypeDefinition.DATETIME));
         
         // create value bindings for the start year and year count attributes
         ValueBinding startYearBind = null;
         ValueBinding yearCountBind = null;
         
         if (showTime)
         {
            startYearBind = facesApp.createValueBinding("#{DateTimePickerGenerator.startYear}");
            yearCountBind = facesApp.createValueBinding("#{DateTimePickerGenerator.yearCount}");
         }
         else
         {
            startYearBind = facesApp.createValueBinding("#{DatePickerGenerator.startYear}");
            yearCountBind = facesApp.createValueBinding("#{DatePickerGenerator.yearCount}");
         }
         
         
         // Need to output component for From and To date selectors and labels
         // also neeed checkbox for enable/disable state - requires an outer wrapper component
         control = (UIPanel)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PANEL);
         control.setRendererType(ComponentConstants.JAVAX_FACES_GRID);
         control.getAttributes().put("columns", Integer.valueOf(2));
         
         // enabled state checkbox
         UIInput checkbox = (UIInput)facesApp.createComponent(ComponentConstants.JAVAX_FACES_SELECT_BOOLEAN);
         checkbox.setRendererType(ComponentConstants.JAVAX_FACES_CHECKBOX);
         checkbox.setId(context.getViewRoot().createUniqueId());
         ValueBinding vbCheckbox = facesApp.createValueBinding(
            "#{" + beanBinding + "[\"" + propDef.getName().toString() + "\"]}");
         checkbox.setValueBinding(VALUE, vbCheckbox);
         control.getChildren().add(checkbox);
         
         // main display label
         UIOutput label = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
         label.setId(context.getViewRoot().createUniqueId());
         label.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         label.setValue(displayLabel + ":");
         control.getChildren().add(label);
         
         // from date label
         UIOutput labelFromDate = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
         labelFromDate.setId(context.getViewRoot().createUniqueId());
         labelFromDate.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         labelFromDate.setValue(Application.getMessage(context, MSG_FROM));
         control.getChildren().add(labelFromDate);
         
         // from date control
         UIInput inputFromDate = (UIInput)facesApp.createComponent(ComponentConstants.JAVAX_FACES_INPUT);
         inputFromDate.setId(context.getViewRoot().createUniqueId());
         inputFromDate.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
         inputFromDate.setValueBinding("startYear", startYearBind);
         inputFromDate.setValueBinding("yearCount", yearCountBind);
         inputFromDate.getAttributes().put("initialiseIfNull", Boolean.TRUE);
         inputFromDate.getAttributes().put("showTime", showTime);
         ValueBinding vbFromDate = facesApp.createValueBinding(
            "#{" + beanBinding + "[\"" + PREFIX_DATE_FROM + propDef.getName().toString() + "\"]}");
         inputFromDate.setValueBinding(VALUE, vbFromDate);
         control.getChildren().add(inputFromDate);
         
         // to date label
         UIOutput labelToDate = (UIOutput)context.getApplication().createComponent(ComponentConstants.JAVAX_FACES_OUTPUT);
         labelToDate.setId(context.getViewRoot().createUniqueId());
         labelToDate.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         labelToDate.setValue(Application.getMessage(context, MSG_TO));
         control.getChildren().add(labelToDate);
         
         // to date control
         UIInput inputToDate = (UIInput)facesApp.createComponent(ComponentConstants.JAVAX_FACES_INPUT);
         inputToDate.setId(context.getViewRoot().createUniqueId());
         inputToDate.setRendererType(RepoConstants.ALFRESCO_FACES_DATE_PICKER_RENDERER);
         inputToDate.setValueBinding("startYear", startYearBind);
         inputToDate.setValueBinding("yearCount", yearCountBind);
         inputToDate.getAttributes().put("initialiseIfNull", Boolean.TRUE);
         inputToDate.getAttributes().put("showTime", showTime);
         ValueBinding vbToDate = facesApp.createValueBinding(
            "#{" + beanBinding + "[\"" + PREFIX_DATE_TO + propDef.getName().toString() + "\"]}");
         inputToDate.setValueBinding(VALUE, vbToDate);
         control.getChildren().add(inputToDate);
      }
      else if (typeName.equals(DataTypeDefinition.NODE_REF))
      {
         control = (UISpaceSelector)facesApp.createComponent(RepoConstants.ALFRESCO_FACES_SPACE_SELECTOR);
         control.setValueBinding(VALUE, vb);
      }
      else
      {
         // any other type is represented as an input text field
         control = (UIInput)facesApp.createComponent(ComponentConstants.JAVAX_FACES_INPUT);
         control.setRendererType(ComponentConstants.JAVAX_FACES_TEXT);
         control.setValueBinding("size", facesApp.createValueBinding("#{TextFieldGenerator.size}"));
         control.setValueBinding("maxlength", facesApp.createValueBinding("#{TextFieldGenerator.maxLength}"));
         control.setValueBinding(VALUE, vb);
      }
      
      // set up the common aspects of the control
      control.setId(context.getViewRoot().createUniqueId());
      
      return control;
   }
}
