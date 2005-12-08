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
package org.alfresco.web.ui.repo.tag.property;

import javax.faces.component.UIComponent;
import org.alfresco.web.ui.common.tag.BaseComponentTag;

/**
 * Tag to represent the combination of a PropertySheet component
 * and a Grid renderer
 * 
 * @author gavinc
 */
public class PropertySheetGridTag extends BaseComponentTag
{
   private String value;
   private String var;
   private String columns;
   private String externalConfig;
   private String configArea;
   private String readOnly;
   private String mode;
   private String labelStyleClass;
   private String cellpadding;
   private String cellspacing;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.PropertySheet";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      return "javax.faces.Grid";
   }
   
   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * @param var The var to set.
    */
   public void setVar(String var)
   {
      this.var = var;
   }

   /**
    * @param columns The columns to set.
    */
   public void setColumns(String columns)
   {
      this.columns = columns;
   }

   /**
    * @param externalConfig The externalConfig to set.
    */
   public void setExternalConfig(String externalConfig)
   {
      this.externalConfig = externalConfig;
   }
   
   /**
    * @param configArea Sets the named config area to use
    */
   public void setConfigArea(String configArea)
   {
      this.configArea = configArea;
   }

   /**
    * @param mode The mode, either "edit" or "view"
    */
   public void setMode(String mode)
   {
      this.mode = mode;
   }

   /**
    * @param readOnly The readOnly to set.
    */
   public void setReadOnly(String readOnly)
   {
      this.readOnly = readOnly;
   }
   
   /**
    * @param labelStyleClass Sets the style class for the label column
    */
   public void setLabelStyleClass(String labelStyleClass)
   {
      this.labelStyleClass = labelStyleClass;
   }

   /**
    * @param cellpadding Sets the cellpadding for the grid
    */
   public void setCellpadding(String cellpadding)
   {
      this.cellpadding = cellpadding;
   }

   /**
    * @param cellspacing Sets the cellspacing for the grid
    */
   public void setCellspacing(String cellspacing)
   {
      this.cellspacing = cellspacing;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "value", this.value);
      setStringProperty(component, "mode", this.mode);
      setStringProperty(component, "configArea", this.configArea);
      setStringStaticProperty(component, "var", this.var);
      setIntProperty(component, "columns", this.columns);
      setStringStaticProperty(component, "labelStyleClass", this.labelStyleClass);
      setBooleanProperty(component, "externalConfig", this.externalConfig);
      setBooleanProperty(component, "readOnly", this.readOnly);
      setStringStaticProperty(component, "cellpadding", this.cellpadding);
      setStringStaticProperty(component, "cellspacing", this.cellspacing);
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#release()
    */
   public void release()
   {
      this.value = null;
      this.var = null;
      this.columns = null;
      this.externalConfig = null;
      this.configArea = null;
      this.readOnly = null;
      this.mode = null;
      this.labelStyleClass = null;
      this.cellpadding = null;
      this.cellspacing = null;
      
      super.release();
   }
}
