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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;
import org.alfresco.config.element.GenericConfigElement;

/**
 * Custom config element that represents the config data for a property sheet
 * 
 * @author gavinc
 */
public class PropertySheetConfigElement extends ConfigElementAdapter
{
   // TODO: Currently this object just deals with properties and associations to show,
   //       in the future it will also deal with properties and associations to hide.
   
   private List<ItemConfig> items = new ArrayList<ItemConfig>();
   private List<ItemConfig> editableItems = new ArrayList<ItemConfig>();
   private Map<String, ItemConfig> itemsMap = new HashMap<String, ItemConfig>();
   private Map<String, ItemConfig> editableItemsMap = new HashMap<String, ItemConfig>();
   private List<String> itemNames = new ArrayList<String>();
   private List<String> editableItemNames = new ArrayList<String>();
   private boolean kidsPopulated = false;
   
   /**
    * Default constructor
    */
   public PropertySheetConfigElement()
   {
      super("property-sheet");
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
      // lazily build the list of generic config elements representing
      // the properties as the caller may not even call this method
      
      List<ConfigElement> kids = null;
      
      if (this.items.size() > 0)
      {
         if (this.kidsPopulated == false)
         {
            Iterator<ItemConfig> items = this.items.iterator();
            while (items.hasNext())
            {
               ItemConfig pc = items.next();
               GenericConfigElement ce = null;
               if (pc instanceof PropertyConfig)
               {
                  ce = new GenericConfigElement(PropertySheetElementReader.ELEMENT_SHOW_PROPERTY);
               }
               else if (pc instanceof AssociationConfig)
               {
                  ce = new GenericConfigElement(PropertySheetElementReader.ELEMENT_SHOW_ASSOC);
               }
               else
               {
                  ce = new GenericConfigElement(PropertySheetElementReader.ELEMENT_SHOW_CHILD_ASSOC);
               }
               
               ce.addAttribute(PropertySheetElementReader.ATTR_NAME, pc.getName());
               ce.addAttribute(PropertySheetElementReader.ATTR_DISPLAY_LABEL, pc.getDisplayLabel());
               ce.addAttribute(PropertySheetElementReader.ATTR_DISPLAY_LABEL_ID, pc.getDisplayLabelId());
               ce.addAttribute(PropertySheetElementReader.ATTR_READ_ONLY, Boolean.toString(pc.isReadOnly()));
               ce.addAttribute(PropertySheetElementReader.ATTR_CONVERTER, pc.getConverter());
               ce.addAttribute(PropertySheetElementReader.ATTR_SHOW_IN_EDIT_MODE, Boolean.toString(pc.isShownInEditMode()));
               this.children.add(ce);
            }
            
            this.kidsPopulated = true;
         }
         
         kids = super.getChildren();
      }
      
      return kids;
   }
   
   /**
    * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      PropertySheetConfigElement combined = new PropertySheetConfigElement();
      
      // add all the existing properties
      Iterator<ItemConfig> items = this.getItemsToShow().iterator();
      while (items.hasNext())
      {
         combined.addItem(items.next());
      }
      
      // add all the properties from the given element
      items = ((PropertySheetConfigElement)configElement).getItemsToShow().iterator();
      while (items.hasNext())
      {
         combined.addItem(items.next());
      }
      
      return combined;
   }
   
   /**
    * Adds an item to show
    * 
    * @param itemConfig A pre-configured property or association config object
    */
   /*package*/ void addItem(ItemConfig itemConfig)
   {
      if (this.itemsMap.containsKey(itemConfig.getName()) == false)
      {
         this.items.add(itemConfig);
         this.itemsMap.put(itemConfig.getName(), itemConfig);
         this.itemNames.add(itemConfig.getName());
         
         // also add to the edit items collections if necessary
         if (itemConfig.isShownInEditMode())
         {
            this.editableItems.add(itemConfig);
            this.editableItemsMap.put(itemConfig.getName(), itemConfig);
            this.editableItemNames.add(itemConfig.getName());
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
    * @param inEdit Sets whether the property should be shown when the property 
    *        sheet is in edit mode
    */
   /*package*/ void addProperty(String name, String displayLabel, String displayLabelId, String readOnly, 
                                String converter, String inEdit)
   {
      addItem(new PropertyConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inEdit));
   }
   
   /**
    * Adds an association to show
    * 
    * @param name The name of the association
    * @param displayLabel Display label to use for the property
    * @param displayLabelId Display label message id to use for the property
    * @param readOnly Sets whether the association should be rendered as read only
    * @param converter The name of a converter to apply to the association control
    * @param inEdit Sets whether the property should be shown when the property 
    *        sheet is in edit mode
    */
   /*package*/ void addAssociation(String name, String displayLabel, String displayLabelId, String readOnly, 
                                   String converter, String inEdit)
   {
      addItem(new AssociationConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inEdit));
   }
   
   /**
    * Adds a child association to show
    * 
    * @param name The name of the child association
    * @param displayLabel Display label to use for the property
    * @param displayLabelId Display label message id to use for the property
    * @param readOnly Sets whether the association should be rendered as read only
    * @param converter The name of a converter to apply to the association control
    */
   /*package*/ void addChildAssociation(String name, String displayLabel, String displayLabelId, String readOnly, 
                                        String converter, String inEdit)
   {
      addItem(new ChildAssociationConfig(name, displayLabel, displayLabelId, Boolean.parseBoolean(readOnly), 
            converter, inEdit));
   }
   
   /**
    * @return Returns a list of item names to display
    */
   public List<String> getItemNamesToShow()
   {
      return this.itemNames;
   }
   
   /**
    * @return Returns the list of item config objects that represent those to display
    */
   public List<ItemConfig> getItemsToShow()
   {
      return this.items;
   }
   
   /**
    * @return Returns a map of the item names to show
    */
   public Map<String, ItemConfig> getItemsMapToShow()
   {
      return this.itemsMap;
   }
   
   /**
    * @return Returns a list of item names to display
    */
   public List<String> getEditableItemNamesToShow()
   {
      return this.editableItemNames;
   }
   
   /**
    * @return Returns the list of item config objects that represent those to display
    */
   public List<ItemConfig> getEditableItemsToShow()
   {
      return this.editableItems;
   }
   
   /**
    * @return Returns a map of the item names to show
    */
   public Map<String, ItemConfig> getEditableItemsMapToShow()
   {
      return this.editableItemsMap;
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
      private boolean readOnly;
      private boolean showInEditMode = true;
      
      public ItemConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inEdit)
      {
         this.name = name;
         this.displayLabel = displayLabel;
         this.displayLabelId = displayLabelId;
         this.readOnly = readOnly;
         this.converter = converter;
         
         if (inEdit != null)
         {
            this.showInEditMode = Boolean.parseBoolean(inEdit);
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
      
      public String getConverter()
      {
         return this.converter;
      }
      
      public boolean isShownInEditMode()
      {
         return this.showInEditMode;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         StringBuilder buffer = new StringBuilder(super.toString());
         buffer.append(" (name=").append(this.name);
         buffer.append(" displaylabel=").append(this.displayLabel);
         buffer.append(" displaylabelId=").append(this.displayLabelId);
         buffer.append(" converter=").append(this.converter);
         buffer.append(" readonly=").append(this.readOnly);
         buffer.append(" showInEditMode=").append(this.showInEditMode).append(")");
         return buffer.toString();
      }
   }
   
   /**
    * Inner class to represent a configured property
    */
   public class PropertyConfig extends ItemConfig
   {
      public PropertyConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inEdit)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, inEdit);
      }
   }
   
   /**
    * Inner class to represent a configured association
    */
   public class AssociationConfig extends ItemConfig
   {
      public AssociationConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inEdit)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, inEdit);
      }
   }
   
   /**
    * Inner class to represent a configured child association
    */
   public class ChildAssociationConfig extends ItemConfig
   {
      public ChildAssociationConfig(String name, String displayLabel, String displayLabelId, 
            boolean readOnly, String converter, String inEdit)
      {
         super(name, displayLabel, displayLabelId, readOnly, converter, inEdit);
      }
   }
}
