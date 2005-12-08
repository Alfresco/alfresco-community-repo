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
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Component to represent an individual association within a property sheet
 * 
 * @author gavinc
 */
public class UIAssociation extends PropertySheetItem
{
   private static final String MSG_ERROR_ASSOC = "error_association";
   private static final String MSG_ERROR_NOT_ASSOC = "error_not_association";

   private static Log logger = LogFactory.getLog(UIAssociation.class);
   
   /**
    * Default constructor
    */
   public UIAssociation()
   {
      // set the default renderer
      setRendererType("org.alfresco.faces.AssociationRenderer");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Association";
   }

   /**
    * @see org.alfresco.web.ui.repo.component.property.PropertySheetItem#getIncorrectParentMsg()
    */
   protected String getIncorrectParentMsg()
   {
      return "The association component must be nested within a property sheet component";
   }
   
   /**
    * @see org.alfresco.web.ui.repo.component.property.PropertySheetItem#generateItem(javax.faces.context.FacesContext, org.alfresco.web.bean.repository.Node, java.lang.String)
    */
   protected void generateItem(FacesContext context, Node node, String var) throws IOException
   {
      String associationName = (String)getName();

      // get details of the association
      DataDictionary dd = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(Application.BEAN_DATA_DICTIONARY);
      AssociationDefinition assocDef = dd.getAssociationDefinition(node, associationName);
      
      if (assocDef == null)
      {
         logger.warn("Failed to find association definition for association '" + associationName + "'");
         
         // add an error message as the property is not defined in the data dictionary and 
         // not in the node's set of properties
         String msg = MessageFormat.format(Application.getMessage(context, MSG_ERROR_ASSOC), new Object[] {associationName});
         Utils.addErrorMessage(msg);
      }
      else
      {
         // we've found the association definition but we also need to check
         // that the association is a parent child one
         if (assocDef.isChild())
         {
            String msg = MessageFormat.format(Application.getMessage(context, MSG_ERROR_NOT_ASSOC), new Object[] {associationName});
            Utils.addErrorMessage(msg);
         }
         else
         {
            String displayLabel = (String)getDisplayLabel();
            if (displayLabel == null)
            {
               // try and get the repository assigned label
               displayLabel = assocDef.getTitle();
               
               // if the label is still null default to the local name of the property
               if (displayLabel == null)
               {
                  displayLabel = assocDef.getName().getLocalName();
               }
            }
            
            // generate the label and type specific control
            generateLabel(context, displayLabel);
            generateControl(context, assocDef, var);
         }
      }
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context JSF context
    * @param propDef The definition of the association to create the control for
    * @param varName Name of the variable the node is stored in the session as 
    *                (used for value binding expression)
    * @param parent The parent component for the control
    */
   private void generateControl(FacesContext context, AssociationDefinition assocDef, 
                                String varName)
   {
      UIPropertySheet propSheet = (UIPropertySheet)this.getParent();
      ValueBinding vb = context.getApplication().createValueBinding("#{" + varName + "}");
      
      UIAssociationEditor control = (UIAssociationEditor)context.
         getApplication().createComponent("org.alfresco.faces.AssociationEditor");
      control.setAssociationName(assocDef.getName().toString());
      
      // set up the common aspects of the control
      control.setId(context.getViewRoot().createUniqueId());
      control.setValueBinding("value", vb);
      
      // disable the component if necessary
      if (propSheet.getMode().equalsIgnoreCase(UIPropertySheet.VIEW_MODE) || isReadOnly() || assocDef.isProtected())
      {
         control.setDisabled(true);
      }
      
      // add the control itself
      this.getChildren().add(control);
   }
}
