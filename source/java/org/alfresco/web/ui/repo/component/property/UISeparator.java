/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to represent a separator within a property sheet
 * 
 * @author gavinc
 */
public class UISeparator extends PropertySheetItem
{
   public static final String COMPONENT_FAMILY = "org.alfresco.faces.Separator";
   
   private static Log logger = LogFactory.getLog(UISeparator.class);

   /**
    * Default constructor
    */
   public UISeparator()
   {
      // set the default renderer
      setRendererType("org.alfresco.faces.SeparatorRenderer");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   protected String getIncorrectParentMsg()
   {
      return "The separator component must be nested within a property sheet component";
   }

   protected void generateItem(FacesContext context, UIPropertySheet propSheet) throws IOException
   {
      String componentGeneratorName = this.getComponentGenerator(); 
      
      if (componentGeneratorName == null)
      {
         componentGeneratorName = RepoConstants.GENERATOR_SEPARATOR;
      }
      
      UIComponent separator = FacesHelper.getComponentGenerator(context, componentGeneratorName).
            generateAndAdd(context, propSheet, this);
      
      if (logger.isDebugEnabled())
         logger.debug("Created separator " + separator + "(" + 
                      separator.getClientId(context) + 
                      ") for '" + this.getName() +  
                      "' and added it to component " + this);
   }
}
