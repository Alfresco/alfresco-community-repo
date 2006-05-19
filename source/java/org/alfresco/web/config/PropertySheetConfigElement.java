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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents the config data for a property sheet
 * 
 * @author gavinc
 */
public class PropertySheetConfigElement extends ConfigElementAdapter
{
   // TODO: Currently this object just deals with properties and associations to show,
   //       in the future it will also deal with properties and associations to hide.
   
   public static final String CONFIG_ELEMENT_ID = "property-sheet";
   
   protected Map<String, ItemConfig> items = new LinkedHashMap<String, ItemConfig>(8, 10f);
   protected Map<String, ItemConfig> viewableItems = new LinkedHashMap<String, ItemConfig>(8, 10f);   
   protected Map<String, ItemConfig> editableItems = new LinkedHashMap<String, ItemConfig>(8, 10f);   
   
   /**
    * Default constructor
    */
   public PropertySheetConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public PropertySheetConfigElement(String name)
   {
      super(name);
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#getChildren()
    */
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the property-sheet config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      PropertySheetConfigElement combinedElement = new PropertySheetConfigElement();
      
      // add all the existing properties
      for (ItemConfig item : this.getItems().values())
      {
         combinedElement.addItem(item);
      }
      
      // add all the properties from the given element
      for (ItemConfig item : ((PropertySheetConfigElement)configElement).getItems().values())
      {
         combinedElement.addItem(item);
      }
      
      return combinedElement;
   }
   
   /**
    * Adds an item to show
    * 
    * @param itemConfig A pre-configured property or association config object
    */
   /*package*/ void addItem(ItemConfig itemConfig)
   {
      this.items.put(itemConfig.getName(), itemConfig);

      if (itemConfig.isShownInViewMode())
      {
         // add the item to the view list if it is editable
         this.viewableItems.put(itemConfig.getName(), itemConfig);
      }
      else
      {
         // if the item was added previously as viewable it should be removed
         if (viewableItems.containsKey(itemConfig.getName()))
         {
            this.viewableItems.remove(itemConfig.getName());
         }
      }
      
      if (itemConfig.isShownInEditMode())
      {
         // add the item to the edit list if it is editable
         this.editableItems.put(itemConfig.getName(), itemConfig);
      }
      else
      {
         // if the item was added previously as editable it should be removed
         if (editableItems.containsKey(itemConfig.getName()))
         {
            this.editableItems.remove(itemConfig.getName());
         }
      }
   }
   
   /**
    * Adds a property to show
    * 
    * @param name The name of the property
    * @param displayLabel Display label to use for the property
    * @param displayLabelId Display label message id to use for the property
    * @param readOnly Sets whether the property should be rendered as read only
    * @param converter The name of a converter to apply to the property control
    * @param inView Sets whether the property should be shown when the property 
    *        sheet is in view mode
    * @param inEdit Sets whether the property should be shown when the property 
    *        sheet is in edit mode
    * @param compGenerator The name of a bean that can be used as a component generator
    * @param ignoreIfMissing Sets whether the property should be rendered if it is not
    *        found in the data dictionary or the node itself
    */
   /*package*/ void addProperty(String name, String displayLabel, String displayLabelId, String readOnly, 
                                String converter, String inView, String inEdit, String compGenerator,
                                String ignoreIfMissing)
   {
      addItem(new PropertyConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inView, inEdit, compGenerator, ignoreIfMissing));
   }
   
   /**
    * Adds an association to show
    * 
    * @param name The name of the association
    * @param displayLabel Display label to use for the property
    * @param displayLabelId Display label message id to use for the property
    * @param readOnly Sets whether the association should be rendered as read only
    * @param converter The name of a converter to apply to the association control
    * @param inView Sets whether the property should be shown when the property 
    *        sheet is in view mode
    * @param inEdit Sets whether the property should be shown when the property 
    *        sheet is in edit mode
    * @param compGenerator The name of a bean that can be used as a component generator
    */
   /*package*/ void addAssociation(String name, String displayLabel, String displayLabelId, String readOnly, 
                                   String converter, String inView, String inEdit, String compGenerator)
   {
      addItem(new AssociationConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inView, inEdit, compGenerator));
   }
   
   /**
    * Adds a child association to show
    * 
    * @param name The name of the child association
    * @param displayLabel Display label to use for the property
    * @param displayLabelId Display label message id to use for the property
    * @param readOnly Sets whether the association should be rendered as read only
    * @param converter The name of a converter to apply to the association control
    * @param inView Sets whether the property should be shown when the property 
    *        sheet is in view mode
    * @param inEdit Sets whether the property should be shown when the property 
    *        sheet is in edit mode
    * @param compGenerator The name of a bean that can be used as a component generator
    */
   /*package*/ void addChildAssociation(String name, String displayLabel, String displayLabelId, String readOnly, 
                                        String converter, String inView, String inEdit, String compGenerator)
   {
      addItem(new ChildAssociationConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inView, inEdit, compGenerator));
   }
   
   /**
    * @return Returns a map of the all the items
    */
   public Map<String, ItemConfig> getItems()
   {
      return this.items;
   }
   
   /**
    * @return Returns a list of item names to display
    */
   public List<String> getItemNamesToShow()
   {
      List<String> propNames = new ArrayList<String>(this.viewableItems.size());
      
      for (String name : this.viewableItems.keySet())
      {
         propNames.add(name);
      }
      
      return propNames;
   }
   
   /**
    * @return Returns a map of the item names to show
    */
   public Map<String, ItemConfig> getItemsToShow()
   {
      return this.viewableItems;
   }
   
   /**
    * @return Returns a list of item names to display
    */
   public List<String> getEditableItemNamesToShow()
   {
      List<String> propNames = new ArrayList<String>(this.editableItems.size());
      
      for (String name : this.editableItems.keySet())
      {
         propNames.add(name);
      }
      
      return propNames;
   }
   
   /**
    * @return Returns a map of the item names to show
    */
   public Map<String, ItemConfig> getEditableItemsToShow()
   {
      return this.editableItems;
   }
   
   /**
    * Inner class to represent a configured property sheet item
    */
   public abstract class ItemConfig
   {
      private String name;
      private String displayLabel;
      private String displayLabelId;
      private String converter;
      private String componentGenerator;
      private boolean readOnly;
      private boolean showInViewMode = true;
      private boolean showInEditMode = true;
      private boolean ignoreIfMissing = true;
      
      public ItemConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inView, String inEdit, 
            String compGenerator, String ignoreIfMissing)
      {
         this.name = name;
         this.displayLabel = displayLabel;
         this.displayLabelId = displayLabelId;
         this.readOnly = readOnly;
         this.converter = converter;
         this.componentGenerator = compGenerator;
         
         if (inView != null)
         {
            this.showInViewMode = Boolean.parseBoolean(inView);
         }
         if (inEdit != null)
         {
            this.showInEditMode = Boolean.parseBoolean(inEdit);
         }
         if (ignoreIfMissing != null)
         {
            this.ignoreIfMissing = Boolean.parseBoolean(ignoreIfMissing);
         }
      }
      
      /**
       * @return The display label
       */
      public String getDisplayLabel()
      {
         return this.displayLabel;
      }
      
      /**
       * @return The display label message id
       */
      public String getDisplayLabelId()
      {
         return this.displayLabelId;
      }
      
      /**
       * @return The property name
       */
      public String getName()
      {
         return this.name;
      }
      
      /**
       * @return Determines whether the property is configured as read only
       */
      public boolean isReadOnly()
      {
         return this.readOnly;
      }
      
      /**
       * @return The converter id
       */
      public String getConverter()
      {
         return this.converter;
      }
      
      /**
       * @return true if the property should be shown when the property sheet is in view mode
       */
      public boolean isShownInViewMode()
      {
         return this.showInViewMode;
      }
      
      /**
       * @return true if the property should be shown when the property sheet is in edit mode
       */
      public boolean isShownInEditMode()
      {
         return this.showInEditMode;
      }
      
      /**
       * @return The name of a bean that generates a component to represent this item
       */
      public String getComponentGenerator()
      {
         return this.componentGenerator;
      }
      
      /**
       * @return Whether the property should be rendered if it is not found in the
       *         data dictionary or the node itself. 
       */
      public boolean getIgnoreIfMissing()
      {
         return this.ignoreIfMissing;
      }  
      
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" display-label=").append(this.displayLabel);
         buffer.append(" display-label-id=").append(this.displayLabelId);
         buffer.append(" converter=").append(this.converter);
         buffer.append(" read-only=").append(this.readOnly);
         buffer.append(" show-in-view-mode=").append(this.showInViewMode);
         buffer.append(" show-in-edit-mode=").append(this.showInEditMode);
         buffer.append(" ignore-if-missing=").append(this.ignoreIfMissing);
         buffer.append(" component-generator=").append(this.componentGenerator).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Inner class to represent a configured property
    */
   public class PropertyConfig extends ItemConfig
   {
      public PropertyConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inView, String inEdit, 
            String compGenerator, String ignoreIfMissing)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, 
               inView, inEdit, compGenerator, ignoreIfMissing);
      }
   }
   
   /**
    * Inner class to represent a configured association
    */
   public class AssociationConfig extends ItemConfig
   {
      public AssociationConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inView, String inEdit, 
            String compGenerator)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, 
               inView, inEdit, compGenerator, null);
      }
   }
   
   /**
    * Inner class to represent a configured child association
    */
   public class ChildAssociationConfig extends ItemConfig
   {
      public ChildAssociationConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inView, String inEdit, 
            String compGenerator)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, 
               inView, inEdit, compGenerator, null);
      }
   }
}
