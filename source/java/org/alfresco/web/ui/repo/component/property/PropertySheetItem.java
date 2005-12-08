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

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

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
         UIPropertySheet propSheet = (UIPropertySheet)parent;
         generateItem(context, propSheet.getNode(), propSheet.getVar());
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
         ValueBinding vb = getValueBinding("displayLabel");
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
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.name;
      values[2] = this.displayLabel;
      values[3] = this.readOnly;
      values[4] = this.converter;
      return (values);
   }
   
   /**
    * Generates the label and control for the item
    * 
    * @param context FacesContext
    * @param node The Node we are displaying the property sheet for
    * @param var The variable name used to store the Node in the session
    */
   protected abstract void generateItem(FacesContext context, Node node, String var)
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
    * @param displayLabel The display label text
    * @param parent The parent component for the label
    */
   protected void generateLabel(FacesContext context, String displayLabel)
   {
      UIOutput label = (UIOutput)context.getApplication().
                        createComponent("javax.faces.Output");
      label.setId(context.getViewRoot().createUniqueId());
      label.setRendererType("javax.faces.Text");
      label.setValue(displayLabel + ": ");
      this.getChildren().add(label);
      
      if (logger.isDebugEnabled())
         logger.debug("Created label " + label.getClientId(context) + 
                      " for '" + displayLabel + "' and added it to component " + this);
   }
}
