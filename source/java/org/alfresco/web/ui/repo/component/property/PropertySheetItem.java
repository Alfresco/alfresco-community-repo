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
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all items that can appear in a property sheet component
 * 
 * @author gavinc
 */
public abstract class PropertySheetItem extends UIPanel implements NamingContainer
{
   private static Log logger = LogFactory.getLog(PropertySheetItem.class);
   
   protected String name;
   protected String displayLabel;
   protected String converter;
   protected Boolean readOnly;
   protected Boolean ignoreIfMissing;
   protected String componentGenerator;
   
   protected String resolvedDisplayLabel;
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      // get the variable being used from the parent
      UIComponent parent = this.getParent();
      if ((parent instanceof UIPropertySheet) == false)
      {
         throw new IllegalStateException(getIncorrectParentMsg());
      }
      
      // only build the components if there are currently no children
      int howManyKids = getChildren().size();
      if (howManyKids == 0)
      {
         generateItem(context, (UIPropertySheet)parent);
      }
      
      super.encodeBegin(context);
   }
   
   /**
    * @return Returns the display label
    */
   public String getDisplayLabel()
   {
      if (this.displayLabel == null)
      {
         ValueBinding vb = getValueBinding("display-label");
         if (vb != null)
         {
            this.displayLabel = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.displayLabel;
   }

   /**
    * @param displayLabel Sets the display label
    */
   public void setDisplayLabel(String displayLabel)
   {
      this.displayLabel = displayLabel;
   }

   /**
    * @return Returns the name
    */
   public String getName()
   {
      if (this.name == null)
      {
         ValueBinding vb = getValueBinding("name");
         if (vb != null)
         {
            this.name = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.name;
   }

   /**
    * @param name Sets the name
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the converter
    */
   public String getConverter()
   {
      if (this.converter == null)
      {
         ValueBinding vb = getValueBinding("converter");
         if (vb != null)
         {
            this.converter = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.converter;
   }

   /**
    * @param componentGenerator Sets the component generator
    */
   public void setComponentGenerator(String componentGenerator)
   {
      this.componentGenerator = componentGenerator;
   }
   
   /**
    * @return Returns the component generator
    */
   public String getComponentGenerator()
   {
      if (this.componentGenerator == null)
      {
         ValueBinding vb = getValueBinding("component-generator");
         if (vb != null)
         {
            this.componentGenerator = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.componentGenerator;
   }

   /**
    * @param converter Sets the converter
    */
   public void setConverter(String converter)
   {
      this.converter = converter;
   }

   /**
    * @return Returns whether the property is read only
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
    * @param readOnly Sets the read only flag for the component
    */
   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }
   
   /**
    * @return Determines whether the item should be ignored (not rendered)
    *         if the item can not be found
    */
   public boolean getIgnoreIfMissing()
   {
      if (this.ignoreIfMissing == null)
      {
         ValueBinding vb = getValueBinding("ignoreIfMissing");
         if (vb != null)
         {
            this.ignoreIfMissing = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      if (this.ignoreIfMissing == null)
      {
         this.ignoreIfMissing = Boolean.TRUE;
      }
      
      return this.ignoreIfMissing;
   }

   /**
    * @param ignoreIfMissing Sets the whether the item will be ignored
    *        if it can not be found
    */
   public void setIgnoreIfMissing(boolean ignoreIfMissing)
   {
      this.ignoreIfMissing = ignoreIfMissing;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.name = (String)values[1];
      this.displayLabel = (String)values[2];
      this.readOnly = (Boolean)values[3];
      this.converter = (String)values[4];
      this.componentGenerator = (String)values[5];
      this.resolvedDisplayLabel = (String)values[6];
      this.ignoreIfMissing = (Boolean)values[7];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[8];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.name;
      values[2] = this.displayLabel;
      values[3] = this.readOnly;
      values[4] = this.converter;
      values[5] = this.componentGenerator;
      values[6] = this.resolvedDisplayLabel;
      values[7] = this.ignoreIfMissing;
      return (values);
   }
   
   /**
    * Returns the resolved display label
    * 
    * @return The display label being used at runtime
    */
   public String getResolvedDisplayLabel()
   {
      return resolvedDisplayLabel;
   }
   
   /**
    * Generates the label and control for the item
    * 
    * @param context FacesContext
    * @param propSheet The property sheet that the item is a child of
    */
   protected abstract void generateItem(FacesContext context, UIPropertySheet propSheet)
      throws IOException;
   
   /**
    * Returns the message to use in the exception that is thrown if the component
    * is not nested inside a PropertySheet component
    * 
    * @return The message
    */
   protected abstract String getIncorrectParentMsg(); 
   
   /**
    * Generates a JSF OutputText component/renderer
    * 
    * @param context JSF context
    * @param propSheet The property sheet that the item is a child of
    * @param displayLabel The display label text
    */
   @SuppressWarnings("unchecked")
   protected void generateLabel(FacesContext context, UIPropertySheet propSheet, String displayLabel)
   {
      UIComponent label = FacesHelper.getComponentGenerator(context, 
            RepoConstants.GENERATOR_LABEL).generateAndAdd(context, propSheet, this);
      
      // remember the display label used (without the : separator)
      this.resolvedDisplayLabel = displayLabel;
      
      label.getAttributes().put("value", displayLabel + ":");
      
      if (logger.isDebugEnabled())
         logger.debug("Created label " + label.getClientId(context) + 
                      " for '" + this.name + "' and added it to component " + this);
   }
}
