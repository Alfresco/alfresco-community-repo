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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigLookupContext;
import org.alfresco.config.ConfigService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.PropertySheetConfigElement;
import org.alfresco.web.config.PropertySheetConfigElement.AssociationConfig;
import org.alfresco.web.config.PropertySheetConfigElement.ChildAssociationConfig;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.alfresco.web.config.PropertySheetConfigElement.PropertyConfig;
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
   
   private String variable;
   private NodeRef nodeRef;
   private Node node;
   private Boolean readOnly;
   private String mode;
   private String configArea;
   
   /**
    * Default constructor
    */
   public UIPropertySheet()
   {
      // set the default renderer for a property sheet
      setRendererType("javax.faces.Grid");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "javax.faces.Panel";
   }

   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      int howManyKids = getChildren().size();
      Boolean externalConfig = (Boolean)getAttributes().get("externalConfig");
      
      // generate a variable name to use if necessary
      if (this.variable == null)
      {
         this.variable = DEFAULT_VAR_NAME;
      }
      
      // force retrieval of node info
      Node node = getNode();
      
      if (howManyKids == 0)
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
               List<ItemConfig> itemsToRender = null;
               
               if (this.getMode().equalsIgnoreCase(EDIT_MODE))
               {
                  itemsToRender = itemsToDisplay.getEditableItemsToShow();
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Items to render: " + itemsToDisplay.getEditableItemNamesToShow());
               }
               else
               {
                  itemsToRender = itemsToDisplay.getItemsToShow();
                  
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
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
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
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.nodeRef;
      values[2] = this.node;
      values[3] = this.variable;
      values[4] = this.readOnly;
      values[5] = this.mode;
      values[6] = this.configArea;
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
         
         // TODO: for now we presume the object is a Node, but we need to support id's too
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
    * Creates all the property components required to display the properties held by the node.
    * 
    * @param context JSF context
    * @param node The Node to show all the properties for 
    * @throws IOException
    */
   private void createComponentsFromNode(FacesContext context, Node node)
      throws IOException
   {
      // add all the properties of the node to the UI
      Map<String, Object> props = node.getProperties();
      for (String propertyName : props.keySet())
      {
         // create the property component
         UIProperty propComp = (UIProperty)context.getApplication().createComponent("org.alfresco.faces.Property");
         propComp.setId(context.getViewRoot().createUniqueId());
         propComp.setName(propertyName);
         
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
                   ") for '" + propertyName +
                   "' and added it to property sheet " + this);
      }
      
      // add all the associations of the node to the UI
      Map associations = node.getAssociations();
      Iterator iter = associations.keySet().iterator();
      while (iter.hasNext())
      {
         String assocName = (String)iter.next();
         UIAssociation assocComp = (UIAssociation)context.getApplication().createComponent("org.alfresco.faces.Association");
         assocComp.setId(context.getViewRoot().createUniqueId());
         assocComp.setName(assocName);
         
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
                   ") for '" + assocName +
                   "' and added it to property sheet " + this);
      }
      
      // add all the child associations of the node to the UI
      Map childAssociations = node.getChildAssociations();
      iter = childAssociations.keySet().iterator();
      while (iter.hasNext())
      {
         String assocName = (String)iter.next();
         UIChildAssociation childAssocComp = (UIChildAssociation)context.getApplication().createComponent(
               "org.alfresco.faces.ChildAssociation");
         childAssocComp.setId(context.getViewRoot().createUniqueId());
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
    * @param properties List of properties to render (driven from configuration) 
    * @throws IOException
    */
   private void createComponentsFromConfig(FacesContext context, List<ItemConfig> items)
      throws IOException
   {
      // **********************************
      // TODO: Make a common base class for the UIxxx components so we can
      //       reduce the code below...
      // **********************************
      
      for (ItemConfig item : items)
      {
         // create the appropriate component
         if (item instanceof PropertyConfig)
         {
            UIProperty propComp = (UIProperty)context.getApplication().createComponent("org.alfresco.faces.Property");
            propComp.setId(context.getViewRoot().createUniqueId());
            propComp.setName(item.getName());
            propComp.setConverter(item.getConverter());
            
            String displayLabel = item.getDisplayLabel();
            if (item.getDisplayLabelId() != null)
            {
               String label = Application.getMessage(context, item.getDisplayLabelId());
               if (label != null)
               {
                  displayLabel = label; 
               }
            }
            propComp.setDisplayLabel(displayLabel);
            
            // if this property sheet is set as read only or the config says the property
            // should be read only set it as such
            if (isReadOnly() || item.isReadOnly())
            {
               propComp.setReadOnly(true);
            }
            
            this.getChildren().add(propComp);
            
            if (logger.isDebugEnabled())
               logger.debug("Created property component " + propComp + "(" + 
                      propComp.getClientId(context) + 
                      ") for '" + item.getName() + 
                      "' and added it to property sheet " + this);
         }
         else if (item instanceof AssociationConfig)
         {
            UIAssociation assocComp = (UIAssociation)context.getApplication().createComponent("org.alfresco.faces.Association");
            assocComp.setId(context.getViewRoot().createUniqueId());
            assocComp.setName(item.getName());
            assocComp.setConverter(item.getConverter());
            
            String displayLabel = item.getDisplayLabel();
            if (item.getDisplayLabelId() != null)
            {
               String label = Application.getMessage(context, item.getDisplayLabelId());
               if (label != null)
               {
                  displayLabel = label; 
               }
            }
            assocComp.setDisplayLabel(displayLabel);
            
            // if this property sheet is set as read only or the config says the property
            // should be read only set it as such
            if (isReadOnly() || item.isReadOnly())
            {
               assocComp.setReadOnly(true);
            }
            
            this.getChildren().add(assocComp);
            
            if (logger.isDebugEnabled())
               logger.debug("Created association component " + assocComp + "(" + 
                      assocComp.getClientId(context) + 
                      ") for '" + item.getName() + 
                      "' and added it to property sheet " + this);
         }
         else if (item instanceof ChildAssociationConfig)
         {
            UIChildAssociation assocComp = (UIChildAssociation)context.getApplication().createComponent("org.alfresco.faces.ChildAssociation");
            assocComp.setId(context.getViewRoot().createUniqueId());
            assocComp.setName(item.getName());
            assocComp.setConverter(item.getConverter());
            
            String displayLabel = item.getDisplayLabel();
            if (item.getDisplayLabelId() != null)
            {
               String label = Application.getMessage(context, item.getDisplayLabelId());
               if (label != null)
               {
                  displayLabel = label; 
               }
            }
            assocComp.setDisplayLabel(displayLabel);
            
            // if this property sheet is set as read only or the config says the property
            // should be read only set it as such
            if (isReadOnly() || item.isReadOnly())
            {
               assocComp.setReadOnly(true);
            }
            
            this.getChildren().add(assocComp);
            
            if (logger.isDebugEnabled())
               logger.debug("Created child association component " + assocComp + "(" + 
                      assocComp.getClientId(context) + 
                      ") for '" + item.getName() + 
                      "' and added it to property sheet " + this);
         }
      }
   }
}
