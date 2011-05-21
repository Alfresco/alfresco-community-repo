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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIForm;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.PropertySheetConfigElement;
import org.alfresco.web.config.PropertySheetConfigElement.AssociationConfig;
import org.alfresco.web.config.PropertySheetConfigElement.ChildAssociationConfig;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.alfresco.web.config.PropertySheetConfigElement.PropertyConfig;
import org.alfresco.web.config.PropertySheetConfigElement.SeparatorConfig;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that represents the properties of a Node
 * 
 * @author gavinc
 */
public class UIPropertySheet extends UIPanel implements NamingContainer
{
   public static final String VIEW_MODE = "view";
   public static final String EDIT_MODE = "edit";
   
   private static Log logger = LogFactory.getLog(UIPropertySheet.class);
   private static String DEFAULT_VAR_NAME = "node";
   private static String PROP_ID_PREFIX = "prop_";
   private static String ASSOC_ID_PREFIX = "assoc_";
   private static String SEP_ID_PREFIX = "sep_";
   
   protected List<ClientValidation> validations = new ArrayList<ClientValidation>();
   private String variable;
   private NodeRef nodeRef;
   private Node node;
   private Boolean readOnly;
   private Boolean validationEnabled;
   private String mode;
   private String configArea;
   private String nextButtonId;
   private String finishButtonId;
   
   /**
    * Default constructor
    */
   public UIPropertySheet()
   {
      // set the default renderer for a property sheet
      setRendererType(ComponentConstants.JAVAX_FACES_GRID);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return UIPanel.COMPONENT_FAMILY;
   }

   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      int howManyChildren = getChildren().size();
      Boolean externalConfig = (Boolean)getAttributes().get("externalConfig");
      
      // generate a variable name to use if necessary
      if (this.variable == null)
      {
         this.variable = DEFAULT_VAR_NAME;
      }
      
      // force retrieval of node info
      Node node = getNode();
      
      if (howManyChildren == 0)
      {
         if (externalConfig != null && externalConfig.booleanValue())
         {
            // configure the component using the config service
            if (logger.isDebugEnabled())
               logger.debug("Configuring property sheet using ConfigService");

            // get the properties to display
            ConfigService configSvc = Application.getConfigService(FacesContext.getCurrentInstance());
            Config configProps = null;
            if (getConfigArea() == null)
            {
               configProps = configSvc.getConfig(node);
            }
            else
            {
               // only look within the given area
               configProps = configSvc.getConfig(node, new ConfigLookupContext(getConfigArea()));
            }
            
            PropertySheetConfigElement itemsToDisplay = (PropertySheetConfigElement)configProps.
               getConfigElement("property-sheet");
            
            if (itemsToDisplay != null)
            {
               Collection<ItemConfig> itemsToRender = null;
               
               if (this.getMode().equalsIgnoreCase(EDIT_MODE))
               {
                  itemsToRender = itemsToDisplay.getEditableItemsToShow().values();
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Items to render: " + itemsToDisplay.getEditableItemNamesToShow());
               }
               else
               {
                  itemsToRender = itemsToDisplay.getItemsToShow().values();
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Items to render: " + itemsToDisplay.getItemNamesToShow());
               }
            
               createComponentsFromConfig(context, itemsToRender);
            }
            else
            {
               if (logger.isDebugEnabled())
                  logger.debug("There are no items to render!");
            }
         }
         else
         {
            // show all the properties for the current node
            if (logger.isDebugEnabled())
               logger.debug("Configuring property sheet using node's current state");
            
            createComponentsFromNode(context, node);
         }
      }
      
      // put the node in the session if it is not there already
      Map sessionMap = getFacesContext().getExternalContext().getSessionMap();
      sessionMap.put(this.variable, node);

      if (logger.isDebugEnabled())
         logger.debug("Put node into session with key '" + this.variable + "': " + node);

      super.encodeBegin(context);
   }

   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeEnd(FacesContext context) throws IOException
   {
      super.encodeEnd(context);
      
      // NOTE: We should really use a renderer to output the JavaScript below but that would
      //       require extending the MyFaces HtmlGridRenderer class which we should avoid doing.
      //       Until we support multiple client types this will be OK.
      
      // output the JavaScript to enforce the required validations (if validation is enabled)
      if (isValidationEnabled() && this.validations.size() > 0)
      {
         renderValidationScript(context);
      }
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.nodeRef = (NodeRef)values[1];
      this.node = (Node)values[2];
      this.variable = (String)values[3];
      this.readOnly = (Boolean)values[4];
      this.mode = (String)values[5];
      this.configArea = (String)values[6];
      this.validationEnabled = (Boolean)values[7];
      this.validations = (List<ClientValidation>)values[8];
      this.finishButtonId = (String)values[9];
      this.nextButtonId = (String)values[10];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[11];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.nodeRef;
      values[2] = this.node;
      values[3] = this.variable;
      values[4] = this.readOnly;
      values[5] = this.mode;
      values[6] = this.configArea;
      values[7] = this.validationEnabled;
      values[8] = this.validations;
      values[9] = this.finishButtonId;
      values[10] = this.nextButtonId;
      return (values);
   }
   
   /**
    * @return Returns the node
    */
   public Node getNode()
   {
      Node node = null;
      
      if (this.node == null)
      {
         // use the value to get hold of the actual object
         Object value = getAttributes().get("value");
         
         if (value == null)
         {
            ValueBinding vb = getValueBinding("value");
            if (vb != null)
            {
               value = vb.getValue(getFacesContext());
            }
         }

         if (value instanceof Node)
         {
            node = (Node)value;
         }
      }
      else
      {
         node = this.node;
      }
      
      return node;
   }
   
   /**
    * @param node The node
    */
   public void setNode(Node node)
   {
      this.node = node;
   }
   
   /**
    * @return Returns the variable.
    */
   public String getVar()
   {
      return this.variable;
   }

   /**
    * @param variable The variable to set.
    */
   public void setVar(String variable)
   {
      this.variable = variable;
   }
   
   /**
    * @return Returns whether the property sheet is read only
    */
   public boolean isReadOnly()
   {
      if (this.readOnly == null)
      {
         ValueBinding vb = getValueBinding("readOnly");
         if (vb != null)
         {
            this.readOnly = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      if (this.readOnly == null)
      {
         this.readOnly = Boolean.FALSE;
      }
      
      return this.readOnly; 
   }

   /**
    * @param readOnly Sets the read only flag for the property sheet
    */
   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = Boolean.valueOf(readOnly);
   }

   /**
    * @return true if validation is enabled for this property sheet
    */
   public boolean isValidationEnabled()
   {
      // if the property sheet is in "view" mode validation will
      // always be disabled
      if (inEditMode() == false)
      {
         return false;
      }
      
      if (this.validationEnabled == null)
      {
         ValueBinding vb = getValueBinding("validationEnabled");
         if (vb != null)
         {
            this.validationEnabled = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      if (this.validationEnabled == null)
      {
         this.validationEnabled = Boolean.TRUE;
      }
      
      return this.validationEnabled; 
   }

   /**
    * @param validationEnabled Sets the validationEnabled flag
    */
   public void setValidationEnabled(boolean validationEnabled)
   {
      this.validationEnabled = Boolean.valueOf(validationEnabled);
   }

   /**
    * Returns the id of the finish button
    * 
    * @return The id of the finish button on the page
    */
   public String getFinishButtonId()
   {
      // NOTE: This parameter isn't value binding enabled
      if (this.finishButtonId == null)
      {
         this.finishButtonId = "finish-button";
      }
      
      return this.finishButtonId;
   }

   /**
    * Sets the id of the finish button being used on the page
    * 
    * @param finishButtonId The id of the finish button
    */
   public void setFinishButtonId(String finishButtonId)
   {
      this.finishButtonId = finishButtonId;
   }
   
   /**
    * Returns the id of the next button
    * 
    * @return The id of the next button on the page
    */
   public String getNextButtonId()
   {
      return this.nextButtonId;
   }

   /**
    * Sets the id of the next button being used on the page
    * 
    * @param nextButtonId The id of the next button
    */
   public void setNextButtonId(String nextButtonId)
   {
      this.nextButtonId = nextButtonId;
   }

   /**
    * @return Returns the mode
    */
   public String getMode()
   {
      if (this.mode == null)
      {
         ValueBinding vb = getValueBinding("mode");
         if (vb != null)
         {
            this.mode = (String)vb.getValue(getFacesContext());
         }
      }
      
      if (this.mode == null)
      {
         mode = EDIT_MODE;
      }
      
      return mode;
   }

   /**
    * @param mode Sets the mode
    */
   public void setMode(String mode)
   {
      this.mode = mode;
   }
   
   /**
    * Determines whether the property sheet is in edit mode
    * 
    * @return true if in edit mode
    */
   public boolean inEditMode()
   {
      return getMode().equalsIgnoreCase(EDIT_MODE);
   }
   
   /**
    * @return Returns the config area to use
    */
   public String getConfigArea()
   {
      if (this.configArea == null)
      {
         ValueBinding vb = getValueBinding("configArea");
         if (vb != null)
         {
            this.configArea = (String)vb.getValue(getFacesContext());
         }
      }
      
      return configArea;
   }
   
   /**
    * @param configArea Sets the config area to use
    */
   public void setConfigArea(String configArea)
   {
      this.configArea = configArea;
   }
   
   /**
    * Adds a validation case to the property sheet
    * 
    * @param validation The validation case to enforce
    */
   public void addClientValidation(ClientValidation validation)
   {
      this.validations.add(validation);
   }
   
   /**
    * @return Returns the list of client validations to enforce
    */
   public List<ClientValidation> getClientValidations()
   {
      return this.validations;
   }
   
   /**
    * Renders the necessary JavaScript to enforce any constraints the properties
    * have.
    * 
    * @param context FacesContext
    */
   @SuppressWarnings("unchecked")
   protected void renderValidationScript(FacesContext context) throws IOException
   {
      ResponseWriter out = context.getResponseWriter();
      UIForm form = Utils.getParentForm(context, this);
         
      // TODO: We need to encode all the JavaScript functions here 
      //       with the client id of the property sheet so that we
      //       can potentially add more than one property sheet to
      //       page and have validation function correctly.
      
      // output the validation.js script
      out.write("\n<script type='text/javascript' src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/scripts/validation.js");
      out.write("'></script>\n<script type='text/javascript'>\n");
      
      // output variable to hold flag for which submit button was pressed
      out.write("var finishButtonPressed = false;\n");
      out.write("var nextButtonPressed = false;\n");
      out.write("var transitionButtonPressed = false;\n");
      
      // output the validate() function
      out.write("function validate()\n{\n   var result = true;\n   ");
      out.write("if ((transitionButtonPressed || finishButtonPressed || nextButtonPressed) && (");
      
      int numberValidations = this.validations.size();
      List<ClientValidation> realTimeValidations = 
         new ArrayList<ClientValidation>(numberValidations);
      
      for (int x = 0; x < numberValidations; x++)
      {
         ClientValidation validation = this.validations.get(x);
         
         if (validation.RealTimeChecking)
         {
            realTimeValidations.add(validation);
         }
         
         renderValidationMethod(out, validation, (x == (numberValidations-1)), true);
      }
      
      // return false if validation failed to stop the form submitting
      out.write(")\n   { result = false; }\n\n");
      out.write("   finishButtonPressed = false;\n   nextButtonPressed = false;\n   transitionButtonPressed = false;\n");
      out.write("   return result;\n}\n\n");
      
      // output the processButtonState() function (if necessary)
      int numberRealTimeValidations = realTimeValidations.size();
      if (numberRealTimeValidations > 0)
      {
         out.write("function processButtonState()\n{\n   if (");
         
         for (int x = 0; x < numberRealTimeValidations; x++)
         {
            renderValidationMethod(out, realTimeValidations.get(x),
                  (x == (numberRealTimeValidations-1)), false);
         }
      
         // disable the finish button if validation failed and 
         // also the next button if it is present
         
         
         out.write("\n   {\n      document.getElementById('");
         out.write(form.getClientId(context));
         out.write(NamingContainer.SEPARATOR_CHAR);
         out.write(getFinishButtonId());
         out.write("').disabled = true; \n");
         if (this.nextButtonId != null && this.nextButtonId.length() > 0)
         {
            out.write("      document.getElementById('");
            out.write(form.getClientId(context));
            out.write(NamingContainer.SEPARATOR_CHAR);
            out.write(this.nextButtonId);
            out.write("').disabled = true; \n");
         }
         out.write("   }\n");
         
         out.write("   else\n   {\n      document.getElementById('");
         out.write(form.getClientId(context));
         out.write(NamingContainer.SEPARATOR_CHAR);
         out.write(getFinishButtonId());
         out.write("').disabled = false;");
         
         if (this.nextButtonId != null && this.nextButtonId.length() > 0)
         {
            out.write("\n      document.getElementById('");
            out.write(form.getClientId(context));
            out.write(NamingContainer.SEPARATOR_CHAR);
            out.write(this.nextButtonId);
            out.write("').disabled = false;");
         }
         
         out.write("\n   }\n}\n\n");
      }
      
      // write out a function to initialise everything
      out.write("function initValidation()\n{\n");
      
      // register the validate function as the form onsubmit handler
      out.write("   document.getElementById('");
      out.write(form.getClientId(context));
      out.write("').onsubmit = validate;\n");
      
      // set the flag when the finish button is clicked
      out.write("   document.getElementById('");
      out.write(form.getClientId(context));
      out.write(NamingContainer.SEPARATOR_CHAR);
      out.write(getFinishButtonId());
      out.write("').onclick = function() { finishButtonPressed = true; }\n");
      
      // transition buttons on the workflow page also need to handle validation
      // so look for submit buttons with ":transition_" in the id
      out.write("   var inputItems = document.getElementsByTagName('input');\n");
      out.write("   for (i in inputItems)\n");
      out.write("   {\n");
      out.write("      if (inputItems[i].type == 'submit' && inputItems[i].id !== undefined && inputItems[i].id.indexOf(':transition_') != -1)\n");
      out.write("      {\n");
      out.write("         inputItems[i].onclick = function() { transitionButtonPressed = true; }\n");
      out.write("      }\n");
      out.write("   }\n");
      
      // set the flag when the next button is clicked
      if (this.nextButtonId != null && this.nextButtonId.length() > 0)
      {
         out.write("   document.getElementById('");
         out.write(form.getClientId(context));
         out.write(NamingContainer.SEPARATOR_CHAR);
         out.write(this.nextButtonId);
         out.write("').onclick = function() { nextButtonPressed = true; }\n");
      }
      
      // perform an initial check at page load time (if we have any real time validations)
      if (numberRealTimeValidations > 0)
      {
         out.write("   processButtonState();\n");
      }
      
      // close out the init function
      out.write("}\n\n");
      
      // setup init function to be called at page load time
      out.write("window.onload=initValidation;\n");
      
      // close out the script block
      out.write("</script>\n");
   }
   
   protected void renderValidationMethod(ResponseWriter out, ClientValidation validation,
         boolean lastMethod, boolean showMessage) throws IOException
   {
      out.write("!");
      out.write(validation.Type);
      out.write("(");
      
      // add the parameters
      int numberParams = validation.Params.size();
      for (int p = 0; p < numberParams; p++)
      {
         out.write(validation.Params.get(p));
         if (p != (numberParams-1))
         {
            out.write(", ");
         }
      }
      
      // add the parameter to show any validation messages
      out.write(", ");
      out.write(Boolean.toString(showMessage));
      out.write(")");
      
      if (lastMethod)
      {
         out.write(")");
      }
      else
      {
         out.write(" || ");
      }
   }
   
   /**
    * Creates all the property components required to display the properties held by the node.
    * 
    * @param context JSF context
    * @param node The Node to show all the properties for 
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private void createComponentsFromNode(FacesContext context, Node node)
      throws IOException
   {
      // add all the properties of the node to the UI
      Map<String, Object> props = node.getProperties();
      for (String propertyName : props.keySet())
      {
         // create the property component
         UIProperty propComp = (UIProperty)context.getApplication().
               createComponent(RepoConstants.ALFRESCO_FACES_PROPERTY);
         
         // get the property name in it's prefix form
         QName qname = QName.createQName(propertyName);
         String prefixPropName = qname.toPrefixString();
         
         FacesHelper.setupComponentId(context, propComp, PROP_ID_PREFIX + prefixPropName);
         propComp.setName(prefixPropName);
         
         // if this property sheet is set as read only, set all properties to read only
         if (isReadOnly())
         {
            propComp.setReadOnly(true);
         }
         
         // NOTE: we don't know what the display label is so don't set it
         
         this.getChildren().add(propComp);
         
         if (logger.isDebugEnabled())
            logger.debug("Created property component " + propComp + "(" + 
                   propComp.getClientId(context) + 
                   ") for '" + prefixPropName +
                   "' and added it to property sheet " + this);
      }
      
      // add all the associations of the node to the UI
      Map associations = node.getAssociations();
      Iterator iter = associations.keySet().iterator();
      while (iter.hasNext())
      {
         String assocName = (String)iter.next();
         UIAssociation assocComp = (UIAssociation)context.getApplication().
               createComponent(RepoConstants.ALFRESCO_FACES_ASSOCIATION);
         
         // get the association name in it's prefix form
         QName qname = QName.createQName(assocName);
         String prefixAssocName = qname.toPrefixString();
         
         FacesHelper.setupComponentId(context, assocComp, ASSOC_ID_PREFIX + prefixAssocName);
         assocComp.setName(prefixAssocName);
         
         // if this property sheet is set as read only, set all properties to read only
         if (isReadOnly())
         {
            assocComp.setReadOnly(true);
         }
         
         // NOTE: we don't know what the display label is so don't set it
         
         this.getChildren().add(assocComp);
         
         if (logger.isDebugEnabled())
            logger.debug("Created association component " + assocComp + "(" + 
                   assocComp.getClientId(context) + 
                   ") for '" + prefixAssocName +
                   "' and added it to property sheet " + this);
      }
      
      // add all the child associations of the node to the UI
      Map childAssociations = node.getChildAssociations();
      iter = childAssociations.keySet().iterator();
      while (iter.hasNext())
      {
         String assocName = (String)iter.next();
         UIChildAssociation childAssocComp = (UIChildAssociation)context.getApplication().
               createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOCIATION);
         FacesHelper.setupComponentId(context, childAssocComp, ASSOC_ID_PREFIX + assocName);
         childAssocComp.setName(assocName);
         
         // if this property sheet is set as read only, set all properties to read only
         if (isReadOnly())
         {
            childAssocComp.setReadOnly(true);
         }
         
         // NOTE: we don't know what the display label is so don't set it
         
         this.getChildren().add(childAssocComp);
         
         if (logger.isDebugEnabled())
            logger.debug("Created child association component " + childAssocComp + "(" + 
                   childAssocComp.getClientId(context) + 
                   ") for '" + assocName +
                   "' and added it to property sheet " + this);
      }
   }
   
   /**
    * Creates all the property components required to display the properties specified
    * in an external config file.
    * 
    * @param context JSF context
    * @param properties Collection of properties to render (driven from configuration) 
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private void createComponentsFromConfig(FacesContext context, Collection<ItemConfig> items)
      throws IOException
   {
      for (ItemConfig item : items)
      {
         String id = null;
         PropertySheetItem propSheetItem = null;
         
         // create the appropriate component
         if (item instanceof PropertyConfig)
         {
            id = PROP_ID_PREFIX + item.getName();
            propSheetItem = (PropertySheetItem)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_PROPERTY);
         }
         else if (item instanceof AssociationConfig)
         {
            id = ASSOC_ID_PREFIX + item.getName();
            propSheetItem = (PropertySheetItem)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_ASSOCIATION);
         }
         else if (item instanceof ChildAssociationConfig)
         {
            id = ASSOC_ID_PREFIX + item.getName();
            propSheetItem = (PropertySheetItem)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOCIATION);
         }
         else if (item instanceof SeparatorConfig)
         {
            id = SEP_ID_PREFIX + item.getName();
            propSheetItem = (PropertySheetItem)context.getApplication().
                  createComponent(RepoConstants.ALFRESCO_FACES_SEPARATOR);
         }
         
         // now setup the common stuff across all component types
         if (propSheetItem != null)
         {
            FacesHelper.setupComponentId(context, propSheetItem, id);
            propSheetItem.setName(item.getName());
            propSheetItem.setConverter(item.getConverter());
            propSheetItem.setComponentGenerator(item.getComponentGenerator());
            propSheetItem.setIgnoreIfMissing(item.getIgnoreIfMissing());
   
            String displayLabel = item.getDisplayLabel();
            if (item.getDisplayLabelId() != null)
            {
               String label = Application.getMessage(context, item.getDisplayLabelId());
               if (label != null)
               {
                  displayLabel = label; 
               }
            }
            propSheetItem.setDisplayLabel(displayLabel);
            
            // if this property sheet is set as read only or the config says the property
            // should be read only set it as such
            if (isReadOnly() || item.isReadOnly())
            {
               propSheetItem.setReadOnly(true);
            }
            
            this.getChildren().add(propSheetItem);
            
            if (logger.isDebugEnabled())
               logger.debug("Created property sheet item component " + propSheetItem + "(" + 
                      propSheetItem.getClientId(context) + 
                      ") for '" + item.getName() + 
                      "' and added it to property sheet " + this);
         }
      }
   }
   
   /**
    * Inner class representing a validation case that must be enforced.
    */
   @SuppressWarnings("serial") 
   public static class ClientValidation implements Serializable
   {
      public String Type;
      public List<String> Params;
      public boolean RealTimeChecking;
      
      /**
       * Default constructor
       * 
       * @param type The type of the validation
       * @param params A List of String parameters to use for the validation
       * @param realTimeChecking true to check the property sheet in real time
       *        i.e. as the user types or uses the mouse
       */
      public ClientValidation(String type, List<String> params, boolean realTimeChecking)
      {
         this.Type = type;
         this.Params = params;
         this.RealTimeChecking = realTimeChecking;
      }
   }
}
