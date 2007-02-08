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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.ui.repo.RepoConstants;
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

   protected String getIncorrectParentMsg()
   {
      return "The association component must be nested within a property sheet component";
   }
      
   protected void generateItem(FacesContext context, UIPropertySheet propSheet) throws IOException
   {
      String associationName = (String)getName();

      // get details of the association
      DataDictionary dd = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
            context).getBean(Application.BEAN_DATA_DICTIONARY);
      AssociationDefinition assocDef = dd.getAssociationDefinition(propSheet.getNode(), associationName);
      
      if (assocDef == null)
      {
         logger.warn("Failed to find association definition for association '" + associationName + "'");
      }
      else
      {
         // we've found the association definition but we also need to check
         // that the association is not a parent child one
         if (assocDef.isChild())
         {
            logger.warn("The association named '" + associationName + "' is not an association");
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
            generateLabel(context, propSheet, displayLabel);
            generateControl(context, propSheet, assocDef);
         }
      }
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context JSF context
    * @param propSheet The property sheet this property belongs to
    * @param assocDef The definition of the association to create the control for
    */
   private void generateControl(FacesContext context, UIPropertySheet propSheet, 
         AssociationDefinition assocDef)
   {
      UIAssociationEditor control = (UIAssociationEditor)FacesHelper.getComponentGenerator(
            context, RepoConstants.GENERATOR_ASSOCIATION).generateAndAdd(context, propSheet, this);
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + assocDef.getName().toString() + 
                      "' and added it to component " + this);
   }
}
